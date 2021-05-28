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

import static uk.ac.gda.core.tool.spring.SpringApplicationContextFacade.publishEvent;

import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.observable.IObserver;
import uk.ac.diamond.daq.client.gui.camera.CameraHelper;
import uk.ac.gda.api.camera.CameraControl;
import uk.ac.gda.api.camera.CameraControllerEvent;

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
		cameraControl.addIObserver(cameraControlObserver);
	}
	public static void removeIObserverFromCameraControl(CameraControl cameraControl) {
		cameraControl.deleteIObserver(cameraControlObserver);
	}
	private static Consumer<CameraControllerEvent> cameraControlEventConsumer = event -> {
		publishEvent(new CameraControlSpringEvent(CameraHelper.class, event));
		logger.debug("{}", event);
	};
	private static final IObserver cameraControlObserver = CameraEventUtils.cameraControlEventObserver(cameraControlEventConsumer);

}
