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

package gda.device.zebra;

import gda.device.DeviceException;
import gda.device.continuouscontroller.ContinuousMoveController;
import gda.device.scannable.ContinuouslyScannableViaController;
import gda.device.scannable.PositionCallableProvider;
import gda.device.scannable.PositionConvertorFunctions;
import gda.device.scannable.ScannableMotor;

import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class ZebraScannableMotor extends ScannableMotor implements ContinuouslyScannableViaController, PositionCallableProvider<Double>, InitializingBean{
	private static final Logger logger = LoggerFactory.getLogger(ZebraScannableMotor.class);
	private boolean operatingContinously;
	private ZebraConstantVelocityMoveController continuousMoveController;
	private double constantVelocitySpeedFactor=0.8;

	@Override
	public void setOperatingContinuously(boolean b) throws DeviceException {
		operatingContinously = b;
		
	}

	@Override
	public boolean isOperatingContinously() {
		return operatingContinously;
	}

	@Override
	public ContinuousMoveController getContinuousMoveController() {
		return continuousMoveController;
	}

	public void setZebraConstantVelocityMoveController(ZebraConstantVelocityMoveController continuousMoveController) {
		this.continuousMoveController = continuousMoveController;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if( continuousMoveController == null){
			throw new Exception("continuousMoveController == null");
		}
		continuousMoveController.setzSM(this);
		
	}
	

	// Scannable //
	@Override
	public void asynchronousMoveTo(Object position) throws DeviceException {
		if (isOperatingContinously()) {
			continuousMoveController.addPoint(PositionConvertorFunctions.toDouble(externalToInternal(position)));
		} else {
			super.asynchronousMoveTo(position);
		}
	}
	@Override
	public Object getPosition() throws DeviceException {
		if (isOperatingContinously()) {
			Object[] pos = (Object[]) internalToExternal(new Double[]{continuousMoveController.getLastPointAdded()});
			if (pos == null) {
				// First point is in process of being added
				return super.getPosition();
			}
			return pos[0];
		}
		return super.getPosition();
	}

	@Override
	public Callable<Double> getPositionCallable() throws DeviceException {
		return continuousMoveController.getPositionCallable(); 
	}

	@Override
	public void atScanLineEnd() throws DeviceException {
		logger.info("atScanLineEnd");
		super.atScanLineEnd();
	}

	@Override
	public void atScanLineStart() throws DeviceException {
		logger.info("atScanLineStart");
		continuousMoveController.atScanLineStart();
		super.atScanLineStart();
	}

	@Override
	public void waitWhileBusy() throws DeviceException, InterruptedException {
		if (isOperatingContinously()) {
			continuousMoveController.waitWhileMoving();
		} else {
			super.waitWhileBusy();
		}
	}

	public double getConstantVelocitySpeedFactor() {
		return constantVelocitySpeedFactor;
	}

	public void setConstantVelocitySpeedFactor(double constantVelocitySpeedFactor) {
		this.constantVelocitySpeedFactor = constantVelocitySpeedFactor;
	}





}
