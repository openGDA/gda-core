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

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.data.scan.datawriter.DataWriter;
import gda.data.scan.datawriter.DefaultDataWriterFactory;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.Temperature;
import gda.device.TemperatureRamp;
import gda.device.scannable.ScannableMotionBase;
import gda.factory.FactoryException;
import gda.util.Alarm;
import gda.util.AlarmListener;
import uk.ac.diamond.daq.concurrent.Async;

/**
 * A base implementation for all temperature controllers. Handles the basic parts of all operations wherever possible.
 * The actual commands are sent to hardware by the template methods which are implemented by the sub-classes.
 */
public abstract class TemperatureBase extends ScannableMotionBase implements AlarmListener, Temperature {

	private static final Logger logger = LoggerFactory.getLogger(TemperatureBase.class);
	protected static final long LONG_POLL_TIME = 5000;
	protected static final long SHORT_POLL_TIME = 1000;
	protected static final long POLL_TIME = 100;
	protected long pollTime = LONG_POLL_TIME;
	protected long longPollTime = LONG_POLL_TIME;
	protected double timeSinceStart = -1000.0;
	protected double lowerTemp = -35.0;
	protected double upperTemp = 200.0;
	private Alarm holdTimeAlarm = null;
	protected double targetTemp = 0.0;
	protected volatile double currentTemp = 0.0;
	protected double setPoint;
	protected List<TemperatureRamp> rampList = new ArrayList<>();
	private boolean running = false;
	protected volatile boolean busy = false;
	protected int currentRamp = -1;
	protected List<String> probeNameList = new ArrayList<>();
	private double accuracy = 0.1;
	protected int count = 0;
	protected DataFileWriter dataFileWriter = null;
	protected DataWriter dataWriter = null;
	protected ArrayList<double[]> bufferedData = new ArrayList<>();
	protected String fileSuffix = null;
	private ScheduledFuture<?> updatingProcess;

	@Override
	public void configure() throws FactoryException{
		if (isConfigured()) {
			return;
		}
		this.setInputNames(new String[] {"temperature"});
		this.setOutputFormat(new String[] {"%5.2f"});

		String filePrefix = LocalProperties.get("gda.device.temperature.datadir");
		if ((filePrefix != null) && (fileSuffix != null)) {
			dataFileWriter = new DataFileWriter(filePrefix, fileSuffix);
		} else {
			try {
				dataWriter = DefaultDataWriterFactory.createDataWriterFromFactory();
			} catch(InterruptedException e) {
				// Restore interrupt status
				Thread.currentThread().interrupt();
				throw new FactoryException(getName() + " Interrupted while creating datawriter", e);
			} catch (Exception e) {
				throw new FactoryException(getName() + " Error creating datawriter", e);
			}
		}
		setConfigured(true);
	}

	/**
	 * Set the poll time for updates.
	 *
	 * @param polltime
	 *            the poll time in msec
	 */
	public void setPolltime(long polltime) {
		this.pollTime = polltime;
	}

	/**
	 * Get the poll time for update (used by Castor)
	 *
	 * @return the poll time in msec
	 */
	public long getPolltime() {
		return pollTime;
	}

	/**
	 * @return Returns the accuracy.
	 */
	public double getAccuracy() {
		return accuracy;
	}

	/**
	 * @param accuracy
	 *            The accuracy to set.
	 */
	public void setAccuracy(double accuracy) {
		this.accuracy = accuracy;
	}

	/**
	 * Adds a ramp to the internal list of ramps
	 *
	 * @param ramp
	 *            the TemperatureRamp to add
	 */
	@Override
	public void addRamp(TemperatureRamp ramp) {
		rampList.add(ramp);
	}

	/**
	 * Called when the holdTimeAlarm goes off. See method startTimer(). This implements the AlarmListener interface.
	 *
	 * @param theAlarm
	 *            the Alarm which has gone off
	 */
	@Override
	public void alarm(Alarm theAlarm) {
		holdTimeAlarm = null;

		logger.debug("{} holdTimer alarm gone off", getName());

		try {
			startNextRamp();
		} catch (DeviceException de) {
			logger.error("Error starting next ramp while handling alarm ({}) in {}", theAlarm, getName(), de);
		}
	}

	/**
	 * Cancel an alarm if set
	 */
	public void cancelAlarm() {
		if (holdTimeAlarm != null) {
			holdTimeAlarm.cancel();
			holdTimeAlarm = null;
		}
	}

	/**
	 * Clears all ramps from the list
	 */
	@Override
	public void clearRamps() {
		logger.debug("{} clearRamps() called", getName());
		rampList.clear();
	}

	/**
	 * starts a poller thread to check and update temperature
	 */
	public void startPoller() {
		logger.info("start a temperature poller thread for '{}' object.", getName());
		updatingProcess = Async.scheduleWithFixedDelay(this::temperatureUpdate, 0, longPollTime, MILLISECONDS, "%s (temp update)", getName());
	}
	/**
	 * interrupt poller thread to stop updating temperature
	 */
	public void stopPoller() {
		logger.info("{} stop the temperature poller thread.", getName());
		updatingProcess.cancel(false);
	}

	protected void setUpdatePeriod(long delay) {
		long remaining = delay;
		if (updatingProcess != null && !updatingProcess.isDone()) {
			remaining = updatingProcess.getDelay(MILLISECONDS);
			updatingProcess.cancel(false);
		}
		updatingProcess = Async.scheduleAtFixedRate(this::temperatureUpdate, remaining, delay, MILLISECONDS, "%s (temp update)", getName());
	}

	/**
	 * Gets the minimum temperature limit
	 *
	 * @return the minimum temperature obtainable
	 * @throws DeviceException
	 */
	@Override
	public double getLowerTemp() throws DeviceException {
		return lowerTemp;
	}

	/**
	 * Set the temperature probe names
	 *
	 * @param probeNames
	 *            the probe name (used by Castor)
	 */
	public void setProbeNames(List<String> probeNames) {
		this.probeNameList = probeNames;
	}

	@Override
	public List<String> getProbeNames() throws DeviceException {
		return probeNameList;
	}

	/**
	 * Get the target temperature
	 *
	 * @return the target temperature
	 * @throws DeviceException
	 */
	@Override
	public double getTargetTemperature() throws DeviceException {
		return targetTemp;
	}

	/**
	 * Gets the maximum temperature limit
	 *
	 * @return the maximum temperature
	 * @throws DeviceException
	 */
	@Override
	public double getUpperTemp() throws DeviceException {
		return upperTemp;
	}

	@Override
	public boolean isAtTargetTemperature() throws DeviceException {
		// Wait for the controller to reach its setPoint.
		// It requires 5 consecutive readings to be +/- accuracy to minimise errors
		// caused be overheat or overcool.
		if (busy) {
			logger.trace("{} isAt TargetTemperature()", getName());
		}
		currentTemp = getCurrentTemperature();
		double diff = setPoint - currentTemp;

		if (Math.abs(diff) <= accuracy && busy)
			count++;
		else
			count = 0;

		return (count >= 5);
	}

	/**
	 * Sets the minimum temperature limit
	 *
	 * @param lowerTemp
	 *            the minimum temperature
	 * @throws DeviceException
	 */
	@Override
	public void setLowerTemp(double lowerTemp) throws DeviceException {
		this.lowerTemp = lowerTemp;
	}

	/**
	 * Add probe names one at a time
	 *
	 * @param probeName
	 *            the probe name
	 * @throws DeviceException
	 */
	@Override
	public void setProbe(String probeName) throws DeviceException {
		this.probeNameList.add(probeName);
	}

	/**
	 * Sets the maximum temperature limit obtainable
	 *
	 * @param upperTemp
	 *            the maximum temperature
	 * @throws DeviceException
	 */
	@Override
	public void setUpperTemp(double upperTemp) throws DeviceException {
		this.upperTemp = upperTemp;
	}

	@Override
	public void rawAsynchronousMoveTo(Object position) throws DeviceException {
		// FIXME needs implementing
	}

	@Override
	public Object rawGetPosition() throws DeviceException {
		return this.getCurrentTemperature();
	}

	@Override
	public boolean isBusy() throws DeviceException {
		return !isAtTargetTemperature();
	}

	/**
	 * Template method for starting a move towards a targetTemperature. Subclasses must provide method
	 * startTowardsTarget which will send the necessary commands.
	 *
	 * @param targetTemp
	 *            the target temperature
	 * @throws DeviceException
	 */
	@Override
	public synchronized void setTargetTemperature(double targetTemp) throws DeviceException {
		if (targetTemp > upperTemp || targetTemp < lowerTemp)
			throw new DeviceException("Target temperature outside limits: Lower Limit=" + lowerTemp + "; Upper Limit="
					+ upperTemp);

		if (busy) {
			double lastTarget=getTargetTemperature();
			throw new DeviceException(getName() + " already ramping to temerature " + lastTarget+". \nYou need to stop it first before issuing an new tartget temperature.");
		}

		this.targetTemp = targetTemp;
		logger.debug("{} setTargetTemperature targetTemp {}", getName(), targetTemp);
		setUpdatePeriod(SHORT_POLL_TIME);
		startTowardsTarget();
	}

	/**
	 * Sets the array of ramps.
	 *
	 * @param newRamps
	 *            an ArrayList<TemperatureRamp> of ramps to be set
	 */
	@Override
	public void setRamps(List<TemperatureRamp> newRamps) {
		if (!running) {
			rampList = newRamps;
		}
		// If already running then only ramps beyond the current one
		// are changed. May need to further enhance this later if
		// requested - e.g. to change dwell time of currentRamp
		else {
			for (int i = currentRamp + 1; i < newRamps.size(); i++)
				rampList.set(i, newRamps.get(i));
		}
	}

	/**
	 * Starts ramping (compare setTargetTemperature which starts towards a single target temperature).
	 */
	@Override
	public void start() {
		if (!rampList.isEmpty()) {
			try {
				running = true;
				currentRamp = 0;
				timeSinceStart = 0.0;
				bufferedData.clear();
				sendRamp(currentRamp);
				doStart();
				setUpdatePeriod(pollTime);
			} catch (DeviceException de) {
				logger.error("Error starting {}", getName(), de);
				running = false;
			}
		} else {
			throw new IllegalStateException("Temperature ramp list is empty.");
		}
	}

	/**
	 *
	 */
	public void startHoldTimer() {
		double holdTime;
		if (currentRamp != -1 && !rampList.isEmpty()) {
			holdTime = rampList.get(currentRamp).getDwellTime();
			logger.debug("{} Hold timer starting for {}", getName(), holdTime);
			// If holdTime is 0.0 should hold forever so do not set alarm
			if (holdTime > 0.0)
				holdTimeAlarm = new Alarm((long) (holdTime * 60000.0), this);
			else {
				// We do not want any hold time so just start the next ramp.
				try {
					startNextRamp();
				} catch (DeviceException de) {
					logger.error("Error starting next ramp while starting hold timer in {}", getName(), de);
				}
			}
		}
	}

	/**
	 * Stops whatever is happening. Applications should call this method and not the subclass implementations of doStop.
	 */
	@Override
	public void stop() {
		cancelAlarm();
		try {
			running = false;
			doStop();
			setUpdatePeriod(LONG_POLL_TIME);
			currentRamp = -1;
			// reset time as this stops the graph plotting. see bug #377
			timeSinceStart = -1000;
		} catch (DeviceException de) {
			logger.error("Error stopping {}", getName(), de);
		}
	}

	/**
	 * perform the shutdown procedure of the temperature controller, i.e. properly running hardware warm-up or cool-down
	 * procedures {@inheritDoc}
	 *
	 * @throws DeviceException
	 * @see gda.device.Temperature#end()
	 */
	@Override
	public void end() throws DeviceException {
		// added in for Oxford Cryostream 700
		logger.info("end() is not available for {}", getName());
	}

	/**
	 * perform the start/restart up procedure of the temperature controller properly running hardware warm-up or
	 * cool-down procedures {@inheritDoc}
	 *
	 * @throws DeviceException
	 * @see gda.device.Temperature#end()
	 */
	@Override
	public void begin() throws DeviceException {
		// added in for Oxford Cryostream 700
		logger.info("begin() is not available for {}", getName());
	}

	/**
	 * Waits for the device to reach its target temperature
	 *
	 * @throws DeviceException
	 */
	@Override
	public synchronized void waitForTemp() throws DeviceException {
		while (!isAtTargetTemperature()) {
			try {
				wait(POLL_TIME);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new DeviceException("Interrupted waiting in waitForTemp()");
			}
		}
	}

	@Override
	public boolean isRunning() throws DeviceException {
		return running;
	}

	// Template methods to be implemented by the sub-classes

	protected abstract void temperatureUpdate();
	// Should send hardware commands which will start ramping
	protected abstract void doStart() throws DeviceException;

	// Should send hardware commands which will stop
	protected abstract void doStop() throws DeviceException;

	// Should send the hardware commands to set up a ramp
	protected abstract void sendRamp(int ramp) throws DeviceException;

	// Should start the next ramp
	protected abstract void startNextRamp() throws DeviceException;

	/**
	 * Should send hardware commands to start heating or cooling towards a single targetTemp
	 *
	 * @throws DeviceException
	 */
	protected abstract void startTowardsTarget() throws DeviceException;

	/**
	 * @param lowerTemp
	 * @throws DeviceException
	 */
	protected abstract void setHWLowerTemp(double lowerTemp) throws DeviceException;

	/**
	 * @param upperTemp
	 * @throws DeviceException
	 */
	protected abstract void setHWUpperTemp(double upperTemp) throws DeviceException;

	/**
	 * Run the ramp sequence.
	 *
	 * @throws DeviceException
	 */
	public abstract void runRamp() throws DeviceException;

	/**
	 * Get the output file suffix.
	 *
	 * @return the file suffix (used by Castor)
	 */
	public String getFileSuffix() {
		return fileSuffix;
	}

	/**
	 * Set the output file suffix.
	 *
	 * @param fileSuffix
	 *            the file suffix
	 */
	public void setFileSuffix(String fileSuffix) {
		this.fileSuffix = fileSuffix;
	}

	/**
	 * sets the ramp rate
	 *
	 * @param rate
	 * @throws DeviceException
	 */
	@Override
	public void setRampRate(double rate) throws DeviceException {
		logger.warn("Not implemented for device : {}", getName());
	}

	/**
	 * gets the ramp rate
	 *
	 * @return the ramp rate
	 * @throws DeviceException
	 */
	@Override
	public double getRampRate() throws DeviceException {
		logger.warn("Not implemented for device : {}", getName());
		return Double.NaN;

	}

	/**
	 * converts object to String array
	 *
	 * @param position
	 * @param scannable
	 * @return String Array
	 */
	public String[] getCurrentPositionArray(Object position, Scannable scannable) {

		// get object returned by getPosition
		Object currentPositionObj = position;

		// if its null or were expecting it to be null from the arrays, return
		// null
		if (currentPositionObj == null
				|| (scannable.getInputNames().length == 0 && scannable.getExtraNames().length == 0)) {
			return null;
		}

		return (String[]) currentPositionObj;

	}

	public Object readout() { return null; }

	public int[] getDataDimensions() {
		return new int[] {getInputNames().length + getExtraNames().length, bufferedData.size()};
	}
}
