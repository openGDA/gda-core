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

package gda.device.scannable.zebra;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Energy;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;
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
import gda.jscience.physics.quantities.QuantityConverters;
import gda.util.QuantityFactory;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.TimeoutException;

/**
 * Under development for B18 only.
 * <p>
 * Proposed replacement for the current gda.device.scannable.QexafsScannable.
 */
public abstract class QexafsScannable extends ScannableMotor implements ContinuouslyScannable, InitializationListener {

	private static final Logger logger = LoggerFactory.getLogger(QexafsScannable.class);

	protected ContinuousParameters continuousParameters;
	protected String accelPV; // in degrees/s/s
	protected String xtalSwitchPV; // will be a string such as "Si(111)"
	protected String braggCurrentSpeedPV; // the max and default speed of
	protected String braggMaxSpeedPV; // the max and default speed of
	protected String energySwitchPV; // combined energy motion flag

	protected boolean channelsConfigured = false;
	protected EpicsController controller;
	protected EpicsChannelManager channelManager;
	protected Channel accelChnl;
	protected Channel xtalSwitchChnl;
	protected Channel currentSpeedChnl;
	protected Channel maxSpeedChnl;
	protected Channel energySwitchChnl;

	protected Amount<Angle> startAngle;
	protected Amount<Angle> endAngle;
	protected Amount<Angle> stepSize;
	protected Amount<Angle> runupPosition;
	protected Amount<Angle> runDownPosition;

	protected Double maxSpeed; // in deg/sec
	protected Double desiredSpeed; // in deg/sec

	protected double extraRunUp = 0;
	private double runUpScaleFactor = 3.0;

	protected boolean runUpOn = true;
	protected boolean runDownOn = true;
	private boolean doToggleEnergyControl = true;
	protected double stepIncDemDeg;


	private Amount<Length> twoDValue;


	@Override
	public void configure() throws FactoryException {
		if (isConfigured()) {
			return;
		}
		try {
			controller = EpicsController.getInstance();
			channelManager = new EpicsChannelManager(this);

			accelChnl = channelManager.createChannel(accelPV, false);
			xtalSwitchChnl = channelManager.createChannel(xtalSwitchPV, false);
			currentSpeedChnl = channelManager.createChannel(braggCurrentSpeedPV, false);
			maxSpeedChnl = channelManager.createChannel(braggMaxSpeedPV, false);
			energySwitchChnl = channelManager.createChannel(energySwitchPV, false);

		} catch (CAException e) {
			throw new FactoryException("CAException while creating channels for " + getName(), e);
		}

		super.configure();
		setConfigured(true);
	}

	protected double energyToDegrees(double energy) {
		Amount<Energy> valueOf = Amount.valueOf(energy, NonSI.ELECTRON_VOLT);
		Amount<Angle> braggAngleOf = null;
		try {
			braggAngleOf = QuantityConverters.braggAngleFromEnergy(valueOf, getTwoD());
		} catch (Exception e) {
			logger.error("Exception fetching Bragg angle", e);
		}
		return radToDeg(braggAngleOf);
	}

	protected void checkDeadbandAndMove(double positionInEV) {
		double currentPositionInEV = 0;
		try {
			currentPositionInEV = (Double) this.rawGetPosition();
		} catch (DeviceException e1) {
			logger.error("Could not read scannable position", e1);
		}

		double demandDegrees = energyToDegrees(positionInEV);
		double currentDegrees = energyToDegrees(currentPositionInEV);
		logger.debug("checkDeadbandAndMove : demand angle = {} deg, current angle = {} deg.", demandDegrees, currentDegrees);
		// Use tolerance (in degrees) of underlying scannable to determine whether to move
		if ( Math.abs(currentDegrees - demandDegrees) > getDemandPositionTolerance() ) {
			try {
				logger.debug("checkDeadbandAndMove : moving to {} deg", demandDegrees);
				asynchronousMoveTo(positionInEV);
			} catch (DeviceException e) {
				logger.error("Could not move scannable", e);
			}
		}
	}

	protected Double getMaxSpeed() {
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
	public ContinuousParameters getContinuousParameters() {
		return continuousParameters;
	}

	@Override
	public void stop() throws DeviceException {

		if (getMotor().getStatus() == MotorStatus.READY)
			return;

		super.stop();
		resetDCMSpeed();
		// always toggle the energy when stopping. This takes a couple of seconds but the motor will not be stopped
		// otherwise
		stopStartEnergyControl();
	}

	@Override
	public void atCommandFailure() throws DeviceException {
		stop();
	}

	@Override
	public void prepareForContinuousMove() throws DeviceException {
		// clear this, so it has to be re-read once every scan to make sure the value has not changed.
		twoDValue = null;
	}

	protected void resetDCMSpeed() throws DeviceException {
		try {
			logger.info("Setting energy to max speed");
			controller.caput(currentSpeedChnl, getMaxSpeed());
		} catch (Exception e) {
			throw new DeviceException("Exception while resetting the DCM speed", e);
		}
	}

	protected void toggleEnergyControl() throws DeviceException {
		// public boolean to switch this off, depending on circumstances e.g. the exact energies being used.
		if (!doToggleEnergyControl){
			return;
		} else {
			stopStartEnergyControl();
		}
	}

	protected void stopStartEnergyControl() throws DeviceException {
		try {

			long timeAtMethodStart = System.currentTimeMillis();
			// return to regular running values
			logger.info("Toggling energy control");
			controller.caputWait(energySwitchChnl, 0);
			controller.caputWait(energySwitchChnl, 1);
			long timeAtMethodEnd = System.currentTimeMillis();
			logger.debug("Time spent in stop = " + (timeAtMethodEnd - timeAtMethodStart) + "ms");

			// Do not catch all exceptions here, only look for the ones we expect. Other types should be propagated to
			// allow higher level handling instead of being wrapped in this method as a DeviceException.
			// TODO switch to Java 1.7 multiple catch statement
		} catch (InterruptedException e) {
			throw new DeviceException("Exception while changing energy switch off/on to stop the motion", e);
		} catch (TimeoutException e) {
			throw new DeviceException("Exception while changing energy switch off/on to stop the motion", e);
		} catch (CAException e) {
			throw new DeviceException("Exception while changing energy switch off/on to stop the motion", e);
		}
	}

	protected double radToDeg(Amount<Angle> angle) {
		double amount = QuantityFactory.createFromObject(angle, NonSI.DEGREE_ANGLE).getEstimatedValue();
		return amount;
	}

	protected double angleToEV(Amount<Angle> angle) throws TimeoutException, CAException, InterruptedException {
		double amount = QuantityFactory.createFromObject(QuantityConverters.photonEnergyFromBraggAngle(angle, getTwoD()),
				NonSI.ELECTRON_VOLT).getEstimatedValue();
		return amount;
	}

	/**
	 * @return 2*lattice spacing for the given Bragg crystal cut in use.
	 * @throws TimeoutException
	 * @throws CAException
	 * @throws InterruptedException
	 */
	protected Amount<Length> getTwoD() throws TimeoutException, CAException, InterruptedException {

		if (twoDValue == null){
			long timeAtMethodStart = System.currentTimeMillis();
			String xtalSwitch = controller.cagetString(xtalSwitchChnl);
			if (xtalSwitch.contains("111")) {
				return Amount.valueOf(0.62711, SI.NANO(SI.METER));
			} else if (xtalSwitch.contains("311")) {
				return Amount.valueOf(0.327, SI.NANO(SI.METER));
			}
			twoDValue = Amount.valueOf(0.62711, SI.NANO(SI.METER));
			long timeAtMethodEnd = System.currentTimeMillis();
			logger.debug("Time spent in getTwoD = " + (timeAtMethodEnd - timeAtMethodStart) + "ms");
		}
		return twoDValue;
	}

	protected double getAcceleration() throws TimeoutException, CAException, InterruptedException {
		return controller.cagetDouble(accelChnl);
	}

	public boolean isExafs() {
		return true;
	}

	protected void calculateMotionInDegrees() throws TimeoutException, CAException, InterruptedException {
		long timeAtMethodStart = System.currentTimeMillis();
		Amount<Length> twoD = getTwoD();

		Amount<Energy> startEng = Amount.valueOf(continuousParameters.getStartPosition(), NonSI.ELECTRON_VOLT);
		startAngle = QuantityConverters.braggAngleFromEnergy(startEng, twoD);

		Amount<Energy> endEng = Amount.valueOf(continuousParameters.getEndPosition(), NonSI.ELECTRON_VOLT);
		// calculate end energy from start, step increment, and number of pulses.
		endAngle = QuantityConverters.braggAngleFromEnergy(endEng, twoD);

		stepSize = (startAngle.minus(endAngle)).divide(continuousParameters.getNumberDataPoints());

		// Calculate run up and run down

		// v^2 = u^2 + 2as
		double acceleration = getAcceleration();
		desiredSpeed = Math.abs(radToDeg(endAngle) - radToDeg(startAngle)) / continuousParameters.getTotalTime();
		double runUp = (desiredSpeed * desiredSpeed) / (2 * acceleration);
		runUp *= runUpScaleFactor; // to be safe multiply by scale factor (>1)
		// Angle runUpAngle = (Angle) QuantityFactory.createFromObject(runUp, NonSI.DEGREE_ANGLE);
		// 1.165E-4 deg is a practical minimum to avoid the motor's deadband
		double step = Math.abs(radToDeg(stepSize));// controller.cagetDouble(this.stepIncDegChnl);

		if (runUp < 2 * step) {// 0.0001165
			runUp = 2 * step;
		}

		Amount<Angle> add = QuantityFactory.createFromObject(extraRunUp, NonSI.DEGREE_ANGLE);

		Amount<Angle> runUpAngle = (QuantityFactory.createFromObject(runUp, NonSI.DEGREE_ANGLE).plus(add));
		logger.debug("Run up size: " + runUpAngle.getEstimatedValue() + "deg");

		// backwards
		if (endAngle.getEstimatedValue() > startAngle.getEstimatedValue()) {
			runupPosition = startAngle.minus(runUpAngle);
			runDownPosition = endAngle.plus(runUpAngle);
		}

		// forwards
		else {
			runupPosition = startAngle.plus(runUpAngle);
			runDownPosition = endAngle.minus(runUpAngle);
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

	/**
	 * When true then the energy control PV will be toggled at the end of a qexafs. Default is false.
	 *
	 * @return boolean
	 */
	public boolean isToggleEnergyControl() {
		return doToggleEnergyControl;
	}

	public void setToggleEnergyControl(boolean toggleEnergyControl) {
		this.doToggleEnergyControl = toggleEnergyControl;
	}

	public double getRunUpScaleFactor() {
		return runUpScaleFactor;
	}

	public void setRunUpScaleFactor(double runUpScaleFactor) {
		this.runUpScaleFactor = runUpScaleFactor;
	}
}
