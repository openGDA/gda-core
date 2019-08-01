/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.bean.BeanEvent;
import org.eclipse.scanning.api.event.bean.IBeanListener;
import org.eclipse.scanning.api.event.core.IBeanProcess;
import org.eclipse.scanning.api.event.queue.QueueCommandBean;
import org.eclipse.scanning.api.event.queue.QueueCommandBean.Command;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.eclipse.scanning.test.util.WaitingAnswer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@RunWith(value = Parameterized.class)
public class ProcessManagementTest extends AbstractJobQueueTest {

	@Parameters(name="{index}: useCommandBean = {0}")
	public static Object[] data() {
		return new Object[] { true, false };
	}

	private WaitingAnswer<Void> waitingAnswer;

	private IBeanListener<QueueCommandBean> commandTopicListener;

	@Mock
	private IBeanProcess<StatusBean> process;

	private StatusBean bean;

	private boolean useCommandBean;

	public ProcessManagementTest(boolean useCommandBean) {
		this.useCommandBean = useCommandBean;
	}

	@SuppressWarnings("unchecked")
	@Override
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this); // requried as this test runs with Parameterized instead of MockitoJUnitRunner
		super.setUp();

		bean = createAndSubmitBeans("test").get(0);

		// set the waiting answer to be called when the process for the bean is started
		when(runner.createProcess(bean, statusTopicPublisher)).thenReturn(process);
		when(process.getBean()).thenReturn(bean);
		waitingAnswer = new WaitingAnswer<>(null);
		doAnswer(waitingAnswer).when(process).start();

		startJobQueue();

		// capture the command topic listener
		ArgumentCaptor<IBeanListener<QueueCommandBean>> commandTopicListenerCaptor =
				(ArgumentCaptor<IBeanListener<QueueCommandBean>>) (ArgumentCaptor<?>) ArgumentCaptor.forClass(IBeanListener.class);
		verify(commandTopicSubscriber).addListener(commandTopicListenerCaptor.capture());
		commandTopicListener = commandTopicListenerCaptor.getValue();

		// wait until the process has been started
		waitingAnswer.waitUntilCalled();
		verify(process).start();
	}

	@Override
	@After
	public void tearDown() {
		waitingAnswer.resume();
	}

	private void sendCommandBean(Command command) {
		final QueueCommandBean pauseBean = new QueueCommandBean(jobQueue.getSubmitQueueName(), command);
		pauseBean.setJobBean(bean);
		commandTopicListener.beanChangePerformed(new BeanEvent<>(pauseBean));
	}

	private void doPause() throws EventException {
		if (useCommandBean) {
			sendCommandBean(Command.PAUSE_JOB);
		} else {
			jobQueue.pauseJob(bean);
		}
	}

	private void doResume() throws EventException {
		if (useCommandBean) {
			sendCommandBean(Command.RESUME_JOB);
		} else {
			jobQueue.resumeJob(bean);
		}
	}

	private void doTerminate() throws EventException {
		if (useCommandBean) {
			sendCommandBean(Command.TERMINATE_JOB);
		} else {
			jobQueue.terminateJob(bean);
		}
	}

	@Test
	public void testPauseResumeProcess() throws Exception {
		doPause();
		verify(process).pause();

		doResume();
		verify(process).resume();
	}

	@Test
	public void testTerminateProcess() throws Exception {
		doTerminate();
		verify(process).terminate();
	}

}
