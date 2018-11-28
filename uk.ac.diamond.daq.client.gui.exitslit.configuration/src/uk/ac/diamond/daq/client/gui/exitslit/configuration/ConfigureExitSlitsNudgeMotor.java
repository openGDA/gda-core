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
import static uk.ac.diamond.daq.client.gui.exitslit.configuration.ConfigureExitSlitsUtils.MOTOR_ERROR;
import static uk.ac.diamond.daq.client.gui.exitslit.configuration.ConfigureExitSlitsUtils.SCANNABLE_UPDATE_PERIOD;
import static uk.ac.diamond.daq.client.gui.exitslit.configuration.ConfigureExitSlitsUtils.createButton;
import static uk.ac.diamond.daq.client.gui.exitslit.configuration.ConfigureExitSlitsUtils.createInstructionLabel;
import static uk.ac.diamond.daq.client.gui.exitslit.configuration.ConfigureExitSlitsUtils.createSeparator;
import static uk.ac.diamond.daq.client.gui.exitslit.configuration.ConfigureExitSlitsUtils.createStopButton;
import static uk.ac.diamond.daq.client.gui.exitslit.configuration.ConfigureExitSlitsUtils.displayError;

import java.text.DecimalFormat;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.IScannableMotor;
import uk.ac.gda.dls.client.views.ReadonlyScannableComposite;

/**
 * Composite to handle tweaking a motor position until the user is satisfied that the position is correct.
 * <p>
 * The composite consists of a the name of the motor, its current value and increment/decrement buttons
 */
public class ConfigureExitSlitsNudgeMotor extends ConfigureExitSlitsComposite {
	private static final Logger logger = LoggerFactory.getLogger(ConfigureExitSlitsNudgeMotor.class);

	private final IScannableMotor motor;

	private final Button btnMinus;
	private final Button btnPlus;

	private static final int COLUMNS = 4;

	public ConfigureExitSlitsNudgeMotor(Composite parent, String title, String description, String instructions,
			IScannableMotor motor, String motorDescription, double tweakAmount) {
		super(parent, title, description);
		this.motor = motor;

		GridLayoutFactory.swtDefaults().numColumns(COLUMNS).applyTo(this);
		setBackground(COLOUR_WHITE);

		// Instructions
		GridDataFactory.swtDefaults().span(COLUMNS, 1).applyTo(createInstructionLabel(this, instructions));
		GridDataFactory.fillDefaults().span(COLUMNS, 1).grab(true,  false).applyTo(createSeparator(this));

		// Motor position with buttons to adjust it
		final DecimalFormat motorPositionFormat = new DecimalFormat("#.#####");
		final String motorPositionString = motorPositionFormat.format(tweakAmount);

		final ReadonlyScannableComposite motorPositionComposite = new ReadonlyScannableComposite(this, SWT.NONE, motor, motorDescription, "", 3, true);
		GridDataFactory.swtDefaults().applyTo(motorPositionComposite);
		motorPositionComposite.setBackground(COLOUR_WHITE);
		motorPositionComposite.setMinPeriodMS(SCANNABLE_UPDATE_PERIOD);

		btnMinus = createButton(this, "-", String.format("Decrement value by %s", motorPositionString));
		GridDataFactory.swtDefaults().hint(NUDGE_BUTTON_WIDTH, SWT.DEFAULT).applyTo(btnMinus);
		btnMinus.addSelectionListener(widgetSelectedAdapter(e -> moveBy(-tweakAmount)));

		btnPlus = createButton(this, "+", String.format("Increment value by %s", motorPositionString));
		GridDataFactory.swtDefaults().hint(NUDGE_BUTTON_WIDTH, SWT.DEFAULT).applyTo(btnPlus);
		btnPlus.addSelectionListener(widgetSelectedAdapter(e -> moveBy(tweakAmount)));

		final Button btnStop = createStopButton(this);
		btnStop.addSelectionListener(widgetSelectedAdapter(e -> stopMotor()));

		updateButtons();
	}

	private void moveBy(double amount) {
		try {
			final double newPosition = (double) motor.getPosition() + amount;
			motor.asynchronousMoveTo(newPosition);
		} catch (DeviceException e) {
			final String message = String.format("Error moving motor %s", motor.getName());
			displayError(MOTOR_ERROR, message, e, logger);
		}
	}

	private void stopMotor() {
		try {
			motor.stop();
		} catch (DeviceException e) {
			displayError(MOTOR_ERROR, "Error stopping motor", e, logger);
		}
	}

	@Override
	public boolean canGoToNextPage() {
		return !isBusy();
	}

	@Override
	public boolean canGoToPreviousPage() {
		return !isBusy();
	}

	private boolean isBusy() {
		try {
			return motor.isBusy();
		} catch (DeviceException e) {
			logger.error("Error getting busy status for motor {}", motor.getName(), e);
			return true;
		}
	}

	@Override
	protected void updateButtons() {
		if (btnMinus != null && btnPlus != null) {
			try {
				final boolean moving = motor.isBusy();
				btnMinus.setEnabled(!moving);
				btnPlus.setEnabled(!moving);
			} catch (DeviceException e) {
				logger.error("Error acccessing motor {}", motor.getName(), e);
			}
		}
	}

	@Override
	public void setVisible(boolean visible) {
		if (visible) {
			motor.addIObserver(updateObserver);
		} else {
			motor.deleteIObserver(updateObserver);
		}
		super.setVisible(visible);
	}

	@Override
	public void dispose() {
		motor.deleteIObserver(updateObserver);
		super.dispose();
	}
}
