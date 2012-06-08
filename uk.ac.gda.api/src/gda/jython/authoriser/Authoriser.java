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

package gda.jython.authoriser;

/**
 * Interface for classes providing an authorisation service for the GDA
 */
public interface Authoriser {
	
	/**
	 * The java property to use to define which class of Authoriser to use
	 */
	public static final String AUTHORISERCLASS_PROPERTY = "gda.gui.AcquisitionGUI.authorisationMethod";
	
	/**
	 * The default java property if none defined.
	 */
	public static final String DEFAULT_AUTHORISER = "gda.jython.authoriser.FileAuthoriser";

	/**
	 * @param username
	 * @return true if the given username is listed in the system
	 */
	public boolean hasAuthorisationLevel(String username);
	
	/**
	 * @param username
	 * @return the authorisation level for the given username for this GDA installation
	 */
	public int getAuthorisationLevel(String username);
	
	/**
	 * Returns true if the username is a member of facility staff and should have elevated privileges over users. Any
	 * accounts used for beamline testing/commissioning should also return true when using this method.
	 * 
	 * @param username
	 * @return the username is in a separate list for beamline staff
	 */
	public boolean isLocalStaff(String username);

}
