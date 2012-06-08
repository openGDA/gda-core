/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package gda.configuration.epics;

/**
 * ConfigurationNotFoundException Class
 */
@SuppressWarnings("serial")
public class ConfigurationNotFoundException extends Exception {

	/**
	 * Constructor
	 */
	public ConfigurationNotFoundException() {
	}

	/**
	 * @param message
	 */
	public ConfigurationNotFoundException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public ConfigurationNotFoundException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public ConfigurationNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

}
