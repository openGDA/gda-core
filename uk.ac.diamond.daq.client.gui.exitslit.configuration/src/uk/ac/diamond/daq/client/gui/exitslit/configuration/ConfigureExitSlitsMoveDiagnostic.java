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

package uk.ac.diamond.daq.client.gui.exitslit.configuration;

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;
import static uk.ac.diamond.daq.client.gui.exitslit.configuration.ConfigureExitSlitsUtils.COLOUR_WHITE;
import static uk.ac.diamond.daq.client.gui.exitslit.configuration.ConfigureExitSlitsUtils.DIAGNOSTIC_COLOUR_MAP;
import static uk.ac.diamond.daq.client.gui.exitslit.configuration.ConfigureExitSlitsUtils.MOTOR_ERROR;
import static uk.ac.diamond.daq.client.gui.exitslit.configuration.ConfigureExitSlitsUtils.SCANNABLE_UPDATE_PERIOD;
import static uk.ac.diamond.daq.client.gui.exitslit.configuration.ConfigureExitSlitsUtils.SHUTTER_COLOUR_MAP;
import static uk.ac.diamond.daq.client.gui.exitslit.configuration.ConfigureExitSlitsUtils.createButton;
import static uk.ac.diamond.daq.client.gui.exitslit.configuration.ConfigureExitSlitsUtils.createStopButton;
import static uk.ac.diamond.daq.client.gui.exitslit.configuration.ConfigureExitSlitsUtils.displayError;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ProgressBar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.scannable.ScannablePositionChangeEvent;
import uk.ac.gda.dls.client.views.ReadonlyScannableComposite;

/**
 * Composite to move the diagnostic unit into or out of the beam and open or close the exit slit shutter
 * <p>
 * The constructor parameter "moveIn" controls how the devices should be moved (see below)
 */
public class ConfigureExitSlitsMoveDiagnostic extends ConfigureExitSlitsComposite {
	private static final Logger logger = LoggerFactory.getLogger(ConfigureExitSlitsMoveDiagnostic.class);

	private static final String DIAGNOSTIC_IN = "Screen";
	private static final String DIAGNOSTIC_OUT = "Out";

	private static final String SHUTTER_OPEN = "Open";
	private static final String SHUTTER_CLOSE = "Close";
	private static final String SHUTTER_CLOSED = "Closed";

	private static final int COLUMNS = 5;

	private boolean stopping = false;

	/**
	 * <code>true</code> if the user should<br>
	 * <ul>
	 * <li>move the diagnostic stick in</li>
	 * <li>open the shutter</li>
	 * </ul>
	 * and hence to make the "Next" button active when it is in
	 * <p>
	 * <code>false</code> if the user should
	 * <ul>
	 * <li>close the shutter</li>
	 * <li>move the stick out</li>
	 * </ul>
	 */
	private final boolean moveIn;

	/**
	 * <code>true</code> if the user has pressed "Move in"
	 * <code>false</code> if the user has pressed "Move out"
	 */
	private Boolean movingIn;

	private Button btnMoveIn;
	private Button btnMoveOut;
	private Button btnStop;

	private final Scannable diagnosticPositioner;
	private final Scannable exitSlitShutter;

	private ProgressBar progressBar;

	public ConfigureExitSlitsMoveDiagnostic(Composite parent, int style, String title, String description, ConfigureExitSlitsParameters params, boolean moveIn) {
		super(parent, style, title, description);
		this.moveIn = moveIn;
		diagnosticPositioner = params.getDiagnosticPositioner();
		exitSlitShutter = params.getExitSlitShutter();

		GridLayoutFactory.swtDefaults().numColumns(COLUMNS).applyTo(this);
		setBackground(COLOUR_WHITE);

		// Diagnostic position control
		final ReadonlyScannableComposite diagnosticPosition = new ReadonlyScannableComposite(this, SWT.TRANSPARENT, diagnosticPositioner, "Diagnostic stick positioner", "", 0, true);
		GridDataFactory.swtDefaults().applyTo(diagnosticPosition);
		diagnosticPosition.setMinPeriodMS(SCANNABLE_UPDATE_PERIOD);
		diagnosticPosition.setColourMap(DIAGNOSTIC_COLOUR_MAP);
		diagnosticPosition.setBackground(COLOUR_WHITE);

		btnMoveIn = createButton(this, "Move in", "Move diagnostic into the beam and open slits");
		btnMoveIn.addSelectionListener(widgetSelectedAdapter(e -> moveIn()));

		btnMoveOut = createButton(this, "Move out", "Close slits and move diagnostic out of the beam");
		btnMoveOut.addSelectionListener(widgetSelectedAdapter(e -> moveOut()));

		btnStop = createStopButton(this);
		btnStop.addSelectionListener(widgetSelectedAdapter(e -> stopMotors()));

		progressBar = new ProgressBar(this, SWT.INDETERMINATE);
		progressBar.setVisible(false);

		// Shutter position (changed automatically in sync with diagnostic)
		final ReadonlyScannableComposite shutterPosition = new ReadonlyScannableComposite(this, SWT.TRANSPARENT, exitSlitShutter, "Exit slit shutter", "", 0, true);
		GridDataFactory.swtDefaults().applyTo(shutterPosition);
		shutterPosition.setMinPeriodMS(SCANNABLE_UPDATE_PERIOD);
		shutterPosition.setColourMap(SHUTTER_COLOUR_MAP);
		shutterPosition.setBackground(COLOUR_WHITE);

		// Synchronise buttons with hardware state
		updateButtons();
	}

	private void moveIn() {
		stopping = false;
		movingIn = true;
		try {
			if (!isDiagnosticIn()) {
				moveDiagnosticPositioner(DIAGNOSTIC_IN);
			} else if (!isShutterOpen())  {
				moveShutter(SHUTTER_OPEN);
			}
		} catch (DeviceException e) {
			displayError(MOTOR_ERROR, "Error moving positioner in", e, logger);
		}
	}

	private void moveOut() {
		stopping = false;
		movingIn = false;
		try {
			if (!isShutterClosed()) {
				moveShutter(SHUTTER_CLOSE);
			} else if (!isDiagnosticOut()) {
				moveDiagnosticPositioner(DIAGNOSTIC_OUT);
			}
		} catch (DeviceException e) {
			displayError(MOTOR_ERROR, "Error moving positioner out", e, logger);
		}
	}

	private void stopMotors() {
		stopping = true;
		try {
			diagnosticPositioner.stop();
		} catch (DeviceException e) {
			displayError(MOTOR_ERROR, "Error stopping diagnostic motor", e, logger);
		}
		try {
			exitSlitShutter.stop();
		} catch (DeviceException e) {
			displayError(MOTOR_ERROR, "Error stopping exit slit shutter", e, logger);
		}
	}

	private void moveDiagnosticPositioner(String position) {
		try {
			diagnosticPositioner.asynchronousMoveTo(position);
		} catch (DeviceException ex) {
			final String message = String.format("Error moving diagnostic positioner %s", diagnosticPositioner.getName());
			displayError(MOTOR_ERROR, message, ex, logger);
		}
	}

	private boolean isDiagnosticIn() throws DeviceException {
		return diagnosticPositioner.getPosition().equals(DIAGNOSTIC_IN);
	}

	private boolean isDiagnosticOut() throws DeviceException {
		return diagnosticPositioner.getPosition().equals(DIAGNOSTIC_OUT);
	}

	private boolean isDiagnosticInPosition() throws DeviceException {
		if (moveIn) {
			return isDiagnosticIn();
		} else {
			return isDiagnosticOut();
		}
	}

	private void moveShutter(String position) {
		try {
			exitSlitShutter.asynchronousMoveTo(position);
		} catch (DeviceException ex) {
			final String message = String.format("Error moving exit slit shutter %s", exitSlitShutter.getName());
			displayError(MOTOR_ERROR, message, ex, logger);
		}
	}

	private boolean isShutterOpen() throws DeviceException {
		return exitSlitShutter.getPosition().equals(SHUTTER_OPEN);
	}

	private boolean isShutterClosed() throws DeviceException {
		return exitSlitShutter.getPosition().equals(SHUTTER_CLOSED);
	}

	private boolean isShutterInPosition() throws DeviceException {
		if (moveIn) {
			return isShutterOpen();
		} else {
			return isShutterClosed();
		}
	}

	@Override
	protected void onUpdate(Object source, Object arg) {
		if (arg instanceof ScannablePositionChangeEvent && !stopping) {
			// If the first motor is in place, start the second one
			try {
				if (movingIn) {
					// Wait for diagnostic to move in, then open shutter
					if (source == diagnosticPositioner && isDiagnosticIn() && !isShutterOpen()) {
						moveShutter(SHUTTER_OPEN);
					}
				} else {
					// Wait for shutter to close, then move diagnostic out
					if (source == exitSlitShutter && isShutterClosed() && !isDiagnosticOut()) {
						moveDiagnosticPositioner(DIAGNOSTIC_OUT);
					}
				}
			} catch (Exception e) {
				displayError(MOTOR_ERROR, "Error moving positioner out", e, logger);
			}
		}
		super.onUpdate(source, arg);
	}

	@Override
	protected void updateButtons() {
		if (btnMoveIn != null && btnMoveOut != null) {
			try {
				btnMoveIn.setEnabled(!isBusy() && (!isDiagnosticIn() || !isShutterOpen()));
				btnMoveOut.setEnabled(!isBusy() && (!isDiagnosticOut() || !isShutterClosed()));
				btnStop.setEnabled(isBusy());
				progressBar.setVisible(isBusy());
			} catch (DeviceException e) {
				displayError(MOTOR_ERROR, "Error getting device information", e, logger);
			}
		}
	}

	@Override
	public boolean canGoToNextPage() {
		try {
			return !isBusy() && isDiagnosticInPosition() && isShutterInPosition();
		} catch (DeviceException e) {
			logger.error("Error getting position of device", e);
			return false;
		}
	}

	@Override
	public boolean canGoToPreviousPage() {
		return !isBusy();
	}

	private boolean isBusy() {
		try {
			return diagnosticPositioner.isBusy() || exitSlitShutter.isBusy();
		} catch (DeviceException e) {
			logger.error("Error getting busy status of device", e);
			return true;
		}
	}

	@Override
	public void setVisible(boolean visible) {
		if (visible) {
			diagnosticPositioner.addIObserver(updateObserver);
			exitSlitShutter.addIObserver(updateObserver);
		} else {
			diagnosticPositioner.deleteIObserver(updateObserver);
			exitSlitShutter.deleteIObserver(updateObserver);
		}
		super.setVisible(visible);
	}

	@Override
	public void dispose() {
		diagnosticPositioner.deleteIObserver(updateObserver);
		exitSlitShutter.deleteIObserver(updateObserver);
		super.dispose();
	}
}
