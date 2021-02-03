/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.ui.experiment.file;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Consumer;

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;

import uk.ac.diamond.daq.mapping.api.IMappingExperimentBean;
import uk.ac.diamond.daq.mapping.ui.experiment.ScanManagementController;
import uk.ac.diamond.daq.mapping.ui.experiment.saver.ScanSaver;
import uk.ac.gda.api.acquisition.resource.event.AcquisitionConfigurationResourceSaveEvent;
import uk.ac.gda.core.tool.spring.SpringApplicationContextFacade;
import uk.ac.gda.ui.tool.spring.ClientRemoteServices;

/**
 *
 * @deprecated No other reference found. To be deleted on GDA 9.21
 */
@Deprecated
public class FileScanSaver extends ScanSaver {
	private final String scanFilesDir;
	private final ScanManagementController smController;

	public FileScanSaver(Consumer<Optional<IMappingExperimentBean>> postLoad, ScanManagementController smController) {
		super(new ArrayList<SavedScanMetaData>(), postLoad);

		this.scanFilesDir = getClientRemoteServices().getIFilePathService().getVisitConfigDir();
		this.smController = smController;
		refreshScanList();
	}

	@Override
	public Optional<IMappingExperimentBean> loadBean(SavedScanMetaData savedScanMetaData) {
		return smController.loadScanMappingBean(createFilename(savedScanMetaData));
	}

	private void publishExposureChangeEvent(String filename) {
		AcquisitionConfigurationResourceSaveEvent event;
		try {
			event = new AcquisitionConfigurationResourceSaveEvent(this, new URL("file", "localhost", filename));
			SpringApplicationContextFacade.publishEvent(event);
		} catch (MalformedURLException e) {
		}
	}

	@Override
	public void save() {
		InputDialog dlg = new InputDialog(Display.getCurrent().getActiveShell(), "Save Scan Definition",
				"Please enter a name for the current Scan Definition", "", null);
		if (dlg.open() == Window.OK) {
			String filename = smController.buildDescriptiveFilename(dlg.getValue());
			filename = String.format("%s/%s", scanFilesDir, filename);
			smController.saveScan(filename);
			refreshScanList();
			publishExposureChangeEvent(filename);
		}
	}

	@Override
	public void delete(SavedScanMetaData savedScanMetaData) {
		String filename = createFilename(savedScanMetaData);
		if (filename != null) {
			smController.deleteScan(filename);
			refreshScanList();
			publishExposureChangeEvent(filename);
		}
	}

	private String createFilename(SavedScanMetaData savedScanMetaData) {
		if (savedScanMetaData != null && savedScanMetaData.getName() != null) {
			return scanFilesDir + "/" + savedScanMetaData.getName();
		}
		return null;
	}

	private void refreshScanList() {
		String[] fileNames = new File(scanFilesDir).list((dir, name) -> name.endsWith(".map"));
		if (fileNames != null) {
			getObservableList().clear();
			Arrays.stream(fileNames).forEach(filaName -> {
				getObservableList().add(new SavedScanMetaData(filaName));
			});
		}
	}

	private static ClientRemoteServices getClientRemoteServices() {
		return SpringApplicationContextFacade.getBean(ClientRemoteServices.class);
	}
}
