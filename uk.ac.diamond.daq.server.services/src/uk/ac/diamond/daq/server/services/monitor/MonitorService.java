/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.server.services.monitor;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.apache.activemq.command.ActiveMQQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

import gda.data.ServiceHolder;
import gda.device.Scannable;
import gda.factory.Configurable;
import gda.factory.FactoryException;
import gda.factory.Finder;
import gda.observable.IObserver;
import uk.ac.diamond.daq.classloading.GDAClassLoaderService;
import uk.ac.diamond.daq.jms.ErrorResponse;
import uk.ac.diamond.daq.jms.Response;
import uk.ac.diamond.daq.jms.monitor.Monitor;
import uk.ac.diamond.daq.jms.monitor.MonitorRequests.AvailableMonitorsRequest;
import uk.ac.diamond.daq.jms.monitor.MonitorRequests.GetMonitorRequest;
import uk.ac.diamond.daq.jms.monitor.MonitorRequests.GetValueRequest;
import uk.ac.diamond.daq.jms.monitor.MonitorResponses.AvailableMonitorsResponse;
import uk.ac.diamond.daq.jms.monitor.MonitorResponses.GetMonitorResponse;
import uk.ac.diamond.daq.jms.monitor.MonitorResponses.GetValueResponse;
import uk.ac.diamond.daq.jms.monitor.MonitorResponses.MonitorUpdateResponse;

/**
 * JMS interface to generic scannables which are "read-only".
 */
public class MonitorService implements IObserver, Configurable, MessageListener {
	private static final Logger log = LoggerFactory.getLogger(MonitorService.class);

	private static final String SERVICE_ID = "GDA-SERVER";

	private final MonitorAdapter monitorAdapter;

	private Session session;

	private boolean connected;
	private boolean configured;
	private MessageProducer posUpdate;

	private MessageConverter messageConveter = jacksonJmsMessageConverter();

	public MonitorService() {
		this.monitorAdapter = new MonitorAdapter();

		configured = false;
	}

	public MessageConverter jacksonJmsMessageConverter() {
		MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
		converter.setTargetType(MessageType.TEXT);
		converter.setTypeIdPropertyName("_type");
		converter.setBeanClassLoader(GDAClassLoaderService.getClassLoaderService().getClassLoader());
		return converter;
	}

	@Override
	public void configure() throws FactoryException {
		try {
			session = ServiceHolder.getSessionService().getSession();

			MessageConsumer consumer = session.createConsumer(new ActiveMQQueue("api.monitor.command"));
			var topic = session.createTopic("api.monitor.update");
			posUpdate = session.createProducer(topic);
			connected = true;

			consumer.setMessageListener(this);

			configured = true;
		} catch (JMSException e) {
			throw new FactoryException("Unable to start Monitor service", e);
		}
	}

	@Override
	public boolean isConfigured() {
		return configured;
	}

	@Override
	public void reconfigure() throws FactoryException {
		// Not required
	}

	@Override
	public boolean isConfigureAtStartup() {
		return true;
	}

	@Override
	public void onMessage(Message message) {
		try {
			var result = messageConveter.fromMessage(message);
			Response response = null;
			try {
				if (result instanceof AvailableMonitorsRequest) {
					response = availableMonitors();
				} else if (result instanceof GetMonitorRequest) {
					response = getMonitor((GetMonitorRequest) result);
				} else if (result instanceof GetValueRequest) {
					response = getValue((GetValueRequest) result);
				}
			} catch (MonitorServiceException e) {
				log.error("Request {} failed: {}", result, e.getMessage());
				response = new ErrorResponse(SERVICE_ID, "error getting monitor");
			}
			if (response != null) {
				Message resonseMessage = messageConveter.toMessage(response, session);
				resonseMessage.setJMSCorrelationID(message.getJMSMessageID());
				MessageProducer producer = session.createProducer(message.getJMSReplyTo());
				producer.send(resonseMessage);
			}
		} catch (JMSException e) {
			log.error("Failed to send response to request", e);
		}
	}

	private AvailableMonitorsResponse availableMonitors() {
		Map<String, Scannable> scannables = Finder.getFindablesOfType(Scannable.class);

		Set<String> monitors = scannables.values().stream().flatMap(scannable -> {
			try {
				Monitor monitor = monitorAdapter.createMonitor(scannable);
				scannable.addIObserver(this);
				return Stream.of(monitor.name());
			} catch (MonitorServiceException e) {
				return Stream.empty();
			}
		}).collect(Collectors.toUnmodifiableSet());

		return new AvailableMonitorsResponse(SERVICE_ID, monitors);
	}

	private Scannable findScannable(String name) throws MonitorServiceException {
		Scannable scannable = Finder.find(name);
		if (scannable != null) {
			return scannable;
		}
		String error = String.format("Monitor %s is not found", name);
		throw new MonitorServiceException(error);
	}

	private GetMonitorResponse getMonitor(GetMonitorRequest request) throws MonitorServiceException {
		Scannable scannable = findScannable(request.monitorName());

		try {
			var positioner = monitorAdapter.createMonitor(scannable);
			scannable.addIObserver(this);
			return new GetMonitorResponse(SERVICE_ID, positioner);
		} catch (MonitorServiceException e) {
			throw new MonitorServiceException("", e);
		}
	}

	private GetValueResponse getValue(GetValueRequest request) throws MonitorServiceException {
		Scannable scannable = findScannable(request.monitorName());

		try {
			String position = monitorAdapter.getValue(scannable);
			scannable.addIObserver(this);
			return new GetValueResponse(SERVICE_ID, position);
		} catch (MonitorServiceException e) {
			throw new MonitorServiceException("", e);
		}
	}

	@Override
	public void update(Object theObserved, Object changeCode) {
		if (connected && theObserved instanceof Scannable) {
			try {
				String position = monitorAdapter.getValue((Scannable) theObserved);
				var status = monitorAdapter.convertStatus((Scannable) theObserved, changeCode);
				Response response = new MonitorUpdateResponse(SERVICE_ID, ((Scannable) theObserved).getName(),
						position, status);
				Message resonseMessage = messageConveter.toMessage(response, session);
				posUpdate.send(resonseMessage);
			} catch (MonitorServiceException | JMSException e) {
				log.error("Failed to send update", e);
			}
		}
	}
}
