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

/**
 * This is a wrapper loader around the ClassLoader for this class which (under Equinox) is the
 * EquinoxClassLoader for this plugin. This plugin is configured to use OSGi DynamicImport-Package
 * allowing it to load exported classes from any plugin.
 *
 * There is optionally an additional loader involved which allows for the case where a library might
 * need to load its own internal (non exported) classes in addition to other classes.
 *
 * Note that this ClassLoader isn't really responsible for loading anything itself but rather to provide
 * an access point to OSGi dynamic imports, that is to say that for each Class object returned, the classloader
 * field will be the EquinoxClassLoader assosicated with the classes containing plugin.
 */
public class GDAClassLoader extends ClassLoader {

	private static final ClassLoader DYNAMIC_LOADER = GDAClassLoader.class.getClassLoader();

	private final Consumer<Class<?>> successCallback;

	/**
	 * ClassLoader with parent set to the bundle loader for this class
	 */
	public GDAClassLoader() {
		super(DYNAMIC_LOADER);
		successCallback = c -> {};
	}

	/**
	 * ClassLoader which will try provided loader first followed by bundle loader
	 * @param support loader to search first
	 * @param successCallback called when a class has been successfully loaded should be threadsafe
	 */
	public GDAClassLoader(ClassLoader support, Consumer<Class<?>> successCallback) {
		super(support);
		this.successCallback = successCallback;
	}

	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		// Delegate to parent class loader, if that fails try default dynamic loader
		try {
			Class<?> cls = getParent().loadClass(name);
			successCallback.accept(cls);
			return cls;
		} catch (ClassNotFoundException e) {
			if (getParent() != DYNAMIC_LOADER) {
				Class<?> cls = DYNAMIC_LOADER.loadClass(name);
				successCallback.accept(cls);
				return cls;
			}
			throw e;
		}
	}
}
