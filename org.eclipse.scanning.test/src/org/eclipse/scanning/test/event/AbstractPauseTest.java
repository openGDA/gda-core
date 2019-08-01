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
import org.eclipse.scanning.api.event.bean.BeanEvent;
import org.eclipse.scanning.api.event.bean.IBeanListener;
import org.eclipse.scanning.api.event.core.IJmsQueueReader;
import org.eclipse.scanning.api.event.core.IJobQueue;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.core.ISubmitter;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.scanning.api.event.dry.DryRunProcessCreator;
import org.eclipse.scanning.api.event.queue.QueueCommandBean;
import org.eclipse.scanning.api.event.queue.QueueCommandBean.Command;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.eclipse.scanning.test.BrokerTest;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

public class AbstractPauseTest extends BrokerTest {


	protected IEventService eservice;
	protected ISubmitter<StatusBean> submitter;
	protected IJmsQueueReader<StatusBean> jmsQueueReader;
	protected IJobQueue<StatusBean> jobQueue;


	@After
	public void dispose() throws EventException {
		submitter.disconnect();
		jmsQueueReader.disconnect();
		jobQueue.clearQueue();
		jobQueue.clearRunningAndCompleted();
		jobQueue.disconnect();
	}

	@Test
	public void testPausingAConsumerByID() throws Exception {

		jobQueue.setRunner(new DryRunProcessCreator<StatusBean>(100,false));
		jobQueue.start();

		StatusBean bean = doSubmit();

		Thread.sleep(200);

		IPublisher<QueueCommandBean> publisher = eservice.createPublisher(uri, EventConstants.CMD_TOPIC);
		QueueCommandBean pauseBean = new QueueCommandBean(jobQueue.getJobQueueId(), Command.PAUSE_QUEUE);
		publisher.broadcast(pauseBean);

		Thread.sleep(200);

		assertTrue(!jobQueue.isActive());

		QueueCommandBean resumeBean = new QueueCommandBean(jobQueue.getJobQueueId(), Command.RESUME_QUEUE);
		publisher.broadcast(resumeBean);

		Thread.sleep(100);

		assertTrue(jobQueue.isActive());
	}


	@Test
	public void testPausingQueueByQueueName() throws Exception {

		jobQueue.setRunner(new DryRunProcessCreator<StatusBean>(100,false));
		jobQueue.start();

		StatusBean bean = doSubmit();

		Thread.sleep(200);

		IPublisher<QueueCommandBean> publisher = eservice.createPublisher(submitter.getUri(), EventConstants.CMD_TOPIC);

		QueueCommandBean pauseBean = new QueueCommandBean(jobQueue.getSubmitQueueName(), Command.PAUSE_QUEUE);
		publisher.broadcast(pauseBean);

		Thread.sleep(200);

		assertTrue(!jobQueue.isActive());

		QueueCommandBean resumeBean = new QueueCommandBean(jobQueue.getSubmitQueueName(), Command.RESUME_QUEUE);
		publisher.broadcast(resumeBean);

		Thread.sleep(100);

		assertTrue(jobQueue.isActive());
	}

	@Ignore("TODO Find out why this does not work, it is supposed to...")
	@Test
	public void testReorderingAPausedQueue() throws Exception {

		jobQueue.setRunner(new DryRunProcessCreator<StatusBean>(0,100,10,100, true));
		jobQueue.start();

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

		QueueCommandBean pauseBean = new QueueCommandBean(jobQueue.getSubmitQueueName(), Command.PAUSE_QUEUE);
		publisher.broadcast(pauseBean);

		// Now we are paused. Read the submission queue
		Thread.sleep(200);
		List<StatusBean> submitQ = jobQueue.getSubmissionQueue();
		assertTrue(submitQ.size()>=4);

		Thread.sleep(1000); // Wait for a while and check again that nothing else is

		submitQ = jobQueue.getSubmissionQueue();
		assertTrue(submitQ.size()>=4);

		// Right then we will reorder it.
		jobQueue.clearQueue();
		jobQueue.clearQueue();

		// Reverse sort
		Collections.sort(submitQ, new Comparator<StatusBean>() {
			@Override
			public int compare(StatusBean o1, StatusBean o2) {
				int y = Integer.valueOf(o1.getUserName());
				int x = Integer.valueOf(o2.getUserName());
				return (x < y) ? -1 : ((x == y) ? 0 : 1);
			}
		});

		// Start the queue again
		QueueCommandBean resumeBean = new QueueCommandBean(jobQueue.getSubmitQueueName(), Command.RESUME_QUEUE);
		publisher.broadcast(resumeBean);

		// Resubmit in new order 4-1
		final List<String> submitted = new ArrayList<>(4); // Order important
		for (StatusBean statusBean : submitQ) {
			System.out.println("Submitting "+statusBean.getName());
			submitter.submit(statusBean);
			submitted.add(statusBean.getName());
		}

		final List<String> run = new ArrayList<>(4); // Order important
		ISubscriber<EventListener> sub = eservice.createSubscriber(uri, jobQueue.getStatusTopicName());
		sub.addListener(new IBeanListener<StatusBean>() {
			@Override
			public void beanChangePerformed(BeanEvent<StatusBean> evt) {
				// Many events come through here but each scan is run in order
				StatusBean bean = evt.getBean();
				if (!run.contains(bean.getName())) run.add(bean.getName());
			}
		});

		while(!jobQueue.getSubmissionQueue().isEmpty()) Thread.sleep(100); // Wait for all to run

		Thread.sleep(500); // ensure last one is in the status set

		assertTrue(run.size()>=4);

		assertTrue(submitted.equals(run));

		sub.disconnect();
	}

	@Test
	public void testReorderedScans() throws Exception {
		// see JIRA bug DAQ-342
		jobQueue.setRunner(new DryRunProcessCreator<StatusBean>(0,100,10,100, true));
		jobQueue.start();

		Thread.sleep(500);
		IPublisher<QueueCommandBean> publisher = eservice.createPublisher(submitter.getUri(), EventConstants.CMD_TOPIC);

		QueueCommandBean pauseBean = new QueueCommandBean(jobQueue.getSubmitQueueName(), Command.PAUSE_QUEUE);
		publisher.broadcast(pauseBean);

		Thread.sleep(500);
		assertTrue(!jobQueue.isActive());

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

		Thread.sleep(500);

		jobQueue.moveForward(statusBean);

		Thread.sleep(500);
		assertTrue(!jobQueue.isActive());
		assertEquals(2, jobQueue.getSubmissionQueue().size());

		QueueCommandBean resumeBean = new QueueCommandBean(jobQueue.getSubmitQueueName(), Command.RESUME_QUEUE);
		publisher.broadcast(resumeBean);

		Thread.sleep(500);
		assertTrue(jobQueue.isActive());

	}

	@Test
	public void testPauseResumeWhenScanRunning() throws Exception {
		jobQueue.setRunner(new DryRunProcessCreator<>(0, 100, 10, 100, true)); // each scan takes 1 second
		jobQueue.start();

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

		QueueCommandBean pauseBean = new QueueCommandBean(jobQueue.getSubmitQueueName(), Command.PAUSE_QUEUE);
		commandPublisher.broadcast(pauseBean);
		QueueCommandBean resumeBean = new QueueCommandBean(jobQueue.getSubmitQueueName(), Command.RESUME_QUEUE);
		commandPublisher.broadcast(resumeBean);

		Thread.sleep(1200); // wait long enough for the first scan to finish

		assertTrue(jobQueue.isActive()); // check the job queue has been resumed
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
