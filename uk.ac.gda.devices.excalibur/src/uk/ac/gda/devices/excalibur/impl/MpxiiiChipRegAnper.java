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

import uk.ac.gda.devices.excalibur.ChipAnper;

/**
 *
 */
public class MpxiiiChipRegAnper implements ChipAnper, InitializingBean {

	/**
	 * Map that stores the channel against the PV name
	 */
	private Map<String, Channel> channelMap = new HashMap<String, Channel>();

	/**
	 * EPICS Controller
	 */
	private final static EpicsController EPICS_CONTROLLER = EpicsController.getInstance();

	/**
	 * Constants holding PV names that follow the base PV names.
	 */
	private static final String TPREFB = "TpRefB";

	private static final String TPREFA = "TpRefA";

	private static final String CAS = "Cas";

	private static final String FBK = "Fbk";

	private static final String TPREF = "TpRef";

	private static final String GND = "Gnd";

	private static final String RPZ = "Rpz";

	private static final String TPBUFFEROUT = "TpBufferOut";

	private static final String TPBUFFERIN = "TpBufferIn";

	private static final String DELAY = "Delay";

	private static final String DACPIXEL = "DacPixel";

	private static final String THRESHOLDN = "ThresholdN";

	private static final String DISC = "Disc";

	private static final String SHAPER = "Shaper";

	private static final String IKRUM = "Ikrum";

	private static final String PREAMP = "Preamp";

	private static final String DISCLS = "Discls";

	private static final String THRESHOLD_0 = "Threshold:0";

	private static final String THRESHOLD_1 = "Threshold:1";

	private static final String THRESHOLD_2 = "Threshold:2";

	private static final String THRESHOLD_3 = "Threshold:3";

	private static final String THRESHOLD_4 = "Threshold:4";

	private static final String THRESHOLD_5 = "Threshold:5";

	private static final String THRESHOLD_6 = "Threshold:6";

	private static final String THRESHOLD_7 = "Threshold:7";

	/**
	 * Base PV Name passed in from the spring configuration
	 */
	private String basePVName;

	@Override
	public int getPreamp() throws Exception {
		return EPICS_CONTROLLER.cagetInt(getChannel(PREAMP));
	}

	@Override
	public void setPreamp(int preamp) throws Exception {
		EPICS_CONTROLLER.caput(getChannel(PREAMP), preamp);
	}

	@Override
	public int getIkrum() throws Exception {
		return EPICS_CONTROLLER.cagetInt(getChannel(IKRUM));
	}

	@Override
	public void setIkrum(int ikrum) throws Exception {
		EPICS_CONTROLLER.caput(getChannel(IKRUM), ikrum);

	}

	@Override
	public int getShaper() throws Exception {
		return EPICS_CONTROLLER.cagetInt(getChannel(SHAPER));
	}

	@Override
	public void setShaper(int shaper) throws Exception {
		EPICS_CONTROLLER.caput(getChannel(SHAPER), shaper);

	}

	@Override
	public int getDisc() throws Exception {
		return EPICS_CONTROLLER.cagetInt(getChannel(DISC));
	}

	@Override
	public void setDisc(int disc) throws Exception {
		EPICS_CONTROLLER.caput(getChannel(DISC), disc);

	}

	@Override
	public int getDiscls() throws Exception {
		return EPICS_CONTROLLER.cagetInt(getChannel(DISCLS));
	}

	@Override
	public void setDiscls(int discls) throws Exception {
		EPICS_CONTROLLER.caput(getChannel(DISCLS), discls);

	}

	@Override
	public int getThresholdn() throws Exception {
		return EPICS_CONTROLLER.cagetInt(getChannel(THRESHOLDN));
	}

	@Override
	public void setThresholdn(int thresholdn) throws Exception {
		EPICS_CONTROLLER.caput(getChannel(THRESHOLDN), thresholdn);

	}

	@Override
	public int getDacPixel() throws Exception {
		return EPICS_CONTROLLER.cagetInt(getChannel(DACPIXEL));
	}

	@Override
	public void setDacPixel(int dacPixel) throws Exception {
		EPICS_CONTROLLER.caput(getChannel(DACPIXEL), dacPixel);

	}

	@Override
	public int getDelay() throws Exception {
		return EPICS_CONTROLLER.cagetInt(getChannel(DELAY));
	}

	@Override
	public void setDelay(int delay) throws Exception {
		EPICS_CONTROLLER.caput(getChannel(DELAY), delay);

	}

	@Override
	public int getTpBufferIn() throws Exception {
		return EPICS_CONTROLLER.cagetInt(getChannel(TPBUFFERIN));
	}

	@Override
	public void setTpBufferIn(int tpBufferIn) throws Exception {
		EPICS_CONTROLLER.caput(getChannel(TPBUFFERIN), tpBufferIn);

	}

	@Override
	public int getTpBufferOut() throws Exception {
		return EPICS_CONTROLLER.cagetInt(getChannel(TPBUFFEROUT));
	}

	@Override
	public void setTpBufferOut(int tpBufferOut) throws Exception {
		EPICS_CONTROLLER.caput(getChannel(TPBUFFEROUT), tpBufferOut);

	}

	@Override
	public int getRpz() throws Exception {
		return EPICS_CONTROLLER.cagetInt(getChannel(RPZ));
	}

	@Override
	public void setRpz(int rpz) throws Exception {
		EPICS_CONTROLLER.caput(getChannel(RPZ), rpz);

	}

	@Override
	public int getGnd() throws Exception {
		return EPICS_CONTROLLER.cagetInt(getChannel(GND));
	}

	@Override
	public void setGnd(int gnd) throws Exception {
		EPICS_CONTROLLER.caput(getChannel(GND), gnd);

	}

	@Override
	public int getTpref() throws Exception {
		return EPICS_CONTROLLER.cagetInt(getChannel(TPREF));
	}

	@Override
	public void setTpref(int tpref) throws Exception {
		EPICS_CONTROLLER.caput(getChannel(TPREF), tpref);
	}

	@Override
	public int getFbk() throws Exception {
		return EPICS_CONTROLLER.cagetInt(getChannel(FBK));
	}

	@Override
	public void setFbk(int fbk) throws Exception {
		EPICS_CONTROLLER.caput(getChannel(FBK), fbk);

	}

	@Override
	public int getCas() throws Exception {
		return EPICS_CONTROLLER.cagetInt(getChannel(CAS));
	}

	@Override
	public void setCas(int cas) throws Exception {
		EPICS_CONTROLLER.caput(getChannel(CAS), cas);

	}

	@Override
	public int getTprefA() throws Exception {
		return EPICS_CONTROLLER.cagetInt(getChannel(TPREFA));
	}

	@Override
	public void setTprefA(int tprefA) throws Exception {
		EPICS_CONTROLLER.caput(getChannel(TPREFA), tprefA);

	}

	@Override
	public int getTprefB() throws Exception {
		return EPICS_CONTROLLER.cagetInt(getChannel(TPREFB));
	}

	@Override
	public void setTprefB(int tprefB) throws Exception {
		EPICS_CONTROLLER.caput(getChannel(TPREFB), tprefB);

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

	private Channel getChannel(String pvPostFix) throws CAException, TimeoutException {
		String fullPvName = basePVName + pvPostFix;
		Channel channel = channelMap.get(fullPvName);
		if (channel == null) {
			channel = EPICS_CONTROLLER.createChannel(fullPvName);
			channelMap.put(fullPvName, channel);
		}
		return channel;
	}

	@Override
	public int getThreshold0() throws Exception {
		return EPICS_CONTROLLER.cagetInt(getChannel(THRESHOLD_0));
	}

	@Override
	public void setThreshold0(int threshold) throws Exception {
		EPICS_CONTROLLER.caput(getChannel(THRESHOLD_0), threshold);
	}

	@Override
	public int getThreshold1() throws Exception {
		return EPICS_CONTROLLER.cagetInt(getChannel(THRESHOLD_1));
	}

	@Override
	public void setThreshold1(int threshold) throws Exception {
		EPICS_CONTROLLER.caput(getChannel(THRESHOLD_1), threshold);
	}

	@Override
	public int getThreshold2() throws Exception {
		return EPICS_CONTROLLER.cagetInt(getChannel(THRESHOLD_2));
	}

	@Override
	public void setThreshold2(int threshold) throws Exception {
		EPICS_CONTROLLER.caput(getChannel(THRESHOLD_2), threshold);
	}

	@Override
	public int getThreshold3() throws Exception {
		return EPICS_CONTROLLER.cagetInt(getChannel(THRESHOLD_3));
	}

	@Override
	public void setThreshold3(int threshold) throws Exception {
		EPICS_CONTROLLER.caput(getChannel(THRESHOLD_3), threshold);
	}

	@Override
	public int getThreshold4() throws Exception {
		return EPICS_CONTROLLER.cagetInt(getChannel(THRESHOLD_4));
	}

	@Override
	public void setThreshold4(int threshold) throws Exception {
		EPICS_CONTROLLER.caput(getChannel(THRESHOLD_4), threshold);
	}

	@Override
	public int getThreshold5() throws Exception {
		return EPICS_CONTROLLER.cagetInt(getChannel(THRESHOLD_5));
	}

	@Override
	public void setThreshold5(int threshold) throws Exception {
		EPICS_CONTROLLER.caput(getChannel(THRESHOLD_5), threshold);
	}

	@Override
	public int getThreshold6() throws Exception {
		return EPICS_CONTROLLER.cagetInt(getChannel(THRESHOLD_6));
	}

	@Override
	public void setThreshold6(int threshold) throws Exception {
		EPICS_CONTROLLER.caput(getChannel(THRESHOLD_6), threshold);
	}

	@Override
	public int getThreshold7() throws Exception {
		return EPICS_CONTROLLER.cagetInt(getChannel(THRESHOLD_7));
	}

	@Override
	public void setThreshold7(int threshold) throws Exception {
		EPICS_CONTROLLER.caput(getChannel(THRESHOLD_7), threshold);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (basePVName == null) {
			throw new IllegalArgumentException("'basePVName' needs to be declared");
		}
	}
}
