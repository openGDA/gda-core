/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

package gda.factory;

/**
 * Thrown when an eceptional factory error codition has occured. For example when configuration fails.
 */
public class FactoryException extends Exception {

	/**
	 * Constructs a factory exception with the specified detail message.
	 * 
	 * @param message
	 *            the detail message.
	 */
	public FactoryException(String message) {
		super(message);
	}

	/**
	 * Constructs a factory exception with the specified detail message.
	 * 
	 * @param message
	 *            the detail message.
	 * @param cause
	 *            cause
	 */
	public FactoryException(String message, Throwable cause) {
		super(message, cause);
	}

}
