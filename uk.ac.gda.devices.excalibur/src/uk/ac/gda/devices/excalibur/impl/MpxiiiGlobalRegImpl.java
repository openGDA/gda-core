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

import uk.ac.gda.devices.excalibur.MpxiiiGlobalReg;

/**
 *
 */
public class MpxiiiGlobalRegImpl implements MpxiiiGlobalReg, InitializingBean {
	private static final String COUNTERDEPTH = "COUNTERDEPTH";
	
	private static final String COUNTERDEPTH_THST = COUNTERDEPTH + ".THST";

	private static final String COUNTERDEPTH_TWST = COUNTERDEPTH + ".TWST";

	private static final String COUNTERDEPTH_ONST = COUNTERDEPTH + ".ONST";

	private static final String COUNTERDEPTH_ZRST = COUNTERDEPTH + ".ZRST";

	private static final String COLOURMODE = "COLOURMODE";

	private static final String COLOURMODE_ONAM = COLOURMODE + ".ONAM";

	private static final String COLOURMODE_ZNAM = COLOURMODE + ".ZNAM";

	private static final String DACNAME = "DACNAME";

	private static final String DACNAMESEL3 = "DACNAMESEL3";

	private static final String DACNAMESEL2 = "DACNAMESEL2";

	private static final String DACNAMESEL1 = "DACNAMESEL1";

	private static final String DACNAMECALC3 = "DACNAMECALC3";

	private static final String DACNAMECALC2 = "DACNAMECALC2";

	private static final String DACNAMECALC1 = "DACNAMECALC1";

	private static final String DACNUMBER = "DACNUMBER";

	/**
	 * Map that stores the channel against the PV name
	 */
	private Map<String, Channel> channelMap = new HashMap<String, Channel>();

	private final static EpicsController EPICS_CONTROLLER = EpicsController.getInstance();

	private String basePVName;

	@Override
	public double getDacNumber() throws Exception {
		return EPICS_CONTROLLER.cagetDouble(getChannel(DACNUMBER));
	}

	@Override
	public void setDacNumber(double dacNumber) throws Exception {
		EPICS_CONTROLLER.caput(getChannel(DACNUMBER), dacNumber);

	}

	@Override
	public double getDacNameCalc1() throws Exception {
		return EPICS_CONTROLLER.cagetDouble(getChannel(DACNAMECALC1));
	}

	@Override
	public void setDacNameCalc1(double dacNameCalc1) throws Exception {
		EPICS_CONTROLLER.caput(getChannel(DACNAMECALC1), dacNameCalc1);
	}

	@Override
	public double getDacNameCalc2() throws Exception {
		return EPICS_CONTROLLER.cagetDouble(getChannel(DACNAMECALC2));
	}

	@Override
	public void setDacNameCalc2(double dacNameCalc2) throws Exception {
		EPICS_CONTROLLER.caput(getChannel(DACNAMECALC2), dacNameCalc2);

	}

	@Override
	public double getDacNameCalc3() throws Exception {
		return EPICS_CONTROLLER.cagetDouble(getChannel(DACNAMECALC3));
	}

	@Override
	public void setDacNameCalc3(double dacNameCalc3) throws Exception {
		EPICS_CONTROLLER.caput(getChannel(DACNAMECALC3), dacNameCalc3);
	}

	@Override
	public int getDacNameSel1() throws Exception {
		return EPICS_CONTROLLER.cagetInt(getChannel(DACNAMESEL1));
	}

	@Override
	public void setDacNameSel1(int dacNameSel1) throws Exception {
		EPICS_CONTROLLER.caput(getChannel(DACNAMESEL1), dacNameSel1);
	}

	@Override
	public int getDacNameSel2() throws Exception {
		return EPICS_CONTROLLER.cagetInt(getChannel(DACNAMESEL2));
	}

	@Override
	public void setDacNameSel2(int dacNameSel2) throws Exception {
		EPICS_CONTROLLER.caput(getChannel(DACNAMESEL2), dacNameSel2);
	}

	@Override
	public int getDacNameSel3() throws Exception {
		return EPICS_CONTROLLER.cagetInt(getChannel(DACNAMESEL3));
	}

	@Override
	public void setDacNameSel3(int dacNameSel3) throws Exception {
		EPICS_CONTROLLER.caput(getChannel(DACNAMESEL3), dacNameSel3);
	}

	@Override
	public String getDacName() throws Exception {
		return EPICS_CONTROLLER.caget(getChannel(DACNAME));
	}

	@Override
	public void setDacName(String dacName) throws Exception {
		EPICS_CONTROLLER.caput(getChannel(DACNAME), dacName);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (basePVName == null) {
			throw new IllegalArgumentException("'basePVName' needs to be declared");
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

	@Override
	public int getColourMode() throws Exception {
		return EPICS_CONTROLLER.cagetInt(getChannel(COLOURMODE));
	}

	@Override
	public String getColourModeAsString() throws Exception {
		int colourMode = getColourMode();
		switch (colourMode) {
		case 0:
			return EPICS_CONTROLLER.caget(getChannel(COLOURMODE_ZNAM));
		case 1:
			return EPICS_CONTROLLER.caget(getChannel(COLOURMODE_ONAM));
		}
		return null;
	}

	@Override
	public void setColourMode(int index) throws Exception {
		EPICS_CONTROLLER.caput(getChannel(COLOURMODE), index);
	}

	@Override
	public void setColourModeAsString(String colourMode) throws Exception {
		int colourModeIndex = 0;
		for (; colourModeIndex < getColourModeLabels().length; colourModeIndex++) {
			if (colourMode.equals(getColourModeLabels()[colourModeIndex])) {
				setColourMode(colourModeIndex);
				return;
			}
		}
		throw new IllegalArgumentException("String does not match available Colour Modes");
	}

	@Override
	public String[] getColourModeLabels() throws Exception {
		String colourZero = EPICS_CONTROLLER.caget(getChannel(COLOURMODE_ZNAM));
		String colourOne = EPICS_CONTROLLER.caget(getChannel(COLOURMODE_ONAM));
		return new String[] { colourZero, colourOne };
	}

	@Override
	public int getCounterDepth() throws Exception {
		// returns something of the form 2x 12 bit
		return EPICS_CONTROLLER.cagetInt(getChannel(COUNTERDEPTH));
	}

	@Override
	public String getCounterDepthAsString() throws Exception {
		int counterDepthIndex = getCounterDepth();
		switch (counterDepthIndex) {
		case 0:
			return EPICS_CONTROLLER.caget(getChannel(COUNTERDEPTH_ZRST));
		case 1:
			return EPICS_CONTROLLER.caget(getChannel(COUNTERDEPTH_ONST));
		case 2:
			return EPICS_CONTROLLER.caget(getChannel(COUNTERDEPTH_TWST));
		case 3:
			return EPICS_CONTROLLER.caget(getChannel(COUNTERDEPTH_THST));
		}
		throw new IllegalArgumentException("Index out of bounds - no val found at that index");
	}

	@Override
	public void setCounterDepthAsString(String counterDepth) throws Exception {
		EPICS_CONTROLLER.caput(getChannel(COUNTERDEPTH), counterDepth);
	}

	@Override
	public void setCounterDepth(int counterDepth) throws Exception {
		EPICS_CONTROLLER.caput(getChannel(COUNTERDEPTH), counterDepth);
	}

	@Override
	public String[] getCounterDepthLabels() throws Exception {
		String zeroState = EPICS_CONTROLLER.caget(getChannel(COUNTERDEPTH_ZRST));
		String oneState = EPICS_CONTROLLER.caget(getChannel(COUNTERDEPTH_ONST));
		String twoState = EPICS_CONTROLLER.caget(getChannel(COUNTERDEPTH_TWST));
		String threeState = EPICS_CONTROLLER.caget(getChannel(COUNTERDEPTH_THST));

		return new String[] { zeroState, oneState, twoState, threeState };

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

}
