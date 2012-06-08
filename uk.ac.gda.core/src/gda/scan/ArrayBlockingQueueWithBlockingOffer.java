/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

package gda.scan;

import java.util.concurrent.ArrayBlockingQueue;

public class ArrayBlockingQueueWithBlockingOffer<E> extends ArrayBlockingQueue<E> {
	
	public ArrayBlockingQueueWithBlockingOffer(int capacity) {
		super(capacity);
	}

	/**
	 * Overridden offer now blocks if queue is full. Returns true if offer was accepted, or
	 * false if the underlying put was interrupted before it succeeded.
	 * 
	 */
	@Override
	public boolean offer(E element) {
			try {
				put(element);
			} catch (InterruptedException e) {
				 // Restore the interrupted status
	             Thread.currentThread().interrupt();
				return false;
			}
			return true;
	}
}