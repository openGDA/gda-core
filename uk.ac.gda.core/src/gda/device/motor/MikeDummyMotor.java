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

import gda.device.MotorException;
import gda.device.MotorStatus;

/**
 * A Dummy motor class
 */
public class MikeDummyMotor extends MotorBase {
	private static final Logger logger = LoggerFactory.getLogger(MikeDummyMotor.class);

	private double currentposition;

	private volatile boolean motorMoving = false;

	private double currentspeed;

	private double targetposition;

	private MotorStatus mystatus;

	private double posinc; // chunks of drive to add while busy

	private int nCalls; // no of getstatus calls made during the move

	private int nCallsBusy = 0;

	// no of getstatus calls returning busy in a move
	private int nCallsBusyMax = 3; // upper limit of nCallsBusy

	/**
	 * Constructor
	 */
	public MikeDummyMotor() {
		mystatus = MotorStatus.READY;
	}

	@Override
	public String toString() {
		return ("A DummyMotor named " + getName());
	}

	@Override
	public void configure() {
		logger.debug("The name of this DummyMotor is " + getName());
		loadPosition(getName());
		logger.debug("Loaded motor position " + getPosition());
	}

	@Override
	public synchronized void moveTo(double newpos) {
		targetposition = newpos;
		posinc = (targetposition - currentposition) / (nCallsBusy + 1);
		// moves in turn allow 0 - nCallsBusyMax busy statuses before completion
		nCallsBusy = nCallsBusy + 1;
		if (nCallsBusy > nCallsBusyMax) {
			nCallsBusy = 0;
		}
		motorMoving = true;
		mystatus = MotorStatus.BUSY;
		nCalls = 0;
	}

	@Override
	public synchronized void moveContinuously(int direction) {
		mystatus = MotorStatus.BUSY;
		nCalls = 0;
	}

	@Override
	public synchronized void moveBy(double amount) {
		moveTo(currentposition + amount);
	}

	@Override
	public synchronized void setPosition(double newposition) {
		currentposition = newposition;
	}

	@Override
	public synchronized double getPosition() {
		return (currentposition);
	}

	@Override
	public synchronized void setSpeed(double stepsPerSecond) throws MotorException {
		currentspeed = stepsPerSecond;
	}

	@Override
	public synchronized double getSpeed() throws MotorException {
		return (currentspeed);
	}

	@Override
	public synchronized void stop() throws MotorException {
	}

	@Override
	public synchronized void panicStop() throws MotorException {
	}

	@Override
	public boolean isMoving() {
		return motorMoving;
	}

	@Override
	public synchronized MotorStatus getStatus() {
		// stay busy for nCallsBusy getStatus calls, increasing position each
		// time
		if ((mystatus == MotorStatus.BUSY) && (nCalls < nCallsBusy)) // disable
		// chunks
		{
			nCalls++;
			currentposition = currentposition + posinc;
			// FIXME: Clarify comment below or remove.
			// **** try without Thread.yield();
			return MotorStatus.BUSY;
		}
		currentposition = targetposition;
		motorMoving = false;
		return MotorStatus.READY;
	}
}
