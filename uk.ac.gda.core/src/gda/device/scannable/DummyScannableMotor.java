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

package gda.device.scannable;

import gda.device.DeviceException;
import gda.device.IScannableMotor;
import gda.device.Motor;

public class DummyScannableMotor extends DummyScannable implements IScannableMotor {

	private double speed = 1.0;
	private double motorResolution = 0.5;
	private double demandPositionTolerance = 0.01;
	private double userOffset = 0.0;
	private double timeToVelocity = 0.1;
	private Double lowerMotorLimit;
	private Double upperMotorLimit;
	private Double lowerInnerLimit;
	private Double upperInnerLimit;

	private Motor motor = null;
	private String motorName = "DummyScannableMotor";

	@Override
	public double getSpeed() throws DeviceException {
		return speed;
	}

	@Override
	public double getMotorResolution() throws DeviceException {
		return motorResolution;
	}

	@Override
	public double getDemandPositionTolerance() {
		return demandPositionTolerance;
	}

	@Override
	public double getUserOffset() throws DeviceException {
		return userOffset;
	}

	@Override
	public double getTimeToVelocity() throws DeviceException {
		return timeToVelocity;
	}

	@Override
	public void setSpeed(double requiredSpeed) throws DeviceException {
		this.speed = requiredSpeed;
	}

	@Override
	public void setMotor(Motor motor) {
		this.motor = motor;
	}

	@Override
	public Motor getMotor() {
		return motor;
	}

	@Override
	public void setMotorName(String motorName) {
		this.motorName = motorName;
	}

	@Override
	public String getMotorName() {
		return motorName;
	}

	@Override
	public void setTimeToVelocity(double timeToVelocity) throws DeviceException {
		this.timeToVelocity = timeToVelocity;
	}

	@Override
	public void setPosition(Object position) throws DeviceException {
		this.currentPosition = (Double) position;
	}

	@Override
	public Double getLowerMotorLimit() throws DeviceException {
		return lowerMotorLimit;
	}

	@Override
	public Double getUpperMotorLimit() throws DeviceException {
		return upperMotorLimit;
	}

	@Override
	public Double getLowerInnerLimit() throws DeviceException {
		return lowerInnerLimit;
	}

	@Override
	public Double getUpperInnerLimit() throws DeviceException {
		return upperInnerLimit;
	}

}
