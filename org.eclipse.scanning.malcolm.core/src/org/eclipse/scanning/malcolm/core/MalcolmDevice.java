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
import static org.eclipse.scanning.api.malcolm.MalcolmConstants.FIELD_NAME_GENERATOR;
import static org.eclipse.scanning.api.malcolm.MalcolmConstants.FIELD_NAME_META;

import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.eclipse.scanning.api.ValidationException;
import org.eclipse.scanning.api.annotation.scan.ScanFinally;
import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.device.models.IMalcolmDetectorModel;
import org.eclipse.scanning.api.device.models.IMalcolmModel;
import org.eclipse.scanning.api.device.models.MalcolmDetectorModel;
import org.eclipse.scanning.api.device.models.MalcolmModel;
import org.eclipse.scanning.api.device.models.SeekStrategy;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.malcolm.MalcolmConstants;
import org.eclipse.scanning.api.malcolm.MalcolmDetectorInfo;
import org.eclipse.scanning.api.malcolm.MalcolmDeviceException;
import org.eclipse.scanning.api.malcolm.MalcolmTable;
import org.eclipse.scanning.api.malcolm.MalcolmVersion;
import org.eclipse.scanning.api.malcolm.attributes.ChoiceAttribute;
import org.eclipse.scanning.api.malcolm.attributes.IDeviceAttribute;
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
import org.eclipse.scanning.api.malcolm.event.MalcolmStateChangedEvent;
import org.eclipse.scanning.api.malcolm.message.MalcolmMessage;
import org.eclipse.scanning.api.malcolm.message.Type;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.points.models.StaticModel;
import org.eclipse.scanning.api.scan.IFilePathService;
import org.eclipse.scanning.api.scan.ScanningException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.osgi.services.ServiceProvider;

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
				logger.error("Could not subscribe to malcolm state changes for device ''{}''", getName(), e);
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
	public static final String ATTRIBUTE_NAME_LAST_GOOD_STEP = "lastGoodStep";
	public static final String FILE_EXTENSION_H5 = "h5";
	public static final String STANDARD_MALCOLM_ERROR_STR = "Error from Malcolm: ";

	// Frequencies and Timeouts
	// broadcast every 250 milliseconds
	public static final long POSITION_COMPLETE_INTERVAL = Long.getLong("org.eclipse.scanning.malcolm.core.positionCompleteInterval", 250);

	// the data-type map of the MalcolmTable describing the detectors controlled by this malcolm device
	private LinkedHashMap<String, Class<?>> detectorsTableTypesMap = null;

	private static boolean resetAfterScan = true;

	private int lastScanPoint = 0;

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

	/*
	 * Requires at least 2 threads: (Run or Configure: blocking), non-blocking status changes
	 * Run blocks while scan is running: potentially as long as 2 days
	 * Configure could take 10 minutes but should never happen simultaneously with Run
	 * Another thread for all other methods, which return quicker (<=15s) and can cause Run/Config to stop
	 *
	 * Timeout left at default (60s), allows same thread to be used for e.g. quick pause/resume,
	 * but will release resources quickly.
	 */
	private ExecutorService executor = Executors.newCachedThreadPool();

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

	// The malcolm detector infos describing detectors controlled by this malcolm device
	private LinkedHashMap<String, MalcolmDetectorInfo> detectorInfos = null;

	/**
	 * A {@link SeekStrategy} can be implemented to calculate the last good point to resume to.
	 * The default will be -1, indicating the the device should return to the last scanned point.
	 */
	private SeekStrategy seekStrategy = point -> -1;

	public MalcolmDevice() {
		this(null, ServiceProvider.getService(IMalcolmConnection.class),
				ServiceProvider.getService(IRunnableDeviceService.class));
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
			lastScanPoint = stepIndex;
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
			logger.error("Could not send scan state change message", e);
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

	/**
	 * Returns the most recent device state from malcolm. This returns
	 * the device state from the most recently state change from the actual
	 * malcolm device. This value returned from this method is the same value that
	 * {@link MalcolmStateChangedEvent#getPreviousState()} will be in the next such event.
	 * @return latest device state of the malcolm device
	 * @throws ScanningException
	 */
	public DeviceState getLatestDeviceState() throws ScanningException {
		return super.getDeviceState();
	}

	/**
	 * Overrides {@link IRunnableDevice#getDeviceState()} to ask the actual
	 * malcolm device for the device state.
	 * @return the device state of the malcolm device
	 */
	@Override
	public DeviceState getDeviceState() throws MalcolmDeviceException {
		final ChoiceAttribute choiceAttr = getEndpointValue(ATTRIBUTE_NAME_STATE);
		return DeviceState.valueOf(choiceAttr.getValue().toUpperCase());
	}

	@Override
	public IMalcolmModel getModel() {
		final MalcolmModel model = (MalcolmModel) super.getModel();
		final List<MalcolmDetectorInfo> detectorInfos = getDetectorInfos();
		if (model.getDetectorModels() == null && detectorInfos != null) {
			// initialize the detector infos and use those to populate the model. This is necessary as
			// we get the detectors from malcolm rather than including them in the spring configuration for the malcolm model
			// The default values for frames per step and exposure time are 1 and 0.0 respectively (malcolm interprets an
			// exposure time of 0.0 to use the maximum possible exposure time calculated from frames per step and step time)
			model.setDetectorModels(detectorInfos.stream().map(this::detectorInfoToModel).collect(toList()));
		}
		return model;
	}

	/**
	 * Fetch the malcolm infos from the actual malcolm device.
	 * @return malcolmInfos keyed by name, using a {@link LinkedHashMap} to maintain insertion order
	 */
	private LinkedHashMap<String, MalcolmDetectorInfo> fetchMalcolmDetectorInfos() {
		try {
			final MalcolmMethodMeta configureMethodMeta = getEndpointValue(MalcolmMethod.CONFIGURE.toString());
			final Map<String, Object> configureDefaults = configureMethodMeta.getDefaults();
			final MalcolmTable defaultDetectorsTable = (MalcolmTable) configureDefaults.get(FIELD_NAME_DETECTORS);
			if (defaultDetectorsTable == null) {
				// malcolm can return a null detectors table if it can't talk to the geobrick, or if it has been configured
				// without detectors. We need to treat this differently than an empty list of detectors
				logger.warn("Malcolm device {} has no detectors configured, it may not be configured correctly.", getName());
				return null;
			}

			// initialize the map of detector columns names to type, if it hasn't already been done
			if (detectorsTableTypesMap == null) {
				detectorsTableTypesMap = defaultDetectorsTable.getTableDataTypes();
			}

			return toDetectorInfosMap(defaultDetectorsTable);
		} catch (MalcolmDeviceException e) {
			logger.error("Error getting malcolm detectors", e);
			return null;
		}
	}

	private LinkedHashMap<String, MalcolmDetectorInfo> toDetectorInfosMap(final MalcolmTable defaultDetectorsTable) {
		// convert each row of the table to a MalcolmDetectorInfo, and build a map keyed by detector names
		return defaultDetectorsTable.stream().map(this::toDetectorInfo).collect(
				toMap(MalcolmDetectorInfo::getName, Function.identity(), (x, y) -> x, LinkedHashMap::new));
	}

	private MalcolmDetectorInfo toDetectorInfo(Map<String, Object> tableRow) {
		final MalcolmDetectorInfo info = new MalcolmDetectorInfo();

		// TODO the enable column is only added for malcolm 4.2, so we need to deal with it not being present
		info.setEnabled((Boolean) tableRow.getOrDefault(DETECTORS_TABLE_COLUMN_ENABLE, true));
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
	public List<MalcolmDetectorInfo> getDetectorInfos() {
		if (detectorInfos == null) {
			detectorInfos = fetchMalcolmDetectorInfos();
		}
		return detectorInfos == null ? null : detectorInfos.values().stream().collect(toList());
	}

	private MalcolmTable detectorModelsToTable(List<IMalcolmDetectorModel> detectorModels) throws MalcolmDeviceException {
		if (detectorInfos == null) { // ensure the cached map of detector mri maps is non-null
			detectorInfos = fetchMalcolmDetectorInfos();
			if (detectorInfos == null) return null; // this can happen if malcolm can't talk to the geobrick
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
	public IMalcolmModel validate(IMalcolmModel model) throws ValidationException {
		logger.debug("validate() called");
		MalcolmMessage reply;
		try {
			final EpicsMalcolmModel epicsModel = createEpicsMalcolmModel(model); // use default point gen and filedir if not set (i.e. we're not in a scan)
			final MalcolmMessage msg = messageGenerator.createCallMessage(MalcolmMethod.VALIDATE, epicsModel);
			reply = sendMessageWithTimeout(msg, Timeout.STANDARD.toMillis());
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
	public void configure(IMalcolmModel model) throws ScanningException {
		logger.debug("configure() called on malcolm device {}", getName());

		// Abort and/or reset the device before configure in case it's in a fault state
		goToReadyState();

		super.configure(model); // sets the model, which we need to do before calling createEpicsMalcolmModel

		final EpicsMalcolmModel epicsModel = createEpicsMalcolmModel(model);
		final MalcolmMessage msg = messageGenerator.createCallMessage(MalcolmMethod.CONFIGURE, epicsModel);
		final MalcolmMessage reply = sendMessageWithTimeout(msg, Timeout.CONFIG.toMillis());

		// use the point generator returned from malcolm
		@SuppressWarnings("unchecked")
		final Map<String, Object> result = (Map<String, Object>) reply.getValue();
		@SuppressWarnings("unchecked")
		final IPointGenerator<CompoundModel> pointGen = (IPointGenerator<CompoundModel>) result.get(FIELD_NAME_GENERATOR);

		// update the configured scan model to use the new point generator
		// TODO: the reason we update the point generator only in the scan model, and not the pointGenerator field
		// directly is that currently ScanProcess (indirectly) calls validate on this malcolm device after calling
		// configure, and we can't validate the point generator returned from malcolm, so pointGenerator holds on
		// to the original one. The pointGenerator field can be removed when this is fixed. See DAQ-2707.
		if (pointGen != null && scanModel != null) scanModel.setPointGenerator(pointGen);

		updateDetectorInfos((MalcolmTable) result.get(FIELD_NAME_DETECTORS));

		lastScanPoint = 0;
		seekStrategy.configure();
	}

	private void updateDetectorInfos(MalcolmTable detectorsTable) {
		final LinkedHashMap<String, MalcolmDetectorInfo> detInfos = toDetectorInfosMap(detectorsTable);

		if (detectorInfos != null) {
			final List<String> previousMris = detectorInfos.values().stream().map(MalcolmDetectorInfo::getId).collect(toList());
			final List<String> newMris = detInfos.values().stream().map(MalcolmDetectorInfo::getId).collect(toList());
			if (!previousMris.equals(newMris)) {
				logger.error("Malcolm detectors not as expected, expected MRIS {}, was {}", previousMris, newMris);
			}
		}

		detectorInfos = detInfos;
	}

	@SuppressWarnings("unchecked")
	private <T> T getEndpointValue(String endpointName) throws MalcolmDeviceException {
		logger.debug("Getting endpoint value {}", endpointName);
		return (T) getEndpointReply(endpointName).getValue();
	}

	private MalcolmMessage getEndpointReply(String endpointName) throws MalcolmDeviceException {
		final MalcolmMessage message = messageGenerator.createGetMessage(endpointName);
		return sendMessageWithTimeout(message, Timeout.STANDARD.toMillis());
	}

	/**
	 * Create the {@link EpicsMalcolmModel} passed to the actual malcolm device. This is created from both the
	 * {@link IMalcolmModel} that this {@link MalcolmDevice} has been configured with and information about
	 * the scan, e.g. the scan path defined by the point generator, that have been set on this object.
	 * <p>
	 * If a point generator or output directory have not been set then the dummy point generator and output
	 * directory will be used instead. This is necessary as sometimes it is necessary to call validate or
	 * configure outside of a scan, for example it is necessary to call
	 * @param model the IMalcolmModel to use
	 * @return
	 * @throws ScanningException
	 */
	private EpicsMalcolmModel createEpicsMalcolmModel(IMalcolmModel model) throws ScanningException {
		// get the point generator for the scan, with the duration set
		this.pointGenerator = createPointGenerator(model);

		// set the file template and output dir
		final String outputDir = this.outputDir == null ? ServiceProvider.getService(IFilePathService.class).getTempDir() : this.outputDir;
		final String fileTemplate = Paths.get(outputDir).getFileName().toString() + "-%s." + FILE_EXTENSION_H5;

		// convert the detector models to a MalcolmTable
		final MalcolmTable detectorTable = detectorModelsToTable(model.getDetectorModels());

		// create the EpicsMalcolmModel with all the arguments that malcolm's configure and validate methods require
		final int[] breakpoints = getBreakpoints();
		final List<String> axesToMove = getConfiguredAxes(model);
		return new EpicsMalcolmModel(outputDir, fileTemplate, axesToMove, pointGenerator, detectorTable, breakpoints);
	}

	/**
	 * Creates the point generator for the scan:<ul>
	 * <li>If this malcolm device has been configured with a point generator as part of being configured for a scan by
	 * {@link #configureScan(org.eclipse.scanning.api.scan.models.ScanModel)}, then modify that point generator by
	 * setting its duration to the exposure time of the malcolm model.</li>
	 * <li>If this malcolm device is not configured with a scan, create a generator from a CompoundModel containing
	 * just a static model</li>
	 * </ul>
	 *
	 * @param model
	 * @return
	 * @throws GeneratorException
	 */
	private IPointGenerator<CompoundModel> createPointGenerator(IMalcolmModel model) throws MalcolmDeviceException {
		// set the exposure time and mutators in the points generator
		final CompoundModel compoundModel = this.pointGenerator == null ?
				new CompoundModel(new StaticModel()) :
				(CompoundModel) pointGenerator.getModel();
		compoundModel.setDuration(model.getExposureTime());
		compoundModel.setMutators(Collections.emptyList());
		try {
			return ServiceProvider.getService(IPointGeneratorService.class).createCompoundGenerator(compoundModel);
		} catch (GeneratorException e) {
			throw new MalcolmDeviceException("Could not create point generator.");
		}
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

	/**
	 * Sends a message to a MalcolmDevice and wraps any Exceptions in a MalcolmDeviceException
	 */
	private MalcolmMessage sendMessageWithTimeout(MalcolmMessage message, long timeout) throws MalcolmDeviceException {
		try {
			logger.debug("Sending message to malcolm device: {}", message);

			final Future<MalcolmMessage> malcolmReplyFuture = executor.submit(() -> malcolmConnection.send(this, message));

			// Calling a malcolm method is not cancellable, so we ignore interrupts until malcolm has returned.
			// To do this we need to retry in a loop. Note, malcolm has only one long-running method, RUN. To cause this to
			// return, call abort() on this MalcolmDevice.
			boolean callCompleted = false;
			MalcolmMessage reply = null;
			InterruptedException interruptedException = null;
			do {
				try {
					reply = malcolmReplyFuture.get(timeout, TimeUnit.MILLISECONDS);
					callCompleted = true;
				} catch (InterruptedException e) {
					logger.warn("Ignored interrupt waiting for reply to message, retrying: {}", message);
					interruptedException = e;
				}
			} while (!callCompleted);

			if (interruptedException != null) {
				// set the interrupted flag on this thread. This is preferable to throwing an exception as that may give the
				// impression that the call was interrupted. It wasn't, because we ignore it.
				Thread.currentThread().interrupt();
			}

			logger.debug("Received reply from malcolm device: {}", reply);
			exceptionOnError(message, reply);
			return reply;
		} catch (MalcolmDeviceException e) {
			throw e;
		} catch (Exception other) {
			throw new MalcolmDeviceException(this, other);
		}
	}

	private void exceptionOnError(MalcolmMessage message, MalcolmMessage reply) throws MalcolmDeviceException {
		if (reply == null) {
			// the real malcolm device should never return a null reply, but Mockito tests will if
			// Mockito.when has not been used to configure the mock to return a reply for a particular message
			throw new MalcolmDeviceException(STANDARD_MALCOLM_ERROR_STR + " got null reply for message: " + message);
		} else if (reply.getType()==Type.ERROR) {
			throw new MalcolmDeviceException(STANDARD_MALCOLM_ERROR_STR + reply.getMessage() + "\n\tfor message: " + message);
		}
	}

	private MalcolmMessage callMethodWithTimeout(MalcolmMethod method, long timeout) throws MalcolmDeviceException{
		return sendMessageWithTimeout(messageGenerator.createCallMessage(method), timeout);
	}

	@Override
	public void run(IPosition pos) throws MalcolmDeviceException {
		logger.debug("run() called with position {}", pos);
		callMethodWithTimeout(MalcolmMethod.RUN, Timeout.RUN.toMillis());
	}

	@Override
	public void seek(int stepNumber) throws MalcolmDeviceException {
		logger.debug("seek() called with step number {}", stepNumber);
		LinkedHashMap<String, Integer> seekParameters = new LinkedHashMap<>(); // TODO why is this a linked hash map
		seekParameters.put(ATTRIBUTE_NAME_LAST_GOOD_STEP, stepNumber);
		final MalcolmMessage msg = messageGenerator.createCallMessage(MalcolmMethod.PAUSE, seekParameters);
		sendMessageWithTimeout(msg, Timeout.CONFIG.toMillis());
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
		callMethodWithTimeout(MalcolmMethod.ABORT, Timeout.STANDARD.toMillis());
	}

	@Override
	public void disable() throws MalcolmDeviceException {
		logger.debug("disable() called");
		callMethodWithTimeout(MalcolmMethod.DISABLE, Timeout.STANDARD.toMillis());
	}

	@Override
	public void reset() throws MalcolmDeviceException {
		logger.debug("reset() called");
		callMethodWithTimeout(MalcolmMethod.RESET, Timeout.STANDARD.toMillis());
	}

	/**
	 * Pauses the device. The implementation delegates to #seek, resulting in a PAUSE
	 * command to Malcolm. The default {@link SeekStrategy} instructs Malcolm to eventually
	 * resume from the last scan point.
	 */
	@Override
	public void pause() throws MalcolmDeviceException {
		logger.debug("pause() called");
		seek(seekStrategy.getPointToSeek(lastScanPoint));
	}

	@Override
	public void resume() throws MalcolmDeviceException {
		logger.debug("resume() called");
		callMethodWithTimeout(MalcolmMethod.RESUME, Timeout.STANDARD.toMillis());
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

	private <T> IDeviceAttribute<T> getAttribute(String attributeName) throws MalcolmDeviceException {
		logger.debug("getAttribute() called with attribute name {}", attributeName);
		final MalcolmMessage reply = getEndpointReply(attributeName);
		if (reply.getType() != Type.ERROR) {
			// found the attribute ok, return it
			@SuppressWarnings("unchecked") // temp variable to avoid annotation on method
			IDeviceAttribute<T> result = (IDeviceAttribute<T>) reply.getValue();
			return result;
		}
		// check if the error message is a connection failure, in this case throw an exception
		throw new MalcolmDeviceException(STANDARD_MALCOLM_ERROR_STR + reply.getMessage());
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

	public static void setResetAfterScan(boolean resetAfterScan) {
		logger.info("Malcolm Devices {} be reset at the end of a scan.", (resetAfterScan ? "will" : "will not"));
		MalcolmDevice.resetAfterScan = resetAfterScan;
	}

	public SeekStrategy getSeekStrategy() {
		return seekStrategy;
	}

	/**
	 * It this is set, it will calculate the scan point the device should resume from based
	 * on the last scan point and other device parameters. Otherwise, the default value is -1,
	 * indicating the device should resume from the last scanned position.
	 */
	public void setSeekStrategy(SeekStrategy strategy) {
		  this.seekStrategy = strategy;
	}

	/**
	 * At the end of a scan, Malcolm remains in a FINISHED state
	 * until reset() is called. Only at this point will Malcolm
	 * close the file.
	 */
	@ScanFinally
	public void closeFile() throws MalcolmDeviceException {
		if (resetAfterScan) {
			reset();
		}
	}

}
