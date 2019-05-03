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

package org.eclipse.scanning.test.event;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.eclipse.scanning.event.queue.IModifiableIdQueue;
import org.eclipse.scanning.event.queue.SynchronizedModifiableIdQueue;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SynchronizedModifiableIdQueueTest {

	private IModifiableIdQueue<StatusBean> queue;

	private List<StatusBean> beanCopies;

	@Before
	public void setUp() {
		queue = new SynchronizedModifiableIdQueue<>();
		List<String> beanNames = Arrays.asList("one", "two", "three", "four", "five");
		List<StatusBean> beans = beanNames.stream()
				.map(name -> createBean(name))
				.collect(toList());
		queue.addAll(beans);
		beanCopies = Collections.unmodifiableList(beans.stream().map(b -> new StatusBean(b)).collect(toList()));
	}

	private StatusBean createBean(String name) {
		StatusBean bean = new StatusBean(name);
		bean.setUniqueId(name); // use the name as the unique id as well
		bean.setStatus(Status.SUBMITTED);
		return bean;
	}

	@After
	public void tearDown() {
		queue = null;
	}

	private List<String> getIds(Collection<StatusBean> beans) {
		return beans.stream().map(StatusBean::getUniqueId).collect(toList());
	}

	@Test
	public void testSize() {
		assertThat(queue, hasSize(5));
	}

	@Test
	public void testIsEmpty() {
		assertThat(queue.isEmpty(), is(false));
		queue.clear();
		assertThat(queue.isEmpty(), is(true));
	}

	@Test
	public void testContains() {
		assertThat(queue.contains(beanCopies.get(2)), is(true));
		assertThat(queue.contains(createBean("nine")), is(false));

		// check that beans are matched by unique id not object equality
		StatusBean bean = beanCopies.get(2);
		bean.setUniqueId("eight");
		assertThat(queue.contains(bean), is(false));
	}

	@Test
	public void testIterator() {
		Iterator<StatusBean> queueIter = queue.iterator();
		Iterator<StatusBean> beanIter = beanCopies.iterator();
		while (queueIter.hasNext()) {
			assertThat(queueIter.next(), is(equalTo(beanIter.next())));
		}
	}

	@Test
	public void testToArray() {
		assertThat(queue.toArray(), is(equalTo(beanCopies.toArray())));
	}

	@Test
	public void testToArray2() {
		assertThat(queue.toArray(new StatusBean[queue.size()]),
				is(equalTo(beanCopies.toArray(new StatusBean[beanCopies.size()]))));
	}

	@Test
	public void testRemove_element() {
		// Tests remove(Object) method defined in Collection
		// Test removing an item from the queue
		assertThat(queue, hasSize(5));
		assertThat(queue.remove(beanCopies.get(3)), is(true));
		assertThat(queue, hasSize(4));
		assertThat(queue.contains(beanCopies.get(3)), is(false));
		assertThat(getIds(queue), contains("one", "two", "three", "five"));

		// Test removing an item that isn't in the queue
		assertThat(queue.remove(createBean("ten")), is(false));
		assertThat(queue, hasSize(4));

		// Test removing an item that has the same id as one in the queue, but is not equal
		StatusBean bean = createBean("foo");
		bean.setUniqueId("two");
		assertThat(queue.contains(bean), is(true));
		queue.remove(bean);
		assertThat(queue, hasSize(3));
		assertThat(queue.contains(bean), is(false));
		assertThat(getIds(queue), contains("one", "three", "five"));
	}

	@Test
	public void testContainsAll() {
		assertThat(queue.containsAll(Arrays.asList(beanCopies.get(2), beanCopies.get(3))), is(true));
		assertThat(queue.containsAll(Arrays.asList(beanCopies.get(1), createBean("six"))), is(false));
		assertThat(queue.containsAll(Arrays.asList(createBean("nine"), createBean("ten"))), is(false));
	}

	@Test
	public void testAddAll() {
		List<StatusBean> toAdd = Arrays.asList("six", "seven", "eight").stream()
				.map(name -> createBean(name)).collect(Collectors.toList());
		queue.addAll(toAdd);
		assertThat(queue, hasSize(8));
		assertThat(queue.containsAll(toAdd), is(true));
		assertThat(getIds(queue), contains("one", "two", "three", "four", "five", "six", "seven", "eight"));
	}

	@Test
	public void testRemoveAll() {
		List<StatusBean> toRemove = Arrays.asList(beanCopies.get(1), beanCopies.get(3), beanCopies.get(4));
		queue.removeAll(toRemove);
		assertThat(queue, hasSize(2));
		for (StatusBean bean : toRemove) {
			assertThat(queue.contains(bean), is(false));
		}
		assertThat(getIds(queue), contains("one", "three"));
	}

	@Test
	public void testRetainAll() {
		List<StatusBean> toRetain = Arrays.asList(beanCopies.get(1), beanCopies.get(3), beanCopies.get(4));
		queue.retainAll(toRetain);
		assertThat(queue.size(), is(3));
		assertThat(queue.toArray(), is(equalTo(toRetain.toArray())));
		assertThat(getIds(queue), contains("two", "four", "five"));
	}

	@Test
	public void testClear() {
		assertThat(queue.isEmpty(), is(false));
		queue.clear();
		assertThat(queue.isEmpty(), is(true));
	}

	@Test
	public void testAdd() {
		StatusBean bean = createBean("six");
		queue.add(bean);

		// check the new bean is added at the end of the queue
		assertThat(queue.size(), is(6));
		assertThat(getIds(queue), contains("one", "two", "three", "four", "five", "six"));
	}

	@Test
	public void testOffer() {
		StatusBean bean = createBean("six");
		queue.offer(bean);

		// check the new bean is added at the end of the queue
		assertThat(queue.size(), is(6));
		assertThat(getIds(queue), contains("one", "two", "three", "four", "five", "six"));
	}

	@Test
	public void testRemove() {
		// Test the remove method inherited from queue that removes the item at the head of the queue
		StatusBean removed = queue.remove();
		assertThat(removed, is(equalTo(beanCopies.get(0))));
		assertThat(queue.contains(removed), is(false));
		assertThat(queue, hasSize(4));
		assertThat(getIds(queue), contains("two", "three", "four", "five"));
	}

	@Test(expected = NoSuchElementException.class)
	public void testRemove_empty() {
		queue.clear();
		queue.remove();
	}

	@Test
	public void testPoll() {
		StatusBean result = queue.poll();
		assertThat(result, is(equalTo(beanCopies.get(0))));
		assertThat(queue.contains(result), is(false));
		assertThat(queue, hasSize(4));
		assertThat(getIds(queue), contains("two", "three", "four", "five"));

		queue.clear();
		assertThat(queue.poll(), is(nullValue()));
	}

	@Test
	public void testPeek() {
		StatusBean result = queue.peek();
		assertThat(result, is(equalTo(beanCopies.get(0))));
		assertThat(queue, hasSize(5));

		queue.clear();
		assertThat(queue.isEmpty(), is(true));
		assertThat(queue.peek(), is(nullValue()));
	}

	@Test
	public void testReplace() {
		StatusBean bean = new StatusBean("foo");
		assertThat(queue.contains(bean), is(false));
		bean.setUniqueId("two");
		assertThat(queue.contains(bean), is(true));

		queue.replace(bean);
		assertThat(queue.contains(bean), is(true));
		assertThat(queue.remove(), is(equalTo(beanCopies.get(0))));
		StatusBean bean2 = queue.remove();
		assertThat(bean2, is(not(equalTo(beanCopies.get(1)))));
		assertThat(bean2, is(sameInstance(bean)));
	}

	@Test
	public void testMoveDown() {
		StatusBean bean = createBean("three");
		queue.moveDown(bean);
		assertThat(getIds(queue), contains("one", "two", "four", "three", "five"));
	}

	@Test
	public void testMoveUp() {
		StatusBean bean = createBean("three");
		queue.moveUp(bean);
		assertThat(getIds(queue), contains("one", "three", "two", "four", "five"));
	}

}