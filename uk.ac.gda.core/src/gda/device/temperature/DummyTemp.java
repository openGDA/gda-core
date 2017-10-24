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

import java.text.NumberFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.Temperature;
import gda.device.TemperatureRamp;
import gda.device.TemperatureStatus;
import gda.factory.FactoryException;
import gda.util.PollerEvent;

/**
 * Simulator device for temperature controllers
 */
public class DummyTemp extends TemperatureBase implements Runnable, Temperature {

	private static final Logger logger = LoggerFactory.getLogger(DummyTemp.class);

	private final static double MAXTEMP = 200.0;

	private final static double MINTEMP = -35.0;

	private static final int IDLE = 0;

	private static final int RAMPING = 2;

	private static final int HOLDING = 3;

	private int rampState = IDLE;

	private double defaultRate = 10;

	private double startTime = 0;

	private Thread runner = null;
	private boolean lastPoint = false;
	/**
	 * Constructor for dummy temperature class
	 */
	public DummyTemp() {
		lowerTemp = MINTEMP;
		upperTemp = MAXTEMP;
	}

	@Override
	public void configure() throws FactoryException {
		super.configure();
		if (runner == null) {
			runner = uk.ac.gda.util.ThreadManager.getThread(this);
			runner.setName(getClass().getName()+":"+getName());
			runner.start();
		}
		startPoller();
		configured = true;
	}

	@Override
	public void reconfigure() throws FactoryException {
		if (!configured) {
			logger.debug("Reconfiguring DummyTemp " + getName());
			configure();
		}
	}

	@Override
	public void close() {
		logger.debug("Dummy temperature " + getName() + " closed");
		configured = false;
		stopPoller();
		poller = null;
	}

	@Override
	public void setHWLowerTemp(double lowLimit) throws DeviceException {
		if (lowLimit < lowerTemp)
			throw new DeviceException("Invalid lower temperature limit");
	}

	/**
	 * Set the upper operating temperature. It must be in the range -35C and 200C when simulating a Lauda bath.
	 *
	 * @param upperLimit
	 *            the upper temperature limit in degrees C
	 * @throws DeviceException
	 */
	@Override
	public void setHWUpperTemp(double upperLimit) throws DeviceException {
		if (upperLimit < lowerTemp || upperLimit > upperTemp)
			throw new DeviceException("Invalid upper temperature limit");
	}

	/**
	 * Switch control variable another source.
	 *
	 * @param name
	 *            the probe name
	 * @throws DeviceException
	 */
	@Override
	public void setProbe(String name) throws DeviceException {
		logger.debug("Setting probe source to " + name);
	}

	/**
	 * Set the proportional band control paramter Xp for the controller.
	 *
	 * @param temp
	 *            the Xp (degrees)
	 */
	public void setXpControl(@SuppressWarnings("unused") double temp) {
	}

	/**
	 * Set the control parameter Tn for the controller.
	 *
	 * @param time
	 *            the Time (secs)
	 */
	public void setTnControl(@SuppressWarnings("unused") double time) {
	}

	/**
	 * Set the control paramter Tv for the controller
	 *
	 * @param time
	 *            the Tv parameter(secs)
	 */
	public void setTvControl(@SuppressWarnings("unused") double time) {
	}

	/**
	 * Program a timed temperature ramp. This does not initiate the program. Unrealistically short times will generate
	 * an error and the parameters will not be set.
	 *
	 * @param finalTemp
	 *            is the desired temperature in degrees C acceptable values are between -35C and 200C.
	 * @param rate
	 *            rise in temperature per minute
	 * @throws DeviceException
	 */
	public void setTempRamp(double finalTemp, double rate) throws DeviceException {
		if (finalTemp > upperTemp || finalTemp < lowerTemp)
			throw new DeviceException("Invalid ramp final temperature");

		double dtime = (finalTemp - currentTemp) / rate;
		int time = (int) dtime;
		int hours = time / 60;
		int minutes = time - (hours * 60);

		if ((hours < 0 || hours > 99) || (minutes < 0 || minutes > 59))
			throw new DeviceException("Invalid ramp time");

		// targetTemperature = finalTemp;
		// rampRate = rate;
	}

	/**
	 * Initiate the programmed temperature ramp.
	 *
	 * @param which
	 *            the current ramp
	 * @throws DeviceException
	 */
	public synchronized void runRamp(int which) throws DeviceException {
		if (busy || rampState != IDLE)
			throw new DeviceException("Already busy");

		currentRamp = which;
		setTargetTemperature(rampList.get(currentRamp).getEndTemperature());
		rampState = RAMPING;
		logger.debug("rampState changed to RAMPING " + currentRamp);
		busy = true;
		notify();
	}

	@Override
	public synchronized void runRamp() throws DeviceException {
		runRamp(0);
	}

	@Override
	public void doStart() throws DeviceException {
		Date d = new Date();
		startTime = d.getTime();
		runRamps();
	}

	@Override
	public void hold() throws DeviceException {
	}

	/**
	 * Start ramping.
	 *
	 * @throws DeviceException
	 */
	public synchronized void runRamps() throws DeviceException {
		// doAllRamps = true;
		if (dataFileWriter != null) {
			dataFileWriter.open();
		}
		runRamp(0);
	}

	@Override
	protected void startNextRamp() throws DeviceException {
		currentRamp++;
		logger.debug("startNextRamp called currentRamp now " + currentRamp);
		if (currentRamp < rampList.size()) {
			sendRamp(currentRamp);
			runRamp(currentRamp);
		} else {
			stop();
		}
	}

	@Override
	protected void doStop() throws DeviceException {
		// doAllRamps = false;
		if (dataFileWriter != null) {
			dataFileWriter.close();
		}
		currentRamp = -1;
		lastPoint = true;
		rampState = IDLE;
		busy = false;
		logger.debug("rampState changed to IDLE");
	}

	/**
	 * Read the temperature of the Lauda bath.
	 *
	 * @return the Lauda water bath temperature
	 * @throws DeviceException
	 * @throws NumberFormatException
	 */
	@Override
	public double getCurrentTemperature() throws DeviceException, NumberFormatException {
		return currentTemp;
	}

	/**
	 * Read the temperature of the controlling device either internal bath external probe1 or external probe2.
	 *
	 * @return the control temperature
	 * @throws NumberFormatException
	 */
	public double getBathTemp() throws NumberFormatException {
		return 0.0;
	}

	/**
	 * Read the temperature of the external probe 1. FIXME: Lauda specific ?
	 *
	 * @return the external probe 1 temperature

	 */
	public double getExtProbe1() {
		return 0.0;
	}

	/**
	 * Get the current setpoint temperature.
	 *
	 * @return the setpoint temperature
	 * @throws DeviceException
	 */
	@Override
	public double getTargetTemperature() throws DeviceException {
		return 0.0;
	}

	/**
	 * Get the status signal
	 *
	 * @return the status
	 */
	public String getStatusSignal()  {
		return "status";
	}

	/**
	 * Read the temperature of the external probe 2.
	 *
	 * @return the external probe 2 temperature
	 */
	public double getExtProbe2() {
		return 0.0;
	}

	/**
	 * Only to fullfill interface
	 *
	 * @return 0.0
	 */
	public double getXp(){
		return 0.0;
	}

	/**
	 * Only to fullfill interface
	 *
	 * @return 0.0
	 */
	public double getTn() {
		return 0.0;
	}

	/**
	 * Only to fullfill interface
	 *
	 * @return 0.0
	 */
	public double getTv(){
		return 0.0;
	}

	/**
	 * The run method implementing the Runnable interface Uses the runner thread to poll the temperature
	 */
	@Override
	public synchronized void run() {
		if (runner == null)
			logger.debug("Thread created for temperature monitor is null");

		while (true) {
			try {
				if (busy) {
					double change;
					if (rampState == RAMPING) {
						change = rampList.get(currentRamp).getRate() * (polltime + 100) / 60000;
					} else {
						change = defaultRate * polltime / 10000.0;
						if (change > getAccuracy() && Math.abs(currentTemp - targetTemp) <= change)
							change = Math.abs(currentTemp - targetTemp);
					}

					if (currentTemp > targetTemp)
						change = -change;

					currentTemp = currentTemp + change;

					logger.debug("DummyTemp has changed currentTemp to " + currentTemp);
					wait(polltime);
				}

				else {
					if (rampState == RAMPING) {
						rampState = HOLDING;
						logger.debug("rampState changed to HOLDING");
					}
					if (rampState == IDLE) {
						wait();
						logger.debug("DummyTemp run thread woken up currentRamp is " + currentRamp);
					}

				}
				wait(100);
			} catch (InterruptedException e1) {
				// deliberately do nothing
			}
		}
	}

	/*
	 * never used private boolean lowLevel() throws DeviceException { return false; }
	 */

	@Override
	public boolean isAtTargetTemperature() {
		return Math.abs(currentTemp - targetTemp) <= getAccuracy();
	}

	@Override
	public void clearRamps() {
		logger.debug("DummyTemp clearRamps() called\n");
		rampList.clear();
	}

	@Override
	public void addRamp(TemperatureRamp ramp) {
		logger.debug("DummyTemp addRamp() called with " + ramp);
		rampList.add(ramp);
	}

	// Implementation of methods which are abstract in the TemperatureBase
	// class

	/**
	 * Actually sends the command which starts the device working towards the target temperature.
	 */
	@Override
	public void startTowardsTarget() {
		busy = true;
		notify();
		Date d = new Date();
		startTime = d.getTime();
		logger.debug("DummyTemp startTowardsTarget called ");
	}

	/**
	 * Method for the poller (started in TemperatureBase)
	 *
	 * @param pe
	 *            the polling event
	 */
	@Override
	public void pollDone(PollerEvent pe) {
		logger.trace("DummyTemp.pollDone() currentTemp is {}", currentTemp);

		String stateString = null;
		String dataString = null;

		NumberFormat n = NumberFormat.getInstance();
		n.setMaximumFractionDigits(2);
		n.setGroupingUsed(false);

		try {
			currentTemp = getCurrentTemperature();
			if (busy && isAtTargetTemperature()) {
				busy = false;
				rampState = IDLE;
				startHoldTimer();
			}
			if (busy)
				stateString = (targetTemp > currentTemp) ? "Heating" : "Cooling";
			else if (currentRamp > -1)
				stateString = "At temperature";
			else
				stateString = "Idle";
		} catch (DeviceException de) {
			logger.error("Error handling pollerEvent in pollDone", de);
		}

		if (timeSinceStart >= 0.0 || lastPoint) {
			Date d = new Date();
			timeSinceStart = d.getTime() - startTime;
		}
		dataString = "" + n.format(timeSinceStart / 1000.0) + " " + currentTemp;
// use this to test DSC data
//		dataString = "" + n.format(timeSinceStart / 1000.0) + " " + currentTemp + " " + (currentTemp+1);

		TemperatureStatus ts = new TemperatureStatus(currentTemp, currentRamp, stateString, dataString);

		if (dataFileWriter != null) {
			dataFileWriter.write(dataString);
		}

		double data[] = new double[2];
		data[0] = currentTemp;
		data[1] = timeSinceStart / 1000.0;
		bufferedData.add(data);

		if (isBeingObserved()) {
			notifyIObservers(this, ts);
		}
		if (lastPoint) {
			lastPoint = false;
			timeSinceStart = -1000;
		}
	}

	/**
	 * Would send the commands to set up a ramp to the actual hardware.
	 *
	 * @param which
	 *            the ramp number
	 */
	@Override
	public void sendRamp(int which) {
		// FIXME: write this method
		logger.debug("DummyTemp.sendRamp() does not work yet");
	}


	public String getDataFileName() {
		return (dataFileWriter != null) ? dataFileWriter.getDataFileName() : null;
	}

	@Override
	public Object getAttribute(String name) {
		if (name.equalsIgnoreCase("DataFilename")) {
			return getDataFileName();
		} else if (name.equalsIgnoreCase("NeedsCooler")) {
			return false;
		} else if (name.equalsIgnoreCase("NeedsCoolerSpeedSetting")) {
			return false;
		} else if (name.equalsIgnoreCase("isDSC")) {
			return false;
		} else {
			return null;
		}
	}

	@Override
	public Object readout() {
		int dims[] = getDataDimensions();
		double data[][] = new double[dims[1]][dims[0]];
		for (int i=0; i<dims[1]; i++)
			data[i] = bufferedData.get(i);
		return data;
	}
}
