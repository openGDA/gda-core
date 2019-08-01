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
package org.eclipse.scanning.test.command;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.core.AbstractLockingPausableProcess;
import org.eclipse.scanning.api.event.core.IJobQueue;
import org.eclipse.scanning.api.event.core.IBeanProcess;
import org.eclipse.scanning.api.event.core.IJmsQueueReader;
import org.eclipse.scanning.api.event.core.IProcessCreator;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.test.ServiceTestHelper;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;


public class SubmissionTest extends AbstractJythonTest {

	private static IJobQueue<ScanBean> jobQueue;
	private static IJmsQueueReader<ScanBean> jmsQueueReader;

	private static BlockingQueue<String> testLog;
	// We'll use this to check that things happen in the right order.
	static {
		testLog = new ArrayBlockingQueue<>(2);
	}

	@BeforeClass
	public static void start() throws Exception {
		ServiceTestHelper.setupServices();

		jobQueue = ServiceTestHelper.getEventService().createJobQueue(uri);

		jobQueue.setRunner(new IProcessCreator<ScanBean>() {
			@Override
			public IBeanProcess<ScanBean> createProcess(
					ScanBean bean, IPublisher<ScanBean> statusNotifier) throws EventException {
				return new AbstractLockingPausableProcess<ScanBean>(bean, statusNotifier) {

					@Override
					public void execute() throws EventException {
						try {
							// Pretend the scan is happening now...
							Thread.sleep(1000);
							testLog.put("Scan complete.");
							bean.setStatus(Status.COMPLETE);
							broadcast(bean);
						} catch (InterruptedException e) {}
					}

					@Override public void terminate() throws EventException {}
				};
			}
		});
		jobQueue.start();

		jmsQueueReader = ServiceTestHelper.getEventService().createJmsQueueReader(uri, jobQueue.getSubmitQueueName());
		jmsQueueReader.start();

		// Put any old ScanRequest in the Python namespace.
		pi.exec("sr = scan_request(step(my_scannable, 0, 10, 1), det=mandelbrot(0.001))");

		pi.exec("srNoDet = scan_request(step(my_scannable, 0, 10, 1))");

	}

	@After
	public void stop() throws EventException {
		jobQueue.clearQueue();
		jobQueue.clearRunningAndCompleted();
	}

	@AfterClass
	public static void disconnect() throws EventException {
		jobQueue.disconnect();
		jmsQueueReader.disconnect();
	}

	@Test
	public void testSubmission() throws InterruptedException {
		submission("sr", true);
	}
	@Test
	public void testSubmissionNoDetector() throws InterruptedException {
		submission("srNoDet", true);
	}
	@Test
	public void testNonBlockingSubmission() throws InterruptedException {
		submission("sr", false);
	}
	@Test
	public void testNonBlockingSubmissionNoDetector() throws InterruptedException {
		submission("srNoDet", false);
	}

	private void submission(String name, boolean blocking) throws InterruptedException {
		pi.exec("submit("+name+", block="+(blocking?"True":"False")+", broker_uri='"+uri+"')");
		testLog.put("Jython command returned.");

		// Jython returns *after* scan is complete.
		if (blocking) {
			assertEquals("Scan complete.", testLog.take());
			assertEquals("Jython command returned.", testLog.take());
		} else {
			// Jython returns *before* scan is complete.
			assertEquals("Jython command returned.", testLog.take());
			assertEquals("Scan complete.", testLog.take());
		}
	}



}
