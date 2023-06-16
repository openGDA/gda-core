/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.experiment.plan;


import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.UUID;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.factory.Finder;
import uk.ac.diamond.daq.experiment.api.ExperimentException;
import uk.ac.diamond.daq.experiment.api.ExperimentService;
import uk.ac.diamond.daq.experiment.api.driver.DriverSignal;
import uk.ac.diamond.daq.experiment.api.driver.ExperimentDriver;
import uk.ac.diamond.daq.experiment.api.plan.IPlan;
import uk.ac.diamond.daq.experiment.api.plan.LimitCondition;
import uk.ac.diamond.daq.experiment.api.remote.ExecutionPolicy;
import uk.ac.diamond.daq.experiment.api.remote.PlanRequest;
import uk.ac.diamond.daq.experiment.api.remote.SegmentRequest;
import uk.ac.diamond.daq.experiment.api.remote.SignalSource;
import uk.ac.diamond.daq.experiment.api.remote.TriggerRequest;
import uk.ac.diamond.daq.mapping.api.document.DocumentMapper;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningAcquisition;
import uk.ac.diamond.daq.service.CommonDocumentService;
import uk.ac.diamond.daq.service.command.receiver.CollectionCommandReceiver;
import uk.ac.diamond.daq.service.command.receiver.FilesCollectionCommandReceiver;
import uk.ac.diamond.daq.service.command.strategy.OutputStrategyFactory;
import uk.ac.gda.core.tool.spring.SpringApplicationContextFacade;

public class PlanRequestParser {

	private PlanCreator creator;
	private CommonDocumentService documentService;
	private DocumentMapper mapper;

	private ExperimentDriver driver;

	public IPlan parsePlanRequest(PlanRequest planRequest) throws DeviceException {

		if (planRequest.isDriverUsed()) {
			driver = Finder.find(planRequest.getDriverBean().getDriver());
			driver.setModel(Finder.findSingleton(ExperimentService.class).getDriverProfile(driver.getName(),
					planRequest.getDriverBean().getProfile(), planRequest.getName()));
		}

		creator = new PlanCreator(planRequest.getName());

		planRequest.getSegmentRequests().forEach(this::addSegment);

		var plan = creator.create();

		if (driver != null) {
			plan.setDriver(driver);
		}

		return plan;
	}

	private void addSegment(SegmentRequest request) {
		var segmentFactory = creator.addSegment(request.getName());

		switch (request.getSignalSource()) {
			case TIME -> segmentFactory.lastingInSeconds(request.getDuration());
			case POSITION -> segmentFactory
								.tracking(getScannable(request.getSampleEnvironmentVariableName()))
								.until(getLimitCondition(request));
		}

		var triggers = request.getTriggerRequests().stream().map(this::processTriggerRequest).toArray(TriggerFactory[]::new);
		segmentFactory.activating(triggers);

	}

	private TriggerFactory processTriggerRequest(TriggerRequest request) {
		var trigger = creator.createTrigger(request.getName());

		trigger.executing(getScan(request.getScanId()));

		if (request.getSignalSource().equals(SignalSource.POSITION)) {
			trigger.tracking(getScannable(request.getSampleEnvironmentVariableName()));
		} else if (request.getSignalSource().equals(SignalSource.TIME)) {
			trigger.timed();
		}

		if (request.getExecutionPolicy().equals(ExecutionPolicy.SINGLE)) {
			trigger.at(request.getTarget()).plusOrMinus(request.getTolerance());
		} else if (request.getExecutionPolicy().equals(ExecutionPolicy.REPEATING)) {
			trigger.every(request.getInterval()).withOffset(request.getOffset());
		}

		return trigger;
	}

	private LimitCondition getLimitCondition(SegmentRequest request) {
		return request.getInequality().getLimitCondition(request.getInequalityArgument());
	}

	private ScanningAcquisition getScan(UUID scanId) {
		OutputStream output = new ByteArrayOutputStream();
		CollectionCommandReceiver<ScanningAcquisition> receiver = new FilesCollectionCommandReceiver<>(ScanningAcquisition.class, output);
		try {
			getDocumentService().selectDocument(receiver, scanId, OutputStrategyFactory.getJSONOutputStrategy());
			return getMapper().convertFromJSON(output.toString(), ScanningAcquisition.class);
		} catch (Exception e) {
			throw new ExperimentException("Error retrieving scan payload", e);
		}
	}

	private CommonDocumentService getDocumentService() {
		if (documentService == null) {
			documentService = SpringApplicationContextFacade.getBean(CommonDocumentService.class);
		}
		return documentService;
	}

	private DocumentMapper getMapper() {
		if (mapper == null) {
			mapper = SpringApplicationContextFacade.getBean(DocumentMapper.class);
		}
		return mapper;
	}

	/**
	 * a sample environment could be any scannable or a driver readout
	 * (which is a scannable with a different name)
	 */
	private Scannable getScannable(String name) {
		String scannableName = name;

		if (driver != null) {
			var driverSignal = driver.getDriverSignals().stream()
					.filter(signal -> signal.signalName().equals(name))
					.map(DriverSignal::scannableName)
					.findAny();

			if (driverSignal.isPresent()) {
				scannableName = driverSignal.get();
			}
		}

		return Finder.find(scannableName);
	}
}