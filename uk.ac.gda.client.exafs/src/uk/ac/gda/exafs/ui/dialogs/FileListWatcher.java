/*-
 * Copyright © 2023 Diamond Light Source Ltd.
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

/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package uk.ac.gda.exafs.ui.dialogs;

import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.observable.IObservable;
import gda.observable.IObserver;
import gda.observable.ObservableComponent;
import uk.ac.diamond.daq.concurrent.Async;

/**
 * Class to maintain a map of XML files storing both the name and type of file.
 * <li> key = file type (i.e. root name of XML object), value = list of names of files of the type
 * <li> A {@link WatchService} is used to update the map when changes occur on disc.
 */
public class FileListWatcher implements IObservable {
	private static final Logger logger = LoggerFactory.getLogger(FileListWatcher.class);

	private ObservableComponent observableComponent = new ObservableComponent();

	public enum Event{LIST_UDDATE_START, LIST_UPDATE_PROGRESSS}

	private List<String> allFileNames = Collections.emptyList();
	private String fileNameExtension = ".xml";

	/**
	 * Map containing the list of files grouped by type (Key = file type, Value = list of files)
	 */
	private Map<String, List<String>> fileTypes = Collections.emptyMap();
	private WatchService watchService;
	private volatile boolean watchServiceRunning = false;

	private String directoryToWatch;

	/**
	 * Start a WatchService to monitor the contents of the given directory
	 * @param dir
	 */
	public void startWatchService(String dir) {
		stopWatchService();
		createWatchService(dir);
		startWatchServiceThread();
	}

	public void stopWatchService() {
		if (watchService == null) {
			return;
		}
		try {
			logger.debug("Stopping WatchService");
			watchService.close();
			while(watchServiceRunning) {
				logger.debug("Waiting for WatchService event thread to stop");
				Thread.sleep(500);
			}
		} catch (IOException | InterruptedException e) {
			logger.warn("Problem stopping WatchService", e);
		}
	}

	private void createWatchService(String directory) {
		try {
			directoryToWatch = directory;
			logger.debug("Creating WatchService for files in : {}", directoryToWatch);
			Path pathToDir = Paths.get(directory);
			watchService = FileSystems.getDefault().newWatchService();
			pathToDir.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
		} catch (IOException e) {
			logger.error("Problem creating WatchService", e);
		}
	}

	private void startWatchServiceThread() {
		if (watchService == null) {
			return;
		}
		Async.execute(this::monitorWatchService);
	}

	private void monitorWatchService() {
		watchServiceRunning = true;
		try {
			logger.debug("Starting WatchService event thread");
			WatchKey key;
			while ((key = watchService.take()) != null) {
				boolean runUpdate = false;
				for (var event : key.pollEvents()) {
					logger.debug("File wath event : kind = {}, file = {}", event.kind(), event.context() + ".");

					// only update if file that has changed ends with the required filename extension
					if (event.context() instanceof Path p && p.toString().endsWith(fileNameExtension)) {
						runUpdate = true;
					}
				}
				if (runUpdate) {
					updateFileTypeMap();
				}
				key.reset();
			}
		} catch (ClosedWatchServiceException e) {
			logger.debug("WatchService closed");
		} catch(IOException | InterruptedException e) {
			logger.warn("Exception during WatchService event thread", e);
		} finally {
			logger.debug("WatchService event thread finished");
			watchServiceRunning = false;
		}
	}

	public synchronized void updateFileTypeMap() throws IOException {
		//List of all XML files in xmlDirectory
		logger.info("Updating file types in : {}", directoryToWatch);
		allFileNames = SpreadsheetViewHelperClasses.getListOfFilesMatchingExtension(directoryToWatch, fileNameExtension);

		logger.info("{} {} files found", allFileNames.size(), fileNameExtension);
		notifyObservers(Event.LIST_UDDATE_START);

		// Create map from filename to type of file
		fileTypes = new LinkedHashMap<>();
		for(String f : allFileNames) {
			String type = SpreadsheetViewHelperClasses.getFirstXmlElementNameFromFile(f);
			fileTypes.computeIfAbsent(type, k -> new ArrayList<>()).add(f);
			notifyObservers(Event.LIST_UPDATE_PROGRESSS);
		}
		logger.info("{} file types found : {}", fileTypes.keySet().size(), fileTypes.keySet());
	}

	public List<String> getFilenameList() {
		return allFileNames;
	}

	/**
	 *
	 * @param types
	 * @return Return list of file names with types matching those specified in the list
	 */
	public List<String> getFileList(List<String> types) {
		if (fileTypes.isEmpty()) {
			try {
				updateFileTypeMap();
			} catch (IOException e) {
				logger.warn("Problem updating list of files", e);
			}
		}

		List<String> files = new ArrayList<>();
		types.forEach(classType ->
			// Add list of files matching specified class type
			fileTypes.keySet()
				.stream()
				.filter(classType::endsWith)
				.findFirst()
				.ifPresent(key -> files.addAll(fileTypes.get(key)))
		);

		return files.stream().toList();
	}

	protected void notifyObservers(Object evt) {
		observableComponent.notifyIObservers(this, evt);
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

	public String getDirectoryToWatch() {
		return directoryToWatch;
	}
}

