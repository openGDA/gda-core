/*-
 * Copyright © 2018 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.gda.beamline.synoptics.utils;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.data.PathConstructor;
import gda.data.metadata.GDAMetadataProvider;
import gda.data.metadata.MetadataEntry;
import gda.factory.Configurable;
import gda.factory.FactoryException;
import gda.factory.Finder;
import gda.observable.IObservable;
import gda.observable.IObserver;
import gda.observable.ObservableComponent;
import uk.ac.gda.beamline.synoptics.events.DirectoryChangeEvent;
import uk.ac.gda.beamline.synoptics.events.LatestFilenameEvent;

public class NewFileListener implements DataDirectoryMonitor, IObserver, Configurable {

	private static final Logger logger = LoggerFactory.getLogger(NewFileListener.class);

	/** Handler for observers of this class */
	private final ObservableComponent obsComp = new ObservableComponent();

	/** Observable to provide updates when new data files are created */
	private IObservable newFileProvider;

	/** Root directory for all data files. Ignore files created elsewhere */
	private Path dataDirectory;

	/** Private cache to save file system look ups of file creation times */
	private Map<Path, FileTime> fileTimeCache = new HashMap<>();

	/**
	 * The set of data files collected by GDA in the given directory
	 * <p>
	 * Maintains order by creation time
	 */
	private Set<Path> dataFiles = new TreeSet<>((a, b) ->  {
		try {
			FileTime t1 = fileTimeCache.computeIfAbsent(a, NewFileListener::creationTime);
			FileTime t2 = fileTimeCache.computeIfAbsent(b, NewFileListener::creationTime);
			return t1.compareTo(t2);
		} catch (IllegalStateException e) {
			return a.compareTo(b);
		}
	});

	/** Set of regexes to filter files by */
	private Set<Predicate<String>> ignoredFiles = new HashSet<>();

	private String fileProviderName;

	private boolean configured;

	@Override
	public List<Path> getDataFilesCollected() {
		return new ArrayList<>(dataFiles);
	}

	@Override
	public void update(Object source, Object arg) {
		if (source instanceof MetadataEntry && ((MetadataEntry) source).getName().equals("visit")) {
			updateDataDirectory();
		} else if (arg instanceof String[]) {
			String[] files = (String[])arg;
			for (String file : files) {
				if (file == null) continue; // Should never happen
				Path newPath = Paths.get(file);
				if (dataDirectory != null && !newPath.startsWith(dataDirectory)) {
					// Ignore this file
					logger.debug("Ignoring new file out side data directory ({} not in {})", newPath, dataDirectory);
					return;
				}
				if (includeFile(newPath)) {
					dataFiles.add(newPath);
					obsComp.notifyIObservers(this, new LatestFilenameEvent(dataFiles.size() - 1, newPath.toString()));
				} else {
					logger.debug("Ignoring new file: {}", newPath);
				}
			}
		}
	}
	public void setFileProviderName(String fileProvider) {
		fileProviderName = fileProvider;
	}

	/**
	 * Set the filters to use when adding new files
	 * <p>
	 * If a new file name matches any of the set regexes, it is ignored.
	 * @param ignorePatterns Array of regexes used to filter new files.
	 */
	public void setIgnoredFiles(String... ignorePatterns) {
		ignoredFiles = Arrays.stream(ignorePatterns)
				.map(Pattern::compile)
				.map(Pattern::asPredicate)
				.collect(toSet());
	}

	@Override
	public void configure() throws FactoryException {
		if (newFileProvider != null) {
			newFileProvider.deleteIObserver(this);
		}
		IObservable fileProvider = Finder.getInstance().find(fileProviderName);
		newFileProvider = fileProvider;
		if (newFileProvider != null) {
			newFileProvider.addIObserver(this);
		}

		updateDataDirectory();

		// Listen for visit changes
		GDAMetadataProvider.getInstance().addIObserver(this);
		configured = true;
	}

	/**
	 * Refresh the stored set of collected files
	 * <p>
	 * Checks to see if the data directory has changed and repopulate it with the new files
	 * if it has. Adds all files in the current datadirectory except those ignored by the preset filters.
	 */
	private void updateDataDirectory() {
		Path newDataDirectory = Paths.get(PathConstructor.getVisitDirectory());
		if (!newDataDirectory.equals(dataDirectory)) {
			dataDirectory = newDataDirectory;
			dataFiles.clear();
			fileTimeCache.clear();
			try {
				dataFiles.addAll(Files.walk(dataDirectory)
						.filter(Files::isRegularFile)
						.filter(this::includeFile)
						.collect(toList()));
				obsComp.notifyIObservers(this, new DirectoryChangeEvent(dataDirectory.toString()));
			} catch (IOException e) {
				logger.error("Could not read contents of data directory {}. Some files may be missing", dataDirectory, e);
			}
		}
	}

	/** Check if a file should be included in the set of collected files.
	 *
	 * @param file The file to check
	 * @return False if the filename matches any of the set {@link #ignoredFiles}
	 */
	private boolean includeFile(Path file) {
		return ignoredFiles.stream().noneMatch(filter -> filter.test(file.getFileName().toString()));
	}

	@Override
	public void addIObserver(IObserver observer) {
		obsComp.addIObserver(observer);
	}

	@Override
	public void deleteIObserver(IObserver observer) {
		obsComp.deleteIObserver(observer);
	}

	@Override
	public void deleteIObservers() {
		obsComp.deleteIObservers();
	}

	@Override
	public boolean isConfigured() {
		return configured;
	}

	/** Get the file creation time of a file */
	private static FileTime creationTime(Path file) {
		try {
			return Files.getLastModifiedTime(file);
		} catch (IOException e) {
			throw new IllegalStateException("Couldn't read creation time of file " + file.toString());
		}
	}
}


