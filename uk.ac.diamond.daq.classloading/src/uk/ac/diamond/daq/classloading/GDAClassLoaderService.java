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

import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import uk.ac.diamond.daq.classloading.impl.SystemClassLoadingService;

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
	 * library requiring this class loader)  <br />
	 * The library bundle will be used to load resources.
	 *
	 * @param libraryClass
	 *            the ClassLoader of this Class object will be searched first
	 */
	ClassLoader getClassLoaderForLibrary(Class<?> libraryClass);

	/**
	 * Obtain a class loader which is capable of loading classes from all bundles/plugins in the application provided
	 * the containing package is exported from its plugin. <br />
	 * This loader allows an additional ClassLoader to be searched first (typically this would be a class from the
	 * library requiring this class loader) <br />
	 * The library bundle will be used to load resources.
	 *
	 * @param libraryClass
	 *            the ClassLoader of this Class object will be searched first
	 * @param onSuccess callback when class is successfully loaded
	 */
	ClassLoader getClassLoaderForLibrary(Class<?> libraryClass, Consumer<Class<?>> onSuccess);


	/**
	 * Obtain a class loader which is capable of loading classes from all bundles/plugins in the application provided
	 * the containing package is exported from its plugin. <br />
	 * This loader allows an additional ClassLoader to be searched first (typically this would be a class from the
	 * library requiring this class loader). <br />
	 *
	 * Resources may be loaded from any of the bundles provided in the {@code resourceBundleNames} parameter.
	 *
	 * @param libraryClass
	 *            the ClassLoader of this Class object will be searched first
	 * @param onSuccess
	 *            callback when class is successfully loaded
	 * @param resourceBundleNames
	 *            set of Bundle-SymbolicNames for bundles to search for resources
	 */
	ClassLoader getClassLoaderForLibrary(Class<?> libraryClass, Consumer<Class<?>> onSuccess,
			Set<String> resourceBundleNames);

	/**
	 * Obtain a class loader which is capable of loading classes from all bundles/plugins in the application provided
	 * the containing package is exported from its plugin. <br />
	 * This loader allows an additional ClassLoader to be searched first (typically this would be a class from the
	 * library requiring this class loader). <br />
	 *
	 * Resources may be loaded from any of the bundles present in the application.
	 *
	 * @param libraryClass
	 *            the ClassLoader of this Class object will be searched first
	 * @param excludedBundles set of bundle symbolic names to exclude from the resource loading
	 */
	ClassLoader getClassLoaderForLibraryWithGlobalResourceLoading(Class<?> libraryClass, Set<String> excludedBundles);

	/**
	 * Get the class loader service from OSGi if it is running otherwise a null service
	 * will be returned. <br />
	 * Possibly not required if there is a solution later implemented for DAQ-2239.
	 *
	 * @return the service instantiated by OSGi DS or a service providing null class loaders if OSGi is not running
	 */
	static GDAClassLoaderService getClassLoaderService() {
		if (Platform.isRunning()) {
			Bundle currentBundle = FrameworkUtil.getBundle(GDAClassLoaderService.class);
			BundleContext bundleContext = currentBundle.getBundleContext();
			ServiceReference<GDAClassLoaderService> serviceReference = bundleContext
					.getServiceReference(GDAClassLoaderService.class);
			return bundleContext.getService(serviceReference);
		}
		return new SystemClassLoadingService();
	}

	/**
	 * Create a {@link TemporaryContextClassLoader} that uses the service provided class
	 * loader and can automatically revert to the existing class loader when
	 * {@link TemporaryContextClassLoader#close() closed}.
	 * <pre>
	 * try (var tccl = temporaryClassLoader()) {
	 *     // Code requiring new class loader
	 * }
	 * </pre>
	 * @see #temporaryClassLoader(Function) temporaryClassLoader(Function) for alternative
	 *       where customisation of the class loader is required
	 * @return A {@link TemporaryContextClassLoader} wrapping the service's class loader
	 */
	static TemporaryContextClassLoader temporaryClassLoader() {
		return new TemporaryContextClassLoader(getClassLoaderService().getClassLoader());
	}
	/**
	 * Create a {@link TemporaryContextClassLoader} that uses the service provided class
	 * loader and can automatically revert to the existing class loader when
	 * {@link TemporaryContextClassLoader#close() closed}.
	 * <pre>
	 * try (var tccl = temporaryClassLoader(s -> s.getClassLoaderForLibrary(LibraryClass.class))) {
	 *     // Code requiring new class loader
	 * }
	 * </pre>
	 * @see #temporaryClassLoader() temporaryClassLoader() for alternative where customisation is not required
	 * @param configurer Function to get a class loader from the class loader service
	 * @return A {@link TemporaryContextClassLoader} wrapping the service's class loader
	 */
	static TemporaryContextClassLoader temporaryClassLoader(Function<GDAClassLoaderService, ClassLoader> configurer) {
		return new TemporaryContextClassLoader(configurer.apply(getClassLoaderService()));
	}

}
