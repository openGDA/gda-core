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

package gda.device.detector.etldetector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.epics.ConfigurationNotFoundException;
import gda.configuration.epics.Configurator;
import gda.device.DeviceBase;
import gda.epics.connection.EpicsChannelManager;
import gda.epics.connection.EpicsController;
import gda.epics.connection.InitializationListener;
import gda.epics.interfaces.ETLdetectorType;
import gda.epics.xml.EpicsRecord;
import gda.factory.FactoryException;
import gda.factory.Finder;
import gda.util.OutOfRangeException;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.TimeoutException;

/**
 * The Class EpicsETLScintillator.
 */
public class EpicsETLController extends DeviceBase implements InitializationListener {

	private static final Logger logger = LoggerFactory.getLogger(EpicsETLController.class);

	/**
	 * The Constant type for this device.
	 */
	public static final String type = "scintillator";

	/**
	 * The Constant description for this device.
	 */
	public static final String description = "ETL scintillator detector";

	private String name;

	/**
	 * List of EPICS channels to connect
	 */
	private Channel ctrl = null; // HV Control

	private Channel ctrlrbv = null;

	private Channel ulim = null; // Upper window Limit

	private Channel ulimrbv = null;

	private Channel llim = null; // Lower window limit

	private Channel llimrbv = null;

	private Channel hvadc = null; // HV actual output

	private EpicsRecord epicsRecord = null;

	private String epicsRecordName = null;

	private String deviceName = null;

	private EpicsChannelManager channelManager;

	private EpicsController controller;

	private String pvRoot;

	public String getPvRoot() {
		return pvRoot;
	}

	/**
	 * The Constructor.
	 */
	public EpicsETLController() {
		channelManager = new EpicsChannelManager(this);
		controller = EpicsController.getInstance();
	}

	@Override
	public void configure() throws FactoryException {
		if (!configured) {
			// EPICS interface verion 2 for phase I beamlines + I22
			if (getEpicsRecordName() != null) {
				if ((epicsRecord = (EpicsRecord) Finder.getInstance().find(epicsRecordName)) != null) {
					String recordName = epicsRecord.getFullRecordName();
					createChannelAccess(recordName);
					channelManager.tryInitialize(100);
				} else {
					logger.error("Epics Record " + epicsRecordName + " not found");
					throw new FactoryException("Epics Record " + epicsRecordName + " not found");
				}
			}
			// EPICS interface version 3 for phase II beamlines (excluding I22).
			else if (getDeviceName() != null) {
				try {
					ETLdetectorType etlConfig = Configurator.getConfiguration(getDeviceName(),
							gda.epics.interfaces.ETLdetectorType.class);
					createChannelAccess(etlConfig);
					channelManager.tryInitialize(100);
				} catch (ConfigurationNotFoundException e) {
					logger.error("Can NOT find EPICS configuration for device: " + getDeviceName(), e);
				}

			} else if (getPvRoot() != null) {
					createChannelAccess(getPvRoot());
					channelManager.tryInitialize(100);
			}
			// Nothing specified in Server XML file
			else {
				logger.error("Missing EPICS interface configuration for the ETL sintillator detector " + getName());
				throw new FactoryException("Missing EPICS interface configuration for the ETL sintillator detectorr "
						+ getName());
			}

		}// end of if (!configured)
	}

	/**
	 * creates channel access implementing phase I beamline EPICS interfaces.
	 *
	 * @param recordName
	 * @throws FactoryException
	 */
	private void createChannelAccess(String recordName) throws FactoryException {
		try {
			ctrl = channelManager.createChannel(recordName + ":CTRL", false);
			ctrlrbv = channelManager.createChannel(recordName + ":CTRL:RBV", false);
			hvadc = channelManager.createChannel(recordName + ":HVADC", false);
			ulim = channelManager.createChannel(recordName + ":ULIM", false);
			ulimrbv = channelManager.createChannel(recordName + ":ULIM:RBV", false);
			llim = channelManager.createChannel(recordName + ":LLIM", false);
			llimrbv = channelManager.createChannel(recordName + ":LLIM:RBV", false);
			// acknowledge that creation phase is completed
			channelManager.creationPhaseCompleted();
			configured = true;
		} catch (Throwable th) {
			throw new FactoryException("failed to create all channels", th);
		}
	}

	/**
	 * create channel access implementing phase II beamline EPICS interfaces.
	 *
	 * @param etlConfig
	 * @throws FactoryException
	 */
	private void createChannelAccess(ETLdetectorType etlConfig) throws FactoryException {
		try {
			ctrl = channelManager.createChannel(etlConfig.getHV().getPv(), false);
			ctrlrbv = channelManager.createChannel(etlConfig.getHVRBV().getPv(), false);
			hvadc = channelManager.createChannel(etlConfig.getHVADC().getPv(), false);
			ulim = channelManager.createChannel(etlConfig.getULIM().getPv(), false);
			ulimrbv = channelManager.createChannel(etlConfig.getULIMRBV().getPv(), false);
			llim = channelManager.createChannel(etlConfig.getLLIM().getPv(), false);
			llimrbv = channelManager.createChannel(etlConfig.getLLIMRBV().getPv(), false);
			// acknowledge that creation phase is completed
			channelManager.creationPhaseCompleted();
			configured = true;
		} catch (Throwable th) {
			throw new FactoryException("failed to create all channels", th);
		}
	}

	/**
	 * Set HV Control voltage setting in mV to ETL Detector. Setting the HV above 2000V will cause damage to the HV
	 * circuit. The pmt maximum voltage rating may restrict this limit further.
	 *
	 * @param hv
	 * @throws CAException
	 * @throws OutOfRangeException
	 * @throws InterruptedException
	 */
	public void setHighVoltage(int hv) throws CAException, OutOfRangeException, InterruptedException {
		if (hv > 2000000 || hv < 0) {
			logger.error("The HV setting value is outside the permitted range 0 - 2000 Volt.");
			throw new OutOfRangeException("Voltage value " + hv + " is outside the permissible range 0 - 2000 Volt.");
		}
		controller.caput(ctrl, hv, channelManager);

	}

	/**
	 * Get HV control voltage setting from the ETL Detector. This returns the contents of the control register, not the
	 * actual HV voltage.
	 *
	 * @return int
	 * @throws CAException
	 * @throws TimeoutException
	 * @throws InterruptedException
	 */
	public int getHighVoltage() throws CAException, TimeoutException, InterruptedException {
		return controller.cagetInt(ctrlrbv);
	}

	/**
	 * Set the upper threshold in mV.
	 *
	 * @param hv
	 * @throws CAException
	 * @throws InterruptedException
	 */
	public void setUpperLimit(int hv) throws CAException, InterruptedException {
		controller.caput(ulim, hv, channelManager);
	}

	/**
	 * Get the Upper Threshold in mV. This is the setting value stored in the control register, not a measure of the
	 * actual voltage.
	 *
	 * @return int
	 * @throws CAException
	 * @throws TimeoutException
	 * @throws InterruptedException
	 */
	public int getUpperLimit() throws CAException, TimeoutException, InterruptedException {
		return controller.cagetInt(ulimrbv);
	}

	/**
	 * Set the lower threshold in mV for the detector.
	 *
	 * @param hv
	 * @throws CAException
	 * @throws InterruptedException
	 */
	public void setLowerLimit(int hv) throws CAException, InterruptedException {
		controller.caput(llim, hv, channelManager);
	}

	/**
	 * Get the lower threshold in mV. This is the setting value stored in the control register, NOT a measure of the
	 * actual voltage.
	 *
	 * @return int
	 * @throws CAException
	 * @throws TimeoutException
	 * @throws InterruptedException
	 */
	public int getLowerLimit() throws CAException, TimeoutException, InterruptedException {
		return controller.cagetInt(llimrbv);
	}

	/**
	 * Get the actual HV output voltage. This reads ADC channel 3.
	 *
	 * @return int
	 * @throws CAException
	 * @throws TimeoutException
	 * @throws InterruptedException
	 */
	public int getActualHVOutput() throws CAException, TimeoutException, InterruptedException {
		return controller.cagetInt(hvadc);
	}

	@Override
	public void initializationCompleted() {
		logger.info("ETL detector controller - " + getName() + " initialised.");
	}

	/**
	 * @return EPICS record name
	 */
	public String getEpicsRecordName() {
		return epicsRecordName;
	}

	/**
	 * @param epicsRecordName
	 */
	public void setEpicsRecordName(String epicsRecordName) {
		this.epicsRecordName = epicsRecordName;
	}

	/**
	 * gets the EPICS short name
	 * @return device name
	 */
	public String getDeviceName() {
		return deviceName;
	}

	/**
	 * sets the EPICS short name
	 * @param deviceName
	 */
	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;

	}

	public void setPvRoot(String pv) {
		this.pvRoot=pv;

	}

}
