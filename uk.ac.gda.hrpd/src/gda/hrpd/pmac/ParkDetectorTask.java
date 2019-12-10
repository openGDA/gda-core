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

package gda.hrpd.pmac;

import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.Scannable;

public class ParkDetectorTask implements Callable<Boolean> {
	private static final Logger logger = LoggerFactory.getLogger(ParkDetectorTask.class);

	private Scannable motor;
	private SafePosition safePosition;
	private boolean active;

	@Override
	public Boolean call() throws Exception {
		if (active) {
			if (Math.abs((double)motor.getPosition() - safePosition.getPosition()) < safePosition.getTolerance()) {
				logger.debug("Motor {} already in correct position", motor.getName());
				return false;
			} else {
				logger.debug("Moving {} to {}", motor.getName(), safePosition.getPosition());
				try {
					motor.moveTo(safePosition.getPosition());
					return true;
				} catch (DeviceException de) {
					logger.error("Error moving detector to safe space", de);
				}
			}
		}
		return false;
	}


	public Scannable getMotor() {
		return motor;
	}


	public void setMotor(Scannable motor) {
		this.motor = motor;
	}


	public SafePosition getSafePosition() {
		return safePosition;
	}


	public void setSafePosition(SafePosition position) {
		this.safePosition = position;
	}


	public boolean isActive() {
		return active;
	}


	public void setActive(boolean active) {
		if (motor == null || safePosition == null) {
			throw new IllegalStateException("Motor and position are required to park detectors");
		}
		logger.info("Setting park task to active for {}", motor.getName());
		this.active = active;
	}

	@Override
	public String toString() {
		return String.format("ParkDetectorTask for %s (%sabled) (position=%f)", motor.getName(), active ? "en" : "dis", safePosition.getPosition());
	}

	/**
	 * For use in scripts to allow calls such as {@code autoParkDelta(True)}
	 * @param state Can be true/false, "on"/"off", or integer (non-zero for active)
	 */
	public void __call__(Object state) {
		if (state instanceof Boolean) {
			setActive((Boolean) state);
		} else if (state instanceof String) {
			String newState = (String) state;
			setActive(newState.equalsIgnoreCase("on"));
		} else if (state instanceof Integer) {
			setActive((Integer) state != 0);
		} else {
			throw new IllegalArgumentException("Unknown active state: " + state
					+ ". Should be True/False, \"on\"/\"off\", or 1/0");
		}
	}

	public void on() {
		setActive(true);
	}

	public void off() {
		setActive(false);
	}
}
