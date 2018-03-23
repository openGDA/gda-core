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
package org.eclipse.scanning.event;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.eclipse.scanning.api.event.EventConstants;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventConnectorService;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.alive.PauseBean;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.core.IQueueConnection;
import org.eclipse.scanning.api.event.core.IQueueReader;
import org.eclipse.scanning.api.event.core.ISubmitter;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractQueueConnection<U extends StatusBean> extends AbstractConnection implements IQueueConnection<U>{

	protected static final long TWO_DAYS = TimeUnit.DAYS.toMillis(2); // ms
	protected static final long A_WEEK = TimeUnit.DAYS.toMillis(7); // ms

	private static final Logger logger = LoggerFactory.getLogger(AbstractQueueConnection.class);

	protected IEventService eservice;

	private Class<U> beanClass;

	AbstractQueueConnection(URI uri, String topic, IEventConnectorService service, IEventService eservice) {
		super(uri, topic, service);
		this.eservice = eservice;
	}

	AbstractQueueConnection(URI uri, String submitQueueName, String statusQueueName, String statusTopicName,
			String commandTopicName, IEventConnectorService service, IEventService eservice) {
		super(uri, submitQueueName, statusQueueName, statusTopicName, commandTopicName, service);
		this.eservice = eservice;
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
		QueueReader<U> reader = new QueueReader<>(getConnectorService());
		try {
			return reader.getBeans(uri, getSubmitQueueName(), beanClass);
		} catch (Exception e) {
			throw new EventException("Cannot get the beans for queue " + getSubmitQueueName(), e);
		}
	}

	@Override
	public List<U> getQueue(String qName) throws EventException {
		QueueReader<U> reader = new QueueReader<>(service);
		try {
			return reader.getBeans(uri, qName, beanClass);
		} catch (Exception e) {
			throw new EventException("Cannot get the beans for queue " + qName, e);
		}
	}

	protected Map<String, U> getMap(String queueName) throws EventException {

		final List<U> queue = getQueue(queueName);
		if (queue==null || queue.isEmpty()) return null;
		final HashMap<String, U> id = new HashMap<>(queue.size());
		for (U u : queue) id.put(u.getUniqueId(), u);
		return id;
	}

	@Override
	public void cleanQueue(String queueName) throws EventException {

		try {
			QueueConnection qCon = null;

			try {
				QueueConnectionFactory connectionFactory = (QueueConnectionFactory)service.createConnectionFactory(uri);
				qCon  = connectionFactory.createQueueConnection();
				QueueSession    qSes  = qCon.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
				Queue queue   = qSes.createQueue(queueName);
				qCon.start();

				QueueBrowser qb = qSes.createBrowser(queue);

				@SuppressWarnings("rawtypes")
				Enumeration  e  = qb.getEnumeration();

				Map<String, StatusBean> failIds = new LinkedHashMap<>();
				List<String>          removeIds = new ArrayList<>();
				while(e.hasMoreElements()) {
					Message m = (Message)e.nextElement();
					if (m==null) continue;
					if (m instanceof TextMessage) {
						TextMessage t = (TextMessage)m;

						try {
							final String     json  = t.getText();
							@SuppressWarnings("unchecked")
							final Class<U> statusBeanClass = (Class<U>) StatusBean.class;
							final StatusBean qbean = service.unmarshal(json, beanClass != null ? beanClass : statusBeanClass);
							if (qbean==null)               continue;
							if (qbean.getStatus()==null)   continue;
							if (!qbean.getStatus().isStarted() || qbean.getStatus()==Status.PAUSED) {
								failIds.put(t.getJMSMessageID(), qbean);
								continue;
							}

							// If it has failed, we clear it up
							if (qbean.getStatus()==Status.FAILED) {
								removeIds.add(t.getJMSMessageID());
								continue;
							}
							if (qbean.getStatus()==Status.NONE) {
								removeIds.add(t.getJMSMessageID());
								continue;
							}

							// If it is running and older than a certain time, we clear it up
							if (qbean.getStatus().isRunning()) {
								final long submitted = qbean.getSubmissionTime();
								final long current   = System.currentTimeMillis();
								if (current-submitted > getMaximumRunningAge()) {
									removeIds.add(t.getJMSMessageID());
									continue;
								}
							}

							if (qbean.getStatus().isFinal()) {
								final long submitted = qbean.getSubmissionTime();
								final long current   = System.currentTimeMillis();
								if (current-submitted > getMaximumCompleteAge()) {
									removeIds.add(t.getJMSMessageID());
								}
							}

						} catch (Exception ne) {
							logger.warn("Message "+t.getText()+" is not legal and will be removed.", ne);
							removeIds.add(t.getJMSMessageID());
						}
					}
				}

				// We fail the non-started jobs now - otherwise we could
				// actually start them late. TODO check this
				final List<String> ids = new ArrayList<>();
				ids.addAll(failIds.keySet());
				ids.addAll(removeIds);

				for (String jMSMessageID : ids) {
					MessageConsumer consumer = qSes.createConsumer(queue, "JMSMessageID = '"+jMSMessageID+"'");
					Message m = consumer.receive(Constants.getReceiveFrequency());
					consumer.close();
					if (removeIds.contains(jMSMessageID)) continue; // We are done

					if (m!=null && m instanceof TextMessage) {
						MessageProducer producer = qSes.createProducer(queue);
						final StatusBean    bean = failIds.get(jMSMessageID);
						bean.setStatus(Status.FAILED);
						producer.send(qSes.createTextMessage(service.marshal(bean)));

						logger.warn("Failed job {} messageid({})", bean.getName(), jMSMessageID);
					}
				}
			} finally {
				if (qCon!=null) qCon.close();
			}
		} catch (Exception ne) {
			throw new EventException("Problem connecting to "+queueName+" in order to clean it!", ne);
		}
	}


	@Override
	public void clearQueue(String qName) throws EventException {

		QueueConnection qCon = null;
		try {
			QueueConnectionFactory connectionFactory = (QueueConnectionFactory)service.createConnectionFactory(uri);
			qCon  = connectionFactory.createQueueConnection(); // This times out when the server is not there.
			QueueSession    qSes  = qCon.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
			Queue queue   = qSes.createQueue(qName);
			qCon.start();

			QueueBrowser qb = qSes.createBrowser(queue);

			@SuppressWarnings("rawtypes")
			Enumeration  e  = qb.getEnumeration();
			while(e.hasMoreElements()) {
				Message msg = (Message)e.nextElement();
				MessageConsumer consumer = qSes.createConsumer(queue, "JMSMessageID = '"+msg.getJMSMessageID()+"'");
				Message rem = consumer.receive(Constants.getReceiveFrequency());
				if (rem!=null) System.out.println("Removed "+rem);
				consumer.close();
			}

		} catch (Exception ne) {
			throw new EventException(ne);

		} finally {
			if (qCon!=null) {
				try {
					qCon.close();
				} catch (JMSException e) {
					logger.error("Cannot close queue!", e);
				}
			}
		}
	}

	@Override
	public boolean reorder(U bean, String queueName, int amount) throws EventException {

		if (amount==0) return false; // Nothing to reorder, no exception required, order unchanged.

		PauseBean pbean = new PauseBean(queueName);
		pbean.setMessage("Pause to reorder '"+bean.getName()+"' "+amount);

		IPublisher<PauseBean> publisher = createPausePublisher();
		final boolean isAlreadyPaused = isQueuePaused(queueName);
		if (!isAlreadyPaused) publisher.broadcast(pbean);

		try {

			// We are paused, read the queue
			List<U> submitted = getQueue(queueName);
			if (submitted==null || submitted.isEmpty())
				throw new EventException("There is nothing submitted waiting to be run\n\nPerhaps the job started to run.");

			Collections.reverse(submitted); // It comes out with the head at 0 and tail at size-1
			boolean found = false;
			int index = -1;
			for (U u : submitted) {
				index++;
				if (u.getUniqueId().equals(bean.getUniqueId())) {
					found=true;
					break;
				}
			}
			if (!found) throw new EventException("Cannot find bean '"+bean.getName()+"' in submission queue!\nIt might be running now.");

			if (index<1 && amount<0) throw new EventException("'"+bean.getName()+"' is already at the tail of the submission queue.");
			if (index+amount>submitted.size()-1) throw new EventException("'"+bean.getName()+"' is already at the head of the submission queue.");

			clearQueue(queueName);

			U existing = submitted.get(index);
			if (amount>0) {
				submitted.add(index+amount+1, existing);
				submitted.remove(index);
			} else {
				submitted.add(index+amount, existing);
				submitted.remove(index+1);
			}

			Collections.reverse(submitted); // It goes back with the head at 0 and tail at size-1

			@SuppressWarnings("unchecked")
			ISubmitter<U> submitter = this instanceof ISubmitter
					                ? (ISubmitter<U>)this
					                : eservice.createSubmitter(getUri(), queueName);

			for (U u : submitted) submitter.submit(u);

			return true; // It was reordered
		} finally {
			if (!isAlreadyPaused) {
				pbean.setPause(false);
				publisher.broadcast(pbean);
			}
		}
	}

	private IPublisher<PauseBean> createPausePublisher() {
		IPublisher<PauseBean> publisher = eservice.createPublisher(getUri(), EventConstants.CMD_TOPIC);
		publisher.setStatusSetName(EventConstants.CMD_SET);
		publisher.setStatusSetAddRequired(true);
		return publisher;
	}

	@Override
	public boolean remove(U bean, String queueName) throws EventException {

		QueueConnection send = null;
		QueueSession session = null;

		PauseBean pbean = new PauseBean(queueName);
		pbean.setMessage("Pause to remove '"+bean.getName()+"' ");

		IPublisher<PauseBean> publisher = createPausePublisher();
		final boolean isAlreadyPaused = isQueuePaused(queueName);
		if (!isAlreadyPaused) publisher.broadcast(pbean);

		try {

			QueueConnectionFactory connectionFactory = (QueueConnectionFactory)service.createConnectionFactory(uri);
			send  = connectionFactory.createQueueConnection(); // This times out when the server is not there.
			session  = send.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
			Queue queue   = session.createQueue(queueName);
			send.start();

			QueueBrowser qb = session.createBrowser(queue);
			@SuppressWarnings("rawtypes")
			Enumeration  e  = qb.getEnumeration();

			String jMSMessageID = null;
			while (e.hasMoreElements()) {
				Message m = (Message)e.nextElement();
				if (m==null) continue;
				if (m instanceof TextMessage) {
					TextMessage t = (TextMessage)m;

					final U qbean = service.unmarshal(t.getText(), null);
					if (qbean==null) continue;
					if (isSame(qbean, bean)) {
						jMSMessageID = t.getJMSMessageID();
						break;
					}
				}
			}

			qb.close();

			if (jMSMessageID != null) {
				MessageConsumer consumer = session.createConsumer(queue, "JMSMessageID = '"+jMSMessageID+"'");
				Message m = consumer.receive(1000);
				consumer.close();
				return m != null; // It might have been removed ok
			}

			return false; // It was not removed
		} catch (Exception ne) {
			throw new EventException("Cannot remove item "+bean, ne);

		}  finally {
			if (!isAlreadyPaused) {
				pbean.setPause(false);
				publisher.broadcast(pbean);
			}
			try {
				if (send!=null)     send.close();
				if (session!=null)  session.close();
			} catch (Exception e) {
				throw new EventException("Cannot close connection as expected!", e);
			}
		}

	}

	@Override
	public boolean replace(U bean, String queueName) throws EventException {


		PauseBean pbean = new PauseBean(queueName);
		pbean.setMessage("Pause to replace '"+bean.getName()+"' ");

		IPublisher<PauseBean> publisher = createPausePublisher();
		final boolean isAlreadyPaused = isQueuePaused(queueName);
		if (!isAlreadyPaused) publisher.broadcast(pbean);

		try {

			// We are paused, read the queue
			List<U> submitted = getQueue(queueName);
			if (submitted==null || submitted.size()<1) throw new EventException("There is nothing submitted waiting to be run\n\nPerhaps the job started to run.");

			boolean found = false;
			for (int i = 0; i < submitted.size(); i++) {
				U u = submitted.get(i);
				if (u.getUniqueId().equals(bean.getUniqueId())) {
					found=true;
					submitted.set(i, bean);
					break;
				}
			}
			if (!found) throw new EventException("Cannot find bean '"+bean.getName()+"' in submission queue!\nIt might be running now.");

			clearQueue(queueName);

			ISubmitter<U> submitter = this instanceof ISubmitter
					                ? (ISubmitter<U>)this
					                : (ISubmitter<U>)eservice.createSubmitter(getUri(), queueName);

		    for (U u : submitted) submitter.submit(u);

		    return true; // It was reordered

		} finally {
			if (!isAlreadyPaused) {
				pbean.setPause(false);
				publisher.broadcast(pbean);
			}
		}

	}

	/**
	 * Defines the time in ms that a job may be in the running state
	 * before the consumer might consider it for deletion. If a consumer
	 * is restarted it will normally delete old running jobs older than
	 * this age.
	 *
	 * @return
	 */
	public long getMaximumRunningAge() {
		if (System.getProperty("org.eclipse.scanning.event.consumer.maximumRunningAge")!=null) {
			return Long.parseLong(System.getProperty("org.eclipse.scanning.event.consumer.maximumRunningAge"));
		}
		return TWO_DAYS;
	}

	/**
	 * Defines the time in ms that a job may be in the complete (or other final) state
	 * before the consumer might consider it for deletion. If a consumer
	 * is restarted it will normally delete old complete jobs older than
	 * this age.
	 *
	 * @return
	 */
	public long getMaximumCompleteAge() {
		if (System.getProperty("org.eclipse.scanning.event.consumer.maximumCompleteAge")!=null) {
			return Long.parseLong(System.getProperty("org.eclipse.scanning.event.consumer.maximumCompleteAge"));
		}
		return A_WEEK;
	}


	public void pause() throws EventException {
		throw new EventException("Method 'pause' is not implemented!");
	}

	public void resume() throws EventException {
		throw new EventException("Method 'pause' is not implemented!");
	}

	public boolean isQueuePaused(String submissionQueueName) {

		PauseBean bean = getPauseBean(submissionQueueName);
		return bean!=null ? bean.isPause() : false;
	}

	/**
	 * Finds and returns the most recent pause bean sent to the command queue.
	 * @param submissionQueueName name of submission queue
	 * @return pause bean
	 */
	protected PauseBean getPauseBean(String submissionQueueName) {

		IQueueReader<PauseBean>   qr=null;
		try {
			qr = eservice.createQueueReader(getUri(), EventConstants.CMD_SET);
			qr.setBeanClass(PauseBean.class);
		    List<PauseBean> pausedList = qr.getQueue();

		    // The most recent bean in the queue is the latest
		    for (PauseBean pauseBean : pausedList) {
				if (submissionQueueName.equals(pauseBean.getQueueName())) return pauseBean;
			}

		} catch (Exception ne) {
			ne.printStackTrace();
			logger.error("Cannot get queue "+EventConstants.CMD_SET, ne);
			return null;

		} finally {
			try {
				if (qr!=null) qr.disconnect();
			} catch (EventException e) {
				logger.error("Cannot get disconnect "+EventConstants.CMD_SET, e);
			}
		}
		return null;
	}

}
