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

import gda.device.DeviceBase;
import gda.epics.connection.EpicsChannelManager;
import gda.epics.connection.EpicsController;
import gda.epics.connection.InitializationListener;
import gda.factory.FactoryException;
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

	private EpicsChannelManager channelManager;

	private EpicsController controller;

	private String pvRoot;

	/**
	 * The Constructor.
	 */
	public EpicsETLController() {
		channelManager = new EpicsChannelManager(this);
		controller = EpicsController.getInstance();
	}

	@Override
	public void configure() throws FactoryException {
		if (!isConfigured()) {
			if (getPvRoot() == null) {
				logger.error("Missing EPICS interface configuration for the ETL sintillator detector " + getName());
				throw new FactoryException("Missing EPICS interface configuration for the ETL sintillator detectorr "
						+ getName());
			}
			createChannelAccess(getPvRoot());
			channelManager.tryInitialize(100);
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
			setConfigured(true);
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
	 * @throws IllegalArgumentException if voltage is outside permissable range
	 * @throws InterruptedException
	 */
	public void setHighVoltage(int hv) throws CAException, InterruptedException {
		if (hv > 2000000 || hv < 0) {
			logger.error("The HV setting value is outside the permitted range 0 - 2000 Volt.");
			throw new IllegalArgumentException("Voltage value " + hv + " is outside the permissible range 0 - 2000 Volt.");
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

	public String getPvRoot() {
		return pvRoot;
	}

}
