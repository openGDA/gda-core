/*-
 * Copyright © 2024 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.devices.specs.phoibos.ui.addons;

import static uk.ac.diamond.daq.api.messaging.messages.DestinationConstants.GDA_MESSAGES_SCAN_TOPIC;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.EventListener;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.dawnsci.datavis.api.DataVisConstants;
import org.dawnsci.datavis.model.DataOptions;
import org.dawnsci.datavis.model.IFileController;
import org.dawnsci.datavis.model.IFileController.OpenMode;
import org.dawnsci.datavis.model.LoadedFile;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.bean.BeanEvent;
import org.eclipse.scanning.api.event.bean.IBeanListener;
import org.eclipse.scanning.api.event.core.IPropertyFilter.FilterAction;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.scanning.api.ui.CommandConstants;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import uk.ac.diamond.daq.api.messaging.messages.ScanMessage;
import uk.ac.diamond.daq.api.messaging.messages.ScanStatus;
import uk.ac.diamond.daq.api.messaging.messages.SwmrStatus;

public class FileReloadDataVisAddon {
	private static final Logger logger = LoggerFactory.getLogger(FileReloadDataVisAddon.class);
	private ISubscriber<EventListener> scanSubscriber;
	private IBeanListener<Map<String, Object>> scanListener;
	private final Gson gson = new Gson();

	@Inject IFileController fileController;
	@Inject ESelectionService selectionService;

	@PostConstruct
	public void monitorEvents(IEventService service) throws URISyntaxException, EventException {
		final String suri = CommandConstants.getScanningBrokerUri();
		if (suri == null) return; // Nothing to start, standard DAWN.
		final URI uri = new URI(suri);
		if (scanSubscriber==null) scanSubscriber = service.createSubscriber(uri, GDA_MESSAGES_SCAN_TOPIC);
		// We don't care about the scan request, removing it means that
		// all the points models and detector models to not have to resolve in
		// order to get the event.
		scanSubscriber.addProperty("scanRequest", FilterAction.DELETE);
		scanSubscriber.addProperty("position", FilterAction.DELETE);
		if (scanListener == null) {
			scanListener = new ScanListener();
		}
		scanSubscriber.addListener(scanListener);
	}

	private class ScanListener implements IBeanListener<Map<String, Object>> {
		@Override
		public void beanChangePerformed(BeanEvent<Map<String, Object>> evt) {
			final JsonElement jsonElement = gson.toJsonTree(evt.getBean());
			final ScanMessage scanMessage = gson.fromJson(jsonElement, ScanMessage.class);

			if ((scanMessage == null) || !scanMessage.getStatus().equals(ScanStatus.UPDATED)) return;
			if (!(scanMessage.getSwmrStatus().equals(SwmrStatus.ACTIVE) || scanMessage.getSwmrStatus().equals(SwmrStatus.ENABLED))) {
				logger.warn("Swimmer mode is not active or not enabled - addon will not update datavis");
				return;
			}

			final String filePathToReload = scanMessage.getFilePath();
			final Display display = PlatformUI.getWorkbench().getDisplay();
			display.asyncExec(() -> {
				try {
					reloadFileWithSelectionRecovery(filePathToReload);
				} catch (InterruptedException e) {
					logger.error("File reload process was interrupted", e);
					Thread.currentThread().interrupt(); // Restore interrupted state
				} catch (Exception e) {
					logger.error("Unexpected error during file reload", e);
				} finally {
					fileController.setOpenMode(OpenMode.DO_NOTHING);
				}
			});
		}

		private void reloadFileWithSelectionRecovery(String filePathToReload) throws InterruptedException {
			final LoadedFile originalFile = findFileByPath(filePathToReload);
			final IStructuredSelection selectedObject = (IStructuredSelection) selectionService.getSelection(DataVisConstants.FILE_PART_ID);
			final boolean isUpdatedFileSelected = selectedObject!=null && originalFile != null && isFileSelected(selectedObject, originalFile.getName());
			final OpenMode originalOpenMode = fileController.getOpenMode();
			List<String> cachedSelectedDataOptions = null;
			logger.warn("Updated file is selected? {}", isUpdatedFileSelected);
			if (isUpdatedFileSelected) {
				fileController.setOpenMode(OpenMode.SELECT);
			}

			if (originalFile != null) {
				cachedSelectedDataOptions = cacheSelectedDataOptions(originalFile);
				unloadFile(originalFile);
				waitUntilUnloaded(filePathToReload);
			}

			loadFile(filePathToReload);
			waitUntilLoaded(filePathToReload);

			final LoadedFile reloadedFile = findFileByPath(filePathToReload);
			restoreDataSelection(reloadedFile, cachedSelectedDataOptions);

			if (isUpdatedFileSelected) {
					fileController.selectFiles(List.of(reloadedFile), true);
					fileController.setOpenMode(originalOpenMode);
				}
			}

		private boolean isFileSelected(IStructuredSelection selectedObject, String name) {
			for (Object obj : selectedObject) {
				if (obj instanceof LoadedFile typed && name.equals(typed.getName())) {
					return true;
				}
			}
			return false;
		}

		private LoadedFile findFileByPath(String path) {
			return fileController.getLoadedFiles().stream().filter(f -> f.getFilePath().contains(path)).findFirst().orElse(null);
		}

		private List<String> cacheSelectedDataOptions(LoadedFile file) {
			final List<String> selectedNames = file.getSelectedDataOptions().stream().map(DataOptions::getName).toList();
			logger.debug("Cached selected data options: {}", selectedNames);
			return selectedNames;
		}

		private void unloadFile(LoadedFile file) {
			logger.debug("Unloading file: {}", file);
			fileController.unloadFiles(List.of(file));
		}

		private void loadFile(String path) {
			logger.debug("Loading file from path: {}", path);
			fileController.loadFiles(new String[]{path}, null, true);
		}

		private void waitUntilUnloaded(String filePath) throws InterruptedException {
			waitUntil(() -> fileController.getLoadedFiles().stream().noneMatch(f -> f.getFilePath().contains(filePath)));
		}

		private void waitUntilLoaded(String filePath) throws InterruptedException {
			waitUntil(() -> fileController.getLoadedFiles().stream().anyMatch(f -> f.getFilePath().contains(filePath)));
		}

		private void restoreDataSelection(LoadedFile file, List<String> selectedOptionNames) {
			if (file == null || selectedOptionNames == null) return;
			file.getDataOptions().forEach(option -> option.setSelected(false));
			selectedOptionNames.forEach(name -> file.getDataOption(name).setSelected(true));
			logger.debug("Restored selected data options: {}", file.getSelectedDataOptions());
		}

		private void waitUntil(BooleanSupplier condition) throws InterruptedException {
			int maxAttempts = 100; // 5 seconds timeout
			int attempts = 0;
			while (Boolean.FALSE.equals(condition.getAsBoolean()) && attempts++ < maxAttempts) {
				Thread.sleep(50);
			}
		}

		@PreDestroy
		public void cleanup() {
			if (scanSubscriber != null && scanListener != null) {
				scanSubscriber.removeListener(scanListener);
				try {
					scanSubscriber.disconnect();
				} catch (EventException e) {
					logger.error("Could not disconnect subscriber to scan topic", e);
				}
				scanSubscriber = null;
			}
		}
	}
}
