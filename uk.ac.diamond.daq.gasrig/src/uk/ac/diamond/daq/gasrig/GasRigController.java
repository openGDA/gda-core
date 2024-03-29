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
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import gda.device.DeviceException;
import gda.factory.FactoryException;
import gda.observable.IObserver;
import gda.observable.ObservableComponent;
import gov.aps.jca.CAStatus;
import gov.aps.jca.Monitor;
import gov.aps.jca.dbr.DBRType;
import gov.aps.jca.dbr.DBR_Double;
import gov.aps.jca.dbr.DBR_LABELS_Enum;
import gov.aps.jca.event.PutListener;
import uk.ac.diamond.daq.gasrig.api.GasRigException;
import uk.ac.diamond.daq.gasrig.api.GasRigSequenceUpdate;

public class GasRigController extends BaseGasRigController implements IGasRigController {

	private ObservableComponent observableComponent = new ObservableComponent();

	private String currentOrLastSequence;
	private String currentOrLastStatus;
	private double currentSequenceProgress;
	private Map<Integer, GasRigFlowController> flowControllers;
	private int v92ControlSleep = 400;


	public GasRigController(String basePvName, List<GasRigFlowController> flowControllers) {
		super(basePvName);
		this.flowControllers = flowControllers.stream()
				.collect(Collectors.toMap(GasRigFlowController::getGasId, Function.identity()));
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

	private double getSequenceProgress(int sequenceId) throws DeviceException {
		return getDoubleValue(constructSequenceProgressPvSuffix(sequenceId), "sequence progress");
	}

	@Override
	public String getGasName(int gasId) throws DeviceException, GasRigException {
		return getFlowController(gasId).getGasName();
	}

	@Override
	public double getMaximumMassFlow(int gasId) throws DeviceException, GasRigException {
		return getFlowController(gasId).getMaximumMassFlow();
	}

	@Override
	public void setMassFlow(int gasId, double massFlow) throws DeviceException, GasRigException {
		getFlowController(gasId).setMassFlow(massFlow);
	}

	private GasRigFlowController getFlowController(int gasId) throws GasRigException {
		return Optional.ofNullable(flowControllers.get(gasId))
				.orElseThrow(() -> new GasRigException("No flow controller configured for gas id " + gasId));
	}

	@Override
	public void runDummySequence() throws DeviceException {
		runSequence(GasRigSequence.DUMMY);
	}

	@Override
	public void evacuateEndStation() throws DeviceException {
		runSequenceAsynchronously(GasRigSequence.EVACUATE_ENDSTATION);
	}

	@Override
	public void evacuateLine(int lineNumber) throws DeviceException {
		setNumericSequenceParameter(GasRigSequence.EVACUATE_LINE, 1, lineNumber);
		runSequence(GasRigSequence.EVACUATE_LINE);
	}

	@Override
	public void evacuateLines() throws DeviceException {
		setNumericSequenceParameter(GasRigSequence.EVACUATE_LINE, 1, 1);
		runSequenceAsynchronously(GasRigSequence.EVACUATE_LINE, evacuateLineCompleted);
	}

	PutListener evacuateLineCompleted = event -> {
		if (event.getStatus() == CAStatus.NORMAL) {
			try {
				evacuateLine(2);
			} catch (DeviceException e) {
				logger.error("Problem with calling evacuate line 2", e);
			}
		} else {
			logger.debug("Evacuate line 1 didn't complete");
		}
	};

	@Override
	public void admitGasToLine(String gasName, int lineNumber) throws DeviceException {
		setEnumSequenceParameter(GasRigSequence.ADMIT_GAS_TO_LINE, 1, gasName);
		setNumericSequenceParameter(GasRigSequence.ADMIT_GAS_TO_LINE, 2, lineNumber);
		runSequence(GasRigSequence.ADMIT_GAS_TO_LINE);
	}

	@Override
	public void admitLineToEndStation(int lineNumber) throws DeviceException {
		setNumericSequenceParameter(GasRigSequence.ADMIT_LINE_TO_ENDSTATION, 1, lineNumber);
		runSequence(GasRigSequence.ADMIT_LINE_TO_ENDSTATION);
	}

	@Override
	public void initialise() throws DeviceException {
		runSequenceAsynchronously(GasRigSequence.INITIALISE);
	}

	@Override
	public void admitLinesToEndStation() throws DeviceException {
		runSequence(GasRigSequence.ADMIT_LINES_TO_ENDSTATION);
	}

	@Override
	public void admitLinesToExhaust() throws DeviceException {
		runSequence(GasRigSequence.ADMIT_LINES_TO_EXHAUST);
	}

	private void runSequence(GasRigSequence sequence) throws DeviceException {
		setStringValue(constructSequenceControlPvSuffix(sequence.getSequenceId()), SEQUENCE_START, sequence.getDescription() + " control");
	}

	private void abortSequence(GasRigSequence sequence) throws DeviceException {
		setStringValueAsynchronously(constructSequenceControlPvSuffix(sequence.getSequenceId()), SEQUENCE_ABORT, sequence.getDescription() + " control");
	}

	private void resetSequence(GasRigSequence sequence) throws DeviceException {
		setStringValueAsynchronously(constructSequenceControlPvSuffix(sequence.getSequenceId()), SEQUENCE_RESET, sequence.getDescription() + " control");
	}

	private void runSequenceAsynchronously(GasRigSequence sequence, PutListener listener) throws DeviceException {
		setStringValueAsynchronously(constructSequenceControlPvSuffix(sequence.getSequenceId()), SEQUENCE_START, listener ,sequence.getDescription() + " control");
	}

	private void runSequenceAsynchronously(GasRigSequence sequence) throws DeviceException {
		setStringValueAsynchronously(constructSequenceControlPvSuffix(sequence.getSequenceId()), SEQUENCE_START, sequence.getDescription() + " control");
	}

	private void setNumericSequenceParameter(GasRigSequence sequence, int parameterNumber, int parameterValue) throws DeviceException {
		setIntegerValue(constructNumericSequenceParameterPv(sequence.getSequenceId(), parameterNumber), parameterValue, sequence.getDescription() + " parameter " + parameterValue);
	}

	private void setEnumSequenceParameter(GasRigSequence sequence, int parameterNumber, String parameterValue) throws DeviceException {
		setStringValue(constructEnumSequenceParameterPv(sequence.getSequenceId(), parameterNumber), parameterValue, sequence.getDescription() + " parameter " + parameterValue);
	}

	@Override
	public void closeLineValvesForGas(int gasId) throws DeviceException, GasRigException {
		getFlowController(gasId).closeLineValves();
	}

	@Override
	public void closeLineValveForGas(int gasId, int lineNumber) throws DeviceException, GasRigException {
		int valveNumber = getFlowController(gasId).getValveNumber(lineNumber);
		getFlowController(gasId).closeValve(valveNumber);
	}

	@Override
	public boolean isGasFlowingToLine(int gasId, int lineNumber) throws DeviceException, GasRigException {
		int valveNumber = getFlowController(gasId).getValveNumber(lineNumber);
		return getFlowController(gasId).isValveOpen(valveNumber);
	}

	@Override
	public void setButterflyValvePressure(double value) throws DeviceException {
		String butteflyValveControlPv = constructValveControlPv(92);
		setDoubleValue(getButterflyValveSetPressurePv(), value, "butterfly valve pressure");
		try {
			Thread.sleep(v92ControlSleep);
			setDoubleValueAsynchronously(butteflyValveControlPv, 4, "V92 control - Set pressure");
			Thread.sleep(v92ControlSleep);
			setDoubleValueAsynchronously(butteflyValveControlPv, 2, "V92 control - Reset");
			Thread.sleep(v92ControlSleep);
			setDoubleValueAsynchronously(butteflyValveControlPv, 0, "V92 control - Pressure ctrl");
			Thread.sleep(v92ControlSleep);
		}catch (InterruptedException e) {
			logger.debug("InterruptedException while setting V92");
		}
		logger.info("Configuring V92 pressure completed successfully");
	}

	@Override
	public void setButterflyValvePosition(double value) throws DeviceException {
		String butteflyValveControlPv = constructValveControlPv(92);
		setDoubleValue(getButterflyValveSetPositionPv(), value, "butterfly valve position");
		try {
			Thread.sleep(v92ControlSleep);
			setDoubleValueAsynchronously(butteflyValveControlPv, 6, "V92 control - Set position");
			Thread.sleep(v92ControlSleep);
			setDoubleValueAsynchronously(butteflyValveControlPv, 2, "V92 control - Reset");
			Thread.sleep(v92ControlSleep);
			setDoubleValueAsynchronously(butteflyValveControlPv, 5, "V92 control - Position ctrl");
			Thread.sleep(v92ControlSleep);
		} catch(InterruptedException e) {
			logger.debug("InterruptedException while setting V92");
		}
		logger.info("Configuring V92 position completed successfully");
	}

	@Override
	public void stopCurrentSequence() throws DeviceException {
		GasRigSequence sequence = GasRigSequence.getByDescription(currentOrLastSequence);
		try {
			abortSequence(sequence);
			Thread.sleep(100);
			resetSequence(sequence);
		} catch(InterruptedException e) {
			logger.debug("InterruptedException while aborting sequence");
		}
		logger.info("Running sequence successfully aborted");
	}

	@Override
	public void setAllGasFlowsToZero(int value) throws DeviceException {
		setIntegerValue(getZeroFlowPv(), value, "zero all gas flows");
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

	public int getV92ControlSleep() {
		return v92ControlSleep;
	}

	public void setV92ControlSleep(int v92ControlSleep) {
		this.v92ControlSleep = v92ControlSleep;
	}
}