/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.devices.keithley;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.epics.connection.EpicsController;
import gda.factory.FactoryException;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.TimeoutException;
import gov.aps.jca.event.MonitorListener;

/**
 * This class is for controlling a Keithley 2600 Series Sourcemeter.
 * <p>
 * The device provides the ability to source current or voltage and read back the voltage, current and resistance of the load.
 * <p>
 * Manual is avaliable from: "S:\Science\I05-ARPES\Lab Instruments\Keithley 2634B SourceMeter\Keithley 2634B manual 2600BS-901-01.pdf"
 *
 * @author James Mudd
 * @since GDA 9.10
 */
public class Keithley2600Series extends AbstractKeithley2600Series {

	private static final Logger logger = LoggerFactory.getLogger(Keithley2600Series.class);

	// Values internal to the object for Channel Access
	private final EpicsController epicsController = EpicsController.getInstance();

	// Map that stores the channel against the PV name
	private final Map<String, Channel> channelMap = new HashMap<>();

	// Status PVs
	private static final String STATUS = "Status";
	private static final String DISABLED = "Disable";

	// Readback PVs
	private static final String VOLTAGE_RBV = "ActualV";
	private static final String CURRENT_RBV = "ActualI";
	private static final String RESISTANCE_RBV = "ActualR";
	private static final String SOURCE_MODE_RBV = "SourceRead";

	// Control PVs
	/** Switches output on or off */
	private static final String OUTPUT = "Output";
	/** Switches output to voltage mode */
	private static final String VOLTAGE_SOURCE_MODE = "VSource.PROC";
	/** Switches output to current mode */
	private static final String CURRENT_SOURCE_MODE = "ISource.PROC";

	/** To be used in voltage source mode */
	private static final String VOLTAGE_SETPOINT = "VSP";
	private static final String VOLTAGE_SETPOINT_RBV = "VSPR";

	/** To be used in current source mode */
	private static final String CURRENT_SETPOINT = "ISP";
	private static final String CURRENT_SETPOINT_RBV = "ISPR";

	/** 2 wire or 4 wire measurement see {@link ResistanceMode} */
	private static final String RESISTANCE_MODE = "ResistanceMode";
	private static final String RESISTANCE_MODE_RBV = "ResistanceModeRead";

	/** This monitors the status flags for voltage or current limits hit */
	private final MonitorListener limitStatusMonitor = ev -> {
		logger.trace("Received status update: {}", ev);
		// Messy conversion, that's EPICS
		int status = (int) ((double[]) ev.getDBR().getValue())[0];
		limitStatus = Status.fromEpicsInt(status);
		if (limitStatus != Status.NORMAL) {
			logger.error("'{}' status changed to '{}'", getName(), limitStatus);
			// If were currently settling abort that
			setting.cancel(true);
		}
	};

	/** The base PV to which the constants in this class are appended */
	private String basePVName = null;

	/**
	 * Lazy initialise channels and store them in a map for retrieval later
	 *
	 * @param pvPostFix
	 *            the suffix of the pv
	 * @return The EPICS channel object
	 */
	private Channel getChannel(String pvSuffix) {
		return channelMap.computeIfAbsent(pvSuffix, pvSuffix2 -> {
			final String fullPvName = basePVName + pvSuffix2;
			try {
				return epicsController.createChannel(fullPvName);
			} catch (CAException | TimeoutException e) {
				logger.error("Failed to create channel: " + fullPvName, e);
				return null;
			}
		});
	}

	@Override
	public void configure() throws FactoryException {
		logger.trace("configure called");

		// Check if we are already configured
		if (isConfigured()) {
			logger.debug("Already configured");
			return;
		}

		// Sets up names and output format
		super.configure();

		// First verify the Spring configuration
		if (basePVName == null) {
			logger.error("Configure called with no basePVName. Check spring configuration!");
			throw new IllegalStateException("Configure called with no basePVName. Check spring configuration!");
		}

		// Check the basePv ends with : if not add it
		if (!basePVName.endsWith(":")) {
			logger.trace("basePv didn't end with : adding one");
			basePVName += ":";
		}

		logger.info("Configuring Keithly with base PV: {}", basePVName);
		try {
			if (isDisabled()) {
				logger.warn("'{}' Is disabled and will not be configured", getName());
				return;
			}

			logger.debug("Adding monitor for Status");
			epicsController.setMonitor(getChannel(STATUS), limitStatusMonitor);
		} catch (Exception e) {
			logger.error("Failed to configure: {}", getName(), e);
		}

		setConfigured(true);
		logger.info("Finished configuring '{}'", getName());
	}

	private boolean isDisabled() throws DeviceException {
		try {
			return "Disabled".equalsIgnoreCase(epicsController.cagetString(getChannel(DISABLED)));
		} catch (Exception e) {
			throw new DeviceException("Failed to get disabled status", e);
		}
	}

	@Override
	public SourceMode getSourceMode() throws DeviceException {
		try {
			return SourceMode.fromEpics(epicsController.cagetString(getChannel(SOURCE_MODE_RBV)));
		} catch (Exception e) {
			throw new DeviceException("Failed to get source mode", e);
		}
	}

	@Override
	public void setSourceMode(SourceMode mode) throws DeviceException {
		logger.debug("'{}' setting source mode to '{}'", getName(), mode);
		try {
			switch (mode) {
			case VOLTAGE:
				epicsController.caputWait(getChannel(VOLTAGE_SOURCE_MODE), 1);
				break;
			case CURRENT:
				epicsController.caputWait(getChannel(CURRENT_SOURCE_MODE), 1);
				break;
			default:
				throw new IllegalArgumentException("Unkown SourceMode: " + mode);
			}

			// The caputWait is not implemented correctly by the IOC it returns before the mode change is actually finished so add a delay here
			waitForSwitchOn();
		} catch (Exception e) {
			throw new DeviceException("Failed to set source mode to " + mode, e);
		}
	}

	@Override
	public ResistanceMode getResistanceMode() throws DeviceException {
		try {
			return ResistanceMode.fromEpics(epicsController.cagetString(getChannel(RESISTANCE_MODE_RBV)));
		} catch (Exception e) {
			throw new DeviceException("Failed to get resistance mode", e);
		}
	}

	@Override
	public void setResistanceMode(ResistanceMode mode) throws DeviceException {
		logger.debug("'{}' setting resistance mode to '{}'", getName(), mode);
		try {
			epicsController.caputWait(getChannel(RESISTANCE_MODE), mode.toEpics());
		} catch (Exception e) {
			throw new DeviceException("Failed to set resistance mode to " + mode, e);
		}
	}

	@Override
	public boolean isOutputOn() throws DeviceException {
		try {
			return "On".equalsIgnoreCase(epicsController.cagetString(getChannel(OUTPUT)));
		} catch (Exception e) {
			throw new DeviceException("Failed to get disabled status", e);
		}
	}

	@Override
	public void outputOn() throws DeviceException {
		logger.debug("'{}' Switching on...", getName());
		try {
			epicsController.caputWait(getChannel(OUTPUT), "On");

			// The caputWait is not implemented correctly by the IOC it returns before the mode change is actually finished so add a delay here
			waitForSwitchOn();
		} catch (Exception e) {
			throw new DeviceException("Keithley failed to swtich on", e);
		}
		logger.info("'{}' is on", getName());
	}

	@Override
	public void outputOff() throws DeviceException {
		logger.debug("'{}' Switching off...", getName());
		switchOnAtNextMove = false; // Explicitly told to switch off don't switch on at next move
		try {
			epicsController.caputWait(getChannel(OUTPUT), "Off");
		} catch (Exception e) {
			throw new DeviceException("Keithley failed to swtich off", e);
		}
		logger.info("'{}' is off", getName());
	}

	public String getBasePVName() {
		return basePVName;
	}

	public void setBasePVName(String basePVName) {
		this.basePVName = basePVName;
	}

	private void setOutputDemand(double demand) throws DeviceException {
		SourceMode sourceMode = getSourceMode();
		switch (sourceMode) {
		case VOLTAGE:
			setVoltageDemand(demand);
			break;
		case CURRENT:
			setCurrentDemand(demand);
			break;
		default:
			throw new IllegalStateException("Unkown SourceMode: " + sourceMode);
		}
	}

	@Override
	protected void setOutputDemandAndWaitToSettle(double demand) {
		try {
			checkStatusAndThrow();
			setOutputDemand(demand);

			if (switchOnAtNextMove) {
				switchOnAtNextMove = false;
				outputOn();
			}
		} catch (DeviceException e) {
			// Here use RuntimeException to allow use in lambda
			throw new RuntimeException("Failed to set output demand", e);
		}
		waitForSettling();
	}

	private void setCurrentDemand(double demand) throws DeviceException {
		// TODO validation
		logger.debug("'{}' setting current demand to: {}", getName(), demand);
		try {
			epicsController.caputWait(getChannel(CURRENT_SETPOINT), demand);
		} catch (Exception e) {
			throw new DeviceException("Failed to set current setpoint to: " + demand, e);
		}
	}

	private void setVoltageDemand(double demand) throws DeviceException {
		// TODO validation
		logger.debug("'{}' setting voltage demand to: {}", getName(), demand);
		try {
			epicsController.caputWait(getChannel(VOLTAGE_SETPOINT), demand);
		} catch (Exception e) {
			throw new DeviceException("Failed to set voltage setpoint to: " + demand, e);
		}
	}

	@Override
	protected double getActualVoltage() throws DeviceException {
		try {
			return epicsController.cagetDouble(getChannel(VOLTAGE_RBV));
		} catch (Exception e) {
			throw new DeviceException("Failed to get actual voltage", e);
		}
	}

	@Override
	protected double getActualCurrent() throws DeviceException {
		try {
			return epicsController.cagetDouble(getChannel(CURRENT_RBV));
		} catch (Exception e) {
			throw new DeviceException("Failed to get actual current", e);
		}
	}

	@Override
	protected double getActualResistance() throws DeviceException {
		try {
			return epicsController.cagetDouble(getChannel(RESISTANCE_RBV));
		} catch (Exception e) {
			throw new DeviceException("Failed to get actual resistance", e);
		}
	}

	@Override
	protected double getDemandVoltage() throws DeviceException {
		try {
			return epicsController.cagetDouble(getChannel(VOLTAGE_SETPOINT_RBV));
		} catch (Exception e) {
			throw new DeviceException("Failed to get demand voltage", e);
		}
	}

	@Override
	protected double getDemandCurrent() throws DeviceException {
		try {
			return epicsController.cagetDouble(getChannel(CURRENT_SETPOINT_RBV));
		} catch (Exception e) {
			throw new DeviceException("Failed to get demand current", e);
		}
	}

}
