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

package gda.device.detector.addetector.triggering;

import gda.device.DeviceBase;
import gda.device.DeviceException;
import gda.device.continuouscontroller.ConstantVelocityMoveController;
import gda.device.continuouscontroller.HardwareTriggerProvider;
import gda.factory.FactoryException;

public class TFG2CVmoveController extends DeviceBase implements HardwareTriggerProvider, ConstantVelocityMoveController{

	@Override
	public void prepareForMove() throws DeviceException, InterruptedException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void startMove() throws DeviceException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isMoving() throws DeviceException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void waitWhileMoving() throws DeviceException, InterruptedException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stopAndReset() throws DeviceException, InterruptedException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void configure() throws FactoryException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setStart(double start) throws DeviceException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public double getStart() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setEnd(double end) throws DeviceException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public double getEnd() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setStep(double step) throws DeviceException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public double getStep() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setTriggerPeriod(double seconds) throws DeviceException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getNumberTriggers() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getTotalTime() throws DeviceException {
		// TODO Auto-generated method stub
		return 0;
	}

}
