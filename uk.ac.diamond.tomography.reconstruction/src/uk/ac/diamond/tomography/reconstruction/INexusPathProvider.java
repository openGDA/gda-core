/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package uk.ac.diamond.tomography.reconstruction;

/**
 * An interface to handle getting and setting
 * nexus paths used for the filters and sorter ui's.
 * Also handles a history of recent sort paths
 */
public interface INexusPathProvider {

	/**
	 * Set the path by which to sort nexus elements
	 * @param path a valid nexus path
	 */
	void setNexusPath(String path);
	
	/**
	 * Get the path by which to  nexus elements
	 * Returns<code>null</code> if path is not set
	 * @return a valid nexus path
	 */
	String getNexusPath();

	/**
	 * Get the recently used paths which have been
	 * saved to history
	 * @return array of path strings or <code>null</code>
	 */
	String[] getNexusPathHistory();

	/**
	 * An initial path suggestion
	 * @return a path suggestion
	 */
	String getSuggestedPath();


}
