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
import gov.aps.jca.Channel;
import gov.aps.jca.Monitor;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.DBR_Double;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;

/**
 * This class is designed to protect the VG Scienta analyser from over-exposing the detector which can cause damage. The live camera image is processed in EPICS
 * using ROI binning and then the STAT plugin provides the maximum value. This PVs has the warning and alarm states on it so EPICS will inform this class if the
 * warning or alarm value is reached.
 * <p>
 * If the <i>alarmValue</i> is exceeded the command defined by <i>alarmActionCommand</i> is run, which takes action to protect the detector, typically closing
 * the shutter. The message defined by <i>userMessage</i> is printed to the terminal informing the user what has happened and how to recover.
 *
 * @author James Mudd
 */
public class VGScientaOverExposureProtector implements Configurable, MonitorListener {
	private static final Logger logger = LoggerFactory.getLogger(VGScientaOverExposureProtector.class);

	// Over-exposure values
	private double warningValue = 0;
	private double alarmValue = 0;

	// PVs
	private String valuePv;
	private String warningPv;
	private String alarmPv;

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

		if (valuePv == null || warningPv == null || alarmPv == null) {
			logger.error("The valuePv, warningPv and alarmPv must be set for the over exposure protection to work. It will not be enabled");
			// return here we wont be able to set this up usefully
			return;
		}

		// Check that the warning and alarm values make sense, this is mostly to catch the case =0 where they have not been set
		if (warningValue <= 0 || alarmValue <= 0) {
			logger.error("The warningValue and alarmValue must be set");
			// return here we wont be able to set this up usefully
			return;
		}

		if (alarmActionCommand == null) {
			logger.warn("No alarmActionCommand has been set. No action will be taken if the alarmValue is exceeded");
		}

		try {
			// Set the warning and alarm values. These are only used by EPICS to indicate to the user the status
			Channel warningChannel = EPICS_CONTROLLER.createChannel(warningPv);
			Channel alarmChannel = EPICS_CONTROLLER.createChannel(alarmPv);
			logger.debug("Setting analyser over-exposure warning value to: {}", warningValue);
			EPICS_CONTROLLER.caputWait(warningChannel, warningValue);
			logger.debug("Setting analyser over-exposure alarm value to: {}", alarmValue);
			EPICS_CONTROLLER.caputWait(alarmChannel, alarmValue);
			logger.info("Setup analyser over-exposure warning and alarm values");

			Channel valueChannel = EPICS_CONTROLLER.createChannel(valuePv);
			// Get updates if the alarm status changes. Should reduce unnecessary updates
			valueChannel.addMonitor(Monitor.ALARM, this);
			logger.debug("Added monitor to analyser over-exposure value channel");

		} catch (Exception e) {
			logger.error("Failed to configure analyser over-exposure protection", e);
		}

		logger.info("Finsihed configuring analyser over-exposure protection");
	}

	@Override
	public void monitorChanged(MonitorEvent ev) {
		DBR dbr = ev.getDBR();
		if (dbr.isDOUBLE()) {
			// Get the actual value
			final double value = ((DBR_Double) dbr).getDoubleValue()[0];

			// Check for the alarm case first its the most important
			if (value >= alarmValue) {
				if (!alarmHandled) { // If alarm state hasn't been handled
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
				return;
			} else if (value >= warningValue) {
				if (!warningHandled) { // If warning state hasn't been handled
					logger.warn("Analyser over-exposure warning level reached");
					warningHandled = true;
				}
				return;
			}

			if (value < warningValue) {
				logger.info("Analyser exposure at safe level");
				alarmHandled = false;
				warningHandled = false;
				logger.debug("Reset alarmHandled and warningHandled to false");
				return;
			}

		} else {
			logger.error("Recieved unexpected DBR type");
		}
	}

	public void setWarningValue(double warningValue) {
		this.warningValue = warningValue;
	}

	public void setAlarmValue(double alarmValue) {
		this.alarmValue = alarmValue;
	}

	public void setValuePv(String valuePv) {
		this.valuePv = valuePv;
	}

	public void setWarningPv(String warningPv) {
		this.warningPv = warningPv;
	}

	public void setAlarmPv(String alarmPv) {
		this.alarmPv = alarmPv;
	}

	public void setAlarmActionCommand(String alarmActionCommand) {
		this.alarmActionCommand = alarmActionCommand;
	}

	public void setUserMessage(String userMessage) {
		this.userMessage = userMessage;
	}

}
