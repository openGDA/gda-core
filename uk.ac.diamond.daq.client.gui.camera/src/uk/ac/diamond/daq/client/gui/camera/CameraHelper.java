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

import static uk.ac.gda.core.tool.spring.SpringApplicationContextFacade.getBean;
import static uk.ac.gda.core.tool.spring.SpringApplicationContextFacade.publishEvent;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.swt.widgets.Composite;
import org.springframework.context.ApplicationListener;

import gda.device.DeviceException;
import gda.factory.Findable;
import uk.ac.diamond.daq.client.gui.camera.beam.BeamCameraMapping;
import uk.ac.diamond.daq.client.gui.camera.event.BeamCameraMappingEvent;
import uk.ac.diamond.daq.client.gui.camera.event.CameraControlSpringEvent;
import uk.ac.diamond.daq.client.gui.camera.event.CameraEventUtils;
import uk.ac.diamond.daq.client.gui.camera.event.ChangeActiveCameraEvent;
import uk.ac.diamond.daq.client.gui.camera.liveview.StreamControlData;
import uk.ac.diamond.daq.client.gui.camera.liveview.state.StreamController;
import uk.ac.diamond.daq.client.gui.camera.monitor.CameraAvailabilityMonitor;
import uk.ac.gda.api.camera.CameraControl;
import uk.ac.gda.api.camera.CameraControllerEvent;
import uk.ac.gda.client.event.RootCompositeAware;
import uk.ac.gda.client.exception.GDAClientException;
import uk.ac.gda.client.live.stream.view.CameraConfiguration;
import uk.ac.gda.client.live.stream.view.StreamType;
import uk.ac.gda.client.properties.camera.CameraConfigurationProperties;
import uk.ac.gda.client.properties.camera.CameraToBeamMap;
import uk.ac.gda.ui.tool.spring.ClientSpringProperties;
import uk.ac.gda.ui.tool.spring.FinderService;

/**
 * Hides the configuration structural design. A typical configuration defining a
 * camera would look like below
 *
 * <pre>
 * {@code
 * client.cameras[0].configuration=d2_cam_config
 * client.cameras[0].name=Camera Zero
 * client.cameras[0].id=PCO_CAMERA
 * client.cameras[0].cameraControl=imaging_camera_control
 * client.cameras[0].beam_mapping_active=true
 * client.cameras[0].readoutTime=true
 * client.cameras[0].triggerModes=AUTO:0,EXTERNAL:1
 * client.cameras[0].motor[0].controller = stagez
 * client.cameras[0].motor[0].name = Camera Axis Z
 * client.cameras[0].motor[1].controller = stagey
 * client.cameras[0].motor[1].name = Camera Axis Y
 * }
 * </pre>
 *
 * where the fields meaning represent
 *
 * <ul>
 * <li><i>client.cameras[INDEX]</i>
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
 * <li><i>motor[MOTOR_INDEX]</i>
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

	private static final Map<Integer, ICameraConfiguration> cameraConfigurations = new HashMap<>();

	static {
		loadAllProperties();
	}

	private CameraHelper() {
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

	/**
	 * Returns the available client camera configuration
	 * @return the available camera configurations
	 */
	public static List<CameraConfigurationProperties> getAllCameraConfigurationProperties() {
		return Collections.unmodifiableList(getCameraProperies().stream()
				.filter(Objects::nonNull)
				.collect(Collectors.toList()));
	}

	public static ICameraConfiguration getICameraConfigurationByConfigurationName(String cameraConfigurationName) {
		return getCameraConfigurationPropertiesByConfigurationName(cameraConfigurationName)
			.map(CameraHelper::createICameraConfiguration)
			.orElse(null);
	}

	/**
	 * Finds a camera configuration from the loaded ones by its detector name,
	 * @param detectorName the detector name
	 * @return a camera configuration, otherwise {@link Optional#empty()}
	 */
	public static Optional<CameraConfigurationProperties> getCameraConfigurationPropertiesByDetectorName(String detectorName) {
		Predicate<? super CameraConfigurationProperties> filter = c -> c.getName().equals(detectorName);
		return getCameraConfigurationProperties(filter);
	}

	/**
	 * Finds a camera configuration from the loaded ones by its cameraControl name,
	 * @param cameraControlName the control name
	 * @return a camera configuration, otherwise {@link Optional#empty()}
	 */
	public static Optional<CameraConfigurationProperties> getCameraConfigurationPropertiesByCameraControlName(String cameraControlName) {
		Predicate<? super CameraConfigurationProperties> filter = c -> c.getCameraControl().equals(cameraControlName);
		return getCameraConfigurationProperties(filter);
	}

	/**
	 * Returns the available client camera configuration
	 * @return the available camera configurations
	 */
	private static Optional<CameraConfigurationProperties> getCameraConfigurationProperties(Predicate<? super CameraConfigurationProperties> filter) {
		return getCameraProperies().stream()
				.filter(filter)
				.findFirst();
	}

	/**
	 * Finds a camera configuration from the loaded ones by its cameraConfigurationName name,
	 * @param cameraConfigurationName the configuration name
	 * @return a camera configuration, otherwise {@link Optional#empty()}
	 */
	public static Optional<CameraConfigurationProperties> getCameraConfigurationPropertiesByConfigurationName(String cameraConfigurationName) {
		Predicate<? super CameraConfigurationProperties> filter = c -> c.getConfiguration().equals(cameraConfigurationName);
		return getCameraConfigurationProperties(filter);
	}

	/**
	 * Returns the IDs of the camera requiring a camera monitoring button
	 * @return a list of camera IDs
	 */
	public static List<String> getCameraMonitors() {
		return Collections.unmodifiableList(getCameraProperies().stream()
				.filter(CameraConfigurationProperties::isWithMonitor)
				.filter(p -> Objects.nonNull(p.getId()))
				.map(CameraConfigurationProperties::getId)
				.collect(Collectors.toList()));
	}

	/**
	 * Returns a {@link CameraConfigurationProperties} by its index
	 * @param cameraIndex the index of the requested camera
	 * @return the required camera configuration
	 */
	public static CameraConfigurationProperties getCameraConfigurationProperties(int cameraIndex) {
		return getCameraProperies().get(cameraIndex);
	}

	public static Optional<CameraConfigurationProperties> getCameraConfigurationPropertiesByID(String id) {
		return getCameraProperies().stream()
				.filter(c -> c.getId().equals(id))
				.findFirst();
	}

	/**
	 * Returns the default camera properties. The actual implementation returns the
	 * first camera as default but this should change in future.
	 *
	 * @return the camera properties, otherwise <code>null</code>
	 */
	public static CameraConfigurationProperties getDefaultCameraConfigurationProperties() {
		if (getCameraProperies().isEmpty()) {
			return null;
		}
		return getCameraProperies().get(0);
	}

	public static ICameraConfiguration createICameraConfiguration(int cameraIndex) {
		return cameraConfigurations.computeIfAbsent(cameraIndex, ICameraConfigurationImpl::new);
	}

	/**
	 * @param cameraProperties
	 * @return may return {@code null}
	 */
	public static ICameraConfiguration createICameraConfiguration(CameraConfigurationProperties cameraProperties) {
		return Optional.ofNullable(getCameraProperies().indexOf(cameraProperties))
				.map(CameraHelper::createICameraConfiguration)
				.orElse(null);
	}

	/**
	 * Adds a mapping between the motors moving the beam, if any, and the camera
	 * array. The method publishes a {@link BeamCameraMappingEvent} to inform any
	 * listener of the camera update.
	 *
	 * @param cameraConfiguration   the camera to map
	 * @param beamCameraMap the camera to beam mapping
	 */
	public static void addBeamCameraMap(ICameraConfiguration cameraConfiguration, CameraToBeamMap beamCameraMap) {
		ICameraConfigurationImpl.class.cast(cameraConfiguration).setBeamCameraMap(beamCameraMap);
		// resets the status to mark the end of the mapping
		cameraConfigurations.entrySet().stream()
			.filter(e -> e.getValue().equals(cameraConfiguration))
			.map(Map.Entry::getKey)
			.findFirst()
			.ifPresent(cameraIndex -> {
				var cmEvent = new BeamCameraMappingEvent(CameraHelper.class, cameraIndex);
				publishEvent(cmEvent);
			});
	}

	private static void observeCameraProperties() {
		getAllCameraConfigurationProperties().stream()
			.map(CameraHelper::createICameraConfiguration)
			.map(ICameraConfiguration::getCameraControl)
			.forEach(cc -> cc.ifPresent(CameraEventUtils::addIObserverToCameraControl));
	}

	private static void monitorCameraAvailability() {
		new CameraAvailabilityMonitor();
	}

	private static void removeObserverCameraProperties() {
		getAllCameraConfigurationProperties().stream()
			.map(CameraHelper::createICameraConfiguration)
			.map(ICameraConfiguration::getCameraControl)
			.forEach(cc -> cc.ifPresent(CameraEventUtils::removeIObserverFromCameraControl));
	}

	private static class ICameraConfigurationImpl implements ICameraConfiguration {

		private final int cameraIndex;
		/**
		 * The mapping from camera array space to the beam drivers, if any
		 */
		private CameraToBeamMap beamCameraMap;

		private BeamCameraMapping beamCameraMapping;

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
			var max = new RectangularROI();
			max.setPoint(0, 0);
			max.setEndPoint(frameSize);
			return max;
		}

		@Override
		public CameraConfigurationProperties getCameraConfigurationProperties() {
			return getAllCameraConfigurationProperties().get(cameraIndex);
		}

		@Override
		public CameraToBeamMap getBeamCameraMap() {
			if (beamCameraMap == null) {
				setBeamCameraMap(getCameraConfigurationProperties().getCameraToBeamMap());
			}
			return beamCameraMap;
		}

		@Override
		public BeamCameraMapping getBeamCameraMapping() {
			return Optional.ofNullable(beamCameraMapping)
					.orElseGet(this::createBeamCameraMapping);
		}

		public void setBeamCameraMap(CameraToBeamMap beamCameraMap) {
			this.beamCameraMap = beamCameraMap;
		}

		private BeamCameraMapping createBeamCameraMapping() {
			beamCameraMapping = Optional.ofNullable(this.getBeamCameraMap())
				.map(CameraToBeamMap::getDriver)
				.filter(d -> d.size() == 2)
				.map(d -> new BeamCameraMapping(d.get(0), d.get(1)))
				.orElse(null);
			return beamCameraMapping;
		}

		private Optional<CameraControl> getCameraControl(int cameraIndex) {
			String findableName = getCameraProperies().get(cameraIndex).getCameraControl();
			if (findableName != null) {
				return getFindable(findableName, CameraControl.class);
			}
			return Optional.empty();
		}

		public static Optional<CameraConfiguration> getCameraConfiguration(int cameraIndex) {
			String findableName = getCameraConfigurationInstance(cameraIndex);
			if (findableName != null) {
				return getFindable(findableName, CameraConfiguration.class);
			}
			return Optional.empty();
		}

		private static <T extends Findable> Optional<T> getFindable(String findableName, Class<T> clazz) {
			return Optional.ofNullable(getFinderService())
					.map(f -> f.getFindableObject(findableName, clazz))
					.filter(Optional::isPresent)
					.map(Optional::get)
					.map(clazz::cast);
		}

		private static FinderService getFinderService() {
			return getBean(FinderService.class);
		}
	}

	private static String getCameraConfigurationInstance(int cameraIndex) {
		return getCameraConfigurationProperties(cameraIndex).getConfiguration();
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
	private static final void loadAllProperties() {
		removeObserverCameraProperties();

		observeCameraProperties();

		monitorCameraAvailability();
	}

	private static  List<CameraConfigurationProperties> getCameraProperies() {
		return getBean(ClientSpringProperties.class)
				.getCameras()
				.stream()
				.filter(c -> c.getConfiguration() != null && c.getCameraControl() != null)
				.collect(Collectors.toList());
	}

	/**
	 * A utility method which creates a Spring listener for {@link ChangeActiveCameraEvent} which implements {@link RootCompositeAware}.
	 * Consequently this listener, before consume the event, verifies that the Composit listening at the event AND the composite which published
	 * the event have the same root parent, that is traversing upward the parents of both, they have the same common parent Composite.
	 *
	 * @param parent the composite interested in camera change events
	 * @param comboItemConsumer the event consumer, if have same root parents
	 * @return a Spring {@code ApplicationListener}
	 */
	public static final ApplicationListener<ChangeActiveCameraEvent> createChangeCameraListener(Composite parent,
			final Consumer<ChangeActiveCameraEvent> comboItemConsumer) {
		return new ApplicationListener<ChangeActiveCameraEvent>() {
			@Override
			public void onApplicationEvent(ChangeActiveCameraEvent event) {
				// if the event arrives from a component with a different common parent, rejects
				// the event
				if (!event.haveSameParent(parent)) {
					return;
				}
				comboItemConsumer.accept(event);
			}
		};
	}

	public static final CameraConfiguration getCameraConfiguration(StreamController streamController) {
		return Optional.ofNullable(streamController.getControlData())
			.map(StreamControlData::getCameraConfigurationProperties)
			.map(CameraHelper::createICameraConfiguration)
			.map(ICameraConfiguration::getCameraConfiguration)
			.filter(Optional::isPresent)
			.map(Optional::get)
			.orElse(null);
	}
}