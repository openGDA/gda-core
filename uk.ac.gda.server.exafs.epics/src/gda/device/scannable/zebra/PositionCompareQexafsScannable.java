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

import gda.device.DeviceException;
import gda.factory.FactoryException;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;

import org.jscience.physics.quantities.Constants;
import org.jscience.physics.quantities.Length;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Under development for B18.
 * <p>
 * Proposed replacement for QexafsScannable where calculations are separated from Epics control to split position
 * compare and zebra code from common code.
 */
public class PositionCompareQexafsScannable extends QexafsScannable {

	private static final Logger logger = LoggerFactory.getLogger(PositionCompareQexafsScannable.class);

	private String outputModePV; // 0 = OFF, 1 = ON, 2 = AUTO
	private String startPV; // in degrees
	private String stopPV; // in degrees
	private String stepPV; // in degrees
	private String numPulsesPV; // the number of pulses that will be sent out, after start,stop,step set
	private String stepIncDemandPV;

	private Channel outputModeChnl;
	private Channel startChnl;
	private Channel stopChnl;
	private Channel stepChnl;
	private Channel numPulsesChnl;

	@Override
	public void configure() throws FactoryException {
		super.configure();
		try {
			outputModeChnl = channelManager.createChannel(outputModePV, false);
			startChnl = channelManager.createChannel(startPV, false);
			stopChnl = channelManager.createChannel(stopPV, false);
			stepChnl = channelManager.createChannel(stepPV, false);
			numPulsesChnl = channelManager.createChannel(numPulsesPV, false);

			channelManager.creationPhaseCompleted();
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
			double runupEnergy = angleToEV(runupPosition);
			if (!runUpOn) {
				runupEnergy = angleToEV(startAngle);
			}
			checkDeadbandAndMove(runupEnergy);
			logger.debug("Time spent after moved to angle");

				controller.caputWait(startChnl, startDeg);
				controller.caputWait(stopChnl, stopDeg);
				controller.caputWait(stepChnl, Math.abs(stepDeg));

			// why sleep half a second? for epics to calculate the number of pulses
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				throw e;
			}
			logger.debug("Time spent after 100ms sleep");
			logger.debug("Time spent after get no. pulses");
			long timeAtMethodEnd = System.currentTimeMillis();
			logger.debug("Time spent in prepareForContinuousMove = " + (timeAtMethodEnd - timeAtMethodStart) + "ms");

		} catch (DeviceException e) {
			throw e;
		} catch (Exception e) {
			throw new DeviceException(getName() + " exception in prepareForContinuousMove", e);
		}
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
	public double calculateEnergy(int frameIndex) throws DeviceException {
		try {
			double continuousCountSteps;
				if (endAngle.getAmount() > startAngle.getAmount())
					continuousCountSteps = -stepIncDemDeg;// back
				else
					continuousCountSteps = stepIncDemDeg;// forth
			// all angles below in units of Radians.
			// add 1.5 to the frame index as the B18 Position Compare does not send the first pulse so the first frame
			// is
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

	public String getNumPulsesPV() {
		return numPulsesPV;
	}

	public void setNumPulsesPV(String numPulsesPV) {
		this.numPulsesPV = numPulsesPV;
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
}
