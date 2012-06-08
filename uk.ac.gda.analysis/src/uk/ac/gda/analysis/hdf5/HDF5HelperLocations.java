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

import java.util.Vector;

public class HDF5HelperLocations extends Vector<HDF5HelperLocation>{
	public String getLocationForOpen(){
		String groupName="";
		for( HDF5HelperLocation loc : this){
			if( groupName.length() >0)
				groupName += "/";
			groupName += loc.name;
		}
		return groupName;
	}
	public HDF5HelperLocations add(String name){
		return add(name, null, null);
	}
	public HDF5HelperLocations add(String name, String attributeName, String attributeValue){
		add( new HDF5HelperLocation(name, attributeName, attributeValue));
		return this;
	}
	
	public HDF5HelperLocations(String name){
		add(name);
	}
	public HDF5HelperLocations(){
	}
	
}
