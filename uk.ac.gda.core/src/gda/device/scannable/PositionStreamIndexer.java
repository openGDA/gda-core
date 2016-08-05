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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.scan.NamedQueueTask;

/**
 * Used as a component in {@link PositionCallableProvider}s that implement {@link PositionInputStream}s an indexer can
 * be deferred to to very easily implement {@link PositionCallableProvider#getPositionCallable()}.
 *
 * @param <T>
 */
public class PositionStreamIndexer<T> implements PositionCallableProvider<T> {

	private static final Logger logger = LoggerFactory.getLogger(PositionStreamIndexer.class);

	private final PositionInputStream<T> stream;
	private final int maxElementsToReadInOneGo;
	Map<Integer, T> readValuesNotGot = new HashMap<Integer, T>();
	private int lastIndexGivenOut = -1;
	private int lastIndexRead = -1;
	private Lock fairGetLock = new ReentrantLock(true);

	public PositionStreamIndexer(PositionInputStream<T> stream) {
		this(stream, Integer.MAX_VALUE);
	}

	public PositionStreamIndexer(PositionInputStream<T> stream, int maxElementsToReadInOneGo) {
		logger.trace("@{}.PositionStreamIndexer({}, {})", Integer.toHexString(hashCode()), stream, maxElementsToReadInOneGo);
		this.stream = stream;
		this.maxElementsToReadInOneGo = maxElementsToReadInOneGo;
	}

	/**
	 * Can only be called once for each index
	 */
	public T get(int index) throws NoSuchElementException, InterruptedException, DeviceException {
		try {
			fairGetLock.lockInterruptibly();
		} catch (InterruptedException e) {
			throw new InterruptedException("PositionStreamIndexer interrupted while waiting for element " + index);
		}

		try {
			// Keep reading until the indexed element is read from the stream.
			while (index > lastIndexRead) {
				List<T> values = stream.read(maxElementsToReadInOneGo);
				if (values.isEmpty()) {
					throw new IllegalStateException("stream returned an empty list (lastIndexRead=" + lastIndexRead + ")");
				}
				for (T value : values) {
					readValuesNotGot.put(++lastIndexRead, value);
				}
			}
			logger.trace("@{}.get({}) readValuesNotGot now {}", Integer.toHexString(hashCode()), index, readValuesNotGot);
			if (!readValuesNotGot.containsKey(index)) {
				throw new IllegalStateException("Element " + index
						+ " is not available. Values can only be got once (to avoid excessive memory use).");
			}
			return readValuesNotGot.remove(index);
		} finally {
			fairGetLock.unlock();
		}
	}

	@Override
	public Callable<T> getPositionCallable() throws DeviceException {
		return getNamedPositionCallable(null, 0);
	}

	public Callable<T> getNamedPositionCallable(String name, int threadPoolSize) {
		lastIndexGivenOut += 1;
		return  name != null ? new NamedPositionStreamIndexPuller<T>(lastIndexGivenOut, this,
				name, threadPoolSize) : new PositionStreamIndexPuller<T>(lastIndexGivenOut, this);
	}
}

class PositionStreamIndexPuller<T> implements Callable<T> {

	private static final Logger logger = LoggerFactory.getLogger(PositionStreamIndexPuller.class);

	private final int index;
	private final PositionStreamIndexer<T> indexer;
	private T value;

	private volatile boolean called = false;

	public PositionStreamIndexPuller(int index, PositionStreamIndexer<T> indexer) {
		this.index = index;
		this.indexer = indexer;
		logger.trace("@{}.PositionStreamIndexPuller({}, {})", Integer.toHexString(hashCode()), index, indexer);
	}

	@Override
	public T call() throws Exception {
		if (!called) {
			try {
				value = indexer.get(index);
			} finally {
				called = true;
			}
		} else {
			logger.warn("@{}.call method called twice for index: {}", Integer.toHexString(hashCode()), index);
		}
		return value;
	}
}

class NamedPositionStreamIndexPuller<T> extends PositionStreamIndexPuller<T> implements NamedQueueTask {

	private static final Logger logger = LoggerFactory.getLogger(NamedPositionStreamIndexPuller.class);

	private String name;
	private final int threadPoolSize;

	public NamedPositionStreamIndexPuller(int index, PositionStreamIndexer<T> indexer, String name, int threadPoolSize) {
		super(index, indexer);
		this.name = name;
		this.threadPoolSize = threadPoolSize;
		logger.trace("@{}.NamedPositionStreamIndexPuller({}, {}, {}, {})", Integer.toHexString(hashCode()),
				index, indexer, name, threadPoolSize);
	}

	@Override
	public String getExecutorServiceName() {
		return name;
	}

	@Override
	public int getThreadPoolSize() {
		return threadPoolSize;
	}
}
