/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package uk.ac.gda.client.tomo.alignment.view.handlers.impl;

import gda.device.DeviceException;
import gda.device.IScannableMotor;

import java.util.ArrayList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseMotorHandler {
	
	private static final Logger logger = LoggerFactory.getLogger(BaseMotorHandler.class);
	
	private ArrayList<IScannableMotor> motorsRunning;

	public BaseMotorHandler() {
		motorsRunning = new ArrayList<IScannableMotor>();
	}

	protected void stopAllMotors() throws DeviceException {
		for (IScannableMotor motor : motorsRunning) {
			motor.stop();
		}
		motorsRunning.clear();
	}

	protected void moveMotor(final IProgressMonitor monitor, final IScannableMotor motor, final double newPosition)
			throws DeviceException, InterruptedException {

		double motorSpeed = motor.getSpeed();

		double position = (Double) motor.getPosition();

		double distance = Math.abs(position - newPosition);

		double timeInSeconds = distance / motorSpeed;

		logger.debug(String.format("Time to move %1$f is %2$f", distance, timeInSeconds));
		logger.debug(String.format("Speed of motor %1$s is %2$f", motor.getName(), motor.getSpeed()));
		motorsRunning.add(motor);

		motor.asynchronousMoveTo(newPosition);

		int totalTimeTakenInMills = (int) (timeInSeconds * 1000);

		final int step = totalTimeTakenInMills / 10000;

		SubMonitor progress = SubMonitor.convert(monitor,
				String.format("Moving %s from %.3g to %.3g", motor.getName(), position, newPosition), 10000);
		int count = 0;
		while (motor.isBusy()) {
			Double currPos = (Double) motor.getPosition();
			progress.subTask(String.format("%s position: %.3g", motor.getName(), currPos));
			progress.worked(1);
			Thread.sleep(step);
			count++;
			if (monitor.isCanceled()) {
				motor.stop();
				throw new InterruptedException("User Cancelled");
			}

		}
		logger.debug("Motor queried count is {}", count);
		motorsRunning.remove(motor);
	}
}
