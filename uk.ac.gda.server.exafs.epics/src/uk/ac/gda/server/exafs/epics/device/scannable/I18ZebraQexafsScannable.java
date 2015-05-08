/*-
 * Copyright © 2015 Diamond Light Source Ltd.
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

import gda.device.ContinuousParameters;
import gda.device.DeviceException;
import gda.device.scannable.ContinuouslyScannable;
import gda.device.scannable.ScannableMotor;
import gda.epics.connection.EpicsChannelManager;
import gda.epics.connection.EpicsController;
import gda.epics.connection.InitializationListener;
import gda.factory.FactoryException;
import gda.jscience.physics.quantities.BraggAngle;
import gda.jscience.physics.quantities.PhotonEnergy;
import gda.jython.InterfaceProvider;
import gda.util.QuantityFactory;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.TimeoutException;

import org.jscience.physics.quantities.Angle;
import org.jscience.physics.quantities.Energy;
import org.jscience.physics.quantities.Length;
import org.jscience.physics.quantities.Quantity;
import org.jscience.physics.units.NonSI;
import org.jscience.physics.units.SI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * For quick-Xanes on I18 using Zebra.
 * <p>
 * I know this is very similar to a number of other ContinuouslyScannable classes for energy scans, but each scan on each
 * beamline with different hardware to provide the energy pulses has its own hardware-specific requirements, so much
 * better to have unique classes. (There is some shared logic which could be refactored out, but the main methods are
 * unique to each situation due to experiment and hardware requirements.).
 */
public class I18ZebraQexafsScannable extends ScannableMotor implements ContinuouslyScannable, InitializationListener {

	private static final Logger logger = LoggerFactory.getLogger(I18ZebraQexafsScannable.class);

	private EpicsController controller;
	private EpicsChannelManager channelManager;
	private boolean channelsConfigured = false;

	private String armTrigSourcePV = "BL18I-OP-DCM-01:ZEBRA:PC_ARM_SEL";
	private String armPV = "BL18I-OP-DCM-01:ZEBRA:PC_ARM";
	private String disarmPV = "BL18I-OP-DCM-01:ZEBRA:PC_DISARM";

	private String gateTrigSourcePV = "BL18I-OP-DCM-01:ZEBRA:PC_GATE_SEL";
	private String gateStartPV = "BL18I-OP-DCM-01:ZEBRA:PC_GATE_START";
	private String gateWidthPV = "BL18I-OP-DCM-01:ZEBRA:PC_GATE_WID";
	private String numGatesPV = "BL18I-OP-DCM-01:ZEBRA:PC_GATE_NGATE";

	private String pulseTrigSourcePV = "BL18I-OP-DCM-01:ZEBRA:PC_PULSE_SEL";
	private String pulseStartPV = "BL18I-OP-DCM-01:ZEBRA:PC_PULSE_START";
	private String pulseWidthPV = "BL18I-OP-DCM-01:ZEBRA:PC_PULSE_WID";
	private String pulseStepPV = "BL18I-OP-DCM-01:ZEBRA:PC_PULSE_STEP";

	private String positionTrigPV = "BL18I-OP-DCM-01:ZEBRA:PC_ENC";
	private String positionDirectionPV = "BL18I-OP-DCM-01:ZEBRA:PC_DIR";

	private String startReadback_deg_PV = "BL18I-OP-DCM-01:ZEBRA:PC_GATE_START:RBV";
	// private String startReadback_counts_PV = "BL18I-OP-DCM-01:ZEBRA:PC_GATE_START:RBV_CTS";
	// private String stepSizeReadback_deg_PV = "BL18I-OP-DCM-01:ZEBRA:PC_PULSE_STEP:RBV";
	private String stepSizeReadback_counts_PV = "BL18I-OP-DCM-01:ZEBRA:PC_PULSE_STEP:RBV_CTS";
	private String widthReadback_deg_PV = "BL18I-OP-DCM-01:ZEBRA:PC_GATE_WID:RBV";
	private String widthReadback_counts_PV = "BL18I-OP-DCM-01:ZEBRA:PC_GATE_WID:RBV_CTS";

//	private String xtalSwitchPV = "BL18I-OP-DCM-01:MP:SELECT";
//	private String accelPV = "BL18I-OP-DCM-01:BRAGG.ACCL";
	private String braggMaxSpeedPV = "BL18I-OP-DCM-01:ENERGY.VMAX";
	private String braggCurrentSpeedPV = "BL18I-OP-DCM-01:ENERGY.VELO";

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

//	private Channel xtalSwitchChnl;
//	private Channel accelChnl;
	private Channel maxSpeedChnl;
	private Channel currentSpeedChnl;

	private double startReadback_deg;
	private double stepSize_counts;
	private double width_deg;
	private double width_counts;

	private Angle startAngle;
	private Angle endAngle;
	private Angle stepSize;
//	private Angle runupPosition;
//	private Angle runDownPosition;

	private Double maxSpeed; // in deg/sec

	private ContinuousParameters continuousParameters;

	private double desiredSpeed;

	@Override
	public void configure() throws FactoryException {
		super.configure();

		try {
			controller = EpicsController.getInstance();
			channelManager = new EpicsChannelManager(this);

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

//			xtalSwitchChnl = channelManager.createChannel(xtalSwitchPV, false);
//			accelChnl = channelManager.createChannel(accelPV, false);
			maxSpeedChnl = channelManager.createChannel(braggMaxSpeedPV, false);
			currentSpeedChnl = channelManager.createChannel(braggCurrentSpeedPV, false);

			channelManager.creationPhaseCompleted();

		} catch (CAException e) {
			throw new FactoryException("CAException while creating channels for " + getName(), e);
		}
	}

	private void calculateMotionInDegrees() {

		Length twoD = getTwoD();

		Energy startEng = Quantity.valueOf(continuousParameters.getStartPosition(), NonSI.ELECTRON_VOLT);
		startAngle = BraggAngle.braggAngleOf(startEng, twoD);

		Energy endEng = Quantity.valueOf(continuousParameters.getEndPosition(), NonSI.ELECTRON_VOLT);
		endAngle = BraggAngle.braggAngleOf(endEng, twoD);

		stepSize = (Angle) (startAngle.minus(endAngle)).divide(continuousParameters.getNumberDataPoints());

		desiredSpeed = Math.abs(radToDeg(endAngle) - radToDeg(startAngle)) / continuousParameters.getTotalTime();
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

			// TODO check units!
			calculateMotionInDegrees();

			super.moveTo(angleToEV(startAngle) - 20);

			// fixed settings
			logger.debug("Time before fixed zebra settings");
			Boolean changeHasBeenMade = Boolean.FALSE;
			changeHasBeenMade = caputTestChangeString(armTrigSourceChnl, "Soft", changeHasBeenMade);
			changeHasBeenMade = caputTestChangeString(gateTrigSourceChnl, "Position", changeHasBeenMade);
			changeHasBeenMade = caputTestChangeDouble(numGatesChnl, 1, changeHasBeenMade);
			changeHasBeenMade = caputTestChangeString(pulseTrigSourceChnl, "Position", changeHasBeenMade);
			changeHasBeenMade = caputTestChangeDouble(pulseStartChnl, 0.0, changeHasBeenMade);
			// controller.caput(pulseWidthChnl, 0.0020);
			changeHasBeenMade = caputTestChangeString(positionTrigChnl, "Enc1", changeHasBeenMade);

			// variable settings
			logger.debug("Time before variable zebra settings");
			double startDeg = radToDeg(startAngle)  * -1.0;
			double stopDeg = radToDeg(endAngle)  * -1.0;
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
		double fractionalChange = Math.abs( Math.abs(current - toPut) / current);
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
				int expectedCounts = (int) Math.round(readbackNumberOfCounts_floored) - 1;
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

				// has mono finished moving after the moving to the run-up position?
				while (isBusy()) {
					logger.info("-----waiting for qscanAxis to finish moving inside perform before starting scanning. after goto runup");
					Thread.sleep(100);
				}
				InterfaceProvider.getTerminalPrinter().print("Mono in position.");
				logger.info("Mono in position.");

				// set the sped (do this now, after the motor has been moved to the run-up position)
				if (desiredSpeed <= getMaxSpeed()) {
					caputTestChangeDouble(currentSpeedChnl, desiredSpeed, null);
				} else {
					logger.info("Continuous motion for " + getName()
							+ " greater than Bragg maximum speed. Speed will be set instead to the maximum speed of "
							+ getMaxSpeed() + " deg/s");
				}

				// prepare zebra to send pulses
				logger.debug("Time before zebra arm with callback");
				InterfaceProvider.getTerminalPrinter().print(
						"Arming Zebra box to trigger detectors during mono move...");
				logger.info("Arming Zebra box to trigger detectors during mono move...");
				controller.caputWait(armChnl, 1);

				// These will be used when calculating the real energy of each step in the scan, so readback once at
				// this point.
				logger.debug("Time before zebra readbacks");
				startReadback_deg = controller.cagetDouble(startReadback_deg_Chnl);
				stepSize_counts = controller.cagetDouble(stepSizeReadback_counts_Chnl);
				width_deg = controller.cagetDouble(widthReadback_deg_Chnl);
				width_counts = controller.cagetDouble(widthReadback_counts_Chnl);

				// do the move asynchronously to this thread
				InterfaceProvider.getTerminalPrinter().print("Mono move started.");
				logger.info("Mono move started.");
				super.asynchronousMoveTo(angleToEV(endAngle));
			} catch (Exception e) {
				throw new DeviceException(e.getMessage(), e);
			}
		}
		long timeAtMethodEnd = System.currentTimeMillis();
		logger.debug("Time spent in performContinuousMove = " + (timeAtMethodEnd - timeAtMethodStart) + "ms");
	}

	private double getMaxSpeed() {
		// TODO check units
		if (maxSpeed == null) {
			try {
				maxSpeed = controller.cagetDouble(maxSpeedChnl);
			} catch (Exception e) {
				logger.warn("Exception while getting Bragg motor max speed. Defaulting to 0.0674934", e);
				maxSpeed = 1.0; // 1 keV/s
			}
		}
		return maxSpeed;
	}

	@Override
	public void continuousMoveComplete() throws DeviceException {
		try {
			// return to regular running values
			controller.caputWait(currentSpeedChnl, getMaxSpeed());
			controller.caputWait(disarmChnl, 1);
		} catch (Exception e) {
			logger.error("Exception while disarming the Zebra. But GDA will continue. This may cause an error later.",
					e);
		}
	}

	@Override
	public double calculateEnergy(int frameIndex) throws DeviceException {
		try {
			return calculateFrameEnergyFromZebraReadback(frameIndex);
		} catch (Exception e) {
			throw new DeviceException("Exception wile calculating frame energy", e);
		}
	}

	private double calculateFrameEnergyFromZebraReadback(int frameIndex) {
		double countsPerDegree = width_deg / width_counts;

		double frameCentre_offset_cts = ((stepSize_counts * frameIndex) + (0.5 * stepSize_counts));
		// TODO change sign based on direction and resolution
		double frameCentre_deg = startReadback_deg + (frameCentre_offset_cts * countsPerDegree);
		if (startAngle.isLessThan(endAngle)) {
			frameCentre_deg = startReadback_deg - (frameCentre_offset_cts * countsPerDegree);
		}
		frameCentre_deg *= -1.0; // I18 bragg is in negative values
		Angle frameCentre_angle = (Angle) QuantityFactory.createFromObject(frameCentre_deg, NonSI.DEGREE_ANGLE);
		double frameCentre_eV = angleToEV(frameCentre_angle);
		return frameCentre_eV;
	}

	private double radToDeg(Angle angle) {
		return QuantityFactory.createFromObject(angle, NonSI.DEGREE_ANGLE).getAmount();
	}

	private double angleToEV(Angle angle) {
		return QuantityFactory.createFromObject(PhotonEnergy.photonEnergyOf(angle, getTwoD()), NonSI.ELECTRON_VOLT)
				.getAmount();
	}

	/**
	 * @return 2*lattice spacing for the given Bragg crystal cut in use.
	 */
	private Length getTwoD() {
		// fixed at 111
		return Quantity.valueOf(0.62711, SI.NANO(SI.METER));
	}

	@Override
	public void initializationCompleted() throws InterruptedException, DeviceException, TimeoutException, CAException {
		channelsConfigured = true;
	}

	@Override
	public void setContinuousParameters(ContinuousParameters parameters) {
		continuousParameters = parameters;
	}

	@Override
	public ContinuousParameters getContinuousParameters() {
		return continuousParameters;
	}
}
