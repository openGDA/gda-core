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

package uk.ac.gda.devices.excalibur.impl;

import gda.epics.connection.EpicsController;
import gda.factory.Findable;
import gov.aps.jca.Channel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import uk.ac.gda.devices.excalibur.IAdFem;

/**
 *
 */
public class AdFem implements IAdFem, Findable, InitializingBean {

	private final static EpicsController EPICS_CONTROLLER = EpicsController.getInstance();

	private static final String CALIBRATION = "Calibration";

	// Setup the logging facilities
	static final Logger logger = LoggerFactory.getLogger(AdFem.class);

	private String basePVName;

	private Channel calibrationChannel;

	@Override
	public short[] getCalibration() throws Exception {
		return EPICS_CONTROLLER.cagetShortArray(getCalibrationChannel());
	}

	@Override
	public void setCalibration(short[] calibration) throws Exception {
		EPICS_CONTROLLER.caput(getCalibrationChannel(), calibration);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (basePVName == null) {
			throw new IllegalArgumentException("'basePVName' not declared");
		}

	}

	/**
	 * @return Returns the basePVName.
	 */
	public String getBasePVName() {
		return basePVName;
	}

	/**
	 * @param basePVName
	 *            The basePVName to set.
	 */
	public void setBasePVName(String basePVName) {
		this.basePVName = basePVName;
	}

	String name;

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	public Channel getCalibrationChannel() throws Exception {
		if (calibrationChannel == null) {
			calibrationChannel = EPICS_CONTROLLER.createChannel(basePVName + CALIBRATION);
		}
		return calibrationChannel;
	}

}
