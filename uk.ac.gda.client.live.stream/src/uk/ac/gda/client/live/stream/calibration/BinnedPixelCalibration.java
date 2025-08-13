/*-
 * Copyright © 2025 Diamond Light Source Ltd.
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

package uk.ac.gda.client.live.stream.calibration;

import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DoubleDataset;
import org.eclipse.january.dataset.IDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.observable.IObserver;
import uk.ac.gda.api.camera.CameraControl;
import uk.ac.gda.api.camera.CameraControllerEvent;

/**
 * A binning-aware axes provider which always returns axes in full-resolution pixel space.
 */
public class BinnedPixelCalibration implements CalibratedAxesProvider, IObserver {

	private static final Logger logger = LoggerFactory.getLogger(BinnedPixelCalibration.class);
	private CameraControl camControl;

	private IDataset xAxis;
	private IDataset yAxis;


	public BinnedPixelCalibration(CameraControl camControl) {
		this.camControl = camControl;
	}

	@Override
	public void connect() {
		camControl.addIObserver(this);
	}

	@Override
	public void disconnect() {
		camControl.deleteIObserver(this);
	}

	private void updateDatasets() {
		try {
			int pixelsX = camControl.getImageSizeX();
			int pixelsY = camControl.getImageSizeY();
			int fullResX = pixelsX * camControl.getBinningPixels().getX();
			int fullResY = pixelsY * camControl.getBinningPixels().getY();
			xAxis = DatasetFactory.createLinearSpace(DoubleDataset.class, 0, fullResX, pixelsX);
			yAxis = DatasetFactory.createLinearSpace(DoubleDataset.class, 0, fullResY, pixelsY);
		} catch (DeviceException e) {
			logger.error("Error reading parameters from {}", camControl.getName(), e);
		}
	}

	@Override
	public void resizeStream(int[] newShape) {
		updateDatasets();
	}

	@Override
	public void update(Object source, Object arg) {
		if (arg instanceof CameraControllerEvent) {
			updateDatasets();
		}
	}

	@Override
	public IDataset getXAxisDataset() {
		if (xAxis == null) {
			updateDatasets();
		}
		return xAxis;
	}

	@Override
	public IDataset getYAxisDataset() {
		if (yAxis == null) {
			updateDatasets();
		}
		return yAxis;
	}

}
