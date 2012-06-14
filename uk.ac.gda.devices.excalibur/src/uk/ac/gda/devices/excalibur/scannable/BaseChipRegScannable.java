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

import gda.device.DeviceException;
import gda.device.scannable.ScannableBase;
import gda.device.scannable.ScannableUtils;
import gda.factory.FactoryException;

import java.util.List;

import uk.ac.gda.devices.excalibur.ExcaliburReadoutNodeFem;
import uk.ac.gda.devices.excalibur.MpxiiiChipReg;

/**
 *
 */
public abstract class BaseChipRegScannable extends ScannableBase {
	protected final List<ExcaliburReadoutNodeFem> fems;

	public BaseChipRegScannable(List<ExcaliburReadoutNodeFem> fems) {
		this.fems = fems;
	}
	
	

	@Override
	public void configure() throws FactoryException {
		setInputNames(new String[]{getName()});
		super.configure();
	}



	@Override
	public boolean isBusy() throws DeviceException {
		return false;
	}

	@Override
	public Object rawGetPosition() throws DeviceException {
		try {
			return doGetPosition();
		} catch (Exception e) {
			throw new DeviceException(e);
		}
	}
	
	@Override
	public void rawAsynchronousMoveTo(Object position) throws DeviceException {
		Double[] positionArray = ScannableUtils.objectToArray(position);
		Double pos = positionArray[0];

		for (ExcaliburReadoutNodeFem fem : fems) {
			for (int chipNum = 0; chipNum < 8; chipNum++) { 
				MpxiiiChipReg chipReg = fem.getIndexedMpxiiiChipReg(chipNum);
				try {
					doAsynchronousMoveTo(chipReg, pos.intValue());
					
				} catch (Exception e) {
					throw new DeviceException(e);
				}
			}
		}
	}

	protected abstract void doAsynchronousMoveTo(MpxiiiChipReg chipReg, int intValue) throws Exception;

	protected abstract Object doGetPosition() throws Exception;

}
