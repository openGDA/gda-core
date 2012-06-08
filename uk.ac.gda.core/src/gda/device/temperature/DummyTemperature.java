/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

package gda.device.temperature;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.Temperature;
import gda.device.TemperatureStatus;
import gda.factory.FactoryException;
import gda.util.PollerEvent;

/**
 * Dummy temperature class designed to simulate the temperature interface and TemperatureBase, without threads, for unit
 * testing.
 */
public class DummyTemperature extends TemperatureBase implements Temperature {
	
	private static final Logger logger = LoggerFactory.getLogger(DummyTemperature.class);

	/**
	 * Constructor
	 */
	public DummyTemperature() {
	}

	@Override
	public void configure() throws FactoryException {
		super.configure();
	}

	@Override
	public double getCurrentTemperature() throws DeviceException {
		logger.debug("getCurrentTemperature: " + currentTemp);
		return currentTemp;
	}

	@Override
	public void setTargetTemperature(double target) throws DeviceException {
		// FIXME - this has a problem with visibility and setting of limits
		if (target > upperTemp || target < lowerTemp) {
			throw new DeviceException("Target temperature outside hard limits");
		}

		if (target > upperTemp || target < lowerTemp) {
			throw new DeviceException("Target temperature outside soft limits");
		}

		if (busy) {
			throw new DeviceException("Water bath already ramping to temerature");
		}

		targetTemp = target;
		// busy = true;
		logger.debug("setTargetTemperature: about to startTowardsTarget: " + targetTemp);
		poller.setPollTime(SHORTPOLLTIME);
		startTowardsTarget();
	}

	@Override
	public boolean isAtTargetTemperature() throws DeviceException {
		logger.debug("isAtTargetTemperature: returning not busy:" + !busy + " and " + "not isRunning: " + !isRunning());
		return !busy && !isRunning();
	}

	@Override
	public void setHWLowerTemp(double lowLimit) throws DeviceException {
		if (lowLimit > upperTemp || lowLimit < lowerTemp || lowLimit > getCurrentTemperature()) {
			throw new DeviceException("Invalid lower temperature limit");
		}
	}

	@Override
	public void setHWUpperTemp(double upperLimit) throws DeviceException {
		if (upperLimit < lowerTemp || upperLimit > upperTemp || upperLimit < getCurrentTemperature()) {
			throw new DeviceException("Invalid upper temperature limit");
		}
	}

	/**
	 * @return upper temperature
	 */
	public double getHWUpperTemp(){
		return upperTemp;
	}

	/**
	 * @return lower temperature
	 */
	public double getHWLowerTemp()  {
		return lowerTemp;
	}

	@Override
	public void hold() throws DeviceException {
		logger.debug("hold and do nothing.");
	}

	@Override
	protected void doStart() throws DeviceException {
		logger.debug("doStart: about to send start.");
		sendStart();
	}

	@Override
	protected void doStop() throws DeviceException {
		// README some do sendStop() that sends busy flag;
		busy = false;

	}

	@Override
	protected void sendRamp(int ramp) throws DeviceException {
		// Purely sends commands to hardware
	}

	@Override
	protected void startNextRamp() throws DeviceException {
		currentRamp++;
		logger.debug("startNextRamp called currentRamp now " + currentRamp);
		if (currentRamp < rampList.size()) {
			sendRamp(currentRamp);
			sendStart();
		} else {
			stop();
		}
	}

	private void sendStart() {
		logger.debug("sendStart: setting busy to true.");
		busy = true;
		try {
			Thread.sleep(1000);
		} catch (InterruptedException ie) {
		}
		// README - Poller would be responsible for this normally.
		logger.debug("sendStart: setting busy to false.");

		busy = false;
	}

	@Override
	protected void startTowardsTarget() throws DeviceException {
		// FIXME check if busy is set elsewhere
		logger.debug("startTowardsTarget: setting busy to true.");
		busy = true;
		currentTemp = targetTemp;
		logger.debug("startTowardsTarget: currentTemp = " + currentTemp);
		try {
			Thread.sleep(250);
		} catch (InterruptedException ie) {
		}
		TemperatureStatus ts = new TemperatureStatus(currentTemp);
		logger.debug("DummyTemp notifyIObservers with " + ts.toString());
		notifyIObservers(this, ts);
		logger.debug("startTowardsTarget: setting busy to false.");
		busy = false;
	}

	@Override
	public void pollDone(PollerEvent pe) {
		logger.debug("pollDone: nothing happening.");
		// Deliberately do nothing
	}

	@Override
	public void runRamp() throws DeviceException {
		// TODO Auto-generated method stub

	}
}