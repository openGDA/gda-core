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
 * An interface to ensure configurability of objects. The {{@link #configure()} method is called after construction of
 * the instance, generally, by the object server, to perform initialisation.
 *
 * @since GDA 4.0
 */
public interface Configurable {
	/**
	 * Perform operations that must be done after Spring initialisation i.e. anything that goes beyond setting member
	 * variables.
	 *
	 * @throws FactoryException if there is an error in configuration e.g. required variable not set or cannot connect to device
	 */
	void configure() throws FactoryException;

	/**
	 * Checks to see if the object is already configured.
	 *
	 * @since GDA 9.8
	 *
	 * @return return <code>true</code> if configured <code>false</code> otherwise
	 */
	boolean isConfigured();
}
