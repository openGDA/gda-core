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

package gda.device.motor.simplemotor;

import org.springframework.beans.factory.InitializingBean;

import gda.device.DeviceException;

/**
 * class that implements SimpleMotorController for a motor that is controller by a multi-axis
 * motor controller. The axis is given by the field index.
 */
public class SimpleMotorViaIndexedController implements SimpleMotorController, InitializingBean{

	SimpleIndexedMotorController simc;
	
	private int index=0;
	
	public void setIndex(int index) {
		this.index = index;
	}

	@Override
	public double getMotorPosition() throws DeviceException {
		return simc.getPosition(index);
	}


	@Override
	public void moveTo(double position) throws DeviceException {
		simc.moveTo(position,index);
	}

	@Override
	public boolean isBusy() throws DeviceException {
		return simc.isBusy();
	}

	
	
	@Override
	public void stop() throws DeviceException {
		simc.stop();
		
	}
	
	@Override
	public void setSpeed(double speed) throws DeviceException {
		simc.setSpeed(speed, index);
	}

	@Override
	public double getSpeed() throws DeviceException {
		return simc.getSpeed(index);
	}
	


	public SimpleIndexedMotorController getSimc() {
		return simc;
	}

	public void setSimc(SimpleIndexedMotorController simc) {
		this.simc = simc;
	}

	public int getIndex() {
		return index;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if( simc == null)
			throw new Exception("p2r is null");
		if( index !=0 && index !=1)
			throw new Exception("index must be 0 or 1");
	}


}
