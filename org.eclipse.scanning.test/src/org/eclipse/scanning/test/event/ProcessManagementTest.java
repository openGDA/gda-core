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

import org.eclipse.scanning.api.event.bean.BeanEvent;
import org.eclipse.scanning.api.event.bean.IBeanListener;
import org.eclipse.scanning.api.event.core.IConsumerProcess;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.eclipse.scanning.test.util.WaitingAnswer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

public class ProcessManagementTest extends AbstractNewConsumerTest {

	private WaitingAnswer<Void> waitingAnswer;

	private IBeanListener<StatusBean> statusTopicListener;

	@Mock
	private IConsumerProcess<StatusBean> process;

	private StatusBean bean;

	@SuppressWarnings("unchecked")
	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();

		bean = setupBeans("test").get(0);

		// set the waiting answer to be called when the process for the bean is started
		when(runner.createProcess(bean, statusTopicPublisher)).thenReturn(process);
		when(process.getBean()).thenReturn(bean);
		waitingAnswer = new WaitingAnswer<>(null);
		doAnswer(waitingAnswer).when(process).start();

		startConsumer();

		// capture the status topic listener
		ArgumentCaptor<IBeanListener<StatusBean>> statusTopicListenerCaptor =
				(ArgumentCaptor<IBeanListener<StatusBean>>) (ArgumentCaptor<?>) ArgumentCaptor.forClass(IBeanListener.class);
		verify(statusTopicSubscriber).addListener(statusTopicListenerCaptor.capture());
		statusTopicListener = statusTopicListenerCaptor.getValue();

		waitingAnswer.waitUntilCalled();
	}

	@Override
	@After
	public void tearDown() {
		waitingAnswer.resume();
	}

	@Test
	public void testPauseResumeProcess() throws Exception {
		bean.setStatus(Status.REQUEST_PAUSE);
		statusTopicListener.beanChangePerformed(new BeanEvent<>(bean));
		verify(process).pause();

		bean.setStatus(Status.REQUEST_RESUME);
		statusTopicListener.beanChangePerformed(new BeanEvent<>(bean));
		verify(process).resume();
	}

	@Test
	public void testTerminateProcess() throws Exception {
		bean.setStatus(Status.REQUEST_TERMINATE);
		statusTopicListener.beanChangePerformed(new BeanEvent<>(bean));
		verify(process).terminate();
	}

}
