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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.factory.FactoryException;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;

/**
 * The Zebra-specific parts of the Qexafs movement
 */
public class ZebraQexafsScannable extends QexafsScannable {

	private static final Logger logger = LoggerFactory.getLogger(ZebraQexafsScannable.class);

	private String armTrigSourcePV = "BL18B-OP-DCM-01:ZEBRA:PC_ARM_SEL";
	private String armPV = "BL18B-OP-DCM-01:ZEBRA:PC_ARM";

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

	private Channel armTrigSourceChnl;
	private Channel armChnl;
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

	@Override
	public void configure() throws FactoryException {
		super.configure();

		try {
			armTrigSourceChnl = channelManager.createChannel(armTrigSourcePV, false);
			armChnl = channelManager.createChannel(armPV, false);
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

		} catch (CAException e) {
			throw new FactoryException("CAException while creating channels for " + getName(), e);
		}
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
			double runupEnergy = angleToEV(runupPosition);
			if (!runUpOn) {
				runupEnergy = angleToEV(startAngle);
			}
			checkDeadbandAndMove(runupEnergy);
			logger.debug("Time spent after moved to angle");
			
			// TODO tidy up and move Strings to constants 

			// fixed settings
			controller.caputWait(armTrigSourceChnl, "Soft");
			controller.caputWait(gateTrigSourceChnl, "Position");
			controller.caputWait(numGatesChnl, 1);
			controller.caputWait(pulseTrigSourceChnl, "Position");
			controller.caputWait(pulseStartChnl, 0.0);
			controller.caputWait(pulseWidthChnl, 0.0020);
			controller.caputWait(positionTrigChnl, "Position");

			// variable settings
			Double startDeg = radToDeg(startAngle);
			Double stopDeg = radToDeg(endAngle);
			Double stepDeg = radToDeg(stepSize);
			String positionDirection = stopDeg > startDeg ? "Positive" : "Negative";
			controller.caputWait(gateStartChnl, startDeg);
			controller.caputWait(gateWidthChnl, Math.abs(stopDeg - stepDeg));
			controller.caputWait(pulseStepChnl, stepDeg);
			controller.caputWait(positionDirectionChnl, positionDirection);

			long timeAtMethodEnd = System.currentTimeMillis();
			logger.debug("Time spent in prepareForContinuousMove = " + (timeAtMethodEnd - timeAtMethodStart) + "ms");
		} catch (DeviceException e) {
			throw e;
		} catch (Exception e) {
			throw new DeviceException(getName() + " exception in prepareForContinuousMove", e);
		}
	}

	@Override
	public int getNumberOfDataPoints() {
		// with Zebra, we will get the expected number of points
		return continuousParameters.getNumberDataPoints();
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

				// prepare zebra to send pulses
				controller.caputWait(armChnl, 1);

				// do the move asynchronously to this thread
				if (runDownOn) {
					super.asynchronousMoveTo(angleToEV(runDownPosition));
				}
				else {
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
		long timeAtMethodStart = System.currentTimeMillis();
		// return to regular running values
		resetDCMSpeed();
		long timeAtMethodEnd = System.currentTimeMillis();
		logger.debug("Time spent in continuousMoveComplete = " + (timeAtMethodEnd - timeAtMethodStart) + "ms");
	}

	@Override
	public double calculateEnergy(int frameIndex) throws DeviceException {
		Double startDeg = radToDeg(startAngle);
		if (endAngle.getAmount() < startAngle.getAmount()) {
			startDeg = radToDeg(endAngle);
		}
		Double stepDeg = radToDeg(stepSize);
		return startDeg + (frameIndex * stepDeg) + (0.5 * stepDeg);
	}

}
