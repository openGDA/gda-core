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

	// Non permanent config variables
	private boolean removeLiveControls;


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
			String message = "An error occured while attempting to evacuate line " + lineNumber;
			logger.error(message, exception);
			throw new GasRigException(message, exception);
		}
	}

	@Override
	public void evacuateLines() throws DeviceException, GasRigException {
		try {
			controller.evacuateLines();
		} catch (DeviceException exception) {
			String message = "An error occured while attempting to evacuate line ";
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

		updateMassFlowsForLine(gasMix, lineNumber);
	}

	private boolean areGasesInUseOnOtherLines(IGasMix gasMix, int lineNumber) throws GasRigException, DeviceException {
		for (int l=1; l<=numberOfLines; l++) {
			if(l != lineNumber) {
				for (var gasFlow : gasMix.getAllGasFlows()) {
					if (gasFlow.getPressure() > 0) {
						if(controller.isGasFlowingToLine(gasFlow.getGasId(), l)) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	@Override
	public void setButterflyValvePressure(double value) throws DeviceException {
		controller.setButterflyValvePressure(value);
	}

	@Override
	public void setButterflyValvePosition(double value) throws DeviceException {
		controller.setButterflyValvePosition(value);
	}

	@Override
	public void stopCurrentSequence() throws DeviceException {
		controller.stopCurrentSequence();
	}

	@Override
	public void setAllGasFlowsToZero(int value) throws DeviceException {
		controller.setAllGasFlowsToZero(value);
	}

	private void updateMassFlowsForLine(IGasMix gasMix, int lineNumber) throws DeviceException, GasRigException {

		// First run admit gas to line for all gases then set mass
		// flow separately to workaround epics issues
		for (var gasFlow : gasMix.getAllGasFlows()) {
			admitGasToLineOrCloseValve(gasFlow.getGasId(), gasFlow.getMassFlow(), lineNumber);
		}

		for (var gasFlow : gasMix.getAllGasFlows()) {
			setMassFlow(gasFlow.getGasId(), gasFlow.getMassFlow());
		}
	}

	private void admitGasToLineOrCloseValve(int gasId, double massFlow, int lineNumber) throws DeviceException, GasRigException {
		Gas gas = getGas(gasId);

		if (massFlow > 0) {
			controller.admitGasToLine(gas.getName(), lineNumber);
		} else {
			controller.closeLineValveForGas(gasId, lineNumber);
		}
		// TODO Call update method from here with info about progress
	}

	private void setMassFlow(int gasId, double massFlow) throws DeviceException, GasRigException {
		if (massFlow > 0) {
			controller.setMassFlow(gasId, massFlow);
		}
	}

	@Override
	public void settleUnusedGases(IGasMix gasMix1, IGasMix gasMix2) throws GasRigException, DeviceException {
		logger.debug("Now setting gasses which are not used on either line to zero");
		for (var gasFlowFromLine1 : gasMix1.getAllGasFlows()) {
			int id = gasFlowFromLine1.getGasId();
			var gasFlowFromLine2 = gasMix2.getGasFlowByGasId(id);
			if(gasFlowFromLine1.getPressure() == 0 && gasFlowFromLine2.getPressure() == 0) {
				controller.setMassFlow(id, 0);
			}
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

	@Override
	public void admitLinesToEndstation() throws GasRigException {
		try {
			controller.admitLinesToEndStation();
		} catch (DeviceException exception) {
			String message = "An error occured while attempting to admit lines to endstation";
			logger.error(message, exception);
			throw new GasRigException(message, exception);
		}
	}

	@Override
	public void admitLinesToExhaust() throws GasRigException {
		try {
			controller.admitLinesToExhaust();
		} catch (DeviceException exception) {
			String message = "An error occured while attempting to admit lines to exhaust";
			logger.error(message, exception);
			throw new GasRigException(message, exception);
		}
	}

	// Non permanent config variable related methods
	@Override
	public boolean isRemoveLiveControls() {
		return removeLiveControls;
	}

	public void setRemoveLiveControls(boolean removeLiveControls) {
		this.removeLiveControls = removeLiveControls;
	}
}
