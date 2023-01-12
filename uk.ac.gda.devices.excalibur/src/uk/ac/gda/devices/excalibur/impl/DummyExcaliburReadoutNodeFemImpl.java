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

	private int counterDepth;
	private int dacSense;

	private int dacExternal;

	@Override
	public int getCounterDepth() throws TimeoutException, CAException, InterruptedException, Exception {
		return counterDepth;
	}

	@Override
	public void setCounterDepth(int counterDepth) throws CAException, InterruptedException, Exception {
		this.counterDepth = counterDepth;
	}

	private MpxiiiChipReg[] mpxiiiChipRegs = new MpxiiiChipReg[8];

	@Override
	public MpxiiiChipReg getMpxiiiChipReg1() {
		return getIndexedMpxiiiChipReg(0);
	}

	@Override
	public MpxiiiChipReg getMpxiiiChipReg2() {
		return getIndexedMpxiiiChipReg(1);
	}

	@Override
	public MpxiiiChipReg getMpxiiiChipReg3() {
		return getIndexedMpxiiiChipReg(2);
	}

	@Override
	public MpxiiiChipReg getMpxiiiChipReg4() {
		return getIndexedMpxiiiChipReg(3);
	}

	@Override
	public MpxiiiChipReg getMpxiiiChipReg5() {
		return getIndexedMpxiiiChipReg(4);
	}

	@Override
	public MpxiiiChipReg getMpxiiiChipReg6() {
		return getIndexedMpxiiiChipReg(5);
	}

	@Override
	public MpxiiiChipReg getMpxiiiChipReg7() {
		return getIndexedMpxiiiChipReg(6);
	}

	@Override
	public MpxiiiChipReg getMpxiiiChipReg8() {
		return getIndexedMpxiiiChipReg(7);
	}

	public void setMpxiiiChipReg1(MpxiiiChipReg mpxiiiChipReg) {
		this.mpxiiiChipRegs[0] = mpxiiiChipReg;
	}

	public void setMpxiiiChipReg2(MpxiiiChipReg mpxiiiChipReg) {
		this.mpxiiiChipRegs[1] = mpxiiiChipReg;
	}

	public void setMpxiiiChipReg3(MpxiiiChipReg mpxiiiChipReg) {
		this.mpxiiiChipRegs[2] = mpxiiiChipReg;
	}

	public void setMpxiiiChipReg4(MpxiiiChipReg mpxiiiChipReg) {
		this.mpxiiiChipRegs[3] = mpxiiiChipReg;
	}

	public void setMpxiiiChipReg5(MpxiiiChipReg mpxiiiChipReg) {
		this.mpxiiiChipRegs[4] = mpxiiiChipReg;
	}

	public void setMpxiiiChipReg6(MpxiiiChipReg mpxiiiChipReg) {
		this.mpxiiiChipRegs[5] = mpxiiiChipReg;
	}

	public void setMpxiiiChipReg7(MpxiiiChipReg mpxiiiChipReg) {
		this.mpxiiiChipRegs[6] = mpxiiiChipReg;
	}

	public void setMpxiiiChipReg8(MpxiiiChipReg mpxiiiChipReg) {
		this.mpxiiiChipRegs[7] = mpxiiiChipReg;
	}

	/**
	 * @param index
	 * @return the {@link MpxiiiChipReg} for the provided index
	 */
	@Override
	public MpxiiiChipReg getIndexedMpxiiiChipReg(int index) {
		return mpxiiiChipRegs[index];
	}

	@Override
	public int getOperationMode() throws TimeoutException, CAException, InterruptedException, Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setOperationMode(int operationMode) throws CAException, InterruptedException, Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public int getCounterSelect() throws TimeoutException, CAException, InterruptedException, Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setCounterSelect(int counterSelect) throws CAException, InterruptedException, Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public int getDacSense() throws Exception {
		return dacSense;
	}

	@Override
	public void setDacSense(int dacSense) throws Exception {
		this.dacSense = dacSense;
	}

	@Override
	public int getDacExternal() throws Exception {
		return dacExternal;
	}

	@Override
	public void setDacExternal(int dacExternal) throws Exception {
		this.dacExternal = dacExternal;
	}

}
