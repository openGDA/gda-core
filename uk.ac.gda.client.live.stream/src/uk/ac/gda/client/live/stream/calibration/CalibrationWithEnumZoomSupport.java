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

import java.util.Map;
import java.util.Objects;

import org.eclipse.january.dataset.IDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.EnumPositioner;
import gda.device.EnumPositionerStatus;
import gda.observable.IObserver;

/**
 * This {@link CalibratedAxesProvider} contains a map of discrete zoom position to calibration bean,
 * and safely swaps the active bean when the zoom position changes.
 */
public class CalibrationWithEnumZoomSupport implements CalibratedAxesProvider {

	private static final Logger logger = LoggerFactory.getLogger(CalibrationWithEnumZoomSupport.class);

	private final EnumPositioner zoom;
	private final Map<String, CalibratedAxesProvider> zoomCalibrators;

	private CalibratedAxesProvider activeCalibrator;
	private int[] dataShape;

	private final IObserver zoomObserver;


	/**
	 * Create a {@link CalibratedAxesProvider} which observes an {@link EnumPositioner} and delegates to the
	 * CalibratedAxesProvider corresponding to the current position.
	 * <p>
	 * This constructor fails if:
	 * <ul>
	 * <li> either argument is {@code null}, or</li>
	 * <li> the map is missing an entry for any possible position.</li>
	 * </ul>
	 *
	 * @param zoom 					the EnumPositioner we will observe
	 * @param zoomCalibrators		map of CalibratedAxesProvider bean for each enumerated zoom position
	 */
	public CalibrationWithEnumZoomSupport(EnumPositioner zoom, Map<String, CalibratedAxesProvider> zoomCalibrators) {

		try {
			validateArguments(zoom, zoomCalibrators);
			activeCalibrator = zoomCalibrators.get(zoom.getPosition());
		} catch (DeviceException e) {
			throw new IllegalStateException("Could not read " + zoom.getName() + " positions");
		}

		this.zoom = zoom;
		this.zoomCalibrators = zoomCalibrators;

		this.zoomObserver = this::updateCalibrator;
	}

	private void validateArguments(EnumPositioner zoom,	Map<String, CalibratedAxesProvider> zoomCalibrators) throws DeviceException {
		Objects.requireNonNull(zoom);
		Objects.requireNonNull(zoomCalibrators);

		for (String zoomPosition : zoom.getPositions()) {
			if (!zoomCalibrators.containsKey(zoomPosition)) {
				throw new IllegalStateException("No calibration configuration found for " + zoom.getName() + " position '" + zoomPosition + "'");
			}
		}
	}

	private void updateCalibrator(Object source, Object arg) {

		if (arg == EnumPositionerStatus.IDLE) {
			logger.debug("Updating live stream calibrator after receiving event from {}", source);
			try {

				final CalibratedAxesProvider newCalibrator = zoomCalibrators.get(zoom.getPosition());

				// We must be careful when updating our calibrator
				// to prevent the stream from briefly receiving null datasets:

				// 1) disconnect currentCalibrator.
				activeCalibrator.disconnect();

				// 2) connect newCalibrator
				newCalibrator.connect();

				// 3) if we have been given a stream shape, set it on newCalibrator
				if (dataShape != null) {
					newCalibrator.resizeStream(dataShape);
				}

				// 4) only then reassign currentCalibrator
				activeCalibrator = newCalibrator;

			} catch (DeviceException e) {
				logger.error("Problem with " + zoom.getName() + "; stream calibration has not been updated", e);
			}
		}
	}

	@Override
	public void connect() {
		zoom.addIObserver(zoomObserver);
		updateCalibrator(this, EnumPositionerStatus.IDLE); // forced update to connect to the appropriate calibrator
	}

	@Override
	public void disconnect() {
		zoom.deleteIObserver(zoomObserver);
		activeCalibrator.disconnect();
	}

	@Override
	public IDataset getXAxisDataset() {
		return activeCalibrator.getXAxisDataset();
	}

	@Override
	public IDataset getYAxisDataset() {
		return activeCalibrator.getYAxisDataset();
	}

	@Override
	public void resizeStream(int[] newShape) {
		dataShape = newShape;
		activeCalibrator.resizeStream(newShape);
	}

}
