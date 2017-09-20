/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package gda.device.detector.pco;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.detector.areadetector.v17.ADBase;
import gda.epics.connection.EpicsController;
import gda.factory.Findable;
import gda.jython.InterfaceProvider;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.TimeoutException;

/**
 * support PCO Driver specific parameters only in EPICS PCO.
 * This is a simplified version of {@link PCOControllerV17} which is independent of any other EPICS plugins.
 * it only requires an instance of {@link ADBase} to support PCO specific {@link PCOTriggerMode}.
 *
 */
public class PCODriverController implements Findable {
	private static final Logger logger = LoggerFactory.getLogger(PCODriverController.class);
	// PCO specific EPICS interface element and PV fields
	private static final String PIX_RATE = "PIX_RATE";
	private static final String PIX_RATE_RBV = "PIX_RATE_RBV";
	private static final String ADC_MODE = "ADC_MODE";
	private static final String ADC_MODE_RBV = "ADC_MODE_RBV";
	private static final String CAM_RAM_USE_RBV = "CAM_RAM_USE_RBV";
	private static final String ELEC_TEMP_RBV = "ELEC_TEMP_RBV";
	private static final String POWER_TEMP_RBV = "POWER_TEMP_RBV";
	private static final String STORAGE_MODE = "STORAGE_MODE";
	private static final String STORAGE_MODE_RBV = "STORAGE_MODE_RBV";
	private static final String RECORDER_MODE = "RECORDER_MODE";
	private static final String RECORDER_MODE_RBV = "RECORDER_MODE_RBV";
	private static final String TIMESTAMP_MODE = "TIMESTAMP_MODE";
	private static final String TIMESTAMP_MODE_RBV = "TIMESTAMP_MODE_RBV";
	private static final String ACQUIRE_MODE = "ACQUIRE_MODE";
	private static final String ACQUIRE_MODE_RBV = "ACQUIRE_MODE_RBV";
	private static final String ARM_MODE = "ARM_MODE";
	private static final String ARM_MODE_RBV = "ARM_MODE_RBV";
	private static final String DELAY_TIME = "DELAY_TIME";
	private static final String DELAY_TIME_RBV = "DELAY_TIME_RBV";

	/**
	 * EPICS Utility
	 */
	private final EpicsController EPICS_CONTROLLER = EpicsController.getInstance();

	/**
	 * Map that stores the channel against the PV name
	 */
	private Map<String, Channel> channelMap = new HashMap<String, Channel>();

	private ADBase areaDetector;
	private String name;
	private String pvName;

	/**
	 * detector's trigger mode - auto, soft, Ext + Soft, Ext Pulse. used to switch detcetor operation mode.
	 */
	private PCOTriggerMode triggermode = PCOTriggerMode.SOFT;

	public PCODriverController() {
	}

	public void setTriggerMode(PCOTriggerMode value) throws Exception {
		this.triggermode = value;
		switch (value) {
		case AUTO:
			getAreaDetector().setTriggerMode((short) PCOTriggerMode.AUTO.ordinal());
			break;
		case SOFT:
			getAreaDetector().setTriggerMode((short) PCOTriggerMode.SOFT.ordinal());
			break;
		case EXTSOFT:
			getAreaDetector().setTriggerMode((short) PCOTriggerMode.EXTSOFT.ordinal());
			break;
		case EXTPULSE:
			getAreaDetector().setTriggerMode((short) PCOTriggerMode.EXTPULSE.ordinal());
			break;
		case EXTONLY:
			getAreaDetector().setTriggerMode((short) PCOTriggerMode.EXTONLY.ordinal());
			break;
		default:
			listTriggerModes();
			throw new IllegalArgumentException();
		}
	}

	public PCOTriggerMode getTriggerMode() {
		return this.triggermode;
	}

	public void listTriggerModes() {
		print("Available trigger modes:");
		for (PCOTriggerMode each : PCOTriggerMode.values()) {
			print(each.name());
		}
	}

	public void setTriggerMode(int value) throws Exception {
		getAreaDetector().setTriggerMode((short) value);
		switch (value) {
		case 0:
			this.triggermode = PCOTriggerMode.AUTO;
			break;
		case 1:
			this.triggermode = PCOTriggerMode.SOFT;
			break;
		case 2:
			this.triggermode = PCOTriggerMode.EXTSOFT;
			break;
		case 3:
			this.triggermode = PCOTriggerMode.EXTPULSE;
			break;
		case 4:
			this.triggermode = PCOTriggerMode.EXTONLY;
			break;
		default:
			listTriggerModes();
			throw new IllegalArgumentException();
		}
	}

	/**
	 * method to print message to the Jython Terminal console.
	 *
	 * @param msg
	 */
	private void print(String msg) {
		if (InterfaceProvider.getTerminalPrinter() != null) {
			InterfaceProvider.getTerminalPrinter().print(msg);
		}
	}

	public int getADCMode() throws TimeoutException, CAException, InterruptedException, Exception {
		try {
			return EPICS_CONTROLLER.cagetInt(getChannel("ADC_MODE_RBV", ADC_MODE_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getADCMode", ex);
			throw ex;
		}
	}

	public void setADCMode(int value) throws Exception {
		try {
			EPICS_CONTROLLER.caputWait(getChannel("ADC_MODE", ADC_MODE), value);
		} catch (Exception ex) {
			logger.warn("Cannot setADCMode", ex);
			logger.error("{} : {}", getName(), getAreaDetector().getStatusMessage_RBV());
			throw ex;
		}
	}

	public void setADCMode(PCOADCMode value) throws Exception {
		try {
			EPICS_CONTROLLER.caputWait(getChannel("ADC_MODE", ADC_MODE), value.ordinal());
		} catch (Exception ex) {
			logger.warn("Cannot setADCMode", ex);
			logger.error("{} : {}", getName(), getAreaDetector().getStatusMessage_RBV());
			throw ex;
		}
	}

	public int getPixRate() throws TimeoutException, CAException, InterruptedException, Exception {
		try {
			return EPICS_CONTROLLER.cagetInt(getChannel("PIX_RATE_RBV", PIX_RATE_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getPixRate", ex);
			throw ex;
		}
	}

	public void setPixRate(int value) throws Exception {
		try {
				EPICS_CONTROLLER.caput(getChannel("PIX_RATE", PIX_RATE), value);
		} catch (Exception ex) {
			logger.warn("Cannot setPixRate", ex);
			logger.error("{} : {}", getName(), getAreaDetector().getStatusMessage_RBV());
			throw ex;
		}
	}

	public double getCamRamUsage() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetDouble(getChannel("CAM_RAM_USE_RBV", CAM_RAM_USE_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getCamRamUsage", ex);
			throw ex;
		}
	}

	public double getElectronicTemperature() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetDouble(getChannel("ELEC_TEMP_RBV", ELEC_TEMP_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getElectronicTemperature", ex);
			throw ex;
		}
	}

	public double getPowerSupplyTemperature() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetDouble(getChannel("POWER_TEMP_RBV", POWER_TEMP_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getPowerSupplyTemperature", ex);
			throw ex;
		}
	}

	public int getStorageMode() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetInt(getChannel("STORAGE_MODE_RBV", STORAGE_MODE_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getStorageMode", ex);
			throw ex;
		}
	}

	public void setStorageMode(int value) throws Exception {
		if (value != 0 && value != 1) {
			throw new IllegalArgumentException(getName()
					+ ": Input must be 0 for 'Recorder' mode or 1 for 'FIFO buffer' mode");
		}
		try {
				EPICS_CONTROLLER.caput(getChannel("STORAGE_MODE", STORAGE_MODE), value);
		} catch (Exception ex) {
			logger.warn("Cannot setStorageMode", ex);
			logger.error("{} : {}", getName(), getAreaDetector().getStatusMessage_RBV());
			throw ex;
		}
	}

	public int getRecorderMode() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetInt(getChannel("RECORDER_MODE_RBV", RECORDER_MODE_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getRecorderMode", ex);
			throw ex;
		}
	}

	public void setRecorderMode(int value) throws Exception {
		if (value != 0 && value != 1) {
			throw new IllegalArgumentException(getName()
					+ ": Input must be 0 for 'Sequence' mode or 1 for 'Ring buffer' mode");
		}
		try {
			EPICS_CONTROLLER.caput(getChannel("RECORDER_MODE", RECORDER_MODE), value);
		} catch (Exception ex) {
			logger.warn("Cannot setRecorderMode", ex);
			logger.error("{} : {}", getName(), getAreaDetector().getStatusMessage_RBV());
			throw ex;
		}
	}

	public int getTimestampMode() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetInt(getChannel("TIMESTAMP_MODE_RBV", TIMESTAMP_MODE_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getTimestampMode", ex);
			throw ex;
		}
	}

	public void setTimestampMode(int value) throws Exception {
		if (value < 0 && value > 3) {
			throw new IllegalArgumentException(getName()
					+ ": Input must be 0 - None, 1 - BCD, 2 - BCD+ASCII, and 3 - ASCII");
		}
		try {
			EPICS_CONTROLLER.caput(getChannel("TIMESTAMP_MODE", TIMESTAMP_MODE), value);
		} catch (Exception ex) {
			logger.warn("Cannot setTimestampMode", ex);
			logger.error("{} : {}", getName(), getAreaDetector().getStatusMessage_RBV());
			throw ex;
		}
	}

	public int getAcquireMode() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetInt(getChannel("ACQUIRE_MODE_RBV", ACQUIRE_MODE_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getAcquireMode", ex);
			throw ex;
		}
	}

	public void setAcquireMode(int value) throws Exception {
		if (value < 0 && value > 2) {
			throw new IllegalArgumentException(getName()
					+ ": Input must be 0 - Auto, 1 - Ext. enable, and 2 - Ext. trigger");
		}
		try {
			EPICS_CONTROLLER.caput(getChannel(ACQUIRE_MODE, ACQUIRE_MODE), value);
		} catch (Exception ex) {
			logger.warn("Cannot setAcquireMode", ex);
			logger.error("{} : {}", getName(), getAreaDetector().getStatusMessage_RBV());
			throw ex;
		}
	}

	public int getArmMode() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetInt(getChannel(ARM_MODE_RBV, ARM_MODE_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getArmMode", ex);
			throw ex;
		}
	}

	public void armCamera() throws Exception {
		setArmMode(1);
	}

	public void disarmCamera() throws Exception {
		setArmMode(0);
	}

	public void setArmMode(int value) throws Exception {
		if (value != 0 && value != 1) {
			throw new IllegalArgumentException(getName() + ": Input must be 0 - Disarmed, 1 - Armed");
		}
		try {
			EPICS_CONTROLLER.caputWait(getChannel(ARM_MODE, ARM_MODE), value);
		} catch (Exception ex) {
			logger.warn("Cannot setArmMode", ex);
			logger.error("{} : {}", getName(), getAreaDetector().getStatusMessage_RBV());
			throw ex;
		}
	}

	public double getDelayTime() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetDouble(getChannel(DELAY_TIME_RBV, DELAY_TIME_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getDelayTime", ex);
			throw ex;
		}
	}

	public void setDelayTime(double value) throws Exception {
		try {
			EPICS_CONTROLLER.caput(getChannel(DELAY_TIME, DELAY_TIME), value);
		} catch (Exception ex) {
			logger.warn("Cannot setDelayTime", ex);
			logger.error("{} : {}", getName(), getAreaDetector().getStatusMessage_RBV());
			throw ex;
		}
	}

	/**
	 * This method allows to toggle between the method in which the PV is acquired.
	 *
	 * @param pvElementName
	 * @param args
	 * @return {@link Channel} to talk to the relevant PV.
	 * @throws Exception
	 */
	private Channel getChannel(String pvElementName, String... args) throws Exception {
		try {
			String pvPostFix = null;
			if (args.length > 0) {
				// PV element name is different from the pvPostFix
				pvPostFix = args[0];
			} else {
				pvPostFix = pvElementName;
			}

			String fullPvName = getPvName() + pvPostFix;
			return createChannel(fullPvName);
		} catch (Exception exception) {
			logger.warn("Problem getting channel", exception);
			throw exception;
		}
	}

	public Channel createChannel(String fullPvName) throws CAException, TimeoutException {
		Channel channel = channelMap.get(fullPvName);
		if (channel == null) {
			try {
				channel = EPICS_CONTROLLER.createChannel(fullPvName);
			} catch (CAException cae) {
				logger.warn("Problem creating channel", cae);
				throw cae;
			} catch (TimeoutException te) {
				logger.warn("Problem creating channel", te);
				throw te;

			}
			channelMap.put(fullPvName, channel);
		}
		return channel;
	}

	public void afterPropertiesSet() throws Exception {
		if (getPvName() == null) {
			throw new IllegalArgumentException("'pvName' needs to be declared");
		}
		if (getAreaDetector() == null) {
			throw new IllegalArgumentException("'areaDetector' needs to be declared");
		}
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	public String getPvName() {
		return pvName;
	}

	public void setPvName(String pvName) {
		this.pvName = pvName;
	}

	public ADBase getAreaDetector() {
		return areaDetector;
	}

	public void setAreaDetector(ADBase areaDetector) {
		this.areaDetector = areaDetector;
	}
}
