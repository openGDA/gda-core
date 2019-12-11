package uk.ac.diamond.daq.client.gui.camera;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.device.DeviceException;
import uk.ac.diamond.daq.client.gui.camera.controller.AbstractCameraConfigurationController;
import uk.ac.diamond.daq.client.gui.camera.controller.ImagingCameraConfigurationController;
import uk.ac.diamond.daq.client.gui.camera.properties.CameraPropertiesBuilder;
import uk.ac.diamond.daq.client.gui.camera.properties.MotorPropertiesBuilder;
import uk.ac.gda.client.composites.FinderHelper;
import uk.ac.gda.client.live.stream.LiveStreamException;
import uk.ac.gda.client.live.stream.view.CameraConfiguration;
import uk.ac.gda.client.properties.CameraProperties;
import uk.ac.gda.client.properties.MotorProperties;
import uk.ac.gda.ui.tool.ClientMessages;
import uk.ac.gda.ui.tool.ClientMessagesUtility;

/**
 * Hides the configuration structural design. A typical configuration defining
 * two camera would look like below
 * 
 * <code>
 * client.cameraConfiguration.0=d2_cam_config
 * client.cameraConfiguration.0.name=Camera Zero
 * client.cameraConfiguration.0.cameraControl=imaging_camera_control
 * client.cameraConfiguration.0.motor.0.controller = stagez
 * client.cameraConfiguration.0.motor.0.name = Camera Axis Z
 * client.cameraConfiguration.0.motor.1.controller = stagey
 * client.cameraConfiguration.0.motor.1.name = Camera Axis Y 
 * client.cameraControl.0=imaging_camera_control 
 * </code>
 * 
 * where
 * 
 * <ul>
 * <li>client.cameraConfiguration.INDEX - represents the camera index</li>
 * <li>motor.INDEX - represents a motor index</li>
 * </ul>
 * 
 * @author Maurizio Nagni
 *
 */
public final class CameraHelper {

	private static final Logger logger = LoggerFactory.getLogger(CameraHelper.class);
	private static final Map<Integer, AbstractCameraConfigurationController> cameraControllers = new HashMap<>();
	private static final List<CameraProperties> cameraProperties = new ArrayList<>();
	private static final List<CameraComboItem> cameraComboItems = new ArrayList<>();

	static {
		parseCameraProperties();
		createCameraComboItems();
	}

	/**
	 * The prefix used in the property files to identify a camera configuration.
	 */
	private static final String CAMERA_CONFIGURATION_PREFIX = "client.cameraConfiguration";

	private static final String CAMERA_PROPERTY_FORMAT = "%s.%s.%s";
	private static final String CAMERA_SIMPLE_FORMAT = "%s.%s";

	private CameraHelper() {
	}

	public static CameraConfiguration getCameraConfiguration(final CameraComboItem cameraItem)
			throws LiveStreamException {
		return getCameraConfiguration(cameraItem.getIndex())
				.orElseThrow(() -> new LiveStreamException("No Camera Confguration found"));
	}

	public static Optional<CameraConfiguration> getCameraConfiguration(int cameraIndex) {
		return FinderHelper.getFindableDevice(getCameraConfigurationInstance(cameraIndex));
	}

	/**
	 * Returns the {@link AbstractCameraConfigurationController} based on a camera
	 * index
	 * 
	 * @param activeCamera the camera index
	 * @return the camera or <code>null</code> if the camera does not exists
	 */
	public static AbstractCameraConfigurationController getCameraControlInstance(int activeCamera) {
		if (!cameraControllers.containsKey(activeCamera) && activeCamera < getAllCameraProperties().size()) {
			ImagingCameraConfigurationController controller;
			try {
				controller = new ImagingCameraConfigurationController(
						getAllCameraProperties().get(activeCamera).getCameraControl());
				cameraControllers.put(activeCamera, controller);
			} catch (DeviceException e) {
				logger.error("Error", e);
			}
		}
		return cameraControllers.get(activeCamera);
	}

	public static List<CameraComboItem> getCameraComboItems() {
		return Collections.unmodifiableList(cameraComboItems);
	}

	public static List<CameraProperties> getAllCameraProperties() {
		return Collections.unmodifiableList(cameraProperties);
	}

	public static CameraProperties getCameraProperties(int cameraIndex) {
		return cameraProperties.get(cameraIndex);
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

	private static void parseCameraProperties(int index) {
		CameraPropertiesBuilder builder = new CameraPropertiesBuilder();
		builder.setIndex(index);
		builder.setName(getCameraNameProperty(index));
		builder.setCameraConfiguration(getCameraConfigurationProperty(index));
		builder.setCameraControl(getCameraControlProperty(index));

		builder.setMotorProperties(getCameraConfigurationMotors(index));
		cameraProperties.add(builder.build());
	}

	private static List<MotorProperties> getCameraConfigurationMotors(int index) {
		return IntStream.range(0, 10).filter(motorIndex -> {
			return getCameraConfigurationMotorControllerProperty(motorIndex, index) != null;
		}).mapToObj(motorIndex -> {
			MotorPropertiesBuilder motorBuilder = new MotorPropertiesBuilder();
			motorBuilder.setName(getCameraConfigurationMotorNameProperty(motorIndex, index));
			motorBuilder.setController(getCameraConfigurationMotorControllerProperty(motorIndex, index));
			return motorBuilder.build();
		}).collect(Collectors.toList());
	}

	//
	/**
	 * Extracts properties formatted like "client.cameraConfiguration.INDEX"
	 * 
	 * @param index the camera index
	 * @return
	 */
	private static String getCameraConfigurationProperty(int index) {
		return LocalProperties.get(String.format(CAMERA_SIMPLE_FORMAT, CAMERA_CONFIGURATION_PREFIX, index), null);
	}

	/**
	 * Extracts properties formatted like
	 * "client.cameraConfiguration.INDEX.cameraControl"
	 * 
	 * @param index the camera index
	 * @return
	 */
	private static String getCameraControlProperty(int index) {
		return LocalProperties.get(formatPropertyKey(CAMERA_CONFIGURATION_PREFIX, index, "cameraControl"), null);
	}

	/**
	 * Extracts properties formatted like "client.cameraConfiguration.INDEX.name"
	 * 
	 * @param index the camera index
	 * @return
	 */
	private static String getCameraNameProperty(int index) {
		return LocalProperties.get(formatPropertyKey(CAMERA_CONFIGURATION_PREFIX, index, "name"),
				ClientMessagesUtility.getMessage(ClientMessages.NOT_AVAILABLE));
	}

	// 
	/**
	 * Assemble a string formatted like something like "PREFIX.INDEX.PROPERTY"
	 * 
	 * @param prefix a string identifying a property  
	 * @param index an index identifying the prefix index
	 * @param property a subproperty of the prefix
	 * @return
	 */
	private static String formatPropertyKey(String prefix, int index, String property) {
		return String.format(CAMERA_PROPERTY_FORMAT, prefix, index, property);
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
		return String.format(CAMERA_SIMPLE_FORMAT, formatPropertyKey(CAMERA_CONFIGURATION_PREFIX, cameraIndex, "motor"),
				motorIndex);
	}

	private static String formatMotorPropertyKey(int cameraIndex, int motorIndex, String property) {
		return String.format(CAMERA_SIMPLE_FORMAT, formatMotorProperty(cameraIndex, motorIndex), property);
	}

	private static String getCameraConfigurationMotorNameProperty(int cameraIndex, int motorIndex) {
		return LocalProperties.get(formatMotorPropertyKey(cameraIndex, motorIndex, "name"),
				ClientMessagesUtility.getMessage(ClientMessages.NOT_AVAILABLE));
	}

	private static String getCameraConfigurationMotorControllerProperty(int cameraIndex, int motorIndex) {
		return LocalProperties.get(formatMotorPropertyKey(cameraIndex, motorIndex, "controller"), null);
	}
}
