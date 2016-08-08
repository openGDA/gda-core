/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.scanning.api.event.EventConstants;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.alive.PauseBean;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import uk.ac.gda.client.experimentdefinition.EventServiceHolder;

public class QueueAndRunExperimentNewQueueCommandHandler extends RunExperimentNewQueueCommandHandler {

	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(QueueAndRunExperimentNewQueueCommandHandler.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		if (event.getCommand().getId()
				.equals("uk.ac.gda.client.experimentdefinition.QueueAndRunMultiExperimentCommand")) {
			queueMultiScan();

		} else if (event.getCommand().getId()
				.equals("uk.ac.gda.client.experimentdefinition.QueueAndRunSingleExperimentCommand")) {

			queueSingleScan();

		} else if (event.getCommand().getId()
				.equals("uk.ac.gda.client.experimentdefinition.QueueAndRunSingleScanOnlyCommand")) {

			queueSingleScanSingleRepetition();
		}

		startQueue();

		return null;
	}

	/**
	 * Starts the new GDA9 queue by sending a {@link PauseBean} with
	 * <code>pause</code> set to <code>false</code>.
	 */
	private void startQueue() {
		IEventService eventService = EventServiceHolder.getEventService();
		if (eventService == null) {
			throw new IllegalStateException("Event service not set - should be set by OSGi DS");
		}

		try {
			URI jmsUri = new URI(LocalProperties.getActiveMQBrokerURI());
			IPublisher<PauseBean> publisher = eventService.createPublisher(jmsUri, EventConstants.CMD_TOPIC);
			publisher.setStatusSetName(EventConstants.CMD_SET);
			publisher.setStatusSetAddRequired(true);

			PauseBean bean = new PauseBean();
			bean.setQueueName(EventConstants.SUBMISSION_QUEUE);
			bean.setPause(false);
			publisher.broadcast(bean);
		} catch (EventException | URISyntaxException e) {
			logger.error("Cannot pause scan queue", e);
		}
	}

}
