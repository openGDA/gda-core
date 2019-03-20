/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package org.eclipse.scanning.sequencer;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.scanning.api.event.EventConstants;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.IConsumer;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.eclipse.scanning.api.ui.CommandConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Helper class to give easy access to submission and run queues
 *
 * Use this class to easily get lists of submitted, running and completed jobs, clear them or stop running jobs.
 *
 * Jython examples:
 *
 * >>> from org.eclipse.scanning.sequencer import MscanUtils
 * >>> MscanUtils.clearRunningAndCompleted()
 * >>> mscan(repeat('dummy1', 10000, 0.0, 0))
 * >>> MscanUtils.stopAll()
 * >>> MscanUtils.stopAllIn(5000) # ms
 * >>> mscan(repeat('dummy1', 10000, 0.0, 0))
 * >>> MscanUtils.clearQueue()
 * >>> MscanUtils.getSubmitted()
 * []
 * >>> MscanUtils.getRunningAndCompleted().size()
 * 2
 * >>> MscanUtils.getRunningFrom(MscanUtils.getRunningAndCompleted()).size()
 * 0
 * >>> MscanUtils.getCompletedFrom(MscanUtils.getRunningAndCompleted()).size()
 * 2
 * >>> MscanUtils.cleanUpCompleted()
 */
public final class MscanUtils {
	private static final Logger logger = LoggerFactory.getLogger(MscanUtils.class);

	private static final ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
	private static final IEventService service = ServiceHolder.getEventService();

	private static IConsumer<StatusBean> consumerProxy;

	private static IConsumer<StatusBean> getConsumerProxy() throws EventException, URISyntaxException {
		if (MscanUtils.consumerProxy == null) {
			MscanUtils.consumerProxy = service.createConsumerProxy(
				new URI(CommandConstants.getScanningBrokerUri()),
				EventConstants.SUBMISSION_QUEUE);
		}
		return MscanUtils.consumerProxy;
	}

	// Public methods

	/**
	 * Cleans up the status set by removing certain old jobs. <BR>
	 * Specifically, the jobs that are removed are those that meet one of the following criteria:
	 * <ul>
	 *   <li>jobs that have the status {@link Status#FAILED} or {@link Status#NONE};</li>
	 *   <li>jobs that are running (i.e. {@link Status#isRunning()} is <code>true</code>) and are older
	 *      than the maximum running age (by default, two days);</li>
	 *   <li>jobs that are final (i.e. {@link Status#isFinal()} is <code>true</code>) and are older than
	 *      the maximum complete age (by default, one week);</li>
	 *   <li>Additionally jobs that are not started or paused will have their status set to {@link Status#FAILED};</li>
	 * </ul>
	 * @see org.eclipse.scanning.api.event.core.IQueueConnection#clearQueue
	 */
	public static void cleanUpCompleted() {
		try {
			getConsumerProxy().cleanUpCompleted();
		} catch (EventException | URISyntaxException e) {
			logger.error("Error cleaning up completed jobs", e);
		}
	}

	/** Removes all pending jobs from the submission queue.
	 *
	 * @see org.eclipse.scanning.api.event.core.IQueueConnection#clearQueue
	 */
	public static void clearQueue() {
		try {
			getConsumerProxy().clearQueue();
		} catch (EventException | URISyntaxException e) {
			logger.error("Error clearing submission queue", e);
		}
	}

	/** Removes all completed jobs from the consumer's status set.
	 *
	 * @see org.eclipse.scanning.api.event.core.IQueueConnection#clearRunningAndCompleted
	 */
	public static void clearRunningAndCompleted() {
		try {
			getConsumerProxy().clearRunningAndCompleted();
		} catch (EventException | URISyntaxException e) {
			logger.error("Error clearing running and completed jobs", e);
		}
	}

	/**
	 * @param runningAndCompleted
	 * @return list of final (terminated, failed, complete and unfinished) jobs
	 */
	public static List<StatusBean> getCompletedFrom(List<StatusBean> runningAndCompleted) {
		return runningAndCompleted.stream().filter(x -> x.getStatus().isFinal()).collect(Collectors.toList());
	}

	/**
	 * @return list of running and completed jobs
	 * @throws EventException
	 * @throws URISyntaxException
	 */
	public static List<StatusBean> getRunningAndCompleted() throws EventException, URISyntaxException {
		return getConsumerProxy().getRunningAndCompleted();
	}

	/** See {@linkplain org.eclipse.scanning.api.event.status.Status#isActive}
	 *
	 * @param runningAndCompleted
	 * @return list of active (running, paused or resumed) jobs
	 */
	public static List<StatusBean> getRunningFrom(List<StatusBean> runningAndCompleted) {
		return runningAndCompleted.stream().filter(x -> x.getStatus().isActive()).collect(Collectors.toList());
	}

	/**
	 * @return list of submitted jobs
	 * @throws EventException
	 * @throws URISyntaxException
	 */
	public static List<StatusBean> getSubmitted() throws EventException, URISyntaxException {
		return getConsumerProxy().getSubmissionQueue();
	}

	/**
	 * Stop all active (running, paused or resumed) jobs
	 */
	public static void stopAll() {
		try {
			logger.trace("stopAll() called, service={}, consumerProxy={}", service, getConsumerProxy());
			List<StatusBean> runningAndCompleted = getRunningAndCompleted();
			List<StatusBean> running = getRunningFrom(runningAndCompleted);
			logger.trace("Selected {} from {}", running, runningAndCompleted);
			for(StatusBean bean : running) {
				try {
					getConsumerProxy().terminateJob(bean);
					logger.info("Requesting termination of {}", bean.getName());
				} catch (Exception e) {
					logger.error("Error requesting termination of {}", bean.getName(), e);
				}
			}
		} catch (EventException | URISyntaxException e) {
			logger.error("Error requesting list of running jobs", e);
		} finally {
			logger.trace("stopAll finally");
		}
	}

	/** Stop all active (running, paused or resumed) jobs after the given delay (in milliseconds)
	 *
	 * @param ms
	 */
	public static void stopAllIn(long ms) {
		logger.debug("stopAllIn({}) called, scheduling in {}ms...", ms, ms);
		scheduledExecutor.schedule(MscanUtils::stopAll, ms, TimeUnit.MILLISECONDS);
	}
}
