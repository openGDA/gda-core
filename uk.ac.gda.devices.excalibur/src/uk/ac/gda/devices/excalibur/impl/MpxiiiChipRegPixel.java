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
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.TimeoutException;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;

import uk.ac.gda.devices.excalibur.ChipPixel;

/**
 *
 */
public class MpxiiiChipRegPixel implements ChipPixel, InitializingBean {

	
	/**
	 * Map that stores the channel against the PV name
	 */
	private Map<String, Channel> channelMap = new HashMap<String, Channel>();

	private static final String THRESHOLDB = "ThresholdB";

	private static final String THRESHOLDA = "ThresholdA";

	private static final String GAINMODE = "GainMode";

	private static final String TEST = "Test";

	private static final String MASK = "Mask";

	private final static EpicsController EPICS_CONTROLLER = EpicsController.getInstance();

	private String basePVName;

	@Override
	public short[] getMask() throws Exception {
		return EPICS_CONTROLLER.cagetShortArray(getChannel(MASK));
	}

	@Override
	public void setMask(short[] mask) throws Exception {
		EPICS_CONTROLLER.caput(getChannel(MASK), mask);

	}

	@Override
	public short[] getTest() throws Exception {
		return EPICS_CONTROLLER.cagetShortArray(getChannel(TEST));
	}

	@Override
	public void setTest(short[] test) throws Exception {
		EPICS_CONTROLLER.caput(getChannel(TEST), test);
	}

	@Override
	public short[] getGainMode() throws Exception {
		return EPICS_CONTROLLER.cagetShortArray(getChannel(GAINMODE));
	}

	@Override
	public void setGainMode(short[] gainMode) throws Exception {
		EPICS_CONTROLLER.caput(getChannel(GAINMODE), gainMode);
	}

	@Override
	public short[] getThresholdA() throws Exception {
		return EPICS_CONTROLLER.cagetShortArray(getChannel(THRESHOLDA));
	}

	@Override
	public void setThresholdA(short[] thresholdA) throws Exception {
		EPICS_CONTROLLER.caput(getChannel(THRESHOLDA), thresholdA);

	}

	@Override
	public short[] getThresholdB() throws Exception {
		return EPICS_CONTROLLER.cagetShortArray(getChannel(THRESHOLDB));
	}

	@Override
	public void setThresholdB(short[] thresholdB) throws Exception {
		EPICS_CONTROLLER.caput(getChannel(THRESHOLDB), thresholdB);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (basePVName == null) {
			throw new IllegalArgumentException("'basePVName' needs to be declared");
		}
	}

	private Channel getChannel(String pvPostFix) throws CAException, TimeoutException {
		String fullPvName = basePVName + pvPostFix;
		Channel channel = channelMap.get(fullPvName);
		if (channel == null) {
			channel = EPICS_CONTROLLER.createChannel(fullPvName);
			channelMap.put(fullPvName, channel);
		}
		return channel;
	}

	/**
	 * @return Returns the basePVName.
	 */
	public String getBasePVName() {
		return basePVName;
	}

	/**
	 * @param basePVName The basePVName to set.
	 */
	public void setBasePVName(String basePVName) {
		this.basePVName = basePVName;
	}

	@Override
	public int getThresholdA_Length() throws Exception {
		return getChannel(THRESHOLDA).getElementCount();
	}

	@Override
	public int getMask_Length() throws Exception {
		return getChannel(MASK).getElementCount();
	}

	@Override
	public int getTest_Length() throws Exception {
		return getChannel(TEST).getElementCount();
	}

	@Override
	public int getGainMode_Length() throws Exception {
		return getChannel(GAINMODE).getElementCount();
	}

	@Override
	public int getThresholdB_Length() throws Exception {
		return getChannel(THRESHOLDB).getElementCount();
	}

}
