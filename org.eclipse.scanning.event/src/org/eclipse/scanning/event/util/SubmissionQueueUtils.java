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
import org.eclipse.scanning.api.event.queue.QueueStatus;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.eclipse.scanning.api.ui.CommandConstants;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class can be used to get information about the submission queue e.g. whether there it is currently empty,
 * or whether there is a job currently running.
 * TODO: refactor - add isQueueEmpty method to IJobQueue?
 */
public class SubmissionQueueUtils {

	private static final Logger logger = LoggerFactory.getLogger(SubmissionQueueUtils.class);

	private static IJobQueue<StatusBean> jobQueueProxy;

	private SubmissionQueueUtils() {
		// private constructor to prevent instantiation
	}

	private static IJobQueue<StatusBean> getJobQueueProxy() throws EventException {
		if (jobQueueProxy == null) {
			final BundleContext context = FrameworkUtil.getBundle(SubmissionQueueUtils.class).getBundleContext();
			final IEventService eventService = context.getService(context.getServiceReference(IEventService.class));
			try {
				URI queueUri = new URI(CommandConstants.getScanningBrokerUri());
				jobQueueProxy = eventService.createJobQueueProxy(queueUri, EventConstants.SUBMISSION_QUEUE);
			} catch (URISyntaxException e) {
				throw new EventException("Could not connect to submission queue", e);
			}
		}
		return jobQueueProxy;
	}

	/**
	 * Returns whether there is a job running or pending. This is the case if either a job is
	 * running, or the queue is running and not empty.
	 * @return <code>true</code> if a job is running or pending, <code>false</code> otherwise
	 */
	public static boolean isJobRunningOrPending() {
		try {
			return (isQueueRunning() && !isQueueEmpty()) || isJobRunning();
		} catch (EventException e) {
			logger.error("Could not read submission queue", e);
			return false;
		}
	}

	/**
	 * Returns whether the submission queue is currently running, i.e. it's state is {@link QueueStatus#RUNNING},
	 * if <code>false</code> it may be either {@link QueueStatus#PAUSED} or {@link QueueStatus#STOPPED}.
	 *
	 * @return <code>true</code> if the submission queue is running, <code>false</code> otherwise
	 * @throws EventException
	 */
	public static boolean isQueueRunning() throws EventException {
		return getJobQueueProxy().getQueueStatus() == QueueStatus.RUNNING;
	}

	public static boolean isQueueEmpty() throws EventException {
		return getJobQueueProxy().getSubmissionQueue().isEmpty();
	}

	public static boolean isJobRunning() throws EventException {
		List<StatusBean> runningOrCompletedScans = jobQueueProxy.getRunningAndCompleted();
		return !runningOrCompletedScans.stream().map(StatusBean::getStatus).allMatch(Status::isFinal);
	}

}
