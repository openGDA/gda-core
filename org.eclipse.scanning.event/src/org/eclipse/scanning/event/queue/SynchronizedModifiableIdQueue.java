/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package org.eclipse.scanning.event.queue;

import static java.util.stream.Collectors.toList;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.scanning.api.event.IdBean;

/**
 * A synchronized (thread-safe) modifiable {@link Queue}, backed by a {@link LinkedList}.
 * In addition to implementing {@link Queue} in a thread-safe wasy, this class also
 * implements {@link IModifiableIdQueue} to add method to reorder and replace items in
 * the queue.
 * <p>
 * The beans in the queue must be {@link IdBean}s (or some sublass), and this class uses
 * the bean's unique id (as returned by {@link IdBean#getUniqueId()} rather than
 * object equality. Note: it is the responsibility of client code to ensure does not
 * contain multiple beans with the same unique id. If this requirement is not upheld
 * the behaviour of this class is undefined.
 * <p>
 * It is imperative that the user manually synchronize on the returned
 * queue when iterating over it:
 * <pre>
 *  Queue queue = new SynchronizedModifiableIdQueue();
 *     ...
 *  synchronized (queue) {
 *      Iterator i = queue.iterator(); // Must be in synchronized block
 *      while (i.hasNext())
 *         foo(i.next());
 *  }
 *  </pre>
 *  Failure to follow this advice may result in non-deterministic behavior.
 *  This warning also applies to any other behavior that the client wishes to
 *  be performed in an atomic manner.
 *
 *  @param <E> the class of objects in the queue
 */
public class SynchronizedModifiableIdQueue<E extends IdBean> extends AbstractCollection<E> implements IModifiableIdQueue<E> {

	private final Map<String, E> beansById;
	private final SortedMap<Integer, String> idsByQueuePosition;
	private int nextQueuePosition = 1;

	public SynchronizedModifiableIdQueue() {
		// TODO temporary code with non-persistent maps
		beansById = new HashMap<>();
		idsByQueuePosition = new TreeMap<>();
	}

	@Override
	public List<E> getElements() {
		synchronized (this) {
			return idsByQueuePosition.values().stream() // bean ids are streamed by queue order
					.map(beansById::get)
					.collect(toList());
		}
	}

	@Override
	public boolean contains(Object obj) {
		if (!(obj instanceof IdBean)) return false;
		synchronized (this) {
			return beansById.containsKey(((IdBean) obj).getUniqueId());
		}
	}

	@Override
	public Object[] toArray() {
		return getElements().toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return getElements().toArray(a);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		synchronized (this) {
			return super.containsAll(c);
		}
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		synchronized (this) {
			return super.addAll(c);
		}
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		synchronized (this) {
			return super.removeAll(c);
		}
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		synchronized (this) {
			return super.retainAll(c);
		}
	}

	@Override
	public void clear() {
		synchronized (this) {
			beansById.clear();
			idsByQueuePosition.clear();
		}
	}

	@Override
	public boolean offer(E e) {
		return add(e);
	}

	@Override
	public boolean add(E e) {
		synchronized (this) {
			if (beansById.containsKey(e.getUniqueId())) {
				throw new IllegalArgumentException("The queue already contains a bean with unique id " + e.getUniqueId());
			}

			beansById.put(e.getUniqueId(), e);
			idsByQueuePosition.put(nextQueuePosition, e.getUniqueId());
			nextQueuePosition++;
			return true;
		}
	}

	private void throwIfEmpty() {
		if (isEmpty()) {
			throw new NoSuchElementException("The queue is empty");
		}
	}

	@Override
	public E remove() {
		synchronized (this) {
			throwIfEmpty();
			return doRemove();
		}
	}

	@Override
	public E poll() {
		synchronized (this) {
			if (isEmpty()) {
				return null;
			}

			return doRemove();
		}
	}

	private E doRemove() {
		final Integer firstPositionNum = idsByQueuePosition.firstKey();
		final String firstBeanId = idsByQueuePosition.remove(firstPositionNum);
		return beansById.remove(firstBeanId);
	}

	@Override
	public E element() {
		synchronized (this) {
			throwIfEmpty();
			return doPeek();
		}
	}

	@Override
	public E peek() {
		synchronized (this) {
			if (isEmpty()) {
				return null;
			}

			return doPeek();
		}
	}

	private E doPeek() {
		final Integer firstBeanPos = idsByQueuePosition.firstKey();
		final String firstBeanId = idsByQueuePosition.get(firstBeanPos);
		return beansById.get(firstBeanId);
	}

	@Override
	public boolean replace(E e) {
		synchronized (this) {
			if (beansById.containsKey(e.getUniqueId())) {
				beansById.put(e.getUniqueId(), e);
			}
		}

		return false;
	}

	@Override
	public boolean moveUp(E e) {
		synchronized (this) {
			Map.Entry<Integer, String> entry = null;
			Map.Entry<Integer, String> prevEntry = null;
			boolean found = false;
			final Iterator<Map.Entry<Integer, String>> iter = idsByQueuePosition.entrySet().iterator();
			while (iter.hasNext() && !found) {
				prevEntry = entry;
				entry = iter.next();
				if (entry.getValue().equals(e.getUniqueId())) {
					found = true;
				}
			}

			if (found) {
				if (prevEntry == null) {
					throw new IndexOutOfBoundsException("The element is already at the head of the queue");
				}
				swap(prevEntry.getKey(), prevEntry.getValue(), entry.getKey(), entry.getValue());
			}
			return found;
		}
	}

	@Override
	public boolean moveDown(E e) {
		synchronized (this) {
			boolean found = false;
			Map.Entry<Integer, String> entry = null;
			final Iterator<Map.Entry<Integer, String>> iter = idsByQueuePosition.entrySet().iterator();
			while (iter.hasNext() && !found) {
				entry = iter.next();
				if (entry.getValue().equals(e.getUniqueId())) {
					found = true;
				}
			}

			if (found) {
				if (!iter.hasNext()) {
					throw new IndexOutOfBoundsException("The element is already at the tail of the queue");
				}

				Map.Entry<Integer, String> nextEntry = iter.next();
				swap(entry.getKey(), entry.getValue(), nextEntry.getKey(), nextEntry.getValue());
				return true;
			}
			return found;
		}
	}

	private void swap(Integer firstPos, String firstBean, Integer secondPos, String secondBean) {
		idsByQueuePosition.put(secondPos, firstBean);
		idsByQueuePosition.put(firstPos, secondBean);
	}

	@Override
	public boolean remove(Object obj) {
		if (!(obj instanceof IdBean)) return false; // can't be in the queue

		synchronized (this) {
			final String id = ((IdBean) obj).getUniqueId();
			idsByQueuePosition.values().removeIf(str -> str.equals(id));
			return beansById.remove(id) != null;
		}
	}

	/**
	 * Returns an iterator over the elements in this queue. Note: client code must manually sync on this object.
	 */
	@Override
	public Iterator<E> iterator() {
		return new Iterator<E>() {

			private final Iterator<String> idsByQueuePosIterator = idsByQueuePosition.values().iterator();

			private String currentId = null;

			@Override
			public boolean hasNext() {
				return idsByQueuePosIterator.hasNext();
			}

			@Override
			public E next() {
				currentId = idsByQueuePosIterator.next();
				return beansById.get(currentId);
			}

			@Override
			public void remove() {
				idsByQueuePosIterator.remove();
				beansById.remove(currentId);
			}

		};
	}

	@Override
	public int size() {
		synchronized (this) {
			return beansById.size();
		}
	}

}
