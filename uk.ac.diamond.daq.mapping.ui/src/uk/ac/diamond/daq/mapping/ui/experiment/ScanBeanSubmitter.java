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

import org.eclipse.scanning.api.event.EventConstants;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.ISubmitter;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;

public class ScanBeanSubmitter {

	private static final Logger logger = LoggerFactory.getLogger(ScanBeanSubmitter.class);

	private IEventService eventService;

	private ISubmitter<ScanBean> submitter;

	/**
	 * Only for use by Equinox DS or in unit tests!
	 */
	public void setEventService(IEventService service) {
		eventService = service;
	}

	public void init() {
		submitter = createScanSubmitter();
	}

	private ISubmitter<ScanBean> createScanSubmitter() {
		if (eventService != null) {
			try {
				URI queueServerURI = new URI(LocalProperties.getActiveMQBrokerURI());
				return eventService.createSubmitter(queueServerURI, EventConstants.SUBMISSION_QUEUE);

			} catch (URISyntaxException e) {
				logger.error("URI syntax problem", e);
				throw new RuntimeException(e);
			}
		}
		throw new NullPointerException("Event service is not set - check OSGi settings");
	}

	public void submitScan(ScanBean scanBean) throws EventException {
		submitter.submit(scanBean);
	}
}
