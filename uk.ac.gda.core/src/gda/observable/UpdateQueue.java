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

package gda.observable;

import java.util.Vector;

/**
 * In response to an update method on an IObserver being called the data is added to the queue and processed in an
 * update queue
 */
public class UpdateQueue implements Runnable {
	Vector<UpdateQueueItem> items = new Vector<UpdateQueueItem>();
	private final UpdateQueueItem[] itemsToBeHandledType = new UpdateQueueItem[0];
	private boolean killed = false;
	private Thread thread = null;

	/**
	 * @param observer
	 * @param theObserved
	 * @param changeCode
	 */
	public void addUpdateEvent(QueuedObserver observer, Object theObserved, Object changeCode) {
		synchronized (items) {
			items.add(new UpdateQueueItem(observer, theObserved, changeCode));
			if (thread == null) {
				thread = uk.ac.gda.util.ThreadManager.getThread(this);
				thread.start();
			}
			items.notifyAll();
		}
	}

	/**
	 * 
	 */
	public void dispose() {
		killed = true;
	}

	@Override
	public void run() {
		while (!killed) {
			UpdateQueueItem[] itemsToBeHandled = null;
			try {
				synchronized (items) {
					if (!killed && items.isEmpty())
						items.wait();
					if (!items.isEmpty()) {
						itemsToBeHandled = items.toArray(itemsToBeHandledType);
						items.clear();
					}
				}
			} catch (Throwable th) {
				th.printStackTrace();
			}
			if (itemsToBeHandled != null) {
				int numItems = itemsToBeHandled.length;
				for (int index = 0; index < numItems; index++) {
					UpdateQueueItem item = itemsToBeHandled[index];
					item.observer.queuedUpdate(item.theObserved, item.changeCode);
				}
			}
		}
	}

}

class UpdateQueueItem {
	final QueuedObserver observer;
	final Object theObserved, changeCode;

	UpdateQueueItem(QueuedObserver observer, Object theObserved, Object changeCode) {
		this.observer = observer;
		this.theObserved = theObserved;
		this.changeCode = changeCode;
	}
}
