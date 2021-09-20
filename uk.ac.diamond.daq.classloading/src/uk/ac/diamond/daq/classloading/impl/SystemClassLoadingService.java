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

import java.util.Set;
import java.util.function.Consumer;

import uk.ac.diamond.daq.classloading.GDAClassLoaderService;

/**
 * Each method returns the same loader, the system class loader.
 * This is designed to be used in non OSGi where the system loader
 * is usually capable of loading all classes.
 */
public class SystemClassLoadingService implements GDAClassLoaderService {

	private static final ClassLoader LOADER = ClassLoader.getSystemClassLoader();

	@Override
	public ClassLoader getClassLoader() {
		return LOADER;
	}

	@Override
	public ClassLoader getClassLoaderForLibrary(Class<?> libraryClass) {
		return LOADER;
	}

	@Override
	public ClassLoader getClassLoaderForLibrary(Class<?> libraryClass, Consumer<Class<?>> onSuccess) {
		return LOADER;
	}

	@Override
	public ClassLoader getClassLoaderForLibrary(Class<?> libraryClass, Consumer<Class<?>> onSuccess,
			Set<String> resourceBundleNames) {
		return LOADER;
	}

	@Override
	public ClassLoader getClassLoaderForLibraryWithGlobalResourceLoading(Class<?> libraryClass, Set<String> excludedBundles) {
		return LOADER;
	}
}
