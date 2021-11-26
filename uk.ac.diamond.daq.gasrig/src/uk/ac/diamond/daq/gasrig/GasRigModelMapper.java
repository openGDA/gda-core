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

import java.util.List;
import java.util.stream.Collectors;

import uk.ac.diamond.daq.gasrig.api.models.CabinetModel;
import uk.ac.diamond.daq.gasrig.api.models.GasModel;
import uk.ac.diamond.daq.gasrig.api.models.GasRigModel;

public class GasRigModelMapper {

	public GasRigModel getGasRigModel(GasRig gasRig) {
		return new GasRigModel(
				getGasModels(gasRig.getNonCabinetGases()),
				getCabinetModels(gasRig.getCabinets())
		);
	}

	public List<GasModel> getGasModels(List<Gas> gases) {
		return gases.stream().map(this::getGasModel).collect(Collectors.toList());
	}

	public GasModel getGasModel(Gas gas) {
		return new GasModel(gas.getId(), gas.getName());
	}

	public List<CabinetModel> getCabinetModels(List<Cabinet> cabinets) {
		return cabinets.stream().map(this::getCabinetModel).collect(Collectors.toList());
	}

	public CabinetModel getCabinetModel(Cabinet cabinet) {
		return new CabinetModel(
				cabinet.getName(),
				getGasModels(cabinet.getGases())
		);
	}
}
