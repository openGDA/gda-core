/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

package uk.ac.gda.client.device.collection;

import gda.factory.Finder;

/**
 * This class provides a uniform interface for clients to 
 * access specific instances of IDeviceCollection. 
 */
public enum DeviceCollectionFactory {
	
	INSTANCE;
	
	/**
	 * Returns a specific IDeviceCollection based on its id or 
	 * null if one is not found matching that id
	 * @param id IDeviceCollection identifier
	 * @return An instance of the IDeviceCollection or null if not found
	 */
	public IDeviceCollection getDeviceCollection(String id){
			return (IDeviceCollection)Finder.getInstance().find(id);
	}
	
	/**
	 * Returns a specific IDeviceCollection for a view based on the unique id of the view
	 * or null if one is not found
	 * 
	 * @param viewId view identifier
	 * @return An instance of the IDeviceCollection or null if not found
	 */
/*	public IDeviceCollection getDeviceCollectionForView(String viewId){	
		Finder finder = Finder.getInstance();
		Map<String, Findable> findablesOfType = finder.getFindablesOfType(DeviceCollectionMapping.class);
		for (Findable findable : findablesOfType.values()) {
			DeviceCollectionMapping deviceCollectionMapping = (DeviceCollectionMapping)findable;
			if (deviceCollectionMapping.getId().equals(viewId)){
				return deviceCollectionMapping.getCollection();
			}
		}
		return null;
	}*/

}
