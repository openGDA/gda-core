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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.factory.FactoryException;
import gda.factory.FindableConfigurableBase;
import uk.ac.diamond.daq.gasrig.api.GasRigException;
import uk.ac.diamond.daq.gasrig.api.IGasRig;
import uk.ac.gda.api.remoting.ServiceInterface;

@ServiceInterface(IGasRig.class)
public class GasRig extends FindableConfigurableBase implements IGasRig {

	protected final Logger logger = LoggerFactory.getLogger(GasRig.class);

	private IGasRigController controller;
	private List<Gas> nonCabinetGases;
	private List<Cabinet> cabinets;
	private int numberOfLines;
	private Map<Integer, GasMix> gasMixes;
	private MolarMassTable molarMasses;

	public GasRig(IGasRigController controller, List<Gas> nonCabinetGases, List<Cabinet> cabinets, MolarMassTable molarMasses, int numberOfLines) {
		this.controller = controller;
		this.nonCabinetGases = nonCabinetGases;
		this.cabinets = cabinets;
		this.molarMasses = molarMasses;
		this.numberOfLines = numberOfLines;
		gasMixes = new HashMap<>();
		for (int i = 1; i < numberOfLines + 1; i++) {
			gasMixes.put(i, new GasMix(getAllGases()));
		}
	}

	@Override
	public void configure() throws FactoryException {
		if (isConfigured() ) {
			return;
		}

		try {
			for (Gas gas : getAllGases()) {
				configureGas(gas);
			}
		} catch (DeviceException | GasRigException exception) {
			throw new FactoryException("An error occured while configuring gases.", exception);
		}
		setConfigured(true);
	}

	private List<Gas> getAllGases() {
		var cabinetGases = cabinets.stream().flatMap(cabinet -> cabinet.getGases().stream());
		return Stream.concat(nonCabinetGases.stream(), cabinetGases).collect(Collectors.toList());
	}

	private void configureGas(Gas gas) throws DeviceException, GasRigException {
		gas.setName(controller.getGasName(gas.getId()));
		gas.setMaximumMassFlow(controller.getMaximumMassFlow(gas.getId()));
		gas.setMolarMass(molarMasses.getMolarMass(gas.getName()));
		logger.info("Gas {} is {}. Molar mass: {}. Maximum mass flow: {}.", gas.getId(), gas.getName(), gas.getMolarMass(), gas.getMaximumMassFlow());
	}

	@Override
	public List<Gas> getNonCabinetGases() {
		return nonCabinetGases;
	}

	@Override
	public List<Cabinet> getCabinets() {
		return cabinets;
	}

	@Override
	public GasMix getGasMix(int lineNumber) throws GasRigException {
		if (lineNumber < 1 || lineNumber > numberOfLines) {
			throw new GasRigException("Invalid line number specified. Please specify a line number in the range 1-" + numberOfLines);
		}

		return gasMixes.get(lineNumber);
	}

	@Override
	public Map<Integer, GasMix> getGasMixes() {
		return gasMixes;
	}
}
