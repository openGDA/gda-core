/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

package gda.device.detector.mythen.data;

import java.io.File;
import java.util.Vector;

/**
 * Merges raw files providing a flat field calibration to create a single MythenRawDataset for use by the DataConvertor.
 * <p>
 * This is for the Mythen on B18 whose calibration files are split by module.
 */
public class SplitFilesFlatFieldCorrectionProvider extends SplitCalibrationFilesBase implements
		FlatFieldDatasetProvider {

	MythenRawDataset storedDataSet;
	String storedMode = "";
	String storedfilePrefix = "";
	String storedCalibrationFolder = "";

	@Override
	public MythenRawDataset getFlatFieldData() {

		if (storedDataSet != null && storedMode.equals(modules.getMode())
				&& storedfilePrefix.equals(modules.getFlatFilePrefix())
				&& storedCalibrationFolder.equals(modules.getCalibrationFolder())) {
			return storedDataSet;
		}

		Vector<MythenRawData> lines = new Vector<MythenRawData>();

		for (int moduleNum = 0; moduleNum < modules.getModules().size(); moduleNum++) {

			String flatFieldFile = modules.getCalibrationFolder() + "/" + modules.getModules().get(moduleNum) + "/"
					+ modules.getMode() + "/flat/" + modules.getCalibrationFilePrefix() + modules.getFlatFilePrefix() + "."
					+ modules.getModules().get(moduleNum).toLowerCase();
			File file = new File(flatFieldFile);
			lines.addAll((new MythenRawDataset(file)).getLines());
		}

		MythenRawDataset dataset = new MythenRawDataset();
		dataset.setLines(lines);

		storedDataSet = dataset;
		storedMode = modules.getMode();
		storedfilePrefix = modules.getFlatFilePrefix();
		storedCalibrationFolder = modules.getCalibrationFolder();

		return dataset;

	}
}
