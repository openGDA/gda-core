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
import uk.ac.diamond.daq.experiment.api.structure.NodeFileCreationRequest;

@Service
public class NodeFileRequesterService {

	@Value("${experiment.structure.job.request.topic:uk.ac.diamond.daq.experiment.structure.job.request.topic}")
	private String requestTopic;

	@Value("${experiment.structure.job.response.topic:uk.ac.diamond.daq.experiment.structure.job.response.topic}")
	private String responseTopic;

	private IRequester<NodeFileCreationRequest> nodeFileRequester;

	private IRequester<NodeFileCreationRequest> getNodeFileRequester() throws EventException {
		if (nodeFileRequester == null) {
			createNodeFileRequester();
		}
		return nodeFileRequester;
	}

	private void createNodeFileRequester() throws EventException {
		try {
			URI activemqURL = new URI(LocalProperties.getActiveMQBrokerURI());
			IEventService eventService = Activator.getService(IEventService.class);
			nodeFileRequester = eventService.createRequestor(activemqURL, requestTopic, responseTopic);
			nodeFileRequester.setTimeout(5, TimeUnit.SECONDS);
		} catch (URISyntaxException e) {
			throw new EventException("Cannot create submitter", e);
		}
	}

	public NodeFileCreationRequest getNodeFileCreationRequestResponse(NodeFileCreationRequest job)
			throws EventException, InterruptedException {
		return getNodeFileRequester().post(job);
	}
}
