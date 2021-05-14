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
package org.eclipse.scanning.jython;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * A simple classloader which can be used as a composite for
 * the purposes of Jython loading.
 *
 * @author Matthew Gerring
 *
 */
class CompositeClassLoader extends ClassLoader {

	private static final Logger logger = LoggerFactory.getLogger(CompositeClassLoader.class);

    private final List<ClassLoader> classLoaders;

    /**
     * Creates a CompositeClassLoader with the argument passed in as the first loader.
     * @param firstLoader
     */
	public CompositeClassLoader(ClassLoader firstLoader) {
		classLoaders = new CopyOnWriteArrayList<>();
		addFirst(firstLoader);
	}

    /**
     * Call to add a loader at the end of this loader
     * @param classLoader
     */
	public void addLast(ClassLoader classLoader) {
		if (validClassLoaderToAdd(classLoader)) {
			classLoaders.add(classLoader);
		}
	}
    /**
     * Call to add a loader at the start of this loader
     * @param classLoader
     */
	public void addFirst(ClassLoader classLoader) {
		if (validClassLoaderToAdd(classLoader)) {
			classLoaders.add(0, classLoader);
		}
	}

	private boolean validClassLoaderToAdd(ClassLoader loader) {
		if (loader == null) {
			throw new IllegalArgumentException("Cannot add null ClassLoader");
		}
		if (classLoaders.contains(loader)) {
			logger.warn("This composite loader already contains the classloader: {}", loader);
			return false;
		}
		return true;
	}

	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {

		for (ClassLoader classLoader : classLoaders) {
			try {
				return classLoader.loadClass(name);
			} catch (ClassNotFoundException notFound) {
				// This is allowable - try next loader
			}
		}

		ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
		if (contextClassLoader != null) {
			return contextClassLoader.loadClass(name);
		} else {
			throw new ClassNotFoundException(name);
		}
	}

}
