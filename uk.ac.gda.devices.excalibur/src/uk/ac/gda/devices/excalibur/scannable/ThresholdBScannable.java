/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package uk.ac.gda.devices.excalibur.scannable;

import java.util.Arrays;
import java.util.List;

import uk.ac.gda.devices.excalibur.ExcaliburReadoutNodeFem;
import uk.ac.gda.devices.excalibur.MpxiiiChipReg;

/**
 *
 */
public class ThresholdBScannable extends BaseChipRegScannable {

	/**
	 * @param fems
	 */
	public ThresholdBScannable(List<ExcaliburReadoutNodeFem> fems) {
		super(fems);
	}

	@Override
	protected void doAsynchronousMoveTo(MpxiiiChipReg chipReg, int intValue) throws Exception {
		int thresholdBLength = chipReg.getPixel().getThresholdB().length;
		short[] values = new short[thresholdBLength];
		Arrays.fill(values, (short) intValue);
		chipReg.getPixel().setThresholdB(values);
	}

	@Override
	protected Object doGetPosition() throws Exception {
		return fems.get(0).getMpxiiiChipReg1().getPixel().getThresholdB()[0];
	}

}
