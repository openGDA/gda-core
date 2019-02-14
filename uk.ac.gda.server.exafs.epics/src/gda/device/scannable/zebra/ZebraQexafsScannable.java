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

import java.io.IOException;

import javax.measure.quantity.Angle;
import javax.measure.unit.NonSI;

import org.jscience.physics.amount.Amount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.zebra.controller.Zebra;
import gda.epics.CachedLazyPVFactory;
import gda.factory.FactoryException;
import gda.jython.InterfaceProvider;
import gda.util.QuantityFactory;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.TimeoutException;

/**
 * The Zebra-specific parts of the Qexafs movement for B18.
 * <p>
 * I had hoped that this would be a generic solution for any motor movement involving Zebra, but it seems that there
 * have had to be many solution-specific parts added.
 * <p>
 * So while this could not be used 'as is' on other beamlines, but it can be a template for other beamlines looking for
 * a similar solution with Zebra.
 */
public class ZebraQexafsScannable extends QexafsScannable {

	private static final Logger logger = LoggerFactory.getLogger(ZebraQexafsScannable.class);

	private Zebra zebraDevice = null;

	private CachedLazyPVFactory pvFactory;
	private static final String PCArm = "PC_ARM";
	private static final String PCPulseStepCountsRBV = "PC_PULSE_STEP:RBV_CTS";
	private static final String PCGateWidthCountsRBV = "PC_GATE_WID:RBV_CTS";

	private double startReadback_deg;

	private double stepSize_counts;

	private double width_deg;

	private double width_counts;


	@Override
	public void configure() throws FactoryException {
		super.configure();
		if (zebraDevice==null) {
			throw new FactoryException("Zebra device has not been set");
		}
		pvFactory = new CachedLazyPVFactory(zebraDevice.getZebraPrefix());
		channelManager.creationPhaseCompleted();
	}

	@Override
	public void prepareForContinuousMove() throws DeviceException {
		long timeAtMethodStart = System.currentTimeMillis();
		super.prepareForContinuousMove();
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
			InterfaceProvider.getTerminalPrinter().print("Moving mono to run-up position...");
			logger.info("Moving mono to run-up position...");
			// but first, ensure that the energy control switch is set to 'on'
			if (controller.cagetInt(energySwitchChnl) == 0){
				controller.caputWait(energySwitchChnl, 1);
			}
			double runupEnergy = angleToEV(runupPosition);
			if (!runUpOn) {
				runupEnergy = angleToEV(startAngle);
			}

			// always toggle before first movement in a scan
			toggleEnergyControl();

			checkDeadbandAndMove(runupEnergy);
			logger.debug("Time spent after moved to angle");

			// fixed settings
			logger.debug("Time before fixed zebra settings");
			zebraDevice.setPCEnc(Zebra.PC_ENC_ENCSUM); //Enc1-4Av
			zebraDevice.setPCArmSource(Zebra.PC_ARM_SOURCE_SOFT);
			zebraDevice.setPCGateSource(Zebra.PC_GATE_SOURCE_POSITION);
			zebraDevice.setPCGateNumberOfGates(1);
			zebraDevice.setPCPulseSource(Zebra.PC_PULSE_SOURCE_POSITION);
			zebraDevice.setPCPulseStart(0.0);

			// variable settings
			logger.debug("Time before variable zebra settings");
			double startDeg = radToDeg(startAngle);
			double stopDeg = radToDeg(endAngle);
			double stepDeg = Math.abs(radToDeg(stepSize));
			double width = Math.abs(stopDeg - startDeg);

			int positionDir = stopDeg > startDeg ? Zebra.PC_DIR_POSITIVE : Zebra.PC_DIR_NEGATIVE;
			zebraDevice.setPCDir(positionDir);
			zebraDevice.setPCGateStart(startDeg);
			zebraDevice.setPCGateWidth(width);

			// this value is set by beamline staff, and is not altered by GDA. It MUST be < stepDeg
			double pulseWidth = zebraDevice.getPCPulseWidth();
			if (pulseWidth > stepDeg) {
				throw new DeviceException(
						"Inconsistent Zebra parameters: the pulse width is greater than the required pulse step, so Zebra will not emit any pulses! You need to change you scan parameters or ask beamline staff.");
			}
			zebraDevice.setPCPulseStep(stepDeg);

			logger.debug("Time after final zebra set");
			long timeAtMethodEnd = System.currentTimeMillis();
			logger.debug("Time spent in prepareForContinuousMove = " + (timeAtMethodEnd - timeAtMethodStart) + "ms");

		} catch (DeviceException e) {
			throw e;
		} catch (Exception e) {
			throw new DeviceException(getName() + " exception in prepareForContinuousMove", e);
		}
	}

	private Boolean caputTestChangeDouble(Channel theChannel, double toPut, Boolean changeMade) throws CAException,
			InterruptedException, TimeoutException {
		double current = controller.cagetDouble(theChannel);
		double fractionalChange = Math.abs(current - toPut) / current;
		if (fractionalChange > 0.001) {
			controller.caput(theChannel, toPut);
			changeMade = Boolean.TRUE; // only update to true
		}
		return changeMade;
	}

	@Override
	public int getNumberOfDataPoints() {
		try {
			// get the actual step size in degrees
			double stepSize_counts = pvFactory.getPVDouble(PCPulseStepCountsRBV).get();
			double width_counts = pvFactory.getPVDouble(PCGateWidthCountsRBV).get();

			Double readbackNumberOfCounts_floored = Math.floor(width_counts / stepSize_counts);
			Double readbackNumberOfCounts = width_counts / stepSize_counts;

			if (readbackNumberOfCounts.equals(readbackNumberOfCounts_floored)) {
				int expectedCounts = (int) Math.round(readbackNumberOfCounts_floored) -1;
				logger.debug("Expecting " + expectedCounts + " points from Zebra.");
				return expectedCounts;
			}
			int expectedCounts = (int) Math.round(readbackNumberOfCounts_floored);
			logger.debug("Expecting from Zebra " + expectedCounts + " points.");

			return expectedCounts;
		} catch (Exception e) {
			logger.error(
					"Exception trying to get step size and width readback, assuming number of datapoints is the demanded amount",
					e);
			return continuousParameters.getNumberDataPoints();
		}
	}

	@Override
	public void performContinuousMove() throws DeviceException {
		long timeAtMethodStart = System.currentTimeMillis();
		if (channelsConfigured && continuousParameters != null) {
			try {

				while (isBusy()) {
					logger.info("-----waiting for qscanAxis to finish moving inside perform before starting scanning. after goto runup");
					Thread.sleep(100);
				}
				InterfaceProvider.getTerminalPrinter().print("Mono in position.");
				logger.info("Mono in position.");

				// set the speed (do this now, after the motor has been moved to the run-up position)
				if (desiredSpeed <= getMaxSpeed()) {
					caputTestChangeDouble(currentSpeedChnl, desiredSpeed, null);
				} else {
					logger.info("Continuous motion for " + getName()
							+ " greater than Bragg maximum speed. Speed will be set instead to the maximum speed of "
							+ getMaxSpeed() + " deg/s");
				}

				// always toggle the energy at the start
				toggleEnergyControl();

				// prepare zebra to send pulses
				logger.debug("Time before zebra arm with callback");
				InterfaceProvider.getTerminalPrinter().print("Arming Zebra box to trigger detectors during mono move...");
				logger.info("Arming Zebra box to trigger detectors during mono move...");

				zebraDevice.pcArm();

				// These will be used when calculating the real energy of each step in the scan, so readback once at this point.
				logger.debug("Time before zebra readbacks");
				startReadback_deg = zebraDevice.getPCGateStartRBV();
				stepSize_counts = pvFactory.getPVDouble(PCPulseStepCountsRBV).get();
				width_deg = zebraDevice.getPCGateWidthRBV();
				width_counts = pvFactory.getPVDouble(PCGateWidthCountsRBV).get();

				// do the move asynchronously to this thread
				InterfaceProvider.getTerminalPrinter().print("Mono move started.");
				logger.info("Mono move started.");
				if (runDownOn) {
					super.asynchronousMoveTo(angleToEV(runDownPosition));
				} else {
					super.asynchronousMoveTo(angleToEV(endAngle));
				}
			} catch (Exception e) {
				throw new DeviceException(e.getMessage(), e);
			}
		}
		long timeAtMethodEnd = System.currentTimeMillis();
		logger.debug("Time spent in performContinuousMove = " + (timeAtMethodEnd - timeAtMethodStart) + "ms");
	}

	private void logStatus() {
		try {
			logger.debug("Motor status : {}", getMotor().getStatus());
			logger.debug("Zebra number of captured points : {}", zebraDevice.getNumberOfPointsCapturedPV().get());
		} catch (DeviceException | IOException e) {
			logger.warn("Problem getting device status", e);
		}
	}

	@Override
	public void continuousMoveComplete() throws DeviceException {
		logger.debug("continuousMoveComplete() called");
		long timeAtMethodStart = System.currentTimeMillis();

		logStatus();

		// should ensure that the motor has finished moving before we carry on
		if (isBusy()){
			try {
				logger.debug("Waiting for motor move to finish");
				waitWhileBusy(5);
			} catch (DeviceException e) {
				// DeviceException means a timeout, so call stop and continue
				logger.error("Exception while waiting for the qexafs mono movement to finish by itself, so stopping the motor automatically", e);
				stop();
			} catch (InterruptedException e) {
				// if interrupted then someone is aborting the scan, so must re-throw the exception
				throw new DeviceException(e.getMessage(),e);
			}
			logStatus();
		}

		// return to regular running values
		resetDCMSpeed();
		try {
			zebraDevice.pcDisarm();
		} catch (Exception e) {
			logger.error("Exception while disarming the Zebra. But GDA will continue. This may cause an error later.", e);
		}
		long timeAtMethodEnd = System.currentTimeMillis();
		logger.debug("Time spent in continuousMoveComplete = " + (timeAtMethodEnd - timeAtMethodStart) + "ms");
	}

	@Override
	public double calculateEnergy(int frameIndex) throws DeviceException {
		try {
			return calculateFrameEnergyFromZebraReadback(frameIndex);
		} catch (Exception e) {
			throw new DeviceException("Exception wile calculating frame energy", e);
		}
	}

	private double calculateFrameEnergyFromZebraReadback(int frameIndex) throws TimeoutException, CAException,
			InterruptedException {
		double countsPerDegree = width_deg / width_counts;

		double frameCentre_offset_cts = ((stepSize_counts * frameIndex) + (0.5 * stepSize_counts));
		// TODO change sign based on direction and resolution
		double frameCentre_deg = startReadback_deg + (frameCentre_offset_cts * countsPerDegree);
		if (startAngle.isGreaterThan(endAngle)) {
			frameCentre_deg = startReadback_deg - (frameCentre_offset_cts * countsPerDegree);
		}
		Amount<Angle> frameCentre_angle = QuantityFactory.createFromObject(frameCentre_deg, NonSI.DEGREE_ANGLE);
		double frameCentre_eV = angleToEV(frameCentre_angle);
		return frameCentre_eV;
	}

	public Zebra getZebraDevice() {
		return zebraDevice;
	}

	public void setZebraDevice(Zebra zebraDevice) {
		this.zebraDevice = zebraDevice;
	}
}
