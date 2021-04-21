package org.eclipse.scanning.jython;

import static java.util.Arrays.stream;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Properties;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.scanning.points.ScanPointGeneratorFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.wiring.BundleWiring;
import org.python.core.Py;
import org.python.core.PyString;
import org.python.core.PySystemState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * An interpreter manager which is designed to create an interpreter which either is sympathic and working with the GDA
 * configuration or creates an empty and working interpreter.
 *
 * @author Matthew Gerring
 *
 */
public class JythonInterpreterManager {

	private static final Logger logger = LoggerFactory.getLogger(JythonInterpreterManager.class);

	private static volatile PySystemState configuredState;

	/**
	 * Call to ensure that an interpreter is set up and configured and able to load the relevant bundles.
	 */
	public static synchronized void setupSystemState(String... bundleNames) {

		ClassLoader loader = null;
		if (configuredState == null) { // Relies on setupSystemState() being called early in the server startup.
			loader = createJythonClassLoader(PySystemState.class.getClassLoader());
			initializePythonPath(loader);
		}

		PySystemState state = Py.getSystemState();
		if (state == configuredState)
			return;

		if (loader == null) {
			// Then someone else has changed the PySystemState
			// They will not have added our
			loader = createJythonClassLoader(state.getClassLoader()); // Don't clobber their working.
		}
		state.setClassLoader(loader);
		// Restricted permissions cause issues in shared (writable) deployments (CVE-2013-2027 Jython 2.7.2)
		state.dont_write_bytecode = true;

		setJythonPaths(state); // Tries to ensure that enough of jython is on the path
		setSpgGeneratorPaths(state, bundleNames); // Adds the scripts directory from points
		Py.setSystemState(state);

		configuredState = state;
	}

	public static synchronized void addPath(String directory) throws IOException {

		// Load one of the standard functions if the state has not been created yet.
		if (configuredState == null)
			ScanPointGeneratorFactory.JOneAxisLineGeneratorFactory();

		// Do nothing if the directory is on the path
		if (configuredState.path.contains(directory))
			return;

		// Check the directory
		final File file = new File(directory);
		if (!file.exists())
			throw new IOException("The module directory '" + directory + "' does not exist!");
		if (!file.isDirectory())
			throw new IOException("The module directory path '" + directory + "' is not a folder!");

		// Add it to the path.
		configuredState.path.add(directory);
	}

	private static final String SCRIPTS = "/scripts/";

	private static void setSpgGeneratorPaths(PySystemState state, String... bundleNames) {

		File location = getBundleLocation("org.eclipse.scanning.points");
		state.path.add(new PyString(location.getAbsolutePath() + SCRIPTS));

		location = getBundleLocation("org.eclipse.scanning.sequencer");
		state.path.add(new PyString(location.getAbsolutePath() + SCRIPTS));

		if (bundleNames != null) {
			for (String bundle : bundleNames) {
				location = getBundleLocation(bundle);
				state.path.add(new PyString(location.getAbsolutePath() + SCRIPTS));
			}
		}
	}

	private static void initializePythonPath(ClassLoader loader) {
		try {
			String jythonBundleName = System.getProperty("org.eclipse.scanning.jython.osgi.bundle.name",
					"uk.ac.diamond.jython");
			File loc = getBundleLocation(jythonBundleName); // TODO Name the jython OSGi bundle without Diamond in it!
			if (loc == null) {
				logger.warn("JythonDir not found while initializing PythonPath");
				return;
			}

			Properties props = new Properties();
			props.setProperty("python.home", loc.getAbsolutePath());
			props.setProperty("python.console.encoding", "UTF-8"); // Used to prevent: console: Failed to install '':
																	// java.nio.charset.UnsupportedCharsetException:
																	// cp0.
			props.setProperty("python.options.showJavaExceptions", "true");
			props.setProperty("python.verbose", "warning");

			Properties preprops = System.getProperties();

			// When initialising in the client ensure that Jython cache is set to location
			// within the instance location (this is specifed by WORKSPACE in gdaclient script)
			if (!preprops.containsKey("python.cachedir") && Platform.isRunning()) {
				// This is location passed in via -data option
				String instanceLoc = Paths.get(Platform.getInstanceLocation().getURL().toURI()).toString();
				props.setProperty("python.cachedir", Paths.get(instanceLoc, "jythonCache").toString());
			}

			PySystemState.initialize(preprops, props, new String[] {}, loader);

		} catch (Throwable ne) {
			logger.debug("Problem loading jython bundles!", ne);
		}
	}

	private static ClassLoader createJythonClassLoader(ClassLoader classLoader) {

		ClassLoader jythonClassloader = ScanPointGeneratorFactory.class.getClassLoader();

		try { // For non-unit tests, attempt to use the OSGi classloader of this bundle.
			String jythonBundleName = getJythonBundleName();
			CompositeClassLoader composite = new CompositeClassLoader(classLoader);
			// Classloader for org.eclipse.scanning.points
			composite.addLast(ScanPointGeneratorFactory.class.getClassLoader());
			addLast(composite, jythonBundleName);
			jythonClassloader = composite;

		} catch (Throwable ne) {
			ne.printStackTrace();
			// Legal, if static classloader does not work in tests, there will be
			// errors. If bundle classloader does not work in product, there will be errors.
			// Typically the message is something like: 'cannot find module org.eclipse.scanning.api'
		}
		return jythonClassloader;
	}

	private static String getJythonBundleName() {
		return System.getProperty("org.eclipse.scanning.jython.osgi.bundle.name", "uk.ac.diamond.jython");
	}

	private static void setJythonPaths(PySystemState state) {

		try {
			String jythonBundleName = getJythonBundleName();
			File loc = getBundleLocation(jythonBundleName); // TODO Name the jython OSGi bundle without Diamond in it!

			// Search for the Lib directory in existing PySystemState, only accept if absolute path
			File lib = stream(state.path.toArray()).filter(String.class::isInstance).map(String.class::cast)
					.filter(str -> str.endsWith(File.separator + "Lib"))
					.map(File::new).filter(File::isAbsolute).findFirst().orElse(null);

			if (lib == null) {
				logger.debug("Jython Lib not found on PySystemState path");
				lib = find(loc, "Lib");
				if (lib == null) {
					logger.warn("Jython Lib not found in jythonDir {}, necessary scripts will not be added to path",
							loc);
				} else {
					logger.info("Adding Jython Lib to PythonPath");
					state.path.add(new PyString(lib.getAbsolutePath()));
				}
			}

			if (lib != null) {
				File site = find(lib, "site-packages");
				if (site == null) {
					throw new IllegalStateException("site-packages could not be found within Jython");
				}
				state.path.add(new PyString(site.getAbsolutePath())); // Resolves the collections

				File[] fa = site.listFiles();
				for (File dir : fa) {
					if (!dir.isDirectory())
						continue;
					if (dir.getName().endsWith("-info"))
						continue;
					state.path.add(new PyString(dir.getAbsolutePath())); // Resolves the collections
				}

			}

		} catch (Exception ne) {
			logger.debug("Problem setting jython path to include scripts!", ne);
		}
	}

	private static ClassLoader getBundleLoader(String name) {
		Bundle bundle = Platform.getBundle(name);
		BundleWiring bundleWiring = bundle.adapt(BundleWiring.class);
		return bundleWiring.getClassLoader();
	}

	/**
	 * @param bundleName
	 * @return File
	 */
	private static File getBundleLocation(final String bundleName) {

		if (Platform.isRunning()) {
			final Bundle bundle = Platform.getBundle(bundleName);
			if (bundle != null) {
				try {
					return FileLocator.getBundleFile(bundle);
				} catch (IOException e) {
					logger.error("Error resolving bundle File", e);
				}
			}
		} else {
			File dir = new File("../" + bundleName);
			if (dir.exists())
				return dir;

			dir = new File("../../scanning.git/" + bundleName);
			if (dir.exists())
				return dir;

			// These paths refer to finding things in the travis build
			// They will not resolve from the IDE or binary.
			dir = new File("../../org.eclipse.scanning/" + bundleName);
			if (dir.exists())
				return dir;
		}
		return null;
	}

	private static File find(File loc, String name) {
		if (loc == null)
			return null;
		if (!loc.exists())
			return null;
		File find = new File(loc, name);
		if (find.exists())
			return find;

		for (File child : loc.listFiles()) {
			if (child.getName().startsWith(name)) {
				return child;
			}
		}
		return null;
	}

	private static void addLast(CompositeClassLoader composite, String bundleName) {
		try {
			ClassLoader cl = getBundleLoader(bundleName);
			composite.addLast(cl);
		} catch (NullPointerException ne) {
			// Allowed, the bundles do not have to be there we are just trying to be helpful
			// in loading classes without making hard dependencies on them.

		}
	}

}
