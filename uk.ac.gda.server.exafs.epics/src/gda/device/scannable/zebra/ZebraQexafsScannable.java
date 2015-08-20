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

import org.jscience.physics.quantities.Angle;
import org.jscience.physics.units.NonSI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
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

	private String armTrigSourcePV = "BL18B-OP-DCM-01:ZEBRA:PC_ARM_SEL";
	private String armPV = "BL18B-OP-DCM-01:ZEBRA:PC_ARM";
	private String disarmPV = "BL18B-OP-DCM-01:ZEBRA:PC_DISARM";

	private String gateTrigSourcePV = "BL18B-OP-DCM-01:ZEBRA:PC_GATE_SEL";
	private String gateStartPV = "BL18B-OP-DCM-01:ZEBRA:PC_GATE_START";
	private String gateWidthPV = "BL18B-OP-DCM-01:ZEBRA:PC_GATE_WID";
	private String numGatesPV = "BL18B-OP-DCM-01:ZEBRA:PC_GATE_NGATE";

	private String pulseTrigSourcePV = "BL18B-OP-DCM-01:ZEBRA:PC_PULSE_SEL";
	private String pulseStartPV = "BL18B-OP-DCM-01:ZEBRA:PC_PULSE_START";
	private String pulseWidthPV = "BL18B-OP-DCM-01:ZEBRA:PC_PULSE_WID";
	private String pulseStepPV = "BL18B-OP-DCM-01:ZEBRA:PC_PULSE_STEP";

	private String positionTrigPV = "BL18B-OP-DCM-01:ZEBRA:PC_ENC";
	private String positionDirectionPV = "BL18B-OP-DCM-01:ZEBRA:PC_DIR";

	private String startReadback_deg_PV = "BL18B-OP-DCM-01:ZEBRA:PC_GATE_START:RBV";
	// private String startReadback_counts_PV = "BL18B-OP-DCM-01:ZEBRA:PC_GATE_START:RBV_CTS";
	// private String stepSizeReadback_deg_PV = "BL18B-OP-DCM-01:ZEBRA:PC_PULSE_STEP:RBV";
	private String stepSizeReadback_counts_PV = "BL18B-OP-DCM-01:ZEBRA:PC_PULSE_STEP:RBV_CTS";
	private String widthReadback_deg_PV = "BL18B-OP-DCM-01:ZEBRA:PC_GATE_WID:RBV";
	private String widthReadback_counts_PV = "BL18B-OP-DCM-01:ZEBRA:PC_GATE_WID:RBV_CTS";

	private Channel armTrigSourceChnl;
	private Channel armChnl;
	private Channel disarmChnl;
	private Channel gateTrigSourceChnl;
	private Channel gateStartChnl;
	private Channel gateWidthChnl;
	private Channel numGatesChnl;
	private Channel pulseTrigSourceChnl;
	private Channel pulseStartChnl;
	private Channel pulseWidthChnl;
	private Channel pulseStepChnl;
	private Channel positionTrigChnl;
	private Channel positionDirectionChnl;
	private Channel startReadback_deg_Chnl;
	// private Channel startReadback_counts_Chnl;
	// private Channel stepSizeReadback_deg_Chnl;
	private Channel stepSizeReadback_counts_Chnl;
	private Channel widthReadback_deg_Chnl;
	private Channel widthReadback_counts_Chnl;

	private double startReadback_deg;

	private double stepSize_counts;

	private double width_deg;

	private double width_counts;
	
	protected Channel isArmedChnl;

	@Override
	public void configure() throws FactoryException {
		super.configure();

		try {
			armTrigSourceChnl = channelManager.createChannel(armTrigSourcePV, false);
			armChnl = channelManager.createChannel(armPV, false);
			disarmChnl = channelManager.createChannel(disarmPV, false);
			gateTrigSourceChnl = channelManager.createChannel(gateTrigSourcePV, false);
			gateStartChnl = channelManager.createChannel(gateStartPV, false);
			gateWidthChnl = channelManager.createChannel(gateWidthPV, false);
			numGatesChnl = channelManager.createChannel(numGatesPV, false);
			pulseTrigSourceChnl = channelManager.createChannel(pulseTrigSourcePV, false);
			pulseStartChnl = channelManager.createChannel(pulseStartPV, false);
			pulseWidthChnl = channelManager.createChannel(pulseWidthPV, false);
			pulseStepChnl = channelManager.createChannel(pulseStepPV, false);
			positionTrigChnl = channelManager.createChannel(positionTrigPV, false);
			positionDirectionChnl = channelManager.createChannel(positionDirectionPV, false);
			startReadback_deg_Chnl = channelManager.createChannel(startReadback_deg_PV, false);
			// startReadback_counts_Chnl = channelManager.createChannel(startReadback_counts_PV, false);
			// stepSizeReadback_deg_Chnl = channelManager.createChannel(stepSizeReadback_deg_PV, false);
			stepSizeReadback_counts_Chnl = channelManager.createChannel(stepSizeReadback_counts_PV, false);
			widthReadback_deg_Chnl = channelManager.createChannel(widthReadback_deg_PV, false);
			widthReadback_counts_Chnl = channelManager.createChannel(widthReadback_counts_PV, false);

			
			isArmedChnl = channelManager.createChannel("BL18B-OP-DCM-01:ZEBRA:PC_ARM_OUT",false);

			channelManager.creationPhaseCompleted();

		} catch (CAException e) {
			throw new FactoryException("CAException while creating channels for " + getName(), e);
		}
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

			// TODO tidy up and move Strings to constants

			// fixed settings
			logger.debug("Time before fixed zebra settings");
			Boolean changeHasBeenMade = Boolean.FALSE;
			changeHasBeenMade = caputTestChangeString(armTrigSourceChnl, "Soft", changeHasBeenMade);
			changeHasBeenMade = caputTestChangeString(gateTrigSourceChnl, "Position", changeHasBeenMade);
			changeHasBeenMade = caputTestChangeDouble(numGatesChnl, 1, changeHasBeenMade);
			changeHasBeenMade = caputTestChangeString(pulseTrigSourceChnl, "Position", changeHasBeenMade);
			changeHasBeenMade = caputTestChangeDouble(pulseStartChnl, 0.0, changeHasBeenMade);
			// controller.caput(pulseWidthChnl, 0.0020);
			changeHasBeenMade = caputTestChangeString(positionTrigChnl, "Enc1-4Av", changeHasBeenMade);

			// variable settings
			logger.debug("Time before variable zebra settings");
			double startDeg = radToDeg(startAngle);
			double stopDeg = radToDeg(endAngle);
			double stepDeg = Math.abs(radToDeg(stepSize));
			double width = Math.abs(stopDeg - startDeg);
			String positionDirection = stopDeg > startDeg ? "Positive" : "Negative";
			changeHasBeenMade = caputTestChangeString(positionDirectionChnl, positionDirection, changeHasBeenMade);
			changeHasBeenMade = caputTestChangeDouble(gateStartChnl, startDeg, changeHasBeenMade);
			changeHasBeenMade = caputTestChangeDouble(gateWidthChnl, width, changeHasBeenMade);

			// this value is set by beamline staff, and is not altered by GDA. It MUST be < stepDeg
			double pulseWidth = controller.cagetDouble(pulseWidthChnl);
			if (pulseWidth > stepDeg) {
				throw new DeviceException(
						"Inconsistent Zebra parameters: the pulse width is greater than the required pulse step, so Zebra will not emit any pulses! You need to change you scan parameters or ask beamline staff.");
			}

			changeHasBeenMade = caputTestChangeDouble(pulseStepChnl, stepDeg, changeHasBeenMade);

			// Has a change been made, so do we need to wait for the template to complete processing?
			// We must wait here if we have made a change so that any subsequent reads e.g. in getNumberOfDataPoints()
			// are consistent with the parameters in this method
			if (changeHasBeenMade) {
				logger.debug("Have changed zebra settings, so sleeping for 1 second to ensure they have been set");
				// yuck, but even if we go a caputwait to ensure that the Zebra record has finished processing,
				// the readback values used in the getNumberOfDataPoints() come out incorrect.
				Thread.sleep(1000);
			}

			logger.debug("Time after final zebra set");
			long timeAtMethodEnd = System.currentTimeMillis();
			logger.debug("Time spent in prepareForContinuousMove = " + (timeAtMethodEnd - timeAtMethodStart) + "ms");

		} catch (DeviceException e) {
			throw e;
		} catch (Exception e) {
			throw new DeviceException(getName() + " exception in prepareForContinuousMove", e);
		}
	}

	private Boolean caputTestChangeString(Channel theChannel, String toPut, Boolean changeMade) throws CAException,
			InterruptedException, TimeoutException {
		String current = controller.cagetString(theChannel);
		if (current.compareTo(toPut) != 0) {
			controller.caput(theChannel, toPut.toString());
			changeMade = Boolean.TRUE; // only update to true
		}
		return changeMade;
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
			double stepSize_counts = controller.cagetDouble(stepSizeReadback_counts_Chnl);
			double width_counts = controller.cagetDouble(widthReadback_counts_Chnl);
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
				controller.caput(armChnl, 1);
				int isArmed = controller.cagetInt(isArmedChnl);
				int timeWaitingToArm = 0; // ms
				while (isArmed == 0){
					Thread.sleep(100);
					isArmed = controller.cagetInt(isArmedChnl);
					timeWaitingToArm += 100;
					
					if(timeWaitingToArm > 20000){
						throw new DeviceException("20s timeout waiting for Zebra to arm");
					}
				}
			
				// These will be used when calculating the real energy of each step in the scan, so readback once at this point.
				logger.debug("Time before zebra readbacks");
				startReadback_deg = controller.cagetDouble(startReadback_deg_Chnl);
				stepSize_counts = controller.cagetDouble(stepSizeReadback_counts_Chnl);
				width_deg = controller.cagetDouble(widthReadback_deg_Chnl);
				width_counts = controller.cagetDouble(widthReadback_counts_Chnl);

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

	@Override
	public void continuousMoveComplete() throws DeviceException {
		
		// should ensure that the motor has finished moving before we carry on
		if (isBusy()){
			try {
				waitWhileBusy(5);
			} catch (DeviceException e) {
				// DeviceException means a timeout, so call stop and continue
				logger.error("Exception while waiting for the qexafs mono movement to finish by itself, so stopping the motor automatically", e);
				stop();
			} catch (InterruptedException e) {
				// if interrupted then someone is aborting the scan, so must re-throw the exception
				throw new DeviceException(e.getMessage(),e);
			}
		}
		
		long timeAtMethodStart = System.currentTimeMillis();
		// return to regular running values
		resetDCMSpeed();
		try {
			controller.caputWait(disarmChnl, 1);
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
		Angle frameCentre_angle = (Angle) QuantityFactory.createFromObject(frameCentre_deg, NonSI.DEGREE_ANGLE);
		double frameCentre_eV = angleToEV(frameCentre_angle);
		return frameCentre_eV;
	}
}
