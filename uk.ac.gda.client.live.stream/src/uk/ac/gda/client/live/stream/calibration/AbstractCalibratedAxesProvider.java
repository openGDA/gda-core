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

package uk.ac.gda.client.live.stream.calibration;

import org.eclipse.january.dataset.IDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.Scannable;

public abstract class AbstractCalibratedAxesProvider implements CalibratedAxesProvider {

	protected static final Logger logger = LoggerFactory.getLogger(AbstractCalibratedAxesProvider.class);

	/** The scannable for the horizontal axis of the camera */
	protected Scannable xAxisScannable;

	/** The width of each pixel of the camera, in the units used by the x-axis scannable. */
	protected double xAxisPixelScaling;

	/** The scannable for the vertical axis of the camera. */
	protected Scannable yAxisScannable;

	/** The height of each pixel of the camera in the units used by the y-axis scannable. */
	protected double yAxisPixelScaling;

	/** updates horizontal axis when x scannable moves */
	protected CalibratedAxesDatasetUpdater xAxisUpdater;

	/** updates vertical axis when y scannable moves */
	protected CalibratedAxesDatasetUpdater yAxisUpdater;

	/**
	 * Creates a new instance of AbstractCalibratedAxesProvider
	 *
	 * @param xAxisScannable The scannable for the horizontal axis of the camera
	 * @param xAxisPixelScaling The width of each pixel of the camera, in the units used by the x-axis scannable
	 * @param yAxisScannable The scannable for the vertical axis of the camera
	 * @param yAxisPixelScaling The height of each pixel of the camera in the units used by the y-axis scannable
	 */
	protected AbstractCalibratedAxesProvider(Scannable xAxisScannable, double xAxisPixelScaling, Scannable yAxisScannable, double yAxisPixelScaling) {
		this.xAxisScannable = xAxisScannable;
		this.xAxisPixelScaling = xAxisPixelScaling;
		this.yAxisScannable = yAxisScannable;
		this.yAxisPixelScaling = yAxisPixelScaling;
	}

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

	protected void updateDatasets() {
		xAxisUpdater.update(null, null);
		yAxisUpdater.update(null, null);
	}

	/** Should be overridden to create two instances of classes implementing CalibratedAxesDatasetUpdater,
	 * one for each axis, which will create the datasets when the scannables move, image is resized, or a
	 * new frame is received.
	 *
	 * In your concrete class, you should call this once all required properties in set, using one of the following:
	 *
	 * 1. Create a constructor which calls it.
	 * 2. Set the init-method property on your spring bean.
	 * 3. Implement InitializingBean and create an afterPropertiesSet() method which calls it.
	 */
	protected abstract void createAxesUpdaters();
}
