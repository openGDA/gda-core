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

package gda.spring.parsers;

import java.util.List;

import org.springframework.beans.factory.support.ManagedList;

/**
 * Utility methods for Spring namespace handlers.
 */
public class NamespaceUtils {

	/**
	 * Adds an object to a Spring {@link ManagedList}. Encapsulated in a
	 * method as this action involves the use of a raw type.
	 * 
	 * @param list the list
	 * @param obj the object to add to the list
	 */
	public static <T> void addToManagedList(ManagedList<T> list, T obj) {
		list.add(obj);
	}
	
	public static <T> void addAllToManagedList(ManagedList<T> list, List<T> objects) {
		list.addAll(objects);
	}
}
