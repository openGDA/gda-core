/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

import static java.util.concurrent.TimeUnit.SECONDS;

import java.lang.reflect.Array;
import java.text.NumberFormat;
import java.util.Date;

import org.python.core.PySequence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.TemperatureRamp;
import gda.device.TemperatureStatus;
import gda.factory.FactoryException;
import gda.factory.Finder;
import gda.jython.InterfaceProvider;
import gda.jython.JythonServerFacade;
import gda.observable.IObserver;
import uk.ac.diamond.daq.concurrent.Async;

/**
 * 4K temperature control supports Lakeshore 340 device using EpicsLakeshore340Controller.
 */
public class GdaLakeshore340 extends TemperatureBase implements IObserver {

	/**
	 * logging instance
	 */
	private static final Logger logger = LoggerFactory.getLogger(GdaLakeshore340.class);

	protected ILakeshoreController controller;

	public ILakeshoreController getController() {
		return controller;
	}

	public void setController(ILakeshoreController controller) {
		this.controller = controller;
	}

	private String epicsLakeshore340ControllerName;

	private long startTime;

	private String stateString = null;

	/**
	 * Constructor
	 */
	public GdaLakeshore340() {

		setInputNames(new String[] { "Temperature" });
		String[] outputFormat = new String[inputNames.length + extraNames.length];
		outputFormat[0] = "%5.2f";
		setOutputFormat(outputFormat);
	}

	@Override
	public void configure() throws FactoryException {

		if (!isConfigured()) {

			String filePrefix = InterfaceProvider.getPathConstructor().createFromProperty("gda.device.temperature.datadir");
			if ((filePrefix != null) && (fileSuffix != null)) {
				dataFileWriter = new DataFileWriter(filePrefix, fileSuffix);
			}
			if (controller == null)
				setController((EpicsLakeshore340Controller) Finder.find(epicsLakeshore340ControllerName));

			if (controller != null) {
				logger.debug("Controller {} found", controller.getName());
				if (controller.isConfigureAtStartup()) {
					int i = 0;
					while (!controller.isConfigured()) {
						if (i > 10)
							break; // wait for 1 second.
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							Thread.currentThread().interrupt();
						}
						i++;
					}
					if (!controller.isConfigured())
						logger.warn("configure {} at start failed. You may need to reconfigure {} after GDA started.",
								epicsLakeshore340ControllerName, getName());
				} else {
					controller.configure();
				}
			} else {
				// if controller does not exist, unregister this listener as no data source is available
				logger.error("Cryo controller {} not found", epicsLakeshore340ControllerName);
				throw new FactoryException("Cryo controller " + epicsLakeshore340ControllerName + " not found");
			}
			if (controller.isConfigured()  && controller.isConnected()) {
				try {
					currentTemp = getCurrentTemperature();
				} catch (DeviceException e) {
					logger.error("failed to get current temperature from {}. Device may not connected.", getName());
					throw new FactoryException("Failed to get Current Temperature. ",e);
				}
				controller.addIObserver(this);
				startPoller();
				setConfigured(true);
			} else {
				logger.warn("'{}' need to reconfigure '{}' before using.", getName(), getName() + ".reconfigure( )");
			}
		}
	}

	@Override
	public void reconfigure() throws FactoryException {
		if (!isConfigured())
			configure();
	}

	@Override
	public double getCurrentTemperature() throws DeviceException {
		currentTemp = controller.getTemp();
		return currentTemp ;
	}

	/**
	 * Get the target temperature
	 *
	 * @return the target temperature
	 * @throws DeviceException
	 */
	@Override
	public double getTargetTemperature() throws DeviceException {
		return controller.getTargetTemp();
	}

	@Override
	protected void doStart() throws DeviceException {
		Date d = new Date();
		startTime = d.getTime();
		sendStart();
	}

	@Override
	public void hold() throws DeviceException {
		logger.warn("This method is not available for Lakeshore 340");
		throw new IllegalStateException("Device " + getName() +" does have this function.");
	}

	@Override
	public void runRamp() throws DeviceException {
		logger.warn("This method is not available for Lakeshore 340");
		throw new IllegalStateException("Device " + getName() +" does have this function.");
	}

	/**
	 * Program a temperature ramp into hardware, this does not initiate the program, it validates the target temperature
	 * and ramp rate. {@inheritDoc}
	 *
	 * @see gda.device.temperature.TemperatureBase#sendRamp(int)
	 */
	@Override
	protected void sendRamp(int which) throws DeviceException {
		double finalTemp;
		double rate;

		TemperatureRamp ramp = rampList.get(which);

		finalTemp = ramp.getEndTemperature();
		rate = ramp.getRate();

		logger.debug("finalTemp is {}", finalTemp);
		if (finalTemp > upperTemp || finalTemp < lowerTemp) {
			throw new DeviceException("Invalid ramp final temperature");
		}

		logger.debug("Ramp rate in K/hour is {}", rate);
		if (rate >= ILakeshoreController.MIN_RAMP_RATE || rate <= ILakeshoreController.MAX_RAMP_RATE) {
			throw new DeviceException("Invalid ramp rate for temperature");
		}

		controller.setTargetTemp(finalTemp);
		targetTemp = finalTemp;
		setPoint = finalTemp;
	}

	/**
	 * Initiate the programmed temperature ramp in the hardware.
	 *
	 * @throws DeviceException
	 */
	public void sendStart() throws DeviceException {
		if (busy) {
			throw new DeviceException(getName() + " is already ramping to temerature");
		}
		busy = true;
		notify();
	}

	@Override
	protected void startNextRamp() throws DeviceException {
		currentRamp++;
		logger.debug("startNextRamp called currentRamp now {}", currentRamp);
		if (currentRamp < rampList.size()) {
			sendRamp(currentRamp);
			sendStart();
		} else {
			stop();
		}
	}

	@Override
	protected void doStop() throws DeviceException {
		if (controller.getConnectionState().equals("Disabled")) {
			//required to support Panic stop which does not handle Exception throws or do not check if device is enabled before calling stop on it.
			return;
		}
		sendStop();
	}

	/**
	 * on stop temperature ramp it sets to hold.
	 *
	 * @throws DeviceException
	 */
	private void sendStop() throws DeviceException {
		setPoint = getCurrentTemperature();
		controller.setTargetTemp(setPoint);
		if (isAtTargetTemperature()) {
			busy = false;
		}
	}

	/**
	 * starts towards a single target temperature at current ramp rate {@inheritDoc}
	 *
	 * @see gda.device.temperature.TemperatureBase#startTowardsTarget()
	 */
	@Override
	protected void startTowardsTarget() throws DeviceException {
		if (busy)
			throw new DeviceException(getName() + " is already ramping to temerature");
		startPoller();
		controller.setTargetTemp(targetTemp);
		setPoint = targetTemp;
		busy = true;
		notify();
		Async.scheduleAtFixedRate(this::notifyUsersOfPosition, 5, 5, SECONDS);
	}

	/**
	 * Notify users of current position for use during moves
	 */
	private void notifyUsersOfPosition() {
		try {
			if (!isBusy()) {
				throw new IllegalStateException(getName() + " has completed its move");
			}
			JythonServerFacade.getInstance().print(toString() + " " + stateString);
		} catch (DeviceException e) {
			logger.error("Failed to get feedback values from EPICS.", e);
		}
	}

	/**
	 * perform the start/restart procedure of hardware
	 *
	 * @throws DeviceException
	 */
	@Override
	public void begin() throws DeviceException {
		logger.warn("This method is not available for Lakeshore 340");
		throw new IllegalStateException("Device " + getName() +" does have this function.");
	}

	/**
	 * perform the shutdown procedure of the hardware {@inheritDoc}
	 *
	 * @see gda.device.temperature.TemperatureBase#end()
	 */
	@Override
	public void end() throws DeviceException {
		logger.warn("This method is not available for Lakeshore 340");
		throw new IllegalStateException("Device " + getName() +" does have this function.");
	}

	/**
	 * sets the ramp rate
	 *
	 * @param rate
	 * @throws DeviceException
	 */
	@Override
	public void setRampRate(double rate) throws DeviceException {
		logger.warn("This method is not available for Lakeshore 340");
		throw new IllegalStateException("Device " + getName() +" does have this function.");
	}

	/**
	 * gets the ramp rate
	 *
	 * @return rate
	 * @throws DeviceException
	 */
	@Override
	public double getRampRate() throws DeviceException {
		logger.warn("This method is not available for Lakeshore 340. Return default value of 2k/min");
		throw new IllegalStateException("Device " + getName() +" does have this function.");
	}

	/**
	 * Temperature GUI update. {@inheritDoc}
	 */
	@Override
	public void temperatureUpdate() {

		NumberFormat n = NumberFormat.getInstance();
		n.setMaximumFractionDigits(2);
		n.setGroupingUsed(false);

		try {
			if (isAtTargetTemperature()) {
				busy = false;
				startHoldTimer();
			}

			if (busy)
				stateString = (targetTemp > currentTemp) ? "Heating" : "Cooling";
			else if (currentRamp > -1)
				stateString = "At temperature";
			else
				stateString = "Idle";
		} catch (DeviceException de) {
			logger.warn("{}.temperatureUpdate() threw exception on isAtTargetTemperature() call", getName(), de);
		}
		if (timeSinceStart >= 0.0) {
			Date d = new Date();
			timeSinceStart = d.getTime() - startTime;
		}

		TemperatureStatus ts;
		String dataString = n.format(timeSinceStart / 1000.0) + " " + currentTemp;

		ts = new TemperatureStatus(currentTemp, lowerTemp, upperTemp, targetTemp, currentRamp, stateString, dataString);
		notifyIObservers(this, ts);
	}

	/**
	 * @return EpicsLakeshore340ControllerName
	 */
	public String getEpicsLakeshore340ControllerName() {
		return epicsLakeshore340ControllerName;
	}

	/**
	 * @param controllerName
	 */
	public void setEpicsLakeshore340ControllerName(String controllerName) {
		epicsLakeshore340ControllerName = controllerName;
	}

	@Override
	public void update(Object theObserved, Object changeCode) {
		if (theObserved instanceof EpicsLakeshore340Controller.CurrentTempListener) {
			if (changeCode instanceof Double) {
				currentTemp = ((Double) changeCode).doubleValue();
			}
		} else if (theObserved instanceof EpicsLakeshore340Controller.ConnectionListener) {
			if (((String)changeCode).equals("Disabled")) {
				if (isConfigured()) {
					setConfigured(false);
				}
				logger.warn("{} is currently NOT connected to hardware.", getName());
			} else if (((String)changeCode).equals("Enabled")) {
				if (!isConfigured()) {
					try {
						reconfigure();
					} catch (FactoryException e) {
						logger.error("Cannot configure "+getName(), e);
					}
				}
				logger.info("{} is current temperature controller.", getName() );
			}
		}

	}

	@Override
	public void rawAsynchronousMoveTo(Object position) throws DeviceException {
		double temp = Double.parseDouble(position.toString());
		setTargetTemperature(temp);
	}

	@SuppressWarnings("unused")
	private Object getPositionWithStatus() {
		String[] value = new String[8];
		value[0] = String.format(getOutputFormat()[0], currentTemp);
		value[1] = stateString;
		return value;
	}

	@Override
	public Object rawGetPosition() throws DeviceException {
		return currentTemp;
	}

	@Override
	public boolean isBusy() throws DeviceException {
		return busy;
	}

	@Override
	public String toString() {
		try {
			StringBuilder myString = new StringBuilder();
			Object position = this.getPosition();

			if (position == null) {
				logger.warn("getPosition() from {} returns NULL.", getName());
				return valueUnavailableString();
			}
			// print out simple version if only one inputName and
			// getPosition and getReportingUnits do not return arrays.
			myString.append(this.getName());
			myString.append(" : ");
			if (!(position.getClass().isArray() || position instanceof PySequence)) {
				if (position instanceof String) {
					myString.append(position.toString());
				} else {
					myString.append(String.format(outputFormat[0], Double.parseDouble(position.toString())));
				}
			} else {
				if (position instanceof PySequence) {
					for (int i = 0; i < ((PySequence) position).__len__(); i++) {
						if (i > 0) {
							myString.append(" ");
						}
						myString.append(String.format(outputFormat[0], Double.parseDouble(((PySequence) position)
								.__finditem__(i).toString())));
					}
				} else {
					for (int i = 0; i < Array.getLength(position); i++) {
						if (i > 0) {
							myString.append(" ");
						}
						myString.append(String.format(outputFormat[0], Double
								.parseDouble(Array.get(position, i).toString())));
					}
				}

			}
			return myString.toString();
		} catch (Exception e) {
			logger.warn("{}: exception while getting value", getName(), e);
			return valueUnavailableString();
		}
	}

	@Override
	protected void setHWLowerTemp(double lowerTemp) throws DeviceException {
		// N/A, This is set by XML configuration to setLowerTemp(lowlimit).
	}

	@Override
	protected void setHWUpperTemp(double upperTemp) throws DeviceException {
		// N/A, This is set by XML configuration to setUpperTemp(highlimit).
	}
}
