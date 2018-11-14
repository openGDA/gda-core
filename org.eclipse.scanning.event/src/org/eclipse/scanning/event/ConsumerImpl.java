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

import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

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

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventConnectorService;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.alive.ConsumerStatus;
import org.eclipse.scanning.api.event.alive.ConsumerStatusBean;
import org.eclipse.scanning.api.event.alive.QueueCommandBean;
import org.eclipse.scanning.api.event.alive.QueueCommandBean.Command;
import org.eclipse.scanning.api.event.bean.BeanEvent;
import org.eclipse.scanning.api.event.bean.IBeanListener;
import org.eclipse.scanning.api.event.core.IConsumer;
import org.eclipse.scanning.api.event.core.IConsumerProcess;
import org.eclipse.scanning.api.event.core.IProcessCreator;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.core.ISubmitter;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ConsumerImpl<U extends StatusBean> extends AbstractConnection implements IConsumer<U> {


	private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerImpl.class);
	private static final long ONE_DAY = TimeUnit.DAYS.toMillis(1);

	private String name;
	private UUID consumerId;
	private IEventService eventService;
	private IPublisher<U> statusTopicPublisher; // a publisher to the status topic
	private ConsumerStatusPublisher consumerStatusBroadcaster;
	private ISubscriber<IBeanListener<QueueCommandBean>> commandTopicSubscriber; // a subscriber to the command topic
	private IPublisher<QueueCommandBean> commandAckTopicPublisher; // a publisher to the command acknowledgement topic
	private ISubmitter<U> statusSetSubmitter; // a submitter to the status set

	private boolean pauseOnStart = false;
	private CountDownLatch latchStart;
	private int waitTime = 0;

	private IProcessCreator<U> runner;
	private MessageConsumer messageConsumer;

	private volatile boolean active = false;
	private Map<String, U> beanOverrideMap;

	private final ProcessManager processManager = new ProcessManager();
	/**
	 * Concurrency design recommended by Keith Ralphs after investigating
	 * how to pause and resume a collection cycle using Reentrant locks.
	 * Design requires these three fields.
	 */
	private ReentrantLock consumerStateChangeLock;
	private Condition shouldResumeCondition;

	/**
	 * A flag to indicate that the consumer should pause before consuming the next bean from the queue.
	 */
	private volatile boolean awaitPaused;

	/**
	 * A flag to indicate if the consumer thread (the thread that consumes items from the queue and runs them)
	 * is running, irrespective of whether it is paused.
	 */
	private volatile boolean consumerThreadRunning = false;

	private Class<U> beanClass;

	private final String commandTopicName;
	private final String commandAckTopicName;
	private final String consumerStatusTopicName;

	private final Set<IConsumerStatusListener> statusListeners = new CopyOnWriteArraySet<>();

	public ConsumerImpl(URI uri, String submitQueueName, String statusQueueName, String statusTopicName,
			String consumerStatusTopicName, String commandTopicName, String commandAckTopicName,
			IEventConnectorService connectorService, IEventService eventService)
			throws EventException {
		super(uri, submitQueueName, statusQueueName, statusTopicName, connectorService);
		this.eventService = eventService;

		this.awaitPaused = false;
		this.consumerStateChangeLock = new ReentrantLock();
		this.shouldResumeCondition = consumerStateChangeLock.newCondition();

		consumerId = UUID.randomUUID();
		name = "Consumer " + consumerId; // This will hopefully be changed to something meaningful...
		this.commandTopicName = commandTopicName;
		this.commandAckTopicName = commandAckTopicName;
		this.consumerStatusTopicName = consumerStatusTopicName;

		connect();
	}

	@Override
	public void addConsumerStatusListener(IConsumerStatusListener listener) {
		statusListeners.add(listener);
	}

	@Override
	public void removeConsumerStatusListener(IConsumerStatusListener listener) {
		statusListeners.remove(listener);
	}

	private void notifyStatusChanged() {
		if (consumerStatusBroadcaster != null) {
			consumerStatusBroadcaster.publishConsumerStatusBean();
		}

		final ConsumerStatus status = getConsumerStatus();
		for (IConsumerStatusListener listener : statusListeners) {
			listener.consumerStatusChanged(status);
		}
	}

	private void connect() throws EventException {
		statusSetSubmitter  = eventService.createSubmitter(uri, getStatusSetName());
		statusTopicPublisher = eventService.createPublisher(uri, getStatusTopicName());
		statusTopicPublisher.setStatusSetName(getStatusSetName()); // We also update values in a queue.

		if (consumerStatusTopicName!=null) {
			consumerStatusBroadcaster = new ConsumerStatusPublisher(uri);
		}

		if (getCommandTopicName()!=null) {
			commandTopicSubscriber = eventService.createSubscriber(uri, getCommandTopicName());
			commandTopicSubscriber.addListener(new CommandListener());
			commandAckTopicPublisher = eventService.createPublisher(uri, commandAckTopicName);
		}

		setConnected(true);
	}

	@Override
	public synchronized void disconnect() throws EventException {
		if (isActive()) stop();

		if (statusSetSubmitter != null) {
			statusSetSubmitter.disconnect();
			statusSetSubmitter = null;
		}
		if (statusTopicPublisher != null) {
			statusTopicPublisher.disconnect();
			statusTopicPublisher = null;
		}
		if (consumerStatusBroadcaster != null) {
			consumerStatusBroadcaster.disconnect();
			consumerStatusBroadcaster = null;
		}
		if (commandTopicSubscriber != null)  {
			commandTopicSubscriber.disconnect();
			commandTopicSubscriber = null;
		}
		if (commandAckTopicPublisher != null) {
			commandAckTopicPublisher.disconnect();
			commandAckTopicPublisher = null;
		}
		if (beanOverrideMap != null) {
			beanOverrideMap.clear();
		}

		super.disconnect();
	}

	protected class CommandListener implements IBeanListener<QueueCommandBean> {
		@Override
		public void beanChangePerformed(BeanEvent<QueueCommandBean> evt) {
			final QueueCommandBean commandBean = evt.getBean();
			if (isCommandForMe(commandBean)) {
				processQueueCommand(commandBean);
			}
		}

		protected boolean isCommandForMe(QueueCommandBean bean) {
			return bean.getConsumerId()!=null && bean.getConsumerId().equals(getConsumerId())
					|| bean.getQueueName()!=null && bean.getQueueName().equals(getSubmitQueueName());
		}
	}

	/**
	 * Processes a command for this consumer from the command topic.
	 * @param commandBean
	 */
	protected void processQueueCommand(QueueCommandBean commandBean) {
		LOGGER.debug("Consumer for queue {} received command bean {}", getSubmitQueueName(), commandBean);
		final Command command = commandBean.getCommand();
		Object result = null;
		try {
			switch (command) {
				case PAUSE_QUEUE:
					pause();
					break;
				case RESUME_QUEUE:
					resume();
					break;
				case STOP_QUEUE:
					stop();
					break;
				case RESTART_QUEUE:
					restart();
					break;
				case CLEAR_QUEUE:
					clearQueue();
					break;
				case CLEAR_COMPLETED:
					clearRunningAndCompleted();
					break;
				case PAUSE_JOB:
					processManager.processJobCommand(commandBean);
					break;
				case RESUME_JOB:
					processManager.processJobCommand(commandBean);
					break;
				case TERMINATE_JOB:
					processManager.processJobCommand(commandBean);
					break;
				case MOVE_FORWARD:
					result = findBeanAndPerformAction(commandBean.getBeanUniqueId(), this::moveForward);
					break;
				case MOVE_BACKWARD:
					result = findBeanAndPerformAction(commandBean.getBeanUniqueId(), this::moveBackward);
					break;
				case REMOVE_FROM_QUEUE:
					result = findBeanAndPerformAction(commandBean.getBeanUniqueId(), this::remove);
					break;
				case REMOVE_COMPLETED:
					removeCompleted(commandBean.getBeanUniqueId());
					break;
				case GET_QUEUE:
					result = getSubmissionQueue();
					break;
				case GET_RUNNING_AND_COMPLETED:
					result = getRunningAndCompleted();
					break;
				case GET_INFO:
					result = getConsumerInfo();
					break;
				default:
					throw new IllegalArgumentException("Unknown command " + commandBean.getCommand());
			}
		} catch (Exception e) {
			commandBean.setErrorMessage(MessageFormat.format("Could not process {0} command for queue {1}: {2}",
					command, getSubmitQueueName(), e.getMessage()));
			if (command == Command.PAUSE_QUEUE || command == Command.RESUME_QUEUE) {
				// for pause and resume commands, we stop and disconnect the consumer
				LOGGER.error("Unable to process {} command on consumer for queue '{}'. Consumer will stop.",
						commandBean.getCommand(), getSubmitQueueName(), e);
				try {
					stop();
					disconnect();
				} catch (EventException ee) {
					LOGGER.error("An internal error occurred trying to terminate the consumer "+getName()+" "+getConsumerId());
				}
			} else {
				LOGGER.error("Unable to process {} command on consumer for queue '{}'.",
						commandBean.getCommand(), getSubmitQueueName(), e);
			}
		} finally {
			try {
				commandBean.setResult(result);
				commandAckTopicPublisher.broadcast(commandBean);
			} catch (EventException e) {
				LOGGER.error("Could not publish acknowledgement for command bean {}", commandBean);
			}
		}
	}

	@Override
	public void restart() throws EventException {
		disconnect();
		connect();
		start();
	}


	/**
	 * Updates the given bean on the queue to have the same status as the one given.
	 * This is done by using a {@link QueueBrowser} to
	 * enumerate all the beans on the queue.
	 * @param bean
	 * @throws EventException
	 */
	protected void updateQueue(U bean) throws EventException {
		LOGGER.debug("Updating bean on queue {}", bean);
		boolean resumeAfter = !awaitPaused;
		Session session = null;
		QueueBrowser queueBrowser = null;
		try {
			pause();

			session = getSession();
			Queue queue = session.createQueue(getSubmitQueueName());
			queueBrowser = session.createBrowser(queue);

			// Iterates through all beans on the queue to find the one with the
			// same unique id as the given bean,
			@SuppressWarnings("rawtypes")
			Enumeration  e  = queueBrowser.getEnumeration();
			while (e.hasMoreElements()) {
				Message msg = (Message)e.nextElement();
				TextMessage t = (TextMessage)msg;
				String json = t.getText();
				final StatusBean b = service.unmarshal(json, getBeanClass());

				MessageConsumer consumer = session.createConsumer(queue, "JMSMessageID = '"+msg.getJMSMessageID()+"'");
				Message rem = consumer.receive(EventTimingsHelper.getReceiveTimeout());

				consumer.close();
				if (b.getUniqueId().equals(bean.getUniqueId())) {
					if (rem == null) {
						// Something went wrong, not sure why it does this, TODO investigate
						addBeanOverride(bean);
						continue;
					}
					b.setStatus(bean.getStatus());
					t = session.createTextMessage(service.marshal(b));
					t.setJMSMessageID(rem.getJMSMessageID());
					t.setJMSExpiration(rem.getJMSExpiration());
					t.setJMSTimestamp(rem.getJMSTimestamp());
					t.setJMSPriority(rem.getJMSPriority());
					t.setJMSCorrelationID(rem.getJMSCorrelationID());
				}

				MessageProducer producer = session.createProducer(queue);
				producer.send(t);
				producer.close();
			}
		} catch (Exception e) {
			throw new EventException("Cannot reorder queue!", e);
		} finally {
			// Only resume if it wasn't in a paused state before this update
			if (resumeAfter) {
				resume();
			}
			try {
				if (queueBrowser != null) queueBrowser.close();
			} catch (JMSException e) {
				throw new EventException("Cannot close session!", e);
			}
		}
	}

	private ConsumerInfo getConsumerInfo() {
		ConsumerInfo info = new ConsumerInfo();
		info.setConsumerId(getConsumerId());
		info.setName(getName());
		info.setStatus(getConsumerStatus());

		return info;
	}

	private void addBeanOverride(U bean) {
		if (beanOverrideMap == null) {
			beanOverrideMap = Collections.synchronizedMap(new HashMap<>());
		}
		beanOverrideMap.put(bean.getUniqueId(), bean);
	}

	@Override
	public List<U> getSubmissionQueue() throws EventException {
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
		statusSet.sort((first, second) -> Long.signum(first.getSubmissionTime() - second.getSubmissionTime()));
		return statusSet;
	}

	@Override
	public void clearQueue() throws EventException {
		// we need to pause the consumer before we clear the submission queue
		final String queueName = getSubmitQueueName();
		doWhilePaused(() -> { doClearQueue(queueName); return null; });
	}

	@Override
	public void clearRunningAndCompleted() throws EventException {
		doClearQueue(getStatusSetName());
	}

	private void doClearQueue(String queueName) throws EventException {
		LOGGER.info("Clearing queue {}", queueName);
		QueueConnection qCon = null;
		try {
			QueueConnectionFactory connectionFactory = (QueueConnectionFactory)service.createConnectionFactory(uri);
			qCon  = connectionFactory.createQueueConnection(); // This times out when the server is not there.
			QueueSession    qSes  = qCon.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
			Queue queue   = qSes.createQueue(queueName);
			qCon.start();

			QueueBrowser qb = qSes.createBrowser(queue);

			@SuppressWarnings("rawtypes")
			Enumeration  e  = qb.getEnumeration();
			while(e.hasMoreElements()) {
				Message msg = (Message)e.nextElement();
				MessageConsumer consumer = qSes.createConsumer(queue, "JMSMessageID = '" + msg.getJMSMessageID() + "'");
				Message rem = consumer.receive(EventTimingsHelper.getReceiveTimeout());
				if (rem != null) {
					LOGGER.trace("Removed bean {}", rem);
				} else {
					LOGGER.warn("Failed to remove next bean");
				}
				consumer.close();
			}
		} catch (Exception ne) {
			throw new EventException(ne);
		} finally {
			if (qCon!=null) {
				try {
					qCon.close();
				} catch (JMSException e) {
					LOGGER.error("Cannot close connection to queue {}", queueName, e);
				}
			}
		}
	}

	@Override
	public void cleanUpCompleted() throws EventException {
		try {
			QueueConnection qCon = null;

			try {
				QueueConnectionFactory connectionFactory = (QueueConnectionFactory)service.createConnectionFactory(uri);
				qCon  = connectionFactory.createQueueConnection();
				QueueSession    qSes  = qCon.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
				Queue queue   = qSes.createQueue(getStatusSetName());
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
							final StatusBean qbean = service.unmarshal(json, getBeanClass() != null ? getBeanClass() : statusBeanClass);
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
								if (current-submitted > EventTimingsHelper.getMaximumRunningAgeMs()) {
									removeIds.add(t.getJMSMessageID());
									continue;
								}
							}

							if (qbean.getStatus().isFinal()) {
								final long submitted = qbean.getSubmissionTime();
								final long current   = System.currentTimeMillis();
								if (current-submitted > EventTimingsHelper.getMaximumCompleteAgeMs()) {
									removeIds.add(t.getJMSMessageID());
								}
							}

						} catch (Exception ne) {
							LOGGER.warn("Message "+t.getText()+" is not legal and will be removed.", ne);
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
					Message m = consumer.receive(EventTimingsHelper.getReceiveTimeout());
					consumer.close();
					if (removeIds.contains(jMSMessageID)) continue; // We are done

					if (m!=null && m instanceof TextMessage) {
						MessageProducer producer = qSes.createProducer(queue);
						final StatusBean    bean = failIds.get(jMSMessageID);
						bean.setStatus(Status.FAILED);
						producer.send(qSes.createTextMessage(service.marshal(bean)));

						LOGGER.warn("Failed job {} messageid({})", bean.getName(), jMSMessageID);
					}
				}
			} finally {
				if (qCon!=null) qCon.close();
			}
		} catch (Exception ne) {
			throw new EventException("Problem connecting to "+statusQueueName+" in order to clean it!", ne);
		}
	}

	@Override
	public boolean moveForward(U bean) throws EventException {
		return doWhilePaused(() -> doReorder(bean, 1));
	}

	@Override
	public boolean moveBackward(U bean) throws EventException {
		return doWhilePaused(() -> doReorder(bean, -1));
	}

	private boolean doReorder(U bean, int amount) throws EventException {
		// We are paused, read the queue
		List<U> submitted = getSubmissionQueue();
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

		clearQueue();

		U existing = submitted.get(index);
		if (amount>0) {
			submitted.add(index+amount+1, existing);
			submitted.remove(index);
		} else {
			submitted.add(index+amount, existing);
			submitted.remove(index+1);
		}

		Collections.reverse(submitted); // It goes back with the head at 0 and tail at size-1

		try (ISubmitter<U> submitter = eventService.createSubmitter(getUri(), getSubmitQueueName())) {
			for (U u : submitted)
				submitter.submit(u);
		}

		return true; // It was reordered
	}

	@Override
	public boolean remove(U bean) throws EventException {
		return doWhilePaused(() -> doRemove(bean, getSubmitQueueName()));
	}

	@Override
	public void removeCompleted(U bean) throws EventException {
		doRemove(bean, getStatusSetName());
	}

	private boolean doRemove(U beanToRemove, String queueName) throws EventException {
		QueueConnection connection = null;
		QueueSession session = null;
		final QueueConnectionFactory connectionFactory = (QueueConnectionFactory) service.createConnectionFactory(uri);
		try {
			connection = connectionFactory.createQueueConnection(); // This times out when the server is not there.
			session = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
			final Queue queue = session.createQueue(queueName);
			connection.start();

			final QueueBrowser queueBrowser = session.createBrowser(queue);
			String jmsMessageId = null;
			@SuppressWarnings("rawtypes")
			final Enumeration e = queueBrowser.getEnumeration();

			while (jmsMessageId == null && e.hasMoreElements()) {
				Message message = (Message) e.nextElement();
				if (message instanceof TextMessage) {
					TextMessage textMsg = (TextMessage) message;

					final U beanFromQueue = service.unmarshal(textMsg.getText(), null);
					if (beanFromQueue != null && isForSameObject(beanFromQueue, beanToRemove)) {
						jmsMessageId = textMsg.getJMSMessageID();
					}
				}
			}

			queueBrowser.close();

			if (jmsMessageId != null) {
				MessageConsumer consumer = session.createConsumer(queue, "JMSMessageID = '" + jmsMessageId + "'");
				Message message = consumer.receive(1000);
				consumer.close();
				if (message == null) {
					// It might have been removed ok
					LOGGER.warn("Could not remove bean (JMSMessageID={}) from queue {}. Bean: {}", jmsMessageId, queueName, beanToRemove);
					return false;
				} else {
					LOGGER.info("Removed bean (JMSMessageId={} from queue", beanToRemove);
					return true;
				}
			} else {
				LOGGER.warn("Could not find bean to remove in queue {}. Bean: {}", queueName, beanToRemove);
				return false;
			}
		} catch (Exception ne) {
			throw new EventException("Cannot remove item " + beanToRemove, ne);

		} finally {
			try {
				if (connection != null)
					connection.close();
				if (session != null)
					session.close();
			} catch (Exception e) {
				throw new EventException("Cannot close connection as expected!", e);
			}
		}
	}

	@Override
	public boolean replace(U bean) throws EventException {
		return doWhilePaused(() -> doReplace(bean));
	}

	private boolean doReplace(U bean) throws EventException {
		// We are paused, read the queue
		List<U> submitted = getSubmissionQueue();
		if (submitted == null || submitted.isEmpty())
			throw new EventException("There is nothing submitted waiting to be run\n\nPerhaps the job started to run.");

		boolean found = false;
		for (int i = 0; i < submitted.size(); i++) {
			U u = submitted.get(i);
			if (u.getUniqueId().equals(bean.getUniqueId())) {
				found = true;
				submitted.set(i, bean);
				break;
			}
		}
		if (!found)
			throw new EventException("Cannot find bean '" + bean.getName() + "' in submission queue!\nIt might be running now.");

		clearQueue();

		try (ISubmitter<U> submitter = eventService.createSubmitter(getUri(), getSubmitQueueName())) {
			for (U u : submitted) {
				submitter.submit(u);
			}
		}
		return true; // It was reordered
	}



	@Override
	public void setRunner(IProcessCreator<U> runner) throws EventException {
		if (isActive()) throw new IllegalStateException("Cannot set runner while the consumer is active");
		this.runner = runner;
	}

	@Override
	public synchronized void start() throws EventException {
		if (isActive()) throw new IllegalStateException("Consumer for queue " + getSubmitQueueName() + " is already running");

		latchStart = new CountDownLatch(1);
		final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
			Thread thread = Executors.defaultThreadFactory().newThread(r);
			thread.setName("Consumer Thread " + getName());
			thread.setDaemon(true);
			thread.setPriority(Thread.NORM_PRIORITY - 1);
			return thread;
		});

		executor.submit(this::runAndStopIfError);
	}

	/**
	 * Awaits the start of the consumer
	 * @throws InterruptedException
	 * @throws Exception
	 */
	@Override
	public void awaitStart() throws InterruptedException {
		if (latchStart!=null) latchStart.await();
	}

	@Override
	public synchronized void stop() throws EventException {
		if (!isActive()) throw new IllegalStateException("Consumer for queue " + getSubmitQueueName() + " is not running");

		setActive(false); // Stops consume loop

		processManager.shutdown();
		consumerThreadRunning = false;

		notifyStatusChanged();
	}

	/**
	 * The process manager is a listener for bean changes that handles
	 * the status of a bean being set to a request state, e.g. {@link Status#REQUEST_PAUSE}
	 * and performs the appropriate action
	 */
	protected class ProcessManager {

		private volatile Map<String, WeakReference<IConsumerProcess<U>>> processMap = new ConcurrentHashMap<>();

		// TODO make this a constant
		private final Map<Command, Status> commandToStatusMap;

		ProcessManager() {
			commandToStatusMap = new HashMap<>();
			commandToStatusMap.put(Command.PAUSE_JOB, Status.REQUEST_PAUSE);
			commandToStatusMap.put(Command.RESUME_JOB, Status.REQUEST_RESUME);
			commandToStatusMap.put(Command.TERMINATE_JOB, Status.REQUEST_TERMINATE);
		}

		public boolean hasRunProcess(U bean) {
			// returns whether there is/was a process for this bean. Note: it may have been garbage collected
			return processMap.containsKey(bean.getUniqueId());
		}

		public void registerProcess(U bean, IConsumerProcess<U> process) {
			processMap.put(bean.getUniqueId(), new WeakReference<IConsumerProcess<U>>(process));
		}

		public void processJobCommand(QueueCommandBean commandBean) throws EventException {
			processJobCommand(commandBean.getBeanUniqueId(), commandBean.getCommand());
		}

		public void processJobCommand(U bean, Command command) throws EventException {
			processJobCommand(bean.getUniqueId(), command);
		}

		private void processJobCommand(String uniqueId, Command command) throws EventException {
			final Optional<U> optBean = findBean(uniqueId);

			if (optBean.isPresent()) {
				updateStatusAndPublish(optBean.get(), command);
				final WeakReference<IConsumerProcess<U>> ref = processMap.get(uniqueId);
				if (ref != null) {
					manageProcess(ref.get(), command);
				} else {
					updateQueue(optBean.get());
				}
			}
		}

		private Optional<U> findBean(String uniqueId) throws EventException {
			final WeakReference<IConsumerProcess<U>> ref = processMap.get(uniqueId);
			if (ref == null) {
				return findBeanWithId(getSubmissionQueue(), uniqueId);
			} else if (ref.get() != null) {
				return Optional.ofNullable(ref.get().getBean());
			}
			return Optional.empty();
		}

		private void manageProcess(IConsumerProcess<U> process, Command command) throws EventException {
			switch (command) {
				case PAUSE_JOB:
					process.pause();
					break;
				case RESUME_JOB:
					process.resume();
					break;
				case TERMINATE_JOB:
					process.terminate();
					break;
				default:
					throw new IllegalArgumentException("Not a process command: " + command);
			}
		}

		private void updateStatusAndPublish(U bean, Command command) throws EventException {
			// TODO: does it work ok without setting the request status?
			Status requestStatus = commandToStatusMap.get(command);
			if (requestStatus == null) throw new IllegalArgumentException("Not a process command " + command);
			bean.setStatus(requestStatus);
			statusTopicPublisher.broadcast(bean);
		}

		public void shutdown() {
			@SuppressWarnings("unchecked")
			final WeakReference<IConsumerProcess<U>>[] wra = processMap.values()
					.toArray(new WeakReference[processMap.size()]);
			for (WeakReference<IConsumerProcess<U>> wr : wra) {
				IConsumerProcess<U> process = wr.get();
				if (process != null) {
					try {
						process.terminate();
					} catch (Exception e) {
						LOGGER.error("Could not terminate process for bean {}", process.getBean());
					}
				}
			}
			processMap.clear();
		}

	}

	/**
	 * Calls the {@link #run()} method. If that method throws an exception
	 * then calls {@link #stop()}. This can only happen in {@link #processException(Exception)}
	 * rethrows an exception.
	 */
	protected final void runAndStopIfError() {
		try {
			run();
		} catch (Exception e) {
			// only exceptions rethrown by processException are caught here
			LOGGER.error("Internal error running consumer {}", getName(), e);
			try {
				ConsumerImpl.this.stop();
			} catch (EventException e1) {
				LOGGER.error("Cannot stop consumer", e1);
			}
		}
	}

	/**
	 * The main consume method for a consumer. When the consumer is running, one thread will be running this
	 * method (typically a thread created by calling the {@link #start()} method, while the consumer is
	 * controlled by calling methods such as {@link #pause()} or {@link #resume()} in other threads.
	 */
	@Override
	public void run() throws EventException {
		if (isActive()) throw new IllegalStateException("Consumer is already running");

		init();

		// run the main event loop until setActive is false
		try {
			while (isActive()) {
				consumeNextMessage();
			}
		} catch (Exception e) {
			LOGGER.warn("Consumer {} exiting run() method with exception", getName(), e);
			throw e;
		}
		LOGGER.info("Consumer for queue {} exiting run() method normally", getName());
	}

	/**
	 * This method performs all the tasks required in one iteration of the main loop of the {@link #run()} method, namely:<ol>
	 * <li>Checks if the consumer should pause, and if so does until it is notified to resume (see {@link #checkPaused()};</li>
	 * <li>Consumes the next message (see {@link #consume()});</li>
	 * <li>Handles any exception thrown by consuming the message, see {@link #processException(Exception)}.</li>
	 * </ol>
	 * @throws EventException
	 */
	private void consumeNextMessage() throws EventException {
		try {
			checkPaused(); // blocks until not paused.
			if (isActive()) { // isActive could have been set to false while we were paused
				consume();
			}
		} catch (Exception e) {
			// if processException returns false, break out of the loop to exit the consumer
			setActive(processException(e));
		}
	}

	private void init() throws EventException {
		LOGGER.debug("Initializing consumer for queue {}", getSubmitQueueName());
		this.waitTime = 0;

		if (runner == null) {
			throw new IllegalStateException("Cannot start a consumer without a runner to run things!");
		}

		setActive(true);

		// We should pause if there are things in the queue
		// This is because on a server restart the user will
		// need to choose the visit again and get the baton.
		// NOTE: Not all consumers check the submit queue and
		// pause before they start.
		checkStartPaused();

		// We're now fully initialized and about to start running the main loop that consumes and runs beans,
		// so set the consumerThreadRunning flag to true and notify any threads that called awaitStart()
		consumerThreadRunning = true;
		notifyStatusChanged();
		if (latchStart!=null) latchStart.countDown();
	}

	private void consume() throws Exception {
		// Consumes messages from the queue.
		Message m = getMessage(uri, getSubmitQueueName());
		if (m != null) {
			waitTime = 0; // We got a message

			// TODO FIXME Check if we have the max number of processes
			// exceeded and wait until we don't...
			TextMessage t = (TextMessage) m;

			final String json = t.getText();
			final U bean = service.unmarshal(json, getBeanClass());
			executeBean(bean);
		}
	}

	/**
	 * Processes an exception that occurred during the {@link #run()} loop.
	 * @param e exception
	 * @return <code>true</code> to keep processing messages, <code>false</code> to exit
	 * @throws EventException
	 */
	private boolean processException(Exception e) throws EventException {
		LOGGER.debug("Processing exception in consumer", e);

		// if error occurred deserializing the bean, log a specific message and don't fail even if not durable
		if (e.getClass().getSimpleName().contains("Json") || e.getClass().getSimpleName().endsWith("UnrecognizedPropertyException")) {
			LOGGER.error("Could not deserialize bean.", e);
			return true;
		}

		// normal error case, log error message and continue if
		if (e instanceof EventException || e instanceof InterruptedException) {
			if (Thread.interrupted()) {
				LOGGER.error("Consumer was interrupted consuming a message", e);
				return false;
			} else {
				LOGGER.error("Cannot consume message ", e);
				return true;
			}
		}

		// TODO: below assumes some connection problem with activemq. why would any remaining
		// exception be a connection problem?
		LOGGER.warn("{} ActiveMQ connection to {} lost.", getName(), uri, e);
		LOGGER.warn("We will check every 2 seconds for 24 hours, until it comes back.");
		final long retryInterval = EventTimingsHelper.getConnectionRetryInterval();
		try {
			if (Thread.interrupted()) return false;
			// Wait for 2 seconds (default time)
			Thread.sleep(retryInterval);
		} catch (InterruptedException ie) {
			throw new EventException("The consumer was unable to wait!", ie);
		}

		waitTime += retryInterval;

		// Exits if wait time more than one day
		if (waitTime > ONE_DAY) {
			LOGGER.warn("ActiveMQ permanently lost. {} will now exit", getName());
			disconnect();
			return false;
		}

		LOGGER.error("Cannot consume message ", e);
		return true;
	}

	/**
	 * Called by {@link #init()} when the consumer starts. If {@link #isPauseOnStart()} is set,
	 * and the queue is not empty, we set the {@link #awaitPaused} flag and publish a
	 * a PauseBean to the command topic.
	 * @throws EventException
	 */
	private void checkStartPaused() throws EventException {
		if (!isPauseOnStart()) {
			return;
		}

		List<U> items = getSubmissionQueue();
		if (items != null && !items.isEmpty()) {
			LOGGER.debug("Pausing consumer {} on start ", getName());
			pause(); // note, sets the awaitPause flag, this thread continues

			try (IPublisher<QueueCommandBean> publisher = eventService.createPublisher(getUri(), getCommandTopicName())) {
				QueueCommandBean pauseBean = new QueueCommandBean(getSubmitQueueName(), Command.PAUSE_QUEUE);
				publisher.broadcast(pauseBean);
			}
		}
	}

	/**
	 * Checks if the {@link #awaitPaused} flag is set, and if so waits until it is cleared.
	 * @throws Exception
	 */
	private void checkPaused() throws Exception {
		if (!isActive())
			throw new EventException("The consumer is not active and cannot be paused!");

		// Check the locking using a condition
		if (!consumerStateChangeLock.tryLock(1, TimeUnit.SECONDS)) {
			throw new EventException("Internal Error - Could not obtain lock to run device!");
		}
		try {
			if (isActive() && awaitPaused) {
				setActive(false);
				LOGGER.info("Consumer pausing {}", getName());
				while (awaitPaused) {
					shouldResumeCondition.await(); // Until unpaused
				}
				LOGGER.info("Consumer resuming {}", getName());
				setActive(true);
			}
		} finally {
			consumerStateChangeLock.unlock();
		}
	}

	@Override
	public boolean isPaused() {
		return awaitPaused;
	}

	/**
	 * Performs a task while the queue is paused.
	 * @param task task to perform
	 * @return the value returned by the given task
	 * @throws EventException wrapping any exception thrown by the given task
	 */
	protected <T> T doWhilePaused(Callable<T> task) throws EventException {
		final boolean requiresPause = !isPaused();

		try {
			if (requiresPause) {
				pause();
			}

			return task.call();
		} catch (EventException e) {
			throw e;
		} catch (Exception e) {
			throw new EventException(e);
		} finally {
			if (requiresPause) {
				resume();
			}
		}
	}

	@Override
	public void pause() throws EventException {
		awaitPaused = true;
		closeMessageConsumer(); // required so that remove(), reorder, etc. work as expected. See DAQ-1453.
		LOGGER.info("Consumer signalled to pause {}", getName());
		notifyStatusChanged();
	}

	private void closeMessageConsumer() throws EventException {
		try {
			if (messageConsumer != null) {
				messageConsumer.close();
				messageConsumer = null;
			}
		} catch (JMSException e) {
			throw new EventException("Could not close JMS consumer " + getName(), e);
		}
	}

	@Override
	public void resume() throws EventException {
		// No need to do anything if we're not paused or awaiting pause
		if (!awaitPaused) return;
		try {
			consumerStateChangeLock.lockInterruptibly();

			awaitPaused = false;
			shouldResumeCondition.signalAll();
			LOGGER.info("Consumer signalled to resume {}", getName());
			notifyStatusChanged();
		} catch (Exception ne) {
			throw new EventException(ne);
		} finally {
			consumerStateChangeLock.unlock();
		}
	}

	public interface BeanAction<T> {

		Object performAction(T bean) throws EventException;

	}

	protected Object findBeanAndPerformAction(String beanUniqueId, BeanAction<U> action) throws EventException {
		final Optional<U> optBean = findBeanWithId(getSubmissionQueue(), beanUniqueId);
		if (optBean.isPresent()) {
			return action.performAction(optBean.get());
		} else {
			LOGGER.error("Cannot find bean with id ''{}'' in submission queue!\nIt might be running now.", beanUniqueId);
			return null;
		}
	}

	protected void removeCompleted(String beanUniqueId) throws EventException {
		final Optional<U> optBean = findBeanWithId(getRunningAndCompleted(), beanUniqueId);
		if (optBean.isPresent()) {
			removeCompleted(optBean.get());
		} else {
			LOGGER.error("Cannot find bean with id ''{}'' in status set");
		}
	}

	private Optional<U> findBeanWithId(List<U> beans, String beanUniqueId) {
		return beans.stream().filter(bean -> bean.getUniqueId().equals(beanUniqueId)).findFirst();
	}

	@Override
	public ConsumerStatus getConsumerStatus() {
		if (!consumerThreadRunning || !isConnected()) return ConsumerStatus.STOPPED;
		return awaitPaused ? ConsumerStatus.PAUSED : ConsumerStatus.RUNNING;
	}

	private void executeBean(U bean) throws EventException, InterruptedException {
		final Instant startTime = Instant.now();
		// If the bean changed after being submitted to the submission queue, and that change was published to the
		// status topic, the bean we've consumed from the submission queue will be out of date, but the override map will
		// have the updated bean, so use that instead
		if (beanOverrideMap != null && beanOverrideMap.containsKey(bean.getUniqueId())) {
			U o = beanOverrideMap.remove(bean.getUniqueId());
			bean.setStatus(o.getStatus());
		}

		// We record the bean in the status queue
		LOGGER.trace("Moving {} to {}", bean, getSubmitQueueName());
		statusSetSubmitter.submit(bean);

		Instant timeNow = Instant.now();
		if (Duration.between(startTime, timeNow).toMillis() > 100) { LOGGER.warn("executeBean() took {}ms to statusSetSubmitter.submit complete", Duration.between(startTime, timeNow).toMillis()); }

		if (processManager.hasRunProcess(bean)) {
			throw new EventException("The bean with unique id '"+bean.getUniqueId()+"' has already been used. Cannot run the same uuid twice!");
		}

		// If terminate has been requested before the bean is run, don't run it
		// instead set state to TERMINATED and publish to status topic
		if (bean.getStatus()==Status.REQUEST_TERMINATE) {
			bean.setStatus(Status.TERMINATED);
			bean.setMessage("Run aborted before started");
			statusTopicPublisher.broadcast(bean);

			timeNow = Instant.now();
			if (Duration.between(startTime, timeNow).toMillis() > 100) { LOGGER.warn("executeBean() took {}ms to statusTopicPublisher.broadcast complete", Duration.between(startTime, timeNow).toMillis()); }

			return;
		}

		if (bean.getStatus().isFinal()) {
			LOGGER.warn("Bean status is already final, it will not be run");
			return; // Sanity check, the bean status should not be final
		}

		try {
			IConsumerProcess<U> process = runner.createProcess(bean, statusTopicPublisher);

			timeNow = Instant.now();
			if (Duration.between(startTime, timeNow).toMillis() > 100) { LOGGER.warn("executeBean() took {}ms to runner.createProcess complete", Duration.between(startTime, timeNow).toMillis()); }

			processManager.registerProcess(bean, process);

			timeNow = Instant.now();
			if (Duration.between(startTime, timeNow).toMillis() > 100) { LOGGER.warn("executeBean() took {}ms to processMap.put complete", Duration.between(startTime, timeNow).toMillis()); }

			LOGGER.info("Starting process for bean {}", bean);
			process.start(); // Depending on the process may run in a separate thread (default is not to)
		} catch (Exception e) {
			// if an exception is thrown, set the bean status to failed. Note the exception is logged in processException()
			bean.setStatus(Status.FAILED);
			bean.setMessage(e.getMessage());
			statusTopicPublisher.broadcast(bean);
			throw e;
		}
	}


	private Message getMessage(URI uri, String submitQName) throws JMSException {
		if (this.messageConsumer == null) {
			this.messageConsumer = createMessageConsumer(uri, submitQName);
		}
		return messageConsumer.receive(EventTimingsHelper.getReceiveTimeout());
	}

	private MessageConsumer createMessageConsumer(URI uri, String submitQName) throws JMSException {

		Session session = getSession(); // TODO: should call getQueueSession after createQueue to get this?
		Queue queue = createQueue(submitQName);

		final MessageConsumer consumer = session.createConsumer(queue);

		LOGGER.info("Consumer for queue {} successfully created connection to ActiveMQ using uri {}.", getName(), uri);

		return consumer;
	}

	@Override
	public void pauseJob(U bean) throws EventException {
		processManager.processJobCommand(bean, Command.PAUSE_JOB);
	}

	@Override
	public void resumeJob(U bean) throws EventException {
		processManager.processJobCommand(bean, Command.RESUME_JOB);
	}

	@Override
	public void terminateJob(U bean) throws EventException {
		processManager.processJobCommand(bean, Command.TERMINATE_JOB);
	}

	@Override
	public IProcessCreator<U> getRunner() {
		return runner;
	}

	@Override
	public UUID getConsumerId() {
		return consumerId;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public boolean isActive() {
		return active;
	}

	/**
	 * Sets the active flag. If this is called with <code>false</code> the main
	 * event loop will exit if running. This method has been made private,
	 * client code should call {@link #stop()} instead.
	 * @param active
	 */
	private void setActive(boolean active) {
		this.active = active;
	}

	@Override
	public boolean isPauseOnStart() {
		return pauseOnStart;
	}

	@Override
	public void setPauseOnStart(boolean pauseOnStart) {
		this.pauseOnStart = pauseOnStart;
	}

	@Override
	public String getConsumerStatusTopicName() {
		return consumerStatusTopicName;
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
	public Class<U> getBeanClass() {
		return beanClass;
	}

	@Override
	public void setBeanClass(Class<U> beanClass) {
		this.beanClass = beanClass;
	}

	/**
	 * Class responsible for updating and broadcasting {@code ConsumerStatusBean}s. Essentially a wrapper for an {@code IPublisher<ConsumerStatusBean>}
	 */
	private class ConsumerStatusPublisher {

		private final IPublisher<ConsumerStatusBean> publisher;

		private volatile ConsumerStatusBean lastBean;

		public ConsumerStatusPublisher(URI uri) {
			publisher = eventService.createPublisher(uri, consumerStatusTopicName);
			lastBean = createConsumerStatusBean();
		}

		private ConsumerStatusBean createConsumerStatusBean() {
			ConsumerStatusBean bean = new ConsumerStatusBean();
			bean.setConsumerId(getConsumerId());
			bean.setConsumerName(getName());
			bean.setConsumerStatus(getConsumerStatus());
			bean.setQueueName(getSubmitQueueName());
			bean.setBeamline(System.getenv("BEAMLINE"));

			try {
				bean.setHostName(InetAddress.getLocalHost().getHostName());
			} catch (UnknownHostException e) {
				LOGGER.warn("Could not resolve local host name", e);
			}

			return bean;
		}

		public void publishConsumerStatusBean() {
			lastBean.setPublishTime(System.currentTimeMillis());
			lastBean.setConsumerStatus(getConsumerStatus());
			try {
				publisher.broadcast(lastBean);
			} catch (EventException e) {
				LOGGER.error("Could not publish consumer status bean", e);
			}
		}

		public void disconnect() throws EventException {
			publisher.disconnect();
		}
	}

}
