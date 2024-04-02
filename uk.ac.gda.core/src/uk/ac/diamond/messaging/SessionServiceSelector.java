/*-
 * Copyright © 2023 Diamond Light Source Ltd.
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

package uk.ac.diamond.messaging;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.QueueSession;
import javax.jms.Session;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import uk.ac.diamond.daq.services.PropertyService;
import uk.ac.diamond.mq.ISessionService;
import uk.ac.diamond.mq.activemq.ManagedActiveMQSessionService;
import uk.ac.diamond.mq.rabbitmq.ManagedRabbitMQSessionService;

@Component(service=ISessionService.class, name="ISessionService")
public final class SessionServiceSelector implements ISessionService {

	private static final Logger logger = LoggerFactory.getLogger(SessionServiceSelector.class);

	private ISessionService sessionService;

	@Reference
	private PropertyService properties;

	@Activate
	protected final void activate() {
		String impl = properties.getAsString(LocalProperties.GDA_MESSAGE_BROKER_IMPL, "activemq");
		logger.info("Starting ISessionService with {} implementation", impl);
		if ("rabbitmq".equals(impl)) {
			this.sessionService = new ManagedRabbitMQSessionService();
		} else {
			this.sessionService = new ManagedActiveMQSessionService();
		}
	}

	@Override
	public Session getSession(String brokerUri, boolean transacted, int acknowledgeMode) throws JMSException {
		return sessionService.getSession(brokerUri, transacted, acknowledgeMode);
	}

	@Override
	public QueueSession getQueueSession(String brokerUri, boolean transacted, int acknowledgeMode) throws JMSException {
		return sessionService.getQueueSession(brokerUri, transacted, acknowledgeMode);
	}

	@Override
	public String getDefaultUri() {
		return LocalProperties.getBrokerURI();
	}

	@Override
	public boolean defaultConnectionActive() {
		return sessionService.defaultConnectionActive();
	}

	@Override
	public Destination declareAMQPTopic(String brokerUri, String topicName) throws IOException, TimeoutException {
		return sessionService.declareAMQPTopic(brokerUri, topicName);
	}

}
