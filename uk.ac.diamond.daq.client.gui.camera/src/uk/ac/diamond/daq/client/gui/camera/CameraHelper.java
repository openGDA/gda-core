/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.client.gui.camera;

import static uk.ac.gda.client.properties.ClientPropertiesHelper.SIMPLE_FORMAT;
import static uk.ac.gda.client.properties.ClientPropertiesHelper.formatPropertyKey;
import static uk.ac.gda.client.properties.ClientPropertiesHelper.getConfigurationBeanProperty;
import static uk.ac.gda.client.properties.ClientPropertiesHelper.getId;
import static uk.ac.gda.client.properties.ClientPropertiesHelper.getNameProperty;
import static uk.ac.gda.client.properties.ClientPropertiesHelper.getProperty;
import static uk.ac.gda.client.properties.ClientPropertiesHelper.getStringArrayProperty;
import static uk.ac.gda.core.tool.spring.SpringApplicationContextFacade.publishEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.device.DeviceException;
import uk.ac.diamond.daq.client.gui.camera.beam.BeamCameraMap;
import uk.ac.diamond.daq.client.gui.camera.event.BeamCameraMappingEvent;
import uk.ac.diamond.daq.client.gui.camera.event.CameraControlSpringEvent;
import uk.ac.diamond.daq.client.gui.camera.event.CameraEventUtils;
import uk.ac.diamond.daq.client.gui.camera.monitor.CameraAvailabilityMonitor;
import uk.ac.diamond.daq.client.gui.camera.properties.CameraPropertiesBuilder;
import uk.ac.diamond.daq.client.gui.camera.properties.MotorPropertiesBuilder;
import uk.ac.gda.api.camera.CameraControl;
import uk.ac.gda.api.camera.CameraControllerEvent;
import uk.ac.gda.client.exception.GDAClientException;
import uk.ac.gda.client.live.stream.LiveStreamException;
import uk.ac.gda.client.live.stream.view.CameraConfiguration;
import uk.ac.gda.client.live.stream.view.StreamType;
import uk.ac.gda.client.properties.CameraProperties;
import uk.ac.gda.client.properties.MotorProperties;
import uk.ac.gda.core.tool.spring.SpringApplicationContextFacade;
import uk.ac.gda.ui.tool.ClientMessages;
import uk.ac.gda.ui.tool.ClientMessagesUtility;
import uk.ac.gda.ui.tool.spring.FinderService;

/**
 * Hides the configuration structural design. A typical configuration defining a
 * camera would look like below
 *
 * <pre>
 * {@code
 * client.cameraConfiguration.0=d2_cam_config
 * client.cameraConfiguration.0.name=Camera Zero
 * client.cameraConfiguration.0.id=PCO_CAMERA
 * client.cameraConfiguration.0.cameraControl=imaging_camera_control
 * client.cameraConfiguration.0.beam_mapping_active=true
 * client.cameraConfiguration.0.readoutTime=true
 * client.cameraConfiguration.0.motor.0.controller = stagez
 * client.cameraConfiguration.0.motor.0.name = Camera Axis Z
 * client.cameraConfiguration.0.motor.1.controller = stagey
 * client.cameraConfiguration.0.motor.1.name = Camera Axis Y
 * }
 * </pre>
 *
 * where the fields meaning represent
 *
 * <ul>
 * <li><i>client.cameraConfiguration.INDEX</i>
 * <ul>
 * <li>the camera index</li>
 * </ul>
 * </li>
 * <li><i>name</i>
 * <ul>
 * <li>a label used in the GUI for this camera</li>
 * </ul>
 * </li>
 * <li><i>id</i>
 * <ul>
 * <li>an optional unique (among other similar caeraConfiguration ids) to
 * identify a specific cameraConfiguration</li>
 * </ul>
 * </li>
 * <li><i>cameraControl</i>
 * <ul>
 * <li>the server side camera control</li>
 * <li>the value, i.e. imaging_camera_control, is a bean id, instance of
 * uk.ac.gda.epics.camera.EpicsCameraControl, defined in the GDA Server Spring
 * Configuration (TBD)</li>
 * </ul>
 * </li>
 * <li><i>beam_mapping_active</i>
 * <ul>
 * <li>marks this camera as eligible for beam-to-camera mapping</li>
 * </ul>
 * </li>
 * <li><i>motor.INDEX</i>
 * <ul>
 * <li>the top element identifying a motor associated with this camera</li>
 * </ul>
 * </li>
 * </ul>
 *
 * <p>
 * As this class is aware of all the available cameras, it easily intercept all {@link CameraControllerEvent}
 * from the existing {@link CameraControl}s and republish them in the Spring context as {@link CameraControlSpringEvent}
 * so any object interested in a {@code CameraControlEvent} property can use spring to be notified when a new
 * {@link CameraControlSpringEvent} is published.
 * </p>
 *
 * <p>
 * For mor information read in Confluence about the <a href=
 * "https://confluence.diamond.ac.uk/display/DIAD/K11+GDA+Properties">Camera
 * Configuration Properties</a>
 * </p>
 *
 * @author Maurizio Nagni
 *
 */
public final class CameraHelper {

	private static final String READOUT_TIME = "readoutTime";

	private static final Logger logger = LoggerFactory.getLogger(CameraHelper.class);

	private static final List<CameraProperties> cameraProperties = new ArrayList<>();
	private static final List<String> cameraMonitors = new ArrayList<>();
	private static final Map<String, CameraProperties> cameraPropertiesByID = new HashMap<>();
	private static final List<CameraComboItem> cameraComboItems = new ArrayList<>();

	private static final Map<Integer, ICameraConfiguration> cameraConfigurations = new HashMap<>();

	static {
		loadAllProperties();
	}

	/**
	 * The prefix used in the property files to identify a camera configuration.
	 */
	private static final String CAMERA_CONFIGURATION_PREFIX = "client.cameraConfiguration";

	/**
	 * The prefix used in the properties file to identify which cameras are associated with a monitor button.
	 */
	private static final String CAMERA_MONITORS_PREFIX = "client.cameraMonitors";

	private CameraHelper() {
	}

	public static CameraConfiguration getCameraConfiguration(final CameraComboItem cameraItem)
			throws LiveStreamException {
		return getCameraConfiguration(cameraItem.getIndex())
				.orElseThrow(() -> new LiveStreamException("No Camera Confguration found"));
	}

	public static Optional<CameraConfiguration> getCameraConfiguration(int cameraIndex) {
		return createICameraConfiguration(cameraIndex).getCameraConfiguration();
	}

	public static Optional<CameraControl> getCameraControl(int cameraIndex) {
		return createICameraConfiguration(cameraIndex).getCameraControl();
	}

	/**
	 * Returns the available {@link StreamType}s for a specific camera
	 *
	 * @param cameraIndex the camera index
	 * @return the available stream types, eventually {@link Optional#empty()} if
	 *         the camera is missing
	 */
	public static Optional<List<StreamType>> getCameraStreamTypes(int cameraIndex) {
		Optional<CameraConfiguration> cc = getCameraConfiguration(cameraIndex);
		if (cc.isPresent()) {
			return Optional.ofNullable(cc.get().cameraStreamTypes());
		}
		return Optional.empty();

	}

	public static List<CameraComboItem> getCameraComboItems() {
		return Collections.unmodifiableList(cameraComboItems);
	}

	public static List<CameraProperties> getAllCameraProperties() {
		return Collections.unmodifiableList(cameraProperties);
	}

	/**
	 * Returns the IDs of the camera requiring a camera monitoring button
	 * @return a list of camera IDs
	 */
	public static List<String> getCameraMonitors() {
		return Collections.unmodifiableList(cameraMonitors);
	}

	public static CameraProperties getCameraProperties(int cameraIndex) {
		return cameraProperties.get(cameraIndex);
	}

	public static Optional<CameraProperties> getCameraPropertiesByID(String id) {
		return Optional.ofNullable(cameraPropertiesByID.get(id));
	}

	/**
	 * Returns the default camera properties. The actual implementation returns the
	 * first camera as default but this should change in future.
	 *
	 * @return the camera properties, otherwise <code>null</code>
	 */
	public static CameraProperties getDefaultCameraProperties() {
		if (cameraProperties.isEmpty()) {
			return null;
		}
		return cameraProperties.get(0);
	}

	public static ICameraConfiguration createICameraConfiguration(int cameraIndex) {
		return cameraConfigurations.computeIfAbsent(cameraIndex, ICameraConfigurationImpl::new);
	}

	/**
	 * Adds a mapping between the motors moving the beam, if any, and the camera
	 * array. The method publishes a {@link BeamCameraMappingEvent} to inform any
	 * listener of the camera update.
	 *
	 * @param cameraIndex   the camera index
	 * @param beamCameraMap the camera to beam mapping
	 */
	public static void addBeamCameraMap(int cameraIndex, BeamCameraMap beamCameraMap) {
		ICameraConfigurationImpl.class.cast(createICameraConfiguration(cameraIndex)).setBeamCameraMap(beamCameraMap);
		// resets the status to mark the end of the mapping
		BeamCameraMappingEvent cmEvent = new BeamCameraMappingEvent(CameraHelper.class, cameraIndex);
		publishEvent(cmEvent);
	}

	private static void createCameraComboItems() {
		getAllCameraProperties().stream().forEach(k -> cameraComboItems.add(new CameraComboItem(k)));
	}

	private static String getCameraConfigurationInstance(int cameraIndex) {
		return getCameraProperties(cameraIndex).getCameraConfiguration();
	}

	private static List<String> getCameraConfigurationKeys() {
		return LocalProperties.getKeysByRegexp(CAMERA_CONFIGURATION_PREFIX + "\\.\\d");
	}

	private static void parseCameraProperties() {
		IntStream.range(0, getCameraConfigurationKeys().size()).forEach(CameraHelper::parseCameraProperties);
		cameraProperties.sort((c1, c2) -> Integer.compare(c1.getIndex(), c2.getIndex()));
	}

	private static void parseCameraMonitors() {
		cameraMonitors.addAll(getMonitoredCameras());
	}

	private static void observeCameraProperties() {
		cameraProperties.stream()
			.map(CameraProperties::getIndex)
			.map(CameraHelper::getCameraControl)
			.forEach(cc -> cc.ifPresent(CameraEventUtils::addIObserverToCameraControl));
	}

	private static void monitorCameraAvailability() {
		new CameraAvailabilityMonitor();
	}

	private static void removeObserverCameraProperties() {
		cameraProperties.stream()
			.map(CameraProperties::getIndex)
			.map(CameraHelper::getCameraControl)
			.forEach(cc -> cc.ifPresent(CameraEventUtils::removeIObserverFromCameraControl));
	}

	private static void parseCameraProperties(int index) {
		CameraPropertiesBuilder builder = new CameraPropertiesBuilder();
		builder.setIndex(index);
		builder.setId(getId(CAMERA_CONFIGURATION_PREFIX, index));
		builder.setName(getNameProperty(CAMERA_CONFIGURATION_PREFIX, index));
		builder.setCameraConfiguration(getConfigurationBeanProperty(CAMERA_CONFIGURATION_PREFIX, index));
		builder.setCameraControl(getCameraControlProperty(index));
		builder.setBeamMappingActive(getBeamMappingProperty(index));
		builder.setPixelBinningEditable(getPixelBinningEditableProperty(index));
		builder.setReadoutTime(getReadoutTimeProperty(index));

		builder.setMotorProperties(getCameraConfigurationMotors(index));
		CameraProperties cp = builder.build();
		cp.getId().ifPresent(id -> cameraPropertiesByID.putIfAbsent(id, cp));
		cameraProperties.add(cp);
	}

	private static List<MotorProperties> getCameraConfigurationMotors(int index) {
		return IntStream.range(0, 10).filter(motorIndex -> {
			return getCameraConfigurationMotorControllerProperty(index, motorIndex) != null;
		}).mapToObj(motorIndex -> {
			MotorPropertiesBuilder motorBuilder = new MotorPropertiesBuilder();
			motorBuilder.setName(getCameraConfigurationMotorNameProperty(index, motorIndex));
			motorBuilder.setController(getCameraConfigurationMotorControllerProperty(index, motorIndex));
			return motorBuilder.build();
		}).collect(Collectors.toList());
	}

	/**
	 * Extracts properties formatted like
	 * "client.cameraConfiguration.INDEX.cameraControl"
	 *
	 * @param index the camera index
	 * @return
	 */
	private static String getCameraControlProperty(int index) {
		return getProperty(CAMERA_CONFIGURATION_PREFIX, index, "cameraControl", null);
	}

	/**
	 * Extracts properties formatted like
	 * "client.cameraConfiguration.INDEX.beam_mapping_active"
	 *
	 * @param index the camera index
	 * @return
	 */
	private static boolean getBeamMappingProperty(int index) {
		return Boolean.parseBoolean(getProperty(CAMERA_CONFIGURATION_PREFIX, index, "beam_mapping_active", "false"));
	}

	/**
	 * Extracts properties formatted like
	 * "client.cameraConfiguration.INDEX.pixelBinningEditable"
	 *
	 * @param index the camera index
	 * @return
	 */
	private static boolean getPixelBinningEditableProperty(int index) {
		return Boolean.parseBoolean(getProperty(CAMERA_CONFIGURATION_PREFIX, index, "pixelBinningEditable", "false"));
	}

	/**
	 * Extracts properties formatted like
	 * "client.cameraConfiguration.INDEX.monitored"
	 *
	 * @param index the camera index
	 * @return
	 */
	private static List<String> getMonitoredCameras() {
		return Arrays.asList(getStringArrayProperty(CAMERA_MONITORS_PREFIX));
	}

	/**
	 * Extracts properties formatted like
	 * "client.cameraConfiguration.INDEX.readoutTime"
	 *
	 * @param index the camera index
	 * @return the camera readout time. Default 0.0;
	 */
	private static double getReadoutTimeProperty(int index) {
		try {
			logger.debug("Reading property {}.{}.{}", CAMERA_CONFIGURATION_PREFIX, index, READOUT_TIME);
			return Double.parseDouble(getProperty(CAMERA_CONFIGURATION_PREFIX, index, READOUT_TIME, "0.0"));
		} catch (NumberFormatException e) {
			logger.warn("Error reading property {}.{}.{}. Uses default to 0.0 ", CAMERA_CONFIGURATION_PREFIX, index, READOUT_TIME);
			return 0;
		}
	}

	// -- motors -- //
	/**
	 * Returns a string like
	 * "client.cameraConfiguration.CAMERA_INDEX.motor.MOTOR_INDEX"
	 *
	 * @param motorIndex
	 * @return
	 */
	private static String formatMotorProperty(int cameraIndex, int motorIndex) {
		return String.format(SIMPLE_FORMAT, formatPropertyKey(CAMERA_CONFIGURATION_PREFIX, cameraIndex, "motor"),
				motorIndex);
	}

	private static String formatMotorPropertyKey(int cameraIndex, int motorIndex, String property) {
		return String.format(SIMPLE_FORMAT, formatMotorProperty(cameraIndex, motorIndex), property);
	}

	private static String getCameraConfigurationMotorNameProperty(int cameraIndex, int motorIndex) {
		return LocalProperties.get(formatMotorPropertyKey(cameraIndex, motorIndex, "name"),
				ClientMessagesUtility.getMessage(ClientMessages.NOT_AVAILABLE));
	}

	private static String getCameraConfigurationMotorControllerProperty(int cameraIndex, int motorIndex) {
		return LocalProperties.get(formatMotorPropertyKey(cameraIndex, motorIndex, "controller"), null);
	}

	public static final Optional<CameraProperties> getCameraPropertiesByCameraControl(CameraControl cameraControl) {
		return cameraProperties.stream()
			.filter(p -> p.getCameraControl().equals(cameraControl.getName()))
			.findFirst();
	}

	private static class ICameraConfigurationImpl implements ICameraConfiguration {

		private final int cameraIndex;
		/**
		 * The mapping from camera array space to the beam drivers, if any
		 */
		private Optional<BeamCameraMap> beamCameraMap;

		public ICameraConfigurationImpl(int cameraIndex) {
			super();
			this.cameraIndex = cameraIndex;
		}

		@Override
		public Optional<CameraConfiguration> getCameraConfiguration() {
			return getCameraConfiguration(cameraIndex);
		}

		@Override
		public int getCameraIndex() {
			return cameraIndex;
		}

		@Override
		public Optional<CameraControl> getCameraControl() {
			return getCameraControl(cameraIndex);
		}

		@Override
		public RectangularROI getMaximumSizedROI() throws GDAClientException {
			int[] frameSize;
			try {
				frameSize = getCameraControl().orElseThrow(() -> new GDAClientException("No Camera Control defined"))
						.getFrameSize();
			} catch (DeviceException e) {
				throw new GDAClientException("Error", e);
			}
			RectangularROI max = new RectangularROI();
			max.setPoint(0, 0);
			max.setEndPoint(frameSize);
			return max;
		}

		@Override
		public CameraProperties getCameraProperties() {
			return getAllCameraProperties().get(cameraIndex);
		}

		@Override
		public Optional<BeamCameraMap> getBeamCameraMap() {
			return beamCameraMap;
		}

		public void setBeamCameraMap(BeamCameraMap beamCameraMap) {
			this.beamCameraMap = Optional.ofNullable(beamCameraMap);
		}

		private Optional<CameraControl> getCameraControl(int cameraIndex) {
			String findableName = getCameraControlProperty(cameraIndex);
			if (findableName != null) {
				return getCameraConfiguration(findableName, CameraControl.class);
			}
			return Optional.empty();
		}

		public static Optional<CameraConfiguration> getCameraConfiguration(int cameraIndex) {
			String findableName = getCameraConfigurationInstance(cameraIndex);
			if (findableName != null) {
				return getCameraConfiguration(findableName, CameraConfiguration.class);
			}
			return Optional.empty();
		}

		private static <T> Optional<T> getCameraConfiguration(String findableName, Class<T> clazz) {
			return Optional.ofNullable(getFinderService())
					.map(f -> f.getFindableObject(findableName))
					.filter(Optional::isPresent)
					.map(Optional::get)
					.map(clazz::cast);
		}

		private static FinderService getFinderService() {
			return SpringApplicationContextFacade.getBean(FinderService.class);
		}
	}

	/**
	 * Loads the cameras configurations.
	 * <p>
	 * This method
	 * <ul>
	 * <li>
	 * is called by the class automatically the first time any method is called
	 * </li>
	 * <li>
	 * can be called multiple time to reload the cameras configurations
	 * </li>
	 * </ul>
	 */
	public static final void loadAllProperties() {
		removeObserverCameraProperties();
		cameraProperties.clear();
		cameraMonitors.clear();
		cameraPropertiesByID.clear();
		cameraConfigurations.clear();
		cameraComboItems.clear();

		parseCameraProperties();
		parseCameraMonitors();
		observeCameraProperties();
		createCameraComboItems();
		monitorCameraAvailability();

	}
}