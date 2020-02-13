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

import org.eclipse.dawnsci.analysis.api.diffraction.DiffractionCrystalEnvironment;
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

	protected double startAngle;
	protected double endAngle;
	protected double stepSize;
	protected double runupPosition;
	protected double runDownPosition;

	protected Double maxSpeed; // in deg/sec
	protected Double desiredSpeed; // in deg/sec

	protected double extraRunUp = 0;
	private double runUpScaleFactor = 3.0;

	protected boolean runUpOn = true;
	protected boolean runDownOn = true;
	private boolean doToggleEnergyControl = true;
	protected double stepIncDemDeg;


	private Double twoDValue;

	private enum CrystalSpacings {
		Si_111("111", 0.62711e-9),
		Si_311("311", 0.327e-9);

		private final String label;
		private final double val;

		private CrystalSpacings(String label, double val) {
			this.label = label;
			this.val = val;
		}
		public String getLabel() {
			return label;
		}
		public double getCrystal2d() {
			return val;
		}
	}

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


	protected void checkDeadbandAndMove(double positionInEV) throws TimeoutException, CAException, InterruptedException {
		double currentPositionInEV = 0;
		try {
			currentPositionInEV = (Double) this.rawGetPosition();
		} catch (DeviceException e1) {
			logger.error("Could not read scannable position", e1);
		}
		double demandDegrees = Math.toDegrees(convertEnergyToAngle(positionInEV, getTwoD()));
		double currentDegrees = Math.toDegrees(convertEnergyToAngle(currentPositionInEV, getTwoD()));
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
		logger.debug("Time spent in getMaxSpeed = {} ms", timeAtMethodEnd - timeAtMethodStart);
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
			logger.debug("Time spent in stop = {} ms", timeAtMethodEnd - timeAtMethodStart);

			// Do not catch all exceptions here, only look for the ones we expect. Other types should be propagated to
			// allow higher level handling instead of being wrapped in this method as a DeviceException.
			// TODO switch to Java 1.7 multiple catch statement
		} catch (InterruptedException | TimeoutException | CAException e) {
			throw new DeviceException("Exception while changing energy switch off/on to stop the motion", e);
		}
	}

	/**
	 * @return 2*lattice spacing for the given Bragg crystal cut in use.
	 * @throws TimeoutException
	 * @throws CAException
	 * @throws InterruptedException
	 */
	protected double getTwoD() throws TimeoutException, CAException, InterruptedException {

		if (twoDValue == null){
			long timeAtMethodStart = System.currentTimeMillis();
			String xtalSwitch = controller.cagetString(xtalSwitchChnl);
			for(CrystalSpacings s : CrystalSpacings.values() ) {
				if (xtalSwitch.contains(s.getLabel()) ) {
					return s.getCrystal2d();
				}
			}
			twoDValue = CrystalSpacings.Si_111.getCrystal2d();
			long timeAtMethodEnd = System.currentTimeMillis();
			logger.debug("Time spent in getTwoD = {} ms ", timeAtMethodEnd - timeAtMethodStart);
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
		double twoD = getTwoD();

		// Start, end step size angles are in radians
		startAngle = convertEnergyToAngle(continuousParameters.getStartPosition(), twoD);
		endAngle = convertEnergyToAngle(continuousParameters.getEndPosition(), twoD);
		stepSize = (startAngle - endAngle)/continuousParameters.getNumberDataPoints();

		// v^2 = u^2 + 2as
		double acceleration = getAcceleration(); // acceleration of the motor (degrees/sec/sec)
		desiredSpeed = Math.abs(Math.toDegrees(endAngle) - Math.toDegrees(startAngle)) / continuousParameters.getTotalTime();
		double runUp = (desiredSpeed * desiredSpeed) / (2 * acceleration); // degrees

		runUp *= runUpScaleFactor; // to be safe multiply by scale factor (>1)
		// Angle runUpAngle = (Angle) QuantityFactory.createFromObject(runUp, NonSI.DEGREE_ANGLE);
		// 1.165E-4 deg is a practical minimum to avoid the motor's deadband
		double step = Math.abs(Math.toDegrees(stepSize));// controller.cagetDouble(this.stepIncDegChnl);

		if (runUp < 2 * step) {// 0.0001165
			runUp = 2 * step;
		}
		// Total runup
		double runUpAngle = Math.toRadians(runUp + extraRunUp);
		logger.debug("Run up size: {} degrees", runUpAngle);

		// backwards
		if (endAngle > startAngle) {
			runupPosition = startAngle - runUpAngle;
			runDownPosition = endAngle + runUpAngle;
		}

		// forwards
		else {
			runupPosition = startAngle + runUpAngle;
			runDownPosition = endAngle - runUpAngle;
		}
		long timeAtMethodEnd = System.currentTimeMillis();
		logger.debug("Time spent in calculateMotionInDegrees = {} ms", timeAtMethodEnd - timeAtMethodStart);
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

	/**
	 * Convert from bragg angle [radians] to energy [eV] using current 2d spacing set from Epics crystal type.
	 * @param angleRad
	 * @return bragg angle [radians]
	 * @throws TimeoutException
	 * @throws CAException
	 * @throws InterruptedException
	 */
	protected double convertAngleToEnergy(double angleRad) throws TimeoutException, CAException, InterruptedException {
		return convertAngleToEnergy(angleRad, getTwoD());
	}

	/**
	 * Convert from bragg angle [radians] to energy [eV]
	 * @param angleRad bragg angle [radians]
	 * @param twoD crystal 2d spacing [metres]
	 * @return energy [eV]
	 */
	protected double convertAngleToEnergy(double angleRad, double twoD) {
		double wave = twoD*Math.sin(angleRad);
		DiffractionCrystalEnvironment de = new DiffractionCrystalEnvironment(wave*1e10);
		return de.getEnergy()*1000;
	}

	/**
	 * Convert from energy [eV] to bragg angle [radians]
	 * @param energyEv
 	 * @param twoD
	 * @return bragg angle [radians]
	 */
	private double convertEnergyToAngle(double energyEv, double twoD) {
		double wave = convertEnergyToWavelength(energyEv);
		return convertWavelengthToAngle(wave, twoD);
	}

	/**
	 * Convert from energy [eV] to wavelength [metres]
	 * @param energy [eV]
	 * @return
	 */
	private double convertEnergyToWavelength(double energyEv) {
		DiffractionCrystalEnvironment de = new DiffractionCrystalEnvironment();
		de.setWavelengthFromEnergykeV(energyEv*0.001);
		return de.getWavelength()*1e-10;
	}

	/**
	 * Convert from wavelength [metres] to angle [radians] using bragg equation
	 * @param wavelength [metres]
	 * @param twoD crystal 2d spacing [metres]
	 * @return
	 */
	private double convertWavelengthToAngle(double wavelengthM, double twoD) {
		return Math.asin(wavelengthM/twoD);
	}
}
