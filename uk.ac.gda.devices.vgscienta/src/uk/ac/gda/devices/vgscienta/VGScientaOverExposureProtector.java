/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package uk.ac.gda.devices.vgscienta;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.epics.connection.EpicsController;
import gda.factory.Configurable;
import gda.factory.FactoryException;
import gda.jython.InterfaceProvider;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.Monitor;
import gov.aps.jca.TimeoutException;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;

/**
 * This class is designed to protect the VG Scienta analyser from over-exposing the detector which can cause damage. The live camera image is processed in EPICS
 * using ROI binning and then the STAT plugin provides the maximum value. This PVs has the warning and alarm states on it so EPICS will inform this class if the
 * warning or alarm value is reached.
 * <p>
 * If the <i>alarmValue</i> is exceeded a <i>delayTime</i> is waited to check for transient events then if the detector is still over-exposed the command
 * defined by <i>alarmActionCommand</i> is run, which takes action to protect the detector, typically closing the shutter. The message defined by
 * <i>userMessage</i> is printed to the terminal informing the user what has happened and how to recover.
 * <p>
 * Everything is configured in Spring and the class is stand alone it doesn't require any other analyser classes to work.
 *
 * @author James Mudd
 */
public class VGScientaOverExposureProtector implements Configurable, MonitorListener {
	private static final Logger logger = LoggerFactory.getLogger(VGScientaOverExposureProtector.class);

	// Over-exposure values
	private long delayTime = 0;

	// PVs
	private String basePv; // The one that corresponds directly to the value
	// The suffixes appended to the base PV to get the warning and alarm level PVs
	private static final String ALARM_PV_SUFFIX = ".HIHI";
	private static final String WARNING_PV_SUFFIX = ".HIGH";

	// Channels
	private Channel warningChannel;
	private Channel alarmChannel;
	private Channel valueChannel;

	// Actions
	private String alarmActionCommand;
	private String userMessage;

	// Internal State
	private volatile boolean alarmHandled = false;
	private volatile boolean warningHandled = false;

	private final EpicsController EPICS_CONTROLLER = EpicsController.getInstance();

	@Override
	public void configure() throws FactoryException {
		logger.info("Configuring over-exposure protection");

		if (basePv == null) {
			logger.error("The basePv must be set for the over exposure protection to work. It will NOT be enabled");
			// return here we wont be able to set this up usefully
			return;
		}

		if (delayTime <= 0) {
			logger.error("The delayTime must be set");
			// return here we wont be able to set this up usefully
			return;
		}

		if (alarmActionCommand == null) {
			logger.warn("No alarmActionCommand has been set. No action will be taken if the alarmValue is exceeded");
		}

		try {
			// Create the warning alarm and value channels
			warningChannel = EPICS_CONTROLLER.createChannel(basePv + WARNING_PV_SUFFIX);
			alarmChannel = EPICS_CONTROLLER.createChannel(basePv + ALARM_PV_SUFFIX);
			valueChannel = EPICS_CONTROLLER.createChannel(basePv);

			// Get updates if the alarm status changes. Should reduce unnecessary updates
			valueChannel.addMonitor(Monitor.ALARM, this);
			logger.debug("Added monitor to analyser over-exposure value channel");

		} catch (Exception e) {
			logger.error("Failed to configure analyser over-exposure protection", e);
		}

		logger.info("Finsihed configuring analyser over-exposure protection");
	}

	private double getAlarmValue() throws TimeoutException, CAException, InterruptedException {
		double alarmValue = EPICS_CONTROLLER.cagetDouble(alarmChannel);
		logger.debug("Alarm value is: {}", alarmValue);
		return alarmValue;
	}

	private double getWarningValue() throws TimeoutException, CAException, InterruptedException {
		double warningValue = EPICS_CONTROLLER.cagetDouble(warningChannel);
		logger.debug("Warning value is: {}", warningValue);
		return warningValue;
	}

	@Override
	public void monitorChanged(MonitorEvent ev) {
		// Ignore the event it just used to trigger this code

		try {
			// Get the actual value
			final double value = EPICS_CONTROLLER.cagetDouble(valueChannel);

			// Check for the alarm case first its the most important
			if (value >= getAlarmValue()) {
				if (!alarmHandled) { // If alarm state hasn't been handled
					logger.warn("Analyser over-exposure detected! Waiting for {} ms to check if it's transient", delayTime);
					// Wait for delayTime to check if its transient
					try {
						Thread.sleep(delayTime);
					} catch (InterruptedException e) {
						logger.error("Interrupted while waiting to recheck exposure, will have waited < {} ms", delayTime, e);
					}

					// Re-check the value channel
					double recheckedValue = EPICS_CONTROLLER.cagetDouble(valueChannel);
					if (recheckedValue < getAlarmValue()) {
						logger.info("After delay exposure is now below the over-exposure limit");
						return; // The value is now below the alarm value so don't take further action
					}

					// If you reach here over-exposure was detected.
					takeAction();
				}
			} else if (value >= getWarningValue()) {
				if (!warningHandled) { // If warning state hasn't been handled
					logger.warn("Analyser over-exposure warning level reached");
					warningHandled = true;
				}
			} else { // i.e < warning value
				logger.info("Analyser exposure at safe level");
				alarmHandled = false;
				warningHandled = false;
				logger.debug("Reset alarmHandled and warningHandled to false");
			}
		} catch (TimeoutException | CAException | InterruptedException e) {
			logger.error("Error rechecking the exposure value. Assuming over-exposure!", e);
			takeAction();
		}
	}

	/**
	 * This method is to take action to stop the detector over-exposure e.g. closing a shutter.
	 */
	private void takeAction() {
		logger.error("Analyser over-exposure detected! Running command: {}", alarmActionCommand);
		if (alarmActionCommand != null) { // Might be null if not set
			InterfaceProvider.getCommandRunner().runCommand(alarmActionCommand);
		}
		if (userMessage != null) {
			// Print a message to the console informing the users what has happened.
			InterfaceProvider.getTerminalPrinter().print(userMessage);
		}
		alarmHandled = true;
	}

	public void setAlarmActionCommand(String alarmActionCommand) {
		this.alarmActionCommand = alarmActionCommand;
	}

	public void setUserMessage(String userMessage) {
		this.userMessage = userMessage;
	}

	public void setDelayTime(long delayTime) {
		this.delayTime = delayTime;
	}

	public void setBasePv(String basePv) {
		this.basePv = basePv;
	}

}
