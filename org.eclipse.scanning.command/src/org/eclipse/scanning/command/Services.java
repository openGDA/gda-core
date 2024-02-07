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
package org.eclipse.scanning.command;

import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.device.IScannableDeviceService;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.points.IPointGeneratorService;

import uk.ac.diamond.daq.util.logging.deprecation.DeprecationLogger;
import uk.ac.diamond.osgi.services.ServiceProvider;
/**
 * This class holds services for the scanning server servlets. Services should be configured to be optional and dynamic
 * and will then be injected correctly by Equinox DS.
 *
 * @author Matthew Gerring
 * @author Colin Palmer
 *
 */
@Deprecated(since="9.35", forRemoval=true)
public class Services {

	private static final DeprecationLogger logger = DeprecationLogger.getLogger(Services.class);

	public static IScannableDeviceService getScannableDeviceService() {
		logger.deprecatedMethod("getScannableDeviceService()", "9.35",
			  "ServiceProvider.getService(IScannableDeviceService.class)");
		return ServiceProvider.getService(IScannableDeviceService.class);
	}

	public void setScannableDeviceService(IScannableDeviceService scannableDeviceService) {
		logger.deprecatedMethod("setScannableDeviceService()", "9.35", "Nothing, just remove");
	}

	public static IEventService getEventService() {
		logger.deprecatedMethod("getEventService()", "9.35",
			  "ServiceProvider.getService(IEventService.class)");
		return ServiceProvider.getService(IEventService.class);
	}

	public void setEventService(IEventService eventService) {
		logger.deprecatedMethod("setEventService()", "9.35", "Nothing, just remove");
	}

	public static IPointGeneratorService getGeneratorService() {
		logger.deprecatedMethod("getGeneratorService()", "9.35",
			  "ServiceProvider.getService(IPointGeneratorService.class)");
		return ServiceProvider.getService(IPointGeneratorService.class);
	}

	public void setGeneratorService(IPointGeneratorService generatorService) {
		logger.deprecatedMethod("setGeneratorService()", "9.35", "Nothing, just remove");
	}

	public static IRunnableDeviceService getRunnableDeviceService() {
		logger.deprecatedMethod("getRunnableDeviceService()", "9.35",
			  "ServiceProvider.getService(IRunnableDeviceService.class)");
		return ServiceProvider.getService(IRunnableDeviceService.class);
	}

	public void setRunnableDeviceService(IRunnableDeviceService deviceService) {
		logger.deprecatedMethod("setRunnableDeviceService()", "9.35", "Nothing, just remove");
	}
}
