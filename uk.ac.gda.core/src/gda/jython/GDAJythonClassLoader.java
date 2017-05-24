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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.osgi.framework.Bundle;
import org.python.core.PyList;
import org.python.core.PyObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Classloader for use with Jython in the GDA environment. It retrieves the class from the correct bundle within the
 * product's OSGi context based on a map initialised at startup from the BundleContext or delegates to its parent.
 */
public class GDAJythonClassLoader extends ClassLoader {
	private static final Logger logger = LoggerFactory.getLogger(GDAJythonClassLoader.class);
	private static final String JAR_EXTENSION = ".jar";
	private static boolean staticInitialized = false;
	private static Map<String, String> standardScriptFolders;
	private static Set<Bundle> initializedBundles;
	private static Map<String, Map<Bundle, Boolean>> packageMap = new HashMap<>();
	private static Map<String, URLClassLoader> jarClassLoaders = new HashMap<>();

	private PyList sysPath;

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
	 * Load the requested class by deriving the appropriate bundle(s) and trying their Classloaders. If the
	 * class cannot be found this way, attempt to get it from Jars on the Jython sys.path. If it still
	 * can't be found, delegate up the hierarchy.
	 *
	 * @param name	The name of the class to be loaded. The Jython infrastructure chunks through the fully
	 * 				qualified class name of the the require class, successively calling this method. For
	 * 				example for the class com.a.b.klass it might call first with com.a, then com.a.b.klass.
	 */
	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		if (StringUtils.isBlank(name))
			throw new ClassNotFoundException(name);

		Class<?> theClass = findLoadedClass(name);         // check we don't already have it
		if (theClass == null) {
			for (Bundle bundle : getMatchingBundlesForName(name).keySet()) {
				try {
					theClass = bundle.loadClass(name);
					logger.debug("Loaded class {} from bundle {}", name, bundle);
					return theClass;
				} catch (ClassNotFoundException er) {
					continue;                              // try the next bundle
				}
			}
			try {
				if (sysPath != null && sysPath.toString().contains(JAR_EXTENSION)) {   // if there are jar(s) on our sys.path
					return useSysPathJarClassLoading(name);
				}
			} catch (ClassNotFoundException er) {
				// If the requested class is not visible to the GDA Jython Class Loader or Jar Class loading, delegate up
			}
			//This is the last resort and will almost certainly fail
			theClass = super.loadClass(name);
		}

		return theClass;
	}

	/**
	 * Attempt to load the specified class from any Jars specified on the Jython sys.path using
	 * a URL ClassLoader created for the each jar path.
	 *
	 * @param name	The name of the class to be loaded. The Jython infrastructure chunks through the fully
	 * 				qualified class name of the the require class, successively calling	this method. For
	 * 				example for the class com.a.b.klass it might call first with com.a, then com.a.b.klass.
	 *
	 * @return		The loaded class if successful
	 * @throws 		ClassNotFoundException if none of the identified Jars can load the class or a valid URL
	 * 				could not be formed from one of the jar paths.
	 */
	private Class<?> useSysPathJarClassLoading(final String name) throws ClassNotFoundException {
		for (PyObject pathObj : sysPath.getArray()) {
			final String path = pathObj.toString().trim();
			if (path.endsWith(JAR_EXTENSION)) {
				if (!jarClassLoaders.containsKey(path)) {             // Check we haven't made a loader for this jar already
					try {
						final URL[] urls = {(new URL("file://" + path))};
						jarClassLoaders.put(path, new URLClassLoader(urls));
					} catch (MalformedURLException e) {
						logger.warn("Unable to resolve jar file path URL for {}", path, e);
						continue;
					}
				}
				try {
					Class<?> theClass = jarClassLoaders.get(path).loadClass(name);
					logger.debug("Loaded class {} from {}", name, path);
					return theClass;
				} catch (ClassNotFoundException cnfe) {
					continue;                                         // Try the next sys.path entry
				}
			}
		}
		throw new ClassNotFoundException();
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
