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

package org.eclipse.scanning.event.util;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
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

	private final LinkedList<E> queue = new LinkedList<>();

	private boolean hasSameId(IdBean bean1, IdBean bean2) {
		return bean1.getUniqueId().equals(bean2.getUniqueId());
	}

	@Override
	public boolean contains(Object obj) {
		if (!(obj instanceof IdBean)) return false;
		final IdBean bean = (IdBean) obj;
		synchronized (this) {
			return queue.stream().anyMatch(b -> hasSameId(b, bean));
		}
	}

	@Override
	public Object[] toArray() {
		synchronized (this) {
			return queue.toArray();
		}
	}

	@Override
	public <T> T[] toArray(T[] a) {
		synchronized (this) {
			return queue.toArray(a);
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
			return queue.addAll(c);
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
			return queue.add(e);
		}
	}

	@Override
	public E remove() {
		synchronized (this) {
			return queue.remove();
		}
	}

	@Override
	public E poll() {
		synchronized (this) {
			return queue.poll();
		}
	}

	@Override
	public E element() {
		synchronized (this) {
			return queue.element();
		}
	}

	@Override
	public E peek() {
		synchronized (this) {
			return queue.peek();
		}
	}

	@Override
	public boolean replace(E e) {
		synchronized (this) {
			for (ListIterator<E> iter = queue.listIterator(); iter.hasNext(); ) {
				final E elem = iter.next();
				if (hasSameId(elem, e)) {
					iter.set(e);
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public boolean moveUp(E e) {
		synchronized (this) {
			for (ListIterator<E> iter = queue.listIterator(); iter.hasNext(); ) {
				final E elem = iter.next();
				if (hasSameId(e, elem)) {
					if (!iter.hasPrevious()) {
						throw new IndexOutOfBoundsException("The element is already at the head of the queue");
					}
					iter.remove();
					iter.previous();
					iter.add(e);
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public boolean moveDown(E e) {
		synchronized (this) {
			for (ListIterator<E> iter = queue.listIterator(); iter.hasNext(); ) {
				final E elem = iter.next();
				if (hasSameId(e, elem)) {
					if (!iter.hasNext()) {
						throw new IndexOutOfBoundsException("The element is already at the tail of the queue");
					}
					iter.remove();
					iter.next();
					iter.add(e);
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public boolean remove(Object obj) {
		if (!(obj instanceof IdBean)) return false; // can't be in the queue

		final IdBean e = (IdBean) obj;
		synchronized (this) {
			for (Iterator<E> iter = queue.iterator(); iter.hasNext(); ) {
				final E elem = iter.next();
				if (hasSameId(e, elem)) {
					iter.remove();
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Returns an iterator over the elements in this queue. Note: client code must manually sync on this object.
	 */
	@Override
	public Iterator<E> iterator() {
		return queue.iterator();
	}

	@Override
	public int size() {
		synchronized (queue) {
			return queue.size();
		}
	}

}
