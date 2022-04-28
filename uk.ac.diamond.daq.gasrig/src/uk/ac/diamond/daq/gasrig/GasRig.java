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
import gda.observable.IObserver;
import gda.observable.ObservableComponent;
import uk.ac.diamond.daq.gasrig.api.GasRigException;
import uk.ac.diamond.daq.gasrig.api.IGasMix;
import uk.ac.diamond.daq.gasrig.api.IGasRig;
import uk.ac.gda.api.remoting.ServiceInterface;

@ServiceInterface(IGasRig.class)
public class GasRig extends FindableConfigurableBase implements IGasRig, IObserver {

	protected final Logger logger = LoggerFactory.getLogger(GasRig.class);

	private ObservableComponent observableComponent = new ObservableComponent();

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

		controller.addIObserver(this);
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

	public Gas getGas(int gasId) throws GasRigException {
		return getAllGases().stream()
				.filter(gas -> gas.getId() == gasId)
				.findFirst()
				.orElseThrow(() -> new GasRigException("No gas found for id " + gasId));
	}

	public Gas getGas(String gasName) throws GasRigException {
		return getAllGases().stream()
				.filter(gas -> gas.getName().equals(gasName))
				.findFirst()
				.orElseThrow(() -> new GasRigException("No gas found with name " + gasName));
	}

	@Override
	public void addIObserver(IObserver observer) {
		observableComponent.addIObserver(observer);
	}

	@Override
	public void deleteIObserver(IObserver observer) {
		observableComponent.deleteIObserver(observer);
	}

	@Override
	public void deleteIObservers() {
		observableComponent.deleteIObservers();
	}

	@Override
	public void update(Object source, Object arg) {
		observableComponent.notifyIObservers(this, arg);
	}

	@Override
	public void initialise() throws DeviceException {
		controller.initialise();
	}

	@Override
	public void runDummySequence() throws GasRigException {
		try {
			controller.runDummySequence();
		} catch (DeviceException exception) {
			String message = "An error occured while attempting to run the dummy sequence";
			logger.error(message);
			throw new GasRigException(message, exception);
		}
	}

	@Override
	public void evacuateEndStation() throws GasRigException {
		try {
			controller.evacuateEndStation();
		} catch (DeviceException exception) {
			String message = "An error occured while attempting to evacuate the endstation";
			logger.error(message, exception);
			throw new GasRigException(message, exception);
		}
	}

	@Override
	public void evacuateLine(int lineNumber) throws GasRigException {
		try {
			controller.evacuateLine(lineNumber);
		} catch (DeviceException exception) {
			String message = "An error occured while attempting to evacuate line " + lineNumber;;
			logger.error(message, exception);
			throw new GasRigException(message, exception);
		}
	}

	@Override
	public void admitLineToEndStation(int lineNumber) throws GasRigException {
		try {
			controller.admitLineToEndStation(lineNumber);
		} catch (DeviceException exception) {
			String message = "An error occured while attempting to admit line " + lineNumber + " to endstation";
			logger.error(message, exception);
			throw new GasRigException(message, exception);
		}
	}

	@Override
	public void configureGasMixForLine(IGasMix gasMix, int lineNumber) throws GasRigException, DeviceException {

		if (areGasesInUseOnOtherLines(gasMix, lineNumber)) {
			throw new GasRigException("Gases already in use on other line.");
		}

		boolean shouldSendBackToEndStation = false;
		if (isLineFlowingToEndStation(lineNumber) && !areSameGasesFlowingToLine(gasMix, lineNumber)) {
			evacuateLine(lineNumber);
			shouldSendBackToEndStation = true;
		}

		updateMassFlowsForLine(gasMix, lineNumber);

		if (shouldSendBackToEndStation) {
			admitLineToEndStation(lineNumber);
		}
	}

	private boolean areGasesInUseOnOtherLines(IGasMix gasMix, int lineNumber) {
		// TODO Auto-generated method stub
		return false;
	}

	private boolean isLineFlowingToEndStation(int lineNumber) {
		// TODO Auto-generated method stub
		return true;
	}

	private boolean areSameGasesFlowingToLine(IGasMix gasMix, int lineNumber) {
		// TODO Auto-generated method stub
		return false;
	}

	private void updateMassFlowsForLine(IGasMix gasMix, int lineNumber) throws DeviceException, GasRigException {
		for (var gasFlow : gasMix.getAllGasFlows()) {
			updateMassFlow(gasFlow.getGasId(), gasFlow.getMassFlow(), lineNumber);
		}
	}

	private void updateMassFlow(int gasId, double massFlow, int lineNumber) throws DeviceException, GasRigException {
		Gas gas = getGas(gasId);

		controller.setMassFlow(gasId, massFlow);

		if (massFlow > 0) {
			controller.admitGasToLine(gas.getName(), lineNumber);
		} else {
			controller.closeLineValvesForGas(gasId);
		}
	}

	@Override
	public void admitGasToLine(int gasId, int lineNumber) throws GasRigException, DeviceException {
		Gas gas = getGas(gasId);
		controller.admitGasToLine(gas.getName(), lineNumber);
	}

	@Override
	public void admitGasToLine(String gasName, int lineNumber) throws DeviceException, GasRigException {
		// We don't need the gas object here, but we call getGas(gasName) here to check that
		// there really is a gas with that name and throw an exception if there isn't
		getGas(gasName);
		controller.admitGasToLine(gasName, lineNumber);
	}
}
