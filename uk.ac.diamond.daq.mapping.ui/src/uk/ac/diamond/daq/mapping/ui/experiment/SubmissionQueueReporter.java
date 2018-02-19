/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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
import java.util.List;

import org.eclipse.scanning.api.event.EventConstants;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.ISubmitter;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;

/**
 * This class can be instantiated to get information about the submission queue e.g. whether there it is currently empty.
 */
public class SubmissionQueueReporter {

	private static final Logger logger = LoggerFactory.getLogger(SubmissionQueueReporter.class);

	/**
	 * Returns whether the submission queue is empty, i.e. there are no currently running or submitted scans.
	 * @return <code>true</code> if there are no running or submitted scans, <code>false</code> otherwise
	 */
	public boolean isQueueEmpty() {
		try {
			final ISubmitter<StatusBean> queueConnection = getQueueConnection();
			// first check whether there are submitted scans which haven't been run yet
			final boolean noSubmittedScans = queueConnection.getQueue(EventConstants.SUBMISSION_QUEUE).isEmpty();
			boolean queueClear = noSubmittedScans;
			if (noSubmittedScans) {
				// if not check whether any scans that have been run are complete (or some other final state)
				List<StatusBean> runningOrCompletedScans = queueConnection.getQueue(EventConstants.STATUS_SET);
				queueClear = runningOrCompletedScans.stream().map(StatusBean::getStatus).allMatch(Status::isFinal);
			}
			return queueClear;
		} catch (URISyntaxException | EventException e) {
			logger.error("Could not read submission queue", e);
			return false;
		}
	}

	private ISubmitter<StatusBean> getQueueConnection() throws URISyntaxException {
		final BundleContext context = FrameworkUtil.getBundle(SubmissionQueueReporter.class).getBundleContext();
		final IEventService eventService = context.getService(context.getServiceReference(IEventService.class));
		final URI queueUri = new URI(LocalProperties.getActiveMQBrokerURI());
		return eventService.createSubmitter(queueUri, EventConstants.SUBMISSION_QUEUE);
	}

}
