/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

import java.util.NoSuchElementException;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Used as a component in {@link PositionCallableProvider}s that implement {@link PositionInputStream}s an
 * indexer can be deferred to to very easily implement {@link PositionCallableProvider#getPositionCallable()}. 
 * 
 * @param <T>
 */
public class PositionStreamIndexer<T> implements PositionCallableProvider<T> {

	public class PositionStreamIndexPuller<T2> implements Callable<T2> {

		private final int index;

		private final PositionStreamIndexer<T2> indexer;

		public PositionStreamIndexPuller(int index, PositionStreamIndexer<T2> indexer) {
			this.index = index;
			this.indexer = indexer;
		}

		@Override
		public T2 call() throws Exception {
			return indexer.get(index);
		}

	}

	private final PositionInputStream<T> stream;

	private final int maxElementsToReadInOneGo;

	Vector<T> entireStream = new Vector<T>();

	private int lastIndexGivenOut = -1;
	
	private Lock fairGetLock = new ReentrantLock(true);

	public PositionStreamIndexer(PositionInputStream<T> stream) {
		this(stream, Integer.MAX_VALUE);
	}

	public PositionStreamIndexer(PositionInputStream<T> stream, int maxElementsToReadInOneGo) {
		this.stream = stream;
		this.maxElementsToReadInOneGo = maxElementsToReadInOneGo;
	}

	public T get(int index) throws NoSuchElementException, InterruptedException, DeviceException {
		fairGetLock.lock();
		// Keep reading until the indexed element is read from the stream.
		while (index > entireStream.size() - 1) {
			entireStream.addAll(stream.read(maxElementsToReadInOneGo));
		}
		T value = entireStream.get(index);
		entireStream.set(index, null); // keep absolute indexes while reducing memory use
		fairGetLock.unlock();
		return value;
	}

	@Override
	public Callable<T> getPositionCallable() throws DeviceException {
		lastIndexGivenOut += 1;
		return new PositionStreamIndexPuller<T>(lastIndexGivenOut, this);
	}

}
