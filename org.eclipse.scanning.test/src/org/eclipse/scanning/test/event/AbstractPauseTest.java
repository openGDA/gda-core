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
import static org.junit.Assert.assertTrue;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EventListener;
import java.util.List;
import java.util.UUID;

import org.eclipse.scanning.api.event.EventConstants;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.alive.QueueCommandBean;
import org.eclipse.scanning.api.event.alive.QueueCommandBean.Command;
import org.eclipse.scanning.api.event.bean.BeanEvent;
import org.eclipse.scanning.api.event.bean.IBeanListener;
import org.eclipse.scanning.api.event.core.IConsumer;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.core.ISubmitter;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.scanning.api.event.dry.DryRunProcessCreator;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.eclipse.scanning.test.BrokerTest;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

public class AbstractPauseTest extends BrokerTest {


	protected IEventService          eservice;
	protected ISubmitter<StatusBean> submitter;
	protected IConsumer<StatusBean>  consumer;


	@After
	public void dispose() throws EventException {
		submitter.disconnect();
		consumer.clearQueue(EventConstants.SUBMISSION_QUEUE);
		consumer.clearQueue(EventConstants.STATUS_SET);
		consumer.clearQueue(EventConstants.CMD_SET);
		consumer.disconnect();
	}

	@Test
	public void testPausingAConsumerByID() throws Exception {

		consumer.setRunner(new DryRunProcessCreator<StatusBean>(100,false));
		consumer.start();

		StatusBean bean = doSubmit();

		Thread.sleep(200);

		IPublisher<QueueCommandBean> publisher = eservice.createPublisher(submitter.getUri(), EventConstants.CMD_TOPIC);
		publisher.setStatusSetName(EventConstants.CMD_SET);
		publisher.setStatusSetAddRequired(true);

		QueueCommandBean pauseBean = new QueueCommandBean(consumer.getConsumerId(), Command.PAUSE);
		publisher.broadcast(pauseBean);

		Thread.sleep(200);

		assertTrue(!consumer.isActive());

		QueueCommandBean resumeBean = new QueueCommandBean(consumer.getConsumerId(), Command.RESUME);
		publisher.broadcast(resumeBean);

		Thread.sleep(100);

		assertTrue(consumer.isActive());
	}


	@Test
	public void testPausingAConsumerByQueueName() throws Exception {

		consumer.setRunner(new DryRunProcessCreator<StatusBean>(100,false));
		consumer.start();

		StatusBean bean = doSubmit();

		Thread.sleep(200);

		IPublisher<QueueCommandBean> publisher = eservice.createPublisher(submitter.getUri(), EventConstants.CMD_TOPIC);
		publisher.setStatusSetName(EventConstants.CMD_SET);
		publisher.setStatusSetAddRequired(true);

		QueueCommandBean pauseBean = new QueueCommandBean(consumer.getSubmitQueueName(), Command.PAUSE);
		publisher.broadcast(pauseBean);

		Thread.sleep(200);

		assertTrue(!consumer.isActive());

		QueueCommandBean resumeBean = new QueueCommandBean(consumer.getSubmitQueueName(), Command.RESUME);
		publisher.broadcast(resumeBean);

		Thread.sleep(100);

		assertTrue(consumer.isActive());
	}

	@Ignore("TODO Find out why this does not work, it is supposed to...")
	@Test
	public void testReorderingAPausedQueue() throws Exception {

		consumer.setRunner(new DryRunProcessCreator<StatusBean>(0,100,10,100, true));
		consumer.start();

		// Bung ten things on there.
		for (int i = 0; i < 5; i++) {
			StatusBean bean = new StatusBean();
			bean.setName("Submission"+i);
			bean.setStatus(Status.SUBMITTED);
			bean.setHostName(InetAddress.getLocalHost().getHostName());
			bean.setMessage("Hello World");
			bean.setUniqueId(UUID.randomUUID().toString());
			bean.setUserName(String.valueOf(i));
			submitter.submit(bean);
		}

		IPublisher<QueueCommandBean> publisher = eservice.createPublisher(submitter.getUri(), EventConstants.CMD_TOPIC);
		publisher.setStatusSetName(EventConstants.CMD_SET);
		publisher.setStatusSetAddRequired(true);

		QueueCommandBean pauseBean = new QueueCommandBean(consumer.getSubmitQueueName(), Command.PAUSE);
		publisher.broadcast(pauseBean);

		// Now we are paused. Read the submission queue
		Thread.sleep(200);
		List<StatusBean> submitQ = consumer.getSubmissionQueue();
		assertTrue(submitQ.size()>=4);

		Thread.sleep(1000); // Wait for a while and check again that nothing else is

		submitQ = consumer.getSubmissionQueue();
		assertTrue(submitQ.size()>=4);

		// Right then we will reorder it.
		consumer.clearQueue(consumer.getSubmitQueueName());
		consumer.clearQueue(consumer.getStatusSetName());

		// Reverse sort
		Collections.sort(submitQ, new Comparator<StatusBean>() {
			@Override
			public int compare(StatusBean o1, StatusBean o2) {
				int y = Integer.valueOf(o1.getUserName());
				int x = Integer.valueOf(o2.getUserName());
				return (x < y) ? -1 : ((x == y) ? 0 : 1);
			}
		});

		// Start the consumer again
		QueueCommandBean resumeBean = new QueueCommandBean(consumer.getSubmitQueueName(), Command.RESUME);
		publisher.broadcast(resumeBean);

		// Resubmit in new order 4-1
		final List<String> submitted = new ArrayList<>(4); // Order important
		for (StatusBean statusBean : submitQ) {
			System.out.println("Submitting "+statusBean.getName());
			submitter.submit(statusBean);
			submitted.add(statusBean.getName());
		}

		final List<String> run = new ArrayList<>(4); // Order important
		ISubscriber<EventListener> sub = eservice.createSubscriber(consumer.getUri(), consumer.getStatusTopicName());
		sub.addListener(new IBeanListener<StatusBean>() {
			@Override
			public void beanChangePerformed(BeanEvent<StatusBean> evt) {
				// Many events come through here but each scan is run in order
				StatusBean bean = evt.getBean();
				if (!run.contains(bean.getName())) run.add(bean.getName());
			}
		});

		while(!consumer.getSubmissionQueue().isEmpty()) Thread.sleep(100); // Wait for all to run

		Thread.sleep(500); // ensure last one is in the status set

		assertTrue(run.size()>=4);

		assertTrue(submitted.equals(run));

		sub.disconnect();
	}

	@Test
	public void testReorderedScans() throws Exception {
		// see JIRA bug DAQ-342
		consumer.setRunner(new DryRunProcessCreator<StatusBean>(0,100,10,100, true));
		consumer.start();

		Thread.sleep(500);
		IPublisher<QueueCommandBean> publisher = eservice.createPublisher(submitter.getUri(), EventConstants.CMD_TOPIC);
		publisher.setStatusSetName(EventConstants.CMD_SET);
		publisher.setStatusSetAddRequired(true);

		QueueCommandBean pauseBean = new QueueCommandBean(consumer.getSubmitQueueName(), Command.PAUSE);
		publisher.broadcast(pauseBean);

		Thread.sleep(500);
		assertTrue(!consumer.isActive());

		// Submit two things.
		StatusBean statusBean = null;
		for (int i = 0; i < 2; i++) {
			statusBean = new StatusBean();
			statusBean.setName("Submission"+i);
			statusBean.setStatus(Status.SUBMITTED);
			statusBean.setHostName(InetAddress.getLocalHost().getHostName());
			statusBean.setMessage("Hello World");
			statusBean.setUniqueId(UUID.randomUUID().toString());
			statusBean.setUserName(String.valueOf(i));
			submitter.submit(statusBean);
		}

		submitter.reorder(statusBean, consumer.getSubmitQueueName(), 1);

		Thread.sleep(500);
		assertTrue(!consumer.isActive());
		assertEquals(2, submitter.getQueue().size());

		QueueCommandBean resumeBean = new QueueCommandBean(consumer.getSubmitQueueName(), Command.RESUME);
		publisher.broadcast(resumeBean);

		Thread.sleep(500);
		assertTrue(consumer.isActive());

	}

	@Test
	public void testPauseResumeWhenScanRunning() throws Exception {
		consumer.setRunner(new DryRunProcessCreator<>(0, 100, 10, 100, true)); // each scan takes 1 second
		consumer.start();

		// Submit two beans
		StatusBean bean = null;
		for (int i = 0; i < 2; i++) {
			bean = new StatusBean();
			bean.setName("Submission"+i);
			bean.setStatus(Status.SUBMITTED);
			bean.setHostName(InetAddress.getLocalHost().getHostName());
			bean.setMessage("Hello World");
			bean.setUniqueId(UUID.randomUUID().toString());
			bean.setUserName(String.valueOf(i));
			submitter.submit(bean);
		}

		Thread.sleep(200); // make sure we're in the first scan

		// pause the queue and immediately resume it
		IPublisher<QueueCommandBean> commandPublisher = eservice.createPublisher(submitter.getUri(), EventConstants.CMD_TOPIC);
		commandPublisher.setStatusSetName(EventConstants.CMD_SET);
		commandPublisher.setStatusSetAddRequired(true);

		QueueCommandBean pauseBean = new QueueCommandBean(consumer.getSubmitQueueName(), Command.PAUSE);
		commandPublisher.broadcast(pauseBean);
		QueueCommandBean resumeBean = new QueueCommandBean(consumer.getSubmitQueueName(), Command.RESUME);
		commandPublisher.broadcast(resumeBean);

		Thread.sleep(1200); // wait long enough for the first scan to finish

		assertTrue(consumer.isActive()); // check the consumer has been resumed
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
}
