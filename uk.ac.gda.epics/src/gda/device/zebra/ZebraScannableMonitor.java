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

import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import gda.device.DeviceException;
import gda.device.continuouscontroller.ContinuousMoveController;
import gda.device.scannable.ContinuouslyScannableViaController;
import gda.device.scannable.PositionCallableProvider;
import gda.device.scannable.ScannableBase;
import gda.factory.FactoryException;

/**
 * Class to use with ZebraConstantVelocityMoveController and ConstantVelocityScanLine to monitor flyscans
 */
public class ZebraScannableMonitor extends ScannableBase implements ContinuouslyScannableViaController, PositionCallableProvider<Double>, InitializingBean {
	private static final Logger logger = LoggerFactory.getLogger(ZebraScannableMonitor.class);
	private ZebraConstantVelocityMoveController continuousMoveController;
	private int pcCapture=0;

	// Class properties

	public int getPcCapture() {
		return pcCapture;
	}

	public void setPcCapture(int pcCapture) {
		this.pcCapture = pcCapture;
	}

	// implements ContinuouslyScannableViaController

	@Override
	public void setOperatingContinuously(boolean b) throws DeviceException {
		logger.debug("setOperatingContinuously({}) ignored, always operating continuously");
	}

	@Override
	public boolean isOperatingContinously() {
		return true;
	}

	@Override
	public ContinuousMoveController getContinuousMoveController() {
		return continuousMoveController;
	}

	public void setZebraConstantVelocityMoveController(ZebraConstantVelocityMoveController continuousMoveController) {
		this.continuousMoveController = continuousMoveController;
	}

	@Override
	public void setContinuousMoveController(ContinuousMoveController continuousMoveController) {
		try {
			ZebraConstantVelocityMoveController zebraController = (ZebraConstantVelocityMoveController) continuousMoveController;
			if (this.continuousMoveController.getZebra() != zebraController.getZebra()) {
				throw new IllegalArgumentException("ZebraConstantVelocityMoveController "+continuousMoveController.getName()+" uses a different zebra to the already configured "+this.continuousMoveController.getName());
			}
			this.continuousMoveController = zebraController;
		} catch (Exception e) {
			throw new IllegalArgumentException("setContinuousMoveController("+continuousMoveController.getName()+") is not a ZebraConstantVelocityMoveController required for "+getName(), e);
		}
	}

	// implements InitializingBean

	@Override
	public void afterPropertiesSet() throws Exception {
		if( continuousMoveController == null){
			throw new Exception("continuousMoveController == null");
		}
	}

	// interface PositionCallableProvider<T>

	@Override
	public Callable<Double> getPositionCallable() throws DeviceException {
		return continuousMoveController.getPositionSteamIndexer(getPcCapture()).getNamedPositionCallable(getName(),1);
	}

	// Scannable

	@Override
	public boolean isBusy() throws DeviceException {
		return false; //this is never busy as it does not talk to hardware
	}

	// ScannableBase

	@Override
	public void configure() throws FactoryException {
		setInputNames(new String[]{});
		setExtraNames(new String[]{getName()});
		setOutputFormat(new String[]{"%5.5g"});
		super.configure();
	}

	@Override
	public void rawAsynchronousMoveTo(Object position) throws DeviceException {
		logger.warn("asynchronousMoveTo({}) monitor, so ignored. Also should not be called during continuous operation!", position);
	}

	@Override
	public Object rawGetPosition() throws DeviceException {
		logger.warn("rawGetPosition() should not be called during continuous operation.");
		return 0.;
	}

	/*
	@Override
	public void waitWhileBusy() throws DeviceException, InterruptedException {
		return; //this is never busy as it does not talk to hardware
	}
*/
}
