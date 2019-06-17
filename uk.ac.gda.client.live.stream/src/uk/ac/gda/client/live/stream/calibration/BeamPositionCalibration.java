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

import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DoubleDataset;
import org.eclipse.january.dataset.IDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.Scannable;

/**
 * Creates axes datasets for a data stream calibrated to x and y scannables given pixel size and pixel coordinates of the beam
 * in the camera feed.
 */

public class BeamPositionCalibration extends AbstractCalibratedAxesProvider {

	protected static final Logger logger = LoggerFactory.getLogger(BeamPositionCalibration.class);

	/** The X pixel position of the beam in the camera feed **/
	private int xAxisBeamPositionInPixels;

	/** The Y pixel position of the beam in the camera feed **/
	private int yAxisBeamPositionInPixels;

	private BeamPositionAxisDatasetUpdater xBeamPositionUpdater;
	private BeamPositionAxisDatasetUpdater yBeamPositionUpdater;

	/**
	 * Creates a new instance of BeamPositionCalibration
	 *
	 * @param xAxisScannable The scannable for the horizontal axis of the camera
	 * @param xAxisPixelScaling The width of each pixel of the camera, in the units used by the x-axis scannable
	 * @param xAxisBeamPositionInPixels The X pixel position of the beam in the camera feed
	 * @param yAxisScannable The scannable for the vertical axis of the camera
	 * @param yAxisPixelScaling The height of each pixel of the camera in the units used by the y-axis scannable
	 * @param yAxisBeamPositionInPixels The Y pixel position of the beam in the camera feed
	 */
	public BeamPositionCalibration(Scannable xAxisScannable, double xAxisPixelScaling, int xAxisBeamPositionInPixels,
			Scannable yAxisScannable, double yAxisPixelScaling, int yAxisBeamPositionInPixels) {
		super(xAxisScannable, xAxisPixelScaling, yAxisScannable, yAxisPixelScaling);

		this.xAxisBeamPositionInPixels = xAxisBeamPositionInPixels;
		this.yAxisBeamPositionInPixels = yAxisBeamPositionInPixels;

		createAxesUpdaters();
	}

	@Override
	protected void createAxesUpdaters() {
		this.xBeamPositionUpdater = new BeamPositionAxisDatasetUpdater(xAxisScannable, xAxisPixelScaling, xAxisBeamPositionInPixels);
		this.yBeamPositionUpdater = new BeamPositionAxisDatasetUpdater(yAxisScannable, yAxisPixelScaling, yAxisBeamPositionInPixels);

		this.xAxisUpdater = xBeamPositionUpdater;
		this.yAxisUpdater = yBeamPositionUpdater;
	}

	public void setXAxisBeamPositionInPixels(int xAxisBeamPositionInPixels) {
		this.xAxisBeamPositionInPixels = xAxisBeamPositionInPixels;
		xBeamPositionUpdater.setBeamPosition(xAxisBeamPositionInPixels);
		updateDatasets();
	}

	public void setYAxisBeamPositionInPixels(int yAxisBeamPositionInPixels) {
		this.yAxisBeamPositionInPixels = yAxisBeamPositionInPixels;
		yBeamPositionUpdater.setBeamPosition(yAxisBeamPositionInPixels);
		updateDatasets();
	}

	public void setBeamPosition(int xAxisBeamPositionInPixels, int yAxisBeamPositionInPixels) {
		setXAxisBeamPositionInPixels(xAxisBeamPositionInPixels);
		setYAxisBeamPositionInPixels(yAxisBeamPositionInPixels);
	}

	private class BeamPositionAxisDatasetUpdater extends AbstractCalibratedAxesDatasetUpdater {

		private double pixelScaling;
		private int beamPositionInPixels;

		private BeamPositionAxisDatasetUpdater(Scannable scannable, double pixelScaling, int beamPositionInPixels) {
			super(scannable);
			this.pixelScaling = pixelScaling;
			this.beamPositionInPixels = beamPositionInPixels;
		}

		private void setBeamPosition(int beamPositionInPixels) {
			this.beamPositionInPixels = beamPositionInPixels;
		}

		@Override
		protected IDataset createDataSet() throws DeviceException {
			// get the current position of the axis scannable
			final double pos = (double) scannable.getPosition();

			// calculate the real-space bounds of the axis
			// assume the beam is in the middle of the pixel, so apply a half pixel adjustment
			final double imageStart = pos - ((beamPositionInPixels + 0.5) * pixelScaling);
			final double imageStop = pos + ((numberOfPixels - beamPositionInPixels - 0.5) * pixelScaling);

			// create the linear dataset and set the name
			final IDataset axisDataset = DatasetFactory.createLinearSpace(DoubleDataset.class, imageStart, imageStop, numberOfPixels);
			axisDataset.setName(scannable.getName());

			return axisDataset;
		}
	}
}