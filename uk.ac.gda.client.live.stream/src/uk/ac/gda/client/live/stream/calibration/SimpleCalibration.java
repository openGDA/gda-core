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
import gda.observable.IObserver;

/**
 * Creates axes datasets for a data stream calibrated to x and y scannables given pixel size and camera offset
 * <p>
 * You <em>must</em> specify {@link #createAxesUpdaters()} as the {@code init-method} in the Spring bean definition
 */
public class SimpleCalibration implements CalibratedAxesProvider {

	private static final Logger logger = LoggerFactory.getLogger(SimpleCalibration.class);

	/** The scannable for the horizontal axis of the camera */
	private Scannable xAxisScannable;

	/** The width of each pixel of the camera, in the units used by the x-axis scannable. */
	private double xAxisPixelScaling;

	/** The position of the camera relative to the x-axis of the sample stage. */
	private double xAxisOffset;

	/** The scannable for the vertical axis of the camera. */
	private Scannable yAxisScannable;

	/** The height of each pixel of the camera in the units used by the y-axis scannable. */
	private double yAxisPixelScaling;

	/** The position of the camera relative to the y-axis of the sample stage. */
	private double yAxisOffset;

	/** updates horizontal axis when x scannable moves */
	private AxisDatasetUpdater xAxisUpdater;

	/** updates vertical axis when y scannable moves */
	private AxisDatasetUpdater yAxisUpdater;


	@Override
	public void connect() {
		xAxisScannable.addIObserver(xAxisUpdater);
		yAxisScannable.addIObserver(yAxisUpdater);
		updateDatasets();
	}

	@Override
	public void disconnect() {
		xAxisScannable.deleteIObserver(xAxisUpdater);
		yAxisScannable.deleteIObserver(yAxisUpdater);
	}

	@Override
	public IDataset getXAxisDataset() {
		return xAxisUpdater.getDataset();
	}

	@Override
	public IDataset getYAxisDataset() {
		return yAxisUpdater.getDataset();
	}

	@Override
	public void resizeStream(int[] newShape) {
		xAxisUpdater.setNumberOfPixels(newShape[1]);
		yAxisUpdater.setNumberOfPixels(newShape[0]);
		updateDatasets();
	}

	/**
	 * Updates the axis dataset related to a particular scannable when this scannable moves
	 */
	private class AxisDatasetUpdater implements IObserver {

		private final Scannable scannable;
		private final double pixelScaling;
		private final double cameraOffset;
		private volatile IDataset dataset;
		private int numberOfPixels;

		private AxisDatasetUpdater(Scannable scannable, double pixelScaling, double cameraOffset) {
			this.scannable = scannable;
			this.pixelScaling = pixelScaling;
			this.cameraOffset = cameraOffset;
		}

		@Override
		public void update(Object source, Object arg) {
			if (numberOfPixels == 0) { // uninitialised
				return;
			}

			boolean moving = true;
			while (moving) {
				try {
					moving = scannable.isBusy();
				} catch (DeviceException e) {
					logger.error("Error while determining whether scannable {} is busy",
							scannable.getName(), e);
				}
				try {
					dataset = createAxisDataset(scannable, cameraOffset, pixelScaling, numberOfPixels);
				} catch (DeviceException e) {
					logger.error("Error reading position of axis {} so calibration was not updated", scannable.getName(), e);
				}
				try {
					Thread.sleep(100); // limit loop rate to 10 Hz
				} catch (InterruptedException e) {
					logger.error("Thread was interrupted", e);
					Thread.currentThread().interrupt();
				}
			}
		}

		/**
		 * Creates and returns a dataset for an axis given the parameters below.
		 * @param axis the scannable for the axis
		 * @param cameraOffset the offset of the camera position relative to the scannable
		 * @param pixelScaling the size of a camera pixel in the units used by the scannable
		 * @param numPixels the number of pixels in the given axis of the scannable
		 * @return a dataset for the axis
		 * @throws DeviceException if the scannable position cannot be read
		 */
		private IDataset createAxisDataset(Scannable axis, double cameraOffset, double pixelScaling, int numPixels) throws DeviceException {
			// get the current position of the axis scannable
			final double pos = (double) axis.getPosition();

			// calculate the camera position and size
			final double cameraPos = pos + cameraOffset;
			final double imageSize = numPixels * pixelScaling;

			// from those values, we can get the image start and stop values
			final double imageStart = cameraPos - imageSize/2;
			final double imageStop = cameraPos + imageSize/2;

			// create the linear dataset and set the name
			final IDataset axisDataset = DatasetFactory.createLinearSpace(DoubleDataset.class, imageStart, imageStop, numPixels);
			axisDataset.setName(axis.getName());

			return axisDataset;
		}

		private void setNumberOfPixels(int numberOfPixels) {
			this.numberOfPixels = numberOfPixels;
		}

		public IDataset getDataset() {
			return dataset;
		}
	}

	private void updateDatasets() {
		xAxisUpdater.update(null, null);
		yAxisUpdater.update(null, null);
	}

	public void setxAxisScannable(Scannable xAxisScannable) {
		this.xAxisScannable = xAxisScannable;
	}

	public void setxAxisOffset(double xAxisOffset) {
		this.xAxisOffset = xAxisOffset;
	}

	public void setxAxisPixelScaling(double xAxisPixelScaling) {
		this.xAxisPixelScaling = xAxisPixelScaling;
	}

	public void setyAxisScannable(Scannable  yAxisScannable) {
		this.yAxisScannable = yAxisScannable;
	}

	public void setyAxisOffset(double yAxisOffset) {
		this.yAxisOffset = yAxisOffset;
	}

	public void setyAxisPixelScaling(double yAxisPixelScaling) {
		this.yAxisPixelScaling = yAxisPixelScaling;
	}

	/**
	 * init-method for Spring
	 */
	public void createAxesUpdaters() {
		xAxisUpdater = new AxisDatasetUpdater(xAxisScannable, xAxisPixelScaling, xAxisOffset);
		yAxisUpdater = new AxisDatasetUpdater(yAxisScannable, yAxisPixelScaling, yAxisOffset);
	}

}
