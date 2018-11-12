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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.net.InetAddress;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EventListener;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.eclipse.dawnsci.analysis.api.persistence.IMarshallerService;
import org.eclipse.dawnsci.json.MarshallerService;
import org.eclipse.scanning.api.event.EventConstants;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.alive.HeartbeatBean;
import org.eclipse.scanning.api.event.alive.HeartbeatEvent;
import org.eclipse.scanning.api.event.alive.IHeartbeatListener;
import org.eclipse.scanning.api.event.alive.QueueCommandBean;
import org.eclipse.scanning.api.event.alive.QueueCommandBean.Command;
import org.eclipse.scanning.api.event.bean.BeanEvent;
import org.eclipse.scanning.api.event.bean.IBeanListener;
import org.eclipse.scanning.api.event.core.IConsumer;
import org.eclipse.scanning.api.event.core.IProcessCreator;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.core.ISubmitter;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.scanning.api.event.dry.DryRunProcessCreator;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.eclipse.scanning.event.EventTimingsHelper;
import org.eclipse.scanning.points.serialization.PointsModelMarshaller;
import org.eclipse.scanning.test.BrokerTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class AbstractConsumerTest extends BrokerTest {


	protected IEventService          eservice;
	protected ISubmitter<StatusBean> submitter;
	protected IConsumer<StatusBean>  consumer;

	@Before
	public void start() {
		EventTimingsHelper.setReceiveTimeout(100);
		EventTimingsHelper.setNotificationInterval(200); // Normally 2000
	}

	@After
	public void stop() throws Exception {
		EventTimingsHelper.setNotificationInterval(2000); // Normally 2000
		submitter.disconnect();
		consumer.clearQueue();
		consumer.clearRunningAndCompleted();
		consumer.disconnect();
	}

	@Test
	public void testSimpleSubmission() throws Exception {

		StatusBean bean = doSubmit();

		// Manually take the submission from the list not using event service for isolated test
		ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(submitter.getUri());
		Connection connection = connectionFactory.createConnection();

		try {
			Session   session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			Queue queue = session.createQueue(EventConstants.SUBMISSION_QUEUE);

			final MessageConsumer consumer = session.createConsumer(queue);
			connection.start();

			TextMessage msg = (TextMessage)consumer.receive(1000);

			IMarshallerService marshaller = new MarshallerService(new PointsModelMarshaller());
			StatusBean fromQ = marshaller.unmarshal(msg.getText(), StatusBean.class);

		if (!fromQ.equals(bean)) throw new Exception("The bean from the queue was not the same as that submitted! q="+fromQ+" submit="+bean);

		} finally {
			consumer.disconnect();
			connection.close();
		}
	}

	@Test (expected=EventException.class)
	public void badURI() throws Exception {
		eservice.createConsumer(new URI("tcp://rubbish:5600"));
	}

	@Test
	public void testSimpleConsumer() throws Exception {
		consumer.setRunner(new DryRunProcessCreator<StatusBean>(100L, true));
		consumer.clearQueue();
		consumer.start();

		StatusBean bean = doSubmit();

		Thread.sleep(2500);

		List<StatusBean> stati = consumer.getRunningAndCompleted();
		if (stati.size()!=1) throw new Exception("Unexpected status size in queue! Might not have status or have forgotten to clear at end of test!");

		StatusBean complete = stati.get(0);

		if (complete.equals(bean)) {
			throw new Exception("The bean from the status queue was the same as that submitted! It should have a different status. q="+complete+" submit="+bean);
		}
		assertEquals("The bean in the queue is not complete!", Status.COMPLETE, complete.getStatus());
		if (complete.getPercentComplete()<100) {
			throw new Exception("The percent complete is less than 100!"+complete);
		}
	}

	@Test
	public void testBeanClass() throws Exception {
		IConsumer<StatusBean> fconsumer = eservice.createConsumer(uri,
				EventConstants.SUBMISSION_QUEUE, EventConstants.STATUS_SET, EventConstants.STATUS_TOPIC,
				EventConstants.HEARTBEAT_TOPIC, EventConstants.CMD_TOPIC, EventConstants.ACK_TOPIC);
		try {
			fconsumer.setRunner(new DryRunProcessCreator<StatusBean>(0, 100, 50, 50L, true));
			fconsumer.clearQueue();
			fconsumer.start(); // No bean!

			FredStatusBean bean = new FredStatusBean();
			bean.setName("Frederick");

			dynamicBean(bean, fconsumer, 1);
		} finally {
			fconsumer.clearQueue();
			fconsumer.clearRunningAndCompleted();
			fconsumer.disconnect();
		}
	}

	@Test
	public void testBeanClass2Beans() throws Exception {
		IConsumer<StatusBean> fconsumer = eservice.createConsumer(uri,
				EventConstants.SUBMISSION_QUEUE, EventConstants.STATUS_SET, EventConstants.STATUS_TOPIC,
				EventConstants.HEARTBEAT_TOPIC, EventConstants.CMD_TOPIC, EventConstants.ACK_TOPIC);
		try {
			fconsumer.setRunner(new DryRunProcessCreator<StatusBean>(0, 100, 50, 50L, true));
			fconsumer.clearQueue();
			fconsumer.start();// It's going now, we can submit

			FredStatusBean fred = new FredStatusBean();
			fred.setName("Frederick");
			dynamicBean(fred, fconsumer, 1);

			BillStatusBean bill = new BillStatusBean();
			bill.setName("Bill");
			dynamicBean(bill, fconsumer, 2);

		} finally {
			fconsumer.clearQueue();
			fconsumer.clearRunningAndCompleted();
			fconsumer.disconnect();
		}
	}

	private void dynamicBean(final StatusBean bean, IConsumer<StatusBean> fconsumer, int statusSize) throws Exception {
		// Hard code the service for the test
		ISubscriber<EventListener> sub = eservice.createSubscriber(uri, fconsumer.getStatusTopicName());
		sub.addListener(new IBeanListener<StatusBean>() {
			@Override
			public void beanChangePerformed(BeanEvent<StatusBean> evt) {
				if (!evt.getBean().getName().equals(bean.getName())) {
					System.out.println("This is not our bean! It's called "+evt.getBean().getName()+" and we are "+bean.getName());
					Thread.dumpStack();
				}
			}
		});

		doSubmit(bean);

		Thread.sleep(500);

		List<StatusBean> statusBeans = fconsumer.getRunningAndCompleted();
		assertEquals("Unexpected status size in queue! Size "+statusBeans.size()+" expected "+statusSize+". Might not have status or have forgotten to clear at end of test!",
				statusSize, statusBeans.size());

		StatusBean complete = statusBeans.get(0); // The queue is date sorted.

		assertNotEquals("The bean from the status queue was the same as that submitted! It should have a different status. q="+complete+" submit="+bean,
				complete, bean);

		assertEquals("The bean in the queue is not complete!"+complete, Status.COMPLETE, complete.getStatus());
		assertEquals("The percent complete is not 100!", 100, complete.getPercentComplete(), 1e-15);

		sub.disconnect();
	}

	@Ignore("TODO Figure out why noit reliable in travis, works locally")
	@Test
	public void testConsumerStop() throws Exception {
		testStop(new DryRunProcessCreator<StatusBean>(0, 100, 1, 100L, true));
	}

	@Ignore("TODO Figure out why noit reliable in travis, works locally")
	@Test
	public void testConsumerStopNonBlockingProcess() throws Exception {
		testStop(new DryRunProcessCreator<StatusBean>(0, 100, 1, 100L, false));
	}

	private void testStop(IProcessCreator<StatusBean> dryRunCreator) throws Exception {
		consumer.setRunner(dryRunCreator);

		consumer.clearQueue();
		consumer.start();

		StatusBean bean = doSubmit();

		Thread.sleep(200);

		consumer.stop();

		Thread.sleep(1000);
		checkTerminatedProcess(bean);
	}

	@Test
	public void testKillingAConsumer() throws Exception {
		consumer.setRunner(new DryRunProcessCreator<StatusBean>(0, 100, 1, 100L, true));
		consumer.clearQueue();
		consumer.start();

		StatusBean bean = doSubmit();

		Thread.sleep(1000); // 10 points

		IPublisher<QueueCommandBean> killer = eservice.createPublisher(submitter.getUri(), EventConstants.CMD_TOPIC);
		QueueCommandBean kbean = new QueueCommandBean(consumer.getConsumerId(), Command.STOP_QUEUE);
		killer.broadcast(kbean);

		Thread.sleep(2500);
		checkTerminatedProcess(bean);
	}

	private void checkTerminatedProcess(StatusBean bean) throws Exception {
		List<StatusBean> stati = consumer.getRunningAndCompleted();
		if (stati.size() != 1)
			throw new Exception("Unexpected status size (" + stati.size()
					+ ") in queue!  Might not have status or have forgotten to clear at end of test!");

		StatusBean complete = stati.get(0);

		if (complete.equals(bean)) {
			throw new Exception(
					"The bean from the status queue was the same as that submitted! It should have a different status. q="
							+ complete + " submit=" + bean);
		}

		if (complete.getStatus() != Status.TERMINATED) {
			throw new Exception("The bean in the queue should be terminated after a stop! It was " + complete);
		}
		if (complete.getPercentComplete() == 100) {
			throw new Exception("The percent complete should not be 100!" + complete);
		}
	}

	private static class HeartbeatListener implements IHeartbeatListener {

		private List<HeartbeatBean> beatsReceived = Collections.synchronizedList(new ArrayList<>());
		private final CountDownLatch countdown;
		private int expectedBeats;

		public HeartbeatListener(int expectedBeats)  {
			countdown = new CountDownLatch(expectedBeats);
			this.expectedBeats = expectedBeats;
		}

		public int numberOfBeats() {
			return beatsReceived.size();
		}

		public void awaitBeats() throws InterruptedException {
			countdown.await(expectedBeats*400, TimeUnit.MILLISECONDS);
		}

		@Override
		public void heartbeatPerformed(HeartbeatEvent evt) {
			beatsReceived.add(evt.getBean());
			countdown.countDown();
		}
}

	@Test
	public void checkedHeartbeat() throws Exception {
		ISubscriber<IHeartbeatListener> subscriber = eservice.createSubscriber(uri, EventConstants.HEARTBEAT_TOPIC);
		HeartbeatListener listener = new HeartbeatListener(6);
		subscriber.addListener(listener);

		consumer.setRunner(new DryRunProcessCreator<StatusBean>(100L, true));
		consumer.clearRunningAndCompleted();
		Instant broadcastStartTime = Instant.now();
		consumer.start();
		listener.awaitBeats();
		Duration broadcastDuration = Duration.between(broadcastStartTime, Instant.now());

		// We should have received 6 heart beats, including the initial one on consumer start-up
		assertEquals(6, listener.numberOfBeats());

		// The heartbeats should have come from our consumer only
		listener.beatsReceived.forEach(beat -> assertEquals(consumer.getConsumerId(), beat.getConsumerId()));

		// 5 heartbeats at a notification period of 200 ms should have taken ~ 1 second
		assertEquals(1000, broadcastDuration.toMillis(), 10);
	}

	@Test
	public void uncheckedHeartbeat() throws Exception {
		ISubscriber<IHeartbeatListener> subscriber = null;
		try {
			consumer.setRunner(new DryRunProcessCreator<StatusBean>(100L, true));
			consumer.clearQueue();
			consumer.start();

			subscriber = eservice.createSubscriber(uri, EventConstants.HEARTBEAT_TOPIC);
			final List<HeartbeatBean> gotBack = new ArrayList<>(3);
			subscriber.addListener(evt -> gotBack.add(evt.getBean()));

			Thread.sleep(800);
			if (gotBack.size() < 1)
				throw new Exception("No hearbeat the paitent might be dead!");

			doSubmit();
			Thread.sleep(500);
			consumer.stop(); // Should also stop heartbeat within 2s
			Thread.sleep(300);

			final int sizeBeforeSleep = gotBack.size();
			if (sizeBeforeSleep < 2)
				throw new Exception("No hearbeat the paitent might be dead!");

			Thread.sleep(400); // Should beat again if not dead
			final int sizeAfterSleep = gotBack.size();
			assertEquals("Got a heartbean after stopping the consumer", sizeBeforeSleep, sizeAfterSleep);
		} finally {
			subscriber.disconnect();
		}
	}

	private StatusBean doSubmit() throws Exception {
		return doSubmit("Test");
	}

	private StatusBean doSubmit(String name) throws Exception {
		StatusBean bean = new StatusBean();
		bean.setName(name);
		return doSubmit(bean);
	}

	private StatusBean doSubmit(StatusBean bean) throws Exception {
		bean.setStatus(Status.SUBMITTED);
		bean.setHostName(InetAddress.getLocalHost().getHostName());
		bean.setMessage("Hello World");
		bean.setUniqueId(UUID.randomUUID().toString());

		submitter.submit(bean);

		return bean;
	}

	@Ignore("Test gives unpredicatable errors on travis.")
	@Test
	public void testMultipleSubmissions() throws Exception {
		consumer.setRunner(new DryRunProcessCreator<StatusBean>(0, 100, 50, 100L, false));
		consumer.clearQueue();
		consumer.start();

		List<StatusBean> submissions = new ArrayList<StatusBean>(10);
		for (int i = 0; i < 10; i++) {
			submissions.add(doSubmit("Test "+i));
			System.out.println("Submitted: Test "+i);
			Thread.sleep(100); // Guarantee that submission time cannot be same.
		}

		Thread.sleep(1000);

		checkStatus(submissions);
	}

	private void checkStatus(List<StatusBean> submissions) throws Exception {
	List<StatusBean> stati = consumer.getRunningAndCompleted();
		if (stati.size()!=10) throw new Exception("Unexpected status size in queue! Should be 10 size is "+stati.size());

		for (int i = 0; i < 10; i++) {

			StatusBean complete = stati.get(i);
			if (!complete.getName().equals("Test "+(9-i))) {
				throw new Exception("Unexpected run order detected! bean is named "+complete.getName()+" and should be 'Test "+(9-i)+"'");
			}

			StatusBean bean     = submissions.get(i);
		if (complete.equals(bean)) {
			throw new Exception("The bean from the status queue was the same as that submitted! It should have a different status. q="+complete+" submit="+bean);
		}

		if (complete.getStatus()!=Status.COMPLETE) {
			throw new Exception("The bean in the queue is not complete!"+complete);
		}
		if (complete.getPercentComplete()<100) {
			throw new Exception("The percent complete is less than 100!"+complete);
		}
		}
	}

	@Test
	@Ignore("This test fails too often")
	public void testMultipleSubmissionsUsingThreads() throws Exception {
		consumer.setRunner(new DryRunProcessCreator<StatusBean>(100L, false));
		consumer.clearQueue();
		consumer.start();

		final List<StatusBean> submissions = new ArrayList<StatusBean>(10);
		for (int i = 0; i < 10; i++) {
			final int finalI = i;
			final Thread thread = new Thread(new Runnable() {
				@Override
				public void run () {
					try {
						submissions.add(doSubmit("Test "+finalI));
						System.out.println("Submitted: Thread Test "+finalI);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
			thread.setName("Thread "+i);
			thread.setDaemon(true);
			thread.start();

			Thread.sleep(100);
		}

		Thread.sleep(4000); // TODO: increase in timeout is temporary. This test should be replaced
		// once the queue and status set have been moved into memory

		checkStatus(submissions);
	}
}
