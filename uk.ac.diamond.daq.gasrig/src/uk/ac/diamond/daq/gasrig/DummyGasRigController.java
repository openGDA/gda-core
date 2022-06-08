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

import gda.device.DeviceException;
import gda.observable.IObservable;
import gda.observable.IObserver;
import gda.observable.ObservableComponent;
import uk.ac.diamond.daq.gasrig.api.GasRigSequenceUpdate;

/**
 * This class serves to simulate the responses from EPICS, and also logs the PVs which
 * will be used in the live environment for debugging purposes, as there is no gas rig simulator.
 *
 * It extends {@link BaseGasRigController} so that it can make use of the same methods which construct
 * the live PVs and log them, to ensure that they are correct.
 *
 * @author Tom Richardson (too27251)
 */
public class DummyGasRigController extends BaseGasRigController implements IGasRigController, IObservable {

	private ObservableComponent observableComponent = new ObservableComponent();

	private List<DummyGas> gases;

	public DummyGasRigController(String basePvName, List<DummyGas> gases) {
		super(basePvName);
		this.gases = gases;
	}

	@Override
	public String getGasName(int gasId) throws DeviceException {
		String gasNamePvSuffix = constructGasNamePvSuffix(gasId);
		String fullGasNamePv = getBasePvName() + gasNamePvSuffix;
		logger.info("Gas name requested for gas {}. Live PV would be {}", gasId, fullGasNamePv);

		return getGas(gasId).getName();
	}

	@Override
	public double getMaximumMassFlow(int gasId) throws DeviceException {
		String maximumMassFlowPvSuffix = constructMaximumMassFlowPvSuffix(gasId);
		String fullMassFlowPv = getBasePvName() + maximumMassFlowPvSuffix;
		logger.info("Maximum mass flow name requested for gas {}. Live PV would be {}", gasId, fullMassFlowPv);

		return getGas(gasId).getMaximumMassFlow();
	}

	private DummyGas getGas(int gasId) throws DeviceException {
		return gases.stream()
				.filter(g -> g.getId() == gasId)
				.findFirst()
				.orElseThrow(() -> new DeviceException("No gas found for id " + gasId));
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
	public void runDummySequence() {

		for (int i = 0; i < 101; i += 10) {
			logger.info("Dummy sequence progress: " + i);
			observableComponent.notifyIObservers(this, new GasRigSequenceUpdate("Dummy", "Running", i));
			try {
				Thread.sleep((500));
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				logger.error("TODO put description of error here", e);
			}
		}

		observableComponent.notifyIObservers(this, new GasRigSequenceUpdate("Dunmmy", "Finished", 100));
	}

	@Override
	public void evacuateEndStation() throws DeviceException {
		// TODO Auto-generated method stub

	}

	@Override
	public void evacuateLine(int lineNumber) throws DeviceException {
		// TODO Auto-generated method stub

	}

	@Override
	public void admitLineToEndStation(int lineNumber) throws DeviceException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setMassFlow(int gasId, double massFlow) throws DeviceException {
		// TODO Auto-generated method stub
	}

	@Override
	public void initialise() throws DeviceException {
		// TODO Auto-generated method stub
	}

	@Override
	public void admitGasToLine(String gasName, int lineNumber) throws DeviceException {
		// TODO Auto-generated method stub

	}

	@Override
	public void closeLineValvesForGas(int gasId) {
		// TODO Auto-generated method stub

	}
}
