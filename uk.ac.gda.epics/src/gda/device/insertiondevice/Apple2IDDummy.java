/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package gda.device.insertiondevice;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.MotorException;
import gda.device.motor.DummyMotor;
import gda.factory.FactoryException;

public class Apple2IDDummy extends Apple2IDBase {

	private static final Logger logger = LoggerFactory.getLogger(Apple2IDDummy.class);

	private DummyMotor gapMotor;
	private DummyMotor topOuterMotor;
	private DummyMotor topInnerMotor;
	private DummyMotor bottomOuterMotor;
	private DummyMotor bottomInnerMotor;

	private List<DummyMotor> motors;

	private double speed;

	public Apple2IDDummy() {
		super();
		motors = new ArrayList<>();
	}

	private boolean motorsMoving() {
		for (DummyMotor motor : motors) {
			if (motor.isMoving()) {
				return true;
			}
		}
		return false;
	}

	// DeviceBase overrides

	@Override
	public void configure() throws FactoryException {
		gapMotor = new DummyMotor();
		topOuterMotor = new DummyMotor();
		topInnerMotor = new DummyMotor();
		bottomOuterMotor = new DummyMotor();
		bottomInnerMotor = new DummyMotor();

		gapMotor.setName("gapMotor");
		topOuterMotor.setName("topOuterMotor");
		topInnerMotor.setName("topInnerMotor");
		bottomOuterMotor.setName("bottomOuterMotor");
		bottomInnerMotor.setName("bottomInnerMotor");

		motors.add(gapMotor);
		motors.add(topOuterMotor);
		motors.add(topInnerMotor);
		motors.add(bottomOuterMotor);
		motors.add(bottomInnerMotor);

		for (final DummyMotor motor : motors) {
			try {
				motor.setSpeed(speed);
			} catch (MotorException e) {
				logger.error("Error setting speed for " + motor.getName(), e);
			}
			motor.configure();
		}

		logger.info("Motors configured");
	}

	// Implementations of abstract base class methods

	@Override
	protected void doMove(Apple2IDPosition position) throws DeviceException {
		gapMotor.moveTo(position.gap);
		topOuterMotor.moveTo(position.topOuterPos);
		topInnerMotor.moveTo(position.topInnerPos);
		bottomOuterMotor.moveTo(position.bottomOuterPos);
		bottomInnerMotor.moveTo(position.bottomInnerPos);

		// Start a thread to wait for all motors to stop moving
		final Thread monitorMotors = new Thread() {
			@Override
			public void run() {
				while (motorsMoving()) {
					try {
						sleep(200);
					} catch (Throwable th) {
						logger.warn("Dummy motor monitor interrupted", th);
						break;
					}
				}
				onMoveFinished();
			}
		};
		monitorMotors.start();
	}

	@Override
	protected double getMotorPosition(IDMotor motor) throws DeviceException {
		switch (motor) {
		case GAP:
			return gapMotor.getPosition();
		case TOP_OUTER:
			return topOuterMotor.getPosition();
		case TOP_INNER:
			return topInnerMotor.getPosition();
		case BOTTOM_OUTER:
			return bottomOuterMotor.getPosition();
		case BOTTOM_INNER:
			return bottomInnerMotor.getPosition();
		}
		throw new DeviceException("Invalid motor " + motor + " requested");
	}

	@Override
	public boolean isEnabled() throws DeviceException {
		return true;
	}

	@Override
	public String getIDMode() throws DeviceException {
		return GAP_AND_PHASE_MODE;
	}

	// Object configuration

	public double getSpeed() {
		return speed;
	}

	public void setSpeed(double speed) {
		this.speed = speed;
	}
}
