/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
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

import static org.osgi.framework.Constants.EXPORT_PACKAGE;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.osgi.util.ManifestElement;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;

import com.google.common.collect.ImmutableMap;

/**
 * A Classloader for use with Jython in the GDA environment. It retrieves the class from the correct bundle within the
 * product based on a map initialised at startup from the BundleContext or delegates to its parent..
 */
public class GDAJythonClassLoader extends ClassLoader {

	private static boolean STATIC_INITIALISED = false;
	private static Set<String> STANDARD_SCRIPT_FOLDERS;
	private static Set<Bundle> ALL_BUNDLES;
	private static final Map<String, Map<Bundle, Boolean>> PACKAGE_MAP = new HashMap<>();

	// These bundles cause problems/noise when you attempt to load classes: the eclipse ones produce large stack traces if
	// the class is not found (i.e. each time a python source module name is supplied) rather than simply throwing the
	// exception. The scanning command package actually leads to a Jython conflict since it has an embedded version 2.7
	// of Jython whereas the rest of the product uses version 2.5. Once we have migrated the target platform to version
	// 2.7 it can be removed from the list.
	private static String SKIPPED_BUNDLES = "org.eclipse.debug.ui, org.eclipse.help.ui, org.eclipse.scanning.command";

	/**
	 * Use the return value of this function to get all the packages available to Jython. Call PySystemState.add_package
	 * on each one.
	 *
	 * @return Returns the packages (including those marked as not part of the Jython API).
	 */
	public Set<String> getJythonPackages() {
		return PACKAGE_MAP.keySet();
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
		Map<Bundle, Boolean> entry = PACKAGE_MAP.get(packageName);
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
		return STATIC_INITIALISED;
	}

	/**
	 * Retrieves the relative paths of the folders available to Jython in all beamlines as sources of Python scripts
	 *
	 * @return A set of paths relative to [WORKSPACE_NAME]_git as Strings.
	 */
	public Set<String> getStandardFolders() {
		return STANDARD_SCRIPT_FOLDERS;
	}

	/**
	 * Set up the packages, their visibility and the script folders so that the class is ready to be instantiated, then
	 * set the status to indicate this.
	 *
	 * @param contextBundles            Array of all the OSGi bundles available in the running context
	 * @param stdScriptFolders          A Set of paths relative to [WORKSPACE_NAME]_git as Strings where Python source files may be found.
	 * @param includedPackagesNames     A Set of bundle and package name combinations indicating those that should be marked as part
	 *                                  of the standard Jython API.
	 * @throws BundleException          if the exported package header of a bundle cannot be parsed.
	 */
	public static void initialize(final Bundle[] contextBundles, final Set<String> stdScriptFolders, final Set<String> includedPackagesNames) throws BundleException {
		ALL_BUNDLES = new HashSet<>(Arrays.asList(contextBundles));
		STANDARD_SCRIPT_FOLDERS = stdScriptFolders;
		initializePackageMap(includedPackagesNames);
		STATIC_INITIALISED = true;
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

		Class<?> class1 = findLoadedClass(name);
		if (class1 == null) {
			final Map<Bundle, Boolean> matchingBundles = getMatchingBundlesForName(name);
			final Set<Bundle> searchBundles = matchingBundles != null ? matchingBundles.keySet() : ALL_BUNDLES;

			for (Bundle bundle : searchBundles) {
				final String bundleName = bundle.getSymbolicName();
				if (SKIPPED_BUNDLES.contains(bundleName)) {
					continue;
				}
				try {
					class1 = bundle.loadClass(name);
					return class1;
				} catch (ClassNotFoundException er) {
					continue;
				}
			}
		}
		// If the requested class is not visible to the GDA Jython Class Loader, delegate up
		return super.loadClass(name);
	}

	/**
	 * Builds a {@link Map} of package name to the set of bundles in the context that export the package which can be
	 * used as a lookup table when subsequently loading a class. Each entry in the bundle set is also associated with a
	 * {@link Boolean} indicating whether the combination of bundle and package has been marked as part of the Jython API.
	 *
	 * @param includedPackagesNames    A Set of bundle and  package name combinations indicating those that should be marked as part
	 *                                 of the standard Jython API.
	 *
	 * @throws BundleException         if the exported package header cannot be parsed.
	 */
	private static void initializePackageMap(final Set<String> includedPackagesNames) throws BundleException {
		for (Bundle bundle : ALL_BUNDLES) {
			final ManifestElement[] exportPackages = ManifestElement.parseHeader(EXPORT_PACKAGE, bundle.getHeaders().get(EXPORT_PACKAGE));
			if (exportPackages != null) {
				for (ManifestElement element : exportPackages) {
					final String pkgName = element.getValue();

					boolean included = includedPackagesNames.contains(bundle.getSymbolicName() + ":" + pkgName);
					if (PACKAGE_MAP.containsKey(pkgName)) {
						PACKAGE_MAP.get(pkgName).put(bundle, included);
					} else {
						PACKAGE_MAP.put(pkgName, new HashMap<>(ImmutableMap.of(bundle, included)));
					}
				}
			}
		}
	}

	/**
	 * Builds a set of bundles that match the supplied possible Java class name. Because of the way Jython resolves its
	 * import directives between Java and Python source code, it is perfectly possible that
	 * <code>potentialJavaClassName</code> will in fact be Python source module name which therefore cannot be loaded
	 * via the Java classloader. In this case it may also contain the dot Java package delimiter (e.g. __gda__.console)
	 * however, it will not be found in {@link #PACKAGE_MAP} and so the returned matchingBundles will be null. <br>
	 * <br>
	 * If <code>potentialJavaClassName</code> corresponds to a real Java class, it will be in its fully qualified form
	 * and so the last dot in the string will separate the package name from the class name. Thus the package name can
	 * be extracted and matched against {@link #PACKAGE_MAP}.
	 *
	 * @param potentialJavaClassName    Could be a fully qualified Java Class name or the name of a Python module.
	 * @return                          A Map of Bundle to whether the package in potentialJavaClassName is marked
	 *                                  as included in the Jython API for the Bundle.
	 */
	private Map<Bundle, Boolean> getMatchingBundlesForName(final String potentialJavaClassName) {
		Map<Bundle, Boolean> matchingBundles = null;
		final int packageBoundary = potentialJavaClassName.lastIndexOf('.');
		if (packageBoundary > 0) {
			final String packageName = potentialJavaClassName.substring(0, packageBoundary);
			if (StringUtils.isNotBlank(packageName) && PACKAGE_MAP.containsKey(packageName)) {
				matchingBundles = PACKAGE_MAP.get(packageName);
			}
		}
		return matchingBundles;
	}
}
