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

import java.io.IOException;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.osgi.framework.Bundle;
import org.python.core.PyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a custom class loader for Jython to use within GDA. It doesn't actually do anything
 * special to load classes but rather delegates to the provided parent class loader and logs
 * classes which are successfully loaded. Other methods are utilities e.g. provide list of Java source locations
 * for Jython to resolve which classes can be imported.
 */
public class GDAJythonClassLoader extends ClassLoader {
	private static final Logger logger = LoggerFactory.getLogger(GDAJythonClassLoader.class);
	private static boolean staticInitialized = false;
	private static Map<String, String> standardScriptFolders;
	private static Set<Bundle> initializedBundles;
	private static Map<String, Map<Bundle, Boolean>> packageMap = new HashMap<>();
	private static Map<String, URLClassLoader> jarClassLoaders = new HashMap<>();

	@SuppressWarnings("unused") // setSysPath called in the tests
	private PyList sysPath;


	/** Custom logger for loaded classes only, used with a specific appender */
	private static final Logger classLoadLogger = LoggerFactory.getLogger("jython-class-loader");

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
	 * Construct this by giving it a parent loader and delegate all the actual loading
	 * This class then provides a hook to log the classes that are loaded by Jython.
	 */
	public GDAJythonClassLoader(ClassLoader parent) {
		super(parent);
		if (!useGDAClassLoader())
			throw new RuntimeException("The GDAJythonClassLoader was not initialized before construction");
	}

	/**
	 *
	 * Log the class name when classes are successfully loaded. This could be extended to catch and re-throw the
	 * exception which would allow, for example, logging of wildcard imports as they appear here too. It would also be
	 * possible to refuse access to Java classes based on the FQCN.
	 *
	 * @param name the name of the class to be loaded.
	 */
	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		Class<?> loadedClass = super.loadClass(name);
		classLoadLogger.trace(name);
		return loadedClass;
	}

	/**
	 * Allows the classLoader to be informed of the Jython sys.path so it can be checked for dynamically added jar files
	 *
	 * @param path A Jython sys.path list of entries
	 */
	public void setSysPath(PyList path) {
		sysPath = path;
	}

	/**
	 * Ensures that any JAR classloaders initialised at runtime can be released at shutdown
	 */
	public static void closeJarClassLoaders() {
		for (Map.Entry< String, URLClassLoader> entry : jarClassLoaders.entrySet()) {
			try {
				entry.getValue().close();
			} catch (IOException e) {
				logger.warn("Unable to close URLClassLoader for Jython sys path {}, potential resource leak.", entry.getKey(), e);
			}
			jarClassLoaders.remove(entry.getKey());
		}
	}
}
