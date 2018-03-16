/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

/*
 * Copyright (c) 2008, 2010, Oracle and/or its affiliates. All rights reserved. Redistribution and use in source and
 * binary forms, with or without modification, are permitted provided that the following conditions are met: -
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer. - Redistributions in binary form must reproduce the above copyright notice, this list of conditions and
 * the following disclaimer in the documentation and/or other materials provided with the distribution. - Neither the
 * name of Oracle nor the names of its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.data.PathConstructor;
import gda.factory.ConfigurableBase;
import gda.factory.FactoryException;
import gda.observable.IObservable;
import gda.observable.IObserver;
import gda.observable.ObservableComponent;
import uk.ac.gda.beamline.synoptics.events.LatestFilenameEvent;


/**
 * Watch a directory (or tree) for changes to files and scan the directory for a file list.
 */

public class WatchDirectory extends ConfigurableBase implements IObservable {

	public static final Logger logger = LoggerFactory.getLogger(WatchDirectory.class);
	private final WatchService watcher;
	private final Map<WatchKey, Path> keys;
	private boolean recursive;
	private String directory = null;
	private boolean trace = false;
	private String[] filenameExtensions = new String[] {};
	private boolean server = false;
	private String[] excludedDirectory = new String[] {};
	private ObservableComponent observableComponent = new ObservableComponent();

	@SuppressWarnings("unchecked")
	static <T> WatchEvent<T> cast(WatchEvent<?> event) {
		return (WatchEvent<T>) event;
	}

	/**
	 * Register the given directory with the WatchService
	 */
	private void register(Path dir) throws IOException {
		WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
		if (trace) {
			Path prev = keys.get(key);
			if (prev == null) {
				logger.info("register: {}", dir);
			} else {
				if (!dir.equals(prev)) {
					logger.info("update: {} -> {}", prev, dir);
				}
			}
		}
		keys.put(key, dir);
	}

	/**
	 * Register the given directory, and all its sub-directories, with the WatchService.
	 */
	private void registerAll(final Path start) throws IOException {
		// register directory and sub-directories
		Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				if (excludedDirectory.length > 0) {
					// don't register excluded directories
					for (String each : excludedDirectory) {
						if (dir.endsWith(each)) {
							return FileVisitResult.CONTINUE;
						}
					}
				}
				register(dir);
				return FileVisitResult.CONTINUE;
			}
		});
	}

	List<Path> dataFileCollected = new ArrayList<>();
	private Path dir;

	public List<Path> getDataFileCollected() {
		return dataFileCollected;
	}

	public void setDataFileCollected(List<Path> dataFileCollected) {
		this.dataFileCollected = dataFileCollected;
	}

	/**
	 * add all data files collected so far in the directory and its sub-directory to a array list which can be accessed
	 * using {@link #getDataFileCollected()}
	 *
	 * @param start
	 * @throws IOException
	 */
	private void dataCollectedSoFar(Path start) throws IOException {
		Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				if (FilenameUtils.isExtension(file.toString(), getFilenameExtensions())) {
					dataFileCollected.add(file);
//					System.out.format("%s: added to the data file list", file);
					logger.info("{}: added to the data file list", file);
				}
				return FileVisitResult.CONTINUE;
			}
		});
	}

	/**
	 * add only the data files collected in the given directory to the array list which can be accessed using
	 * {@link #getDataFileCollected()}
	 *
	 * @param dir
	 * @throws IOException
	 */
	private void dataFilesInDirectory(Path dir) throws IOException {
		Files.walkFileTree(dir, EnumSet.noneOf(FileVisitOption.class), 0, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				if (FilenameUtils.isExtension(file.toString(), getFilenameExtensions())) {
					dataFileCollected.add(file);
//					System.out.format("%s: added to the data file list", file);
					logger.info("{}: added to the data file list", file);
				}
				return FileVisitResult.CONTINUE;
			}
		});
	}
	/**
	 * create watch service.
	 * @throws IOException
	 */
	public WatchDirectory() throws IOException {
		this.watcher = FileSystems.getDefault().newWatchService();
		this.keys = new HashMap<>();
	}
	/**
	 * initialise the watcher object - scan the specified directory for initial state and update any observer the latest filename index and value.
	 */
	@Override
	public void configure() throws FactoryException {
		if (!isConfigured()) {
			if (directory == null || directory.isEmpty()) {
				if (isServer()) {
					dir = Paths.get(PathConstructor.createFromDefaultProperty());
				} else {
					dir = Paths.get(PathConstructor.createFromRCPProperties());
				}
			} else {
				dir = Paths.get(directory);
			}
			if (isRecursive()) {
				logger.info("Scanning {} ...", dir);
				try {
					registerAll(dir);
				} catch (IOException e) {
					logger.error("Failed to register directory " + dir, e);
					throw new FactoryException("Failed to register directory " + dir, e);
				}
				try {
					dataCollectedSoFar(dir);
				} catch (IOException e) {
					logger.error("Failed to add data collected so far to the list", e);
					throw new FactoryException("Failed to add files in directory " + dir + " to the data file list", e);
				}
				logger.info("Scanning Done.");
			} else {
				try {
					register(dir);
				} catch (IOException e) {
					logger.error("Failed to register directory " + dir, e);
					throw new FactoryException("Failed to register directory " + dir, e);
				}
				try {
					dataFilesInDirectory(dir);
				} catch (IOException e) {
					logger.error("Failed to add collected data to list", e);
					throw new FactoryException("Failed to add files in directory " + dir + " to the data file list", e);
				}
			}

			// enable trace after initial registration
			this.trace = true;
			setConfigured(true);
			notifyIObservers();
			//kick start the watcher event processing.
			Thread processEventThread=new Thread(new Runnable() {

				@Override
				public void run() {
					processEvents();
				}
			}, "DirectoryWatchEvent");
			processEventThread.start();
		}
	}

	private void notifyIObservers() {
		String latestFilename="Waiting ...";
		int numberOfDataFileCollected=0;
		if (!dataFileCollected.isEmpty()) {
			numberOfDataFileCollected=dataFileCollected.size();
			latestFilename=dataFileCollected.get(numberOfDataFileCollected-1).toString();
		}
		observableComponent.notifyIObservers(this, new LatestFilenameEvent(numberOfDataFileCollected-1,latestFilename ));
	}

	/**
	 * Creates a WatchService and registers the given directory
	 */
	public WatchDirectory(Path dir, boolean recursive) throws IOException {
		this.watcher = FileSystems.getDefault().newWatchService();
		this.keys = new HashMap<>();
		this.setRecursive(recursive);

		if (recursive) {
			logger.info("Scanning {} ...", dir);
			registerAll(dir);
			dataCollectedSoFar(dir);
			logger.info("Scanning Done.");
		} else {
			register(dir);
			dataFilesInDirectory(dir);
		}

		// enable trace after initial registration
		this.trace = true;
	}

	/**
	 * Creates a WatchService and registers the given directory
	 */
	public WatchDirectory(String directory, boolean recursive) throws IOException {
		this(Paths.get(directory), recursive);
	}

	/**
	 * Process all events for keys queued to the watcher
	 */
	void processEvents() {
		for (;;) {

			// wait for key to be signalled
			WatchKey key;
			try {
				key = watcher.take();
			} catch (InterruptedException x) {
				return;
			}

			Path dir = keys.get(key);
			if (dir == null) {
				logger.error("WatchKey not recognized!!");
				continue;
			}

			for (WatchEvent<?> event : key.pollEvents()) {
				WatchEvent.Kind kind = event.kind();

				// TBD - provide example of how OVERFLOW event is handled
				if (kind == OVERFLOW) {
					continue;
				}

				// Context for directory entry event is the file name of entry
				WatchEvent<Path> ev = cast(event);
				Path name = ev.context();
				Path child = dir.resolve(name);

				// print out event
				logger.info("{}: {}", event.kind().name(), child);

				if (isRecursive()) {
					if (kind == ENTRY_CREATE) {
						// if directory is created, and watching recursively, then
						// register it and its sub-directories
						try {
							if (Files.isDirectory(child, NOFOLLOW_LINKS)) {
								registerAll(child);
							} else {
								if (FilenameUtils.isExtension(child.toString(), getFilenameExtensions())) {
									dataFileCollected.add(child);
//									System.out.format("%s: added to the data file list", child);
									logger.info("{}: added to the data file list", child);
									notifyIObservers();
								}
							}
						} catch (IOException x) {
							// ignore to keep sample readable
						}
					}
				} else {
					if (kind == ENTRY_CREATE) {
						// if a file is created, not watching recursively then add it to data file list
						if (!Files.isDirectory(child, NOFOLLOW_LINKS)) {
							if (FilenameUtils.isExtension(child.toString(), getFilenameExtensions())) {
								dataFileCollected.add(child);
//								System.out.format("%s: added to the data file list", child);
								notifyIObservers();
								logger.info("{}: added to the data file list", child);
							}
						}
					}
				}
				if (kind == ENTRY_DELETE) {
					if (!Files.isDirectory(child, NOFOLLOW_LINKS)) {
						if (FilenameUtils.isExtension(child.toString(), getFilenameExtensions())) {
							dataFileCollected.remove(child);
//							System.out.format("%s: added to the data file list", child);
							notifyIObservers();
							logger.info("{}: removed from the data file list", child);
						}
					}
				}
			}

			// reset key and remove from set if directory no longer accessible
			boolean valid = key.reset();
			if (!valid) {
				keys.remove(key);

				// all directories are inaccessible
				if (keys.isEmpty()) {
					break;
				}
			}
		}
	}

	static void usage() {
		System.err.println("usage: java WatchDir [-r] dir");
		System.exit(-1);
	}

	public static void main(String[] args) throws IOException {
		// parse arguments
		if (args.length == 0 || args.length > 2)
			usage();
		boolean recursive = false;
		int dirArg = 0;
		if (args[0].equals("-r")) {
			if (args.length < 2)
				usage();
			recursive = true;
			dirArg++;
		}

		// register directory and process its events
		Path dir = Paths.get(args[dirArg]);
		WatchDirectory dirWatcher = new WatchDirectory();
		dirWatcher.setDirectory(dir.toString());
		dirWatcher.setFilenameExtensions(new String[] {"dat","tif","nxs"});
		dirWatcher.setRecursive(recursive);
		try {
			dirWatcher.configure();
		} catch (FactoryException e) {
			logger.error("Failed to configure directory watch", e);
		}
//		dirWatcher.processEvents();
	}

	public boolean isRecursive() {
		return recursive;
	}

	public void setRecursive(boolean recursive) {
		this.recursive = recursive;
	}

	public String getDirectory() {
		return directory;
	}

	public void setDirectory(String directory) {
		this.directory = directory;
	}

	public String[] getFilenameExtensions() {
		return filenameExtensions;
	}

	public void setFilenameExtensions(String[] filenameExtensions) {
		this.filenameExtensions = filenameExtensions;
	}

	public boolean isServer() {
		return server;
	}

	public void setServer(boolean server) {
		this.server = server;
	}

	public String[] getExcludedDirectory() {
		return excludedDirectory;
	}

	public void setExcludedDirectory(String[] excludedDirectory) {
		this.excludedDirectory = excludedDirectory;
	}

	@Override
	public void addIObserver(IObserver observer) {
		observableComponent.addIObserver(observer);
	}

	@Override
	public void deleteIObserver(IObserver observer) {
		observableComponent.deleteIObserver(observer);
	}

	@Override
	public void deleteIObservers() {
		observableComponent.deleteIObservers();
	}

}
