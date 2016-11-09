/*-
 * Copyright © 2016 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

package gda.jython;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.osgi.framework.Bundle;

/**
 * A Classloader for use with Jython in the GDA environment. It retrieves the class from the correct bundle within the
 * product's OSGi context based on a map initialised at startup from the BundleContext or delegates to its parent.
 */
public class GDAJythonClassLoader extends ClassLoader {

	private static boolean staticInitialized = false;
	private static Map<String, String> standardScriptFolders;
	private static Set<Bundle> initializedBundles;
	private static Map<String, Map<Bundle, Boolean>> packageMap = new HashMap<>();

	// These bundles cause problems/noise when you attempt to load classes: the eclipse ones produce large stack traces if
	// the class is not found (i.e. each time a python source module name is supplied) rather than simply throwing the
	// exception. The scanning command package actually leads to a Jython conflict since it has an embedded version 2.7
	// of Jython whereas the rest of the product uses version 2.5. Once we have migrated the target platform to version
	// 2.7 it can be removed from the list.
	private static final String SKIPPED_BUNDLES = "org.eclipse.debug.ui, org.eclipse.help.ui, org.eclipse.scanning.command, org.python.pydev.jython";

	/**
	 * Use the return value of this function to get all the packages available to Jython. Call PySystemState.add_package
	 * on each one.
	 *
	 * @return Returns the packages (including those marked as not part of the Jython API).
	 */
	public Set<String> getJythonPackages() {
		return packageMap.keySet();
	}

	/**
	 * Tests whether the specified bundle is mapped by the server to allow non-server bundles
	 * to be filtered out when initialising the Jython Interpreter
	 * @param bundleName		Name of the bundle to be checked
	 * @return					true if the bundle is part of the servers's OSGi namespace
	 */
	public boolean isMappedBundle(final String bundleName) {
		return initializedBundles.stream().anyMatch(bundle -> bundle.getSymbolicName().equals(bundleName));
	}

	/**
	 * Determines whether the combination of a package and its owning bundle is included in the supported Jython API
	 * returning an Optional of the boolean result.
	 *
	 * @param bundle            The bundle that owns the package
	 * @param packageName       The package name
	 * @return                  An Optional of a boolean so that, if the combination is invalid {@link Optional#empty()}
	 *                          will be returned, otherwise an {@link Optional} containing true or false will result.
	 */
	public Optional<Boolean> isIncludedCombination(final Bundle bundle, final String packageName) {
		Map<Bundle, Boolean> entry = packageMap.get(packageName);
		if (entry == null) {
			return Optional.empty();
		}
		Boolean included = entry.get(bundle);
		return Optional.of(included);
	}

	/**
	 * Use this function to check if the GDA Class Loader should be used.
	 *
	 * @return Returns indication of whether initialize method has been called.
	 */
	public static boolean useGDAClassLoader() {
		return staticInitialized;
	}

	/**
	 * Retrieves the relative paths of the folders available to Jython in all beamlines as sources of Python scripts
	 *
	 * @return A set of paths relative to [WORKSPACE_NAME]_git as Strings.
	 */
	public Map<String, String> getStandardFolders() {
		return standardScriptFolders;
	}

	/**
	 * Set up the packages, their visibility and the script folders so that the class is ready to be instantiated, then
	 * set the status to indicate this.
	 *
 	 * @param contextBundles        Array of all the OSGi bundles available in the running context
	 * @param stdScriptFolders      A Set of paths relative to [WORKSPACE_NAME]_git as Strings where Python source files may be found.
	 * @param pkgMap                A map of package name to a map of Bundle to whether it is part of the standard Jython API
	 */
	public static void initialize(final Bundle[] contextBundles, final Map<String, String> stdScriptFolders, final Map<String, Map<Bundle, Boolean>> pkgMap) {
		initializedBundles= new HashSet<>(Arrays.asList(contextBundles));
		standardScriptFolders = stdScriptFolders;
		packageMap = pkgMap;
		staticInitialized = true;
	}

	/**
	 * Construct a new GDA Classloader with the access restrictions to the packages as initialized
	 */
	public GDAJythonClassLoader() {
		super();
		if (!useGDAClassLoader())
			throw new RuntimeException("The GDAJythonClassLoader was not initialized before construction");
	}

	/**
	 * Load the requested class by deriving the appropriate bundle and using its Classloader. If the
	 * class cannot be found, delegate up the hierarchy.
	 *
	 * @param name	The fully qualified name of the class to be loaded.
	 */
	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		if (StringUtils.isBlank(name))
			throw new ClassNotFoundException(name);

		Class<?> class1 = findLoadedClass(name);		// check we don't already have it
		if (class1 == null) {
			for (Bundle bundle : getMatchingBundlesForName(name).keySet()) {
				if (SKIPPED_BUNDLES.contains(bundle.getSymbolicName())) {
					continue;
				}
				try {
					return bundle.loadClass(name);
				} catch (ClassNotFoundException er) {
					continue;
				}
			}
			// If the requested class is not visible to the GDA Jython Class Loader, delegate up
			class1 = super.loadClass(name);
		}
		return class1;
	}

	/**
	 * Builds a set of bundles that match the supplied possible Java class name. Because of the way Jython resolves its
	 * import directives between Java and Python source code, it is perfectly possible that
	 * <code>potentialJavaClassName</code> will in fact be Python source module name which therefore cannot be loaded
	 * via the Java classloader. In this case it may also contain the dot Java package delimiter (e.g. __gda__.console)
	 * however, it will not be found in {@link #packageMap} and so the returned matchingBundles will be empty.<br>
	 * <br>
	 * If <code>potentialJavaClassName</code> corresponds to a real Java class, it will be in its fully qualified form
	 * and so the last dot in the string will separate the package name from the class name. Thus the package name can
	 * be extracted and matched against {@link #packageMap}.
	 *
	 * @param potentialJavaClassName    Could be a fully qualified Java Class name or the name of a Python module.
	 * @return                          A Map of Bundle to whether the package in potentialJavaClassName is marked as
	 *                                  included in the Jython API for the Bundle. Will be empty if no match can be found
	 */
	private Map<Bundle, Boolean> getMatchingBundlesForName(final String potentialJavaClassName) {
		Map<Bundle, Boolean> matchingBundles = new HashMap<>();
		final int packageBoundary = potentialJavaClassName.lastIndexOf('.');
		if (packageBoundary > 0) {
			final String packageName = potentialJavaClassName.substring(0, packageBoundary);
			if (StringUtils.isNotBlank(packageName) && packageMap.containsKey(packageName)) {
				matchingBundles = packageMap.get(packageName);
			}
		}
		return matchingBundles;
	}
}
