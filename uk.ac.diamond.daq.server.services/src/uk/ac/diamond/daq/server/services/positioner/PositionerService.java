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

package uk.ac.diamond.daq.server.services.positioner;

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
import uk.ac.diamond.daq.concurrent.Async;
import uk.ac.diamond.daq.jms.ErrorResponse;
import uk.ac.diamond.daq.jms.Response;
import uk.ac.diamond.daq.jms.positioner.Positioner;
import uk.ac.diamond.daq.jms.positioner.PositionerQueue;
import uk.ac.diamond.daq.jms.positioner.PositionerStatus;
import uk.ac.diamond.daq.jms.positioner.request.AvailablePositionersRequest;
import uk.ac.diamond.daq.jms.positioner.request.GetPositionRequest;
import uk.ac.diamond.daq.jms.positioner.request.GetPositionerRequest;
import uk.ac.diamond.daq.jms.positioner.request.SetPositionRequest;
import uk.ac.diamond.daq.jms.positioner.request.StopRequest;
import uk.ac.diamond.daq.jms.positioner.response.AvailablePositionersResponse;
import uk.ac.diamond.daq.jms.positioner.response.GetPositionResponse;
import uk.ac.diamond.daq.jms.positioner.response.GetPositionerResponse;
import uk.ac.diamond.daq.jms.positioner.response.PositionerUpdateResponse;
import uk.ac.diamond.daq.jms.positioner.response.SetPositionResponse;
import uk.ac.diamond.daq.jms.positioner.response.StopResponse;

/**
 * Presents a simplified JMS interface to the scannable layer. Available scannables can be list, and their positions got
 * and set.
 *
 * @author Eliot Hall
 *
 */
public class PositionerService implements IObserver, Configurable, MessageListener {
	private static final Logger log = LoggerFactory.getLogger(PositionerService.class);

	private static final String SERVICE_ID = "GDA-SERVER";

	private final PositionerFactory positionerFactory;

	private Session session;

	private boolean connected;
	private boolean configured;
	private MessageProducer posUpdate;

	private MessageConverter messageConveter = jacksonJmsMessageConverter();

	public PositionerService(PositionerFactory positionerFactory) {
		this.positionerFactory = positionerFactory;

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

			MessageConsumer consumer = session.createConsumer(new ActiveMQQueue(PositionerQueue.COMMAND_QUEUE));
			var topic = session.createTopic(PositionerQueue.UPDATE_QUEUE);
			posUpdate = session.createProducer(topic);
			connected = true;

			consumer.setMessageListener(this);

			configured = true;
		} catch (JMSException e) {
			throw new FactoryException("Unable to start Positioner service", e);
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
				if (result instanceof AvailablePositionersRequest) {
					response = availablePositioners();
				} else if (result instanceof GetPositionerRequest) {
					response = getPositioner((GetPositionerRequest) result);
				} else if (result instanceof GetPositionRequest) {
					response = getPosition((GetPositionRequest) result);
				} else if (result instanceof SetPositionRequest) {
					response = setPosition((SetPositionRequest) result);
				} else if (result instanceof StopRequest) {
					response = stop((StopRequest) result);
				}
			} catch (PositionerServiceException e) {
				log.error("Request {} failed: {}", result, e.getMessage());
				response = e.getErrorResponse();
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

	private AvailablePositionersResponse availablePositioners() {
		Map<String, Scannable> scannables = Finder.getFindablesOfType(Scannable.class);

		Set<String> positioners = scannables.values().stream().flatMap(scannable -> {
			try {
				Positioner positioner = positionerFactory.createPositioner(scannable);
				scannable.addIObserver(this);
				return Stream.of(positioner.getName());
			} catch (PositionerFactoryException e) {
				return Stream.empty();
			}
		}).collect(Collectors.toUnmodifiableSet());

		return new AvailablePositionersResponse(SERVICE_ID, positioners);
	}

	private Scannable findScannable(String name) throws PositionerServiceException {
		Scannable scannable = Finder.find(name);
		if (scannable != null) {
			return scannable;
		}
		String error = String.format("Positioner %s is not found", name);
		throw new PositionerServiceException(new ErrorResponse(SERVICE_ID, error));
	}

	private GetPositionerResponse getPositioner(GetPositionerRequest request) throws PositionerServiceException {
		Scannable scannable = findScannable(request.getPositionerName());

		try {
			Positioner positioner = positionerFactory.createPositioner(scannable);
			scannable.addIObserver(this);
			return new GetPositionerResponse(SERVICE_ID, positioner);
		} catch (PositionerFactoryException e) {
			throw new PositionerServiceException(new ErrorResponse(SERVICE_ID, e.getMessage()));
		}
	}

	private GetPositionResponse getPosition(GetPositionRequest request) throws PositionerServiceException {
		Scannable scannable = findScannable(request.getPositionerName());

		try {
			String position = positionerFactory.getPosition(scannable);
			scannable.addIObserver(this);
			return new GetPositionResponse(SERVICE_ID, position);
		} catch (PositionerFactoryException e) {
			throw new PositionerServiceException(new ErrorResponse(SERVICE_ID, e.getMessage()));
		}
	}

	private SetPositionResponse setPosition(SetPositionRequest request) throws PositionerServiceException {
		Scannable scannable = findScannable(request.getPositionerName());
		try {
			scannable.addIObserver(this);
			String message = positionerFactory.moveTo(scannable, request.getValue());
			return new SetPositionResponse(SERVICE_ID, message);
		} catch (PositionerFactoryException e) {
			throw new PositionerServiceException(new ErrorResponse(SERVICE_ID, e.getMessage()));
		}
	}

	private StopResponse stop(StopRequest request) throws PositionerServiceException {
		Scannable scannable = findScannable(request.getPositionerName());
		try {
			String message = positionerFactory.stop(scannable);
			return new StopResponse(SERVICE_ID, message);
		} catch (PositionerFactoryException e) {
			throw new PositionerServiceException(new ErrorResponse(SERVICE_ID, e.getMessage()));
		}
	}

	@Override
	public void update(Object theObserved, Object changeCode) {
		if (connected && theObserved instanceof Scannable) {
			try {
				PositionerWorker positionerWorker = new PositionerWorker((Scannable) theObserved, session,
						positionerFactory, SERVICE_ID);
				Async.execute(positionerWorker);

				String position = positionerFactory.getPosition((Scannable) theObserved);
				PositionerStatus status = positionerFactory.convertStatus((Scannable) theObserved, changeCode);
				Response response = new PositionerUpdateResponse(SERVICE_ID, ((Scannable) theObserved).getName(),
						position, status);
				Message resonseMessage = messageConveter.toMessage(response, session);
				posUpdate.send(resonseMessage);
			} catch (PositionerFactoryException | JMSException e) {
				log.error("Failed to send update", e);
			}
		}
	}
}
