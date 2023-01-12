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

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import uk.ac.diamond.daq.gasrig.api.IGasRig;

/**
 * Constructs and contains a set of {@link GasViewModel}s, {@link CabinetViewModel}s, and
 * {@link GasMixViewModel}s from an {@link IGasRig}.
 */
public class GasRigViewModel {

	private List<GasViewModel> nonCabinetGases;
	private List<CabinetViewModel> cabinets;
	private List<GasMixViewModel> gasMixes;

	public GasRigViewModel(IGasRig gasRig) {

		nonCabinetGases = gasRig.getNonCabinetGases().stream()
				.map(GasViewModel::new)
				.collect(Collectors.toList());

		cabinets = gasRig.getCabinets().stream()
				.map(CabinetViewModel::new)
				.collect(Collectors.toList());

		gasMixes= gasRig.getGasMixes().entrySet().stream()
			.map(entry -> new GasMixViewModel(entry.getKey(), entry.getValue()))
			.sorted(Comparator.comparing(GasMixViewModel::getLineNumber))
			.collect(Collectors.toList());
	}

	public List<GasViewModel> getNonCabinetGases() {
		return nonCabinetGases;
	}

	public List<CabinetViewModel> getCabinets() {
		return cabinets;
	}

	public List<GasMixViewModel> getGasMixes() {
		return gasMixes;
	}

	public int getNumberOfMixes() {
		return gasMixes.size();
	}
}
