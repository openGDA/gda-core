/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package gda.util;

/**
 * Exception thrown as a result of issues listening to JSON messages
 */
public class JsonMessageConversionException extends Exception {
	public JsonMessageConversionException() {
		super();
	}

	public JsonMessageConversionException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public JsonMessageConversionException(String message, Throwable cause) {
		super(message, cause);
	}

	public JsonMessageConversionException(String message) {
		super(message);
	}

	public JsonMessageConversionException(Throwable cause) {
		super(cause);
	}
}
