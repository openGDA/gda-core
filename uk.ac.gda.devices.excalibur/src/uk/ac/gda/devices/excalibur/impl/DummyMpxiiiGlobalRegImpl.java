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

import uk.ac.gda.devices.excalibur.MpxiiiGlobalReg;

/**
 *
 */
public class DummyMpxiiiGlobalRegImpl implements MpxiiiGlobalReg {
	private double dacNumber;
	private double dacNameCalc1;
	private int dacNameSel1;
	private double dacNameCalc2;
	private double dacNameCalc3;
	private int dacNameSel2;
	private int dacNameSel3;
	private String dacName="dacName";
	private int colourMode;
	private String colourModeString="colourModeString";
	private int counterDepth;
	private String counterDepthAsString;

	@Override
	public double getDacNumber() throws Exception {
		return dacNumber;
	}

	@Override
	public void setDacNumber(double dacNumber) throws Exception {
		this.dacNumber = dacNumber;

	}

	@Override
	public double getDacNameCalc1() throws Exception {
		return dacNameCalc1;
	}

	@Override
	public void setDacNameCalc1(double dacNameCalc1) throws Exception {
		this.dacNameCalc1 = dacNameCalc1;
	}

	@Override
	public double getDacNameCalc2() throws Exception {
		return dacNameCalc2;
	}

	@Override
	public void setDacNameCalc2(double dacNameCalc2) throws Exception {
		this.dacNameCalc2 = dacNameCalc2;

	}

	@Override
	public double getDacNameCalc3() throws Exception {
		return dacNameCalc3;
	}

	@Override
	public void setDacNameCalc3(double dacNameCalc3) throws Exception {
		this.dacNameCalc3 = dacNameCalc3;
	}

	@Override
	public int getDacNameSel1() throws Exception {
		return dacNameSel1;
	}

	@Override
	public void setDacNameSel1(int dacNameSel1) throws Exception {
		this.dacNameSel1 = dacNameSel1;
	}

	@Override
	public int getDacNameSel2() throws Exception {
		return dacNameSel2;
	}

	@Override
	public void setDacNameSel2(int dacNameSel2) throws Exception {
		this.dacNameSel2 = dacNameSel2;
	}

	@Override
	public int getDacNameSel3() throws Exception {
		return dacNameSel3;
	}

	@Override
	public void setDacNameSel3(int dacNameSel3) throws Exception {
		this.dacNameSel3 = dacNameSel3;
	}

	@Override
	public String getDacName() throws Exception {
		return dacName;
	}

	@Override
	public void setDacName(String dacName) throws Exception {
		this.dacName = dacName;
	}

	@Override
	public int getColourMode() throws Exception {
		return colourMode;
	}

	@Override
	public String getColourModeAsString() throws Exception {
		return colourModeString;
	}

	@Override
	public void setColourMode(int index) throws Exception {
		this.colourMode = index;
	}

	@Override
	public void setColourModeAsString(String colourMode) throws Exception {
		colourModeString = colourMode;
	}

	@Override
	public String[] getColourModeLabels() throws Exception {
		return new String[] { "colourZero", "colourOne" };
	}

	@Override
	public int getCounterDepth() throws Exception {
		// returns something of the form 2x 12 bit
		return counterDepth;
	}

	@Override
	public String getCounterDepthAsString() throws Exception {
		return counterDepthAsString;
	}

	@Override
	public void setCounterDepthAsString(String counterDepth) throws Exception {
		counterDepthAsString = counterDepth;
	}

	@Override
	public void setCounterDepth(int counterDepth) throws Exception {
		this.counterDepth = counterDepth;
	}

	@Override
	public String[] getCounterDepthLabels() throws Exception {
		return new String[] { "zeroState", "oneState", "twoState", "threeState" };

	}

}
