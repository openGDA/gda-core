/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package gda.device.scannable;

import gda.device.DeviceException;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A PositionInputStream that combines N multiple PositionInputStreams of type <T> and returns N point Lists of <T>.
 *
 * @param <T>
 */
public class PositionInputStreamCombiner<T> implements PositionInputStream<List<T>> {

	
	private List<PositionInputStream<T>> streams;
	
	private List<Queue<T>> streamsQueues = new ArrayList<Queue<T>>();
	
	private Lock fairReadLock = new ReentrantLock(true);
	
	public PositionInputStreamCombiner(List<PositionInputStream<T>> streams) {
		this.streams = streams;
		for (int i = 0; i < streams.size(); i++) {
			streamsQueues.add(new ConcurrentLinkedQueue<T>());
		}
	}
	
	private int minimumQueueLength() {
		int min = Integer.MAX_VALUE;
		for (Queue<T> queue : streamsQueues) {
			if (queue.size() < min) {
				min = queue.size();
			}
		}
		return min;
	}
	
	@Override
	public List<List<T>> read(int maxToRead) throws NoSuchElementException, InterruptedException, DeviceException {

		try {
			fairReadLock.lockInterruptibly();
		} catch (InterruptedException e) {
			throw new InterruptedException("Interrupted while waiting to read from PositionInputStreamCombiner");
		}
		// Visit each queue and read into any that are empty
		for (int i = 0; i < streams.size(); i++) {
			if (streamsQueues.get(i).size() == 0) {
				List<T> newElements = streams.get(i).read(maxToRead);
				streamsQueues.get(i).addAll(newElements);
			}
		}

		// Pop an item from each queue until the shortest queue is empty
		int numToPop = minimumQueueLength();
		List<List<T>> elementLists = new ArrayList<List<T>>(numToPop);
		for (int rowIndex = 0; rowIndex < numToPop; rowIndex++) {
			List<T> elementList = new ArrayList<T>(streams.size());
			for (int streamIndex = 0; streamIndex < streams.size(); streamIndex++) {
				elementList.add(streamsQueues.get(streamIndex).remove());
			}
			elementLists.add(elementList);
		}

		fairReadLock.unlock();
		return elementLists;
	}

}
