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

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.eclipse.scanning.api.malcolm.MalcolmConstants.ATTRIBUTE_NAME_HEALTH;
import static org.eclipse.scanning.api.malcolm.MalcolmConstants.ATTRIBUTE_NAME_SIMULTANEOUS_AXES;
import static org.eclipse.scanning.api.malcolm.MalcolmConstants.ATTRIBUTE_NAME_STATE;
import static org.eclipse.scanning.api.malcolm.MalcolmConstants.DETECTORS_TABLE_COLUMN_ENABLE;
import static org.eclipse.scanning.api.malcolm.MalcolmConstants.DETECTORS_TABLE_COLUMN_EXPOSURE;
import static org.eclipse.scanning.api.malcolm.MalcolmConstants.DETECTORS_TABLE_COLUMN_FRAMES_PER_STEP;
import static org.eclipse.scanning.api.malcolm.MalcolmConstants.DETECTORS_TABLE_COLUMN_MRI;
import static org.eclipse.scanning.api.malcolm.MalcolmConstants.DETECTORS_TABLE_COLUMN_NAME;
import static org.eclipse.scanning.api.malcolm.MalcolmConstants.FIELD_NAME_AXES_TO_MOVE;
import static org.eclipse.scanning.api.malcolm.MalcolmConstants.FIELD_NAME_DETECTORS;
import static org.eclipse.scanning.api.malcolm.MalcolmConstants.FIELD_NAME_META;
import static org.eclipse.scanning.api.malcolm.connector.IMalcolmConnection.ERROR_MESSAGE_PREFIX_FAILED_TO_CONNECT;

import java.nio.file.Paths;
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
import java.util.function.Function;

import org.eclipse.scanning.api.ValidationException;
import org.eclipse.scanning.api.annotation.scan.ScanFinally;
import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.device.models.IMalcolmDetectorModel;
import org.eclipse.scanning.api.device.models.IMalcolmModel;
import org.eclipse.scanning.api.device.models.MalcolmDetectorModel;
import org.eclipse.scanning.api.device.models.MalcolmModel;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.malcolm.MalcolmConstants;
import org.eclipse.scanning.api.malcolm.MalcolmDetectorInfo;
import org.eclipse.scanning.api.malcolm.MalcolmDeviceException;
import org.eclipse.scanning.api.malcolm.MalcolmTable;
import org.eclipse.scanning.api.malcolm.MalcolmVersion;
import org.eclipse.scanning.api.malcolm.attributes.ChoiceAttribute;
import org.eclipse.scanning.api.malcolm.attributes.IDeviceAttribute;
import org.eclipse.scanning.api.malcolm.attributes.MalcolmAttribute;
import org.eclipse.scanning.api.malcolm.attributes.NumberAttribute;
import org.eclipse.scanning.api.malcolm.attributes.StringArrayAttribute;
import org.eclipse.scanning.api.malcolm.attributes.StringAttribute;
import org.eclipse.scanning.api.malcolm.connector.IMalcolmConnection;
import org.eclipse.scanning.api.malcolm.connector.IMalcolmConnection.IMalcolmConnectionEventListener;
import org.eclipse.scanning.api.malcolm.connector.IMalcolmConnection.IMalcolmConnectionStateListener;
import org.eclipse.scanning.api.malcolm.connector.IMalcolmMessageGenerator;
import org.eclipse.scanning.api.malcolm.connector.MalcolmMethod;
import org.eclipse.scanning.api.malcolm.connector.MalcolmMethodMeta;
import org.eclipse.scanning.api.malcolm.event.MalcolmEvent;
import org.eclipse.scanning.api.malcolm.message.MalcolmMessage;
import org.eclipse.scanning.api.malcolm.message.Type;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.points.models.StaticModel;
import org.eclipse.scanning.api.scan.ScanningException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Object that make the connection to the device and monitors its status.
 *
 * @author Matthew Gerring
 */
public class MalcolmDevice extends AbstractMalcolmDevice {

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
				logger.error("Could not subscribe to malcolm state changes for device ''{}''", getName());
			}
		}

		/**
		 * Handle a change in the connection state of this device. Event is sent by the communications layer.
		 *
		 * @param connected <code>true</code> if the device has changed to being connected, <code>false</code> if
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

	// Constants, note: these constants are here instead of MalcolmConstants as they are not required outside of this class and tests
	public static final String ATTRIBUTE_NAME_COMPLETED_STEPS = "completedSteps";
	public static final String FILE_EXTENSION_H5 = "h5";
	public static final String STANDARD_MALCOLM_ERROR_STR = "Error from Malcolm Device Connection: ";

	// Frequencies and Timeouts
	// broadcast every 250 milliseconds
	public static final long POSITION_COMPLETE_INTERVAL = Long.getLong("org.eclipse.scanning.malcolm.core.positionCompleteInterval", 250);

	private static final String DUMMY_OUTPUT_DIR = System.getProperty("user.dir"); // any existing directory would work, malcolm just validates it exists

	// the data-type map of the MalcolmTable describing the detectors controlled by this malcolm device
	private LinkedHashMap<String, Class<?>> detectorsTableTypesMap = null;

//	// TODO: We currently get the detector table type map from MalcolmTable returned by malcolm. This is
//	// because the 'enable' column is only present for malcolm v4.2 onwards. Once all malcolm devices have
//  // been upgraded this table should be made static with the using the static initializer commented out below
//	static {
//		DETECTORS_TABLE_TYPES_MAP = new LinkedHashMap<>();
//		DETECTORS_TABLE_TYPES_MAP.put(DETECTORS_TABLE_COLUMN_ENABLE, Boolean.class);
//		DETECTORS_TABLE_TYPES_MAP.put(DETECTORS_TABLE_COLUMN_NAME, String.class);
//		DETECTORS_TABLE_TYPES_MAP.put(DETECTORS_TABLE_COLUMN_MRI, String.class);
//		DETECTORS_TABLE_TYPES_MAP.put(DETECTORS_TABLE_COLUMN_EXPOSURE, Double.class);
//		DETECTORS_TABLE_TYPES_MAP.put(DETECTORS_TABLE_COLUMN_FRAMES_PER_STEP, Integer.class);
//	}

	private static IPointGenerator<?> dummyPointGenerator = null;

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

	// The malcolm detector infos describing controlled by this malcolm device
	private LinkedHashMap<String, MalcolmDetectorInfo> detectorInfos = null;

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

			stateSubscribeMessage = messageGenerator.createSubscribeMessage(ATTRIBUTE_NAME_STATE);
			subscribe(stateSubscribeMessage, stateChangeListener);

			scanSubscribeMessage = messageGenerator.createSubscribeMessage(ATTRIBUTE_NAME_COMPLETED_STEPS);
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
		logger.debug("Received malcolm message on endpoint {}, message: {}", ATTRIBUTE_NAME_COMPLETED_STEPS, message);

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
			logger.debug("Received malcolm message on endpoint {}, message: {}", ATTRIBUTE_NAME_STATE, message);

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
		final ChoiceAttribute choiceAttr = getEndpointValue(ATTRIBUTE_NAME_STATE);
		return DeviceState.valueOf(choiceAttr.getValue().toUpperCase());
	}

	@Override
	public IMalcolmModel getModel() {
		final MalcolmModel model = (MalcolmModel) super.getModel();
		if (detectorInfos == null) {
			// initialize the detector infos, and use those
			detectorInfos = fetchMalcolmDetectorInfos();
			if (model.getDetectorModels() == null) {
				model.setDetectorModels(detectorInfos.values().stream().map(this::detectorInfoToModel).collect(toList()));
			}
		}
		return model;
	}

	private LinkedHashMap<String, MalcolmDetectorInfo> fetchMalcolmDetectorInfos() {
		try {
			final MalcolmMethodMeta configureMethodMeta = getEndpointValue(MalcolmMethod.CONFIGURE.toString());
			final Map<String, Object> configureDefaults = configureMethodMeta.getDefaults();
			final MalcolmTable defaultDetectorsTable = (MalcolmTable) configureDefaults.get(FIELD_NAME_DETECTORS);
			if (detectorsTableTypesMap == null) {
				detectorsTableTypesMap = defaultDetectorsTable.getTableDataTypes();
			}
			return defaultDetectorsTable.stream().map(this::malcolmTableRowToDetectorInfo).collect(
					toMap(MalcolmDetectorInfo::getName, Function.identity(), (x, y) -> x, LinkedHashMap::new));
		} catch (MalcolmDeviceException e) {
			logger.error("Error getting malcolm detectors", e);
			return new LinkedHashMap<>();
		}
	}

	private MalcolmDetectorInfo malcolmTableRowToDetectorInfo(Map<String, Object> tableRow) {
		final MalcolmDetectorInfo info = new MalcolmDetectorInfo();

		// TODO the enable column is only added for malcolm 4.2, so we need to deal with it not being present
//		final boolean enabled = (Boolean) row.get(DETECTORS_TABLE_COLUMN_ENABLE),
		final Boolean enabledWrapper = (Boolean) tableRow.get(DETECTORS_TABLE_COLUMN_ENABLE);
		final boolean enabled = enabledWrapper == null || enabledWrapper.booleanValue();
		info.setEnabled(enabled);
		info.setId((String) tableRow.get(DETECTORS_TABLE_COLUMN_MRI));
		info.setName((String) tableRow.get(DETECTORS_TABLE_COLUMN_NAME));
		info.setFramesPerStep((Integer) tableRow.get(DETECTORS_TABLE_COLUMN_FRAMES_PER_STEP));
		info.setExposureTime((Double) tableRow.get(DETECTORS_TABLE_COLUMN_EXPOSURE));

		return info;
	}

	private IMalcolmDetectorModel detectorInfoToModel(MalcolmDetectorInfo detectorInfo) {
		final IMalcolmDetectorModel detectorModel = new MalcolmDetectorModel();
		detectorModel.setEnabled(detectorInfo.isEnabled());
		detectorModel.setName(detectorInfo.getName());
		detectorModel.setExposureTime(detectorInfo.getExposureTime());
		detectorModel.setFramesPerStep(detectorInfo.getFramesPerStep());

		return detectorModel;
	}

	@Override
	public List<MalcolmDetectorInfo> getDetectorInfos() throws MalcolmDeviceException {
		detectorInfos = fetchMalcolmDetectorInfos();
		return detectorInfos.values().stream().collect(toList());
	}

	private MalcolmTable detectorModelsToTable(List<IMalcolmDetectorModel> detectorModels) throws MalcolmDeviceException {
		if (detectorInfos == null) { // ensure the cached map of detector mri maps is non-null
			detectorInfos = fetchMalcolmDetectorInfos();
		}

		// create a list for each property of the model, corresponding to a table column
		final int numDetectors = detectorModels.size();
		final List<String> names = new ArrayList<>(numDetectors);
		final List<String> mris = new ArrayList<>(numDetectors);
		final List<Double> exposures = new ArrayList<>(numDetectors);
		final List<Integer> framesPerSteps = new ArrayList<>(numDetectors);
		final List<Boolean> enablements = new ArrayList<>(numDetectors);

		// iterate through the models populating the lists
		for (IMalcolmDetectorModel detectorModel : detectorModels) {
			names.add(detectorModel.getName());
			if (!detectorInfos.containsKey(detectorModel.getName()))
				throw new MalcolmDeviceException("No such detector '" + detectorModel.getName() + "' for malcolm device '" + getName() + "'");
			mris.add(detectorInfos.get(detectorModel.getName()).getId());
			exposures.add(detectorModel.getExposureTime());
			framesPerSteps.add(detectorModel.getFramesPerStep());
			enablements.add(detectorModel.isEnabled());
		}

		// build the table data map
		final LinkedHashMap<String, List<?>> tableData = new LinkedHashMap<>();
		tableData.put(DETECTORS_TABLE_COLUMN_NAME, names);
		tableData.put(DETECTORS_TABLE_COLUMN_MRI, mris);
		tableData.put(DETECTORS_TABLE_COLUMN_EXPOSURE, exposures);
		tableData.put(DETECTORS_TABLE_COLUMN_FRAMES_PER_STEP, framesPerSteps);
		if (detectorsTableTypesMap.containsKey(DETECTORS_TABLE_COLUMN_ENABLE)) {
			tableData.put(DETECTORS_TABLE_COLUMN_ENABLE, enablements); // enable column only present for malcolm v4.2 and above
		}

		return new MalcolmTable(tableData, detectorsTableTypesMap);
	}

	@Override
	public String getDeviceHealth() throws MalcolmDeviceException {
		final StringAttribute attribute = getEndpointValue(ATTRIBUTE_NAME_HEALTH);
		return attribute.getValue();
	}

	@Override
	public boolean isDeviceBusy() throws MalcolmDeviceException {
		return !getDeviceState().isRestState();
	}

	@Override
	public void validate(IMalcolmModel model) throws ValidationException {
		logger.debug("validate() called");
		validateWithReturn(model);
	}

	@Override
	public IMalcolmModel validateWithReturn(IMalcolmModel model) throws ValidationException {
		if (Boolean.getBoolean("org.eclipse.scanning.malcolm.skipvalidation")) {
			logger.warn("Skipping Malcolm Validate");
			return null;
		}

		MalcolmMessage reply = null;
		try {
			final EpicsMalcolmModel epicsModel = createEpicsMalcolmModel(model, true); // use default point gen and filedir if not set (i.e. we're not in a scan)
			final MalcolmMessage msg = messageGenerator.createCallMessage(MalcolmMethod.VALIDATE, epicsModel);
			reply = send(msg, Timeout.STANDARD.toMillis());
			if (reply.getType()==Type.ERROR) {
				throw new ValidationException(STANDARD_MALCOLM_ERROR_STR + reply.getMessage());
			}

		} catch (Exception mde) {
			throw new ValidationException(mde);
		}

		// the returned value is a map with the same key names as the fields as the EpicsMalcolmModel
		// malcolm may have modified the scan point generator and/or the detector models
		@SuppressWarnings("unchecked")
		final Map<String, Object> result = (Map<String, Object>) reply.getValue();
		return validateResultToMalcolmModel(model, result);
	}

	private IMalcolmModel validateResultToMalcolmModel(IMalcolmModel model, Map<String, Object> validateResult) {
		final MalcolmTable modifiedDetectorsMalcolmTable = (MalcolmTable) validateResult.get(FIELD_NAME_DETECTORS);
		final List<IMalcolmDetectorModel> detectorModels = modifiedDetectorsMalcolmTable.stream()
				.map(this::detectorRowToMalcolmDetectorModel).collect(toList());
		@SuppressWarnings("unchecked")
		final List<String> axesToMove = (List<String>) validateResult.get(FIELD_NAME_AXES_TO_MOVE);

		final MalcolmModel newModel = new MalcolmModel();
		newModel.setName(model.getName()); // get the name and timeout from the original model, they aren't changed
		newModel.setTimeout(model.getTimeout());
		newModel.setExposureTime(model.getExposureTime()); // TODO get from the scan point generator
		newModel.setAxesToMove(axesToMove);
		newModel.setDetectorModels(detectorModels);

		return newModel;
	}

	private MalcolmDetectorModel detectorRowToMalcolmDetectorModel(Map<String, Object> row) {
		// TODO the enable column is only added for malcolm 4.2, so we need it not being present
		// final boolean enabled = (Boolean) row.get(DETECTORS_TABLE_COLUMN_ENABLE),
		final Boolean enabledWrapper = (Boolean) row.get(DETECTORS_TABLE_COLUMN_ENABLE);
		final boolean enabled = enabledWrapper == null || enabledWrapper.booleanValue();

		final String name = (String) row.get(DETECTORS_TABLE_COLUMN_NAME);
		final double exposureTime = (Double) row.get(DETECTORS_TABLE_COLUMN_EXPOSURE);
		final int framesPerStep = (Integer) row.get(DETECTORS_TABLE_COLUMN_FRAMES_PER_STEP);
		return new MalcolmDetectorModel(name, exposureTime, framesPerStep, enabled);
	}

	@Override
	public void configure(IMalcolmModel model) throws MalcolmDeviceException {
		logger.debug("configure() called");

		// Abort and/or reset the device before configure in case it's in a fault state
		goToReadyState();

		final EpicsMalcolmModel epicsModel = createEpicsMalcolmModel(model, false);
		final MalcolmMessage msg = messageGenerator.createCallMessage(MalcolmMethod.CONFIGURE, epicsModel);
		MalcolmMessage reply = wrap(()->send(msg, Timeout.CONFIG.toMillis()));
		if (reply.getType() == Type.ERROR) {
			throw new MalcolmDeviceException(STANDARD_MALCOLM_ERROR_STR + reply.getMessage());
		}
		setModel(model);
	}

	@SuppressWarnings("unchecked")
	private <T> T getEndpointValue(String endpointName) throws MalcolmDeviceException {
		logger.debug("Getting endpoint value {}", endpointName);
		final MalcolmMessage reply = getEndpointReply(endpointName);
		if (reply.getType()==Type.ERROR) {
			throw new MalcolmDeviceException(STANDARD_MALCOLM_ERROR_STR + reply.getMessage());
		}

		return (T) reply.getValue();
	}

	private MalcolmMessage getEndpointReply(String endpointName) throws MalcolmDeviceException {
		final MalcolmMessage message = messageGenerator.createGetMessage(endpointName);
		return wrap(()->send(message, Timeout.STANDARD.toMillis()));
	}

	/**
	 * Returns the default point generator. This is sent to the actual malcolm device when
	 * validate is called outside of a scan, so that the rest of the model can be validated.
	 *
	 * Note that this must be created lazily instead of in a static initializer as
	 * we need the get the point generator service from the service holder,
	 * which may not have been set at before this class is initialized.
	 *
	 * @return the dummy point generator
	 */
	public static IPointGenerator<?> getDummyPointGenerator() {
		if (dummyPointGenerator == null && Services.getPointGeneratorService() != null) {
			try {
				dummyPointGenerator = Services.getPointGeneratorService().createCompoundGenerator(new CompoundModel(new StaticModel()));
			} catch (GeneratorException e) {
				logger.error("Could not generate default point generator", e);
			}
		}

		return dummyPointGenerator;
	}

	/**
	 * Create the {@link EpicsMalcolmModel} passed to the actual malcolm device. This is created from both the
	 * {@link IMalcolmModel} that this {@link MalcolmDevice} has been configured with and information about
	 * the scan, e.g. the scan path defined by the point generator, that have been set on this object.
	 * @param model the IMalcolmModel to use
	 * @param useDefaults <code>true</code> to use a default point generator, <code>false</code> otherwise
	 * @return
	 */
	private EpicsMalcolmModel createEpicsMalcolmModel(IMalcolmModel model, boolean useDefaults) throws MalcolmDeviceException {
		double exposureTime = model.getExposureTime();

		// set the exposure time and mutators in the points generator
		IPointGenerator<?> pointGen = this.pointGenerator;
		if (pointGen == null && useDefaults) {
			pointGen = getDummyPointGenerator();
		}
		if (pointGen != null) {
			((CompoundModel) pointGen.getModel()).setMutators(Collections.emptyList());
			((CompoundModel) pointGen.getModel()).setDuration(exposureTime);
		}

		// set the file template and output dir
		String fileTemplate = null;
		String outputDir = this.outputDir;
		if (outputDir == null && useDefaults) {
			outputDir = DUMMY_OUTPUT_DIR;
		}

		if (outputDir != null) {
			fileTemplate = Paths.get(outputDir).getFileName().toString() + "-%s." + FILE_EXTENSION_H5;
		}

		// get the axes to move
		List<String> axesToMove = model.getAxesToMove();
		if (axesToMove == null && pointGen != null) {
			final List<String> scannableNames = pointGen.getNames();
			final List<String> availableAxes = getAvailableAxes();
			int i = scannableNames.size() - 1;
			while (i >= 0 && availableAxes.contains(scannableNames.get(i))) {
				i--;
			}
			// i is now the index of the first non-malcolm axis, or -1 if all axes malcolm controlled
			axesToMove = scannableNames.subList(i + 1, scannableNames.size());
		}

		// convert the detector models to a MalcolmTable
		final MalcolmTable detectorTable = detectorModelsToTable(model.getDetectorModels());

		// create the EpicsMalcolmModel with all the arguments that malcolm's configure and validate methods require
		return new EpicsMalcolmModel(outputDir, fileTemplate, axesToMove, pointGen, detectorTable);
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
		logger.debug("Calling '{}' method on malcolm device", method);
		final MalcolmMessage reply = callWithTimeout(()->call(method), timeout);
		logger.debug("Received reply from malcolm device '{}' method: {}", method, reply);
		return reply;
	}

	private MalcolmMessage call(MalcolmMethod method) throws MalcolmDeviceException {
		final MalcolmMessage message = messageGenerator.createCallMessage(method);
		return malcolmConnection.send(this, message);
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
		seekParameters.put(ATTRIBUTE_NAME_COMPLETED_STEPS, stepNumber);
		final MalcolmMessage msg = messageGenerator.createCallMessage(MalcolmMethod.PAUSE, seekParameters);
		final MalcolmMessage reply = wrap(()->send(msg, Timeout.CONFIG.toMillis()));
		if (reply.getType()==Type.ERROR) {
			throw new MalcolmDeviceException(STANDARD_MALCOLM_ERROR_STR + reply.getMessage());
		}
	}

	/**
	 * This method should leave the Malcolm device in a READY state.
	 *
	 * First we call abort(), which—if successful—will put Malcolm in an ABORTED state.
	 * Then we call reset() which will put Malcolm in a READY state.
	 *
	 * Some states are unabortable, and abort() will throw exception. However,
	 * those states are resettable, so we can swallow the first exception and trust that
	 * reset() will work.
	 *
	 * If reset() throws exception, something is wrong.
	 *
	 * @throws MalcolmDeviceException
	 */
	private void goToReadyState() throws MalcolmDeviceException {
		try {
			abort();
		} catch (Exception e) {
			logger.warn("Error aborting malcolm device '{}'. This is normal depending on the current state", getName());
		}

		reset();
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
	public MalcolmVersion getVersion() throws MalcolmDeviceException {
//		getModel(); // call get model to ensure detectorsTableTypesMap has been initialized
//		if (detectorsTableTypesMap.containsKey(DETECTORS_TABLE_COLUMN_ENABLE)) { // TODO remove these lines ***********
//			return MalcolmVersion.VERSION_4_2;
//		}
//		return MalcolmVersion.VERSION_4_0;

		// The version of a malcolm device can be retrieved from the tags of the 'meta' attribute
		final StringArrayAttribute value = getEndpointValue(FIELD_NAME_META);
		final String[] tags = value.getTags();
		if (tags.length == 0) {
			return MalcolmVersion.VERSION_4_0; // last version without a way to get the version
		}

		return MalcolmVersion.fromVersionString(tags[0]);
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
		final IDeviceAttribute<String[]> axesAttribute = getAttribute(ATTRIBUTE_NAME_SIMULTANEOUS_AXES);
		return Arrays.asList(axesAttribute.getValue());
	}

	@Override
	public MalcolmTable getDatasets() throws MalcolmDeviceException {
		return getAttributeValue(MalcolmConstants.ATTRIBUTE_NAME_DATASETS);
	}

	/**
	 * At the end of a scan, Malcolm remains in a FINISHED state
	 * until reset() is called. Only at this point will Malcolm
	 * close the file.
	 */
	@ScanFinally
	public void closeFile() throws MalcolmDeviceException {
		if (getDeviceState() == DeviceState.FINISHED) { // TODO remove check when all beamlines are on Malcolm 4+
			reset();
		}
	}

	/**
	 * Instances of {@link EpicsMalcolmModel} are used to configure the actual malcolm device.
	 * This class should be distinguished from the {@link IMalcolmModel} that this
	 * class is configured with. The majority of the information contained in
	 * an {@link EpicsMalcolmModel} describes the scan, e.g. the scan path
	 * defined by the {@link IPointGenerator} and the directory to write to.
	 * <p>
	 * Note: the field names within this class are as required by malcolm.
	 * They cannot be changed without breaking their deserialization by malcolm.
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

		/**
		 * A {@link MalcolmTable} to configure the detectors controlled by malcolm.
		 * The column headings are 'name', 'mri', 'exposure' and 'framesPerStep'.
		 */
		private final MalcolmTable detectors;

		public EpicsMalcolmModel(String fileDir, String fileTemplate,
				List<String> axesToMove, IPointGenerator<?> generator, MalcolmTable detectors) {
			this.fileDir = fileDir;
			this.fileTemplate = fileTemplate;
			this.axesToMove = axesToMove;
			this.generator = generator;
			this.detectors = detectors;
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

		public MalcolmTable getDetectors() {
			return detectors;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((axesToMove == null) ? 0 : axesToMove.hashCode());
			result = prime * result + ((fileDir == null) ? 0 : fileDir.hashCode());
			result = prime * result + ((fileTemplate == null) ? 0 : fileTemplate.hashCode());
			result = prime * result + ((generator == null) ? 0 : generator.hashCode());
			result = prime * result + ((detectors == null) ? 0 : detectors.hashCode());
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
			if (detectors == null)
				if (other.detectors != null)
					return false;
			return true;
		}

	}

}
