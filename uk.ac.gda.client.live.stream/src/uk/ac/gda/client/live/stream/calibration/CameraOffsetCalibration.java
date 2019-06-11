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

package uk.ac.gda.client.live.stream.calibration;

import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DoubleDataset;
import org.eclipse.january.dataset.IDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.Scannable;

/**
 * Creates axes datasets for a data stream calibrated to x and y scannables given pixel size and camera offset
 */
public class CameraOffsetCalibration extends AbstractCalibratedAxesProvider {

	protected static final Logger logger = LoggerFactory.getLogger(CameraOffsetCalibration.class);

	/** The position of the camera relative to the x-axis of the sample stage. */
	protected double xAxisOffset;

	/** The position of the camera relative to the y-axis of the sample stage. */
	protected double yAxisOffset;

	/**
	 * Creates a new instance of BeamPixelPositionCalibration
	 *
	 * @param xAxisScannable The scannable for the horizontal axis of the camera
	 * @param xAxisPixelScaling The width of each pixel of the camera, in the units used by the x-axis scannable
	 * @param xAxisOffset he position of the camera relative to the x-axis of the sample stage
	 * @param yAxisScannable The scannable for the vertical axis of the camera
	 * @param yAxisPixelScaling The height of each pixel of the camera in the units used by the y-axis scannable
	 * @param yAxisOffset The position of the camera relative to the y-axis of the sample stage.
	 */
	public CameraOffsetCalibration(Scannable xAxisScannable, double xAxisPixelScaling, double xAxisOffset,
			Scannable yAxisScannable, double yAxisPixelScaling, double yAxisOffset) {
		super(xAxisScannable, xAxisPixelScaling, yAxisScannable, yAxisPixelScaling);

		this.xAxisOffset = xAxisOffset;
		this.yAxisOffset = yAxisOffset;

		createAxesUpdaters();
	}

	@Override
	protected void createAxesUpdaters() {
		xAxisUpdater = new AxisDatasetUpdater(xAxisScannable, xAxisPixelScaling, xAxisOffset);
		yAxisUpdater = new AxisDatasetUpdater(yAxisScannable, yAxisPixelScaling, yAxisOffset);
	}

	/**
	 * Updates the axis dataset related to a particular scannable when this scannable moves
	 */
	private class AxisDatasetUpdater extends AbstractCalibratedAxesDatasetUpdater {

		private final double pixelScaling;
		private final double cameraOffset;

		private AxisDatasetUpdater(Scannable scannable, double pixelScaling, double cameraOffset) {
			super(scannable);
			this.pixelScaling = pixelScaling;
			this.cameraOffset = cameraOffset;
		}

		/**
		 * Creates and returns a dataset for the scannable
		 * @throws DeviceException if the scannable position cannot be read
		 */
		@Override
		protected IDataset createDataSet() throws DeviceException {
			// get the current position of the axis scannable
			final double pos = (double) scannable.getPosition();

			// calculate the camera position and size
			final double cameraPos = pos + cameraOffset;
			final double imageSize = numberOfPixels * pixelScaling;

			// from those values, we can get the image start and stop values
			final double imageStart = cameraPos - imageSize/2;
			final double imageStop = cameraPos + imageSize/2;

			// create the linear dataset and set the name
			final IDataset axisDataset = DatasetFactory.createLinearSpace(DoubleDataset.class, imageStart, imageStop, numberOfPixels);
			axisDataset.setName(scannable.getName());

			return axisDataset;
		}
	}
}
