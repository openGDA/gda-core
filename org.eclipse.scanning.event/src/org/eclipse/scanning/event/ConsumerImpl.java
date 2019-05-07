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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventConnectorService;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.bean.BeanEvent;
import org.eclipse.scanning.api.event.bean.IBeanListener;
import org.eclipse.scanning.api.event.consumer.ConsumerStatus;
import org.eclipse.scanning.api.event.consumer.ConsumerStatusBean;
import org.eclipse.scanning.api.event.consumer.QueueCommandBean;
import org.eclipse.scanning.api.event.consumer.QueueCommandBean.Command;
import org.eclipse.scanning.api.event.core.IConsumer;
import org.eclipse.scanning.api.event.core.IConsumerProcess;
import org.eclipse.scanning.api.event.core.IProcessCreator;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.eclipse.scanning.event.queue.IModifiableIdQueue;
import org.eclipse.scanning.event.queue.SynchronizedModifiableIdQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ConsumerImpl<U extends StatusBean> extends AbstractConnection implements IConsumer<U> {


	private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerImpl.class);
	private String name;
	private UUID consumerId;
	private IEventService eventService;
	private IPublisher<U> statusTopicPublisher; // a publisher to the status topic
	private IPublisher<ConsumerStatusBean> consumerStatusPublisher;
	private ISubscriber<IBeanListener<QueueCommandBean>> commandTopicSubscriber; // a subscriber to the command topic
	private IPublisher<QueueCommandBean> commandAckTopicPublisher; // a publisher to the command acknowledgement topic

	private boolean pauseOnStart = false;
	private CountDownLatch latchStart;
	private CountDownLatch latchStop;

	private IProcessCreator<U> runner;

	private volatile boolean active = false;
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
	private final IModifiableIdQueue<U> submissionQueue;
	private final IModifiableIdQueue<U> statusSet;

	private final Set<IConsumerStatusListener> consumerStatusListeners = new CopyOnWriteArraySet<>();
	private ConsumerStatusBean consumerStatusBean;

	public ConsumerImpl(URI uri, String submitQueueName, String statusTopicName, String consumerStatusTopicName,
			String commandTopicName, String commandAckTopicName, IEventConnectorService connectorService,
			IEventService eventService)
			throws EventException {
		super(uri, submitQueueName, statusTopicName, connectorService);
		this.eventService = eventService;

		this.awaitPaused = false;
		this.consumerStateChangeLock = new ReentrantLock();
		this.shouldResumeCondition = consumerStateChangeLock.newCondition();

		consumerId = UUID.randomUUID();
		name = "Consumer " + consumerId; // This will hopefully be changed to something meaningful...
		this.commandTopicName = commandTopicName;
		this.commandAckTopicName = commandAckTopicName;
		this.consumerStatusTopicName = consumerStatusTopicName;

		submissionQueue = new SynchronizedModifiableIdQueue<>();
		statusSet = new SynchronizedModifiableIdQueue<>();

		connect();
	}

	@Override
	public void addConsumerStatusListener(IConsumerStatusListener listener) {
		consumerStatusListeners.add(listener);
	}

	@Override
	public void removeConsumerStatusListener(IConsumerStatusListener listener) {
		consumerStatusListeners.remove(listener);
	}

	private void notifyStatusChanged() {
		if (consumerStatusBean != null && consumerStatusPublisher != null) {
			publishConsumerStatusBean();
		}

		final ConsumerStatus status = getConsumerStatus();
		for (IConsumerStatusListener listener : consumerStatusListeners) {
			listener.consumerStatusChanged(status);
		}
	}

	private void connect() throws EventException {
		statusTopicPublisher = eventService.createPublisher(uri, getStatusTopicName());

		if (consumerStatusTopicName!=null) {
			consumerStatusPublisher = eventService.createPublisher(uri, consumerStatusTopicName);
			consumerStatusBean = createConsumerStatusBean();
		}

		if (getCommandTopicName()!=null) {
			commandTopicSubscriber = eventService.createSubscriber(uri, getCommandTopicName());
			commandTopicSubscriber.addListener(new CommandListener());
			commandAckTopicPublisher = eventService.createPublisher(uri, commandAckTopicName);
		}

		setConnected(true);
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
		consumerStatusBean.setPublishTime(System.currentTimeMillis());
		consumerStatusBean.setConsumerStatus(getConsumerStatus());
		try {
			consumerStatusPublisher.broadcast(consumerStatusBean);
		} catch (EventException e) {
			LOGGER.error("Could not publish consumer status bean", e);
		}
	}

	@Override
	public synchronized void disconnect() throws EventException {
		if (isActive()) stop();

		if (statusTopicPublisher != null) {
			statusTopicPublisher.disconnect();
			statusTopicPublisher = null;
		}
		if (consumerStatusPublisher != null) {
			consumerStatusPublisher.disconnect();
			consumerStatusPublisher = null;
		}
		if (commandTopicSubscriber != null)  {
			commandTopicSubscriber.disconnect();
			commandTopicSubscriber = null;
		}
		if (commandAckTopicPublisher != null) {
			commandAckTopicPublisher.disconnect();
			commandAckTopicPublisher = null;
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
				case CLEAR_QUEUE:
					clearQueue();
					break;
				case CLEAR_COMPLETED:
					clearRunningAndCompleted();
					break;
				case SUBMIT_JOB:
					submit((U) commandBean.getJobBean());
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
					result = findBeanAndPerformAction(commandBean.getJobBean(), this::moveForward);
					break;
				case MOVE_BACKWARD:
					result = findBeanAndPerformAction(commandBean.getJobBean(), this::moveBackward);
					break;
				case REMOVE_FROM_QUEUE:
					result = findBeanAndPerformAction(commandBean.getJobBean(), this::remove);
					break;
				case REMOVE_COMPLETED:
					removeCompleted((U) commandBean.getJobBean());
					break;
				case GET_QUEUE:
					result = getSubmissionQueue();
					break;
				case GET_RUNNING_AND_COMPLETED:
					result = getRunningAndCompleted();
					break;
				case GET_INFO:
					result = consumerStatusBean;
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
	public List<U> getSubmissionQueue() throws EventException {
		synchronized (submissionQueue) {
			return new ArrayList<>(submissionQueue);
		}
	}

	@Override
	public List<U> getRunningAndCompleted() throws EventException {
		return statusSet.getElements();
	}

	@Override
	public void clearQueue() throws EventException {
		submissionQueue.clear();
	}

	@Override
	public void clearRunningAndCompleted() throws EventException {
		statusSet.clear();
	}

	@Override
	public void cleanUpCompleted() throws EventException {
		synchronized (statusSet) {
			for (Iterator<U> iter = statusSet.iterator(); iter.hasNext(); ) {
				final U bean = iter.next();
				final Status status = bean.getStatus();
				if (status == null) {
					// do nothing (not sure this case is even possible
				} else if (!status.isStarted() || status == Status.PAUSED) {
					// set paused or not started beans to failed
					bean.setStatus(Status.FAILED);
					LOGGER.warn("Failed job {}: {}", bean.getName(), bean);
				} else if (status == Status.FAILED || status == Status.NONE) {
					iter.remove(); // remove failed jobs (note: Status.NONE is probably not possible)
				} else if (status.isRunning()) {
					// remove running jobs older than the maximum running age
					if (System.currentTimeMillis() - bean.getSubmissionTime() > EventTimingsHelper.getMaximumRunningAgeMs()) {
						iter.remove();
					}
				} else if (status.isFinal()) {
					if (System.currentTimeMillis() - bean.getSubmissionTime() > EventTimingsHelper.getMaximumCompleteAgeMs()) {
						iter.remove();
					}
				}
			}
		}
	}

	@Override
	public boolean moveForward(U bean) throws EventException {
		return submissionQueue.moveUp(bean);
	}

	@Override
	public boolean moveBackward(U bean) throws EventException {
		return submissionQueue.moveDown(bean);
	}

	@Override
	public void submit(U bean) throws EventException {
		submissionQueue.add(bean);
	}

	@Override
	public boolean remove(U bean) throws EventException {
		return submissionQueue.remove(bean);
	}

	@Override
	public boolean removeCompleted(U bean) throws EventException {
		synchronized (statusSet) {
			for (Iterator<U> iter = statusSet.iterator(); iter.hasNext(); ) {
				if (iter.next().getUniqueId().equals(bean.getUniqueId())) {
					iter.remove();
					LOGGER.info("Removed bean with id {} from set of completed jobs. Bean: {}", bean.getUniqueId(), bean);
					return true;
				}
			}
		}

		LOGGER.warn("Could not find bean with id {} to remove from set of completed jobs. Bean: {}", bean.getUniqueId(), bean);
		return false;
	}

	@Override
	public boolean replace(U newBean) throws EventException {
		return submissionQueue.replace(newBean);
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
		latchStop = new CountDownLatch(1);
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
		if (latchStart != null) latchStart.await();
	}

	@Override
	public void awaitStop() throws InterruptedException {
		if (latchStop != null) latchStop.await();
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
			processJobCommand((U) commandBean.getJobBean(), commandBean.getCommand());
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
					replace(optBean.get());
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
				if (process != null && !process.getBean().getStatus().isFinal()) {
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

		if (latchStop != null) latchStop.countDown();
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
				consume();
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
	 */
	private void consume() {
		try {
			checkPaused(); // blocks until not paused.
			if (isActive()) { // isActive could have been set to false while we were paused
				final U bean = getNextBean();
				if (bean == null) {
					Thread.sleep(20); // TODO use constant for this sleep?
				} else {
					executeBean(bean);
				}
			}
		} catch (Exception e) {
			// if processException returns false, break out of the loop to exit the consumer
			setActive(processException(e));
		}
	}

	private U getNextBean() {
		return submissionQueue.poll();
	}

	private void init() throws EventException {
		LOGGER.debug("Initializing consumer for queue {}", getSubmitQueueName());

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

	/**
	 * Processes an exception that occurred during the {@link #run()} loop.
	 * @param e exception
	 * @return <code>true</code> to keep processing messages, <code>false</code> to exit
	 * @throws EventException
	 */
	private boolean processException(Exception e) {
		LOGGER.debug("Processing exception in consumer", e);

		if (e instanceof InterruptedException) {
			LOGGER.error("Consumer was interrupted", e);
			return false;
		}

		LOGGER.error("Cannot consume bean", e);
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

		if (!submissionQueue.isEmpty()) {
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

	@Override
	public void pause() throws EventException {
		awaitPaused = true;
		LOGGER.info("Consumer signalled to pause {}", getName());
		notifyStatusChanged();
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

	protected Object findBeanAndPerformAction(StatusBean bean, BeanAction<U> action) throws EventException {
		final Optional<U> optBean = findBeanWithId(getSubmissionQueue(), bean.getUniqueId());
		if (optBean.isPresent()) {
			return action.performAction(optBean.get());
		} else {
			LOGGER.error("Cannot find bean with id ''{}'' in submission queue!\nIt might be running now.", bean.getUniqueId());
			return null;
		}
	}

	protected void removeCompleted(String beanUniqueId) throws EventException {
		final Optional<U> optBean = findBeanWithId(getRunningAndCompleted(), beanUniqueId);
		if (optBean.isPresent()) {
			removeCompleted(optBean.get());
		} else {
			LOGGER.error("Cannot find bean with id ''{}'' in status set", beanUniqueId);
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
		// We record the bean in the status queue
		LOGGER.trace("Adding bean {} to status set for queue:", bean, getSubmitQueueName());
		statusSet.add(bean);

		Instant timeNow = Instant.now();
		if (Duration.between(startTime, timeNow).toMillis() > 100) { LOGGER.warn("executeBean() took {}ms to statusSetSubmitter.submit complete", Duration.between(startTime, timeNow).toMillis()); }

		if (processManager.hasRunProcess(bean)) {
			throw new EventException("The bean with unique id '"+bean.getUniqueId()+"' has already been used. Cannot run the same uuid twice!");
		}

		// If terminate has been requested before the bean is run, don't run it
		// instead set state to TERMINATED and publish to status topic
		if (bean.getStatus()==Status.REQUEST_TERMINATE) {
			LOGGER.warn("Run aborted before started");
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
		} catch (InterruptedException e) {
			bean.setStatus(Status.TERMINATED);
			bean.setMessage(e.getMessage());
			statusTopicPublisher.broadcast(bean);
			throw e;
		} catch (Exception e) {
			// if an exception is thrown, set the bean status to failed. Note the exception is logged in processException()
			bean.setStatus(Status.FAILED);
			bean.setMessage(e.getMessage());
			statusTopicPublisher.broadcast(bean);
			throw e;
		}
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

}
