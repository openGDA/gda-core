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
 * A marker interface used to define whether the object is constructed for local access. The object server will not try
 * to create a CORBA impl class if its state is set to true. This should be defined in the XML. The default value is
 * false.
 * 
 * @since GDA 4.0
 */
public interface Localizable {
	/**
	 * Set or change the local mode (as defined in XML).
	 * 
	 * @param local
	 *            set to true for locally created objects or else false for remote objects.
	 */
	public void setLocal(boolean local);

	/**
	 * Returns the local/remote state of an object. Used by Castor.
	 * 
	 * @return true if this is a local object.
	 */
	public boolean isLocal();

}