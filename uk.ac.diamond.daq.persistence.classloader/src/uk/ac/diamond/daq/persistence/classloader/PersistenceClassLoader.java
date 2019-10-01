/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.persistence.classloader;

import static org.osgi.framework.Constants.EXPORT_PACKAGE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.osgi.util.ManifestElement;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.persistence.implementation.service.PersistenceException;

public class PersistenceClassLoader extends ClassLoader implements IPersistenceClassLoader {
	private static final Logger logger = LoggerFactory.getLogger(PersistenceClassLoader.class);

	private static final PersistenceClassLoader instance = new PersistenceClassLoader();

	private boolean initialised = false;

	// For each package, map the bundle(s) in which it is defined
	private Map<String, List<Bundle>> packageMap = new HashMap<>();

	private PersistenceClassLoader() {
		// prevent instantiation
	}

	public static PersistenceClassLoader getInstance() {
		return instance;
	}

	@Override
	public void initialise(Bundle[] contextBundles) throws PersistenceException {
		logger.debug("Initialising persistence class loader");
		initialisePackageMap(contextBundles);
		initialised = true;
	}

	@Override
	public boolean isInitialised() {
		return initialised;
	}

	@Override
	public Class<?> forName(String canonicalName) throws PersistenceException {
		if (canonicalName == null || canonicalName.length() == 0) {
			throw new PersistenceException(canonicalName + " not found");
		}

		final Class<?> loadedClass = findLoadedClass(canonicalName); // check we don't already have it
		if (loadedClass != null) {
			return loadedClass;
		}

		for (Bundle bundle : getMatchingBundlesForName(canonicalName)) {
			try {
				final Class<?> theClass = bundle.loadClass(canonicalName);
				logger.debug("Loaded class {} from bundle {}", canonicalName, bundle);
				return theClass;
			} catch (ClassNotFoundException er) {
				// try the next bundle
			}
		}

		// This is the last resort and will almost certainly fail
		try {
			return super.loadClass(canonicalName);
		} catch (ClassNotFoundException e) {
			throw new PersistenceException("Error loading class " + canonicalName, e);
		}
	}

	private void initialisePackageMap(Bundle[] contextBundles) throws PersistenceException {
		try {
			for (Bundle bundle : contextBundles) {
				final ManifestElement[] exportPackages = ManifestElement.parseHeader(EXPORT_PACKAGE, bundle.getHeaders().get(EXPORT_PACKAGE));
				if (exportPackages != null) {
					for (ManifestElement element : exportPackages) {
						final String pkgName = element.getValue();
						final List<Bundle> bundles = packageMap.get(pkgName);
						if (bundles == null) {
							packageMap.put(pkgName, new ArrayList<Bundle>(Arrays.asList(bundle)));
						} else {
							bundles.add(bundle);
						}
					}
				}
			}
		} catch (Exception e) {
			throw new PersistenceException("Error initialising package map", e);
		}
	}

	/**
	 * Builds a set of bundles that match the supplied possible Java class name
	 */
	private List<Bundle> getMatchingBundlesForName(final String potentialJavaClassName) {
		List<Bundle> matchingBundles = new ArrayList<>();
		final int packageBoundary = potentialJavaClassName.lastIndexOf('.');
		if (packageBoundary > 0) {
			final String packageName = potentialJavaClassName.substring(0, packageBoundary);
			if (packageName != null && packageName.length() > 0 && packageMap.containsKey(packageName)) {
				matchingBundles = packageMap.get(packageName);
			}
		}
		return matchingBundles;
	}
}
