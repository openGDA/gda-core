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

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Vector;

import org.apache.commons.lang.ArrayUtils;
import org.python.core.Py;
import org.python.core.PyObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A object to be used by the Interceptor classes in this package to find and then store a list of methods which have
 * been annotated as protected.
 */
public class ProtectedMethodComponent {
	
	private static final Logger logger = LoggerFactory.getLogger(ProtectedMethodComponent.class);

	protected Vector<String> protectedMethodNames = new Vector<String>();
	protected HashMap<Integer, Class<?>[][]> protectedMethodParameters = new HashMap<Integer, Class<?>[][]>();
	protected Class<?>[] analysedInterfaces = new Class<?>[0];
	// Jython invoke methods
	Method jythonInvoke, jythonInvoke1, jythonInvoke2, jythonInvoke3, jythonInvoke4;

	/**
	 * Constructor.
	 * 
	 * @param clazz
	 *            - the Class of the object whose list of protected methods this object is to determine and store.
	 */
	public ProtectedMethodComponent(Class<?> clazz) {
		findProtectedMethods(clazz);
		try {
			jythonInvoke = PyObject.class.getMethod("invoke", String.class);
			jythonInvoke1 = PyObject.class.getMethod("invoke", String.class, PyObject.class);
			jythonInvoke2 = PyObject.class.getMethod("invoke", String.class, PyObject[].class);
			jythonInvoke3 = PyObject.class.getMethod("invoke", String.class, PyObject.class, PyObject.class);
			jythonInvoke4 = PyObject.class.getMethod("invoke", String.class, PyObject[].class, String[].class);
		} catch (Exception e) {
			logger.error("Error finding invoke methods from PyObject. Is the correct version of Jython installed?");
		}
	}

	/**
	 * Matches the given method to the list of protected methods held by this object. This does not do a simple equalTo
	 * operation as methods might be annotated as being protected in interfaces but the method uder test might be in a
	 * concrete class.
	 * <p>
	 * So this method does a comparison of method name and parameter types instead.
	 * 
	 * @param calledMethod
	 * @return true if the given method matches one of the list of protected methods held in this object
	 */
	public boolean isMethodProtected(Method calledMethod) {

		// find the index of this method name
		Integer indexNumber = protectedMethodNames.indexOf(calledMethod.getName());

		// if not in the list of method names then not protected.
		if (indexNumber == -1) {
			return false;
		}

		// get the list of parameter types associated with this method name
		Class<?>[][] listOfParameterTypes = protectedMethodParameters.get(indexNumber);

		// if any list of parameter types matches those of the given method then we have a match and return true
		for (Class<?>[] parameterTypes : listOfParameterTypes) {
			if (parameterTypesArrayComparison(parameterTypes, calledMethod.getParameterTypes())) {
				return true;
			}
		}

		// if get here then nothing found
		return false;
	}

	/**
	 * @param method
	 * @return true if the given method is one of the PyObject invoke methods
	 */
	public boolean isAnInvokeMethod(Method method) {
		return method.equals(jythonInvoke) || method.equals(jythonInvoke1) || method.equals(jythonInvoke2)
				|| method.equals(jythonInvoke3) || method.equals(jythonInvoke4);
	}

	/**
	 * Tests if the method is an invoke method from PyObject. If so, runs the method else throws a
	 * MethodNotPyObjectInvokeException.
	 * <p>
	 * have a SuppressWarnings beacuase of call to Py.tojava(). This method is not really deprecated: its a typo in the
	 * Jython code!
	 * 
	 * @param obj
	 * @param method
	 * @param args
	 * @return Object - result of the method call
	 * @throws Throwable
	 */
	public Object testForJythonInvocation(Object obj, Method method, Object[] args) throws Throwable {

		// if we are calling one of the jtyhon invoke methods
		if (isAnInvokeMethod(method)) {

			// extract the method name
			String methodName = args[0].toString();

			// extract the array of arguments
			Object[] newArgs = ArrayUtils.subarray(args, 1, args.length);

			// as the method is being called via a Jython invoke, we know that the arguments must be an array of
			// PyObjects.

			// if no arguments
			if (newArgs.length == 0) {
				return ((PyObject) obj).__getattr__(methodName).__call__();
			}
			// if more than two arguments, then jython will have put them into an array of PyObjects
			else if (newArgs.length == 1 && newArgs[0] instanceof PyObject[]) {
				return ((PyObject) obj).__getattr__(methodName).__call__((PyObject[]) newArgs[0], Py.NoKeywords);
			}
			// if one argument then call the shortcut for one method (see Jython Javadoc)
			else if (newArgs.length == 1) {
				return ((PyObject) obj).__getattr__(methodName).__call__((PyObject) newArgs[0]);
			}
			// if two arguments then call the shortcut for one method (see Jython Javadoc)
			else if (newArgs.length == 2) {
				return ((PyObject) obj).__getattr__(methodName).__call__((PyObject) newArgs[0], (PyObject) newArgs[1]);
			}
		}

		// if get here so call method anyway
		return method.invoke(obj, args);
	}

	/**
	 * Looks at the object this object is acting as a proxy for and identify all the methods which are designated to be
	 * protected by an annotation.
	 * 
	 * @param clazz
	 */
	@SuppressWarnings("rawtypes")
	private void findProtectedMethods(Class clazz) {
		// look at interfaces implemented directly by this class
		analyseClass(clazz);

		// then work up the class structure to include every inherited superclass
		Class superclass = clazz.getSuperclass();
		while (superclass != null) {
			analyseClass(superclass);
			superclass = superclass.getSuperclass();
		}
	}

	/**
	 * Analyse the given class and add any methods annotated to be protected to the protectedMethods array.
	 * 
	 * @param clazz
	 */
	@SuppressWarnings("rawtypes")
	private void analyseClass(Class clazz) {
		// loop over this classes methods
		Method[] newMethods = clazz.getMethods();
		for (Method thisMethod : newMethods) {
			if (thisMethod.isAnnotationPresent(gda.jython.accesscontrol.MethodAccessProtected.class)
					&& thisMethod.getAnnotation(MethodAccessProtected.class).isProtected()) {
				addMethod(thisMethod);
			}
		}

		// loop over the interfaces the class implements
		Class[] newInterfaces = clazz.getInterfaces();
		for (Class thisInterface : newInterfaces) {
			// if its a new interface (for performance, ensure each interface only looked at once)
			if (!ArrayUtils.contains(analysedInterfaces, thisInterface)) {
				analysedInterfaces = (Class[]) ArrayUtils.add(analysedInterfaces, thisInterface);

				// check if any method in that interface is annotated that it should be protected
				for (Method method : thisInterface.getMethods()) {
					if (method.isAnnotationPresent(gda.jython.accesscontrol.MethodAccessProtected.class)
							&& method.getAnnotation(MethodAccessProtected.class).isProtected()) {
						addMethod(method);
					}
				}
			}
		}
	}

	/**
	 * Adds the given method to the list of protected methods. The parameter types are also stored as this object does
	 * not work simply on method names. This is done by using two internal hashmaps linked with a common index number.
	 * 
	 * @param method
	 */
	private void addMethod(Method method) {
		// find the index number of this method name. If its not there then add the method name to the list
		if (!protectedMethodNames.contains(method.getName())) {
			protectedMethodNames.add(method.getName());
		}
		Integer indexNumber = protectedMethodNames.indexOf(method.getName());

		// using the index number, add the list of parameters types of this method to the array stored in the hashmap
		if (!protectedMethodParameters.containsKey(indexNumber)) {
			Class<?>[][] newEntry = new Class<?>[1][];
			newEntry[0] = method.getParameterTypes();
			protectedMethodParameters.put(indexNumber, newEntry);
		} else {
			Class<?>[][] oldEntry = protectedMethodParameters.get(indexNumber);
			boolean found = false;
			for (Class<?>[] parameterTypes : oldEntry) {
				if (parameterTypesArrayComparison(parameterTypes, method.getParameterTypes())) {
					found = true;
				}
			}
			if (!found) {
				oldEntry = (Class<?>[][]) ArrayUtils.add(oldEntry, method.getParameterTypes());
				protectedMethodParameters.put(indexNumber, oldEntry);
			}
		}
	}

	/**
	 * Element by element comparsion of two arrays of objects and return true if they are identical
	 * 
	 * @param first
	 * @param second
	 * @return true if a match
	 */
	private boolean parameterTypesArrayComparison(Class<?>[] first, Class<?>[] second) {

		if (first.length != second.length) {
			return false;
		}

		for (int i = 0; i < first.length; i++) {
			if (first[i] != second[i]) {
				return false;
			}
		}

		return true;
	}

}
