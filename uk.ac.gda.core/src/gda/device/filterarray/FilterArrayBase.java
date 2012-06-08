/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

package gda.device.filterarray;

import gda.device.DeviceBase;
import gda.device.DeviceException;
import gda.device.FilterArray;

/**
 * Abstract base cdlass for the FilterArray interface.
 */
public abstract class FilterArrayBase extends DeviceBase implements FilterArray {
	protected boolean useMonoEnergy = false;

	@Override
	public boolean isUsingMonoEnergy() throws DeviceException {
		return useMonoEnergy;
	}

	@Override
	public void setUseMonoEnergy(boolean useEnergy) throws DeviceException {
		useMonoEnergy = useEnergy;
	}

}
