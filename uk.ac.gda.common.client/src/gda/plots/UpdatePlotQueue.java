/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

package gda.plots;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Queue decoupling requests for scan plot updates to the actual update thread.
 */
/**
 *
 */
public class UpdatePlotQueue {
	private static final Logger logger = LoggerFactory.getLogger(UpdatePlotQueue.class);

	private static final String THREAD_NAME = UpdatePlotQueue.class.getSimpleName();

	private final BlockingQueue<XYDataHandler> items = new LinkedBlockingQueue<>();
	private final Thread thread;

	public UpdatePlotQueue() {
		thread = new Thread(this::runPlotQueueUpdates, THREAD_NAME);
		thread.setDaemon(true);
		thread.start();
		logger.debug("Started {} thread", THREAD_NAME);
	}


	/**
	 * Add a request for an update to the queue
	 *
	 * @param simplePlot
	 */
	public void update(XYDataHandler simplePlot) {
		items.add(simplePlot);
		logger.trace("Added update plot request '{}' to queue", simplePlot);
	}

	private void runPlotQueueUpdates() {
		while (!Thread.currentThread().isInterrupted()) {
			try {
				XYDataHandler item = items.take();
				item.onUpdate(false);
			} catch (InterruptedException e) {
				// We are about to be ended
				Thread.currentThread().interrupt();
			} catch (Exception e) {
				logger.error("Error running plot queue", e);
			}
		}
		logger.debug("{} thread ending", THREAD_NAME);
	}


	/**
	 * Causes the plot update thread to be killed
	 */
	public void kill() {
		logger.debug("Requested thread to be killed");
		thread.interrupt();
	}
}

