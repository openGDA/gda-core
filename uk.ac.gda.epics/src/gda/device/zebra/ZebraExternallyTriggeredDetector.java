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

package gda.device.zebra;

import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import gda.device.DeviceException;
import gda.device.continuouscontroller.ContinuousMoveController;
import gda.device.continuouscontroller.HardwareTriggerProvider;
import gda.device.detector.DetectorBase;
import gda.device.detector.hardwaretriggerable.HardwareTriggeredDetector;
import gda.device.scannable.ContinuouslyScannableViaController;
import gda.device.scannable.PositionCallableProvider;
import gda.scan.DetectorWithReadoutTime;

/**
 * Class to use with ZebraMonitorController and ConstantVelocityScanLine to monitor flyscans
 * on a Zebra triggered by a ZebraConstantVelocityMoveController as a detector.
 */
public class ZebraExternallyTriggeredDetector extends DetectorBase implements ContinuouslyScannableViaController,
		InitializingBean, PositionCallableProvider<Double>, HardwareTriggeredDetector, DetectorWithReadoutTime {

	private static final Logger logger = LoggerFactory.getLogger(ZebraExternallyTriggeredDetector.class);

	private ZebraMonitorController zebraMonitorController;
	private int pcCapture=0;

	// Class properties

	public void setZebraMonitorController(ZebraMonitorController continuousMoveController) {
		this.zebraMonitorController = continuousMoveController;
	}

	public int getPcCapture() {
		return pcCapture;
	}

	public void setPcCapture(int pcCapture) {
		this.pcCapture = pcCapture;
	}

	// interface ContinuouslyScannableViaController

	// TODO: Do we really need this interface? If not, remove it and the fix to ConstantVelocityScanLine.checkRemainingArgs() won't be needed.

	@Override
	public void setOperatingContinuously(boolean b) throws DeviceException {
		logger.debug("setOperatingContinuously({}) ignored, always operating continuously", b);
	}

	@Override
	public boolean isOperatingContinously() {
		return true;
	}

	@Override
	public ContinuousMoveController getContinuousMoveController() {
		return zebraMonitorController.getContinuousMoveController();
	}

	@Override
	public void setContinuousMoveController(ContinuousMoveController controller) {
		logger.trace("setContinuousMoveController({})", controller);
		try {
			ZebraConstantVelocityMoveController zebraController = (ZebraConstantVelocityMoveController) controller;
			if (this.zebraMonitorController.getZebraCVMoveController().getZebra() != zebraController.getZebra()) {
				throw new IllegalArgumentException("ZebraConstantVelocityMoveController "+controller.getName()+" uses a different zebra to the already configured "+this.zebraMonitorController.getZebraCVMoveController().getName());
			}
			this.zebraMonitorController.setZebraCVMoveController(zebraController);
		} catch (Exception e) {
			throw new IllegalArgumentException("setContinuousMoveController("+controller.getName()+") is not a ZebraConstantVelocityMoveController required for "+getName(), e);
		}
	}

	// implements InitializingBean

	@Override
	public void afterPropertiesSet() throws Exception {
		if( zebraMonitorController == null){
			throw new Exception("continuousMoveController == null");
		}
	}

	// interface PositionCallableProvider<T>

	@Override
	public Callable<Double> getPositionCallable() throws DeviceException {
		logger.trace("getPositionCallable() for {}", getPcCapture());
		return zebraMonitorController.getPositionSteamIndexer(getPcCapture()).getNamedPositionCallable(getName(),1);
	}

	// interface Detector

	@Override
	public void collectData() throws DeviceException {
		logger.trace("collectData() nothing to do!");
		// Nothing to do here, the detector should already be armed and waiting for the trigger
	}

	@Override
	public int getStatus() throws DeviceException {
		return (zebraMonitorController.isBusy() ? BUSY : IDLE);
	}

	@Override
	public Object readout() throws DeviceException {
		logger.trace("readout() called when operatingContinuously!");
		throw new IllegalStateException("readout() called when operatingContinuously!");
	}

	@Override
	public void prepareForCollection() throws DeviceException {
		logger.trace("prepareForCollection(), nothing to do!");
		// Nothing to do here, the controller will be prepared by the ZebraConstantVelocityMoveController
	}

	//public void endCollection() throws DeviceException;

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		logger.trace("createsOwnFiles() returning false");
		return false; // Return data directly
	}

	// interface DetectorWithReadoutTime

	@Override
	public double getReadOutTime() {
		logger.trace("getReadOutTime()");
		// TODO We should get this from the ZebraMonitorController, so it can depend on the time units selected
		return 0.000002; // or 0.002s
	}

	// interface HardwareTriggeredDetector

	@Override
	public HardwareTriggerProvider getHardwareTriggerProvider() {
		logger.trace("getHardwareTriggerProvider()");
		return zebraMonitorController.getContinuousMoveController();
	}

	@Override
	public void setNumberImagesToCollect(int numberImagesToCollect) {
		logger.trace("setNumberImagesToCollect({}) ignored, nothing to do!", numberImagesToCollect);
		// Nothing to do here, the number of images collected will be determined by the ZebraConstantVelocityMoveController
	}

	@Override
	public boolean integratesBetweenPoints() {
		logger.trace("integratesBetweenPoints() returning true");
		return true;
	}
}
