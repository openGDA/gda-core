/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package uk.ac.gda.analysis.hdf5;

public class HDF5NexusLocation extends HDF5HelperLocation {

	public HDF5NexusLocation(String name, String attributeValue) {
		super(name, attributeValue != null ? "NX_class" : null, attributeValue);
	}
	public HDF5NexusLocation(String name) {
		super(name,  null, null);
	}
	
	public static HDF5HelperLocations makeNXEntry(){
		HDF5HelperLocations loc = new HDF5HelperLocations();
		loc.add(new HDF5NexusLocation("entry1","NXentry"));
		return loc;
	}

}
