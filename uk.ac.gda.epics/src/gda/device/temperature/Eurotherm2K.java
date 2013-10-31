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

import gda.data.PathConstructor;
import gda.device.DeviceException;
import gda.device.TemperatureRamp;
import gda.device.TemperatureStatus;
import gda.factory.FactoryException;
import gda.factory.Finder;
import gda.jython.JythonServerFacade;
import gda.observable.IObserver;
import gda.util.Poller;
import gda.util.PollerEvent;

import java.lang.reflect.Array;
import java.text.NumberFormat;
import java.util.Date;

import org.python.core.PySequence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that supports Eurotherm 2000 series temperature controller.
 */
public class Eurotherm2K extends TemperatureBase implements IObserver {

	/**
	 * logging instance
	 */
	private static final Logger logger = LoggerFactory.getLogger(Eurotherm2K.class);
	private final double ROOM_TEMPERATURE = 22.0; // C
	private EpicsEurotherm2kController controller;

	private String controllerName;

	private long startTime;

	private String stateString = null;

	private String dataString = null;

	private String timeElapsed = null;

	/**
	 * Constructor
	 */
	public Eurotherm2K() {
		
		setInputNames(new String[] {"Temperature"});
		//setExtraNames(new String[] {"Status", "Time Elapsed"});
		String[] outputFormat = new String[inputNames.length + extraNames.length];
		outputFormat[0] = "%5.2f";
		//outputFormat[1] = "%s";
		//outputFormat[2] = "%s";
		setOutputFormat(outputFormat);
	}

	@Override
	public void configure() throws FactoryException {

		if (!configured) {
			poller = new Poller();
			poller.setPollTime(LONGPOLLTIME);
			// register this as listener to poller for update temperature values.
			poller.addListener(this);

			//String filePrefix = LocalProperties.get("gda.device.temperature.datadir");
			String filePrefix = PathConstructor.createFromProperty("gda.device.temperature.datadir");
			if ((filePrefix != null) && (fileSuffix != null)) {
				dataFileWriter = new DataFileWriter(filePrefix, fileSuffix);
			}
			
			if (controller == null) {
				setController( (EpicsEurotherm2kController)Finder.getInstance().find(controllerName) );
			}
			
			if (controller != null) {
				logger.debug("Controller {} found", controllerName);
				if (controller.isConfigureAtStartup()) {
					int i = 0;
					while (!controller.isConfigured()) {
						if (i > 10)
							break; // wait for 1 second.
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							// no-op
						}
						i++;
					}
					if (!controller.isConfigured())
						logger.warn("configure {} at start failed. You may need to reconfigure {} after GDA started.",
								controllerName, getName());
				} else {
					controller.configure();
				}
			} else {
				// if controller does not exist, unregister this listener as no data source is available
				poller.deleteListener(this);
				logger.error("Controller {} not found", controllerName);
				throw new FactoryException("Controller " + controllerName + " not found");
			}
			//add this observer and start poller thread only if controller is configured properly.
			if (controller.isConfigured() && controller.isConnected()) {
				try {
					currentTemp = getCurrentTemperature();
				} catch (DeviceException e) {
					logger.error("failed to get current temperature from {}. Device may not connected.", getName());
					throw new FactoryException("Device - "+getName()+" - is not available.", e);
				}
				controller.addIObserver(this);
				startPoller();
				configured = true;
			} else {
				logger.warn("'{}' need to reconfigure '{}' before using.", getName(), getName() + ".reconfigure( )");
			}
		}
	}

	@Override
	public void reconfigure() throws FactoryException {
		if (!configured)
			configure();
	}

	@Override
	public double getCurrentTemperature() throws DeviceException {
		return currentTemp = controller.getTemp();
	}

	/**
	 * Get the target temperature
	 * 
	 * @return the target temperature
	 * @throws DeviceException
	 */
	@Override
	public double getTargetTemperature() throws DeviceException {
		return controller.getTargetTemperature();
	}

	@Override
	protected void doStart() throws DeviceException {
		Date d = new Date();
		startTime = d.getTime();
		sendStart();
	}

	@Override
	public void hold() throws DeviceException {
		logger.warn("hold() is not available for {}", getName());
	}

	@Override
	public void runRamp() throws DeviceException {
		logger.warn("runRamp() is not available for {}", getName());
	}

	/**
	 * program a temperature ramp into hardware, this does not initiate the program, it validates the target temperature
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

		logger.debug("finalTemp is " + finalTemp);
		if (finalTemp > upperTemp || finalTemp < lowerTemp)
			throw new DeviceException("Invalid ramp final temperature");

		logger.debug("Ramp rate in K/hour is " + rate);
		if (rate >= controller.minRampRate || rate <= controller.maxRampRate)
			throw new DeviceException("Invalid ramp rate for temperature");

		controller.setRampRate(rate);
		// cont.setTargetTemperature(finalTemp);
		targetTemp = finalTemp;
		setPoint = finalTemp;
	}

	/**
	 * Initiate the programmed temperature ramp in the hardware.
	 * 
	 * @throws DeviceException
	 */
	public void sendStart() throws DeviceException {
		if (busy)
			throw new DeviceException(getName() + " is already ramping to temerature");
		controller.setTargetTemperature(targetTemp);
		busy = true;
		notify();
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

	@Override
	protected void doStop() throws DeviceException {
		sendStop();
	}

	/**
	 * on stop temperature ramp it sets to hold.
	 * 
	 * @throws DeviceException
	 */
	private void sendStop() throws DeviceException {
		setPoint=controller.getTemp();
		controller.setTargetTemperature(setPoint);
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
		startPoller();
		setPoint = targetTemp;
		controller.setTargetTemperature(targetTemp);
		busy = true;
		notify();
		Thread update = uk.ac.gda.util.ThreadManager.getThread(new UpdateThread(this), "feedback thread");
		update.start();
	}
	/**
	 * print current values to Jython Terminal during temperature change by "pos" command.
	 * @author fy65
	 *
	 */
	class UpdateThread implements Runnable {
		Eurotherm2K ocs;

		public UpdateThread(Eurotherm2K eurotherm2K) {
			this.ocs = eurotherm2K;
		}

		@Override
		public void run() {
			try {
				while (isBusy()) {
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						// noop
					}
					JythonServerFacade.getInstance().print(ocs.toString() + " " + stateString);
				}
			} catch (DeviceException e) {
				logger.error("Failed to get feedback values from EPICS.", e);
			}
		}

	}

	/**
	 * sets the ramp rate
	 * 
	 * @param rate
	 * @throws DeviceException
	 */
	@Override
	public void setRampRate(double rate) throws DeviceException {
		controller.setRampRate(rate);
	}

	/**
	 * gets the ramp rate
	 * 
	 * @return rate
	 * @throws DeviceException
	 */
	@Override
	public double getRampRate() throws DeviceException {
		return controller.getRampRate();
	}

	/**
	 * move device to room temperature {@inheritDoc}
	 * 
	 * @see gda.device.temperature.TemperatureBase#end()
	 */
	@Override
	public void end() throws DeviceException {
		controller.setTargetTemperature(ROOM_TEMPERATURE);
	}

	@Override
	public void pollDone(PollerEvent pe) {

		NumberFormat n = NumberFormat.getInstance();
		n.setMaximumFractionDigits(2);
		n.setGroupingUsed(false);

		logger.debug(getName() + " pollDone called");

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
			logger.warn("pollDone throw exception on isAtTargetTemperature() call", de);
		}
		if (timeSinceStart >= 0.0) {
			Date d = new Date();
			timeSinceStart = d.getTime() - startTime;
		}

		TemperatureStatus ts;
		dataString = "" + n.format(timeSinceStart / 1000.0) + " " + currentTemp;
		timeElapsed = n.format(timeSinceStart / 1000.0) + " sec";
		ts = new TemperatureStatus(currentTemp, lowerTemp, upperTemp, targetTemp, currentRamp, stateString, dataString);
		// logger.debug(getName() + " notifying IObservers with " + ts);
		notifyIObservers(this, ts);
	}

	/**
	 * @return controllerName
	 */
	public String getControllerName() {
		return controllerName;
	}

	/**
	 * @param controllerName
	 */
	public void setControllerName(String controllerName) {
		this.controllerName = controllerName;
	}

	/**
	 * @param controller
	 *            the controller
	 */
	public void setController(EpicsEurotherm2kController controller) {
		this.controller = controller;
	}

	/**
	 * @return EpicsEurotherm2kController
	 */
	public EpicsEurotherm2kController getController() {
		return controller;
	}

	
	

	@Override
	public void update(Object theObserved, Object changeCode) {
		if (theObserved instanceof EpicsEurotherm2kController.ErrorListener) {
			if (changeCode instanceof String) {
				String alarmStatus = changeCode.toString();
				TemperatureStatus ts = new TemperatureStatus(currentTemp, alarmStatus);
				JythonServerFacade.getInstance().print("Alarm Status: " + alarmStatus);
				notifyIObservers(this, ts);
			}
		} else if (theObserved instanceof EpicsEurotherm2kController.TempListener) {
			if (changeCode instanceof Double) {
				currentTemp = ((Double) changeCode).doubleValue();
			}
		} else if (theObserved instanceof EpicsEurotherm2kController.ConnectionListener) {
			if (((String)changeCode).equals("Disabled")) {
				logger.warn("{} is currently NOT connected to hardware.", getName());
			} else if (((String)changeCode).equals("Enabled")) {
				logger.info("{} is current temperature controller.", getName() );
			}
		}
		
	}

	@Override
	public void rawAsynchronousMoveTo(Object position) throws DeviceException {
		double temp = Double.parseDouble(position.toString());
		setTargetTemperature(temp);
	}

	@Override
	public Object rawGetPosition() throws DeviceException {
		return currentTemp;
	}

	@SuppressWarnings("unused")
	private Object getPositionWithStatus() throws DeviceException {
		String[] value = new String[3];
		value[0] = String.format(getOutputFormat()[0], currentTemp);
		value[1] = stateString;
		value[2] = timeElapsed;
		return value;
	}

	@Override
	public boolean rawIsBusy() throws DeviceException {
		return busy;
	}

//	@Override
//	public String toString() {
//		try {
//
//			// get the current position as an array of doubles
//			Object position = getPositionWithStatus();
//
//			// if position is null then simply return the name
//			if (position == null) {
//				logger.warn("getPosition() from " + getName() + " returns NULL.");
//				return getName() + " : NOT AVAILABLE";
//			}
//
//			String[] positionAsArray = getCurrentPositionArray(position, this);
//
//			// if cannot create array of doubles then use position's toString
//			// method
//			if (positionAsArray == null || positionAsArray.length == 1) {
//				return getName() + " : " + position.toString();
//			}
//
//			// else build a string of formatted positions
//			String output = getName() + " : ";
//			int i = 0;
//			for (; i < this.inputNames.length; i++) {
//				output += this.inputNames[i] + ": " + positionAsArray[i] + " ";
//			}
//
//			for (int j = 0; j < this.extraNames.length; j++) {
//				output += this.extraNames[j] + ": " + positionAsArray[i + j] + " ";
//			}
//			return output.trim();
//
//		} catch (PyException e) {
//			logger.info(getName() + ": jython exception while getting position. " + e.toString());
//			return getName();
//		} catch (Exception e) {
//			logger.info(getName() + ": exception while getting position. " + e.getMessage() + "; " + e.getCause(), e);
//			return getName();
//		}
//	}
	@Override
	public String toString() {
		String myString = "";
		try {
			Object position = this.getPosition();

			if (position == null) {
				logger.warn("getPosition() from " + this.getName() + " returns NULL.");
				return this.getName() + " : NOT AVAILABLE";
			}
			// print out simple version if only one inputName and
			// getPosition and getReportingUnits do not return arrays.
			if (!(position.getClass().isArray() || position instanceof PySequence)) {
				myString += this.getName() + " : ";
				if (position instanceof String) {
					myString += position.toString();
				} else {
					myString += String.format(outputFormat[0], Double.parseDouble(position.toString()));
				}
			} else {
				myString += this.getName() + " : ";
				if (position instanceof PySequence) {
					for (int i = 0; i < ((PySequence) position).__len__(); i++) {
						if (i > 0) {
							myString += " ";
						}
						myString += String.format(outputFormat[0], Double.parseDouble(((PySequence) position).__finditem__(i)
								.toString()));
					}
				} else {
					for (int i = 0; i < Array.getLength(position); i++) {
						if (i > 0) {
							myString += " ";
						}
						myString += String.format(outputFormat[0], Double.parseDouble(Array.get(position, i).toString()));
					}
				}

			}
		} catch (NumberFormatException e) {
			logger.error("Number Format Exception ", e);
		} catch (ArrayIndexOutOfBoundsException e) {
			logger.error("Array Index out of bounds ", e);
		} catch (IllegalArgumentException e) {
			logger.error("Illegal Argument ", e);
		} catch (DeviceException e) {
			logger.error("Device Exception ", e);
		}
		return myString;
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
