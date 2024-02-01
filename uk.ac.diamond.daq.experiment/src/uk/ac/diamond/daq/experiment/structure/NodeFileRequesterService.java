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

package uk.ac.diamond.daq.experiment.structure;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.IRequester;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import gda.configuration.properties.LocalProperties;
import uk.ac.diamond.daq.experiment.api.Activator;
import uk.ac.diamond.daq.experiment.api.structure.ExperimentController;
import uk.ac.diamond.daq.experiment.api.structure.NodeInsertionRequest;

/**
 * Service through which the {@link ExperimentController} requests the insertion of node files
 * as they are created during the experiment.
 * <p>
 * The ActiveMQ topics through which the requests operate can be modified by setting the properties
 * <ul>
 * <li>{@code experiment.structure.job.request.topic}, and</li>
 * <li>{@code experiment.structure.job.response.topic}</li>
 * </ul>
 */
@Service
public class NodeFileRequesterService {

	@Value("${experiment.structure.job.request.topic:uk.ac.diamond.daq.experiment.structure.job.request.topic}")
	private String requestTopic;

	@Value("${experiment.structure.job.response.topic:uk.ac.diamond.daq.experiment.structure.job.response.topic}")
	private String responseTopic;

	private IRequester<NodeInsertionRequest> nodeFileRequester;

	private IRequester<NodeInsertionRequest> getNodeFileRequester() throws EventException {
		if (nodeFileRequester == null) {
			createNodeFileRequester();
		}
		return nodeFileRequester;
	}

	private void createNodeFileRequester() throws EventException {
		try {
			URI jmsURI = new URI(LocalProperties.getBrokerURI());
			IEventService eventService = Activator.getService(IEventService.class);
			nodeFileRequester = eventService.createRequestor(jmsURI, requestTopic, responseTopic);
			nodeFileRequester.setTimeout(5, TimeUnit.SECONDS);
		} catch (URISyntaxException e) {
			throw new EventException("Cannot create submitter", e);
		}
	}

	public NodeInsertionRequest getNodeFileCreationRequestResponse(NodeInsertionRequest job)
			throws EventException, InterruptedException {
		return getNodeFileRequester().post(job);
	}
}
