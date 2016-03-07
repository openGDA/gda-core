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

package uk.ac.gda.remoting.server;

import gda.factory.Findable;
import gda.observable.IObservable;

import java.rmi.RemoteException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.remoting.rmi.RmiServiceExporter;

import uk.ac.gda.remoting.ServiceInterface;

/**
 * A bean that can be used in place of Spring's {@link RmiServiceExporter}. Makes an object remotely available using
 * RMI. Also creates an observer for the 'real' object that injects events into the GDA event system.
 */
public class GdaRmiServiceExporter implements InitializingBean {

	// TODO allow manipulation of parameters/return value/exceptions, to retain CORBA impl class behaviour

	private static final Logger logger = LoggerFactory.getLogger(GdaRmiServiceExporter.class);

	private Class<?> serviceInterface;

	public void setServiceInterface(Class<?> serviceInterface) {
		this.serviceInterface = serviceInterface;
	}

	public Class<?> getServiceInterface() {
		return serviceInterface;
	}

	private Object service;

	public void setService(Object service) {
		this.service = service;
	}

	public Object getService() {
		return service;
	}

	private String serviceName;

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getServiceName() {
		return serviceName;
	}

	private RmiServiceExporter serviceExporter = new RmiServiceExporter();

	@Override
	public void afterPropertiesSet() throws RemoteException {
		if (getService() == null) {
			throw new RemoteException("The 'service' property is required");
		}

		// If a service name hasn't been explicitly set, try to determine it automatically
		if (getServiceName() == null) {
			if (getService() instanceof Findable) {
				final Findable findable = (Findable) getService();
				final String findableName = findable.getName();
				final String serviceName = "gda/" + findableName;
				logger.debug("'serviceName' property was not set; using automatically-generated name '{}'", serviceName);
				setServiceName(serviceName);
			} else {
				throw new RemoteException("Property 'serviceName' is not set, and as the object doesn't implement Findable (which has the getName() method), I can't determine a name automatically");
			}
		}

		// If the service interface hasn't been explicitly set, try to determine it automatically by looking for
		// a @ServiceInterface annotation
		if (getServiceInterface() == null) {
			final Class<?> serviceClass = getService().getClass();
			ServiceInterface serviceInterfaceAnnotation = AnnotationUtils.findAnnotation(serviceClass, ServiceInterface.class);
			if (serviceInterfaceAnnotation == null) {
				throw new RemoteException("Property 'serviceInterface' is not set, and I couldn't automatically determine the service interface for class " + serviceClass.getName());
			}
			final Class<?> serviceInterface = serviceInterfaceAnnotation.value();
			logger.debug("Automatically determined the service interface for {} to be {}", serviceClass.getName(), serviceInterface.getName());
			setServiceInterface(serviceInterface);
		}

		// Initialise the real service exporter
		serviceExporter.setService(getService());
		serviceExporter.setServiceName(getServiceName());
		serviceExporter.setServiceInterface(getServiceInterface());
		// Substitute my class loader so that we can see the Spring Jars to get the exported beans
		ClassLoader tccl = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
			serviceExporter.afterPropertiesSet();

			try {
				setupEventDispatch();
				logger.debug("Service " + getServiceName() + " exported");
			} catch (Exception e) {
				throw new RemoteException("Unable to export service", e);
			}
		} finally {
			Thread.currentThread().setContextClassLoader(tccl);
		}
	}

	private void setupEventDispatch() throws Exception {
		final Object service = getService();
		if (service instanceof IObservable && service instanceof Findable) {
			final Findable findable = (Findable) service;
			final IObservable observable = (IObservable) service;

			ServerSideEventDispatcher observer = new ServerSideEventDispatcher();
			observer.setSourceName(findable.getName());
			observer.setObject(observable);
			observer.afterPropertiesSet();
		}

		else {
			throw new RuntimeException("Object " + serviceName + " must implement IObservable and Findable in order for events to be sent over CORBA. Use " + RmiServiceExporter.class.getName() + " instead of " + getClass().getSimpleName() + " if event handling is not necessary.");
		}
	}

}
