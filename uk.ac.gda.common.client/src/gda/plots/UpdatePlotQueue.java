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

import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class UpdatePlotQueue implements Runnable {
	private static final Logger logger = LoggerFactory.getLogger(UpdatePlotQueue.class);

	private Vector<XYDataHandler> items = new Vector<XYDataHandler>();
	private final XYDataHandler[] itemsToBeHandledType = new XYDataHandler[0];

	private boolean killed = false;
	private Thread thread = null;

	public boolean isKilled() {
		return killed;
	}

	public void setKilled(boolean killed) {
		this.killed = killed;
	}

	private int plotPeriodMS=500;

	/**
	 * @param simplePlot
	 */
	public void update(XYDataHandler simplePlot) {
		synchronized (items) {
			if( items.contains(simplePlot))
				return;
			items.add(simplePlot);
			if (thread == null) {
				thread = uk.ac.gda.util.ThreadManager.getThread(this, "XYDataHandler:UpdatePlotQueue");
			}
			items.notifyAll();
		}
		if( !thread.isAlive())
			thread.start();
	}

	@Override
	public void run() {
		while (!killed) {
			try {
				XYDataHandler[] itemsToBeHandled = null;
				synchronized (items) {
					if (!killed && items.isEmpty())
						items.wait();
					if (!items.isEmpty()) {
						itemsToBeHandled = items.toArray(itemsToBeHandledType);
						items.clear();
					}
				}
				if (itemsToBeHandled != null && !killed) {
					Thread.sleep(plotPeriodMS);
					int numItems = itemsToBeHandled.length;
					for (int index = 0; index < numItems; index++) {
						try {
							XYDataHandler simplePlot = itemsToBeHandled[index];
							if( simplePlot != null){
								simplePlot.onUpdate(false);
								//remove duplicates as another update of the same plot is not required
								for (int j = index; j < numItems; j++) {
									if (itemsToBeHandled[j] == simplePlot)
										itemsToBeHandled[j]=null;
								}
							}
						} catch (Exception ex) {
							logger.error("Error running plot queue", ex);
						}
					}
				}
			} catch (Exception e) {
				logger.error("Error running plot queue", e);
			}
		}
	}

	public void setPlotPeriodMS(int plotPeriodMS) {
		this.plotPeriodMS = plotPeriodMS;
	}
}

