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

import java.util.Objects;

import org.eclipse.january.dataset.IDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.EnumPositioner;
import gda.device.EnumPositionerStatus;
import gda.observable.IObserver;

/**
 * This implementation observes a given {@link EnumPositioner}
 * and always delegates to the {@link CalibratedAxesProvider}
 * corresponding to the current enumerated position.
 */
public class PositionerDeterminedCalibration implements CalibratedAxesProvider {

	private static final Logger logger = LoggerFactory.getLogger(PositionerDeterminedCalibration.class);

	private final EnumPositioner positioner;
	private final CalibrationStore calibrations;

	private CalibratedAxesProvider activeCalibration;
	private int[] dataShape;

	private final IObserver switchObserver;


	/**
	 * Create a {@link CalibratedAxesProvider} which observes an {@link EnumPositioner} and delegates to the
	 * CalibratedAxesProvider corresponding to the positioner's current position.
	 * <p>
	 * This constructor fails if:
	 * <ul>
	 * <li> either argument is {@code null}, or</li>
	 * <li> the calibration store is missing any entry for the possible positions
	 * </ul>
	 *
	 * @param positioner 		the EnumPositioner we will observe
	 * @param calibrations		CalibrationStore with CalibratedAxesProviders for each enum switch position
	 */
	public PositionerDeterminedCalibration(EnumPositioner positioner, CalibrationStore calibrations) {

		try {
			validateArguments(positioner, calibrations);
			activeCalibration = calibrations.get((String) positioner.getPosition());
		} catch (DeviceException e) {
			throw new IllegalStateException("Could not read " + positioner.getName() + " positions");
		}

		this.positioner = positioner;
		this.calibrations = calibrations;

		this.switchObserver = this::updateCalibrator;
	}

	private void validateArguments(EnumPositioner positioner,	CalibrationStore calibrations) throws DeviceException {
		Objects.requireNonNull(positioner);
		Objects.requireNonNull(calibrations);

		for (String position : positioner.getPositions()) {
			if (!calibrations.containsKey(position)) {
				throw new IllegalStateException("No calibration configuration found for " + positioner.getName() + " position '" + position + "'");
			}
		}
	}

	private void updateCalibrator(Object source, Object arg) {

		if (arg == EnumPositionerStatus.IDLE) {
			logger.debug("Updating live stream calibrator after receiving event from {}", source);
			try {

				final CalibratedAxesProvider newCalibrator = calibrations.get((String) positioner.getPosition());

				// We must be careful when updating our calibrator
				// to prevent the stream from briefly receiving null datasets:

				// 1) disconnect currentCalibrator.
				activeCalibration.disconnect();

				// 2) connect newCalibrator
				newCalibrator.connect();

				// 3) if we have been given a stream shape, set it on newCalibrator
				if (dataShape != null) {
					newCalibrator.resizeStream(dataShape);
				}

				// 4) only then reassign currentCalibrator
				activeCalibration = newCalibrator;

			} catch (DeviceException e) {
				logger.error("Problem with " + positioner.getName() + "; stream calibration has not been updated", e);
			}
		}
	}

	@Override
	public void connect() {
		positioner.addIObserver(switchObserver);
		updateCalibrator(this, EnumPositionerStatus.IDLE); // forced update to connect to the appropriate calibrator
	}

	@Override
	public void disconnect() {
		positioner.deleteIObserver(switchObserver);
		activeCalibration.disconnect();
	}

	@Override
	public IDataset getXAxisDataset() {
		return activeCalibration.getXAxisDataset();
	}

	@Override
	public IDataset getYAxisDataset() {
		return activeCalibration.getYAxisDataset();
	}

	@Override
	public void resizeStream(int[] newShape) {
		dataShape = newShape;
		activeCalibration.resizeStream(newShape);
	}

}
