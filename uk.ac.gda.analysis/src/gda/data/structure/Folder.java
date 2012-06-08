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

package gda.data.structure;

import gda.analysis.datastructure.ManagedDataObject;

import java.util.Iterator;
import java.util.Vector;

/**
 * Code originally written by SLAC TEAM (AIDA) Modified at Diamond A Folder is a managed object representing a directory
 * in a project.
 */
public class Folder extends ManagedDataObject {
	/** A hash map containing managed objects */
	private OrderedHashMap<String, Object> map = new OrderedHashMap<String, Object>();

	Vector<String> folderlist = new Vector<String>();

	Vector<String> linklist = new Vector<String>();

	/**
	 * Constructor.
	 */
	public Folder() {
	}

	/**
	 * Add a managed object to the folder
	 * 
	 * @param objectName
	 * @param object
	 */
	public void add(String objectName, Object object) {
		map.put(objectName, object);
		if (object instanceof Folder) {
			folderlist.add(objectName);
		} else if (object instanceof Link) {
			linklist.add(objectName);
		}
	}

	/**
	 * Removed an object from the folder
	 * 
	 * @param objectName
	 */
	public void remove(Object objectName) {
		folderlist.remove(objectName);
		linklist.remove(objectName);
		map.remove(objectName);
	}

	/**
	 * Get a managed object
	 * 
	 * @param name
	 * @return managed object
	 */
	public Object getChild(String name) {
		return map.get(name);
	}

	/**
	 * Get a managed object name in the hash map
	 * 
	 * @param child
	 * @return Key of this object in hashmap
	 */
	public String getChildName(Object child) {
		return map.getKey(child);
	}

	/**
	 * Get a managed object name in the hash map
	 * 
	 * @param i
	 * @return Key of this object in hashmap
	 */
	public String getChildName(int i) {
		return map.getKey(i);
	}

	/**
	 * Get a managed object name in the hash map
	 * 
	 * @param name
	 * @return Key of this object in hashmap
	 */
	public boolean containsName(String name) {
		return map.containsKey(name);
	}

	/**
	 * @return The no. of managed objects
	 */
	public int getChildCount() {
		return map.size();
	}

	/**
	 * @param child
	 * @return The index of a managed object child
	 */
	public int getIndexOfChild(Object child) {
		Iterator<Object> iter = map.values().iterator();
		for (int i = 0; iter.hasNext(); i++) {
			if (iter.next().equals(child))
				return i;
		}
		return -1;
	}

	/**
	 * Get child Object for a given index.
	 * 
	 * @param index
	 * @return child Object.
	 */
	public Object getChild(int index) {
		Iterator<Object> iter = map.values().iterator();
		for (int i = 0; i < index; i++)
			iter.next();
		return iter.next();
	}

	/**
	 * @return Get the type of this object as a string i.e. dir
	 */
	@Override
	public String getType() {
		return "dir";
	}

	/**
	 * Test to see if name is a Folder.
	 * 
	 * @param name
	 * @return Is name is a Folder.
	 */
	public boolean isFolder(String name) {
		return folderlist.contains(name);
	}

	/**
	 * Test to see if name is a Link.
	 * 
	 * @param name
	 * @return Is name a Link.
	 */
	public boolean isLink(String name) {
		return linklist.contains(name);
	}

}
