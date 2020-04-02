package uk.ac.diamond.daq.client.gui.camera;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;

import gda.configuration.properties.LocalProperties;
import gda.device.DeviceException;
import uk.ac.diamond.daq.client.gui.camera.controller.AbstractCameraConfigurationController;
import uk.ac.diamond.daq.client.gui.camera.controller.ImagingCameraConfigurationController;
import uk.ac.diamond.daq.client.gui.camera.properties.CameraPropertiesBuilder;
import uk.ac.diamond.daq.client.gui.camera.properties.MotorPropertiesBuilder;
import uk.ac.gda.api.camera.CameraControl;
import uk.ac.gda.client.composites.FinderHelper;
import uk.ac.gda.client.exception.GDAClientException;
import uk.ac.gda.client.live.stream.LiveStreamException;
import uk.ac.gda.client.live.stream.view.CameraConfiguration;
import uk.ac.gda.client.live.stream.view.StreamType;
import uk.ac.gda.client.properties.CameraProperties;
import uk.ac.gda.client.properties.MotorProperties;
import uk.ac.gda.ui.tool.ClientMessages;
import uk.ac.gda.ui.tool.ClientMessagesUtility;

import static uk.ac.gda.client.properties.ClientPropertiesHelper.*;

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

	private static final Map<Integer, AbstractCameraConfigurationController> cameraControllers = new HashMap<>();
	private static final List<CameraProperties> cameraProperties = new ArrayList<>();
	private static final Map<String, CameraProperties> cameraPropertiesByID = new HashMap<>();
	private static final List<CameraComboItem> cameraComboItems = new ArrayList<>();

	private static final Map<Integer, ICameraConfiguration> cameraConfigurations = new HashMap<>();

	static {
		parseCameraProperties();
		createCameraComboItems();
	}

	/**
	 * The prefix used in the property files to identify a camera configuration.
	 */
	private static final String CAMERA_CONFIGURATION_PREFIX = "client.cameraConfiguration";

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

	public static Optional<CameraControl> getCameraControl(int cameraIndex) {
		return FinderHelper.getFindableDevice(getCameraControlProperty(cameraIndex));
	}

	/**
	 * Returns the {@link AbstractCameraConfigurationController} based on a camera
	 * index
	 * 
	 * @param activeCamera the camera index
	 * @return the camera or <code>null</code> if the camera does not exists
	 */
	public static Optional<AbstractCameraConfigurationController> getCameraControlInstance(int activeCamera) {
		if (!cameraConfigurations.containsKey(activeCamera) && activeCamera < getAllCameraProperties().size()) {
			cameraControllers.put(activeCamera, new ImagingCameraConfigurationController(
					getAllCameraProperties().get(activeCamera).getCameraControl()));
		}
		return Optional.ofNullable(cameraControllers.get(activeCamera));
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
		builder.setId(getId(CAMERA_CONFIGURATION_PREFIX, index));
		builder.setName(getNameProperty(CAMERA_CONFIGURATION_PREFIX, index));
		builder.setCameraConfiguration(getConfigurationBeanProperty(CAMERA_CONFIGURATION_PREFIX, index));
		builder.setCameraControl(getCameraControlProperty(index));

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
		return LocalProperties.get(formatPropertyKey(CAMERA_CONFIGURATION_PREFIX, index, "cameraControl"), null);
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

	private static class ICameraConfigurationImpl implements ICameraConfiguration {

		private final int cameraIndex;

		public ICameraConfigurationImpl(int cameraIndex) {
			super();
			this.cameraIndex = cameraIndex;
		}

		@Override
		public Optional<CameraConfiguration> getCameraConfiguration() {
			return FinderHelper.getFindableDevice(getCameraConfigurationInstance(cameraIndex));
		}

		@Override
		public int getCameraIndex() {
			return cameraIndex;
		}

		@Override
		public Optional<CameraControl> getCameraControl() {
			return FinderHelper.getFindableDevice(getCameraControlProperty(cameraIndex));
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
	}
}
