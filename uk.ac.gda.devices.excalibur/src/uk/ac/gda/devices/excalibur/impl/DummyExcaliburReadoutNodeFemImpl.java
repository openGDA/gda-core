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

import gda.device.detector.areadetector.v17.ADBase;
import gda.device.detector.areadetector.v17.impl.ADBaseSimulator;
import gov.aps.jca.CAException;
import gov.aps.jca.TimeoutException;
import uk.ac.gda.devices.excalibur.ExcaliburReadoutNodeFem;
import uk.ac.gda.devices.excalibur.MpxiiiChipReg;

/**
 *
 */
public class DummyExcaliburReadoutNodeFemImpl extends ADBaseSimulator implements ExcaliburReadoutNodeFem, ADBase {

	private MpxiiiChipReg mpxiiiChipReg1;
	private MpxiiiChipReg mpxiiiChipReg2;
	private MpxiiiChipReg mpxiiiChipReg3;
	private MpxiiiChipReg mpxiiiChipReg4;
	private MpxiiiChipReg mpxiiiChipReg5;
	private MpxiiiChipReg mpxiiiChipReg6;
	private MpxiiiChipReg mpxiiiChipReg7;
	private MpxiiiChipReg mpxiiiChipReg8;
	private int counterDepth;
	@Override
	public int getCounterDepth() throws TimeoutException, CAException, InterruptedException, Exception {
		return counterDepth;
	}

	@Override
	public void setCounterDepth(int counterDepth) throws CAException, InterruptedException, Exception {
		this.counterDepth = counterDepth;
	}

	@Override
	public MpxiiiChipReg getMpxiiiChipReg1() {
		return mpxiiiChipReg1;
	}

	@Override
	public MpxiiiChipReg getMpxiiiChipReg2() {
		return mpxiiiChipReg2;
	}

	@Override
	public MpxiiiChipReg getMpxiiiChipReg3() {
		return mpxiiiChipReg3;
	}

	@Override
	public MpxiiiChipReg getMpxiiiChipReg4() {
		return mpxiiiChipReg4;
	}

	@Override
	public MpxiiiChipReg getMpxiiiChipReg5() {
		return mpxiiiChipReg5;
	}

	@Override
	public MpxiiiChipReg getMpxiiiChipReg6() {
		return mpxiiiChipReg6;
	}

	@Override
	public MpxiiiChipReg getMpxiiiChipReg7() {
		return mpxiiiChipReg7;
	}

	@Override
	public MpxiiiChipReg getMpxiiiChipReg8() {
		return mpxiiiChipReg8;
	}

	public void setMpxiiiChipReg1(MpxiiiChipReg mpxiiiChipReg1) {
		this.mpxiiiChipReg1 = mpxiiiChipReg1;
	}

	public void setMpxiiiChipReg2(MpxiiiChipReg mpxiiiChipReg2) {
		this.mpxiiiChipReg2 = mpxiiiChipReg2;
	}

	public void setMpxiiiChipReg3(MpxiiiChipReg mpxiiiChipReg3) {
		this.mpxiiiChipReg3 = mpxiiiChipReg3;
	}

	public void setMpxiiiChipReg4(MpxiiiChipReg mpxiiiChipReg4) {
		this.mpxiiiChipReg4 = mpxiiiChipReg4;
	}

	public void setMpxiiiChipReg5(MpxiiiChipReg mpxiiiChipReg5) {
		this.mpxiiiChipReg5 = mpxiiiChipReg5;
	}

	public void setMpxiiiChipReg6(MpxiiiChipReg mpxiiiChipReg6) {
		this.mpxiiiChipReg6 = mpxiiiChipReg6;
	}

	public void setMpxiiiChipReg7(MpxiiiChipReg mpxiiiChipReg7) {
		this.mpxiiiChipReg7 = mpxiiiChipReg7;
	}

	public void setMpxiiiChipReg8(MpxiiiChipReg mpxiiiChipReg8) {
		this.mpxiiiChipReg8 = mpxiiiChipReg8;
	}

	/**
	 * 
	 * @param index
	 * @return the {@link MpxiiiChipReg} for the provided index
	 */
	@Override
	public MpxiiiChipReg getIndexedMpxiiiChipReg(int index) {
		switch (index) {
		case 1:
			return mpxiiiChipReg1;
		case 2:
			return mpxiiiChipReg2;
		case 3:
			return mpxiiiChipReg3;
		case 4:
			return mpxiiiChipReg4;
		case 5:
			return mpxiiiChipReg5;
		case 6:
			return mpxiiiChipReg6;
		case 7:
			return mpxiiiChipReg7;
		case 8:
			return mpxiiiChipReg8;

		}
		throw new IllegalArgumentException("Invalid chip register");
	}
}
