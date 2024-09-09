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
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
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

	private String warningMessageFormat = "Cannot find VMA axis calibration to use for current value of camera zoom ('%s') - using pixels for axes instead";
	private volatile boolean warningDisplayed = false;

	/**
	 * Create a {@link CalibratedAxesProvider} which observes an {@link EnumPositioner} and delegates to the
	 * CalibratedAxesProvider corresponding to the positioner's current position.
	 * <p>
	 * This constructor fails if either argument is {@code null}
	 * </ul>
	 *
	 * @param positioner 		the EnumPositioner we will observe
	 * @param calibrations		CalibrationStore with CalibratedAxesProviders for each enum switch position
	 */
	public PositionerDeterminedCalibration(EnumPositioner positioner, CalibrationStore calibrations) {
		validateArguments(positioner, calibrations);

		this.positioner = positioner;
		this.calibrations = calibrations;
		activeCalibration = getCurrentAxesProvider();
		this.switchObserver = this::updateCalibrator;
	}

	private void validateArguments(EnumPositioner positioner,	CalibrationStore calibrations) {
		Objects.requireNonNull(positioner);
		Objects.requireNonNull(calibrations);
	}

	private String getCurrentPosition() throws DeviceException {
		return (String) positioner.getPosition();
	}

	/**
	 *
	 * @return CalibratedAxisProvider for current position value
	 */
	private CalibratedAxesProvider getCurrentAxesProvider() {
		try {
			return getCurrentAxesProvider(getCurrentPosition());
		} catch(DeviceException de) {
			logger.warn("Problem getting current position of "+positioner.getName(), de);
			return calibrations.getFallbackAxes();
		}
	}

	/**
	 * Return calibration provider for given position value.
	 * If matching one is not found in the CalibrationStore a fallback one is returned
	 * that gives pixels for axes values.
	 *
	 * @param position
	 * @return CalibratedAxisProvider for given position value
	 */
	private CalibratedAxesProvider getCurrentAxesProvider(String position) {
		if (!calibrations.containsKey(position)) {
			logger.warn("Axes calibration for position '{}' not found. Using pixels for axes values instead", position);
			return calibrations.getFallbackAxes();
		} else {
			return calibrations.get(position);
		}
	}

	private void updateCalibrator(Object source, Object arg) {

		if (arg != EnumPositionerStatus.IDLE) {
			return;
		}

		logger.debug("Updating live stream calibrator after receiving event from {}", source);

		checkPositionOk();

		final CalibratedAxesProvider newCalibrator = getCurrentAxesProvider();

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
	}

	/**
	 * Check that there is an axis calibration for the current positioner value.
	 * Show a dialog box by calling {@link #showWarning(String)} if no calibration is found.
	 *
	 */
	private void checkPositionOk() {
		String currentPosition = "unknown";
		try {
			currentPosition = getCurrentPosition();
		}catch(DeviceException de) {
			//don't need to log message here - same exception will be logged later in call to getCurrentAxisProvider()
		}
		// Show warning if axis provider for current position can't be found
		if (!calibrations.containsKey(currentPosition)) {
			showWarning(currentPosition);
		}
	}

	/**
	 * Display warning about missing axis calibration for zoom level
	 * <li> This uses a background thread to avoid blocking the GUI.
	 * <li> The user needs to close the warning in before any more warning messages will be displayed
	 * (to avoid avoid multiple boxes getting displayed each time there is a problem).
	 *
	 * @param zoomLevel
	 */
	private void showWarning(String zoomLevel) {
		if (calibrations.containsKey(zoomLevel)) {
			return;
		}

		if (warningDisplayed) {
			return;
		}
		Display.getDefault().asyncExec(() -> {
			warningDisplayed = true;
			MessageDialog.openWarning(Display.getDefault().getActiveShell(), "Cannot find calibration for zoom level",
					String.format(warningMessageFormat, zoomLevel));
			logger.info("Warining dialog closed");
			warningDisplayed = false;
		});
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
