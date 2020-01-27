package uk.ac.diamond.daq.client.gui.camera;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.device.DeviceException;
import gda.factory.Finder;
import uk.ac.diamond.daq.client.gui.camera.controller.AbstractCameraConfigurationController;
import uk.ac.diamond.daq.client.gui.camera.controller.ImagingCameraConfigurationController;
import uk.ac.diamond.daq.client.gui.camera.properties.CameraPropertiesBuilder;
import uk.ac.diamond.daq.client.gui.camera.properties.MotorPropertiesBuilder;
import uk.ac.gda.client.live.stream.view.CameraConfiguration;
import uk.ac.gda.client.live.stream.view.StreamType;
import uk.ac.gda.client.properties.CameraProperties;
import uk.ac.gda.client.properties.MotorProperties;

/**
 * Hides the configuration structural design. In the property file are listed
 * pairs of
 * 
 * <ol>
 * <li>{@link #CAMERA_CONFIGURATION_PREFIX}.aNumber</li>
 * <li>{@link #CAMERA_CONTROL_PREFIX}.aNumber</li>
 * </ol>
 * 
 * which identify the beans defining a camera property.
 * 
 * A typical configuration defining two camera would look like below
 * 
 * <code>
 * client.cameraConfiguration.0=d2_cam_config
 * client.cameraConfiguration.name.0=Camera Zero
 * client.cameraConfiguration.motor.0.controller.0 = stagez
 * client.cameraConfiguration.motor.0.name.0 = Camera Axis Z
 * client.cameraControl.0=imaging_camera_control
 * 
 * client.cameraConfiguration.1=d2_cam_config_another
 * client.cameraConfiguration.name.1=Camera One
 * client.cameraConfiguration.motor.0.controller.1 = stagey
 * client.cameraConfiguration.motor.0.name.1 = Camera Axis Y
 * client.cameraControl.1=imaging_camera_control
 * </code>
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
	public static final String CAMERA_CONFIGURATION_PREFIX = "client.cameraConfiguration";

	/**
	 * The prefix used in the property files to identify a camera control.
	 */
	public static final String CAMERA_CONTROL_PREFIX = "client.cameraControl";

	private static final String CAMERA_PROPERTY_FORMAT = "%s.%s";
	private static final String MOTOR_PROPERTY_FORMAT = CAMERA_CONFIGURATION_PREFIX + ".motor.%s";

	private CameraHelper() {
	}

	public static CameraConfiguration getCameraConfiguration(CameraComboItem cameraItem) {
		return getCameraConfiguration(getCameraConfigurationInstance(cameraItem.getIndex()));
	}

	public static CameraConfiguration getCameraConfiguration(int cameraIndex) {
		return getCameraConfiguration(getCameraConfigurationInstance(cameraIndex));
	}

	/**
	 * Returns a {@link AbstractCameraConfigurationController} based on a camera
	 * index
	 * 
	 * @param activeCamera the camera index
	 * @return the camera or <code>null</code> if the camera does not exists
	 */
	public static AbstractCameraConfigurationController getCameraControlInstance(int activeCamera) {
		if (!cameraControllers.containsKey(activeCamera) && activeCamera < getCameraProperties().size()) {
			ImagingCameraConfigurationController controller;
			try {
				controller = new ImagingCameraConfigurationController(
						getCameraProperties().get(activeCamera).getCameraControl());
				cameraControllers.put(activeCamera, controller);
			} catch (DeviceException e) {
				logger.error("Error", e);
			}
		}
		return cameraControllers.get(activeCamera);
	}

	public static List<StreamType> getCameraStreamTypes(int cameraIndex) {
		return getCameraConfiguration(cameraIndex).cameraStreamTypes();
	}
	
	public static List<CameraComboItem> getCameraComboItems() {
		return Collections.unmodifiableList(cameraComboItems);
	}

	public static List<CameraProperties> getCameraProperties() {
		return Collections.unmodifiableList(cameraProperties);
	}

	private static void createCameraComboItems() {
		getCameraProperties().stream().forEach(k -> cameraComboItems.add(new CameraComboItem(k)));
	}

	/**
	 * Retrieves the {@link CameraConfiguration} associated with a given camera.
	 * 
	 * @param cameraPropertyName the property name (in
	 *                           xx-config/properties/{mode}/anyName.properties
	 *                           file) whose value is the bean name of a findable
	 *                           {@link CameraConfiguration}
	 * @return the camera configuration associated with the camera name
	 */
	private static CameraConfiguration getCameraConfiguration(String cameraPropertyName) {
		return Finder.getInstance().find(cameraPropertyName);
	}

	private static String getCameraConfigurationInstance(int cameraIndex) {
		return getCameraProperties().get(cameraIndex).getCameraConfiguration();
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

	private static String getCameraConfigurationProperty(int index) {
		return LocalProperties.get(formatProperty(CAMERA_CONFIGURATION_PREFIX, index), null);
	}

	private static String getCameraControlProperty(int index) {
		return LocalProperties.get(formatProperty(CAMERA_CONTROL_PREFIX, index), null);
	}

	private static String getCameraNameProperty(int index) {
		return LocalProperties.get(formatProperty(formatPropertyKey(CAMERA_CONFIGURATION_PREFIX, "name"), index), null);
	}

	private static String formatProperty(String prefix, int index) {
		return String.format(CAMERA_PROPERTY_FORMAT, prefix, index);
	}

	private static String formatPropertyKey(String prefix, String property) {
		return String.format(CAMERA_PROPERTY_FORMAT, prefix, property);
	}

	// -- motors -- //
	/**
	 * Returns a string like "client.cameraConfiguration.motor.MOTOR_INDEX"
	 * 
	 * @param motorIndex
	 * @return
	 */
	private static String formatMotorProperty(int motorIndex) {
		return String.format(MOTOR_PROPERTY_FORMAT, motorIndex);
	}

	private static String formatMotorPropertyKey(String property, int motorIndex) {
		return formatPropertyKey(formatMotorProperty(motorIndex), property);
	}

	private static String getCameraConfigurationMotorNameProperty(int motorIndex, int index) {
		return LocalProperties.get(formatProperty(formatMotorPropertyKey("name", motorIndex), index), null);
	}

	private static String getCameraConfigurationMotorControllerProperty(int motorIndex, int index) {
		return LocalProperties.get(formatProperty(formatMotorPropertyKey("controller", motorIndex), index), null);
	}
}
