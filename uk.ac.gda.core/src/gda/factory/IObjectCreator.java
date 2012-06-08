/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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
 * Interface to be implemented by object creators, that instantiate objects for
 * GDA.
 * 
 * @deprecated Adding Spring beans directly to the application context is the
 * preferred method for instantiating objects. Encapsulating objects within a
 * {@link Factory} results in objects that cannot be referenced from the
 * application context.
 */
@Deprecated
public interface IObjectCreator {

	/**
	 * Returns the object factory created by this object creator.
	 * 
	 * @return the object factory
	 * 
	 * @throws FactoryException if the factory cannot be created
	 */
	public Factory getFactory() throws FactoryException;
	
	/**
	 * Indicates whether this object creator creates an object factory that
	 * contains entirely local objects.
	 * 
	 * @return whether this object creator instantiates only local objects
	 */
	public boolean isLocal();

}
