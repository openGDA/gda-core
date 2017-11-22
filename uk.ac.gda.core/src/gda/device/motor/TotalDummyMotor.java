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

import gda.device.Motor;
import gda.device.MotorException;
import gda.device.MotorStatus;
import gda.device.scannable.MotorUnitStringSupplier;
import gda.observable.IObservable;

public class TotalDummyMotor extends MotorBase implements Runnable, IObservable, Motor, MotorUnitStringSupplier {

	private double posn = 0.0;

	private double speed = 2;

	private MotorStatus status = MotorStatus.READY;

	private boolean moving = false;

	@Override
	public void configure(){
		// no configuration required
	}

	@Override
	public void moveBy(double steps) throws MotorException {
		moving = true;
		posn += steps;
		moving = false;
	}

	@Override
	public void moveTo(double steps) throws MotorException {
		moving = true;
		posn = steps;
		moving = false;
	}

	@Override
	public void moveContinuously(int direction) throws MotorException {
		moving = true;
		posn = posn + direction * 10;
	}

	@Override
	public void setPosition(double steps) throws MotorException {
		posn = steps;
	}

	@Override
	public double getPosition() throws MotorException {
		return posn;
	}

	@Override
	public void setSpeed(double speed) throws MotorException {
		this.speed = speed;
	}

	@Override
	public double getSpeed() throws MotorException {
		return speed;
	}

	@Override
	public void stop() throws MotorException {
		moving = false;
	}

	@Override
	public void panicStop() throws MotorException {
		moving = false;
	}

	@Override
	public MotorStatus getStatus() {
		return status;
	}

	@Override
	public boolean isMoving() throws MotorException {
		return moving;
	}

	@Override
	public void run() {
		// not required to do anything
	}

	@Override
	public String getUnitString() throws MotorException {
		return "mm";
	}
}
