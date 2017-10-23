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

package gda.jython.accesscontrol;

import java.lang.reflect.Modifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.device.Device;
import gda.device.corba.impl.DeviceAdapter;
import gda.factory.Findable;
import gda.factory.corba.util.NetService;
import gda.factory.corba.util.RbacEnabledAdapter;

/**
 * Contains methods that handle wrapping of objects to enable role-based access control (RBAC).
 */
public class RbacUtils {

	private static final Logger logger = LoggerFactory.getLogger(RbacUtils.class);

	static boolean wrapWithInterceptors;

	static
	{
		wrapWithInterceptors=LocalProperties.isAccessControlEnabled();
	}

	protected static boolean canProxyUsingCglib(Findable findable) {
		final boolean isFinal = Modifier.isFinal(findable.getClass().getModifiers());
		if (isFinal) {
			logger.warn("Access control cannot be applied to findable '{}' because its class ({}) is final",
					findable.getName(),
					findable.getClass().getName());
		}
		return !isFinal;
	}

	public static Findable wrapFindableWithInterceptor(Findable findable){

		if (!wrapWithInterceptors){
			return findable;
		}

		try {
			if (findable instanceof Device && canProxyUsingCglib(findable)) {
				return DeviceInterceptor.newDeviceInstance((Device) findable);
			}
		} catch (Exception e) {
			logger.warn("Exception while trying to wrap {} with interceptor", findable.getName(), e);
		}
		return findable;
	}

	public static Findable buildProxy(final Findable findable) {

		if (!(findable instanceof RbacEnabledAdapter)) {
			return findable;
		}

		final RbacEnabledAdapter adapter = (RbacEnabledAdapter) findable;

		final org.omg.CORBA.Object corbaObject = adapter.getCorbaObject();
		final String name = findable.getName();
		final NetService netService = adapter.getNetService();

		return buildProxy(findable, corbaObject, name, netService);
	}

	private static Findable buildProxy(Findable theFindable, org.omg.CORBA.Object theDevice, String name, NetService netService) {
		// rebuild every device object inside an RBACProxy
		if (theFindable instanceof DeviceAdapter) {
			return DeviceInterceptor.newDeviceAdapterInstance((DeviceAdapter) theFindable, theDevice,
					name, netService);
		}
		return theFindable;
	}

	/**
	 * Returns {@code true} if the object is a cglib proxy.
	 */
	public static boolean objectIsCglibProxy(Object o) {
		return (o instanceof net.sf.cglib.proxy.Factory);
	}

}
