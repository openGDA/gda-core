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

import static uk.ac.diamond.daq.client.gui.exitslit.configuration.ConfigureExitSlitsUtils.COLOUR_WHITE;
import static uk.ac.diamond.daq.client.gui.exitslit.configuration.ConfigureExitSlitsUtils.createInstructionLabel;
import static uk.ac.diamond.daq.client.gui.exitslit.configuration.ConfigureExitSlitsUtils.createSeparator;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.IScannableMotor;
import gda.rcp.views.NudgePositionerComposite;

/**
 * Composite to handle tweaking a motor position until the user is satisfied that the position is correct.
 * <p>
 * The composite consists of a the name of the motor, its current value and increment/decrement buttons
 */
public class ConfigureExitSlitsNudgeMotor extends ConfigureExitSlitsComposite {
	private static final Logger logger = LoggerFactory.getLogger(ConfigureExitSlitsNudgeMotor.class);

	private final IScannableMotor motor;

	public ConfigureExitSlitsNudgeMotor(Composite parent, String title, String description, String instructions,
			IScannableMotor motor, String displayName, double initialTweakAmount) {
		super(parent, title, description);
		this.motor = motor;

		GridLayoutFactory.swtDefaults().applyTo(this);
		setBackground(COLOUR_WHITE);

		// Instructions
		GridDataFactory.swtDefaults().applyTo(createInstructionLabel(this, instructions));
		GridDataFactory.fillDefaults().grab(true,  false).applyTo(createSeparator(this));

		// Motor position with buttons to adjust it
		final NudgePositionerComposite motorPositionComposite = new NudgePositionerComposite(this,
				SWT.HORIZONTAL | SWT.READ_ONLY);
		GridDataFactory.fillDefaults().applyTo(motorPositionComposite);
		motorPositionComposite.setScannable(motor);
		motorPositionComposite.setIncrement(initialTweakAmount);
		motorPositionComposite.setIncrementTextWidth(50);
		motorPositionComposite.setDisplayName(displayName);

		updateButtons();
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
		// Nothing to do - buttons managed by composite
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
