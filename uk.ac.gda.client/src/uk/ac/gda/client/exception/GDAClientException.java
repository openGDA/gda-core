/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package uk.ac.gda.client.exception;

/**
 * A generic class for any GDA client related exception
 *
 * @author Maurizio Nagni
 */
public class GDAClientException extends Exception {

	public GDAClientException() {
		// TODO Auto-generated constructor stub
	}

	public GDAClientException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public GDAClientException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	public GDAClientException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public GDAClientException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

}
