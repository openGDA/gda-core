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

import gda.device.detector.areadetector.IPVProvider;
import gda.epics.connection.EpicsController;
import gov.aps.jca.Channel;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;

import uk.ac.gda.devices.excalibur.ChipAnper;
import uk.ac.gda.devices.excalibur.MpxiiiChipReg;
import uk.ac.gda.devices.excalibur.ChipPixel;

/**
 *
 */
public class MpxiiiChipRegImpl implements MpxiiiChipReg, InitializingBean {
	private static final String DACEXTERNAL_NAME = "DacExternal:Name";

	private static final String DACEXTERNAL_DECODE = "DacExternal:Decode";

	private static final String DACEXTERNAL = "DacExternal";

	private static final String DACSENSE_NAME = "DacSense:Name";

	private static final String DACSENSE_DECODE = "DacSense:Decode";

	private static final String DACSENSE = "DacSense";

	/**
	 * Map that stores the channel against the PV name
	 */
	private Map<String, Channel> channelMap = new HashMap<String, Channel>();

	private ChipAnper anper;

	private ChipPixel pixel;

	private String basePVName;

	private IPVProvider pvProvider;

	private final static EpicsController EPICS_CONTROLLER = EpicsController.getInstance();

	@Override
	public int getDacSense() throws Exception {
		return EPICS_CONTROLLER.cagetInt(getChannel(DACSENSE));
	}

	@Override
	public void setDacSense(int dacSense) throws Exception {
		EPICS_CONTROLLER.caput(getChannel(DACSENSE), dacSense);
	}

	@Override
	public int getDacSenseDecode() throws Exception {
		return EPICS_CONTROLLER.cagetInt(getChannel(DACSENSE_DECODE));
	}

	@Override
	public void setDacSenseDecode(int dacSenseDecode) throws Exception {
		EPICS_CONTROLLER.caput(getChannel(DACSENSE_DECODE), dacSenseDecode);
	}

	@Override
	public String getDacSenseName() throws Exception {
		return EPICS_CONTROLLER.caget(getChannel(DACSENSE_NAME));
	}

	@Override
	public void setDacSenseName(String dacSenseName) throws Exception {
		EPICS_CONTROLLER.caput(getChannel(DACSENSE_NAME), dacSenseName);
	}

	@Override
	public int getDacExternal() throws Exception {
		return EPICS_CONTROLLER.cagetInt(getChannel(DACEXTERNAL));
	}

	@Override
	public void setDacExternal(int dacExternal) throws Exception {
		EPICS_CONTROLLER.caput(getChannel(DACEXTERNAL), dacExternal);
	}

	@Override
	public int getDacExternalDecode() throws Exception {
		return EPICS_CONTROLLER.cagetInt(getChannel(DACEXTERNAL_DECODE));
	}

	@Override
	public void setDacExternalDecode(int dacExternalDecode) throws Exception {
		EPICS_CONTROLLER.caput(getChannel(DACEXTERNAL_DECODE), dacExternalDecode);
	}

	@Override
	public String getDacExternalName() throws Exception {
		return EPICS_CONTROLLER.caget(getChannel(DACEXTERNAL_NAME));
	}

	@Override
	public void setDacExternalName(String dacExternalName) throws Exception {
		EPICS_CONTROLLER.caput(getChannel(DACEXTERNAL_NAME), dacExternalName);
	}

	@Override
	public ChipAnper getAnper() {
		return anper;
	}

	@Override
	public void setAnper(ChipAnper anper) {
		this.anper = anper;

	}

	@Override
	public ChipPixel getPixel() {
		return pixel;
	}

	@Override
	public void setPixel(ChipPixel pixel) {
		this.pixel = pixel;

	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (basePVName == null && pvProvider == null) {
			throw new IllegalArgumentException("'basePVName' or pvProvider needs to be declared");
		}

		if (pixel == null) {
			throw new IllegalArgumentException("'pixel' needs to be declared");
		}
		if (anper == null) {
			throw new IllegalArgumentException("'anper' needs to be declared");
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

	private Channel getChannel(String pvPostFix) throws Exception {
		String fullPvName;
		if (pvProvider != null) {
			fullPvName = pvProvider.getPV(pvPostFix);
		} else {
			fullPvName = basePVName + pvPostFix;
		}
		Channel channel = channelMap.get(fullPvName);
		if (channel == null) {
			channel = EPICS_CONTROLLER.createChannel(fullPvName);
			channelMap.put(fullPvName, channel);
		}
		return channel;
	}

	/**
	 * @return Returns the pvProvider.
	 */
	public IPVProvider getPvProvider() {
		return pvProvider;
	}

	/**
	 * @param pvProvider
	 *            The pvProvider to set.
	 */
	public void setPvProvider(IPVProvider pvProvider) {
		this.pvProvider = pvProvider;
	}
}
