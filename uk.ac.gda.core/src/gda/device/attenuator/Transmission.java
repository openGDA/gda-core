/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

package gda.device.attenuator;

import java.util.Arrays;

/**
 * Represents a transmission that can be achieved using a particular arrangement of filters.
 */
public class Transmission {
	
	private final double transmission;
	
	private final boolean[] filterStates;
	
	/**
	 * Creates a transmission object, denoting that the specified transmission can be obtained using the specified
	 * filter states.
	 */
	public Transmission(double transmission, boolean[] filterStates) {
		this.transmission = transmission;
		this.filterStates = filterStates;
	}
	
	public double getTransmission() {
		return transmission;
	}
	
	public boolean[] getFilterStates() {
		return filterStates;
	}
	
	@Override
	public String toString() {
		return String.format("Transmission(transmisson=%.20f, filterStates=%s)",
			transmission,
			Arrays.toString(filterStates));
	}
	
}
