/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

package gda.device.scannable;

import gda.device.ContinuousParameters;
import gda.device.DeviceException;
import gda.epics.connection.EpicsChannelManager;
import gda.epics.connection.EpicsController;
import gda.epics.connection.InitializationListener;
import gda.factory.FactoryException;
import gda.jscience.physics.quantities.BraggAngle;
import gda.jscience.physics.quantities.PhotonEnergy;
import gda.util.QuantityFactory;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.TimeoutException;

import org.jscience.physics.quantities.Angle;
import org.jscience.physics.quantities.Constants;
import org.jscience.physics.quantities.Energy;
import org.jscience.physics.quantities.Length;
import org.jscience.physics.quantities.Quantity;
import org.jscience.physics.units.NonSI;
import org.jscience.physics.units.SI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * For B18 QEXAFS scans. Operates the mono energy but also set position compare to define when and how TTL pulses are
 * sent from the bragg motor to the TFG.
 * <p>
 * This will change the speed of the Bragg motor if the required movement is slower than the Bragg's maximum (which is
 * also the Bragg default speed). If the required motion is faster than the maximum then this will be logged and the
 * speed will be set to the maximum.
 * <p>
 * TODO: change movement control based on exit mode (current behaviour is fixed exit mode); 
 */
public class QexafsScannable extends ScannableMotor implements ContinuouslyScannable, InitializationListener, IQexafsScannableState {

	private static final Logger logger = LoggerFactory.getLogger(QexafsScannable.class);

	private ContinuousParameters continuousParameters;
	private String outputModePV; // 0 = OFF, 1 = ON, 2 = AUTO
	private String startPV; // in degrees
	private String stopPV; // in degrees
	private String stepPV; // in degrees
	private String accelPV; // in degrees/s/s
	private String xtalSwitchPV; // will be a string such as "Si(111)"
	private String braggCurrentSpeedPV; // the max and default speed of
	private String braggMaxSpeedPV; // the max and default speed of
	private String energySwitchPV; // combined energy motion flag
	private String stepIncDegPV; // after start,stop,step set this is the step size in deg
	private String pulseWidthDegPV; // pulse width - treated here as read-only
	private String numPulsesPV; // the number of pulses that will be sent out, after start,stop,step set

	private boolean channelsConfigured = false;
	private EpicsController controller;
	private EpicsChannelManager channelManager;
	private Channel outputModeChnl;
	private Channel startChnl;
	private Channel stopChnl;
	private Channel stepChnl;
	private Channel accelChnl;
	private Channel xtalSwitchChnl;
	private Channel currentSpeedChnl;
	private Channel maxSpeedChnl;
	private Channel energySwitchChnl;
	private Channel stepIncDegChnl;
	private Channel pulseWidthDegChnl;
	private Channel numPulsesChnl;

	private Angle startAngle;
	private Angle endAngle;
	private Angle stepSize;
	private Angle runupPosition;
	private Angle runDownPosition;

	private Double maxSpeed; // in deg/sec
	private Double desiredSpeed; // in deg/sec

	private String state = "idle";
	
	private double extraRunUp=0;
	
	
	public double getExtraRunUp() {
		return extraRunUp;
	}

	public void setExtraRunUp(double extraRunUp) {
		this.extraRunUp = extraRunUp;
	}

	@Override
	public void configure() throws FactoryException {
		try {
			controller = EpicsController.getInstance();
			channelManager = new EpicsChannelManager(this);

			outputModeChnl = channelManager.createChannel(outputModePV, false);
			startChnl = channelManager.createChannel(startPV, false);
			stopChnl = channelManager.createChannel(stopPV, false);
			stepChnl = channelManager.createChannel(stepPV, false);
			accelChnl = channelManager.createChannel(accelPV, false);
			xtalSwitchChnl = channelManager.createChannel(xtalSwitchPV, false);
			currentSpeedChnl = channelManager.createChannel(braggCurrentSpeedPV, false);
			maxSpeedChnl = channelManager.createChannel(braggMaxSpeedPV, false);
			energySwitchChnl = channelManager.createChannel(energySwitchPV, false);
			stepIncDegChnl = channelManager.createChannel(stepIncDegPV, false);
			pulseWidthDegChnl = channelManager.createChannel(pulseWidthDegPV, false);
			numPulsesChnl = channelManager.createChannel(numPulsesPV, false);

			channelManager.creationPhaseCompleted();
		} catch (CAException e) {
			throw new FactoryException("CAException while creating channels for " + getName(), e);
		}

		super.configure();
	}

	@Override
	public int prepareForContinuousMove() throws DeviceException {
		
		state = "preparing";
		notifyIObservers(this, state);
		
		if (!channelsConfigured) {
			throw new DeviceException("Cannot set continuous mode on for " + getName()
					+ " as Epics channels not configured");
		}
		if (continuousParameters == null) {
			throw new DeviceException("Cannot set continuous mode on for " + getName()
					+ " as ContinuousParameters not set");
		}
		try {
			
			
			calculateMotionInDegrees();
			Double startDeg = radToDeg(startAngle);
			Double stopDeg = radToDeg(endAngle);
			Double stepDeg = radToDeg(stepSize);
			controller.caputWait(outputModeChnl, 0); // ensure at 0 for the run-up movement. No longer in EDM screen.
			controller.caputWait(currentSpeedChnl, getMaxSpeed()); // ensure at max speed for the run-up movement
			// move to run-up position so ready to collect
			//super.asynchronousMoveTo(angleToEV(runupPosition));
			super.moveTo(angleToEV(runupPosition));
			
			controller.caputWait(startChnl, startDeg);
			controller.caputWait(stopChnl, stopDeg);
			controller.caputWait(stepChnl, stepDeg);
			if (checkStepGTPulse()){
				throw new DeviceException("Too many data points, so pulses would overlap - no pulses would be sent from Bragg motor");
			}
			
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			return controller.cagetInt(this.numPulsesChnl);
			

		} catch (Exception e) {
			if( e instanceof DeviceException)
				throw (DeviceException)e;
			throw new DeviceException(getName() +" exception in prepareForContinuousMove", e);
		}
	}

	private boolean checkStepGTPulse() throws TimeoutException, CAException, InterruptedException {
		Double stepInc = controller.cagetDouble(this.stepIncDegChnl);
		Double pulseWidth = controller.cagetDouble(this.pulseWidthDegChnl);
			
		Double ratio = stepInc/pulseWidth;	
		return ratio < 2.5;
	}

	private Double getMaxSpeed() {
		if (maxSpeed == null) {
			try {
				maxSpeed = controller.cagetDouble(maxSpeedChnl);
			} catch (Exception e) {
				logger.warn("Exception while getting Bragg motor max speed. Defaulting to 0.0674934", e);
				maxSpeed = 0.0674934; // default value in use in Sept 2010
			}
		}

		return maxSpeed;
	}
	
	@Override
	public void performContinuousMove() throws DeviceException {
		
		state = "running";
		notifyIObservers(this, state);

		
		if (channelsConfigured && continuousParameters != null) {
			try {
				//set the sped (do this now, after the motor has been moved to the run-up position)
				if (desiredSpeed <= getMaxSpeed()) {
					controller.caputWait(currentSpeedChnl, desiredSpeed);
				} else {
					logger.info("Continuous motion for " + getName()
							+ " greater than Bragg maximum speed. Speed will be set instead to the max imum speed of "
							+ getMaxSpeed() + " deg/s");
				}
				controller.caputWait(outputModeChnl, 2);
				super.asynchronousMoveTo(angleToEV(runDownPosition));
			} catch (Exception e) {
				throw new DeviceException(e.getMessage(), e);
			}
		}
	}

	@Override
	public void continuousMoveComplete() throws DeviceException {
		try {
			// return to regular running values
			controller.caputWait(outputModeChnl, 0);
			controller.caputWait(currentSpeedChnl, getMaxSpeed());
		} catch (Exception e) {
			throw new DeviceException("Exception while switching output mode to \'off\'", e);
		}
		
		state = "idle";
		notifyIObservers(this, state);
	}

	@Override
	public ContinuousParameters getContinuousParameters() {
		return continuousParameters;
	}
	
	@Override
	public void stop() throws DeviceException {
		try {
			// return to regular running values
			controller.caputWait(outputModeChnl, 0);
			controller.caputWait(currentSpeedChnl, getMaxSpeed());
			
			controller.caputWait(energySwitchChnl, 0); //off
			controller.caputWait(energySwitchChnl, 1); //on
		} catch (Exception e) {
			throw new DeviceException("Exception while changing energy switch off/on to stop the motion", e);
		}
		
		state = "idle";
		notifyIObservers(this, state);
	}

	private double radToDeg(Angle angle) {
		return QuantityFactory.createFromObject(angle, NonSI.DEGREE_ANGLE).getAmount();
	}

	private double angleToEV(Angle angle) throws TimeoutException, CAException, InterruptedException {
		return QuantityFactory.createFromObject(PhotonEnergy.photonEnergyOf(angle, getTwoD()), NonSI.ELECTRON_VOLT)
				.getAmount();
	}

	/**
	 * @return 2*lattice spacing for the given Bragg crystal cut in use.
	 * @throws TimeoutException
	 * @throws CAException
	 * @throws InterruptedException 
	 */
	private Length getTwoD() throws TimeoutException, CAException, InterruptedException {
		String xtalSwitch = controller.cagetString(xtalSwitchChnl);
		if (xtalSwitch.contains("111")) {
			return Quantity.valueOf(0.62711, SI.NANO(SI.METER));
		}
		else if (xtalSwitch.contains("311")) {
			return Quantity.valueOf(0.327, SI.NANO(SI.METER));
		}
		// TODO need 311 value else its 311
		return Quantity.valueOf(0.62711, SI.NANO(SI.METER));
	}

	
	public boolean isExafs(){
		return true;
	}
	
	@Override
	public double calculateEnergy(int frameIndex) throws DeviceException{
		
		try {
			stepSize = (Angle) (startAngle.minus(endAngle)).divide(continuousParameters.getNumberDataPoints());
			double continuousCountSteps = (Math.round(radToDeg(stepSize)*111121.98)/111121.98);
			
			double braggAngle = startAngle.doubleValue() - frameIndex * Math.toRadians(continuousCountSteps);
			Length twoD = getTwoD();
			
			double top = (Constants.h.times(Constants.c).divide(Constants.ePlus)).doubleValue();
			
			double bottom = twoD.doubleValue() * Math.sin(braggAngle);
			
			double result = top/bottom;
			
			return result;
		} catch (Exception e) {
			throw new DeviceException(e.getMessage());
		}
	}
	
	

	private void calculateMotionInDegrees() throws TimeoutException, CAException, InterruptedException {

		Length twoD = getTwoD();

		Energy startEng = Quantity.valueOf(continuousParameters.getStartPosition(), NonSI.ELECTRON_VOLT);
		startAngle = BraggAngle.braggAngleOf(startEng, twoD);

		Energy endEng = Quantity.valueOf(continuousParameters.getEndPosition(), NonSI.ELECTRON_VOLT);
		endAngle = BraggAngle.braggAngleOf(endEng, twoD);

		stepSize = (Angle) (startAngle.minus(endAngle)).divide(continuousParameters.getNumberDataPoints());

		// v^2 = u^2 + 2as
		double acceleration = controller.cagetDouble(accelChnl);
		desiredSpeed = Math.abs(radToDeg(endAngle) - radToDeg(startAngle)) / continuousParameters.getTotalTime();
		double runUp = (desiredSpeed * desiredSpeed) / (2 * acceleration);
		runUp *= 3.0; // to be safe add 10%
		Angle runUpAngle = (Angle) QuantityFactory.createFromObject(runUp, NonSI.DEGREE_ANGLE);
		// 1.165E-4 deg is a practical minimum to avoid the motor's deadband
		double step = controller.cagetDouble(this.stepIncDegChnl);
		
		if (runUpAngle.doubleValue() < 10*step) {//0.0001165
			runUpAngle = (Angle) QuantityFactory.createFromObject(10*step, NonSI.DEGREE_ANGLE);
		}
		
		Quantity add = QuantityFactory.createFromObject(extraRunUp, NonSI.DEGREE_ANGLE);
		
		runUpAngle = (Angle) runUpAngle.plus(add);
		
		if (endAngle.getAmount() > startAngle.getAmount()) {
			runupPosition = (Angle) startAngle.minus(runUpAngle);
			runDownPosition = (Angle) endAngle.plus(runUpAngle);
		} else {
			runupPosition = (Angle) startAngle.plus(runUpAngle);
			runDownPosition = (Angle) endAngle.minus(runUpAngle);
		}
	}

	
	
	@Override
	public void setContinuousParameters(ContinuousParameters parameters) {
		continuousParameters = parameters;
	}

	@Override
	public void initializationCompleted() {
		channelsConfigured = true;
	}

	@Override
	public String getState() {
		return state;
	}

	public String getOutputModePV() {
		return outputModePV;
	}

	public void setOutputModePV(String outputModePV) {
		this.outputModePV = outputModePV;
	}

	public String getStartPV() {
		return startPV;
	}

	public void setStartPV(String startPV) {
		this.startPV = startPV;
	}

	public String getStopPV() {
		return stopPV;
	}

	public void setStopPV(String stopPV) {
		this.stopPV = stopPV;
	}

	public String getStepPV() {
		return stepPV;
	}

	public void setStepPV(String stepPV) {
		this.stepPV = stepPV;
	}

	public String getAccelPV() {
		return accelPV;
	}

	public void setAccelPV(String accelPV) {
		this.accelPV = accelPV;
	}

	public String getXtalSwitchPV() {
		return xtalSwitchPV;
	}

	public void setXtalSwitchPV(String xtalSwitchPV) {
		this.xtalSwitchPV = xtalSwitchPV;
	}

	public String getBraggMaxSpeedPV() {
		return braggMaxSpeedPV;
	}

	public void setBraggMaxSpeedPV(String braggMaxSpeedPV) {
		this.braggMaxSpeedPV = braggMaxSpeedPV;
	}

	public String getEnergySwitchPV() {
		return energySwitchPV;
	}

	public void setEnergySwitchPV(String energySwitchPV) {
		this.energySwitchPV = energySwitchPV;
	}

	public String getStepIncDegPV() {
		return stepIncDegPV;
	}

	public void setStepIncDegPV(String stepIncDegPV) {
		this.stepIncDegPV = stepIncDegPV;
	}

	public String getPulseWidthDegPV() {
		return pulseWidthDegPV;
	}

	public void setPulseWidthDegPV(String pulseWidthDegPV) {
		this.pulseWidthDegPV = pulseWidthDegPV;
	}

	public String getNumPulsesPV() {
		return numPulsesPV;
	}

	public void setNumPulsesPV(String numPulsesPV) {
		this.numPulsesPV = numPulsesPV;
	}
}