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

import gda.device.Device;
import gda.device.DeviceBase;
import gda.device.DeviceException;
import uk.ac.gda.api.remoting.ServiceInterface;

/**
 * FindableSimpleServlet Class
 */
@ServiceInterface(Device.class)
public class FindableSimpleServlet extends DeviceBase {

	/**
	 * Servlet Name
	 */
	public static final String SimpleServletName = "SimpleServlet";

	/**
	 * Constructor
	 */
	public FindableSimpleServlet() {
	}

	@Override
	public Object getAttribute(String attributeName) throws DeviceException {
		Object returnObj = null;
		try {
			returnObj = SimpleServlet.execute(attributeName);
		} catch (InvocationTargetException e) {
			throw new DeviceException("Error getting attribute: " + attributeName,  e.getTargetException());
		} catch (Exception e) {
			throw new DeviceException("Error getting attribute: " + attributeName, e);
		}
		return returnObj;
	}

	@Override
	public void setAttribute(String attributeName, Object value) throws DeviceException {
		try {
			SimpleServlet.execute(attributeName, value);
		} catch (InvocationTargetException e) {
			throw new DeviceException("FindableSimpleServlet. InvocationTargetException "
					+ e.getTargetException().getMessage(), e.getTargetException());
		} catch (Exception e) {
			throw new DeviceException("FindableSimpleServlet. Exception " + e.getMessage(), e);
		}
	}

}
