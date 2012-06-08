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

import uk.ac.gda.devices.excalibur.IAdFem;
import uk.ac.gda.devices.excalibur.IExcaliburController;
import uk.ac.gda.devices.excalibur.MpxiiiChipReg;
import uk.ac.gda.devices.excalibur.MpxiiiGlobalReg;

/**
 *Implementation of the area detector controller class for the Excalibur Area Detector
 */
public class ExcaliburController implements IExcaliburController, InitializingBean{

	
	//Excalibur specific controllers
	private IAdFem adFem;
	private MpxiiiChipReg mpxiiiChipReg1;
	private MpxiiiChipReg mpxiiiChipReg2;
	private MpxiiiChipReg mpxiiiChipReg3;
	private MpxiiiChipReg mpxiiiChipReg4;
	private MpxiiiChipReg mpxiiiChipReg5;
	private MpxiiiChipReg mpxiiiChipReg6;
	private MpxiiiChipReg mpxiiiChipReg7;
	private MpxiiiChipReg mpxiiiChipReg8;

	private MpxiiiGlobalReg mpxiiiGlobalReg;
	
	@Override
	public void afterPropertiesSet() throws Exception {
	}

	/**
	 * @return Returns the adFem.
	 */
	@Override
	public IAdFem getAdFem() {
		return adFem;
	}

	/**
	 * @param adFem The adFem to set.
	 */
	public void setAdFem(IAdFem adFem) {
		this.adFem = adFem;
	}

	/**
	 * @return Returns the mpxiiiGlobalReg.
	 */
	@Override
	public MpxiiiGlobalReg getMpxiiiGlobalReg() {
		return mpxiiiGlobalReg;
	}

	/**
	 * @param mpxiiiGlobalReg The mpxiiiGlobalReg to set.
	 */
	public void setMpxiiiGlobalReg(MpxiiiGlobalReg mpxiiiGlobalReg) {
		this.mpxiiiGlobalReg = mpxiiiGlobalReg;
	}
	
	/**
	 * @return Returns the mpxiiiChipReg1.
	 */
	@Override
	public MpxiiiChipReg getMpxiiiChipReg1() {
		return mpxiiiChipReg1;
	}

	/**
	 * @param mpxiiiChipReg1 The mpxiiiChipReg1 to set.
	 */
	public void setMpxiiiChipReg1(MpxiiiChipReg mpxiiiChipReg1) {
		this.mpxiiiChipReg1 = mpxiiiChipReg1;
	}

	/**
	 * @return Returns the mpxiiiChipReg2.
	 */
	@Override
	public MpxiiiChipReg getMpxiiiChipReg2() {
		return mpxiiiChipReg2;
	}

	/**
	 * @param mpxiiiChipReg2 The mpxiiiChipReg2 to set.
	 */
	public void setMpxiiiChipReg2(MpxiiiChipReg mpxiiiChipReg2) {
		this.mpxiiiChipReg2 = mpxiiiChipReg2;
	}

	/**
	 * @return Returns the mpxiiiChipReg3.
	 */
	@Override
	public MpxiiiChipReg getMpxiiiChipReg3() {
		return mpxiiiChipReg3;
	}

	/**
	 * @param mpxiiiChipReg3 The mpxiiiChipReg3 to set.
	 */
	public void setMpxiiiChipReg3(MpxiiiChipReg mpxiiiChipReg3) {
		this.mpxiiiChipReg3 = mpxiiiChipReg3;
	}

	/**
	 * @return Returns the mpxiiiChipReg4.
	 */
	@Override
	public MpxiiiChipReg getMpxiiiChipReg4() {
		return mpxiiiChipReg4;
	}

	/**
	 * @param mpxiiiChipReg4 The mpxiiiChipReg4 to set.
	 */
	public void setMpxiiiChipReg4(MpxiiiChipReg mpxiiiChipReg4) {
		this.mpxiiiChipReg4 = mpxiiiChipReg4;
	}

	/**
	 * @return Returns the mpxiiiChipReg5.
	 */
	@Override
	public MpxiiiChipReg getMpxiiiChipReg5() {
		return mpxiiiChipReg5;
	}

	/**
	 * @param mpxiiiChipReg5 The mpxiiiChipReg5 to set.
	 */
	public void setMpxiiiChipReg5(MpxiiiChipReg mpxiiiChipReg5) {
		this.mpxiiiChipReg5 = mpxiiiChipReg5;
	}

	/**
	 * @return Returns the mpxiiiChipReg6.
	 */
	@Override
	public MpxiiiChipReg getMpxiiiChipReg6() {
		return mpxiiiChipReg6;
	}

	/**
	 * @param mpxiiiChipReg6 The mpxiiiChipReg6 to set.
	 */
	public void setMpxiiiChipReg6(MpxiiiChipReg mpxiiiChipReg6) {
		this.mpxiiiChipReg6 = mpxiiiChipReg6;
	}

	/**
	 * @return Returns the mpxiiiChipReg7.
	 */
	@Override
	public MpxiiiChipReg getMpxiiiChipReg7() {
		return mpxiiiChipReg7;
	}

	/**
	 * @param mpxiiiChipReg7 The mpxiiiChipReg7 to set.
	 */
	public void setMpxiiiChipReg7(MpxiiiChipReg mpxiiiChipReg7) {
		this.mpxiiiChipReg7 = mpxiiiChipReg7;
	}
	
	/**
	 * @return Returns the mpxiiiChipReg8.
	 */
	@Override
	public MpxiiiChipReg getMpxiiiChipReg8() {
		return mpxiiiChipReg8;
	}

	/**
	 * @param mpxiiiChipReg8 The mpxiiiChipReg8 to set.
	 */
	public void setMpxiiiChipReg8(MpxiiiChipReg mpxiiiChipReg8) {
		this.mpxiiiChipReg8 = mpxiiiChipReg8;
	}

}
