/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.detectors.addetector;

import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.ScanningException;

import uk.ac.diamond.daq.detectors.addetector.api.AreaDetectorRunnableDeviceModel;
import uk.ac.diamond.daq.detectors.addetector.api.DarkImageAreaDetectorWritingFilesRunnableDeviceModel;

public class DarkImageAreaDetectorWritingFilesRunnableDevice extends AbstractAreaDetectorWritingFilesRunnableDevice {

	// interface IConfigurable

	@Override
	public void configure(AreaDetectorRunnableDeviceModel model) throws ScanningException {
		if (!model.getClass().isInstance(DarkImageAreaDetectorWritingFilesRunnableDeviceModel.class)) {
			throw new ScanningException("DarkImageAreaDetectorWritingFilesRunnableDeviceModel expected!");
		}
		super.configure(model);
	}

	// interface IRunnableDevice

	@Override
	public void run(IPosition position) throws ScanningException, InterruptedException {
		// Other logic may be done here as when to get the dark image.
		DarkImageAreaDetectorWritingFilesRunnableDeviceModel dmodel = (DarkImageAreaDetectorWritingFilesRunnableDeviceModel) model;
		if (position.getStepIndex()%dmodel.getFrequency() == 0) {
			super.run(position);
		}
	}

}
