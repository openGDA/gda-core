/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package uk.ac.gda.ui.tool.spring;

import static uk.ac.gda.core.tool.spring.SpringApplicationContextFacade.publishEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import gda.device.DeviceException;
import gda.device.IScannableMotor;
import gda.observable.IObserver;
import uk.ac.gda.client.composites.FinderHelper;
import uk.ac.gda.client.event.ScannableStateEvent;

/**
 * A collection of utilities to move {@link IScannableMotor} so the motor publishes its position as {@link ScannableStateEvent}.
 *
 * @author Maurizio Nagni
 */
@Component
public class MotorUtils {

	private static final Logger logger = LoggerFactory.getLogger(MotorUtils.class);

	private MotorUtils() {}

	/**
	 * Moves a motor asynchronously.
	 *
	 * @param findableMotor the name of a findable {@link IScannableMotor}
	 * @param position where move the motor
	 */
	public void moveMotorAsynchronously(String findableMotor, double position) {
		FinderHelper.getIScannableMotor(findableMotor)
			.ifPresent(motor -> moveMotorAsynchronously(motor, position));
	}

	/**
	 * Moves a motor asynchronously.
	 *
	 * @param motor a {@link IScannableMotor}
	 * @param position where move the motor
	 */
	public void moveMotorAsynchronously(IScannableMotor motor, double position) {
		try {
			motor.addIObserver(new MotorObserver(position));
			motor.asynchronousMoveTo(position);
		} catch (DeviceException e) {
			logger.error("Error moving {}", motor);
		}
	}

	/**
	 * Moves a motor synchronously.
	 *
	 * @param findableMotor the name of a findable {@link IScannableMotor}
	 * @param position where move the motor
	 */
	public void moveMotorSynchronously(String findableMotor, double position) {
		FinderHelper.getIScannableMotor(findableMotor)
			.ifPresent(motor -> moveMotorSynchronously(motor, position));
	}

	/**
	 * Moves a motor synchronously.
	 *
	 * @param motor a {@link IScannableMotor}
	 * @param position where move the motor
	 */
	public void moveMotorSynchronously(IScannableMotor motor, double position) {
		try {
			motor.addIObserver(new MotorObserver(position));
			if (!motor.isBusy()) {
				motor.moveTo(position);
			}
		} catch (DeviceException e) {
			logger.error("Error moving {}", motor);
		}
	}

	private class MotorObserver implements IObserver {
		private final double position;

		MotorObserver(double position) {
			this.position = position;
		}

		@Override
		public void update(Object source, Object arg) {
			IScannableMotor motor = (IScannableMotor) source;
			try {
				publishEvent(new ScannableStateEvent(motor, motor.getName(), (double) motor.getPosition()));
				if (motor.isAt(position)) {
					motor.deleteIObserver(this);
				}
			} catch (DeviceException e) {
				logger.error("Cannot read motor position at {}", position);
			}
		}
	}
}
