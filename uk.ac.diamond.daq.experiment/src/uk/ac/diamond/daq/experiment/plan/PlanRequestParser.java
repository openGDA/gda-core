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

import static uk.ac.diamond.daq.experiment.api.Services.getExperimentService;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.UUID;
import java.util.stream.Collectors;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.factory.Finder;
import uk.ac.diamond.daq.experiment.api.ExperimentException;
import uk.ac.diamond.daq.experiment.api.driver.DriverModel;
import uk.ac.diamond.daq.experiment.api.driver.IExperimentDriver;
import uk.ac.diamond.daq.experiment.api.plan.IPlan;
import uk.ac.diamond.daq.experiment.api.plan.ISampleEnvironmentVariable;
import uk.ac.diamond.daq.experiment.api.plan.ISegment;
import uk.ac.diamond.daq.experiment.api.plan.ITrigger;
import uk.ac.diamond.daq.experiment.api.plan.Payload;
import uk.ac.diamond.daq.experiment.api.plan.PayloadService;
import uk.ac.diamond.daq.experiment.api.remote.PlanRequest;
import uk.ac.diamond.daq.experiment.api.remote.SegmentRequest;
import uk.ac.diamond.daq.experiment.api.remote.TriggerRequest;
import uk.ac.diamond.daq.mapping.api.document.DocumentMapper;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningAcquisition;
import uk.ac.diamond.daq.service.CommonDocumentService;
import uk.ac.diamond.daq.service.command.receiver.CollectionCommandReceiver;
import uk.ac.diamond.daq.service.command.receiver.FilesCollectionCommandReceiver;
import uk.ac.diamond.daq.service.command.strategy.OutputStrategyFactory;
import uk.ac.gda.core.tool.spring.SpringApplicationContextFacade;

public class PlanRequestParser {

	private IPlan plan;
	private CommonDocumentService documentService;
	private DocumentMapper mapper;
	private PayloadService payloadService;
	
	private IExperimentDriver<DriverModel> driver;

	public IPlan parsePlanRequest(PlanRequest planRequest) throws DeviceException {
		plan = new Plan(planRequest.getName());

		if (planRequest.isDriverUsed()) {
			driver = Finder.find(planRequest.getDriverBean().getDriver());
			driver.setModel(getExperimentService().getDriverProfile(driver.getName(),
					planRequest.getDriverBean().getProfile(), planRequest.getName()));
			plan.setDriver(driver);
		}

		planRequest.getSegmentRequests().forEach(this::addSegment);

		return plan;
	}

	private ISegment addSegment(SegmentRequest request) {
		ITrigger[] triggers = request.getTriggerRequests().stream().map(this::convertToTrigger)
				.collect(Collectors.toList()).toArray(new ITrigger[0]);

		switch (request.getSignalSource()) {
		case TIME:
			return plan.addSegment(request.getName(), plan.addTimer(), request.getDuration(), triggers);

		case POSITION:
			Scannable scannable = getScannable(request.getSampleEnvironmentVariableName());
			ISampleEnvironmentVariable sev = plan.addSEV(scannable);
			return plan.addSegment(request.getName(), sev,
					request.getInequality().getLimitCondition(request.getInequalityArgument()), triggers);

		default:
			throw new IllegalStateException("Not a recognised signal source (" + request.getSignalSource() + ")");
		}
	}

	private Payload getScanPayload(UUID scanId) {
		OutputStream output = new ByteArrayOutputStream();
		CollectionCommandReceiver<ScanningAcquisition> receiver = new FilesCollectionCommandReceiver<>(ScanningAcquisition.class, output);
		try {
			getDocumentService().selectDocument(receiver, scanId, OutputStrategyFactory.getJSONOutputStrategy());
			var document = getMapper().convertFromJSON(output.toString(), ScanningAcquisition.class);
			return getPayloadService().wrap(document);
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

	private PayloadService getPayloadService() {
		if (payloadService == null) {
			payloadService = SpringApplicationContextFacade.getBean(PayloadService.class);
		}
		return payloadService;
	}

	private ITrigger convertToTrigger(TriggerRequest request) {

		var scanPayload = getScanPayload(request.getScanId());

		ISampleEnvironmentVariable sev;

		switch (request.getSignalSource()) {
		case POSITION:
			Scannable scannable = getScannable(request.getSampleEnvironmentVariableName());
			sev = plan.addSEV(scannable);
			break;

		case TIME:
			sev = plan.addTimer();
			break;

		default:
			throw new IllegalStateException("Unrecognised signal source ('" + request.getSignalSource() + "')");
		}

		switch (request.getExecutionPolicy()) {
		case REPEATING:
			return plan.addTrigger(request.getName(), scanPayload, sev, request.getInterval());

		case SINGLE:
			return plan.addTrigger(request.getName(), scanPayload, sev, request.getTarget(), request.getTolerance());

		default:
			throw new IllegalStateException("Unrecognised execution policy ('" + request.getExecutionPolicy() + "')");
		}
	}
	
	/**
	 * a sample environment could be any scannable or a driver readout
	 * (which is a scannable with a different name) 
	 */
	private Scannable getScannable(String name) {
		var scannable = driver != null ? driver.getReadout(name) : null;
		return scannable != null ? scannable : Finder.find(name);
	}

}