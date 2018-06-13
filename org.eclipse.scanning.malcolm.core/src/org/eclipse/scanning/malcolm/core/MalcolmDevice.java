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
package org.eclipse.scanning.malcolm.core;

import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.eclipse.scanning.api.ValidationException;
import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.device.models.IMalcolmModel;
import org.eclipse.scanning.api.device.models.MalcolmModel;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.malcolm.MalcolmDeviceException;
import org.eclipse.scanning.api.malcolm.attributes.IDeviceAttribute;
import org.eclipse.scanning.api.malcolm.attributes.MalcolmAttribute;
import org.eclipse.scanning.api.malcolm.attributes.NumberAttribute;
import org.eclipse.scanning.api.malcolm.connector.IMalcolmConnection;
import org.eclipse.scanning.api.malcolm.connector.IMalcolmConnection.IMalcolmConnectionEventListener;
import org.eclipse.scanning.api.malcolm.connector.IMalcolmConnection.IMalcolmConnectionStateListener;
import org.eclipse.scanning.api.malcolm.connector.IMalcolmMessageGenerator;
import org.eclipse.scanning.api.malcolm.connector.MalcolmMethod;
import org.eclipse.scanning.api.malcolm.event.MalcolmEvent;
import org.eclipse.scanning.api.malcolm.message.MalcolmMessage;
import org.eclipse.scanning.api.malcolm.message.MalcolmUtil;
import org.eclipse.scanning.api.malcolm.message.Type;
import org.eclipse.scanning.api.points.IMutator;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.scan.ScanningException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Object that make the connection to the device and monitors its status.
 *
 * @author Matthew Gerring
 */
public class MalcolmDevice<M extends MalcolmModel> extends AbstractMalcolmDevice<M> {

	/**
	 * An enumeration of timeout property names and default values for different operations.
	 */
	private enum Timeout {
		STANDARD("org.eclipse.scanning.malcolm.core.timeout", Duration.ofSeconds(5)),
		CONFIG("org.eclipse.scanning.malcolm.core.configureTimeout", Duration.ofMinutes(10)),
		RUN("org.eclipse.scanning.malcolm.core.runTimeout", Duration.ofDays(2));

		private final long timeoutMillis;

		Timeout(String propertyName, Duration defaultTimeout) {
			timeoutMillis = Long.getLong(propertyName, defaultTimeout.toMillis());
		}

		public long toMillis() {
			return timeoutMillis;
		}
	}

	/**
	 * A listener to the whether we are connected to malcolm.
	 */
	private class ConnectionStateListener implements IMalcolmConnectionStateListener {

		private final ExecutorService executor;

		public ConnectionStateListener() {
			executor = Executors.newSingleThreadExecutor(r -> {
				Thread thread = Executors.defaultThreadFactory().newThread(r);
				thread.setName("Malcolm Connection Thread " + getName());
				thread.setDaemon(true);
				return thread;
			});
		}

		public void subscribe() {
			// Call doSubscribe in a separate thread as it is blocking with no timeout
			executor.submit(this::doSubscribe);
		}

		private void doSubscribe() {
			try {
				// Subscribe to state change events. This is done is a separate thread as this method
				// blocks with no timeout until a connection is made
				malcolmConnection.subscribeToConnectionStateChange(MalcolmDevice.this, this);
				connectedToMalcolm();
			} catch (MalcolmDeviceException e) {
				logger.error("Could not subsribe to malcolm state changes for device ''{}''", getName());
			}
		}

		/**
		 * Handle a change in the connection state of this device. Event is sent by the communications layer.
		 *
		 * @param <code>true</code> if the device has changed to being connected, <code>false</code> if
		 *            it has been disconnected
		 */
		@Override
		public void connectionStateChanged(boolean connected) {
			try {
				setAlive(connected);
				if (connected) {
					logger.info("Malcolm Device '{}' connection state changed to connected", getName());
					executor.submit(this::connectedToMalcolm);
				} else {
					logger.warn("Malcolm Device '{}' connection state changed to not connected", getName());
					executor.submit(this::disconnectedFromMalcolm);
				}
			} catch (Exception ne) {
				logger.error("Problem dispatching message!", ne);
			}
		}

		private void connectedToMalcolm() {
			try {
				if (!succesfullyInitialised) {
					// if we failed to initialize on startup, try again now we have a connection
					initialize();
				}

				final DeviceState deviceState = getDeviceState();
				logger.info("Connected to ''{}''. Current state: {}", getName(), deviceState);

				handleStateChange(deviceState, "connected to " + getName());
			} catch (MalcolmDeviceException ex) {
				logger.warn("Unable to initialise/getDeviceState for device '{}' on reconnection", getName(), ex);
			}
		}

		private void disconnectedFromMalcolm() {
			handleStateChange(DeviceState.OFFLINE, "disconnected from " + getName());
		}

		public void dispose() {
			// TODO IMalcolmConnection currently offers no unsubscribeFromConnectionStateChange method
			executor.shutdown();
		}

	}

	private static final Logger logger = LoggerFactory.getLogger(MalcolmDevice.class);

	// Constants
	public static final String STATE_ENDPOINT = "state";
	public static final String HEALTH_ENDPOINT = "health";
	public static final String COMPLETED_STEPS_ENDPOINT = "completedSteps";
	private static final String FILE_EXTENSION_H5 = "h5";
	public static final String STANDARD_MALCOLM_ERROR_STR = "Error from Malcolm Device Connection: ";

	// Frequencies and Timeouts
	// broadcast every 250 milliseconds
	public static final long POSITION_COMPLETE_INTERVAL = Long.getLong("org.eclipse.scanning.malcolm.core.positionCompleteInterval", 250);


	// Listeners to malcolm endpoints
	private final IMalcolmConnectionEventListener stateChangeListener = this::handleStateChange;
	private final IMalcolmConnectionEventListener scanEventListener = this::handleStepsCompleted;

	// Subscriber messages
	private MalcolmMessage stateSubscribeMessage;
	private MalcolmMessage scanSubscribeMessage;

	// Local data.
	private long lastBroadcastTime = System.currentTimeMillis();
	private boolean succesfullyInitialised = false;

	// Factory class for creating message to send to the connection layer
	private IMalcolmMessageGenerator messageGenerator;

	private IMalcolmConnection malcolmConnection;

	private ConnectionStateListener connectionStateListener;


	public MalcolmDevice() {
		this(null, Services.getConnectorService(), Services.getRunnableDeviceService());
	}

	public MalcolmDevice(String name, IMalcolmConnection malcolmConnection,
			IRunnableDeviceService runnableDeviceService) {
		super(runnableDeviceService);
		this.malcolmConnection = malcolmConnection;
		this.messageGenerator = malcolmConnection.getMessageGenerator();
		setName(name);
		try {
			setDeviceState(DeviceState.OFFLINE);
		} catch (ScanningException e) {
			logger.error("Error setting device state", e); // Impossible as no listeners yet
		}

		setAlive(false);
	}

	@Override
	public void initialize() throws MalcolmDeviceException {
		logger.debug("initialize() called");

		setAlive(false);
		final DeviceState currentState = getDeviceState();
		logger.debug("Connecting to ''{}''. Current state: {}", getName(), currentState);

		try {
			stateSubscribeMessage = messageGenerator.createSubscribeMessage(STATE_ENDPOINT);
			subscribe(stateSubscribeMessage, stateChangeListener);

			scanSubscribeMessage = messageGenerator.createSubscribeMessage(COMPLETED_STEPS_ENDPOINT);
			subscribe(scanSubscribeMessage, scanEventListener);

			setAlive(true);
			succesfullyInitialised = true;
			logger.debug("Malcolm device ''{}'' successfully initialized", getName());
		} finally {
			// Listen for malcolm connection state changes. This creates a new thread that
			// blocks until the connection is made, and then calls initialize again if
			// successfullyInitialized is false, i.e. an exception was thrown, which will be
			// the case if malcolm was down. This is done in a finally block for this
			// reason and so that messages to malcolm are sent in a predicatable order for testing
			if (connectionStateListener == null) {
				connectionStateListener = new ConnectionStateListener();
				connectionStateListener.subscribe();
			}
		}
	}

	protected void handleStepsCompleted(MalcolmMessage message) {
		logger.debug("Received state change event with message: {}", message);

		Integer stepIndex = null;
		Object value = message.getValue();
		if (value instanceof Map) {
			// TODO: it's likely that its always the same one of these cases.
			stepIndex = (Integer)((Map<?,?>)value).get("value");
		} else if (value instanceof NumberAttribute) {
			stepIndex = (Integer)((NumberAttribute)value).getValue();
		}

		// Fire a position complete only if it's past the timeout value
		if (stepIndex != null) {
			long currentTime = System.currentTimeMillis();
			// fire position complete event to listeners, if we are over the position complete interval since the last update
			if (currentTime - lastBroadcastTime >= POSITION_COMPLETE_INTERVAL) {
				final MalcolmEvent event = MalcolmEvent.forStepsCompleted(this, stepIndex, message.getMessage());
				try {
					logger.debug("Sending malcolm steps completed event : {}", event);
					sendEvent(event);
				} catch (Exception e) {
					logger.error("Exception firing position complete", e);
				}

				lastBroadcastTime = System.currentTimeMillis();
			}
		}
	}

	protected void handleStateChange(MalcolmMessage message) {
		try {
			logger.debug("Received malcolm state change with message: {}", message);
			DeviceState newState = MalcolmUtil.getState(message, false);

			handleStateChange(newState, message.getMessage());

			if (message.getType().isError()) { // Currently used for debugging the device.
				logger.error("Error message encountered: {}", message);
			}
		} catch (Exception e) {
			logger.error("Could not send scan state change message");
		}
	}

	protected void handleStateChange(DeviceState newState, String message) {
		DeviceState prevState = null;
		try {
			prevState = super.getDeviceState();
			setDeviceState(newState);
		} catch (ScanningException e) {
			logger.error("Could not set device state to {}", newState, e);
		}

		final MalcolmEvent event = MalcolmEvent.forStateChange(this, newState, prevState, message);
		logger.debug("Sending malcolm event: {}", event);
		try {
			sendEvent(event);
		} catch (Exception e) {
			logger.error("Could not update listeners", e);
		}
	}

	@Override
	public DeviceState getDeviceState() throws MalcolmDeviceException {
		try {
			final MalcolmMessage message = messageGenerator.createGetMessage(STATE_ENDPOINT);
			final MalcolmMessage reply = send(message, Timeout.STANDARD.toMillis());
			if (reply.getType()==Type.ERROR) {
				throw new MalcolmDeviceException(STANDARD_MALCOLM_ERROR_STR + reply.getMessage());
			}

			return MalcolmUtil.getState(reply); // TODO refactor this to not use MalcolmUtil. See JIRA ticket DAQ-1436
		} catch (MalcolmDeviceException mne) {
			throw mne;
		} catch (Exception ne) {
			throw new MalcolmDeviceException(this, "Cannot connect to device '" + getName() + "'", ne);
		}
	}

	@Override
	public String getDeviceHealth() throws MalcolmDeviceException {
		try {
			final MalcolmMessage message = messageGenerator.createGetMessage(HEALTH_ENDPOINT);
			final MalcolmMessage reply = send(message, Timeout.STANDARD.toMillis());
			if (reply.getType()==Type.ERROR) {
				throw new MalcolmDeviceException(STANDARD_MALCOLM_ERROR_STR + reply.getMessage());
			}

			return MalcolmUtil.getHealth(reply); // TODO refactor this to not use MalcolmUtil. See JIRA ticket DAQ-1436
		} catch (MalcolmDeviceException mne) {
			throw mne;
		} catch (Exception ne) {
			throw new MalcolmDeviceException(this, "Cannot connect to device '" + getName() + "'", ne);
		}
	}

	@Override
	public boolean isDeviceBusy() throws MalcolmDeviceException {
		return !getDeviceState().isRestState();
	}

	@Override
	public void validate(M params) throws ValidationException {
		logger.debug("validate() called");
		validateWithReturn(params);
	}

	@Override
	public Object validateWithReturn(M params) throws ValidationException {
		if (Boolean.getBoolean("org.eclipse.scanning.malcolm.skipvalidation")) {
			logger.warn("Skipping Malcolm Validate");
			return null;
		}

		final EpicsMalcolmModel epicsModel = createEpicsMalcolmModel(params);
		MalcolmMessage reply = null;
		try {
			final MalcolmMessage msg = messageGenerator.createCallMessage(MalcolmMethod.VALIDATE, epicsModel);
			reply = send(msg, Timeout.STANDARD.toMillis());
			if (reply.getType()==Type.ERROR) {
				throw new ValidationException(STANDARD_MALCOLM_ERROR_STR + reply.getMessage());
			}

		} catch (Exception mde) {
			throw new ValidationException(mde);
		}

		// TODO: reply.getRawValue is probably the EpicsMalcolmModel. It may be modified, e.g. the
		// exposure time may have been changed by malcolm to be a multiple of the clock speed. We
		// should take the updated duration time and update it in the malcolm model and then
		// return that. See JIRA ticket DAQ-1437
		return reply.getRawValue();
	}

	@Override
	public void configure(M model) throws MalcolmDeviceException {
		logger.debug("configure() called");

		// Reset the device before configure in case it's in a fault state
		try {
			reset();
		} catch (Exception ex) {
			// Swallow the error as it might throw one if in a non-resetable state
		}

		final EpicsMalcolmModel epicsModel = createEpicsMalcolmModel(model);
		final MalcolmMessage msg = messageGenerator.createCallMessage(MalcolmMethod.CONFIGURE, epicsModel);
		MalcolmMessage reply = wrap(()->send(msg, Timeout.CONFIG.toMillis()));
		if (reply.getType() == Type.ERROR) {
			throw new MalcolmDeviceException(STANDARD_MALCOLM_ERROR_STR + reply.getMessage());
		}
		setModel(model);
	}

	/**
	 * Create the {@link EpicsMalcolmModel} passed to the actual malcolm device. This is created from both the
	 * {@link IMalcolmModel} that this {@link MalcolmDevice} has been configured with and information about
	 * the scan, e.g. the scan path defined by the point generator, that have been set on this object.
	 * @param model
	 * @return
	 */
	private EpicsMalcolmModel createEpicsMalcolmModel(M model) {
		double exposureTime = model.getExposureTime();

		if (pointGenerator != null) {
			List<IMutator> mutators = new ArrayList<>();
			((CompoundModel<?>) pointGenerator.getModel()).setMutators(mutators);
			((CompoundModel<?>) pointGenerator.getModel()).setDuration(exposureTime);
		}

		String fileTemplate = null;
		if (fileDir != null) {
			fileTemplate = new File(fileDir).getName() + "-%s." + FILE_EXTENSION_H5;
		}

		return new EpicsMalcolmModel(fileDir, fileTemplate, model.getAxesToMove(), pointGenerator);
	}

	private void subscribe(MalcolmMessage subscribeMessage, IMalcolmConnectionEventListener listener) throws MalcolmDeviceException {
		malcolmConnection.subscribe(this, subscribeMessage, listener);
	}

	private MalcolmMessage unsubscribe(MalcolmMessage subscribeMessage, IMalcolmConnectionEventListener listener) throws MalcolmDeviceException {
		final MalcolmMessage unsubscribeMessage = messageGenerator.createUnsubscribeMessage();
		unsubscribeMessage.setId(subscribeMessage.getId()); // the id is used to identify the listener to remove
		final MalcolmMessage reply = malcolmConnection.unsubscribe(this, unsubscribeMessage, listener);
		logger.debug("Unsubscription {} made {}", getName(), unsubscribeMessage);
		return reply;
	}

	private MalcolmMessage send(MalcolmMessage message, long timeout) throws InterruptedException, ExecutionException, TimeoutException {
		logger.debug("Sending message to malcolm device: {}", message);
		return callWithTimeout(()->malcolmConnection.send(this, message), timeout);
	}

	private MalcolmMessage call(MalcolmMethod method, long timeout) throws InterruptedException, ExecutionException, TimeoutException {
		logger.debug("Calling method on malcolm device: {}", method);
		return callWithTimeout(()->call(method), timeout);
	}

	private MalcolmMessage call(MalcolmMethod method) throws MalcolmDeviceException {
		final MalcolmMessage msg = messageGenerator.createCallMessage(method);
		return malcolmConnection.send(this, msg);
	}

	/**
	 * Calls the function but wraps the exception if it is not MalcolmDeviceException
	 * @param callable
	 * @return
	 * @throws MalcolmDeviceException
	 */
	private MalcolmMessage wrap(Callable<MalcolmMessage> callable) throws MalcolmDeviceException {
		try {
			return callable.call();
		} catch (MalcolmDeviceException m) {
			throw m;
		} catch (Exception other) {
			throw new MalcolmDeviceException(this, other);
		}
	}

	private MalcolmMessage callWithTimeout(final Callable<MalcolmMessage> callable, long timeout) throws InterruptedException, ExecutionException, TimeoutException {
		final ExecutorService service = Executors.newSingleThreadExecutor();
		try {
			return service.submit(callable).get(timeout, TimeUnit.MILLISECONDS);
		} finally {
			service.shutdownNow();
		}
	}

	@Override
	public void run(IPosition pos) throws MalcolmDeviceException, InterruptedException, ExecutionException, TimeoutException {
		logger.debug("run() called with position {}", pos);
		MalcolmMessage reply = call(MalcolmMethod.RUN, Timeout.RUN.toMillis());
		if (reply.getType()==Type.ERROR) {
			throw new MalcolmDeviceException(STANDARD_MALCOLM_ERROR_STR + reply.getMessage());
		}
	}

	@Override
	public void seek(int stepNumber) throws MalcolmDeviceException {
		logger.debug("seek() called with step number {}", stepNumber);
		LinkedHashMap<String, Integer> seekParameters = new LinkedHashMap<>(); // TODO why is this a linked hash map
		seekParameters.put(COMPLETED_STEPS_ENDPOINT, stepNumber);
		final MalcolmMessage msg = messageGenerator.createCallMessage(MalcolmMethod.PAUSE, seekParameters);
		final MalcolmMessage reply = wrap(()->send(msg, Timeout.CONFIG.toMillis()));
		if (reply.getType()==Type.ERROR) {
			throw new MalcolmDeviceException(STANDARD_MALCOLM_ERROR_STR + reply.getMessage());
		}
	}

	@Override
	public void abort() throws MalcolmDeviceException {
		logger.debug("abort() called");
		MalcolmMessage reply = wrap(()->call(MalcolmMethod.ABORT, Timeout.STANDARD.toMillis()));
		if (reply.getType()==Type.ERROR) {
			throw new MalcolmDeviceException(STANDARD_MALCOLM_ERROR_STR + reply.getMessage());
		}
	}

	@Override
	public void disable() throws MalcolmDeviceException {
		logger.debug("disable() called");
		MalcolmMessage reply = wrap(()->call(MalcolmMethod.DISABLE, Timeout.STANDARD.toMillis()));
		if (reply.getType()==Type.ERROR) {
			throw new MalcolmDeviceException(STANDARD_MALCOLM_ERROR_STR + reply.getMessage());
		}
	}

	@Override
	public void reset() throws MalcolmDeviceException {
		logger.debug("reset() called");
		MalcolmMessage reply = wrap(()->call(MalcolmMethod.RESET, Timeout.STANDARD.toMillis()));
		if (reply.getType()==Type.ERROR) {
			throw new MalcolmDeviceException(STANDARD_MALCOLM_ERROR_STR + reply.getMessage());
		}
	}

	@Override
	public void pause() throws MalcolmDeviceException {
		logger.debug("pause() called");
		MalcolmMessage reply = wrap(()->call(MalcolmMethod.PAUSE, Timeout.CONFIG.toMillis()));
		if (reply.getType()==Type.ERROR) {
			throw new MalcolmDeviceException(STANDARD_MALCOLM_ERROR_STR + reply.getMessage());
		}
	}

	@Override
	public void resume() throws MalcolmDeviceException {
		logger.debug("resume() called");
		MalcolmMessage reply = wrap(()->call(MalcolmMethod.RESUME, Timeout.STANDARD.toMillis()));
		if (reply.getType()==Type.ERROR) {
			throw new MalcolmDeviceException(STANDARD_MALCOLM_ERROR_STR + reply.getMessage());
		}
	}

	@Override
	public void dispose() throws MalcolmDeviceException {
		logger.debug("dispose() called");
		if (stateSubscribeMessage != null) {
			unsubscribe(stateSubscribeMessage, stateChangeListener);
		}
		if (scanSubscribeMessage != null) {
			unsubscribe(scanSubscribeMessage, scanEventListener);
		}

		if (connectionStateListener != null) {
			connectionStateListener.dispose();
			connectionStateListener = null;
		}

		setAlive(false);
	}

	@Override
	public boolean isLocked() throws MalcolmDeviceException {
		final DeviceState state = getDeviceState();
		return state.isTransient(); // Device is not locked but it is doing something.
	}

	@Override
	public DeviceState latch(long time, TimeUnit unit, final DeviceState... ignoredStates) throws MalcolmDeviceException {
		try {
			final CountDownLatch latch = new CountDownLatch(1);
			final List<DeviceState> stateContainer = new ArrayList<>(1);
			final List<Exception> exceptionContainer = new ArrayList<>(1);

			// Make a listener to check for state and then add it and latch
			IMalcolmConnectionEventListener stateChanger = msg -> {
				try {
					DeviceState state = MalcolmUtil.getState(msg);
					if (state != null && ignoredStates != null && Arrays.asList(ignoredStates).contains(state)) {
						return; // Found state that we don't want!
					}
					stateContainer.add(state);
					latch.countDown();

				} catch (Exception ne) {
					exceptionContainer.add(ne);
					latch.countDown();
				}
			};

			subscribe(stateSubscribeMessage, stateChanger);

			boolean countedDown = false;
			if (time>0) {
				countedDown = latch.await(time, unit);
			} else {
				latch.await();
			}

			unsubscribe(stateSubscribeMessage, stateChanger);

			if (!exceptionContainer.isEmpty()) throw exceptionContainer.get(0);

			if (!stateContainer.isEmpty()) return stateContainer.get(0);

			if (countedDown) {
				throw new MalcolmDeviceException("The countdown of "+time+" "+unit+" timed out waiting for state change for device "+getName());
			} else {
				throw new MalcolmDeviceException("A problem occured trying to latch state change for device "+getName());
			}

		} catch (MalcolmDeviceException ne) {
			throw ne;

		} catch (Exception neOther) {
			throw new MalcolmDeviceException(this, neOther);
		}

	}

	@Override
	public <T> IDeviceAttribute<T> getAttribute(String attributeName) throws MalcolmDeviceException {
		logger.debug("getAttribute() called with attribute name {}", attributeName);
		final MalcolmMessage message = messageGenerator.createGetMessage(attributeName);
		final MalcolmMessage reply   = wrap(()->send(message, Timeout.STANDARD.toMillis()));
		if (reply.getType()==Type.ERROR) {
			throw new MalcolmDeviceException(STANDARD_MALCOLM_ERROR_STR + reply.getMessage());
		}

		Object result = reply.getValue();
		if (!(result instanceof MalcolmAttribute)) {
			throw new MalcolmDeviceException("No such attribute: " + attributeName);
		}

		@SuppressWarnings("unchecked")
		IDeviceAttribute<T> attribute = (IDeviceAttribute<T>) result;
		return attribute;
	}

	@Override
	public List<IDeviceAttribute<?>> getAllAttributes() throws MalcolmDeviceException {
		logger.debug("getAllAttributes() called");
		final MalcolmMessage message = messageGenerator.createGetMessage("");
		final MalcolmMessage reply   = wrap(()->send(message, Timeout.STANDARD.toMillis()));
		if (reply.getType()==Type.ERROR) {
			throw new MalcolmDeviceException(STANDARD_MALCOLM_ERROR_STR + reply.getMessage());
		}

		@SuppressWarnings("unchecked")
		Map<String, Object> wholeBlockMap = (Map<String, Object>) reply.getValue();
		return wholeBlockMap.values().stream().
				filter(MalcolmAttribute.class::isInstance).map(IDeviceAttribute.class::cast).
				collect(Collectors.toList());
	}

	/**
	 * Gets the value of an attribute on the device
	 */
	@Override
	public <T> T getAttributeValue(String attributeName) throws MalcolmDeviceException {
		logger.debug("getAttributeValue() called with attribute name {}", attributeName);
		IDeviceAttribute<T> attribute = getAttribute(attributeName);
		return attribute.getValue();
	}

	/**
	 * Instances of {@link EpicsMalcolmModel} are used to configure the actual malcolm device.
	 * This class should be distinguished from the {@link IMalcolmModel} that this
	 * class is configured with. The majority of the information contained in
	 * an {@link EpicsMalcolmModel} describes the scan, e.g. the scan path
	 * defined by the {@link IPointGenerator} and the directory to write to.
	 */
	public static final class EpicsMalcolmModel {

		/**
		 * A point generator describing the scan path.
		 */
		private final IPointGenerator<?> generator;

		/**
		 * The axes of the scan that malcolm should control. Can be <code>null</code>
		 * in which case malcolm will control all the axes that it knows about.
		 */
		private final List<String> axesToMove;

		/**
		 * The directory in which malcolm should write its files for the scan,
		 * typically something like e.g. {@code /dls/ixx/data/2018/cm12345-1/ixx-123456/}
		 * where the final segment identifies the scan.
		 */
		private final String fileDir;

		/**
		 * A file template for the files that malcolm creates, e.g.
		 * {@code ixx-123456-%s.h5}, where '%s' is the placeholder for malcolm to
		 * insert the device name, e.g. {@code PANDABOX}.
		 */
		private final String fileTemplate;

		public EpicsMalcolmModel(String fileDir, String fileTemplate,
				List<String> axesToMove, IPointGenerator<?> generator) {
			this.fileDir = fileDir;
			this.fileTemplate = fileTemplate;
			this.axesToMove = axesToMove;
			this.generator = generator;
		}

		public String getFileDir() {
			return fileDir;
		}

		public String getFileTemplate() {
			return fileTemplate;
		}

		public List<String> getAxesToMove() {
			return axesToMove;
		}

		public IPointGenerator<?> getGenerator() {
			return generator;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((axesToMove == null) ? 0 : axesToMove.hashCode());
			result = prime * result + ((fileDir == null) ? 0 : fileDir.hashCode());
			result = prime * result + ((fileTemplate == null) ? 0 : fileTemplate.hashCode());
			result = prime * result + ((generator == null) ? 0 : generator.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			EpicsMalcolmModel other = (EpicsMalcolmModel) obj;
			if (axesToMove == null) {
				if (other.axesToMove != null)
					return false;
			} else if (!axesToMove.equals(other.axesToMove))
				return false;
			if (fileDir == null) {
				if (other.fileDir != null)
					return false;
			} else if (!fileDir.equals(other.fileDir))
				return false;
			if (fileTemplate == null) {
				if (other.fileTemplate != null)
					return false;
			} else if (!fileTemplate.equals(other.fileTemplate))
				return false;
			if (generator == null) {
				if (other.generator != null)
					return false;
			} else if (!generator.equals(other.generator))
				return false;
			return true;
		}

	}

}
