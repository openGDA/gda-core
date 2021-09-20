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

package uk.ac.diamond.daq.classloading.impl;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.classloading.GDAClassLoaderService;

/**
 * Provides ClassLoaders based on the Osgi bundle loader for this plugin.
 *
 * Note this plugin uses the DS builder to generate the service xml files (in OSGI-INF)
 * automatically from annotations
 */
@Component(name = "GDAOsgiClassLoaderService")
public class GDAOsgiClassLoaderService implements GDAClassLoaderService {

	private static final Logger logger = LoggerFactory.getLogger(GDAOsgiClassLoaderService.class);

	@Override
	public ClassLoader getClassLoader() {
		return new GDAClassLoader();
	}

	@Override
	public ClassLoader getClassLoaderForLibrary(Class<?> libraryClass) {
		return getClassLoaderForLibrary(libraryClass, c -> {});
	}

	@Override
	public ClassLoader getClassLoaderForLibrary(Class<?> libraryClass, Consumer<Class<?>> onSuccess) {
		var libBundle = FrameworkUtil.getBundle(libraryClass);
		return createClassLoader(libraryClass, onSuccess, Set.of(libBundle));
	}

	@Override
	public ClassLoader getClassLoaderForLibrary(Class<?> libraryClass, Consumer<Class<?>> onSuccess,
			Set<String> resourceBundleNames) {
		var resourceBundles = resourceBundleNames.stream().map(this::getBundle).filter(Objects::nonNull).collect(Collectors.toSet());
		return createClassLoader(libraryClass, onSuccess, resourceBundles);
	}

	@Override
	public ClassLoader getClassLoaderForLibraryWithGlobalResourceLoading(Class<?> libraryClass, Set<String> excludedBundles) {
		Set<Bundle> bundles = Arrays.stream(FrameworkUtil.getBundle(getClass()).getBundleContext().getBundles()).collect(Collectors.toSet());
		bundles.removeIf(b -> excludedBundles.contains(b.getSymbolicName()));
		return createClassLoader(libraryClass, c -> {}, bundles);
	}

	private ClassLoader createClassLoader(Class<?> libraryClass, Consumer<Class<?>> onSuccess,
			Set<Bundle> resourceBundles) {
		var libloader = libraryClass.getClassLoader();
		return new GDAClassLoader(libloader, onSuccess, resourceBundles);
	}

	/**
	 * Get the Bundle object from the framework. In the case that there are multiple bundles with the same
	 * BSN, it is not determined which will be provided.
	 * @param bundleName Bundle-SymbolicName
	 * @return the bundle or null if the bundle is not in currently installed
	 */
	private Bundle getBundle(String bundleName) {
		return Arrays.stream(FrameworkUtil.getBundle(getClass()).getBundleContext().getBundles())
				.filter(b -> b.getSymbolicName().equals(bundleName)).findFirst().orElseGet(() -> {
					logger.warn("Bundle {} not present in application", bundleName);
					return null;
				});
	}

}
