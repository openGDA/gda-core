/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package gda.util;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceBase;
import gda.factory.FactoryException;

/**
 * Findable object used to invoke Runnable objects and then wait for a time
 */
public class TimedRunnableInvoker extends DeviceBase {

	private static final Logger logger = LoggerFactory.getLogger(TimedRunnableInvoker.class);

	Integer waitTime = 1000; // 1m

	boolean running = true;

	List<Runnable> runnables;

	Thread t;

	public TimedRunnableInvoker() {
		setLocal(true);
	}

	@Override
	public void configure() throws FactoryException {
		setConfigured(true);
		start();
	}

	void start() {
		if (!configured)
			return;
		if (t == null && runnables != null && !runnables.isEmpty() && isRunning()) {
			t = new Thread(new Runnable() {
				@Override
				public void run() {
					while (isRunning()) {
						for (Runnable runnable : runnables) {
							try {
								runnable.run();
							} catch (Throwable ex) {
								logger.error("Error running {}", runnable, ex);
							}
						}
						try {
							Thread.sleep(getWaitTime());
						} catch (InterruptedException e) {
							logger.warn("Ignoring interrupted exception", e);
							// do nothing
						}
					}
					t = null;
				}
			});
			t.start();
		}
	}

	/**
	 * @return wait time in ms
	 */
	public Integer getWaitTime() {
		return waitTime;
	}

	/**
	 * @param waitTime
	 *            wait time in ms
	 */
	public void setWaitTime(Integer waitTime) {
		this.waitTime = waitTime;
	}

	/**
	 * @return true if running
	 */
	public boolean isRunning() {
		return running;
	}

	/**
	 * @param running
	 *            true if running
	 */
	public void setRunning(boolean running) {
		this.running = running;
		if (running && configured)
			start();
	}

	/**
	 * @param runnables
	 */
	public void setRunnables(List<Runnable> runnables) {
		this.runnables = runnables;
	}
}
