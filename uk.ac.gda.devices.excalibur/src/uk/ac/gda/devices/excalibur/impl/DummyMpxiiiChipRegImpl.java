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
import uk.ac.gda.devices.excalibur.ChipPixel;
import uk.ac.gda.devices.excalibur.MpxiiiChipReg;

/**
 *
 */
public class DummyMpxiiiChipRegImpl implements MpxiiiChipReg, InitializingBean {
	private ChipAnper anper;

	private ChipPixel pixel;

	private int dacSense;

	private int dacSenseDecode;

	private String dacSenseName="dacSenseName";

	private int dacExternal;

	private int dacExternalDecode;

	private String dacExternalName="dacExternalName";

	@Override
	public int getDacSense() throws Exception {
		return dacSense;
	}

	@Override
	public void setDacSense(int dacSense) throws Exception {
		this.dacSense = dacSense;
	}

	@Override
	public int getDacSenseDecode() throws Exception {
		return dacSenseDecode;
	}

	@Override
	public void setDacSenseDecode(int dacSenseDecode) throws Exception {
		this.dacSenseDecode = dacSenseDecode;
	}

	@Override
	public String getDacSenseName() throws Exception {
		return dacSenseName;
	}

	@Override
	public void setDacSenseName(String dacSenseName) throws Exception {
		this.dacSenseName = dacSenseName;
	}

	@Override
	public int getDacExternal() throws Exception {
		return dacExternal;
	}

	@Override
	public void setDacExternal(int dacExternal) throws Exception {
		this.dacExternal = dacExternal;
	}

	@Override
	public int getDacExternalDecode() throws Exception {
		return dacExternalDecode;
	}

	@Override
	public void setDacExternalDecode(int dacExternalDecode) throws Exception {
		this.dacExternalDecode = dacExternalDecode;
	}

	@Override
	public String getDacExternalName() throws Exception {
		return dacExternalName;
	}

	@Override
	public void setDacExternalName(String dacExternalName) throws Exception {
		this.dacExternalName = dacExternalName;
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
		if (pixel == null) {
			throw new IllegalArgumentException("'pixel' needs to be declared");
		}
		if (anper == null) {
			throw new IllegalArgumentException("'anper' needs to be declared");
		}

	}


}
