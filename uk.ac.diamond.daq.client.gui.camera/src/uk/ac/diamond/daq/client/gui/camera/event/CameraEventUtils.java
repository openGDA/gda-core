package uk.ac.diamond.daq.client.gui.camera.event;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.observable.IObserver;
import uk.ac.diamond.daq.client.gui.camera.CameraHelper;
import uk.ac.gda.api.camera.CameraControl;
import uk.ac.gda.api.camera.CameraControllerEvent;
import uk.ac.gda.ui.tool.spring.SpringApplicationContextProxy;

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
			if (CameraControllerEvent.class.isInstance(arg)) {
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
	private static Consumer<CameraControllerEvent> cameraControlEventConsumer = event -> {
		SpringApplicationContextProxy.publishEvent(new CameraControlSpringEvent(CameraHelper.class, event));
		logger.debug("{}", event);			
	};
	private static final IObserver cameraControlObserver = CameraEventUtils.cameraControlEventObserver(cameraControlEventConsumer);
	
}
