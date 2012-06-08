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

package gda.jython;

/**
*
* Interface used by some classes to get authorisation
* Provided to ensure loose coupling between callers and command runner implementation
*/
public interface IAuthorisationHolder {
	/**
	 * Returns the current authorisation level (this value could be dynamic if a baton is in use)
	 *
	 * @return the authorisationLevel at this moment in time
	 */
	public int getAuthorisationLevel();
	/**
	 * @return the authorisation level ignoring any current baton status
	 */
	public int getAuthorisationLevelAtRegistration() ;
}
