/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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
 * filter information for the Nexus Filter
 */
public interface INexusFilterInfoProvider extends INexusPathProvider {

	/**
	 * Set filter descriptor
	 * @param descriptor a filter descriptor or null for no filter
	 */
	void setFilterDescriptor(INexusFilterDescriptor descriptor);

	/**
	 * Get the filter descriptor
	 * @return the filter descriptor or null if one has not been set
	 */
	public abstract INexusFilterDescriptor getFilterDescriptor();

	/**
	 * Get the most recent filter descriptors
	 * @return array of filter descriptors
	 */
	INexusFilterDescriptor[] getFilterDescriptorHistory();
}
