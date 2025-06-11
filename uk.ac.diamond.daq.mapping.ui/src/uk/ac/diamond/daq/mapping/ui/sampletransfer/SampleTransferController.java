/*-
 * Copyright © 2025 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.ui.sampletransfer;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.scanning.api.event.EventConstants;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.bean.IBeanListener;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import uk.ac.diamond.osgi.services.ServiceProvider;
import uk.ac.gda.core.sampletransfer.SequenceRequest;
import uk.ac.gda.core.sampletransfer.StepStatus;

public class SampleTransferController {
	private static final Logger logger = LoggerFactory.getLogger(SampleTransferController.class);

	private final StepStatusListener listener;

	private URI uri;
	private IPublisher<SequenceRequest> publisher;
	private ISubscriber<IBeanListener<StepStatus>> subscriber;

    public SampleTransferController(StepStatusListener listener) {
        this.listener = listener;
    }

	/**
	 * Creates a publisher and a subscriber to send and receive messages related to sample transfer process.
	 */
    public void connect() {
    	var eventService = ServiceProvider.getService(IEventService.class);
		try {
			uri = new URI(LocalProperties.getBrokerURI());
		} catch (URISyntaxException e) {
			logger.error("Could not create URI", e);
		}

		publisher = eventService.createPublisher(uri, EventConstants.SAMPLE_TRANSFER_CMD_TOPIC);
		subscriber = eventService.createSubscriber(uri, EventConstants.SAMPLE_TRANSFER_SERVER_TOPIC);

		try {
	        subscriber.addListener(event -> {
	            StepStatus status = event.getBean();
	            switch (status.getStatus()) {
	                case RUNNING -> listener.onStepRunning(status);
	                case TERMINATED -> listener.onSequenceTerminated(status);
	                case COMPLETE -> listener.onSequenceCompleted(status);
	                case FAILED -> listener.onSequenceFailed(status);
	                default -> logger.warn("Received unknown sequence status: {}", status.getStatus());
	            }
	            listener.onSequenceStatusUpdate(status);
	        });
		} catch (EventException e) {
			logger.error("Could not connect to remote event", e);
		}
    }

    public void disconnect() {
		try {
			publisher.disconnect();
			subscriber.disconnect();
		} catch (EventException e) {
			logger.error("Error disconnecting messaging component", e);
		}
    }

	public void broadcast(SequenceRequest message) {
		try {
			publisher.broadcast(message);
		} catch (EventException e) {
			logger.error("Could not publish the message", e);
		}
	}

}
