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

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventConnectorService;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.IReadOnlyQueueConnection;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractReadOnlyQueueConnection<U extends StatusBean> extends AbstractConnection implements IReadOnlyQueueConnection<U> {

	private static final Logger logger = LoggerFactory.getLogger(AbstractReadOnlyQueueConnection.class);

	protected IEventService eventService;

	protected Class<U> beanClass;

	AbstractReadOnlyQueueConnection(URI uri, IEventConnectorService connectorService, IEventService eventService) {
		super(uri, connectorService);
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
		return getQueue(getSubmitQueueName(), beanClass);
	}

	protected <T> List<T> getQueue(String queueName, Class<T> beanClass) throws EventException {
		QueueReader<T> reader = new QueueReader<>(getConnectorService());
		try {
			return reader.getBeans(uri, queueName, beanClass);
		} catch (Exception e) {
			throw new EventException("Cannot get the beans for queue " + getSubmitQueueName(), e);
		}
	}

	@Override
	public List<U> getRunningAndCompleted() throws EventException {
		final List<U> statusSet = getQueue(getStatusSetName(), beanClass);
		statusSet.sort((first, second) -> Long.signum(second.getSubmissionTime() - first.getSubmissionTime()));
		return statusSet;
	}

}
