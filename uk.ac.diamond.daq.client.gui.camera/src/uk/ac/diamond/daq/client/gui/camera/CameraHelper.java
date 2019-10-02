package uk.ac.diamond.daq.client.gui.camera;

import gda.configuration.properties.LocalProperties;
import gda.factory.Finder;
import uk.ac.gda.client.live.stream.IConnectionFactory;
import uk.ac.gda.client.live.stream.LiveStreamConnection;
import uk.ac.gda.client.live.stream.view.CameraConfiguration;
import uk.ac.gda.client.live.stream.view.StreamType;

/**
 * Hides the configuration structural design.
 * 
 * @author Maurizio Nagni
 *
 */
public final class CameraHelper {
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
		return IConnectionFactory.getLiveStremConnection(getCameraConfiguration(cameraPropertyName), streamType);
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
}
