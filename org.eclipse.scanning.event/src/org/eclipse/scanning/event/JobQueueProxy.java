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

package org.eclipse.scanning.event;

import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;

import org.eclipse.scanning.api.event.EventConstants;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventConnectorService;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.IJobQueue;
import org.eclipse.scanning.api.event.core.IProcessCreator;
import org.eclipse.scanning.api.event.core.IRequester;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.scanning.api.event.queue.IQueueStatusBeanListener;
import org.eclipse.scanning.api.event.queue.QueueCommandBean;
import org.eclipse.scanning.api.event.queue.QueueCommandBean.Command;
import org.eclipse.scanning.api.event.queue.QueueStatus;
import org.eclipse.scanning.api.event.queue.QueueStatusBean;
import org.eclipse.scanning.api.event.status.StatusBean;

public final class JobQueueProxy<U extends StatusBean> extends AbstractConnection implements IJobQueue<U> {

	private final String commandTopicName;
	private final String commandAckTopicName;

	private final Set<IQueueStatusListener> queueStatusListeners = new CopyOnWriteArraySet<>();

	private final IEventService eventService;

	private IRequester<QueueCommandBean> queueCommandRequestor;

	private ISubscriber<IQueueStatusBeanListener> queueStatusTopicSubscriber;

	public JobQueueProxy(URI uri, String submissionQueueName, String commandTopicName, String commandAckTopicName, IEventConnectorService connectorService, IEventService eventService) throws EventException {
		super(uri, submissionQueueName, connectorService);
		this.commandTopicName = commandTopicName;
		this.commandAckTopicName = commandAckTopicName;
		this.eventService = eventService;

		subscribeToQueueStatusTopic();
	}

	private void subscribeToQueueStatusTopic() throws EventException {
		queueStatusTopicSubscriber = eventService.createSubscriber(uri, EventConstants.QUEUE_STATUS_TOPIC);
		queueStatusTopicSubscriber.addListener(evt -> fireStatusChangeListeners(evt.getBean().getQueueStatus()));
	}

	private void fireStatusChangeListeners(QueueStatus status) {
		for (IQueueStatusListener listener : queueStatusListeners) {
			listener.queueStatusChanged(status);
		}
	}

	private QueueStatusBean getConsumerInfo() {
		try {
			return (QueueStatusBean) sendCommand(Command.GET_INFO);
		} catch (EventException e) {
			throw new RuntimeException("Could not get consumer info", e);
		}
	}

	private Object sendCommand(Command command) throws EventException {
		return sendCommandBean(new QueueCommandBean(getSubmitQueueName(), command));
	}

	private Object sendCommand(Command command, U bean) throws EventException {
		QueueCommandBean commandBean = new QueueCommandBean(getSubmitQueueName(), command, bean);
		return sendCommandBean(commandBean);
	}

	private Object sendCommand(Command command, String commandParam) throws EventException {
		return sendCommandBean(new QueueCommandBean(getSubmitQueueName(), command, commandParam));
	}

	private synchronized Object sendCommandBean(QueueCommandBean commandBean) throws EventException {
		if (queueCommandRequestor == null) {
			queueCommandRequestor = eventService.createRequestor(uri, getCommandTopicName(), getCommandAckTopicName());
			queueCommandRequestor.setTimeout(10, TimeUnit.SECONDS); // allow a very generous timeout, can take > 1 sec to get queue from ActiveMQ
		}

		try {
			QueueCommandBean response = queueCommandRequestor.post(commandBean);
			if (response.getErrorMessage() != null) {
				throw new EventException("The consumer could not process the command " + commandBean + ". Reason: " + response.getErrorMessage());
			}
			return response.getResult();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new EventException(e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<U> getRunningAndCompleted() throws EventException {
		return (List<U>) sendCommand(Command.GET_RUNNING_AND_COMPLETED);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<U> getRunningAndCompleted(String optionalArgument) throws EventException {
		return (List<U>) sendCommand(Command.GET_RUNNING_AND_COMPLETED, optionalArgument);
	}

	@Override
	public void clearQueue() throws EventException {
		sendCommand(Command.CLEAR_QUEUE);
	}

	@Override
	public void clearRunningAndCompleted() throws EventException {
		sendCommand(Command.CLEAR_COMPLETED);
	}

	@Override
	public void clearRunningAndCompleted(boolean bool) throws EventException {
		sendCommand(Command.CLEAR_COMPLETED, String.valueOf(bool));
	}

	@Override
	public synchronized void disconnect() throws EventException {
		super.disconnect();
		queueCommandRequestor.disconnect();
		queueStatusTopicSubscriber.disconnect();
	}

	/*
	 *  Only called when a StatusBean is re-submitted, not on initial submit (see SubmitScanSection.submit()) or from mscan (old or new)
	 *  Therefore, probably want to refresh the queue of all clients watching the JobQueueImpl
	 */
	@Override
	public void submit(U bean) throws EventException {
		sendCommand(Command.SUBMIT_JOB, bean);
		sendCommand(Command.REFRESH_QUEUE_VIEW);
	}

	@Override
	public boolean moveForward(U bean) throws EventException {
		boolean result = sendCommand(Command.MOVE_FORWARD, bean) == Boolean.TRUE;
		if (result) sendCommand(Command.REFRESH_QUEUE_VIEW);
		return result;
	}

	@Override
	public boolean moveBackward(U bean) throws EventException {
		boolean result = sendCommand(Command.MOVE_BACKWARD, bean) == Boolean.TRUE;
		if (result) sendCommand(Command.REFRESH_QUEUE_VIEW);
		return result;
	}

	@Override
	public boolean remove(U bean) throws EventException {
		boolean result = sendCommand(Command.REMOVE_FROM_QUEUE, bean) == Boolean.TRUE;
		if (result) sendCommand(Command.REFRESH_QUEUE_VIEW);
		return result;
	}

	@Override
	public boolean removeCompleted(U bean) throws EventException {
		return sendCommand(Command.REMOVE_COMPLETED, bean) == Boolean.TRUE;
	}

	@Override
	public boolean replace(U bean) throws EventException {
		// This method is not yet supported by the proxy as it is not currently required to be.
		throw new UnsupportedOperationException("This method is not implemented by this proxy class");
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<U> getSubmissionQueue() throws EventException {
		return (List<U>) sendCommand(Command.GET_QUEUE);
	}

	@Override
	public void setRunner(IProcessCreator<U> process) throws EventException {
		// can't set the runner via the proxy
		throw new UnsupportedOperationException("This method is not implemented by this proxy class");
	}

	@Override
	public void start() throws EventException {
		// there is no command to start a consumer that isn't currently running.
		throw new UnsupportedOperationException("This method is not implemented by this proxy class");
	}

	@Override
	public void stop() throws EventException {
		sendCommand(Command.STOP_QUEUE);
	}

	@Override
	public void awaitStart() throws InterruptedException {
		// This method cannot be performed via a proxy
		throw new UnsupportedOperationException("This method is not implemented by this proxy class");
	}

	@Override
	public void awaitStop() throws InterruptedException {
		// This method cannot be performed via a proxy
		throw new UnsupportedOperationException("This method is not implemented by this proxy class");
	}

	@Override
	public void run() throws EventException {
		// This method cannot be performed via a proxy
		throw new UnsupportedOperationException("This method is not implemented by this proxy class");
	}

	@Override
	public void pause() throws EventException {
		sendCommand(Command.PAUSE_QUEUE);
	}

	@Override
	public void resume() throws EventException {
		sendCommand(Command.RESUME_QUEUE);
	}

	@Override
	public boolean isPaused() {
		return getQueueStatus() == QueueStatus.PAUSED;
	}

	@Override
	public void cleanUpCompleted() throws EventException {
		// The queue cannot be cleaned up via a proxy, clearCompleted is available however
		throw new UnsupportedOperationException("This method is not implemented by this proxy class");
	}

	@Override
	public void pauseJob(U bean) throws EventException {
		sendCommand(Command.PAUSE_JOB, bean);
	}

	@Override
	public void resumeJob(U bean) throws EventException {
		sendCommand(Command.RESUME_JOB, bean);
	}

	@Override
	public void terminateJob(U bean) throws EventException {
		sendCommand(Command.TERMINATE_JOB, bean);
	}

	@Override
	public IProcessCreator<U> getRunner() {
		// Cannot get the runner from a proxy
		throw new UnsupportedOperationException("This method is not implemented by this proxy class");
	}

	@Override
	public String getCommandTopicName() {
		return commandTopicName;
	}

	@Override
	public String getCommandAckTopicName() {
		return commandAckTopicName;
	}

	@Override
	public String getQueueStatusTopicName() {
		return queueStatusTopicSubscriber.getTopicName();
	}

	@Override
	public UUID getJobQueueId() {
		return getConsumerInfo().getQueueId();
	}

	@Override
	public QueueStatus getQueueStatus() {
		return getConsumerInfo().getQueueStatus();
	}

	@Override
	public void addQueueStatusListener(IQueueStatusListener listener) {
		queueStatusListeners.add(listener);
	}

	@Override
	public void removeQueueStatusListener(IQueueStatusListener listener) {
		queueStatusListeners.remove(listener);
	}

	@Override
	public String getName() {
		return getConsumerInfo().getJobQueueName();
	}

	@Override
	public void setName(String name) {
		throw new UnsupportedOperationException("This method is not implemented by this proxy class");
	}

	@Override
	public boolean isActive() {
		return getQueueStatus() != QueueStatus.STOPPED;
	}

	// TODO, move configuration methods to an interface that the proxy doesn't implement?

	@Override
	public boolean isPauseOnStart() {
		// This property is not visible on the proxy
		throw new UnsupportedOperationException("This method is not implemented by this proxy class");
	}

	@Override
	public void setPauseOnStart(boolean pauseOnStart) {
		// This property is not visible on the proxy
		throw new UnsupportedOperationException("This method is not implemented by this proxy class");
	}

	@Override
	public Class<U> getBeanClass() {
		// This property is not visible on the proxy
		throw new UnsupportedOperationException("This method is not implemented by this proxy class");
	}

	@Override
	public void setBeanClass(Class<U> beanClass) {
		// This property is not visible on the proxy
		throw new UnsupportedOperationException("This method is not implemented by this proxy class");
	}

	@Override
	public void defer(U bean) throws EventException {
		sendCommand(Command.DEFER, bean);

	}

	@Override
	public void undefer(U bean) throws EventException {
		sendCommand(Command.UNDEFER, bean);

	}

}
