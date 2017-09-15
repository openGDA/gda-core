/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package uk.ac.gda.server.exafs.epics.device.scannable;

import org.jscience.physics.quantities.Angle;
import org.jscience.physics.quantities.Constants;
import org.jscience.physics.quantities.Energy;
import org.jscience.physics.quantities.Length;
import org.jscience.physics.quantities.Quantity;
import org.jscience.physics.units.NonSI;
import org.jscience.physics.units.SI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.ContinuousParameters;
import gda.device.DeviceException;
import gda.device.MotorStatus;
import gda.device.scannable.ContinuouslyScannable;
import gda.device.scannable.ScannableMotor;
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

/**
 * For QEXAFS scans for B18 only. Operates the mono energy but also set position compare to define when and how TTL
 * pulses are sent from the bragg motor to the TFG.
 * <p>
 * This will change the speed of the Bragg motor if the required movement is slower than the Bragg's maximum (which is
 * also the Bragg default speed). If the required motion is faster than the maximum then this will be logged and the
 * speed will be set to the maximum.
 * <p>
 * Deprecated now that B18 uses Zebra and not position compare
 */
@Deprecated
public class QexafsScannable extends ScannableMotor implements ContinuouslyScannable, InitializationListener {

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
	private String numPulsesPV; // the number of pulses that will be sent out, after start,stop,step set
	private String stepIncDemandPV;

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
	private Channel stepIncDemChnl;
	private Channel numPulsesChnl;

	private Angle startAngle;
	private Angle endAngle;
	private Angle stepSize;
	private Angle runupPosition;
	private Angle runDownPosition;

	private Double maxSpeed; // in deg/sec
	private Double desiredSpeed; // in deg/sec

	private double extraRunUp = 0;
	private boolean runUpOn = true;
	private boolean runDownOn = true;

	public boolean calcEndFromGui = true;

	private boolean toggleEnergyControl = false;

	private double stepIncDemDeg;

	double newStartAngleDeg;
	double newEndAngleDeg;
	double newNumberOfDataPoints;
	double newStep;
	double newStopAngleDeg;

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
			stepIncDemChnl = channelManager.createChannel(stepIncDemandPV, false);
			numPulsesChnl = channelManager.createChannel(numPulsesPV, false);

			channelManager.creationPhaseCompleted();
		} catch (CAException e) {
			throw new FactoryException("CAException while creating channels for " + getName(), e);
		}

		super.configure();
	}

	@Override
	public void prepareForContinuousMove() throws DeviceException {
		long timeAtMethodStart = System.currentTimeMillis();
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

			// wait until run down period has finished
			logger.debug("Time spent before busy loop");
			while (isBusy()) {
				logger.info("-----waiting for qscanAxis to finish moving inside prepare");
				Thread.sleep(100);
			}
			logger.debug("Time spent after busy loop");
			resetDCMSpeed();
			logger.debug("Time spent after max speed set");

			// move to run-up position so ready to collect
			if (runUpOn) {
				double angleToEV = angleToEV(runupPosition);
				move(angleToEV);
			} else {
				move(angleToEV(startAngle));
			}
			logger.debug("Time spent after moved to angle");

			if (calcEndFromGui) {
				controller.caputWait(startChnl, startDeg);
				controller.caputWait(stopChnl, stopDeg);
				controller.caputWait(stepChnl, Math.abs(stepDeg));
			} else {

				controller.caputWait(stopChnl, newEndAngleDeg);
				controller.caputWait(startChnl, newStartAngleDeg);
				controller.caputWait(stepChnl, Math.abs(newStep));

				double stepIncDemand = Double.parseDouble(controller.caget(stepIncDemChnl));
				stepIncDemDeg = stepIncDemand * 360 / 40000000;
			}
			// why sleep half a second? for epics to calculate the number of pulses
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				throw e;
			}
			logger.debug("Time spent after 100ms sleep");
			int cagetInt = controller.cagetInt(this.numPulsesChnl);
			logger.debug("Time spent after get no. pulses");
			long timeAtMethodEnd = System.currentTimeMillis();
			logger.debug("Time spent in prepareForContinuousMove = " + (timeAtMethodEnd - timeAtMethodStart) + "ms");
			if (!calcEndFromGui)
				newStopAngleDeg = newStartAngleDeg - (stepIncDemDeg * cagetInt);

		} catch (DeviceException e) {
			throw e;
		} catch (Exception e) {
			throw new DeviceException(getName() + " exception in prepareForContinuousMove", e);
		}
	}

	private double energyToDegrees(double energy) {
		Energy valueOf = Quantity.valueOf(energy, NonSI.ELECTRON_VOLT);
		Angle braggAngleOf = null;
		try {
			braggAngleOf = BraggAngle.braggAngleOf(valueOf, getTwoD());
		} catch (TimeoutException e) {
			logger.error("Timeout error: failed to get 2D lattice spacing value from channel " +getXtalSwitchPV(), e);
		} catch (CAException e) {
			logger.error("Channel error: channel failure occurred while getting 2D lattice spacing value from channel " +getXtalSwitchPV(), e);
		} catch (InterruptedException e) {
			logger.error("Interrupt error: the process thread was interrupted for channel " +getXtalSwitchPV()+ "while getting 2D lattice spacing", e);
		}
		return radToDeg(braggAngleOf);
	}

	private void move(double position) {
		double currentPosition = 0;
		try {
			currentPosition = (Double) this.rawGetPosition();
		} catch (DeviceException e1) {
			logger.error("TCould not read scannable position", e1);
		}

		double demandDegrees = energyToDegrees(position);
		double currentDegrees = energyToDegrees(currentPosition);

		if (Math.abs(currentDegrees - demandDegrees) > 0.00011) {
			try {
				super.moveTo(position);
			} catch (DeviceException e) {
				logger.error("Could not move scannable", e);
			}
		}
	}

	private Double getMaxSpeed() {
		long timeAtMethodStart = System.currentTimeMillis();
		if (maxSpeed == null) {
			try {
				maxSpeed = controller.cagetDouble(maxSpeedChnl);
			} catch (Exception e) {
				logger.warn("Exception while getting Bragg motor max speed. Defaulting to 0.0674934", e);
				maxSpeed = 0.0674934; // default value in use in Sept 2010
			}
		}
		long timeAtMethodEnd = System.currentTimeMillis();
		logger.debug("Time spent in getMaxSpeed = " + (timeAtMethodEnd - timeAtMethodStart) + "ms");
		return maxSpeed;
	}

	@Override
	public void performContinuousMove() throws DeviceException {
		long timeAtMethodStart = System.currentTimeMillis();
		if (channelsConfigured && continuousParameters != null) {
			try {
				// set the sped (do this now, after the motor has been moved to the run-up position)
				if (desiredSpeed <= getMaxSpeed()) {
					while (isBusy()) {
						logger.info("-----waiting for qscanAxis to finish moving inside perform before starting scanning. after goto runup");
						Thread.sleep(100);
					}
					controller.caputWait(currentSpeedChnl, desiredSpeed);
				} else {
					logger.info("Continuous motion for " + getName()
							+ " greater than Bragg maximum speed. Speed will be set instead to the max imum speed of "
							+ getMaxSpeed() + " deg/s");
				}
				controller.caputWait(outputModeChnl, 2);
				if (runDownOn)
					super.asynchronousMoveTo(angleToEV(runDownPosition));
				else
					super.asynchronousMoveTo(angleToEV(endAngle));
			} catch (Exception e) {
				throw new DeviceException(e.getMessage(), e);
			}
		}
		long timeAtMethodEnd = System.currentTimeMillis();
		logger.debug("Time spent in performContinuousMove = " + (timeAtMethodEnd - timeAtMethodStart) + "ms");
	}

	@Override
	public void continuousMoveComplete() throws DeviceException {
		long timeAtMethodStart = System.currentTimeMillis();
		try {
			// return to regular running values
			controller.caputWait(outputModeChnl, 0);
			resetDCMSpeed();
		} catch (Exception e) {
			throw new DeviceException("Exception while switching output mode to \'off\'", e);
		}
		long timeAtMethodEnd = System.currentTimeMillis();
		logger.debug("Time spent in continuousMoveComplete = " + (timeAtMethodEnd - timeAtMethodStart) + "ms");
	}

	@Override
	public ContinuousParameters getContinuousParameters() {
		return continuousParameters;
	}

	@Override
	public void stop() throws DeviceException {

		if (getMotor().getStatus() == MotorStatus.READY)
			return;

		super.stop();
		resetDCMSpeed();
		if (toggleEnergyControl) {
			toggleEnergyControl();
		}
	}

	@Override
	public void atCommandFailure() throws DeviceException {
		stop();
	}

	private void resetDCMSpeed() throws DeviceException {
		try {
			logger.info("Setting energy to max speed");
			controller.caputWait(currentSpeedChnl, getMaxSpeed());
		} catch (Exception e) {
			throw new DeviceException("Exception while resetting the DCM speed", e);
		}
	}

	private void toggleEnergyControl() throws DeviceException {
		try {
			long timeAtMethodStart = System.currentTimeMillis();
			// return to regular running values
			logger.info("Toggling energy control");
			controller.caputWait(energySwitchChnl, 0);
			controller.caputWait(energySwitchChnl, 1);
			long timeAtMethodEnd = System.currentTimeMillis();
			logger.debug("Time spent in stop = " + (timeAtMethodEnd - timeAtMethodStart) + "ms");
		} catch (Exception e) {
			throw new DeviceException("Exception while changing energy switch off/on to stop the motion", e);
		}
	}

	private double radToDeg(Angle angle) {
		double amount = QuantityFactory.createFromObject(angle, NonSI.DEGREE_ANGLE).getAmount();
		return amount;
	}

	private double angleToEV(Angle angle) throws TimeoutException, CAException, InterruptedException {
		long timeAtMethodStart = System.currentTimeMillis();
		double amount = QuantityFactory.createFromObject(PhotonEnergy.photonEnergyOf(angle, getTwoD()),
				NonSI.ELECTRON_VOLT).getAmount();
		long timeAtMethodEnd = System.currentTimeMillis();
		logger.debug("Time spent in angleToEV = " + (timeAtMethodEnd - timeAtMethodStart) + "ms");
		return amount;
	}

	/**
	 * @return 2*lattice spacing for the given Bragg crystal cut in use.
	 * @throws TimeoutException
	 * @throws CAException
	 * @throws InterruptedException
	 */
	private Length getTwoD() throws TimeoutException, CAException, InterruptedException {
		long timeAtMethodStart = System.currentTimeMillis();
		String xtalSwitch = controller.cagetString(xtalSwitchChnl);
		if (xtalSwitch.contains("111")) {
			return Quantity.valueOf(0.62711, SI.NANO(SI.METER));
		} else if (xtalSwitch.contains("311")) {
			return Quantity.valueOf(0.327, SI.NANO(SI.METER));
		}
		Length valueOf = Quantity.valueOf(0.62711, SI.NANO(SI.METER));
		long timeAtMethodEnd = System.currentTimeMillis();
		logger.debug("Time spent in getTwoD = " + (timeAtMethodEnd - timeAtMethodStart) + "ms");
		return valueOf;
	}

	public boolean isExafs() {
		return true;
	}

	@Override
	public double calculateEnergy(int frameIndex) throws DeviceException {
		try {
			double continuousCountSteps;
			if (!calcEndFromGui) {
				if (endAngle.getAmount() > startAngle.getAmount())
					continuousCountSteps = -stepIncDemDeg;// back
				else
					continuousCountSteps = stepIncDemDeg;// forth
			} else {// step from editor
				stepSize = (Angle) (startAngle.minus(endAngle)).divide(continuousParameters.getNumberDataPoints());
				continuousCountSteps = (Math.round(radToDeg(stepSize) * 111121.98) / 111121.98);
			}
			// all angles below in units of Radians.
			// add 1.5 to the frame index as the B18 Position Compare does not send the first pulse so the first frame is
			// always missed.
			double braggAngle = startAngle.doubleValue() - (frameIndex + 1.5) * Math.toRadians(continuousCountSteps);
			Length twoD = getTwoD();
			double top = (Constants.h.times(Constants.c).divide(Constants.ePlus)).doubleValue();
			double bottom = twoD.doubleValue() * Math.sin(braggAngle);
			double result = top / bottom;
			return result;
		} catch (Exception e) {
			throw new DeviceException(e.getMessage());
		}
	}

	private void calculateMotionInDegrees() throws TimeoutException, CAException, InterruptedException {
		long timeAtMethodStart = System.currentTimeMillis();
		Length twoD = getTwoD();

		Energy startEng = Quantity.valueOf(continuousParameters.getStartPosition(), NonSI.ELECTRON_VOLT);
		startAngle = BraggAngle.braggAngleOf(startEng, twoD);

		Energy endEng = Quantity.valueOf(continuousParameters.getEndPosition(), NonSI.ELECTRON_VOLT);
		// calculate end energy from start, step increment, and number of pulses.
		endAngle = BraggAngle.braggAngleOf(endEng, twoD);

		if (!calcEndFromGui) {
			newStartAngleDeg = radToDeg(startAngle);
			newEndAngleDeg = radToDeg(endAngle);
			newNumberOfDataPoints = continuousParameters.getNumberDataPoints();
			newStep = Math.abs(newEndAngleDeg - newStartAngleDeg) / newNumberOfDataPoints;
		}

		stepSize = (Angle) (startAngle.minus(endAngle)).divide(continuousParameters.getNumberDataPoints());

		// Calculate run up and run down

		// v^2 = u^2 + 2as
		double acceleration = controller.cagetDouble(accelChnl);
		desiredSpeed = Math.abs(radToDeg(endAngle) - radToDeg(startAngle)) / continuousParameters.getTotalTime();
		double runUp = (desiredSpeed * desiredSpeed) / (2 * acceleration);
		runUp *= 3.0; // to be safe add 10%
		// Angle runUpAngle = (Angle) QuantityFactory.createFromObject(runUp, NonSI.DEGREE_ANGLE);
		// 1.165E-4 deg is a practical minimum to avoid the motor's deadband
		double step = Math.abs(radToDeg(stepSize));// controller.cagetDouble(this.stepIncDegChnl);

		if (runUp < 10 * step) {// 0.0001165
			runUp = 10 * step;
		}

		Quantity add = QuantityFactory.createFromObject(extraRunUp, NonSI.DEGREE_ANGLE);

		Angle runUpAngle = (Angle) QuantityFactory.createFromObject(runUp, NonSI.DEGREE_ANGLE).plus(add);
		logger.debug("Run up size: " + runUpAngle.getAmount() + "deg");

		// backwards
		if (endAngle.getAmount() > startAngle.getAmount()) {
			runupPosition = (Angle) startAngle.minus(runUpAngle);
			runDownPosition = (Angle) endAngle.plus(runUpAngle);
		}

		// forwards
		else {
			runupPosition = (Angle) startAngle.plus(runUpAngle);
			runDownPosition = (Angle) endAngle.minus(runUpAngle);
		}
		long timeAtMethodEnd = System.currentTimeMillis();
		logger.debug("Time spent in calculateMotionInDegrees = " + (timeAtMethodEnd - timeAtMethodStart) + "ms");
	}

	@Override
	public void setContinuousParameters(ContinuousParameters parameters) {
		continuousParameters = parameters;
	}

	@Override
	public void initializationCompleted() {
		channelsConfigured = true;
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

	public String getNumPulsesPV() {
		return numPulsesPV;
	}

	public void setNumPulsesPV(String numPulsesPV) {
		this.numPulsesPV = numPulsesPV;
	}

	public String getBraggCurrentSpeedPV() {
		return braggCurrentSpeedPV;
	}

	public void setBraggCurrentSpeedPV(String braggCurrentSpeedPV) {
		this.braggCurrentSpeedPV = braggCurrentSpeedPV;
	}

	public double getExtraRunUp() {
		return extraRunUp;
	}

	public void setExtraRunUp(double extraRunUp) {
		this.extraRunUp = extraRunUp;
	}

	public boolean isRunUpOn() {
		return runUpOn;
	}

	public void setRunUpOn(boolean runUpOn) {
		this.runUpOn = runUpOn;
	}

	public boolean isRunDownOn() {
		return runDownOn;
	}

	public void setRunDownOn(boolean runDownOn) {
		this.runDownOn = runDownOn;
	}

	public boolean isCalcEndFromGui() {
		return calcEndFromGui;
	}

	public void setCalcEndFromGui(boolean calcEndFromGui) {
		this.calcEndFromGui = calcEndFromGui;
	}

	public String getStepIncDemandPV() {
		return stepIncDemandPV;
	}

	public void setStepIncDemandPV(String stepIncDemandPV) {
		this.stepIncDemandPV = stepIncDemandPV;
	}

	@Override
	public int getNumberOfDataPoints() {
		try {
			return controller.cagetInt(this.numPulsesChnl);
		} catch (Exception e) {
			logger.error("Error getting number of data points from controller", e);
		}
		return 0;
	}

	/**
	 * When true then the energy control PV will be toggled at the end of a qexafs. Default is false.
	 *
	 * @return boolean
	 */
	public boolean isToggleEnergyControl() {
		return toggleEnergyControl;
	}

	public void setToggleEnergyControl(boolean toggleEnergyControl) {
		this.toggleEnergyControl = toggleEnergyControl;
	}
}
