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
 * An interface to ensure configurability of findable objects. The configure method is called after construction of the
 * instance, generally, by the object server, to perform initialisation.
 * 
 * @since GDA 4.0
 */
public interface Configurable {
	/**
	 * Perform initialisation of values and states that cannot be achieved directly from instantitation with Castor.
	 * 
	 * @throws FactoryException
	 */
	public void configure() throws FactoryException;
}
