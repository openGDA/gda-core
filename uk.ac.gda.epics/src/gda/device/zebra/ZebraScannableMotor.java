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
import gda.device.scannable.PositionConvertorFunctions;
import gda.device.scannable.ScannableMotor;

/**
 * Class to use with ZebraConstantVelocityMoveController and ConstantVelocityScanLine to perform flyscans
 *
 *
 */
public class ZebraScannableMotor extends ScannableMotor implements ContinuouslyScannableViaController, ZebraMotorInfoProvider,
																	PositionCallableProvider<Double>, InitializingBean {
	private static final Logger logger = LoggerFactory.getLogger(ZebraScannableMotor.class);
	private ZebraConstantVelocityMoveController continuousMoveController;
	private double scurveTimeToVelocity=.03;//default set to rotation stage on I13
	private int pcEnc=0;
	private ScannableMotor scannableMotor;

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
		return continuousMoveController;
	}

	public void setZebraConstantVelocityMoveController(ZebraConstantVelocityMoveController continuousMoveController) {
		this.continuousMoveController = continuousMoveController;
	}

	@Override
	public void setContinuousMoveController(ContinuousMoveController continuousMoveController) {
		logger.trace("setContinuousMoveController({})", continuousMoveController);
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

	@Override
	public void afterPropertiesSet() throws Exception {
		if( continuousMoveController == null){
			throw new Exception("continuousMoveController == null");
		}
	}

	// Scannable //
	@Override
	public void asynchronousMoveTo(Object position) throws DeviceException {
		logger.trace("asynchronousMoveTo({})", position);
		continuousMoveController.addPoint(PositionConvertorFunctions.toDouble(externalToInternal(position)));
	}

	@Override
	public Object getPosition() throws DeviceException {
		//TODO this will not be called as we have getPositionCallable so getLastPointAdded is not required not addPoint
		Object[] pos = (Object[]) internalToExternal(new Double[]{continuousMoveController.getLastPointAdded()});
		if (pos == null || pos[0] == null) {
			// First point is in process of being added
			return super.getPosition();
		}
		return pos[0];
	}

	@Override
	public Callable<Double> getPositionCallable() throws DeviceException {
		return continuousMoveController.getPositionSteamIndexer(getPcEnc()).getNamedPositionCallable(getName(),1);
	}

	@Override
	public void waitWhileBusy() throws DeviceException, InterruptedException {
		return; //this is never busy as it does not talk to hardware
	}

	/**
	 *
	 * @param velocity  in units of motor units/second e.g. mm/s
	 * @return distance in motor units e.g. mm
	 */
	@Override
	public double distanceToAccToVelocity(double velocity) {
		//for an S curve with time to velocity of 30ms
		return scurveTimeToVelocity * velocity/2;
	}

	public double getScurveTimeToVelocity() {
		return scurveTimeToVelocity;
	}

	/**
	 *
	 * @param scurveTimeToVelocity - if using linear acceleration this is the ACCEL field of the
	 * motor in EPICS (Time to Velocity)
	 */
	public void setScurveTimeToVelocity(double scurveTimeToVelocity) {
		logger.trace("setScurveTimeToVelocity({})", scurveTimeToVelocity);
		this.scurveTimeToVelocity = scurveTimeToVelocity;
	}

	@Override
	public int getPcEnc() {
		return pcEnc;
	}

	public void setPcEnc(int pcEnc) {
		this.pcEnc = pcEnc;
	}

	@Override
	public ScannableMotor getActualScannableMotor() {
		if (scannableMotor == null){
			scannableMotor = new ScannableMotor();
			scannableMotor.setMotor(getMotor());
			scannableMotor.setName(getName()+"_actualMotor");
		}
		return scannableMotor;
	}
	private double exposureStep;
	private boolean exposureStepDefined=false;

	@Override
	public double getExposureStep() {
		return Math.abs(exposureStep);
	}

	public void setExposureStep(double exposureStep) {
		logger.trace("setExposureStep({})", exposureStep);
		this.exposureStep = exposureStep;
	}

	@Override
	public boolean isExposureStepDefined() {
		return exposureStepDefined;
	}

	public void setExposureStepDefined(boolean exposureStepDefined) {
		logger.trace("setExposureStepDefined({})", exposureStepDefined);
		this.exposureStepDefined = exposureStepDefined;
	}

	@Override
	public void atScanStart() throws DeviceException {
		logger.trace("atScanStart()");
		// Due to the fragility of ZebraConstantVelocityMoveController and particularly the life cycle of
		// its parts which read out time series arrays: here we will clear these so that controller this
		//ZebraScannableMotor returns is fit to be used by the scan.
		continuousMoveController.prepareControllerToBeUsedForUpcomingScan();
	}
}
