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
