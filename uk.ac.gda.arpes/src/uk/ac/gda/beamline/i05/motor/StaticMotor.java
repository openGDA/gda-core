/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package uk.ac.gda.beamline.i05.motor;

import gda.device.MotorException;
import gda.device.motor.DummyMotor;

public class StaticMotor extends DummyMotor {

	private double fixedPosition = 0;

	public double getFixedPosition() {
		return fixedPosition;
	}
	public void setFixedPosition(double fixedPosition) {
		this.fixedPosition = fixedPosition;
	}
	@Override
	public void configure() {
		super.configure();
		super.setPosition(fixedPosition);
	}
	
	@Override
	public void moveBy(double steps) throws MotorException {
	}

	@Override
	public void moveTo(double steps) throws MotorException {
	}

	@Override
	public void moveContinuously(int direction) {
	}

	@Override
	public void setPosition(double steps) {
	}
}