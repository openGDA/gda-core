/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.gasrig;

import java.io.Serializable;
import java.util.List;

import uk.ac.diamond.daq.gasrig.api.ICabinet;

/**
 * Represents a cabinet in the gas rig containing {@link Gas}es
 */
public class Cabinet implements Serializable, ICabinet {

	private String name;
	private List<Gas> gases;

	public Cabinet(String name, List<Gas> gases) {
		this.name = name;
		this.gases = gases;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public List<Gas> getGases() {
		return gases;
	}
}
