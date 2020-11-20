/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.client.gui.camera.controller;

import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;

import uk.ac.gda.api.camera.BinningFormat;

public class CameraConfigurationAdapter implements CameraConfigurationListener {

	@Override
	public void setCameraConfigurationMode(CameraConfigurationMode cameraConfigurationMode) {
		//do nothing
	}

	@Override
	public void setROI(RectangularROI roi) {
		//do nothing
	}

	@Override
	public void clearRegionOfInterest() {
		//do nothing
	}

	@Override
	public void setRatio(int highRegion, int lowRegion, double ratio) {
		//do nothing
	}

	@Override
	public void setBinningFormat(BinningFormat binningFormat) {
		//do nothing
	}

	@Override
	public void refreshSnapshot() {
		//do nothing
	}
}
