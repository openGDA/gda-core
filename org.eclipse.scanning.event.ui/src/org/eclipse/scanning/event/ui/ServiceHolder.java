/*-
 *******************************************************************************
 * Copyright (c) 2011, 2016 Diamond Light Source Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthew Gerring - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.scanning.event.ui;

import org.eclipse.scanning.api.device.IScannableDeviceService;
import org.eclipse.scanning.api.event.IEventConnectorService;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.scan.IBeanSummariser;
import org.eclipse.scanning.api.event.scan.ScanBeanSummariser;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class ServiceHolder {

	private static IEventConnectorService eventConnectorService;
	private static IEventService          eventService;
	private static IPointGeneratorService generatorService;
	private static IScannableDeviceService deviceConnectorService;
	private static BundleContext context;
	private static IBeanSummariser beanSummary;

	public static IEventConnectorService getEventConnectorService() {
		if (eventConnectorService==null) eventConnectorService = getService(IEventConnectorService.class);
		return eventConnectorService;
	}

	public void setEventConnectorService(IEventConnectorService eventService) {
		ServiceHolder.eventConnectorService = eventService;
	}

	public static IEventService getEventService() {
		if (eventService==null) eventService = getService(IEventService.class);
		return eventService;
	}

	public void setEventService(IEventService eventService) {
		ServiceHolder.eventService = eventService;
	}

	public static IPointGeneratorService getGeneratorService() {
		if (generatorService==null) generatorService = getService(IPointGeneratorService.class);
		return generatorService;
	}

	public void setGeneratorService(IPointGeneratorService generatorService) {
		ServiceHolder.generatorService = generatorService;
	}

	public static IScannableDeviceService getDeviceConnectorService() {
		if (deviceConnectorService==null) deviceConnectorService = getService(IScannableDeviceService.class);
		return deviceConnectorService;
	}

	public void setDeviceConnectorService(IScannableDeviceService deviceConnectorService) {
		ServiceHolder.deviceConnectorService = deviceConnectorService;
	}

	public void start(BundleContext context) {
		ServiceHolder.context = context;
	}

	private static <T> T getService(Class<T> clazz) {
		if (context == null) return null;
		try {
			ServiceReference<T> ref = context.getServiceReference(clazz);
	        return context.getService(ref);
		} catch (NullPointerException npe) {
			return null;
		}
	}

	public static IBeanSummariser getBeanSummariser() {
		if (beanSummary == null) beanSummary = new ScanBeanSummariser();
		return beanSummary;
	}
}