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

package uk.ac.diamond.daq.client.gui.camera.event;

import static uk.ac.diamond.daq.client.gui.camera.CameraHelper.getCameraConfigurationPropertiesByCameraControlName;
import static uk.ac.diamond.daq.client.gui.camera.CameraHelper.getCameraControlObservers;
import static uk.ac.gda.core.tool.spring.SpringApplicationContextFacade.publishEvent;

import java.util.Optional;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.observable.IObserver;
import uk.ac.diamond.daq.client.gui.camera.CameraHelper;
import uk.ac.gda.api.camera.CameraControl;
import uk.ac.gda.api.camera.CameraControllerEvent;
import uk.ac.gda.client.properties.camera.CameraConfigurationProperties;

public final class CameraEventUtils {

	private static final Logger logger = LoggerFactory.getLogger(CameraEventUtils.class);

	private CameraEventUtils() {}

	/**
	 * Provides a listener of a handy method to observe and consume an {@link CameraControlEvent}.
	 * @param consumer
	 * @return
	 */
	private static final IObserver cameraControlEventObserver(Consumer<CameraControllerEvent> consumer) {
		return (source, arg) -> {
			if (arg instanceof CameraControllerEvent) {
				consumer.accept(CameraControllerEvent.class.cast(arg));
			}
		};
	}

	/**
	 * Attaches an observer to a {@code CameraControl} so to publish a {@code CameraControlSpringEvent}
	 * if a {@code CameraControllerEvent} arrives.
	 *
	 * @param cameraControl the camera controller to monitor
	 */
	public static void addIObserverToCameraControl(CameraControl cameraControl) {
		getCameraControlObservers()
			.putIfAbsent(cameraControl, cameraControlEventObserver(getCameraControlEventConsumer(cameraControl)));
		Optional.ofNullable(getCameraControlObservers().get(cameraControl))
			.ifPresent(cameraControl::addIObserver);
	}
	public static void removeIObserverFromCameraControl(CameraControl cameraControl) {
		Optional.ofNullable(getCameraControlObservers().get(cameraControl))
			.ifPresent(cameraControl::deleteIObserver);
	}

	private static Consumer<CameraControllerEvent> getCameraControlEventConsumer(CameraControl cameraControl) {
		return event -> {
			getCameraConfigurationPropertiesByCameraControlName(cameraControl.getName())
				.map(CameraConfigurationProperties::getId)
				.ifPresent(id -> publishEvent(new CameraControlSpringEvent(CameraHelper.class, event, id)));
			logger.debug("{}", event);
		};
	}
}
