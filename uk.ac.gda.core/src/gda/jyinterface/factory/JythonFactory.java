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

package gda.jyinterface.factory;

import org.python.core.Py;
import org.python.util.PythonInterpreter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class JythonFactory {
	private static final Logger logger = LoggerFactory.getLogger(JythonFactory.class);
	private static JythonFactory instance = null;

	/**
	 * @return JythonFactory
	 */
	public static JythonFactory getInstance() {
		if (instance == null) {
			instance = new JythonFactory();
		}
		return instance;
	}

	/**
	 * converts jython object to java object
	 * 
	 * @param interfaceName
	 * @param pathToJythonModule
	 * @return Object
	 * @throws InstantiationException
	 */
	public static Object getJythonObject(String interfaceName, String pathToJythonModule) throws InstantiationException {

		Object javaInt = null;
		PythonInterpreter interpreter = new PythonInterpreter();
		interpreter.execfile(pathToJythonModule);
		String tempName = pathToJythonModule.substring(pathToJythonModule.lastIndexOf("/") + 1);
		tempName = tempName.substring(0, tempName.indexOf("."));
		System.out.println(tempName);
		String instanceName = tempName.toLowerCase();
		String javaClassName = tempName.substring(0, 1).toUpperCase() + tempName.substring(1);
		String objectDef = "=" + javaClassName + "()";
		interpreter.exec(instanceName + objectDef);
		try {
			Class<?> JavaInterface = Class.forName(interfaceName);
			javaInt = interpreter.get(instanceName).__tojava__(JavaInterface);
		} catch (ClassNotFoundException ex) {
			logger.error("Can not find class.", ex);
			throw new InstantiationException("Cannot find class " + interfaceName);
		}
		if (javaInt == Py.NoConversion) {
			throw new InstantiationException("Unable to create a " + interfaceName + " instance");
		}
		return javaInt;
	}
}