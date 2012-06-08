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
 * An interface to ensure reconfigurability of findable objects. The reconfigure method is maybe called at any time
 * after initial configuration.
 * 
 * @since GDA 6.8
 */
public interface Reconfigurable {
	/**
	 * Re-initialisation of values and states.
	 * 
	 * @throws FactoryException
	 */
	public void reconfigure() throws FactoryException;
}
