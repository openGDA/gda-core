/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package gda.device.continuouscontroller;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.scannable.ScannableBase;
import gda.device.scannable.ScannableUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class CVScannable extends ScannableBase implements ConstantVelocityMoveController{
	private static final Logger logger = LoggerFactory.getLogger(CVScannable.class);
	
	private double triggerPeriod;
	private double end;
	private double step;
	private double start;
	Scannable scannableBeingMoved;
	
	
	public Scannable getScannableBeingMoved() {
		return scannableBeingMoved;
	}

	public void setScannableBeingMoved(Scannable scannableBeingMoved) {
		this.scannableBeingMoved = scannableBeingMoved;
	}

	@Override
	public void setTriggerPeriod(double seconds) throws DeviceException {
		logger.info("setTriggerPeriod:"+seconds);
		triggerPeriod = seconds; //readout need to use readout time;

	}

	@Override
	public int getNumberTriggers() {
		logger.info("getNumberTriggers");
		try {
			return ScannableUtils.getNumberSteps(scannableBeingMoved, new Double(start),new Double(end),new Double(step))+1;
		} catch (Exception e) {
			logger.error("Error getting number of triggers", e);
			return 0;
		}
	}

	@Override
	public double getTotalTime() throws DeviceException {
		logger.info("getTotalTime");
		return (getNumberTriggers() == 0) ? 0 : triggerPeriod * (getNumberTriggers() - 1);
	}
	@Override
	public boolean isBusy() throws DeviceException {
		return false;
	}

	@Override
	public void prepareForMove() throws DeviceException, InterruptedException {
		logger.info("prepareForMove");
	}

	@Override
	public void startMove() throws DeviceException {
		logger.info("startMove");
	}

	@Override
	public boolean isMoving() throws DeviceException {
		logger.info("isMoving");
		return false;
	}

	@Override
	public void waitWhileMoving() throws DeviceException, InterruptedException {
		logger.info("waitWhileMoving");
	}

	@Override
	public void stopAndReset() throws DeviceException, InterruptedException {
		logger.info("stopAndReset");
	}

	@Override
	public void setStart(double start) throws DeviceException {
		logger.info("setStart:" + start); 
		this.start = start;
	}

	@Override
	public double getStart() {
		return start;
	}

	@Override
	public void setEnd(double end) throws DeviceException {
		logger.info("setEnd:" + end);
		this.end = end;
	}

	@Override
	public double getEnd() {
		return end;
	}

	@Override
	public void setStep(double step) throws DeviceException {
		logger.info("setStep:"+ step);
		this.step = step;
	}

	@Override
	public double getStep() {
		return step;
	}
}