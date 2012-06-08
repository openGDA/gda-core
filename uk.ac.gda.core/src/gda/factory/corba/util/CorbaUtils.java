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

package gda.factory.corba.util;

import gda.factory.Findable;

import org.springframework.util.ClassUtils;

/**
 * CORBA-related utility methods.
 */
public class CorbaUtils {

	/**
	 * Finds the CORBA implementation class name for the specified type.
	 * 
	 * @param clazz the class of the object
	 * 
	 * @return the CORBA implementation class name
	 */
	public static String getImplementationClassName(Class<?> clazz) {
		
		CorbaImplClass implAnnotation = clazz.getAnnotation(CorbaImplClass.class);
		if (implAnnotation != null) {
			return implAnnotation.value().getName();
		}
		
		return getCorbaClassName(clazz.getName(), "Impl");
	}
	
	/**
	 * Finds the CORBA adapter class name for the specified type.
	 * 
	 * @param clazz the class of the object
	 * 
	 * @return the CORBA adapter class name
	 */
	public static String getAdapterClassName(Class<?> clazz) {
		
		CorbaAdapterClass adapterAnnotation = clazz.getAnnotation(CorbaAdapterClass.class);
		if (adapterAnnotation != null) {
			return adapterAnnotation.value().getName();
		}
		
		return getCorbaClassName(clazz.getName(), "Adapter");
	}

	
	private static String getCorbaClassName(String type, String suffix) {
		final String fullyQualifiedParentPackageName = type.substring(0, type.lastIndexOf('.'));
		
		// Take last component of parent package name and capitalise it; this
		// plus the suffix is used as the class name
		final String[] packageParts = fullyQualifiedParentPackageName.split("\\.");
		final String parentPackageName = packageParts[packageParts.length-1];
		final String capitalisedParentPackageName = parentPackageName.substring(0, 1).toUpperCase() + parentPackageName.substring(1);
		
		return fullyQualifiedParentPackageName + ".corba.impl." + capitalisedParentPackageName + suffix;
	}
	
	/**
	 * Determines if the object is a CORBA adapter.
	 */
	public static boolean isCorbaAdapter(Findable findable) {
		
		// To determine if the object is an adapter, we look for a 3-arg constructor (taking a CORBA Object, a String,
		// and a NetService). We don't have to do any special handling for cglib proxies here: if the original class had
		// this 3-arg constructor, the subclass created by cglib will have it, too.
		
		final Class<?> clazz = findable.getClass();
		return ClassUtils.hasConstructor(clazz, ADAPTER_CONSTRUCTOR_ARGS);
	}
	
	/**
	 * Argument types for the 3-argument constructor that all CORBA adapter classes must have.
	 */
	private static final Class<?>[] ADAPTER_CONSTRUCTOR_ARGS = new Class<?>[] {
		org.omg.CORBA.Object.class,
		String.class,
		NetService.class
	};
	
}
