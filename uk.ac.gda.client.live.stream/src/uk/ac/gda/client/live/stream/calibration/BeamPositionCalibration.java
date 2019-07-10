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

import java.io.IOException;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DoubleDataset;
import org.eclipse.january.dataset.IDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.Scannable;
import uk.ac.diamond.daq.persistence.jythonshelf.LocalParameters;

/**
 * Creates axes datasets for a data stream calibrated to x and y scannables given pixel size and pixel coordinates of the beam
 * in the camera feed.
 */

public class BeamPositionCalibration extends AbstractCalibratedAxesProvider {

	protected static final Logger logger = LoggerFactory.getLogger(BeamPositionCalibration.class);

	private static final String BEAM_POSITION_CONFIG_NAME = "beam_calibration";
	private static final String CONFIG_X_POSITION_KEY = "beam_position.x";
	private static final String CONFIG_Y_POSITION_KEY = "beam_position.y";

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
			Scannable yAxisScannable, double yAxisPixelScaling, int yAxisBeamPositionInPixels, boolean attemptToLoadLocalParameters) {
		super(xAxisScannable, xAxisPixelScaling, yAxisScannable, yAxisPixelScaling);

		this.xAxisBeamPositionInPixels = xAxisBeamPositionInPixels;
		this.yAxisBeamPositionInPixels = yAxisBeamPositionInPixels;

		if (attemptToLoadLocalParameters) {
			try {
				loadCalibrationValues();
			} catch (ConfigurationException | IOException e) {
				logger.error("Unable to load calibration values from XML.", e);
			}
		}

		createAxesUpdaters();
		updateDatasets();
	}

	@Override
	protected void createAxesUpdaters() {
		this.xBeamPositionUpdater = new BeamPositionAxisDatasetUpdater(xAxisScannable, xAxisPixelScaling, xAxisBeamPositionInPixels);
		this.yBeamPositionUpdater = new BeamPositionAxisDatasetUpdater(yAxisScannable, yAxisPixelScaling, yAxisBeamPositionInPixels);

		this.xAxisUpdater = xBeamPositionUpdater;
		this.yAxisUpdater = yBeamPositionUpdater;
	}

	public void setBeamPosition(int xAxisBeamPositionInPixels, int yAxisBeamPositionInPixels, boolean saveCalibrationValues) {
		this.xAxisBeamPositionInPixels = xAxisBeamPositionInPixels;
		xBeamPositionUpdater.setBeamPosition(xAxisBeamPositionInPixels);

		this.yAxisBeamPositionInPixels = yAxisBeamPositionInPixels;
		yBeamPositionUpdater.setBeamPosition(yAxisBeamPositionInPixels);

		updateDatasets();

		if (saveCalibrationValues) {
			try {
				saveCalibrationValues();
			} catch (ConfigurationException | IOException exception) {
				logger.error("Unable to save calibration values.", exception);
			}
		}
	}

	private void saveCalibrationValues() throws ConfigurationException, IOException {
		XMLConfiguration configuration = LocalParameters.getXMLConfiguration(BEAM_POSITION_CONFIG_NAME);

		configuration.setProperty(CONFIG_X_POSITION_KEY, xAxisBeamPositionInPixels);
		configuration.setProperty(CONFIG_Y_POSITION_KEY, yAxisBeamPositionInPixels);

		configuration.save();
	}

	private void loadCalibrationValues() throws ConfigurationException, IOException {
		XMLConfiguration configuration = LocalParameters.getXMLConfiguration(BEAM_POSITION_CONFIG_NAME);

		this.xAxisBeamPositionInPixels = configuration.getInteger(CONFIG_X_POSITION_KEY, xAxisBeamPositionInPixels);
		this.yAxisBeamPositionInPixels= configuration.getInteger(CONFIG_Y_POSITION_KEY, yAxisBeamPositionInPixels);
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