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

import org.springframework.beans.factory.InitializingBean;

import uk.ac.gda.devices.excalibur.ChipAnper;

/**
 *
 */
public class DummyMpxiiiChipRegAnper implements ChipAnper, InitializingBean {

	private int cas;
	private int tprefA;
	private int tprefB;
	private int threshold0;
	private int threshold1;
	private int threshold2;
	private int threshold3;
	private int threshold4;
	private int threshold5;
	private int threshold6;
	private int threshold7;
	private int fbk;
	private int gnd;
	private int rpz;
	private int tpBufferOut;
	private int tpBufferIn;
	private int delay;
	private int dacPixel;
	private int thresholdn;
	private int discls;
	private int disc;
	private int shaper;
	private int iKrum;
	private int preamp;
	private int tpref;

	@Override
	public int getPreamp() throws Exception {
		return preamp;
	}

	@Override
	public void setPreamp(int preamp) throws Exception {
		this.preamp = preamp;
	}

	@Override
	public int getIkrum() throws Exception {
		return iKrum;
	}

	@Override
	public void setIkrum(int ikrum) throws Exception {
		this.iKrum = ikrum;

	}

	@Override
	public int getShaper() throws Exception {
		return shaper;
	}

	@Override
	public void setShaper(int shaper) throws Exception {
		this.shaper = shaper;

	}

	@Override
	public int getDisc() throws Exception {
		return disc;
	}

	@Override
	public void setDisc(int disc) throws Exception {
		this.disc = disc;

	}

	@Override
	public int getDiscls() throws Exception {
		return discls;
	}

	@Override
	public void setDiscls(int discls) throws Exception {
		this.discls = discls;

	}

	@Override
	public int getThresholdn() throws Exception {
		return thresholdn;
	}

	@Override
	public void setThresholdn(int thresholdn) throws Exception {
		this.thresholdn = thresholdn;

	}

	@Override
	public int getDacPixel() throws Exception {
		return dacPixel;
	}

	@Override
	public void setDacPixel(int dacPixel) throws Exception {
		this.dacPixel = dacPixel;

	}

	@Override
	public int getDelay() throws Exception {
		return delay;
	}

	@Override
	public void setDelay(int delay) throws Exception {
		this.delay = delay;

	}

	@Override
	public int getTpBufferIn() throws Exception {
		return tpBufferIn;
	}

	@Override
	public void setTpBufferIn(int tpBufferIn) throws Exception {
		this.tpBufferIn = tpBufferIn;

	}

	@Override
	public int getTpBufferOut() throws Exception {
		return tpBufferOut;
	}

	@Override
	public void setTpBufferOut(int tpBufferOut) throws Exception {
		this.tpBufferOut = tpBufferOut;

	}

	@Override
	public int getRpz() throws Exception {
		return rpz;
	}

	@Override
	public void setRpz(int rpz) throws Exception {
		this.rpz = rpz;

	}

	@Override
	public int getGnd() throws Exception {
		return gnd;
	}

	@Override
	public void setGnd(int gnd) throws Exception {
		this.gnd = gnd;

	}

	@Override
	public int getTpref() throws Exception {
		return tpref;
	}

	@Override
	public void setTpref(int tpref) throws Exception {
		this.tpref = tpref;
	}

	@Override
	public int getFbk() throws Exception {
		return fbk;
	}

	@Override
	public void setFbk(int fbk) throws Exception {
		this.fbk = fbk;

	}

	@Override
	public int getCas() throws Exception {
		return cas;
	}

	@Override
	public void setCas(int cas) throws Exception {
		this.cas = cas;
	}

	@Override
	public int getTprefA() throws Exception {
		return tprefA;
	}

	@Override
	public void setTprefA(int tprefA) throws Exception {
		this.tprefA = tprefA;

	}

	@Override
	public int getTprefB() throws Exception {
		return tprefB;
	}

	@Override
	public void setTprefB(int tprefB) throws Exception {
		this.tprefB = tprefB;

	}


	@Override
	public int getThreshold0() throws Exception {
		return threshold0;
	}

	@Override
	public void setThreshold0(int threshold) throws Exception {
		this.threshold0 = threshold;
	}

	@Override
	public int getThreshold1() throws Exception {
		return threshold1;
	}

	@Override
	public void setThreshold1(int threshold) throws Exception {
		this.threshold1 = threshold;
	}

	@Override
	public int getThreshold2() throws Exception {
		return threshold2;
	}

	@Override
	public void setThreshold2(int threshold) throws Exception {
		this.threshold2 = threshold;
	}

	@Override
	public int getThreshold3() throws Exception {
		return threshold3;
	}

	@Override
	public void setThreshold3(int threshold) throws Exception {
		this.threshold3 = threshold;
	}

	@Override
	public int getThreshold4() throws Exception {
		return threshold4;
	}

	@Override
	public void setThreshold4(int threshold) throws Exception {
		this.threshold4 = threshold;
	}

	@Override
	public int getThreshold5() throws Exception {
		return threshold5;
	}

	@Override
	public void setThreshold5(int threshold) throws Exception {
		this.threshold5 = threshold;
	}

	@Override
	public int getThreshold6() throws Exception {
		return threshold6;
	}

	@Override
	public void setThreshold6(int threshold) throws Exception {
		this.threshold6 = threshold;
	}

	@Override
	public int getThreshold7() throws Exception {
		return threshold7;
	}

	@Override
	public void setThreshold7(int threshold) throws Exception {
		this.threshold7 = threshold;
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
	}

}
