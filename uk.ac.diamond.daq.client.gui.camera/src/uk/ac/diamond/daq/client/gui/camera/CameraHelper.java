package uk.ac.diamond.daq.client.gui.camera;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.device.DeviceException;
import gda.factory.Finder;
import uk.ac.diamond.daq.client.gui.camera.controller.AbstractCameraConfigurationController;
import uk.ac.diamond.daq.client.gui.camera.controller.ImagingCameraConfigurationController;
import uk.ac.gda.client.live.stream.view.CameraConfiguration;

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
 * server.cameraControl.0=imaging_camera_control
 * 
 * client.cameraConfiguration.1=d2_cam_config
 * client.cameraConfiguration.name.1=Camera One
 * server.cameraControl.1=imaging_camera_control
 * </code>
 * 
 * @author Maurizio Nagni
 *
 */
public final class CameraHelper {

	private static final Logger logger = LoggerFactory.getLogger(CameraHelper.class);
	private final static Map<Integer, AbstractCameraConfigurationController> cameraControllers = new HashMap<>();

	/**
	 * Allows the regex below to be extended to any number followed by any other
	 * format
	 */
	private static final String CAMERA_REGEX_EXTENSION = "\\.%s(\\..*)?";
	/**
	 * The prefix used in the property files to identify a camera configuration.
	 */
	public static final String CAMERA_CONFIGURATION_PREFIX = "client.cameraConfiguration";
	public static final String CAMERA_CONFIGURATION_REGEX = CAMERA_CONFIGURATION_PREFIX + CAMERA_REGEX_EXTENSION;
	/**
	 * The prefix used in the property files to identify a camera control.
	 */
	public static final String CAMERA_CONTROL_PREFIX = "server.cameraControl";
	public static final String CAMERA_CONTROL_REGEX = CAMERA_CONTROL_PREFIX + CAMERA_REGEX_EXTENSION;

	private CameraHelper() {
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
	public static CameraConfiguration getCameraConfiguration(String cameraPropertyName) {
		return Finder.getInstance().find(cameraPropertyName);
	}
	
	public static CameraConfiguration getCameraConfiguration(CameraComboItem cameraItem) {
		return getCameraConfiguration(getCameraConfigurationInstance(cameraItem.getIndex()));
	}
	
	public static CameraConfiguration getCameraConfiguration(int cameraIndex) {
		return getCameraConfiguration(getCameraConfigurationInstance(cameraIndex));
	}

	public static String getCameraConfigurationInstance(int cameraIndex) {
		String cameraKey = LocalProperties.getFirstKeyByRegexp(
				String.format(CAMERA_CONFIGURATION_REGEX, Integer.toString(cameraIndex)), CAMERA_CONFIGURATION_PREFIX);
		return LocalProperties.get(cameraKey);
	}
	
	/**
	 * Returns a {@link AbstractCameraConfigurationController} based on a camera
	 * index
	 * 
	 * @param activeCamera the camera index
	 * @return the camera or <code>null</code> if the camera does not exists
	 */
	public static AbstractCameraConfigurationController getCameraControlInstance(int activeCamera) {
		if (!cameraControllers.containsKey(activeCamera)) {
			ImagingCameraConfigurationController controller;
			try {
				controller = new ImagingCameraConfigurationController(getCameraControllerBeanID(activeCamera));
				cameraControllers.put(activeCamera, controller);
			} catch (DeviceException e) {
				logger.error("Error", e);
			}
		}
		return cameraControllers.get(activeCamera);
	}

	public static List<CameraComboItem> getCameraComboItems() {
		List<CameraComboItem> items = new ArrayList<>();
		List<String> confKey = getCameraConfigurationKeys();
		confKey.stream().forEach(k -> {
			final int index = Integer.parseInt(k.substring(k.lastIndexOf('.') + 1));
			final String name = getCameraConfigurationName(index);
			items.add(new CameraComboItem(name, index));
		});
		return items;
	}	
	
	public static List<String> getCameraControlKeys() {
		return LocalProperties.getKeysByRegexp(CAMERA_CONTROL_PREFIX + "\\.\\d");
	}

	public static List<String> getCameraConfigurationKeys() {
		return LocalProperties.getKeysByRegexp(CAMERA_CONFIGURATION_PREFIX + "\\.\\d");
	}
	
	public static String getCameraConfigurationName(int index) {
		String confName = LocalProperties.getKeysByRegexp(CAMERA_CONFIGURATION_PREFIX + ".*\\.name\\." + index).get(0);
		return LocalProperties.get(confName);
	}
	
	/**
	 * Returns the value of the cameraController property associated with an index
	 * 
	 * @param cameraIndex
	 * @return
	 */
	private static String getCameraControllerBeanID(int cameraIndex) {
		String cameraKey = LocalProperties.getFirstKeyByRegexp(
				String.format(CAMERA_CONTROL_REGEX, Integer.toString(cameraIndex)), CAMERA_CONTROL_PREFIX);
		return LocalProperties.get(cameraKey);
	}
}
