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

package uk.ac.diamond.daq.gasrig.ui.viewmodels;

import java.util.List;
import java.util.stream.Collectors;

import uk.ac.diamond.daq.gasrig.api.ICabinet;

/**
 * Provides information about an {@link ICabinet} and constructs and contains
 * a set of {@link GasViewModel}s
 */
public class CabinetViewModel {

	private String name;
	private List<GasViewModel> gases;

	public CabinetViewModel(ICabinet cabinet) {

		this.name = cabinet.getName();

		this.gases = cabinet.getGases().stream()
				.map(GasViewModel::new)
				.collect(Collectors.toList());
	}

	public List<GasViewModel> getGases() {
		return gases;
	}

	public String getName() {
		return name;
	}
}
