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

import gov.aps.jca.CAException;
import gov.aps.jca.TimeoutException;

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

	@Override
	public void enableChip() throws CAException, InterruptedException, TimeoutException {

	}

	@Override
	public void disableChip() throws CAException, InterruptedException, TimeoutException {

	}

	@Override
	public boolean isChipEnabled() throws TimeoutException, CAException, InterruptedException {
		return false;
	}
	
	@Override
	public void loadDacConfig() throws Exception{
		//do nothing
	}

	@Override
	public void loadPixelConfig() throws Exception {
		// do nothing
	}	
}
