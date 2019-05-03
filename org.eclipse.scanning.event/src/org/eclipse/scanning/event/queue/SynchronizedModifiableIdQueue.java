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

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Queue;

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

	// LinkedHashMap provides natural ordering, so can be used as a queue as well as for quick lookup by unique id
	private final Map<String, E> queue = new LinkedHashMap<>();

	@Override
	public List<E> getElements() {
		synchronized (this) {
			return new ArrayList<>(queue.values());
		}
	}

	@Override
	public boolean contains(Object obj) {
		if (!(obj instanceof IdBean)) return false;
		synchronized (this) {
			return queue.containsKey(((IdBean) obj).getUniqueId());
		}
	}

	@Override
	public Object[] toArray() {
		synchronized (this) {
			return queue.values().toArray();
		}
	}

	@Override
	public <T> T[] toArray(T[] a) {
		synchronized (this) {
			return queue.values().toArray(a);
		}
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
			queue.clear();
		}
	}

	@Override
	public boolean offer(E e) {
		return add(e);
	}

	@Override
	public boolean add(E e) {
		synchronized (this) {
			return queue.put(e.getUniqueId(), e) == null;
		}
	}

	@Override
	public E remove() {
		synchronized (this) {
			final Iterator<E> iter = queue.values().iterator();
			final E removed = iter.next(); // throws NoSuchElementException if empty
			iter.remove();
			return removed;
		}
	}

	@Override
	public E poll() {
		synchronized (this) {
			if (queue.isEmpty()) {
				return null;
			}

			return remove();
		}
	}

	@Override
	public E element() {
		synchronized (this) {
			return queue.values().iterator().next(); // throws NoSuchElementException if empty
		}
	}

	@Override
	public E peek() {
		synchronized (this) {
			if (queue.isEmpty()) {
				return null;
			}

			return element();
		}
	}

	@Override
	public boolean replace(E e) {
		synchronized (this) {
			if (queue.containsKey(e.getUniqueId())) {
				queue.put(e.getUniqueId(), e);
			}
		}

		return false;
	}

	@Override
	public boolean moveUp(E e) {
		synchronized (this) {
			boolean foundElement = false;
			final List<E> items = new ArrayList<>(queue.values());
			for (ListIterator<E> iter = items.listIterator(); iter.hasNext(); ) {
				boolean hasPrevious = iter.hasPrevious();
				final E elem = iter.next();
				if (e.getUniqueId().equals(elem.getUniqueId())) {
					if (!hasPrevious) {
						throw new IndexOutOfBoundsException("The element is already at the head of the queue");
					}
					foundElement = true;
					iter.remove();
					iter.previous();
					iter.add(e);
					break;
				}
			}

			// since LinkedHashMap uses insertion order, we need to clear the map and add all the items again in the new order
			// note ListOrderedMap from Apache Commons would make this easier, but this is only a temporary solution anyway
			queue.clear();
			for (E elem : items) {
				queue.put(elem.getUniqueId(), elem);
			}

			return foundElement;
		}
	}

	@Override
	public boolean moveDown(E e) {
		synchronized (this) {
			boolean foundElement = false;
			final List<E> items = new ArrayList<>(queue.values());
			for (ListIterator<E> iter = items.listIterator(); iter.hasNext(); ) {
				final E elem = iter.next();
				if (e.getUniqueId().equals(elem.getUniqueId())) {
					if (!iter.hasNext()) {
						throw new IndexOutOfBoundsException("The element is already at the tail of the queue");
					}
					foundElement = true;
					iter.remove();
					iter.next();
					iter.add(e);
					break;
				}
			}

			// since LinkedHashMap uses insertion order, we need to clear the map and add all the items again in the new order
			// note ListOrderedMap from Apache Commons would make this easier, but this is only a temporary solution anyway
			queue.clear();
			for (E elem : items) {
				queue.put(elem.getUniqueId(), elem);
			}

			return foundElement;
		}
	}

	@Override
	public boolean remove(Object obj) {
		if (!(obj instanceof IdBean)) return false; // can't be in the queue

		return queue.remove(((IdBean) obj).getUniqueId()) != null;
	}

	/**
	 * Returns an iterator over the elements in this queue. Note: client code must manually sync on this object.
	 */
	@Override
	public Iterator<E> iterator() {
		return queue.values().iterator();
	}

	@Override
	public int size() {
		synchronized (this) {
			return queue.size();
		}
	}

}
