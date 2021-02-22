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

import java.util.function.Consumer;

import org.osgi.service.component.annotations.Component;

import uk.ac.diamond.daq.classloading.GDAClassLoaderService;

/**
 * Provides ClassLoaders based on the Osgi bundle loader for this plugin.
 *
 * Note this plugin uses the DS builder to generate the service xml files (in OSGI-INF)
 * automatically from annotations
 */
@Component(name = "GDAOsgiClassLoaderService")
public class GDAOsgiClassLoaderService implements GDAClassLoaderService {

	private final GDAClassLoader defaultDynamicLoader = new GDAClassLoader();

	@Override
	public ClassLoader getClassLoader() {
		return defaultDynamicLoader;
	}

	@Override
	public ClassLoader getClassLoaderForLibrary(Class<?> libraryClass) {
		return getClassLoaderForLibrary(libraryClass, c -> {});
	}

	@Override
	public ClassLoader getClassLoaderForLibrary(Class<?> libraryClass, Consumer<Class<?>> onSuccess) {
		ClassLoader libloader = libraryClass.getClassLoader();
		return new GDAClassLoader(libloader, onSuccess);
	}

}
