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

import uk.ac.gda.devices.excalibur.ChipPixel;

/**
 *
 */
public class DummyMpxiiiChipRegPixel implements ChipPixel, InitializingBean {

	

	private short[] mask;
	private short[] test;
	private short[] gainMode;
	private short[] thresholdA;
	private short[] thresholdB;

	int size=512*512;
	
	@Override
	public short[] getMask() throws Exception {
		return mask;
	}

	@Override
	public void setMask(short[] mask) throws Exception {
		this.mask = mask;

	}

	@Override
	public short[] getTest() throws Exception {
		return test;
	}

	@Override
	public void setTest(short[] test) throws Exception {
		this.test = test;
	}

	@Override
	public short[] getGainMode() throws Exception {
		return gainMode;
	}

	@Override
	public void setGainMode(short[] gainMode) throws Exception {
		this.gainMode = gainMode;
	}

	@Override
	public short[] getThresholdA() throws Exception {
		return thresholdA;
	}

	@Override
	public void setThresholdA(short[] thresholdA) throws Exception {
		this.thresholdA = thresholdA;

	}

	@Override
	public short[] getThresholdB() throws Exception {
		return thresholdB;
	}

	@Override
	public void setThresholdB(short[] thresholdB) throws Exception {
		this.thresholdB = thresholdB;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		mask= new short[size];
		test= new short[size];
		gainMode= new short[size];
		thresholdA= new short[size];
		thresholdB= new short[size];		
	}


}
