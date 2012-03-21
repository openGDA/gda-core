/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

package gda.device.detector.xmap;

import gda.device.DeviceException;

/**
 * For use when a detector readout using an Xmap ADC is normally triggered externally e.g. by a TFG and so needs a slave
 * mode
 */
public class XmapwithSlaveMode extends gda.device.detector.xmap.Xmap {

	boolean slave;
	
	@Override
	public void collectData() throws DeviceException{
		if (!slave){
			super.collectData();
		}
	}


	/**
	 * @return Returns the slave.
	 */
	public boolean isSlave() {
		return slave;
	}


	/**
	 * @param slave The slave to set.
	 */
	public void setSlave(boolean slave) {
		this.slave = slave;
	}

}
