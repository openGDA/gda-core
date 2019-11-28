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

package uk.ac.diamond.daq.mapping.ui.experiment.saver;

import java.io.File;
import java.util.Optional;
import java.util.function.Consumer;

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.scanning.api.scan.IFilePathService;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

import uk.ac.diamond.daq.mapping.api.IMappingExperimentBean;
import uk.ac.diamond.daq.mapping.ui.experiment.ScanManagementController;
import uk.ac.diamond.daq.mapping.ui.experiment.file.SavedScanMetaData;

public class FileScanSaver extends ScanSaver {
	private final String scanFilesDir;
	private final ScanManagementController smController;

	public FileScanSaver(Consumer<Optional<IMappingExperimentBean>> postLoad,
			ScanManagementController smController) {
		super(postLoad);

		this.scanFilesDir = PlatformUI.getWorkbench().getService(IFilePathService.class).getVisitConfigDir();
		this.smController = smController;
	}

	@Override
	public SavedScanMetaData[] listScans() {
		SavedScanMetaData[] result = null;
		String[] list = new File(scanFilesDir).list((dir, name) -> name.endsWith(".map"));
		if (list != null) {
			result = new SavedScanMetaData[list.length];
			for (int i=0;i<list.length;i++) {
				result[i] = new SavedScanMetaData(list[i]);
			}
		}
		return result;
	}

	@Override
	public Optional<IMappingExperimentBean> loadBean(SavedScanMetaData savedScanMetaData) {
		return smController.loadScanMappingBean(createFilename(savedScanMetaData));
	}

	@Override
	public void save () {
        InputDialog dlg = new InputDialog(Display.getCurrent().getActiveShell(),
                "Save Scan Definition", "Please enter a name for the current Scan Definition", "", null);
        if (dlg.open() == Window.OK) {
        	String filename = smController.buildDescriptiveFilename(dlg.getValue());
        	filename = scanFilesDir + "/" + filename;
        	smController.saveScan(filename);
        }
	}

	@Override
	public void delete(SavedScanMetaData savedScanMetaData) {
		String filename = createFilename(savedScanMetaData);
		if (filename != null) {
			smController.deleteScan(filename);
		}
	}

	private String createFilename (SavedScanMetaData savedScanMetaData) {
		if (savedScanMetaData != null && savedScanMetaData.getName() != null) {
			return scanFilesDir + "/" + savedScanMetaData.getName();
		}
		return null;
	}
}
