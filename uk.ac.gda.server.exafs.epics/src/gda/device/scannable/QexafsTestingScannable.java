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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * For testing the QEXAFS on B18 with the signals from the Bragg encoder replaced by a pulse generator.
 * <p>
 * This should be point to the actual Bragg motor like a ScannableMotor object
 * <p>
 * To use this class in QEXAFS scans, run the scan over this object and once the scan has started run the pulse
 * generator. Once the pulse generator has stopped, call the pulseSequenceFinished method in this class to finish off
 * the scan.
 * <p>
 * This otherwise is a simulation of energy so should point to a ScannableMotor object for real movement.
 */
public class QexafsTestingScannable extends ScannableMotor implements ContinuouslyScannable, IQexafsScannableState{

	private static final Logger logger = LoggerFactory.getLogger(QexafsTestingScannable.class);

	private ContinuousParameters continuousParameters;

	private String state = "idle";


	@Override
	public int prepareForContinuousMove() throws DeviceException {
		state = "preparing";
		notifyIObservers(this, state);
		try {
			super.setSpeed(1000);
		} catch (DeviceException e) {
			logger.error("Could not set speed to 1000", e);
		}
		super.moveTo(continuousParameters.getStartPosition()-1);
		return continuousParameters.getNumberDataPoints();
	}
	
	@Override
	public void performContinuousMove() throws DeviceException {
		state = "running";
		notifyIObservers(this, state);
		logger.info(getName() + " - move not passed to real motor - you need to start the pulse generator now. Once the pulse generator has finished, call the pulseSequenceFinished on this object");
		double start = continuousParameters.getStartPosition();
		double end = continuousParameters.getEndPosition();
		double speed = (end-start) / continuousParameters.getTotalTime();
		super.setSpeed(speed);
		super.asynchronousMoveTo(continuousParameters.getEndPosition());
	}
	
	@Override
	public void continuousMoveComplete(){
		state = "idle";
		try {
			super.setSpeed(1000);
		} catch (DeviceException e) {
			logger.error("Could not set speed to 1000", e);
		}
		notifyIObservers(this, state);
		logger.info(getName() + " - continuous move completed - ");
	}

	@Override
	public ContinuousParameters getContinuousParameters() {
		return continuousParameters;
	}

	@Override
	public void setContinuousParameters(ContinuousParameters parameters) {
		continuousParameters = parameters;
	}

	@Override
	public double calculateEnergy(int frameIndex) {
		double start = continuousParameters.getStartPosition();
		double end = continuousParameters.getEndPosition();
		int noPoints = continuousParameters.getNumberDataPoints();
		double step = (end-start)/noPoints;
		return start+(step*frameIndex);
	}

	@Override
	public String getState() {
		return state;
	}
}
