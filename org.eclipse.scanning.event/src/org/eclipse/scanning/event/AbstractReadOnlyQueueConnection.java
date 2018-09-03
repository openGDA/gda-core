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

import org.eclipse.scanning.api.event.EventConstants;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventConnectorService;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.alive.QueueCommandBean;
import org.eclipse.scanning.api.event.alive.QueueCommandBean.Command;
import org.eclipse.scanning.api.event.core.IQueueReader;
import org.eclipse.scanning.api.event.core.IReadOnlyQueueConnection;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractReadOnlyQueueConnection<U extends StatusBean> extends AbstractConnection implements IReadOnlyQueueConnection<U> {

	private static final Logger logger = LoggerFactory.getLogger(AbstractReadOnlyQueueConnection.class);

	protected IEventService eventService;

	protected Class<U> beanClass;

	AbstractReadOnlyQueueConnection(URI uri, String topic, IEventConnectorService connectorService, IEventService eventService) {
		super(uri, topic, connectorService);
		this.eventService = eventService;
	}

	AbstractReadOnlyQueueConnection(URI uri, String submitQueueName, String statusQueueName, String statusTopicName,
			IEventConnectorService connectorService, IEventService eventService) {
		super(uri, submitQueueName, statusQueueName, statusTopicName, connectorService);
		this.eventService = eventService;

	}

	@Override
	public Class<U> getBeanClass() {
		return beanClass;
	}

	@Override
	public void setBeanClass(Class<U> beanClass) {
		this.beanClass = beanClass;
	}

	@Override
	public List<U> getQueue() throws EventException {
		return getQueue(getSubmitQueueName());
	}

	public List<U> getQueue(String queueName) throws EventException {
		IQueueReader<U> reader = eventService.createQueueReader(uri, queueName);
		reader.setBeanClass(beanClass);
		return reader.getQueue();
	}

	@Override
	public List<U> getRunningAndCompleted() throws EventException {
		final List<U> statusSet = getQueue(getStatusSetName());
		statusSet.sort((first, second) -> Long.signum(second.getSubmissionTime() - first.getSubmissionTime()));
		return statusSet;
	}

	@Override
	public boolean isQueuePaused() {
		QueueCommandBean bean = getLastPauseResumeBean(getSubmitQueueName());
		return bean != null && bean.getCommand() == Command.PAUSE;
	}

	/**
	 * Finds and returns the most recent pause bean sent to the command queue.
	 * @param submissionQueueName name of submission queue
	 * @return pause bean
	 */
	protected QueueCommandBean getLastPauseResumeBean(String submissionQueueName) {
		IQueueReader<QueueCommandBean>   queueReader=null;
		try {
			queueReader = eventService.createQueueReader(getUri(), EventConstants.CMD_SET);
			queueReader.setBeanClass(QueueCommandBean.class);
		    List<QueueCommandBean> commandQueue = queueReader.getQueue();

		    // The most recent bean in the queue is the latest
		    for (QueueCommandBean commandBean : commandQueue) {
		    	Command command = commandBean.getCommand();
				if (submissionQueueName.equals(commandBean.getQueueName())
						&& (command == Command.PAUSE || command == Command.RESUME)) {
					return commandBean;
				}
			}
		} catch (Exception ne) {
			logger.error("Cannot get queue "+EventConstants.CMD_SET, ne);
			return null;
		} finally {
			try {
				if (queueReader!=null) queueReader.disconnect();
			} catch (EventException e) {
				logger.error("Cannot get disconnect "+EventConstants.CMD_SET, e);
			}
		}
		return null;
	}


}
