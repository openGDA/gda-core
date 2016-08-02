/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package uk.ac.gda.client.experimentdefinition.ui.handlers;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.scanning.api.event.EventConstants;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.ISubmitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.commandqueue.CommandBean;
import gda.configuration.properties.LocalProperties;
import uk.ac.gda.client.experimentdefinition.EventServiceHolder;

/**
 * An instance of this class wraps an ActiveMQ connection and can be used to
 * submit {@link CommandBean}s.
 */
public class CommandBeanSubmitter {

	private static final Logger logger = LoggerFactory.getLogger(CommandBeanSubmitter.class);

	private ISubmitter<CommandBean> submitter;

	public void init() {
		submitter = createScanSubmitter();
	}

	private ISubmitter<CommandBean> createScanSubmitter() {
		IEventService eventService = EventServiceHolder.getEventService();
		if (eventService == null) {
			throw new NullPointerException("Event service is not set - check OSGi settings");
		}

		try {
			URI queueServiceURI = new URI(LocalProperties.getActiveMQBrokerURI());
			return eventService.createSubmitter(queueServiceURI, EventConstants.SUBMISSION_QUEUE);
		} catch (URISyntaxException e) {
			logger.error("URI syntax problem", e);
			throw new RuntimeException(e);
		}
	}

	public void submitScan(CommandBean bean) throws EventException {
		if (submitter == null) {
			throw new IllegalStateException(this.getClass().getSimpleName() + " has not been initialised.");
		}

		submitter.submit(bean);
	}

}
