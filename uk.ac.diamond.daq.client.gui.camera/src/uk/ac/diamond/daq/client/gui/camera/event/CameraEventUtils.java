package uk.ac.diamond.daq.client.gui.camera.event;

import java.util.function.Consumer;

import gda.observable.IObserver;
import uk.ac.gda.api.camera.CameraControllerEvent;

public final class CameraEventUtils {

	private CameraEventUtils() {}
	
	/**
	 * Provides a listener of a handy method to observe and consume an {@link CameraControlEvent}. 
	 * @param consumer
	 * @return
	 */
	public static final IObserver cameraControlEventObserver(Consumer<CameraControllerEvent> consumer) {
		return (source, arg) -> {
			if (CameraControllerEvent.class.isInstance(arg)) {
				consumer.accept(CameraControllerEvent.class.cast(arg));
			}
		};
	}
	
}
