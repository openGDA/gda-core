package uk.ac.diamond.daq.client.gui.camera;

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
import uk.ac.gda.client.live.stream.IConnectionFactory;
import uk.ac.gda.client.live.stream.LiveStreamConnection;
import uk.ac.gda.client.live.stream.view.CameraConfiguration;
import uk.ac.gda.client.live.stream.view.StreamType;

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
	 * Uses {@link IConnectionFactory} to retrieve the {@link LiveStreamConnection}
	 * associated with a given camera. The camera configuration is retrieved using
	 * {@link #getCameraConfiguration(String)}.
	 * 
	 * @param cameraPropertyName the property name (in
	 *                           xx-config/properties/{mode}/anyName.properties
	 *                           file) whose value is the bean name of a findable
	 *                           {@link CameraConfiguration}
	 * @return the live stream associated with the camera configuration
	 */
	public static LiveStreamConnection getLiveStreamConnection(String cameraPropertyName, final StreamType streamType) {
		return IConnectionFactory.getLiveStreamConnection(getCameraConfiguration(cameraPropertyName), streamType);
	}

	public static LiveStreamConnection getLiveStreamConnection(int cameraIndex, final StreamType streamType) {
		return IConnectionFactory.getLiveStreamConnection(getCameraConfiguration(cameraIndex), streamType);
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
		String cameraName = LocalProperties.get(cameraPropertyName);
		return Finder.getInstance().find(cameraName);
	}

	public static CameraConfiguration getCameraConfiguration(int cameraIndex) {
		String cameraKey = LocalProperties.getFirstKeyByRegexp(
				String.format(CAMERA_CONFIGURATION_REGEX, Integer.toString(cameraIndex)), CAMERA_CONFIGURATION_PREFIX);
		return getCameraConfiguration(cameraKey);
	}

	/**
	 * Returns the value of the cameraController property associated with an index
	 * 
	 * @param cameraIndex
	 * @return
	 */
	public static String getCameraControllerBeanID(int cameraIndex) {
		String cameraKey = LocalProperties.getFirstKeyByRegexp(
				String.format(CAMERA_CONTROL_REGEX, Integer.toString(cameraIndex)), CAMERA_CONTROL_PREFIX);
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

	public List<String> getCameraControlKeys() {
		return LocalProperties.getKeysByRegexp(CAMERA_CONTROL_PREFIX + ".*");
	}

	public List<String> getCameraConfigurationKeys() {
		return LocalProperties.getKeysByRegexp(CAMERA_CONFIGURATION_PREFIX + ".*");
	}
}
