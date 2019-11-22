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

package org.eclipse.scanning.event.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.eclipse.scanning.api.event.EventConstants;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.IJobQueue;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.eclipse.scanning.api.ui.CommandConstants;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class can be used to get information about the submission queue e.g. whether there it is currently empty.
 * TODO: refactor - add isQueueEmpty method to IJobQueue?
 */
public class QueueUtils {

	private static final Logger logger = LoggerFactory.getLogger(QueueUtils.class);

	private QueueUtils() {
		// private constructor to prevent instantiation
	}

	/**
	 * Returns whether the submission queue is empty, i.e. there are no currently running or submitted scans.
	 * @return <code>true</code> if there are no running or submitted scans, <code>false</code> otherwise
	 */
	public static boolean isQueueEmpty() {
		try (IJobQueue<StatusBean> jobQueueProxy = createJobQueueProxy()) {
			// first check whether there are submitted scans which haven't been run yet
			final boolean noSubmittedScans = jobQueueProxy.getSubmissionQueue().isEmpty();
			boolean queueClear = noSubmittedScans;
			if (noSubmittedScans) {
				// if not check whether any scans that have been run are complete (or some other final state)
				List<StatusBean> runningOrCompletedScans = jobQueueProxy.getRunningAndCompleted();
				queueClear = runningOrCompletedScans.stream().map(StatusBean::getStatus).allMatch(Status::isFinal);
			}
			return queueClear;
		} catch (URISyntaxException | EventException e) {
			logger.error("Could not read submission queue", e);
			return false;
		}
	}

	private static IJobQueue<StatusBean> createJobQueueProxy() throws EventException, URISyntaxException {
		final BundleContext context = FrameworkUtil.getBundle(QueueUtils.class).getBundleContext();
		final IEventService eventService = context.getService(context.getServiceReference(IEventService.class));
		final URI queueUri = new URI(CommandConstants.getScanningBrokerUri());
		return eventService.createJobQueueProxy(queueUri, EventConstants.SUBMISSION_QUEUE);
	}

}
