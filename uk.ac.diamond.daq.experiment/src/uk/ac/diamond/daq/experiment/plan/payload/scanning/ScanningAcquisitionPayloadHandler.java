/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.experiment.plan.payload.scanning;

import static org.eclipse.scanning.server.servlet.Services.getRunnableDeviceService;

import java.net.URL;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.scan.ScanningException;
import org.springframework.stereotype.Component;

import uk.ac.diamond.daq.experiment.api.Activator;
import uk.ac.diamond.daq.experiment.api.ExperimentException;
import uk.ac.diamond.daq.experiment.api.plan.PayloadHandler;
import uk.ac.diamond.daq.experiment.api.structure.ExperimentController;
import uk.ac.diamond.daq.experiment.api.structure.ExperimentControllerException;
import uk.ac.diamond.daq.experiment.scans.mapping.QueuePreventingScanSubmitter;
import uk.ac.diamond.daq.mapping.api.document.ScanRequestFactory;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningAcquisition;
import uk.ac.gda.core.tool.spring.SpringApplicationContextFacade;

@Component
public class ScanningAcquisitionPayloadHandler implements PayloadHandler<ScanningAcquisitionPayload> {

	private QueuePreventingScanSubmitter scanSubmitter;
	private IEventService eventService;

	@Override
	public Class<?> getSourceClass() {
		return ScanningAcquisition.class;
	}

	@Override
	public Class<ScanningAcquisitionPayload> getTargetClass() {
		return ScanningAcquisitionPayload.class;
	}

	@Override
	public ScanningAcquisitionPayload wrap(Object rawPayload) {
		return new ScanningAcquisitionPayload((ScanningAcquisition) rawPayload, QueueResolution.DROP);
	}

	@Override
	public Object handle(ScanningAcquisitionPayload payload) {
		var scan = payload.getScan();
		try {
			URL url = SpringApplicationContextFacade.getBean(ExperimentController.class).prepareAcquisition(scan.getName());
			scan.setAcquisitionLocation(url);
		} catch (ExperimentControllerException e) {
			throw new ExperimentException(e);
		}

		var factory = new ScanRequestFactory(scan);
		try {
			var scanRequest = factory.createScanRequest(getRunnableDeviceService());
			var scanBean = new ScanBean(scanRequest);

			switch (payload.getQueueResolution()) {
			case DROP:
				getScanSubmitter().submitScan(scanBean);
				break;
			case STOP_PREVIOUS_SCANS:
				getScanSubmitter().submitImportantScan(scanBean);
				break;
			default:
				throw new IllegalArgumentException("Unsupported variant: " + payload.getQueueResolution());
			}

			return scanBean;

		} catch (ScanningException | EventException e) {
			throw new ExperimentException(e);
		}
	}

	private QueuePreventingScanSubmitter getScanSubmitter() {
		if (scanSubmitter == null) {
			scanSubmitter = new QueuePreventingScanSubmitter();
			scanSubmitter.setEventService(getEventService());
		}
		return scanSubmitter;
	}

	private IEventService getEventService() {
		if (eventService == null) {
			eventService = Activator.getService(IEventService.class);
		}
		return eventService;
	}

}
