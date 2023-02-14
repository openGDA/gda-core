/*-
 * Copyright © 2023 Diamond Light Source Ltd.
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

package gda.device.scannable;

import java.util.Arrays;
import java.util.Vector;

import gda.device.DeviceException;
import gda.device.scannable.scannablegroup.ScannableGroup;

public class ScannableGroupSinglePosition extends ScannableGroup {

	/**
	 * Generate array with same demand position for each scannable in the group
	 * @param position - demand position (a single number/single number contained in array)
	 */
	@Override
	protected Vector<Object[]> extractPositionsFromObject(Object position) throws DeviceException {
		Double[] demandPosition = ScannableUtils.objectToArray(position);
		if (demandPosition == null || demandPosition.length != 1) {
			throw new DeviceException("Invalid demand position for "+getName()+" : expected single value but was passed "+String.valueOf(position));
		}
		// Make array of identical demand positions
		Double[] demandPositions = new Double[getGroupMembers().size()];
		Arrays.fill(demandPositions, demandPosition[0]);
		return super.extractPositionsFromObject(demandPositions);
	}

	@Override
	public String[] getInputNames() {
		return new String[]{};
	}

	@Override
	public String[] getExtraNames() {
		return super.getInputNames();
	}
}
