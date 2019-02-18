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

import static org.eclipse.scanning.api.malcolm.MalcolmConstants.ATTRIBUTE_NAME_AXES_TO_MOVE;
import static org.eclipse.scanning.api.malcolm.MalcolmConstants.ATTRIBUTE_NAME_SIMULTANEOUS_AXES;
import static org.eclipse.scanning.api.malcolm.connector.IMalcolmConnection.ERROR_MESSAGE_PREFIX_FAILED_TO_CONNECT;

import java.io.File;
import java.text.MessageFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.scanning.api.ValidationException;
import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.device.models.IMalcolmModel;
import org.eclipse.scanning.api.device.models.MalcolmModel;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.malcolm.MalcolmConstants;
import org.eclipse.scanning.api.malcolm.MalcolmDeviceException;
import org.eclipse.scanning.api.malcolm.MalcolmTable;
import org.eclipse.scanning.api.malcolm.attributes.ChoiceAttribute;
import org.eclipse.scanning.api.malcolm.attributes.IDeviceAttribute;
import org.eclipse.scanning.api.malcolm.attributes.MalcolmAttribute;
import org.eclipse.scanning.api.malcolm.attributes.NumberAttribute;
import org.eclipse.scanning.api.malcolm.attributes.StringAttribute;
import org.eclipse.scanning.api.malcolm.connector.IMalcolmConnection;
import org.eclipse.scanning.api.malcolm.connector.IMalcolmConnection.IMalcolmConnectionEventListener;
import org.eclipse.scanning.api.malcolm.connector.IMalcolmConnection.IMalcolmConnectionStateListener;
import org.eclipse.scanning.api.malcolm.connector.IMalcolmMessageGenerator;
import org.eclipse.scanning.api.malcolm.connector.MalcolmMethod;
import org.eclipse.scanning.api.malcolm.event.MalcolmEvent;
import org.eclipse.scanning.api.malcolm.message.MalcolmMessage;
import org.eclipse.scanning.api.malcolm.message.Type;
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
		/**
		 * Malcolm has a basic internal timeout of 10 seconds.
		 * Our equivalent timeout is greater in order to get a MalcolmMessage with an error
		 * in the event of an internal Malcolm timeout.
		 */
		STANDARD("org.eclipse.scanning.malcolm.core.timeout", Duration.ofSeconds(15)),
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

		try {
			logger.debug("Connecting to malcolm device ''{}''.", getName());
			setAlive(false);
			final DeviceState currentState = getDeviceState(); // throws exception if no connection
			logger.debug("Current state of malcolm device ''{}'' is: {}", getName(), currentState);

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
		logger.debug("Received malcolm message on endpoint {}, message: {}", COMPLETED_STEPS_ENDPOINT, message);

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
			logger.debug("Received malcolm message on endpoint {}, message: {}", STATE_ENDPOINT, message);

			final ChoiceAttribute attribute = (ChoiceAttribute) message.getValue();
			final DeviceState newState = DeviceState.valueOf(attribute.getValue().toUpperCase());
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
		final ChoiceAttribute choiceAttr = (ChoiceAttribute) getEndpointValue(STATE_ENDPOINT);
		return DeviceState.valueOf(choiceAttr.getValue().toUpperCase());
	}

	@Override
	public String getDeviceHealth() throws MalcolmDeviceException {
		final StringAttribute attribute = (StringAttribute) getEndpointValue(HEALTH_ENDPOINT);
		return attribute.getValue();
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

		MalcolmMessage reply = null;
		try {
			final EpicsMalcolmModel epicsModel = createEpicsMalcolmModel(params);
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

	private Object getEndpointValue(String endpointName) throws MalcolmDeviceException {
		final MalcolmMessage reply = getEndpointReply(endpointName);
		if (reply.getType()==Type.ERROR) {
			throw new MalcolmDeviceException(STANDARD_MALCOLM_ERROR_STR + reply.getMessage());
		}

		return reply.getValue();
	}

	private MalcolmMessage getEndpointReply(String endpointName) throws MalcolmDeviceException {
		final MalcolmMessage message = messageGenerator.createGetMessage(endpointName);
		return wrap(()->send(message, Timeout.STANDARD.toMillis()));
	}

	/**
	 * Create the {@link EpicsMalcolmModel} passed to the actual malcolm device. This is created from both the
	 * {@link IMalcolmModel} that this {@link MalcolmDevice} has been configured with and information about
	 * the scan, e.g. the scan path defined by the point generator, that have been set on this object.
	 * @param model
	 * @return
	 */
	private EpicsMalcolmModel createEpicsMalcolmModel(M model) throws MalcolmDeviceException {
		double exposureTime = model.getExposureTime();

		// set the exposure time and mutators in the points generator
		if (pointGenerator != null) {
			((CompoundModel<?>) pointGenerator.getModel()).setMutators(Collections.emptyList());
			((CompoundModel<?>) pointGenerator.getModel()).setDuration(exposureTime);
		}


		// set the file template
		String fileTemplate = null;
		if (fileDir != null) {
			fileTemplate = new File(fileDir).getName() + "-%s." + FILE_EXTENSION_H5;
		}

		// get the axes to move
		List<String> axesToMove = model.getAxesToMove();
		if (axesToMove == null && pointGenerator != null) {
			final List<String> scannableNames = pointGenerator.iterator().next().getNames(); // TODO get names direct from point generator when we can
			final List<String> availableAxes = getAvailableAxes();
			int i = scannableNames.size() - 1;
			while (i >= 0 && availableAxes.contains(scannableNames.get(i))) {
				i--;
			}
			// i is now the index of the first non-malcolm axis, or -1 if all axes malcolm controlled
			axesToMove = scannableNames.subList(i + 1, scannableNames.size());
		}

		return new EpicsMalcolmModel(fileDir, fileTemplate, axesToMove, pointGenerator);
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
		final MalcolmMessage reply = callWithTimeout(()->malcolmConnection.send(this, message), timeout);
		logger.debug("Received reply from malcolm device: {}", reply);
		return reply;
	}

	private MalcolmMessage call(MalcolmMethod method, long timeout) throws InterruptedException, ExecutionException, TimeoutException {
		logger.debug("Calling method on malcolm device: {}", method);
		final MalcolmMessage reply = callWithTimeout(()->call(method), timeout);
		logger.debug("Received reply from malcolm device: {}", reply);
		return reply;
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


		// First call abort to abort any existing scan. This
		MalcolmMessage reply = wrap(()->call(MalcolmMethod.ABORT, Timeout.STANDARD.toMillis()));
		if (reply.getType() == Type.ERROR) {
			logger.warn("Error aborting malcolm device ''{}''. This is normal depending on the current state", getName());
		}

		reply = wrap(()->call(MalcolmMethod.RESET, Timeout.STANDARD.toMillis()));
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

	private <T> Optional<IDeviceAttribute<T>> getOptionalAttribute(String attributeName) throws MalcolmDeviceException {
		logger.debug("getAttribute() called with attribute name {}", attributeName);
		final MalcolmMessage reply = getEndpointReply(attributeName);
		if (reply.getType() != Type.ERROR && reply.getValue() instanceof MalcolmAttribute) {
			// found the attribute ok, return it
			@SuppressWarnings("unchecked") // temp variable to avoid annotation on method
			IDeviceAttribute<T> result = (IDeviceAttribute<T>) reply.getValue();
			return Optional.of(result);
		}
		// check if the error message is a connection failure, in this case throw an exception
		if (reply.getType() == Type.ERROR && reply.getMessage().startsWith(ERROR_MESSAGE_PREFIX_FAILED_TO_CONNECT)) {
			throw new MalcolmDeviceException(STANDARD_MALCOLM_ERROR_STR + reply.getMessage());
		}

		// otherwise (presumably when there is no such attribute) return an empty optional
		return Optional.empty();
	}

	private <T> IDeviceAttribute<T> getAttribute(String attributeName) throws MalcolmDeviceException {
		@SuppressWarnings("unchecked") // temp variable to avoid annotation on method
		final IDeviceAttribute<T> attribute = (IDeviceAttribute<T>) getOptionalAttribute(attributeName).
				orElseThrow(() -> new MalcolmDeviceException(STANDARD_MALCOLM_ERROR_STR + "No such attribute: " + attributeName));
		return attribute;
	}

	/**
	 * Gets the value of an attribute on the device
	 */
	private <T> T getAttributeValue(String attributeName) throws MalcolmDeviceException {
		logger.debug("getAttributeValue() called with attribute name {}", attributeName);
		IDeviceAttribute<T> attribute = getAttribute(attributeName);
		return attribute.getValue();
	}

	@Override
	public List<String> getAvailableAxes() throws MalcolmDeviceException {
		// as of malcolm version 3, the attribute 'axesToMove' will be replaced by 'simultaneousAxes' with
		// a subtly different meaning
		Optional<IDeviceAttribute<String[]>> optAttr = getOptionalAttribute(ATTRIBUTE_NAME_SIMULTANEOUS_AXES);
		if (!optAttr.isPresent()) {
			optAttr = getOptionalAttribute(ATTRIBUTE_NAME_AXES_TO_MOVE);
		}

		return optAttr.map(IDeviceAttribute::getValue).map(Arrays::asList).map(ArrayList::new).orElseThrow(
				() -> new MalcolmDeviceException(MessageFormat.format("Malcolm device has no axes attribute, either {0} or {1}",
						ATTRIBUTE_NAME_SIMULTANEOUS_AXES, ATTRIBUTE_NAME_AXES_TO_MOVE)));
	}

	@Override
	public boolean isNewMalcolmVersion() throws MalcolmDeviceException {
		return getOptionalAttribute(ATTRIBUTE_NAME_SIMULTANEOUS_AXES).isPresent();
	}

	@Override
	public MalcolmTable getDatasets() throws MalcolmDeviceException {
		return getAttributeValue(MalcolmConstants.ATTRIBUTE_NAME_DATASETS);
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
