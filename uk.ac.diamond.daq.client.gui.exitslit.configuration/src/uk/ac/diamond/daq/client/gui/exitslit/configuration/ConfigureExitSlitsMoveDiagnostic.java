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
import static uk.ac.diamond.daq.client.gui.exitslit.configuration.ConfigureExitSlitsUtils.createInstructionLabel;
import static uk.ac.diamond.daq.client.gui.exitslit.configuration.ConfigureExitSlitsUtils.createSeparator;
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
import uk.ac.diamond.daq.concurrent.Async;
import uk.ac.gda.dls.client.views.ReadonlyScannableComposite;

/**
 * Composite to move the diagnostic unit into or out of the beam, coordinated with moving exit slits and
 * starting/stopping data acquisition
 * <p>
 * The constructor parameter "moveIn" controls how the devices should be moved (see below)
 */
public class ConfigureExitSlitsMoveDiagnostic extends ConfigureExitSlitsComposite {
	private static final Logger logger = LoggerFactory.getLogger(ConfigureExitSlitsMoveDiagnostic.class);

	private static final String DIAGNOSTIC_IN = "Screen";
	private static final String DIAGNOSTIC_OUT = "Out";

	private static final String SHUTTER_OPEN = "Open";
	private static final String SHUTTER_CLOSED = "Closed";

	private static final int COLUMNS = 5;

	private volatile boolean stopping = false;

	/**
	 * <code>true</code> if the user should<br>
	 * <ul>
	 * <li>move the diagnostic stick in</li>
	 * <li>open the shutter</li>
	 * <li>start data acquisition</li>
	 * </ul>
	 * <p>
	 * <code>false</code> if the user should
	 * <ul>
	 * <li>stop data acquisition</li>
	 * <li>close the shutter</li>
	 * <li>move the stick out</li>
	 * </ul>
	 */
	private final boolean moveIn;

	/**
	 * Number of pages to go forwards if the user presses the "Skip" button
	 * <p>
	 * This is relevant only on the "move diagnostic in" screen.<br>
	 * If the user does not want to tweak the mirror pitch, they can go directly to moving the horizontal slit position.
	 */
	private static final int SKIP_AMOUNT = 4;

	private Button btnMoveIn;
	private Button btnMoveOut;
	private Button btnStop;

	private final Scannable diagnosticPositioner;
	private final Scannable exitSlitShutter;

	private ProgressBar progressBar;

	private ConfigureExitSlitsParameters params;

	public ConfigureExitSlitsMoveDiagnostic(Composite parent, String title, String description, String instructions, ConfigureExitSlitsParameters params, boolean moveIn) {
		super(parent, title, description);
		this.moveIn = moveIn;
		this.params = params;
		diagnosticPositioner = params.getDiagnosticPositioner();
		exitSlitShutter = params.getExitSlitShutter();

		GridLayoutFactory.swtDefaults().numColumns(COLUMNS).applyTo(this);
		setBackground(COLOUR_WHITE);

		// Instructions
		GridDataFactory.swtDefaults().span(COLUMNS, 1).applyTo(createInstructionLabel(this, instructions));

		GridDataFactory.fillDefaults().span(COLUMNS, 1).grab(true,  false).applyTo(createSeparator(this));

		// Diagnostic position control
		final ReadonlyScannableComposite diagnosticPosition = new ReadonlyScannableComposite(this, SWT.TRANSPARENT, diagnosticPositioner, "Diagnostic stick positioner", "", 0, true);
		GridDataFactory.swtDefaults().applyTo(diagnosticPosition);
		diagnosticPosition.setMinPeriodMS(SCANNABLE_UPDATE_PERIOD);
		diagnosticPosition.setColourMap(DIAGNOSTIC_COLOUR_MAP);
		diagnosticPosition.setBackground(COLOUR_WHITE);

		btnMoveIn = createButton(this, "Move in", "Move diagnostic into the beam and open slits");
		btnMoveIn.addSelectionListener(widgetSelectedAdapter(e -> Async.execute(this::moveIn)));

		btnMoveOut = createButton(this, "Move out", "Close slits and move diagnostic out of the beam");
		btnMoveOut.addSelectionListener(widgetSelectedAdapter(e -> Async.execute(this::moveOut)));

		btnStop = createStopButton(this);
		btnStop.addSelectionListener(widgetSelectedAdapter(e -> stopMotors()));

		progressBar = new ProgressBar(this, SWT.INDETERMINATE);
		progressBar.setVisible(false);

		// Shutter position
		final ReadonlyScannableComposite shutterPosition = new ReadonlyScannableComposite(this, SWT.TRANSPARENT, exitSlitShutter, "Exit slit shutter", "", 0, true);
		GridDataFactory.swtDefaults().applyTo(shutterPosition);
		shutterPosition.setMinPeriodMS(SCANNABLE_UPDATE_PERIOD);
		shutterPosition.setColourMap(SHUTTER_COLOUR_MAP);
		shutterPosition.setBackground(COLOUR_WHITE);

		// If everything is already in position, start data acquisition automatically
		try {
			if (moveIn && isDiagnosticInPosition() && isShutterInPosition()) {
				params.getCameraControl().startAcquiring();
			}
		} catch (DeviceException ex) {
			displayError(MOTOR_ERROR, "Error starting data acquisition", ex, logger);
		}

		// Synchronise buttons with hardware state
		updateButtons();
	}

	private void moveIn() {
		stopping = false;
		try {
			diagnosticPositioner.moveTo(DIAGNOSTIC_IN);
			if (!stopping) {
				exitSlitShutter.moveTo(SHUTTER_OPEN);
			}
			if (!stopping) {
				params.getCameraControl().startAcquiring();
			}
		} catch (DeviceException e) {
			displayError(MOTOR_ERROR, "Error moving positioner in", e, logger);
		}
	}

	private void moveOut() {
		stopping = false;
		try {
			params.getCameraControl().stopAcquiring();
			if (!stopping) {
				exitSlitShutter.moveTo(SHUTTER_CLOSED);
			}
			if (!stopping) {
				diagnosticPositioner.moveTo(DIAGNOSTIC_OUT);
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

	private boolean isDiagnosticIn() throws DeviceException {
		return diagnosticPositioner.getPosition().equals(DIAGNOSTIC_IN);
	}

	private boolean isDiagnosticOut() throws DeviceException {
		return diagnosticPositioner.getPosition().equals(DIAGNOSTIC_OUT);
	}

	private boolean isDiagnosticInPosition() throws DeviceException {
		return moveIn ? isDiagnosticIn() : isDiagnosticOut();
	}

	private boolean isShutterOpen() throws DeviceException {
		return exitSlitShutter.getPosition().equals(SHUTTER_OPEN);
	}

	private boolean isShutterClosed() throws DeviceException {
		return exitSlitShutter.getPosition().equals(SHUTTER_CLOSED);
	}

	private boolean isShutterInPosition() throws DeviceException {
		return moveIn ? isShutterOpen() : isShutterClosed();
	}

	@Override
	protected void updateButtons() {
		try {
			if (btnMoveIn != null) {
				btnMoveIn.setEnabled(!isBusy() && (!isDiagnosticIn() || !isShutterOpen()));
			}
			if (btnMoveOut != null) {
				btnMoveOut.setEnabled(!isBusy() && (!isDiagnosticOut() || !isShutterClosed()));
			}
			if (btnStop != null) {
				btnStop.setEnabled(isBusy());
			}
			if (progressBar != null) {
				progressBar.setVisible(isBusy());
			}
		} catch (DeviceException e) {
			displayError(MOTOR_ERROR, "Error getting device information", e, logger);
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
	public boolean canSkip() {
		// If the user does not need to tweak the mirror pitch, they can skip the
		// apertures out - tweak mirror - apertures in sequence
		return moveIn && canGoToNextPage();
	}

	@Override
	protected int getSkipAmount() {
		// Skipping is only relevant on the "move diagnostic in" screen
		return moveIn ? SKIP_AMOUNT : 0;
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
