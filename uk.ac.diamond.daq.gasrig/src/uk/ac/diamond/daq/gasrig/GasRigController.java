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

import gda.device.DeviceException;
import gda.factory.FactoryException;
import gda.observable.IObservable;
import gda.observable.IObserver;
import gda.observable.ObservableComponent;
import gov.aps.jca.Monitor;
import gov.aps.jca.dbr.DBRType;
import gov.aps.jca.dbr.DBR_Double;
import gov.aps.jca.dbr.DBR_LABELS_Enum;
import uk.ac.diamond.daq.gasrig.api.GasRigSequenceUpdate;

public class GasRigController extends BaseGasRigController implements IGasRigController, IObservable {

	private ObservableComponent observableComponent = new ObservableComponent();

	private String currentOrLastSequence;
	private String currentOrLastStatus;
	private double currentSequenceProgress;

	public GasRigController(String basePvName) {
		super(basePvName);
	}

	@Override
	public void configure() throws FactoryException {
		if (isConfigured()) {
			return;
		}

		for (GasRigSequence sequence : GasRigSequence.values()) {
			try {
				configureSequenceStatusMonitor(sequence);
				configureSequenceProgressMonitor(sequence);
			} catch (DeviceException exception) {
				throw new FactoryException("An error occured while configuring status monitors", exception);
			}
		}

		setConfigured(true);
	}

	private void configureSequenceStatusMonitor(GasRigSequence sequence) throws DeviceException {
		String statusPv = constructSequenceStatusPvSuffix(sequence.getSequenceId());

		setMonitor(statusPv, DBRType.LABELS_ENUM, Monitor.VALUE, event -> {
			logger.info("Received status update for sequence: {}", sequence.getDescription());

			DBR_LABELS_Enum dbr = (DBR_LABELS_Enum)event.getDBR();
			String[] labels = dbr.getLabels();
			short value = ((short[])dbr.getValue())[0];
			String sequenceStatus = labels[value];

			logger.info("New sequence status is {}", sequenceStatus);

			currentOrLastSequence = sequence.getDescription();
			currentOrLastStatus = sequenceStatus;

			try {
				currentSequenceProgress = getSequenceProgress(sequence.getSequenceId());
			} catch (DeviceException exception) {
				logger.error("An error was encountered while trying to update sequence progress from EPICS", exception);
			}

			sendSequenceUpdate();
		});
	}

	private void configureSequenceProgressMonitor(GasRigSequence sequence) throws DeviceException {
		String statusPv = constructSequenceProgressPvSuffix(sequence.getSequenceId());

		setMonitor(statusPv, DBRType.DOUBLE, Monitor.VALUE, event -> {
			logger.info("Received progress update for sequence: {}", sequence.getDescription());

			try {
				DBR_Double dbr = (DBR_Double)event.getDBR();
				currentSequenceProgress = dbr.getDoubleValue()[0];

				logger.info("New sequence progress is {}", currentSequenceProgress);
			} catch (Exception exception) {
				logger.error("Error processing process update.", exception);
			}

			sendSequenceUpdate();
		});
	}

	private void sendSequenceUpdate() {
		var update = new GasRigSequenceUpdate(currentOrLastSequence, currentOrLastStatus, currentSequenceProgress);
		logger.info("Notifying observers of sequence update: {}", update);
		observableComponent.notifyIObservers(this, update);
	}

	@Override
	public String getGasName(int gasId) throws DeviceException {
		return getStringValue(constructGasNamePvSuffix(gasId), "gas name");
	}

	@Override
	public double getMaximumMassFlow(int gasId) throws DeviceException {
		return getDoubleValue(constructMaximumMassFlowPvSuffix(gasId), "maximum mass flow");
	}

	public void startDummySequence() throws DeviceException {
		setStringValue(constructSequenceControlPvSuffix(DUMMY_SEQUENCE_NUMBER), SEQUENCE_START, "dummy sequence control");
	}

	private double getSequenceProgress(int sequenceId) throws DeviceException {
		return getDoubleValue(constructSequenceProgressPvSuffix(sequenceId), "sequence progress");
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
		// TODO Auto-generated method stub

	}
}