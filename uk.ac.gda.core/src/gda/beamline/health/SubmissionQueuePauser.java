/*-
 * Copyright © 2024 Diamond Light Source Ltd.
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

package gda.beamline.health;

import org.eclipse.scanning.api.event.EventConstants;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.IJobQueue;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.factory.ConfigurableBase;
import gda.factory.FactoryException;
import gda.observable.IObserver;
import uk.ac.diamond.osgi.services.ServiceProvider;

/**
 * This class provides functionality to resume or pause the submission queue
 * based on the state of a Scannable.
 */
public class SubmissionQueuePauser extends ConfigurableBase {
	private static Logger logger = LoggerFactory.getLogger(SubmissionQueuePauser.class);

	private Scannable monitor;

	/**
	 * Only resume on this position, pause on all others.
	 */
	private String goodStateName;
	private IJobQueue<? extends StatusBean> submissionQueue;
	private boolean paused = false;

	public SubmissionQueuePauser(Scannable monitor, String goodStateName) {
		this.monitor = monitor;
		this.goodStateName = goodStateName;
	}

	@Override
	public void configure() throws FactoryException {
		try {
			submissionQueue = ServiceProvider.getService(IEventService.class).getJobQueue(EventConstants.SUBMISSION_QUEUE);
		} catch(Exception e) {
			logger.error("Could not conect to {} queue", EventConstants.SUBMISSION_QUEUE, e);
			throw new FactoryException("Failed to configure the submission queue pauser", e);
		}

		IObserver handler = (source, arg) -> handleState();
		monitor.addIObserver(handler);
		setConfigured(true);
		handleState();
	}

	private void handleState() {
		try {
			var state = monitor.getPosition();
			updateQueue(state.toString());
		} catch(DeviceException e) {
			logger.error("Could not get the monitor position", e);
		}
	}

	private void updateQueue(String state) {
		try {
			if (state.equals(goodStateName) && paused) {
				submissionQueue.resume();
				paused = false;
			} else {
				submissionQueue.pause();
				paused = true;
			}
		} catch (EventException e) {
			logger.error("Could not update {} queue", EventConstants.SUBMISSION_QUEUE, e);
		}
	}
}
