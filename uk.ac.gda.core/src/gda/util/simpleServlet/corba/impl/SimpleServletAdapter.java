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

package gda.util.simpleServlet.corba.impl;

import java.io.Serializable;
import gda.device.Device;
import gda.device.DeviceException;
import gda.device.corba.impl.DeviceAdapter;
import gda.factory.Findable;
import gda.factory.Finder;
import gda.factory.corba.util.NetService;

/**
 * A client side implementation of the adapter pattern for the ControlPoint class
 */
public class SimpleServletAdapter extends DeviceAdapter implements Findable, Device {
	/**
	 * Create client side interface to the CORBA package.
	 * 
	 * @param obj
	 *            the CORBA object
	 * @param name
	 *            the name of the object
	 * @param netService
	 *            the CORBA naming service
	 */
	public SimpleServletAdapter(org.omg.CORBA.Object obj, String name, NetService netService) {
		super(obj, name, netService);
	}

	/**
	 * public method to ensure callers pass correct information
	 * 
	 * @param servletName
	 * @param className
	 * @param methodName
	 * @param args
	 * @return object
	 * @throws DeviceException
	 */
	public static Object runServlet(String servletName, String className, String methodName, String args)
			throws DeviceException {
		Object obj = Finder.getInstance().find(servletName);
		if (obj == null || !(obj instanceof Device)) {
			throw new IllegalArgumentException("Unable to find object named " + servletName);
		}
		return ((Device) obj).getAttribute(className + "?" + methodName + (args != null ? "?" + args : ""));
	}

	/**
	 * @param className
	 * @param methodName
	 * @param args
	 * @return runServlet
	 * @throws DeviceException
	 */
	public static Object runServlet(String className, String methodName, String args) throws DeviceException {
		return runServlet(gda.util.simpleServlet.FindableSimpleServlet.SimpleServletName, className, methodName, args);
	}

	/**
	 * @param servletName
	 * @param className
	 * @param methodName
	 * @param args
	 * @param object
	 * @throws DeviceException
	 */
	public static void runServlet(String servletName, String className, String methodName, String args,
			Serializable object) throws DeviceException {
		Object obj = Finder.getInstance().find(servletName);
		if (obj == null || !(obj instanceof Device)) {
			throw new IllegalArgumentException("Unable to find object named " + servletName);
		}
		((Device)obj).setAttribute(className + "?" + methodName + (args != null ? "?" + args : ""), object);
	}

	/**
	 * @param className
	 * @param methodName
	 * @param args
	 * @param object
	 * @throws DeviceException
	 */
	public static void runServlet(String className, String methodName, String args, Serializable object)
			throws DeviceException {
		runServlet(gda.util.simpleServlet.FindableSimpleServlet.SimpleServletName, className, methodName, args, object);
	}

}