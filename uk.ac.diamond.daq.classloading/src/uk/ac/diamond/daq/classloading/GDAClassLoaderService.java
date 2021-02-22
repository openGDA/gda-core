/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.classloading;

import java.util.function.Consumer;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

public interface GDAClassLoaderService {

	/**
	 * Obtain a class loader which is capable of loading classes from all bundles/plugins in the application provided
	 * the containing package is exported from its plugin.
	 */
	ClassLoader getClassLoader();

	/**
	 * Obtain a class loader which is capable of loading classes from all bundles/plugins in the application provided
	 * the containing package is exported from its plugin. <br />
	 * This loader allows an additional ClassLoader to be searched first (typically this would be a class from the
	 * library requiring this class loader)
	 *
	 * @param libraryClass
	 *            the ClassLoader of this Class object will be searched first
	 */
	ClassLoader getClassLoaderForLibrary(Class<?> libraryClass);

	/**
	 * Obtain a class loader which is capable of loading classes from all bundles/plugins in the application provided
	 * the containing package is exported from its plugin. <br />
	 * This loader allows an additional ClassLoader to be searched first (typically this would be a class from the
	 * library requiring this class loader)
	 *
	 * @param libraryClass
	 *            the ClassLoader of this Class object will be searched first
	 * @param onSuccess callback when class is successfully loaded
	 */
	ClassLoader getClassLoaderForLibrary(Class<?> libraryClass, Consumer<Class<?>> onSuccess);

	/**
	 * Get the class loader service from OSGi
	 * Possibly not required if there is a solution later implemented for DAQ-2239.
	 *
	 * @return the service instantiated by OSGi DS
	 */
	static GDAClassLoaderService getClassLoaderService() {
		Bundle currentBundle = FrameworkUtil.getBundle(GDAClassLoaderService.class);
		BundleContext bundleContext = currentBundle.getBundleContext();
		ServiceReference<GDAClassLoaderService> serviceReference = bundleContext
				.getServiceReference(GDAClassLoaderService.class);
		return bundleContext.getService(serviceReference);
	}

}
