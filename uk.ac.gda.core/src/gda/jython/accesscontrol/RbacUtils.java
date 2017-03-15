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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

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
			logger.warn("Access control cannot be applied to findable " + StringUtils.quote(findable.getName()) + " because its class (" + findable.getClass().getName() + ") is final");
		}
		return !isFinal;
	}

	public static Findable wrapFindableWithInterceptor(Findable findable){

		if (!wrapWithInterceptors){
			return findable;
		}

		try {
			if (findable instanceof Device && canProxyUsingCglib(findable)) {
				findable = DeviceInterceptor.newDeviceInstance((Device) findable);
			} else if (isOEInPath()) {
				if (OE.isInstance(findable) && canProxyUsingCglib(findable)) {
					findable = (Findable) newoeinstance.invoke(OEInterceptor,OE.cast(findable));
				}
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

	@SuppressWarnings({ "null" })
	private static Findable buildProxy(Findable theFindable, org.omg.CORBA.Object theDevice, String name, NetService netService) {
		//test to see if we are creating OE wrappers as well
		boolean oeInPath = true;
		Class<?> OEInterceptor = null;
		Class<?> OEAdapter = null;
		java.lang.reflect.Method newoeinstance = null;
		try {
			OEAdapter = Class.forName("gda.oe.corba.impl.OeAdapter");
			OEInterceptor = Class.forName("gda.jython.accesscontrol.OEInterceptor");
		} catch (Exception e1) {
			oeInPath = false;
		}

		if (oeInPath) {
			try {
				newoeinstance = OEInterceptor.getMethod("newOEAdapterInstance", OEAdapter, org.omg.CORBA.Object.class,String.class,NetService.class);
			} catch (NoSuchMethodException e) {
				oeInPath = false;
				logger.warn("OE classes are available, but the newOEAdapterInstance method could not be found");
			}
		}

		// rebuild every device object inside an RBACProxy
		if (theFindable instanceof DeviceAdapter) {
			Findable findableProxy = DeviceInterceptor.newDeviceAdapterInstance((DeviceAdapter) theFindable, theDevice,
					name, netService);
			return findableProxy;
		} else if (oeInPath) {
			try {
				if (OEAdapter.isInstance(theFindable)) {
					Findable findableProxy;
					findableProxy = (Findable) newoeinstance.invoke(OEInterceptor,
						theFindable,
						theDevice,
						name,
						netService);
					return findableProxy;
				}
			} catch (Exception e) {
				logger.warn("Exception while trying to create an OEInterceptor", e);
			}
		}
		return theFindable;
	}

	// The below methods and fields are for OEs only - they should be removed with OEs.
	// The reflection is done to prevent the core plugin being dependent on the oe plugin.

	static Method newoeinstance;

	static Boolean oeInPath;

	static Class<?> OEInterceptor;

	static Class<?> OE;

	private synchronized static boolean isOEInPath(){
		if ( oeInPath == null){
			try {
				OE = Class.forName("gda.oe.OE");
				OEInterceptor = Class.forName("gda.jython.accesscontrol.OEInterceptor");
				newoeinstance = OEInterceptor.getMethod("newOEInstance", OE);
				oeInPath = true;
			} catch (Exception e1) {
				oeInPath = false;
			}
		}
		return oeInPath;
	}

	/**
	 * Returns {@code true} if the object is a cglib proxy.
	 */
	public static boolean objectIsCglibProxy(Object o) {
		return (o instanceof net.sf.cglib.proxy.Factory);
	}

}
