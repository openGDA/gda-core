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

package uk.ac.gda.remoting.client;

import gda.factory.Findable;
import gda.observable.IObservable;

import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.remoting.rmi.RmiClientInterceptor;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;
import org.springframework.util.ClassUtils;

/**
 * A Spring {@link FactoryBean} that can be used in place of Spring's standard {@link RmiProxyFactoryBean}. Uses our
 * custom {@link MethodInterceptor}, avoiding remote invocations for certain methods, and connecting the proxy to the
 * GDA event system.
 */
public class GdaRmiProxyFactoryBean extends RmiClientInterceptor implements BeanNameAware, FactoryBean<Object> {

	// TODO allow manipulation of parameters/return value/exceptions, to retain CORBA adapter class behaviour

	/**
	 * Name of the remote object.
	 */
	private String objectName;

	/**
	 * Sets the name of the remote object.
	 */
	public void setObjectName(String objectName) {
		this.objectName = objectName;
	}

	@Override
	public void setBeanName(String name) {
		setObjectName(name);
	}

	private Object serviceProxy;

	@Override
	public void afterPropertiesSet() {
		super.afterPropertiesSet();

		if (objectName == null) {
			throw new IllegalStateException("Property 'objectName' is required");
		}

		if (getServiceInterface() == null) {
			throw new IllegalArgumentException("Property 'serviceInterface' is required");
		}

		// This is our custom interceptor, which handles calls to methods in the IObservable interface
		ClientSideIObservableMethodInterceptor interceptor = new ClientSideIObservableMethodInterceptor();

		// Create the proxy. It will implement all the methods on the service interface (that the object itself
		// implements).
		ProxyFactory pf = new ProxyFactory();
		pf.addInterface(getServiceInterface());
		// Custom interceptor runs first, to deal with the IObservable methods
		pf.addAdvice(interceptor);
		// Then the RMI interceptor runs, doing a RMI for all other method calls
		pf.addAdvice(this);
		this.serviceProxy = pf.getProxy(getBeanClassLoader());

		// If the remote object is observable, create an event receiver object that will receive events relating to
		// the remote object.
		if (remoteObjectIsObservable()) {
			try {
				ClientSideEventReceiver receiver = new ClientSideEventReceiver();
				receiver.setObjectName(objectName);
				receiver.setProxy(serviceProxy);
				receiver.setObservableComponent(interceptor.getObservableComponent());
				receiver.afterPropertiesSet();
			} catch (Exception e) {
				throw new RuntimeException("Unable to receive events for remote object", e);
			}
		}

		// If the remote object is Findable, check the name used for the RMI proxy matches the name of the remote
		// object. If they don't match the creation of the RMI proxy can cause the remote object's name to change!
		if (remoteObjectIsFindable()) {
			final Findable remoteFindable = (Findable) serviceProxy;
			final String remoteObjectName = remoteFindable.getName();
			if (!objectName.equals(remoteObjectName)) {
				logger.warn(String.format("RMI proxy has name \"%s\" but remote object has name \"%s\"", objectName, remoteObjectName));
			}
		}
	}

	private boolean remoteObjectIsObservable() {
		return ClassUtils.isAssignable(IObservable.class, getServiceInterface());
	}

	private boolean remoteObjectIsFindable() {
		return ClassUtils.isAssignable(Findable.class, getServiceInterface());
	}

	@Override
	public Object getObject() throws Exception {
		return this.serviceProxy;
	}

	@Override
	public Class<?> getObjectType() {
		return getServiceInterface();
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

}
