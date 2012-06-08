/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

package gda.device.motor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.Motor;
import gda.device.MotorException;
import gda.device.MotorStatus;
import gda.factory.Configurable;
import gda.factory.Finder;
import gda.observable.IObservable;

/**
 * A class to control Delta Tau PMAC motors
 */
public class DeltaTauMotor extends MotorBase implements Configurable, IObservable, Motor {
	
	private static final Logger logger = LoggerFactory.getLogger(DeltaTauMotor.class);
	
	private String deltaTauControllerName = null;

	private DeltaTauController deltaTauController = null;

	private int motorNumber;

	private boolean motorMoving = false;

	@Override
	public void configure() {
		if ((deltaTauController = (DeltaTauController) Finder.getInstance().find(deltaTauControllerName)) == null) {
			logger.error("deltaTauController " + deltaTauControllerName + " not found");
		}
	}

	@Override
	public void moveBy(double steps) throws MotorException {
		deltaTauController.sendCommand("#" + motorNumber + "J^" + steps);
		motorMoving = true;
	}

	@Override
	public void moveTo(double steps) throws MotorException {
		deltaTauController.sendCommand("#" + motorNumber + "J=" + steps);
		motorMoving = true;
	}

	@Override
	public void moveContinuously(int direction) throws MotorException {
		// not implmented yet
	}

	@Override
	public void setPosition(double steps) throws MotorException {
		// not implmented yet
	}

	@Override
	public double getPosition() throws MotorException {
		double value = Double.NaN;
		String reply = deltaTauController.sendCommand("#" + motorNumber + "P");
		value = Double.parseDouble(reply);
		return value;
	}

	@Override
	public void setSpeed(double speed) throws MotorException {
		// not implmented yet
	}

	@Override
	public double getSpeed() throws MotorException {
		return Double.NaN;
		// not implmented yet
	}

	@Override
	public void stop() throws MotorException {
		deltaTauController.sendCommand("#" + motorNumber + "J/");
	}

	@Override
	public void panicStop() throws MotorException {
		// not implmented yet
	}

	@Override
	public MotorStatus getStatus() throws MotorException {
		MotorStatus status = MotorStatus.READY;

		// deltaTauController.sendCommand("?");
		return status;
	}

	@Override
	public boolean isMoving() throws MotorException {
		return motorMoving;
	}

	/**
	 * @return controller name
	 */
	public String getDeltaTauControllerName() {
		return deltaTauControllerName;
	}

	/**
	 * @param deltaTauControllerName
	 */
	public void setDeltaTauControllerName(String deltaTauControllerName) {
		this.deltaTauControllerName = deltaTauControllerName;
	}

	/**
	 * @return Returns the motorNumber.
	 */
	public int getMotorNumber() {
		return motorNumber;
	}

	/**
	 * @param motorNumber
	 *            The motorNumber to set.
	 */
	public void setMotorNumber(int motorNumber) {
		this.motorNumber = motorNumber;
	}
}
