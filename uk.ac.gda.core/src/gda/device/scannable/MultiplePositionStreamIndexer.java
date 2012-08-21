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

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.Callable;

/**
 * Used as a component in {@link PositionCallableProvider}s that implement {@link PositionInputStream}s an indexer can
 * be deferred to to very easily implement {@link PositionCallableProvider#getPositionCallable()}.
 * 
 * @param <T>
 */
public class MultiplePositionStreamIndexer<T> implements PositionCallableProvider<List<T>> {

	public class PositionStreamIndexPuller<T2> implements Callable<List<T2>> {

		private final int index;

		private final MultiplePositionStreamIndexer<T2> indexer;

		private boolean called = false;

		private List<T2> value;

		public PositionStreamIndexPuller(int index, MultiplePositionStreamIndexer<T2> indexer) {
			this.index = index;
			this.indexer = indexer;
		}

		@Override
		public List<T2> call() throws Exception {
			if (!called ) {
				value = indexer.get(index);
				called = true;
			}
			return value;
		}

	}

	private final List<PositionStreamIndexer<T>> streamIndexerList = new ArrayList<PositionStreamIndexer<T>>();

	List<T> entireStream = new ArrayList<T>();

	private int lastIndexGivenOut = -1;

	public MultiplePositionStreamIndexer(List<PositionInputStream<T>> streamList) {
		this(streamList, Integer.MAX_VALUE);
	}

	public MultiplePositionStreamIndexer(List<PositionInputStream<T>> streamList, int maxElementsToReadInOneGo) {
		for (PositionInputStream<T> stream : streamList) {
			streamIndexerList.add(new PositionStreamIndexer<T>(stream, maxElementsToReadInOneGo));
		}
	}

	public List<T> get(int index) throws NoSuchElementException, InterruptedException, DeviceException {
		List<T> values = new ArrayList<T>();
		for (PositionStreamIndexer<T> streamIndexer : streamIndexerList) {
			values.add(streamIndexer.get(index));
		}
		return values;
	}

	@Override
	public Callable<List<T>> getPositionCallable() throws DeviceException {
		lastIndexGivenOut += 1;
		return new PositionStreamIndexPuller<T>(lastIndexGivenOut, this);
	}

}
