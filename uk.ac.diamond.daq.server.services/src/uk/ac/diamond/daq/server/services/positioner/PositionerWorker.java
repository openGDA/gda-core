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

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.apache.activemq.command.ActiveMQQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

import gda.device.Scannable;
import gda.observable.IObserver;
import uk.ac.diamond.daq.jms.Response;
import uk.ac.diamond.daq.jms.positioner.PositionerQueue;
import uk.ac.diamond.daq.jms.positioner.PositionerStatus;
import uk.ac.diamond.daq.jms.positioner.response.PositionerUpdateResponse;

public class PositionerWorker implements Runnable, IObserver {
	private static final Logger log = LoggerFactory.getLogger(PositionerWorker.class);

	public enum WorkerStatus {
		WAITING, MOVING, COMPLETE
	}

	private long sleep = 500;

	private String serviceId;
	private Session session;
	private Scannable scannable;

	private PositionerFactory positionerFactory;
	private MessageProducer producer;
	private WorkerStatus workerStatus;

	private MessageConverter messageConveter = jacksonJmsMessageConverter();

	public PositionerWorker(Scannable scannable, Session session, PositionerFactory positionerFactory, String serviceId)
			throws JMSException {
		this.scannable = scannable;
		this.session = session;
		this.positionerFactory = positionerFactory;
		this.serviceId = serviceId;

		producer = session.createProducer(new ActiveMQQueue(PositionerQueue.UPDATE_QUEUE));
		workerStatus = WorkerStatus.WAITING;

		scannable.addIObserver(this);
	}

	public MessageConverter jacksonJmsMessageConverter() {
		MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
		converter.setTargetType(MessageType.TEXT);
		converter.setTypeIdPropertyName("_type");
		return converter;
	}

	@Override
	public void run() {
		while (workerStatus != WorkerStatus.COMPLETE) {
			sendMessage();

			try {
				Thread.sleep(sleep);
			} catch (InterruptedException e) {
				// Has been interrupted
			}
		}

		scannable.deleteIObserver(this);
	}

	public void sendMessage() {
		try {
			PositionerStatus positionerStatus = positionerFactory.getStatus(scannable);
			setStatus(positionerStatus);

			if (workerStatus == WorkerStatus.MOVING) {
				String position = positionerFactory.getPosition(scannable);
				Response response = new PositionerUpdateResponse(serviceId, scannable.getName(), position,
						positionerStatus);
				Message resonseMessage = messageConveter.toMessage(response, session);
				producer.send(resonseMessage);
			}
		} catch (PositionerFactoryException e) {
			log.error("Cannot convert", e);
			workerStatus = WorkerStatus.COMPLETE;
		} catch (JMSException e) {
			log.error("Failed to send message, exiting worker", e);
			workerStatus = WorkerStatus.COMPLETE;
		}
	}

	private void setStatus(PositionerStatus positionerStatus) {
		if (workerStatus == WorkerStatus.COMPLETE) {
			return;
		}

		if (positionerStatus == PositionerStatus.ERROR) {
			workerStatus = WorkerStatus.COMPLETE;
		} else if (workerStatus == WorkerStatus.WAITING && positionerStatus == PositionerStatus.MOVING) {
			workerStatus = WorkerStatus.MOVING;
		} else if (workerStatus == WorkerStatus.MOVING && positionerStatus == PositionerStatus.STOPPED) {
			workerStatus = WorkerStatus.COMPLETE;
		}
	}

	@Override
	public void update(Object source, Object arg) {
		try {
			PositionerStatus positionerStatus = positionerFactory.convertStatus((Scannable) source, arg);
			setStatus(positionerStatus);
		} catch (PositionerFactoryException e) {
			log.error("Cannot convert", e);
			workerStatus = WorkerStatus.COMPLETE;
		}
	}

	public void setWorkerStatus(WorkerStatus workerStatus) {
		this.workerStatus = workerStatus;
	}

	public WorkerStatus getWorkerStatus() {
		return workerStatus;
	}
}
