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
import uk.ac.diamond.daq.concurrent.Async;

/**
 * Class that supports Oxford Cryostream 700.
 */
public class OxfordCryostream700 extends TemperatureBase implements IObserver {

	/**
	 * logging instance
	 */
	private static final Logger logger = LoggerFactory.getLogger(OxfordCryostream700.class);

	private CryoController cryoController;

	public CryoController getCryoController() {
		return cryoController;
	}

	public void setCryoController(CryoController cryoController) {
		this.cryoController = cryoController;
	}

	private String cryoControllerName;

	private long startTime;

	private String stateString = null;

	/**
	 * Constructor
	 */
	public OxfordCryostream700() {

		setInputNames(new String[] { "Temperature" });
		String[] outputFormat = new String[inputNames.length + extraNames.length];
		outputFormat[0] = "%5.2f";

		setOutputFormat(outputFormat);
	}

	@Override
	public void configure() throws FactoryException {

		if (!isConfigured()) {
			String filePrefix = PathConstructor.createFromProperty("gda.device.temperature.datadir");
			if ((filePrefix != null) && (fileSuffix != null)) {
				dataFileWriter = new DataFileWriter(filePrefix, fileSuffix);
			}
			if (cryoController == null) { // CASTOR configuration
				if ((cryoController = Finder.getInstance().find(cryoControllerName)) != null) {
					logger.debug("CryoController {} found", cryoControllerName);
				} else {
					logger.error("Cryo controller {} not found", cryoControllerName);
					throw new FactoryException("Cryo controller " + cryoControllerName + " not found");
				}
			}

			if (cryoController.isConfigureAtStartup()) {
				int i = 0;
				while (!cryoController.isConfigured()) {
					if (i > 10)
						break; // wait for 1 second.
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
						// no-op
					}
					i++;
				}
				if (!cryoController.isConfigured())
					logger.warn("configure {} at start failed. You may need to reconfigure {} after GDA started.",
							cryoController.getName(), getName());
			} else {
				cryoController.configure();
			}

			poller = new Poller();
			poller.setPollTime(LONG_POLL_TIME);
			// register this as listener to poller for update temperature values.
			poller.addListener(this);


			if (cryoController.isConfigured() && cryoController.isConnected()) {
				try {
					currentTemp = getCurrentTemperature();
				} catch (DeviceException e) {
					logger.error("failed to get current temperature from {}. Device may not connected.", getName());
					throw new FactoryException("Failed to get Current Temperature. ",e);
				}
				cryoController.addIObserver(this);
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
		return currentTemp = cryoController.getTemp();
	}

	/**
	 * Get the target temperature
	 *
	 * @return the target temperature
	 * @throws DeviceException
	 */
	@Override
	public double getTargetTemperature() throws DeviceException {
		return cryoController.getTargetTemp();
	}

	@Override
	protected void doStart() throws DeviceException {
		Date d = new Date();
		startTime = d.getTime();
		sendStart();
	}

	@Override
	public void hold() throws DeviceException {
		cryoController.hold();
	}

	@Override
	public void runRamp() throws DeviceException {
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

		logger.debug("finalTemp is {}", finalTemp);
		if (finalTemp > upperTemp || finalTemp < lowerTemp)
			throw new DeviceException("Invalid ramp final temperature");

		logger.debug("Ramp rate in K/hour is {}", rate);
		if (rate >= cryoController.MIN_RAMP_RATE || rate <= cryoController.MAX_RAMP_RATE)
			throw new DeviceException("Invalid ramp rate for temperature");

		cryoController.setRampRate(rate);
		cryoController.setTargetTemp(finalTemp);
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
		cryoController.ramp();
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
		sendStop();
	}

	/**
	 * on stop temperature ramp it sets to hold.
	 *
	 * @throws DeviceException
	 */
	private void sendStop() throws DeviceException {
		cryoController.hold();
		setPoint = getCurrentTemperature();
		cryoController.setTargetTemp(setPoint);
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
		cryoController.setTargetTemp(targetTemp);
		setPoint = targetTemp;
		cryoController.ramp();
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
		JythonServerFacade.getInstance().print("start/restart up, please wait.");
		cryoController.start();
	}

	/**
	 * perform the shutdown procedure of the hardware {@inheritDoc}
	 *
	 * @see gda.device.temperature.TemperatureBase#end()
	 */
	@Override
	public void end() throws DeviceException {
		JythonServerFacade.getInstance().print("CryoController shutdown, please wait.");
		cryoController.end();
	}

	/**
	 * sets the ramp rate
	 *
	 * @param rate
	 * @throws DeviceException
	 */
	@Override
	public void setRampRate(double rate) throws DeviceException {
		cryoController.setRampRate(rate);
	}

	/**
	 * gets the ramp rate
	 *
	 * @return rate
	 * @throws DeviceException
	 */
	@Override
	public double getRampRate() throws DeviceException {
		return cryoController.getRampRate();
	}

	/**
	 * Temperature GUI update. {@inheritDoc}
	 *
	 * @see gda.util.PollerListener#pollDone(gda.util.PollerEvent)
	 */
	@Override
	public void pollDone(PollerEvent pe) {

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
			logger.warn("pollDone throw exception on isAtTargetTemperature() call", de);
		}
		if (timeSinceStart >= 0.0) {
			Date d = new Date();
			timeSinceStart = d.getTime() - startTime;
		}

		TemperatureStatus ts;
		String dataString = "" + n.format(timeSinceStart / 1000.0) + " " + currentTemp;

		ts = new TemperatureStatus(currentTemp, lowerTemp, upperTemp, targetTemp, currentRamp, stateString, dataString);
		notifyIObservers(this, ts);
	}

	/**
	 * @return cryoControllerName
	 */
	public String getCryoControllerName() {
		return cryoControllerName;
	}

	/**
	 * @param cryoControllerName
	 */
	public void setCryoControllerName(String cryoControllerName) {
		this.cryoControllerName = cryoControllerName;
	}

	@Override
	public void update(Object theObserved, Object changeCode) {
		if (theObserved instanceof CryoController.AlarmListener) {
			if (changeCode instanceof String) {
				String alarmStatus = changeCode.toString();
				TemperatureStatus ts = new TemperatureStatus(currentTemp, alarmStatus);
				JythonServerFacade.getInstance().print("Alarm Status: " + alarmStatus);
				notifyIObservers(this, ts);
			}
		} else if (theObserved instanceof CryoController.CurrentTempListener) {
			if (changeCode instanceof Double) {
				currentTemp = ((Double) changeCode).doubleValue();
			}
		} else if (theObserved instanceof CryoController.ConnectionListener) {
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
	private Object getPositionWithStatus() throws DeviceException {
		String[] value = new String[8];
		value[0] = String.format(getOutputFormat()[0], currentTemp);
		value[1] = stateString;
		value[2] = String.format(getOutputFormat()[0], cryoController.getRemaining());
		value[3] = cryoController.getPhase();
		value[4] = cryoController.getRunmode();
		return value;
	}

	@Override
	public Object rawGetPosition() throws DeviceException {
		return currentTemp;
	}

	@Override
	public boolean rawIsBusy() throws DeviceException {
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
			if (!(position.getClass().isArray() || position instanceof PySequence)) {
				myString.append(this.getName());
				myString.append(" : ");
				if (position instanceof String) {
					myString.append(position.toString());
				} else {
					myString.append(String.format(outputFormat[0], Double.parseDouble(position.toString())));
				}
			} else {
				myString.append(this.getName());
				myString.append(" : ");
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
