/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package gda.util.simpleServlet;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * A 'simple servlet' that allows {@code static} methods to be invoked.
 */
public class SimpleServlet {
	/**
	 * Invokes a {@code static} method, that either takes no parameters, or
	 * takes a single {@link String} parameter.
	 * 
	 * @param params invocation parameters: {@code "class-name?method-name"} or {@code "class-name?method-name?string"}
	 * 
	 * @return Object the object returned by the invoked method
	 * 
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 */
	static public Object execute(String params) throws IllegalAccessException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException {
		String className = params.substring(0, params.indexOf("?"));
		String rest = params.substring(params.indexOf("?") + 1);
		String methodName = null;
		String commandStr = null;
		if (rest.contains("?")) {
			methodName = rest.substring(0, rest.indexOf("?"));
			commandStr = rest.substring(rest.indexOf("?") + 1);
		} else {
			methodName = rest;
		}
		Class<? extends Object> c = Class.forName(className);
		Class<?>[] argsTypes = commandStr == null ? new Class[] {} : new Class[] { String.class };
		Method m = c.getDeclaredMethod(methodName, argsTypes);
		Object[] args = commandStr == null ? new Object[] {} : new Object[] { commandStr };
		return m.invoke(null, args);
	}

	/**
	 * Invokes a {@code static} method, that either takes a single {@link Object},
	 * or a {@link String} and an {@link Object}.
	 * 
	 * @param params invocation parameters: {@code "class-name?method-name"} or {@code "class-name?method-name?string"}
	 * @param object an object
	 * 
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 */
	static public void execute(String params, Object object) throws IllegalAccessException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException {
		String className = params.substring(0, params.indexOf("?"));
		String rest = params.substring(params.indexOf("?") + 1);
		String methodName = null;
		String commandStr = null;
		if (rest.contains("?")) {
			methodName = rest.substring(0, rest.indexOf("?"));
			commandStr = rest.substring(rest.indexOf("?") + 1);
		} else {
			methodName = rest;
		}
		Class<? extends Object> c = Class.forName(className);
		Class<?>[] argsTypes = commandStr == null ? new Class[] { object.getClass() } : new Class[] { String.class, object.getClass() };
		Method m = c.getDeclaredMethod(methodName, argsTypes);
		Object[] args = commandStr == null ? new Object[] { object } : new Object[] { commandStr, object };
		m.invoke(null, args);
	}

}
