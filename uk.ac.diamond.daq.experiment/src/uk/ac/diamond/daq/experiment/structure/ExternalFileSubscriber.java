/*-
 * Copyright © 2023 Diamond Light Source Ltd.
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
import java.util.EventListener;
import java.util.Map;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.bean.IBeanListener;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.scanning.server.servlet.Services;

import gda.configuration.properties.LocalProperties;
import uk.ac.diamond.daq.experiment.api.EventConstants;
import uk.ac.diamond.daq.experiment.api.structure.ExperimentControllerException;

/**
 * When external processing files are created/updated/finished,
 * they are announced on one of two topics, depending on whether they
 * are live (i.e. SWMR) files.
 *
 * Subscribers to both topics are encapsulated in this single class
 * to simplify an identical subscription.
 */
public class ExternalFileSubscriber {

	private ISubscriber<EventListener> liveFileSubscriber;
	private ISubscriber<EventListener> staticFileSubscriber;

	private final ExternalFileListener fileConsumer;

	/** extracts "filePath" from bean and invokes {@link #fileConsumer} */
	private final IBeanListener<Map<String, Object>> staticFileListener;

	/** like {@link #staticFileListener} but only invokes {@link #fileConsumer} if status is {@code FINISHED} */
	private final IBeanListener<Map<String, Object>> liveFileListener;

	public ExternalFileSubscriber(ExternalFileListener listener) {
		this.fileConsumer = listener;

		staticFileListener = event -> fileConsumer.externalFileAvailable(event.getBean().get("filePath").toString());
		liveFileListener = event -> {
			var bean = event.getBean();
			if (bean.get("status").equals("FINISHED")) {
				fileConsumer.externalFileAvailable(bean.get("filePath").toString());
			}
		};
	}

	interface ExternalFileListener {
		void externalFileAvailable(String filePath);
	}

	public void attachListener() throws ExperimentControllerException {
		try {
			getStaticFileSubscriber().addListener(staticFileListener);
			getLiveFileSubscriber().addListener(liveFileListener);
		} catch (EventException | URISyntaxException e) {
			throw new ExperimentControllerException("Error attaching external file listener", e);
		}
	}

	public void dettachListener() throws ExperimentControllerException {
		try {
			getStaticFileSubscriber().removeListener(staticFileListener);
			getLiveFileSubscriber().removeListener(liveFileListener);
		} catch (URISyntaxException e) {
			throw new ExperimentControllerException("Error dettaching external file listener", e);
		}
	}


	private ISubscriber<EventListener> getStaticFileSubscriber() throws URISyntaxException {
		if (staticFileSubscriber == null) {
			staticFileSubscriber = createSubscriber(EventConstants.EXTERNAL_STATIC_FILE_PUBLISHED_TOPIC);
		}

		return staticFileSubscriber;
	}

	private ISubscriber<EventListener> getLiveFileSubscriber() throws URISyntaxException {
		if (liveFileSubscriber == null) {
			liveFileSubscriber = createSubscriber(EventConstants.EXTERNAL_LIVE_FILE_PUBLISHED_TOPIC);
		}

		return liveFileSubscriber;
	}


	private ISubscriber<EventListener> createSubscriber(String topic) throws URISyntaxException {
		var eventService = Services.getEventService();
		var activemqUri = new URI(LocalProperties.getActiveMQBrokerURI());
		return eventService.createSubscriber(activemqUri, topic);
	}


}
