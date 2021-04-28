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

package uk.ac.diamond.daq.client.gui.camera.monitor;


import static uk.ac.diamond.daq.client.gui.camera.CameraHelper.getAllCameraConfigurationProperties;
import static uk.ac.gda.core.tool.spring.SpringApplicationContextFacade.publishEvent;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import gda.configuration.properties.LocalProperties;
import gda.device.DeviceException;
import uk.ac.diamond.daq.client.gui.camera.CameraHelper;
import uk.ac.diamond.daq.client.gui.camera.ICameraConfiguration;
import uk.ac.diamond.daq.client.gui.camera.event.CameraControlSpringEvent;
import uk.ac.gda.api.camera.CameraControl;
import uk.ac.gda.api.camera.CameraControllerEvent;
import uk.ac.gda.api.camera.CameraState;

/**
 * Start a periodic runnable service to verify the cameras availability.
 *
 * <p>
 * {@link CameraHelper} consumes {@code CameraControllerEvent} published by the {@code EpicsCameraControl} to receive camera updates.
 * However, at the moment, the {@code EpicsCameraControl} cannot publish events related to IOC availability.
 * This class polls, per camera, its status and publishes the associated {@link CameraControlSpringEvent}.
 * </p>
 *
 * <p>
 * The polling period, in seconds, can be updated setting the <i>client.camera.state.polling</i> property.
 * The default is 5 and should be enough as its only goal is to report entering or exiting from UNAVAILABLE state,
 * not as general tool for monitoring the camera state.
 * </p>
 * @author Maurizio Nagni
 *
 */
public class CameraAvailabilityMonitor {
	private final ScheduledExecutorService executorService;

	private int period = LocalProperties.getInt("client.camera.state.polling", 5);

	public CameraAvailabilityMonitor() {
		executorService = Executors.newScheduledThreadPool(getAllCameraConfigurationProperties().size());
		getAllCameraConfigurationProperties().stream()
			.map(CameraHelper::createICameraConfiguration)
			.forEach(this::attachMonitor);
	}

	private void attachMonitor(ICameraConfiguration iCameraConfiguration) {
		executorService.scheduleWithFixedDelay(runnableMonitor(iCameraConfiguration), 1, period, TimeUnit.SECONDS);
	}

	private Runnable runnableMonitor(ICameraConfiguration iCameraConfiguration) {
		return () -> checkCameraAvailability(iCameraConfiguration);
	}

	private void checkCameraAvailability(ICameraConfiguration iCameraConfiguration) {
		iCameraConfiguration.getCameraControl()
			.ifPresent(c -> publishCameraControllerEvent(c, iCameraConfiguration));
	}

	private void publishCameraControllerEvent(CameraControl cameraControl, ICameraConfiguration iCameraConfiguration) {
		var event = new CameraControllerEvent();
		try {
			event.setName(cameraControl.getName());
			event.setCameraState(cameraControl.getAcquireState());
			event.setAcquireTime(cameraControl.getAcquireTime());
			event.setBinningFormat(cameraControl.getBinningPixels());
		} catch (DeviceException e) {
			event.setCameraState(CameraState.UNAVAILABLE);
		}
		publishEvent(new CameraControlSpringEvent(CameraAvailabilityMonitor.class, event, iCameraConfiguration.getCameraConfigurationProperties().getId()));
	}
}
