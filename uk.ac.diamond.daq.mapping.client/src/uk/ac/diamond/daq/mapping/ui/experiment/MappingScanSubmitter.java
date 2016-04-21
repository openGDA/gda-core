/*-
 * Copyright © 2015 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.ui.experiment;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.ISubmitter;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import uk.ac.diamond.daq.mapping.api.IDetectorModelWrapper;
import uk.ac.diamond.daq.mapping.api.IMappingScanRegion;
import uk.ac.diamond.daq.mapping.api.MappingExperimentStatusBean;

public class MappingScanSubmitter {

	private static final Logger logger = LoggerFactory.getLogger(MappingScanSubmitter.class);

	private static IEventService eventService;

	private ISubmitter<ScanBean> submitter;

	/**
	 * Only for use by Equinox DS or in unit tests!
	 *
	 * @param service
	 */
	public synchronized void setEventService(IEventService service) {
		eventService = service;
	}

	public synchronized void unsetEventService(IEventService service) {
		if (eventService == service) {
			eventService = null;
		}
	}

	public void init() {
		submitter = createScanSubmitter();
	}

	private ISubmitter<ScanBean> createScanSubmitter() {
		if (eventService != null) {
			try {
				// FIXME These hard coded URIs need replacing somehow
				URI queueServerURI = new URI(LocalProperties.get(LocalProperties.GDA_ACTIVEMQ_BROKER_URI, ""));
				return eventService.createSubmitter(queueServerURI, IEventService.SUBMISSION_QUEUE);

			} catch (URISyntaxException e) {
				logger.error("URI syntax problem", e);
				throw new RuntimeException(e);
			}
		}
		throw new NullPointerException("Event service is not set - check OSGi settings");
	}

	public void submitScan(MappingExperimentStatusBean eBean) throws EventException {

		ScanBean scanBean = new ScanBean();
		String sampleName = eBean.getMappingExperimentBean().getSampleMetadata().getSampleName();
		if (sampleName == null || sampleName.length() == 0) {
			sampleName = "unknown sample";
		}
		String pathName = eBean.getMappingExperimentBean().getScanDefinition().getMappingScanRegion().getScanPath().getName();
		scanBean.setName(String.format("%s - %s Scan", sampleName, pathName));
		ScanRequest<IROI> req = new ScanRequest<IROI>();
		scanBean.setScanRequest(req);

		IMappingScanRegion scanRegion = eBean.getMappingExperimentBean().getScanDefinition().getMappingScanRegion();
		req.setModels(scanRegion.getScanPath());
		req.putRegion(scanRegion.getScanPath().getUniqueKey(), scanRegion.getRegion().toROI());

		for (IDetectorModelWrapper detectorWrapper : eBean.getMappingExperimentBean().getDetectorParameters()) {
			if (detectorWrapper.isIncludeInScan()) {
				req.putDetector(detectorWrapper.getName(), detectorWrapper.getModel());
			}
		}

		submitter.submit(scanBean);
	}
}
