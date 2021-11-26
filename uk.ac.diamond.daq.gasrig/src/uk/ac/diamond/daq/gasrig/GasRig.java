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
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.factory.FactoryException;
import gda.factory.FindableConfigurableBase;
import uk.ac.diamond.daq.gasrig.api.IGasRig;
import uk.ac.diamond.daq.gasrig.api.models.GasRigModel;
import uk.ac.gda.api.remoting.ServiceInterface;

@ServiceInterface(IGasRig.class)
public class GasRig extends FindableConfigurableBase implements IGasRig {

	protected final Logger logger = LoggerFactory.getLogger(GasRig.class);

	private IGasRigController controller;
	private List<Gas> nonCabinetGases;
	private List<Cabinet> cabinets;

	private GasRigModelMapper modelMapper = new GasRigModelMapper();

	public GasRig(IGasRigController controller, List<Gas> nonCabinetGases, List<Cabinet> cabinets) {
		this.controller = controller;
		this.nonCabinetGases = nonCabinetGases;
		this.cabinets = cabinets;
	}

	@Override
	public void configure() throws FactoryException {
		if (isConfigured() ) {
			return;
		}

		try {
			for (Gas gas : getAllGases()) {
				updateGasName(gas);
			}
		} catch (DeviceException exception) {
			throw new FactoryException("An error occured while configuring gas names", exception);
		}
		setConfigured(true);
	}

	private List<Gas> getAllGases() {
		var cabinetGases = cabinets.stream().flatMap(cabinet -> cabinet.getGases().stream());
		return Stream.concat(nonCabinetGases.stream(), cabinetGases).collect(Collectors.toList());
	}

	private void updateGasName(Gas gas) throws DeviceException {
		gas.setName(controller.getGasName(gas.getId()));
		logger.info("Gas {} is {}", gas.getId(), gas.getName());
	}

	public List<Gas> getNonCabinetGases() {
		return nonCabinetGases;
	}

	public List<Cabinet> getCabinets() {
		return cabinets;
	}

	@Override
	public GasRigModel getGasRigInfo() {
		return modelMapper.getGasRigModel(this);
	}
}
