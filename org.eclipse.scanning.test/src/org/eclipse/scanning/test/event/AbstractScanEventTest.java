/*-
 *******************************************************************************
 * Copyright (c) 2011, 2016 Diamond Light Source Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthew Gerring - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.scanning.test.event;

import static org.eclipse.scanning.api.event.status.Status.COMPLETE;
import static org.eclipse.scanning.api.event.status.Status.PREPARING;
import static org.eclipse.scanning.api.event.status.Status.RUNNING;
import static org.eclipse.scanning.api.event.status.Status.SUBMITTED;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.eclipse.scanning.api.event.EventConstants;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.scanning.api.event.scan.IScanListener;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.event.scan.ScanEvent;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.test.BrokerTest;
import org.junit.After;
import org.junit.Test;

public class AbstractScanEventTest extends BrokerTest {

	private static class TestScanListener implements IScanListener {

		private List<ScanBean> beansReceived = new ArrayList<>();

		private Optional<Consumer<ScanEvent>> handler = Optional.empty();

		private boolean scanChangeOnly = true;

		public void setScanChangeOnly(boolean scanChangeOnly) {
			this.scanChangeOnly = scanChangeOnly;
		}

		@Override
		public void scanStateChanged(ScanEvent event) {
			handleEvent(event);
		}

		@Override
		public void scanEventPerformed(ScanEvent event) {
			if (!scanChangeOnly)
				handleEvent(event);
		}

		private void handleEvent(ScanEvent event) {
			beansReceived.add(event.getBean());
			handler.ifPresent(action -> action.accept(event));
		}

		public List<ScanBean> getBeansReceived() {
			return beansReceived;
		}

		public void setHandler(Consumer<ScanEvent> handler) {
			this.handler = Optional.ofNullable(handler);
		}

	}

	protected IEventService eventService;
	protected IPublisher<ScanBean> publisher;
	protected ISubscriber<IScanListener> subscriber;

	@After
	public void dispose() throws EventException {
		publisher.disconnect();
		subscriber.disconnect();
	}

	/**
	 * Test that publishing to a bad URI throws an exception
	 * @throws Exception
	 */
	@Test(expected=EventException.class)
	public void badURITest() throws Exception {
		final URI uri = new URI("tcp://rubbish:5600");
		publisher = eventService.createPublisher(uri, EventConstants.SCAN_TOPIC);
		final ScanBean bean = new ScanBean();
		publisher.broadcast(bean);
	}

	/**
	 * Test publishing a bean doesn't throw an exception.
	 * @throws Exception
	 */
	@Test
	public void testBroadcast() throws Exception {
		final ScanBean bean = new ScanBean();
		bean.setName("fred");
		publisher.broadcast(bean);
	}

	/**
	 * Test publishing a bean and the subscriber receiving it.
	 * @throws Exception
	 */
	@Test
	public void testBroadcastSubscribe() throws Exception {
		final ScanBean bean = new ScanBean();
		bean.setName("fred");

		final TestScanListener listener = new TestScanListener();
		listener.setScanChangeOnly(false);
		subscriber.addListener(listener);

		publisher.broadcast(bean);

		Thread.sleep(100); // The bean should go back and forth in ms anyway

		List<ScanBean> beansReceived = listener.getBeansReceived();
		assertThat(beansReceived, hasSize(1)); // Test bean received
		assertEquals(beansReceived.get(0), bean);
	}

	/**
	 * Publish the ScanBean state changes that would be expected when
	 * running a scan and check they are received correctly.
	 * @throws Exception
	 */
	@Test
	public void testScanStateListener() throws Exception {
		final ScanBean bean = createScanBean("fred");

		final TestScanListener listener = new TestScanListener();
		subscriber.addListener(listener);

		// Mimic a scan
		mimicScan(bean);
		Thread.sleep(500); // The bean should go back and forth in ms anyway

		final List<ScanBean> beansReceived = listener.getBeansReceived();
		checkReceivedScanBeans(beansReceived, bean.getUniqueId(), SUBMITTED, PREPARING, RUNNING, COMPLETE);
	}

	private void mimicScan(ScanBean bean) {
		try {
			bean.setStatus(SUBMITTED);
			publisher.broadcast(bean);
			bean.setPreviousStatus(SUBMITTED);

			bean.setStatus(PREPARING);
			publisher.broadcast(bean);
			bean.setPreviousStatus(PREPARING);

			for (int i = 0; i < 10; i++) {
				bean.setStatus(RUNNING);

				bean.setPercentComplete(i*10);
				publisher.broadcast(bean);
				bean.setPreviousStatus(RUNNING);
			}

			bean.setStatus(COMPLETE);
			publisher.broadcast(bean);
		} catch (EventException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Adds a listener for a specific scan bean.
	 * Publish the ScanBean state changes for that bean and another and
	 * checks that only the state changes for the correct bean are received.
	 * @throws Exception
	 */
	@Test
	public void testStateChangesScanSpecificListener() throws Exception {
		testStateChanges(null);
	}

	/**
	 * As {@link #testStateChangesScanSpecificListener()}, except that the listener sleeps on each bean received.
	 * This tests that events beans sent during this sleep are still received (because the subscriber implementation
	 * queues the requests.
	 * @throws Exception
	 */
	@Test
	public void testStateChangesMissedEvents() throws Exception {
		testStateChanges(event -> {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				throw new RuntimeException(e); // not expected in a test
			}
		});
	}

	/**
	 * As {@link #testStateChangesScanSpecificListener()} except that the listener additionally throws an exception.
	 * This tests that the subscriber implementation recovers from a listener throwing an exception and that listeners
	 * continue to receive the correct events.
	 * @throws Exception
	 */
	@Test
	public void testEventHandlerThrowsException() throws Exception {
		testStateChanges(event -> { throw new RuntimeException(); });
	}

	private void testStateChanges(Consumer<ScanEvent> eventHandler) throws Exception {
		final ScanBean bean = createScanBean("fred");
		final ScanBean bean2 = createScanBean("fred2");

		final TestScanListener beanSpecificListener = new TestScanListener();
		beanSpecificListener.setHandler(eventHandler);
		subscriber.addListener(bean.getUniqueId(), beanSpecificListener);

		final TestScanListener allScansListeners = new TestScanListener();
		subscriber.addListener(allScansListeners);

		// Mimic two scans happening in parallel
		ExecutorService executors = Executors.newFixedThreadPool(2);
		executors.submit(() -> mimicScan(bean));
		executors.submit(() -> mimicScan(bean2));
		executors.awaitTermination(1, TimeUnit.SECONDS);

		final List<ScanBean> specificScanBeans = beanSpecificListener.getBeansReceived();
		checkReceivedScanBeans(specificScanBeans, bean.getUniqueId(), SUBMITTED, PREPARING, RUNNING, COMPLETE);

		// the other scan has the same events, so the list of all events should be twice the size
		assertEquals(specificScanBeans.size() * 2, allScansListeners.getBeansReceived().size());
	}

	private ScanBean createScanBean(String name) {
		final ScanBean scanBean = new ScanBean();
		scanBean.setName(name);
		return scanBean;
	}

	private void checkReceivedScanBeans(List<ScanBean> scanBeans, String expectedBeanId, Status... expectedStates) {
		assertThat(scanBeans, hasSize(expectedStates.length));
		for (int i = 0; i < scanBeans.size(); i++) {
			final ScanBean scanBean = scanBeans.get(i);
			assertEquals(expectedBeanId, scanBean.getUniqueId());
			assertEquals(expectedStates[i], scanBean.getStatus());
		}
	}

}
