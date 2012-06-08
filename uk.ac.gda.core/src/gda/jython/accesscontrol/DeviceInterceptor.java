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

package gda.jython.accesscontrol;

import gda.device.Device;
import gda.device.corba.impl.DeviceAdapter;
import gda.factory.corba.util.NetService;
import gda.jython.JythonServerFacade;
import gda.jython.JythonServer.JythonServerThread;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.python.core.PyObject;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.Factory;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

/**
 * Implementation of the CGLIB MethodInterceptor interface. This object acts as a proxy around other objects and
 * intercepts all method calls. If a method is called from within a thread originating from the Command Server, and the
 * access level of that thread is not equal or greater than the protection level of the object this class encapsulates,
 * then the method is not called and an exception thrown.
 */
public class DeviceInterceptor extends PyObject implements MethodInterceptor {

	private Device theObject;
	private ProtectedMethodComponent protectedMethods;

	/**
	 * Factory method to create a copy of the supplied object encapsulated by an RBACInterceptor object. This object
	 * should be used as a proxy to the original.
	 * 
	 * @param theObject
	 * @return the wrapped object
	 */
	public static Device newDeviceInstance(Device theObject) {
		// create the object which will do the work when the proxied object is
		// called
		MethodInterceptor interceptor = new DeviceInterceptor(theObject);

		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(theObject.getClass());
		enhancer.setCallback(interceptor);

		// create using the null constructor. We know that this is safe as the object could not be called by Castor
		// otherwise.
		Object proxyObject = enhancer.create();

		((Factory) proxyObject).setCallback(0, interceptor);

		return (Device) proxyObject;
	}

	/**
	 * Factory method to create a DeviceAdapter wrapped in a CGLIB proxy for method interception.
	 * 
	 * @param theCorbaDevice
	 * @param theDevice
	 * @param name
	 * @param netService
	 * @return DeviceAdapter
	 */
	public static DeviceAdapter newDeviceAdapterInstance(DeviceAdapter theDevice, org.omg.CORBA.Object theCorbaDevice,
			String name, NetService netService) {

		MethodInterceptor interceptor = new DeviceInterceptor(theDevice);

		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(theDevice.getClass());
		enhancer.setCallback(interceptor);

		// As we are creating DeviceAdapters we know that they all have the same form of constructor:
		// org.omg.CORBA.Object obj, String name, NetService netService

		Object[] args = new Object[] { theCorbaDevice, name, netService };
		Class<?>[] parameterTypes = new Class<?>[] { org.omg.CORBA.Object.class, String.class, NetService.class };

		Object proxyObject = enhancer.create(parameterTypes, args);

		((Factory) proxyObject).setCallback(0, interceptor);

		return (DeviceAdapter) proxyObject;
	}

	/**
	 * Constructor
	 * 
	 * @param theDevice
	 */
	public DeviceInterceptor(Device theDevice) {
		this.theObject = theDevice;
		this.protectedMethods = new ProtectedMethodComponent(theDevice.getClass());
	}

	protected Object callProtectedMethodInJythonServerThread(Device theObject, Method method, Object[] args)
			throws Throwable {
		JythonServerThread currentThread = (JythonServerThread) Thread.currentThread();
		// are we in a thread from the JythonServer and have enough permission to run the method
		if (currentThread.hasBeenAuthorised) {
			return InterceptorUtils.invokeMethod(method, theObject, args);
		} else if (currentThread.authorisationLevel >= theObject.getProtectionLevel()) {
			try {
				currentThread.hasBeenAuthorised = true;
				Object ret = method.invoke(theObject, args);
				currentThread.hasBeenAuthorised = false;
				return ret;
			} catch (InvocationTargetException ite) {
				throw ite.getCause();
			}
		}

		// throw exception telling user that there's not enough permissions
		if (currentThread.authorisationLevel == 0) {
			throw new AccessDeniedException(AccessDeniedException.NOBATON_EXCEPTION_MESSAGE);
		}
		throw new AccessDeniedException("You need a permission level of " + theObject.getProtectionLevel()
				+ " to perform this operation. Your current level is " + currentThread.authorisationLevel + ".");

	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see net.sf.cglib.proxy.MethodInterceptor#intercept(java.lang.Object, java.lang.reflect.Method,
	 *      java.lang.Object[], net.sf.cglib.proxy.MethodProxy)
	 */
	@Override
	public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {

		if (this.protectedMethods.isAnInvokeMethod(method)) {
			// most of time we are jython and are simply calling invoke...
			return this.protectedMethods.testForJythonInvocation(obj, method, args);
		}

		// if it is the equals method, then we want to call the interceptor's equals instead (for example when looking
		// in arrays of objects)
		if (method.getName().equals("equals") && method.getParameterTypes().length == 1
				&& method.getParameterTypes()[0] == java.lang.Object.class) {
			return this.equals(args[0]);
		}
		// __tojava__ is called by Jython code at various points e.g. pos scannable value
		if (method.getName().equals("__tojava__") && method.getParameterTypes().length == 1) {
			return obj;
		}

		// if get here then we are directly calling the objects methods

		// let's not be limited by protected modifier!
		method.setAccessible(true);

		// is the method protected?
		if (this.protectedMethods.isMethodProtected(method)) {
			// are we in a thread from the JythonServer and have enough permission to run the method
			if (Thread.currentThread() instanceof JythonServerThread) {
				return callProtectedMethodInJythonServerThread(theObject, method, args);
			}

			if (theObject instanceof DeviceAdapter) {
				// else get authorisation from JythonServerFacade
				if (JythonServerFacade.getInstance().getAuthorisationLevel() >= theObject.getProtectionLevel()) {
					return InterceptorUtils.invokeMethod(method, theObject, args);
				}

				// throw exception telling user that there's not enough permissions
				if (JythonServerFacade.getInstance().getAuthorisationLevel() == 0) {
					throw new AccessDeniedException(AccessDeniedException.NOBATON_EXCEPTION_MESSAGE);
				}
				throw new AccessDeniedException("You need a permission level of " + theObject.getProtectionLevel()
						+ " to perform this operation. Your current level is "
						+ JythonServerFacade.getInstance().getAuthorisationLevel() + ".");
			}
		}
		// else simply call the method
		return InterceptorUtils.invokeMethod(method, theObject, args);
	}

	@Override
	public boolean equals(Object obj) {

		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		
		if (theObject instanceof DeviceAdapter && !(obj instanceof net.sf.cglib.proxy.Factory)) {
			return this.theObject.equals(obj);
		}

		if (!(obj instanceof net.sf.cglib.proxy.Factory)) {
			return false;
		}

		net.sf.cglib.proxy.Factory other = (net.sf.cglib.proxy.Factory) obj;
		net.sf.cglib.proxy.Callback callback = other.getCallback(0);
		if (!(callback instanceof DeviceInterceptor)) {
			return false;
		}

		DeviceInterceptor otherDevInterceptor = (DeviceInterceptor) callback;
		return theObject.equals(otherDevInterceptor.theObject);
	}

	@Override
	public int hashCode() {
		if (theObject instanceof DeviceAdapter) {
			// base on wrapped object
			return theObject.hashCode();
		}

		return super.hashCode();
	}

}
