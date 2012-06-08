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

package gda.jython.accesscontrol;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Holds utility methods used by interceptors.
 */
public class InterceptorUtils {

	/**
	 * Invokes a method on the specified object with the specified parameters.
	 * If the underlying method throws an exception, it is rethrown.
	 * 
	 * @param method the method to invoke
	 * @param theObject object to invoke the method on
	 * @param args parameters for the method call
	 * 
	 * @return result of invoking the method
	 * 
	 * @throws Throwable if the underlying method throws an exception
	 */
	public static Object invokeMethod(Method method, Object theObject, Object[] args) throws Throwable {
		try {
			return method.invoke(theObject, args);
		} catch (InvocationTargetException e) {
			throw e.getTargetException();
		}
	}
}
