package uk.ac.diamond.daq.client.gui.camera.exception;

import uk.ac.gda.client.exception.GDAClientException;

/**
 * Generic exception thrown for camera error in the client
 * 
 * @author Maurizio Nagni
 *
 */
public class CameraException extends GDAClientException {

	private static final long serialVersionUID = 5536302202720156479L;

	public CameraException() {
	}

	public CameraException(String message) {
		super(message);
	}

	public CameraException(Throwable cause) {
		super(cause);
	}

	public CameraException(String message, Throwable cause) {
		super(message, cause);
	}

	public CameraException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
