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

import static java.util.Collections.enumeration;
import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

import org.osgi.framework.Bundle;

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
 * field will be the EquinoxClassLoader associated with the classes containing plugin.
 */
public class GDAClassLoader extends ClassLoader {

	private static final ClassLoader DYNAMIC_LOADER = GDAClassLoader.class.getClassLoader();

	private final Consumer<Class<?>> successCallback;

	private final Set<Bundle> resourceBundles;

	/**
	 * ClassLoader with parent set to the bundle loader for this class
	 */
	public GDAClassLoader() {
		super(DYNAMIC_LOADER);
		successCallback = c -> {};
		resourceBundles = Collections.emptySet();
	}

	/**
	 * ClassLoader which will try provided loader first followed by bundle loader
	 *
	 * This constructor sets the support loader as the parent class loader.
	 *
	 * @param support
	 *            loader to search first
	 * @param successCallback
	 *            called when a class has been successfully loaded, should be threadsafe
	 */
	public GDAClassLoader(ClassLoader support, Consumer<Class<?>> successCallback, Set<Bundle> resourceBundles) {
		super(support);
		this.successCallback = successCallback;
		this.resourceBundles = unmodifiableSet(resourceBundles);
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

	@Override
	public URL getResource(String name) {
		// Try and find the resource in the bundles, return the first match
		return resourceBundles.stream().map(b -> b.getResource(name))
				.filter(Objects::nonNull)
				.findFirst().orElse(null);
	}

	@Override
	public Enumeration<URL> getResources(String name) throws IOException {
		// Look through all bundles and return matches
		try {
			return enumeration(resourceBundles.stream().map(b -> getResourcesFromBundle(name, b))
				.flatMap(Collection::stream).collect(toList()));
		} catch (LoaderIOException e) {
			throw e.getCause();
		}
	}

	private Collection<URL> getResourcesFromBundle(String name, Bundle bundle) {
		List<URL> result = new ArrayList<>();
		try {
			var bResources = bundle.getResources(name);
			if (bResources != null) {
				bResources.asIterator().forEachRemaining(result::add);
			}
			return result;
		} catch (IOException e) {
			throw new LoaderIOException(e);
		}
	}

	/**
	 * Custom exception to allow the throwing {@link Bundle#getResources(String)} to be used in stream
	 */
	private static class LoaderIOException extends UncheckedIOException {
		public LoaderIOException(IOException cause) {
			super(cause);
		}
	}
}
