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

package uk.ac.gda.beamline.synoptics.composites;

import static java.util.stream.Collectors.toList;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.observable.IObserver;
import uk.ac.gda.beamline.synoptics.api.PlottingFileProcessor;
import uk.ac.gda.beamline.synoptics.events.DirectoryChangeEvent;
import uk.ac.gda.beamline.synoptics.events.LatestFilenameEvent;
import uk.ac.gda.beamline.synoptics.utils.DataDirectoryMonitor;

/**
 * Controller used by {@link DetectorFileSelection} composite to display detector files as they
 * are collected.
 */
class LatestFileController {
	private static final Logger logger = LoggerFactory.getLogger(LatestFileController.class);

	/** The view displaying the selected file to the user */
	private LatestFileDisplay display;

	/** A list of all files matching the current filter */
	private List<String> files = new ArrayList<>();

	/** A list of all files that match the current filter */
	private List<String> allFiles = new ArrayList<>();

	/** Map of available file filters keyed by display name */
	private Map<String, Predicate<String>> fileFilters = new LinkedHashMap<>();

	/** The currently enabled filter */
	private Predicate<String> currentFilter = p -> true;

	/** The index of the currently selected file in the list of eligible files */
	private int currentIndex = -1;

	/** Whether the view is automatically plotting new data */
	private boolean updating = true;

	/** Whether the plot is being cleared before new data is plotted */
	private boolean clearing = true;

	/** Source of files. Sends updates for new files and directory changes */
	private DataDirectoryMonitor fileProvider;

	/** The handler to do something with the selected file */
	private PlottingFileProcessor processor;

	/** Shutdown hook to be called when this controller is no longer required */
	private Runnable shutdown;

	LatestFileController(LatestFileDisplay view, DataDirectoryMonitor fileNotifier, Map<String, Predicate<String>> filters, boolean updatingAtStart,
			boolean clearingAtStart, PlottingFileProcessor plotter) {
		fileFilters = filters;
		fileProvider = fileNotifier;
		clearing = clearingAtStart;
		updating = updatingAtStart;
		processor = plotter;

		IObserver update = (source, arg) -> this.updateFiles(arg);
		fileProvider.addIObserver(update);
		shutdown = () -> fileProvider.deleteIObserver(update);
		display = view;
	}

	/** Call after view has been fully created to ensure that its initial state is correct */
	void initialise() {
		display.setAutoUpdate(updating);
		display.setClearing(clearing);
		resetAllFiles();
	}

	/** Handle a user entered string as a new index. If index is out of range, reset the view */
	void setIndex(String indexString) {
		logger.debug("User input index '{}'", indexString);
		try {
			int index = Integer.parseInt(indexString);
			if (index <= 0 || index > files.size()) { // 1-indexed for user
				logger.error("Given index out of range");
			} else {
				currentIndex = index - 1; //but 0-indexed here
			}
		} catch (NumberFormatException e) {
			logger.error("Could not parse the given index: {}", e.getMessage());
		}
		updateFile();
	}

	/**
	 * Handle a string passed in by the user as a file path
	 * <p>
	 * Makes a best effort to resolve a valid path even if the given path does not exist.
	 * @param fileString The string as entered by the user
	 */
	void setFilePath(String fileString) {
		logger.debug("User set file path '{}'", fileString);

		Path selected = Paths.get(fileString);
		// Check if path is already valid
		if (!selected.toFile().exists()) {
			// If it's not, check if a file with a matching name is known (possibly in a different
			// subdirectory or just filename was given)
			String fileName = selected.getFileName().toString();
			logger.debug("File doesn't exist, trying to match file name: {}", fileName);
			Path possible = Paths.get(allFiles.stream()
					.filter(f -> f.endsWith(fileName))
					.findFirst()
					.orElse(fileString));
			if (!possible.toFile().exists()) {
				logger.debug("Can't find in unfiltered list. Resetting file path");
				updateFile();
				return;
			}
			fileString = possible.toString();
			display.setFilePath(fileString);
			logger.debug("Found: {}", fileString);
		}

		// At this point file must exist (ignoring race condition of it being moved/deleted)
		int index = files.indexOf(fileString);
		if (index < 0) {
			display.setFileNumber(-1, files.size());
			handleSelectedFile(fileString);
		} else {
			currentIndex = index;
			updateFile();
		}

	}

	/** Handle user selecting next file button */
	void nextFile() {
		logger.trace("User selected next file: {} -> {}", currentIndex, currentIndex + 1);
		if (currentIndex >= files.size() -1) {
			logger.warn("Trying to go to next file from last index");
			return;
		}
		currentIndex++;
		updateFile();
	}

	/** Handle user selecting previous file button */
	void previousFile() {
		logger.trace("User selected previous file: {} -> {}", currentIndex, currentIndex - 1);
		if (currentIndex <= 0) {
			logger.warn("Trying to go to previous file from index 0");
			return;
		}
		currentIndex--;
		updateFile();
	}

	/** Handle user selecting first file button */
	void firstFile() {
		logger.trace("User selected first file: {} -> 0", currentIndex);
		currentIndex = 0;
		updateFile();

	}

	/** Handle user selecting latest file button */
	void latestFile() {
		logger.trace("User selected latest file: {} -> {}", currentIndex, files.size() - 1);
		selectLatestFile();
	}

	/**
	 * Handle the pause/resume button being pressed.
	 * <p>
	 * Needs to be called from the GUI thread as it accesses the state of the widget.
	 */
	void pauseResume() {
		logger.trace("User pressed pause/resume: {} -> {}", updating, !updating);
		updating = !updating;
		display.setAutoUpdate(updating);
	}

	/**
	 * Handle the clear plot check box being selected.
	 * <p>
	 * If checkbox is checked, new plots should clear any exist plotted data before updating.
	 * Needs to be called in the GUI thread as state of the widget is required.
	 */
	void toggleClear() {
		logger.trace("User toggled clear plot checkbox: {} -> {}", clearing, !clearing);
		clearing = !clearing;
		display.setClearing(clearing);
	}

	/** Handle user selecting filter */
	void setFilter(String filterString) {
		logger.trace("User selected filter '{}' -> '{}'", currentFilter, filterString);
		currentFilter = fileFilters.getOrDefault(filterString, currentFilter);
		updateFilteredFiles();
	}

	/** Get the available filters */
	String[] getFilterKeys() {
		return fileFilters.keySet().toArray(new String[] {});
	}

	/** Perform any clean up required when the controlled view is disposed of */
	void shutdown() {
		logger.info("Shutting down");
		if (shutdown != null) {
			try {
				shutdown.run();
			} catch (Exception e) {
				logger.error("Couldn't run shutdown hook");
			}
		}
	}

	/** Set the current index to the latest and refresh the view */
	private void selectLatestFile() {
		currentIndex = files.size() - 1;
		updateFile();
	}

	/** Update the file shown on the display based on the current index */
	private void updateFile() {
		display.setFileNumber(currentIndex + 1, files.size());
		if (currentIndex >= 0 && currentIndex < files.size()) {
			String path = files.get(currentIndex);
			display.setFilePath(path);
			handleSelectedFile(path);
		} else {
			display.setFilePath("No files found");
		}
	}

	/** Reset all file lists known to this controller. Used when visit is changed */
	private void resetAllFiles() {
		allFiles = fileProvider.getDataFilesCollected().stream()
				.map(Path::toString)
				.collect(toList());
		updateFilteredFiles();
	}

	/** Reset the list of filtered files. Used when visit or filter is changed */
	private void updateFilteredFiles() {
		files = allFiles.stream()
				.filter(currentFilter)
				.collect(toList());
		selectLatestFile();
	}

	/**
	 * Handle update from file provider notifying update to files
	 * <p>
	 * Update can be either a new file or a notification that the visit has changed
	 * and all files should be refreshed.
	 * */
	private void updateFiles(Object event) {
		if (event instanceof LatestFilenameEvent) {
			LatestFilenameEvent evt = (LatestFilenameEvent) event;
			logger.debug("New file: {}", evt.getFilename());
			addNewFile(evt.getFilename());
		} else if (event instanceof DirectoryChangeEvent) {
			DirectoryChangeEvent evt = (DirectoryChangeEvent) event;
			logger.debug("New directory: {}", evt.getNewDirectory());
			resetAllFiles();
		}
	}

	/** Add a new file to the lists of files this controller maintains */
	private void addNewFile(String filepath) {
		allFiles.add(filepath);
		if (currentFilter.test(filepath)) {
			files.add(filepath);
			if (updating) {
				selectLatestFile();
			} else {
				updateFile();
			}
		}
	}

	/**
	 * Perform any processes required when a new file is selected.
	 * <p>
	 * Either from automatically updating when a new file is created or from a user
	 * selecting a new file from the display.
	 * @param fileString The path the file to handle
	 */
	private void handleSelectedFile(String fileString) {
		if (processor != null) {
			logger.debug("Processing file: {}", fileString);
			processor.setNewPlot(clearing);
			processor.processFile(fileString);
		}
	}
}