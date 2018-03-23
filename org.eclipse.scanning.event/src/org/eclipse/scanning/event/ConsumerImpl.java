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
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.QueueConnectionFactory;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.eclipse.scanning.api.event.EventConstants;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventConnectorService;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.alive.ConsumerCommandBean;
import org.eclipse.scanning.api.event.alive.ConsumerStatus;
import org.eclipse.scanning.api.event.alive.HeartbeatBean;
import org.eclipse.scanning.api.event.alive.KillBean;
import org.eclipse.scanning.api.event.alive.PauseBean;
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

final class ConsumerImpl<U extends StatusBean> extends AbstractQueueConnection<U> implements IConsumer<U> {

	private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerImpl.class);
	private static final long ONE_DAY = TimeUnit.DAYS.toMillis(1);

	private String name;
	private UUID consumerId;
	private IPublisher<U> statusTopicPublisher; // a publisher to the status topic
	private HeartbeatBroadcaster heartbeatBroadcaster;
	private ISubscriber<IBeanListener<U>> statusTopicSubscriber; // a subscriber to the status topic
	private ISubscriber<IBeanListener<ConsumerCommandBean>> commandTopicSubscriber; // a subscriber to the command topic
	private ISubmitter<U> statusSetSubmitter; // a submitter to the status set

	private boolean pauseOnStart = false;
	private CountDownLatch latchStart;
	private int waitTime = 0;

	private IProcessCreator<U> runner;
	private boolean durable;
	private MessageConsumer messageConsumer;

	private volatile boolean active = false;
	private volatile Map<String, WeakReference<IConsumerProcess<U>>> processMap;
	private Map<String, U> beanOverrideMap;

	/**
	 * Concurrency design recommended by Keith Ralphs after investigating
	 * how to pause and resume a collection cycle using Reentrant locks.
	 * Design requires these three fields.
	 */
	private ReentrantLock consumerStateChangeLock;
	private Condition shouldResumeCondition;
	private volatile boolean awaitPaused;
	private final String heartbeatTopicName;

	ConsumerImpl(URI uri, String submitQueueName, String statusQueueName, String statusTopicName,
			String heartbeatTopicName, String commandTopicName, IEventConnectorService service,
			IEventService eservice)
			throws EventException {
		super(uri, submitQueueName, statusQueueName, statusTopicName, commandTopicName, service, eservice);
		this.consumerStateChangeLock = new ReentrantLock();
		this.shouldResumeCondition = consumerStateChangeLock.newCondition();

		durable = true;
		consumerId = UUID.randomUUID();
		name = "Consumer " + consumerId; // This will hopefully be changed to something meaningful...
		this.processMap = Collections.synchronizedMap(new HashMap<>());
		this.heartbeatTopicName = heartbeatTopicName;
		connect();
	}

	private void connect() throws EventException {
		statusSetSubmitter  = eservice.createSubmitter(uri, getStatusSetName());
		statusTopicPublisher = eservice.createPublisher(uri, getStatusTopicName());
		statusTopicPublisher.setStatusSetName(getStatusSetName()); // We also update values in a queue.

		if (heartbeatTopicName!=null) {
			heartbeatBroadcaster = new HeartbeatBroadcaster(uri, heartbeatTopicName, this);
		}

		if (getCommandTopicName()!=null) {
			commandTopicSubscriber = eservice.createSubscriber(uri, getCommandTopicName());
			commandTopicSubscriber.addListener(new CommandListener());
		}
	}

	@Override
	public void disconnect() throws EventException {
		if (isActive()) stop();

		statusSetSubmitter.disconnect();
		statusTopicPublisher.disconnect();
		if (heartbeatBroadcaster!=null) heartbeatBroadcaster.disconnect();
		if (commandTopicSubscriber!=null) commandTopicSubscriber.disconnect();
		if (beanOverrideMap!=null) beanOverrideMap.clear();

		super.disconnect();
	}

	protected class CommandListener implements IBeanListener<ConsumerCommandBean> {
		@Override
		public void beanChangePerformed(BeanEvent<ConsumerCommandBean> evt) {
			ConsumerCommandBean bean = evt.getBean();
			if (isCommandForMe(bean)) {
				if (bean instanceof KillBean) terminate((KillBean)bean);
				if (bean instanceof PauseBean) processPause((PauseBean)bean);
			}
		}

		protected boolean isCommandForMe(ConsumerCommandBean bean) {
			return bean.getConsumerId()!=null && bean.getConsumerId().equals(getConsumerId())
					|| bean.getQueueName()!=null && bean.getQueueName().equals(getSubmitQueueName());
		}

	}

	protected void processPause(PauseBean bean) {
		try {
			if (bean.isPause()) {
				pause();
			} else {
				resume();
			}

		} catch (Exception ne) {
			LOGGER.error("Unable to process pause command on consumer '{}'. Consumer will stop.", getName(), ne);
			try {
				stop();
				disconnect();
			} catch (EventException e) {
				LOGGER.error("An internal error occurred trying to terminate the consumer "+getName()+" "+getConsumerId());
			}
		}
	}

	protected void terminate(KillBean kbean) {
		try {
			stop();
			if (kbean.isDisconnect()) disconnect();
		} catch (EventException e) {
			LOGGER.error("An internal error occurred trying to terminate the consumer "+getName()+" "+getConsumerId());
		}
		if (kbean.isExitProcess()) {
			try {
				Thread.sleep(2500);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				LOGGER.error("Unable to pause before exit", e);
			}
			exit();
		}
		if (kbean.isRestart()) {
			try {
				if (kbean.isDisconnect()) {
					connect();
				}
				start();
			} catch (EventException e) {
				LOGGER.error("Unable to restart, please contact your support representative.", e);
			}
		}
	}

	public void exit() {
		// TODO: We should almost certainly not be killing the VM. We need to investigate
		// further why we would want to do this and what we should do instead.
		// - when called from processException() maybe just return false
		// - when called from terminate? possibly just set active to false
		System.exit(0); // Normal orderly exit
	}

	/**
	 * Updates the given bean on the queue to have the same status as the one given.
	 * This is done by using a {@link QueueBrowser} to
	 * enumerate all the beans on the queue.
	 * @param bean
	 * @throws EventException
	 */
	protected void updateQueue(U bean) throws EventException {
		boolean resumeAfter = !awaitPaused;
		Session session = null;
		QueueBrowser queueBrowser = null;
		try {
			pause();

			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
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
				Message rem = consumer.receive(Constants.getReceiveFrequency());

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
		} catch (Exception ne) {
			throw new EventException("Cannot reorder queue!", ne);
		} finally {
			// Only resume if it wasn't in a paused state before this update
			if (resumeAfter) {
				resume();
			}
			try {
				if (queueBrowser != null) queueBrowser.close();
				if (session != null) session.close();
			} catch (JMSException e) {
				throw new EventException("Cannot close session!", e);
			}
		}
	}

	private void addBeanOverride(U bean) {
		if (beanOverrideMap == null) {
			beanOverrideMap = Collections.synchronizedMap(new HashMap<>());
		}
		beanOverrideMap.put(bean.getUniqueId(), bean);
	}

	@Override
	public List<U> getSubmissionQueue() throws EventException {
		return getQueue(getSubmitQueueName());
	}

	@Override
	public List<U> getStatusSet() throws EventException {
		final List<U> statusSet = getQueue(getStatusSetName());
		statusSet.sort((first, second) -> Long.signum(second.getSubmissionTime() - first.getSubmissionTime()));
		return statusSet;
	}

	@Override
	public void setRunner(IProcessCreator<U> runner) throws EventException {
		if (isActive()) throw new IllegalStateException("Cannot set runner while the consumer is active");
		this.runner = runner;
	}

	@Override
	public void start() throws EventException {
		latchStart = new CountDownLatch(1);
		final Thread consumerThread = new Thread("Consumer Thread "+getName()) {
			@Override
			public void run() {
				try {
					ConsumerImpl.this.run();
				} catch (Exception ne) {
					LOGGER.trace("Internal error running consumer "+getName(), ne);
					try {
						ConsumerImpl.this.stop();
					} catch (EventException e) {
						LOGGER.error("Cannot complete stop", ne);
					}
				}
			}
		};
		consumerThread.setDaemon(true);
		consumerThread.setPriority(Thread.NORM_PRIORITY-1);
		consumerThread.start();
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

	private void startProcessManager() throws EventException {
		if (statusTopicSubscriber!=null) statusTopicSubscriber.disconnect();
		statusTopicSubscriber = eservice.createSubscriber(uri, getStatusTopicName());
		statusTopicSubscriber.addListener(new ProcessManager());
	}

	/**
	 * The process manager is a listener for bean changes that handles
	 * the status of a bean being set to a request state, e.g. {@link Status#REQUEST_PAUSE}
	 * and performs the appropriate action
	 */
	protected class ProcessManager implements IBeanListener<U> {
		@Override
		public void beanChangePerformed(BeanEvent<U> evt) {
			U bean = evt.getBean();
			if (!bean.getStatus().isRequest()) return;

			WeakReference<IConsumerProcess<U>> ref = processMap.get(bean.getUniqueId());
			try {
				if (ref==null) { // Might be in submit queue still
					updateQueue(bean);
				} else {
					IConsumerProcess<U> process = ref.get();
					if (process!=null) {
						manageProcess(process, bean);
					}
				}
			} catch (EventException ne) {
				LOGGER.error("Internal error, please contact your support representative.", ne);
			}
		}

		private void manageProcess(IConsumerProcess<U> process, U bean) throws EventException {
			process.getBean().setStatus(bean.getStatus());
			process.getBean().setMessage(bean.getMessage());
			if (bean.getStatus()==Status.REQUEST_TERMINATE) {
				processMap.remove(bean.getUniqueId());
				if (process.isPaused()) process.resume();
				process.terminate();
			} else if (bean.getStatus()==Status.REQUEST_PAUSE) {
				process.pause();
			} else if (bean.getStatus()==Status.REQUEST_RESUME) {
				process.resume();
			}
		}
	}

	@Override
	public void stop() throws EventException {
		try {
			heartbeatBroadcaster.stop();
			setActive(false); // Stops event loop

			@SuppressWarnings("unchecked")
			final WeakReference<IConsumerProcess<U>>[] wra = processMap.values()
					.toArray(new WeakReference[processMap.size()]);
			for (WeakReference<IConsumerProcess<U>> wr : wra) {
				if (wr.get() != null) {
					terminateProcess(wr.get());
				}
			}
		} finally {
			processMap.clear();
		}
	}

	private void terminateProcess(IConsumerProcess<U> process) throws EventException {
		// TODO More to terminate than call terminate?
		// Relies on process always setting state correctly to TERMINATED.
		if (process!=null) process.terminate();
	}

	@Override
	public void run() throws EventException {
		init();

		// run the main event loop until setActive is false
		while (isActive()) {
			try {
				checkPaused(); // blocks until not paused.
				if (isActive()) { // Might have paused for a long time.
					consume();
				}
			} catch (Exception e) {
				// if processException returns false, break out of the loop to exit the consumer
				setActive(processException(e));
			}
		}
	}

	private void init() throws EventException {
		this.waitTime = 0;

		if (runner!=null) {
			heartbeatBroadcaster.start();
		} else {
			throw new IllegalStateException("Cannot start a consumer without a runner to run things!");
		}

		// We process the paused state
		PauseBean pbean = getPauseBean(getSubmitQueueName());
		if (pbean!=null) processPause(pbean); // Might set the pause lock and block on checkPaused().

		startProcessManager();
		setActive(true);

		// We should pause if there are things in the queue
		// This is because on a server restart the user will
		// need to choose the visit again and get the baton.
		// NOTE: Not all consumers check the submit queue and
		// pause before they start.
		checkStartPaused();

		// It is possible to call start() and then awaitStart().
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
				return isDurable();
			}
		}

		if (!isDurable()) return false;

		// TODO: below assumes some connection problem with activemq. why would any remaining
		// exception be a connection problem?
		LOGGER.warn("{} ActiveMQ connection to {} lost.", getName(), uri, e);
		LOGGER.warn("We will check every 2 seconds for 24 hours, until it comes back.");
		try {
			if (Thread.interrupted()) return false;
			// Wait for 2 seconds (default time)
			Thread.sleep(Constants.getNotificationFrequency());
		} catch (InterruptedException ie) {
			throw new EventException("The consumer was unable to wait!", ie);
		}

		waitTime += Constants.getNotificationFrequency();
		checkTime(waitTime); // Exits if wait time more than one day

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
			pause(); // note, sets the awaitPause flag, this thread continues

			IPublisher<PauseBean> pauser = eservice.createPublisher(getUri(), getCommandTopicName());
			pauser.setStatusSetName(EventConstants.CMD_SET); // The set that other clients may check
			pauser.setStatusSetAddRequired(true);

			PauseBean pbean = new PauseBean();
			pbean.setQueueName(getSubmitQueueName()); // The queue we are pausing
			pbean.setPause(true);
			pauser.broadcast(pbean);
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
				LOGGER.info("Pausing consumer {}", getSubmitQueueName());
				while (awaitPaused) {
					shouldResumeCondition.await(); // Until unpaused
				}
				LOGGER.info("Resuming consumer {}", getSubmitQueueName());
				setActive(true);
			}
		} finally {
			consumerStateChangeLock.unlock();
		}
	}

	@Override
	public void pause() throws EventException {
		if (!isActive()) return; // Nothing to pause
		try {
			consumerStateChangeLock.lockInterruptibly();
		} catch (Exception ne) {
			throw new EventException(ne);
		}

		try {
			awaitPaused = true;
			if (messageConsumer!=null) messageConsumer.close();
			messageConsumer = null; // Force unpaused consumers to make a new connection.
			LOGGER.info("{} is paused", getName());
		} catch (Exception ne) {
			throw new EventException(ne);
		} finally {
			consumerStateChangeLock.unlock();
		}
	}

	@Override
	public void resume() throws EventException {
		if (isActive()) return;
		try {
			consumerStateChangeLock.lockInterruptibly();
		} catch (Exception ne) {
			throw new EventException(ne);
		}

		try {
			awaitPaused = false;
			// We don't have to actually start anything again because the getMessage(...) call reconnects automatically.
			shouldResumeCondition.signalAll();
			LOGGER.info("{} is running", getName());
		} finally {
			consumerStateChangeLock.unlock();
		}
	}

	@Override
	public ConsumerStatus getConsumerStatus() {
		return awaitPaused ? ConsumerStatus.PAUSED : ConsumerStatus.RUNNING;
	}

	private void executeBean(U bean) throws EventException, InterruptedException {
		// If the bean changed after being submitted to the submission queue, and that change was published to the
		// status topic, the bean we've consumed from the submission queue will be out of date, but the override map will
		// have the updated bean, so use that instead
		if (beanOverrideMap != null && beanOverrideMap.containsKey(bean.getUniqueId())) {
			U o = beanOverrideMap.remove(bean.getUniqueId());
			bean.setStatus(o.getStatus());
		}

		// We record the bean in the status queue
		LOGGER.trace("Moving {} to {}", bean, statusSetSubmitter.getSubmitQueueName());
		statusSetSubmitter.submit(bean);

		if (processMap.containsKey(bean.getUniqueId())) {
			throw new EventException("The bean with unique id '"+bean.getUniqueId()+"' has already been used. Cannot run the same uuid twice!");
		}

		// If terminate has been requested before the bean is run, don't run it
		// instead set state to TERMINATED and publish to status topic
		if (bean.getStatus()==Status.REQUEST_TERMINATE) {
			bean.setStatus(Status.TERMINATED);
			bean.setMessage("Run aborted before started");
			statusTopicPublisher.broadcast(bean);
			return;
		}

		if (bean.getStatus().isFinal()) {
			LOGGER.warn("Bean status is already final, it will not be run");
			return; // Sanity check, the bean status should not be final
		}

		try {
			IConsumerProcess<U> process = runner.createProcess(bean, statusTopicPublisher);
			processMap.put(bean.getUniqueId(), new WeakReference<IConsumerProcess<U>>(process));

			process.start(); // Depending on the process may run in a separate thread (default is not to)
		} catch (Exception e) {
			// if an exception is thrown, set the bean status to failed. Note the exception is logged in processException()
			bean.setStatus(Status.FAILED);
			bean.setMessage(e.getMessage());
			statusTopicPublisher.broadcast(bean);
			throw e;
		}
	}

	/**
	 * Exits the whole VM(!) if wait time has exceeded one day.
	 * @param waitTime
	 */
	protected void checkTime(long waitTime) {
		if (waitTime > ONE_DAY) {
			setActive(false);
			LOGGER.warn("ActiveMQ permanently lost. {} will now shutdown!", getName());
			exit();
		}
	}

	private Message getMessage(URI uri, String submitQName) throws JMSException {
		try {
			if (this.messageConsumer == null) {
				this.messageConsumer = createMessageConsumer(uri, submitQName);
			}
			return messageConsumer.receive(Constants.getReceiveFrequency());

		} catch (Exception ne) {
			if (Thread.interrupted()) return null;
			messageConsumer = null;
			try {
				connection.close();
			} catch (Exception expected) {
				LOGGER.info("Cannot close old connection", ne);
			}
			throw ne;
		}
	}

	private MessageConsumer createMessageConsumer(URI uri, String submitQName) throws JMSException {
		QueueConnectionFactory connectionFactory = (QueueConnectionFactory)service.createConnectionFactory(uri);
		this.connection = connectionFactory.createQueueConnection();
		Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		Queue queue = session.createQueue(submitQName);

		final MessageConsumer consumer = session.createConsumer(queue);
		connection.start();

		LOGGER.info("{} Submission ActiveMQ connection to {} made.", getName(), uri);

		return consumer;
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
	public boolean isDurable() {
		return durable;
	}

	@Override
	public void setDurable(boolean durable) {
		this.durable = durable;
	}

	@Override
	public boolean isPauseOnStart() {
		return pauseOnStart;
	}

	@Override
	public void setPauseOnStart(boolean pauseOnStart) {
		this.pauseOnStart = pauseOnStart;
	}

	/**
	 * Class responsible for updating and broadcasting {@code HeartbeatBean}s. Essentially a wrapper for an {@code IPublisher<HeartbeatBean>}
	 */
	private class HeartbeatBroadcaster {

		private final IPublisher<HeartbeatBean> publisher;
		private final IConsumer<?> consumer;
		private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
			Thread thread = Executors.defaultThreadFactory().newThread(r);
			thread.setName("Alive Notification " + getTopicName());
			thread.setDaemon(true);
			thread.setPriority(Thread.MIN_PRIORITY);
			return thread;
		});
		private volatile HeartbeatBean lastBeat;
		private boolean broadcasting = false;

		public HeartbeatBroadcaster(URI uri, String heartbeatTopicName, IConsumer<?> consumer) {
			this.consumer = Objects.requireNonNull(consumer);
			publisher = eservice.createPublisher(uri, heartbeatTopicName);
		}

		/**
		 * Starts a thread which broadcasts {@link HeartbeatBean}s to the heartbeat topic
		 * at a frequency determined by the property <code>org.eclipse.scanning.event.heartbeat.freq</code>
		 */
		public void start() {
			if (broadcasting) throw new IllegalStateException("Cannot start heartbeat broadcast; it has already been started");
			final HeartbeatBean beat = new HeartbeatBean();
			beat.setConceptionTime(System.currentTimeMillis());

			scheduler.scheduleAtFixedRate(()->{
				try {
					updateHeartbeatBean(beat);
					publisher.broadcast(beat);
				} catch (Exception e) {
					LOGGER.error("Error encountered while broadcasting heartbeat", e);
				}
				lastBeat = beat;
			}, Constants.getNotificationFrequency(), Constants.getNotificationFrequency(), TimeUnit.MILLISECONDS);
			broadcasting = true;
		}

		/**
		 * Stops the heartbeat broadcast thread.
		 * @throws EventException
		 */
		public void stop() throws EventException {
			if (!broadcasting) throw new IllegalStateException("Cannot stop Heartbeat broadcaster; it is not running");
			scheduler.shutdownNow();
			lastBeat.setConsumerStatus(ConsumerStatus.STOPPED);
			publisher.broadcast(lastBeat);
			broadcasting = false;
		}

		private void updateHeartbeatBean(HeartbeatBean beat) throws UnknownHostException {
			beat.setPublishTime(System.currentTimeMillis());
			beat.setConsumerId(consumer.getConsumerId());
			beat.setConsumerName(consumer.getName());
			beat.setConsumerStatus(consumer.getConsumerStatus());
			beat.setBeamline(System.getenv("BEAMLINE"));
			beat.setHostName(InetAddress.getLocalHost().getHostName());
		}

		public void disconnect() throws EventException {
			publisher.disconnect();
		}
	}
}
