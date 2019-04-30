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

import java.nio.ByteBuffer;
import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Queue;

import org.eclipse.scanning.api.event.IEventConnectorService;
import org.eclipse.scanning.api.event.IdBean;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.h2.mvstore.WriteBuffer;
import org.h2.mvstore.type.DataType;
import org.h2.mvstore.type.StringDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A persistent thread-safe modifiable {@link Queue}.
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
public class SynchronizedModifiableIdQueue<E extends IdBean> extends AbstractCollection<E> implements IPersistentModifiableIdQueue<E> {

	/**
	 * A custom {@link DataType} that serializes beans using the marshaller service.
	 * This is required as not all objects that form part of a ScanBean/ScanRequest
	 * object tree are Java serializable. (Also, Java serialization, which is how
	 * MV store (de)serializes objects by default is bad and may be removed in future).
	 */
	private final class JsonSerializableDataType implements DataType {

		@SuppressWarnings("unchecked")
		@Override
		public int compare(Object a, Object b) {
			if (a instanceof Comparable && b instanceof Comparable) {
				return ((Comparable<Object>) a).compareTo(b);
			}

			// it looks like this is only used to compare for equality, so this is ok
			return a.equals(b) ? 0 : 1;
		}

		@Override
		public int getMemory(Object obj) {
			try {
				return eventConnectorService.marshal(obj).getBytes().length + 64;
			} catch (Exception e) {
				logger.error("Could not serialize bean: {}", obj);
				throw new RuntimeException(e);
			}
		}

		@Override
		public void write(WriteBuffer buff, Object obj) {
			try {
				final String str = eventConnectorService.marshal(obj);
				StringDataType.INSTANCE.write(buff, str);
			} catch (Exception e) {
				logger.error("Could not serialize bean: {}", obj);
				throw new RuntimeException(e);
			}
		}

		@Override
		public void write(WriteBuffer buff, Object[] obj, int len, boolean key) {
			for (int i = 0; i < len; i++) {
				write(buff, obj[i]);
			}
		}

		@Override
		public Object read(ByteBuffer buff) {
			final String str = StringDataType.INSTANCE.read(buff);
			try {
				return eventConnectorService.unmarshal(str, null);
			} catch (Exception e) {
				logger.error("Could not deserialize bean: {}", e);
				throw new RuntimeException(e);
			}
		}

		@Override
		public void read(ByteBuffer buff, Object[] obj, int len, boolean key) {
			for (int i = 0; i < len; i++) {
				obj[i] = read(buff);
			}
		}

	}

	private static final Logger logger = LoggerFactory.getLogger(SynchronizedModifiableIdQueue.class);

	private final IEventConnectorService eventConnectorService;
	private final String queueName;
	private final MVStore store;
	private final MVMap<String, E> beansById;
	private final MVMap<Integer, String> idsByQueuePosition;

	private int nextQueuePosition = 1;

	public SynchronizedModifiableIdQueue(IEventConnectorService eventConnectorService,
			String dbStoreFilename, String queueName) {
		this(eventConnectorService, MVStore.open(dbStoreFilename), queueName);
	}

	public SynchronizedModifiableIdQueue(IEventConnectorService eventConnectorService,
			MVStore store, String queueName) {
		this.eventConnectorService = eventConnectorService;
		this.store = store;
		this.queueName = queueName;
		MVMap.Builder<String, E> mapBuilder = new MVMap.Builder<>();
		mapBuilder.setKeyType(StringDataType.INSTANCE);
		mapBuilder.setValueType(new JsonSerializableDataType());

		beansById = store.openMap(queueName, mapBuilder);
		idsByQueuePosition = store.openMap(queueName + "-posmap");
	}

	@Override
	public String getQueueName() {
		return queueName;
	}

	@Override
	public void close() {
		store.close();
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
			idsByQueuePosition.entrySet().stream()
				.filter(entry -> entry.getValue().equals(id))
				.map(Map.Entry::getKey)
				.findFirst()
				.ifPresent(idsByQueuePosition::remove);

			return beansById.remove(id) != null;
		}
	}

	/**
	 * Returns an iterator over the elements in this queue. Note: client code must manually sync on this queue object.
	 */
	@Override
	public Iterator<E> iterator() {
		return new Iterator<E>() {

			private final Iterator<Map.Entry<Integer, String>> idsByQueuePosIterator = idsByQueuePosition.entrySet().iterator();

			private Integer currentPos = null;
			private String currentId = null;

			@Override
			public boolean hasNext() {
				return idsByQueuePosIterator.hasNext();
			}

			@Override
			public E next() {
				final Map.Entry<Integer, String> currEntry = idsByQueuePosIterator.next();
				currentPos = currEntry.getKey();
				currentId = currEntry.getValue();
				return beansById.get(currentId);
			}

			@Override
			public void remove() {
				idsByQueuePosition.remove(currentPos);
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
