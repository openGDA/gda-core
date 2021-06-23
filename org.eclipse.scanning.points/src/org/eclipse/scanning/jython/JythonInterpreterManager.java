package org.eclipse.scanning.jython;

import static java.util.Arrays.stream;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Properties;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.scanning.points.ScanPointGeneratorFactory;
import org.osgi.framework.Bundle;
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
public final class JythonInterpreterManager {

	private static final Logger logger = LoggerFactory.getLogger(JythonInterpreterManager.class);

	/** Standard name of script dirs in roots of projects */
	private static final String SCRIPTS = "scripts";

	private static PySystemState configuredState;

	private JythonInterpreterManager() {}

	/**
	 * Call to ensure that an interpreter is set up and configured and able to load the relevant bundles.
	 */
	public static synchronized void setupSystemState(String... bundleNames) {
		boolean jythonInitialsedInThisProcess = (Py.defaultSystemState != null);

		// If alreadyInitialised before configuredState set then Jython is already
		// in use for this process (e.g. GDA Server) so we don't want to modify the state too much:
		// only add the required scripts to the path
		// (Py.defaultSystemState is set after PySystemState is initialised)
		PySystemState state;

		if (jythonInitialsedInThisProcess) {
			// Calling getSystemState static initialises PySystemState the first time
			// So we can't call this outside of this block as initializePythonPath must be called first
			state = Py.getSystemState();
			if ((configuredState != null) && (configuredState == state)) {
				return;
			}
			// A composite class loader is set just in case the existing loader is not
			// capable of loading the Java classes from this plugin
			ClassLoader loader = createJythonClassLoader(state.getClassLoader());
			state.setClassLoader(loader);
		} else {
			// Create class loader using the Jython bundle loader as base
			ClassLoader loader = createJythonClassLoader(PySystemState.class.getClassLoader());
			initializePythonPath(loader);
			state = Py.getSystemState();
			state.setClassLoader(loader);
			// Ensure that enough of jython is on the path
			setJythonPaths(state);
			// Restricted permissions cause issues in shared (writable) deployments (CVE-2013-2027 Jython 2.7.2)
			state.dont_write_bytecode = true;
		}

		// Adds the scripts directory from points
		setSpgGeneratorPaths(state, bundleNames);

		configuredState = state;
	}

	public static synchronized void addPath(String directory) throws IOException {
		// Load one of the standard functions if the state has not been created yet.
		if (configuredState == null) {
			ScanPointGeneratorFactory.JOneAxisLineGeneratorFactory();
		}
		// Do nothing if the directory is on the path
		if (configuredState.path.contains(directory)) {
			return;
		}
		addPathToPySystemState(configuredState, Paths.get(directory));
	}

	private static void setSpgGeneratorPaths(PySystemState state, String... bundleNames) {
		logger.info("Adding default scanning script sources to Jython path");

		addBundleScriptsToPythonPath(state, "org.eclipse.scanning.points");
		addBundleScriptsToPythonPath(state, "org.eclipse.scanning.sequencer");

		if (bundleNames != null) {
			Arrays.stream(bundleNames).forEach(bundle -> addBundleScriptsToPythonPath(state, bundle));
		}
	}

	private static void addBundleScriptsToPythonPath(PySystemState state, String bundleName) {
		try {
			Path location = getBundleLocation(bundleName).toAbsolutePath().resolve(SCRIPTS);
			addPathToPySystemState(state, location);
		} catch (IOException e) {
			logger.warn("Could find bundle script directory", e);
		}
	}

	private static void addPathToPySystemState(PySystemState state, Path path) throws IOException {
		if (Files.exists(path) && Files.isDirectory(path)) {
			logger.debug("Adding script source: {}", path);
			state.path.add(new PyString(path.toString()));
		} else {
			throw new IOException("Script directory doesn't exist: " + path);
		}
	}

	private static void initializePythonPath(ClassLoader loader) {
		logger.info("Initialising Jython");

		Properties props = new Properties();
		String jythonBundleName = getJythonBundleName();
		try {
			Path loc = getBundleLocation(jythonBundleName);
			props.setProperty("python.home", loc.toAbsolutePath().toString());
		} catch (IOException e) {
			// This will happen for non OSGi execution but in this case Jython
			// doesn't require python.home to be set
			logger.info("Jython bundle not found while initializing PythonPath");
		}

		// Used to prevent: console: Failed to install '':java.nio.charset.UnsupportedCharsetException:cp0.
		props.setProperty("python.console.encoding", "UTF-8");
		props.setProperty("python.options.showJavaExceptions", "true");
		props.setProperty("python.verbose", "warning");

		Properties preprops = System.getProperties();

		// When initialising in the client ensure that Jython cache is set to location
		// within the instance location (this is specifed by WORKSPACE in gdaclient script)
		if (!preprops.containsKey("python.cachedir") && Platform.isRunning()) {
			// This is location passed in via -data option
			try {
				String instanceLoc = Paths.get(Platform.getInstanceLocation().getURL().toURI()).toString();
				props.setProperty("python.cachedir", Paths.get(instanceLoc, "jythonCache").toString());
			} catch (URISyntaxException e) {
				logger.error("Could not resolve platform data location");
			}
		}
		PySystemState.initialize(preprops, props, new String[] {}, loader);
	}

	/**
	 * Create a {@link CompositeClassLoader} for Jython to use for the scanning framework.
	 * This is created from a base loader and augmented with the class loader for
	 * this {@code org.eclipse.scanning.points}.
	 * @param baseLoader
	 */
	private static ClassLoader createJythonClassLoader(ClassLoader baseLoader) {
		ClassLoader pointsLoader = ScanPointGeneratorFactory.class.getClassLoader();
		if (baseLoader == null) {
			return new CompositeClassLoader(pointsLoader);
		} else {
			CompositeClassLoader composite = new CompositeClassLoader(baseLoader);
			composite.addLast(pointsLoader);
			return composite;
		}
	}

	private static String getJythonBundleName() {
		return System.getProperty("org.eclipse.scanning.jython.osgi.bundle.name", "uk.ac.diamond.jython");
	}

	private static void setJythonPaths(PySystemState state) {
		// Check that Jython's Lib is on its path
		// This will have been set via PySystemState.initialize
		Path lib = stream(state.path.toArray()).filter(String.class::isInstance)
				.map(String.class::cast).filter(str -> str.endsWith(File.separator + "Lib"))
				.map(Paths::get).filter(Path::isAbsolute).findFirst()
				.orElseThrow(() -> new IllegalStateException("Jython does not contain Lib on path"));

		Path site = lib.resolve("site-packages");
		if (!Files.exists(site)) {
			throw new IllegalStateException("site-packages could not be found within Jython");
		}

		// Add site-packages to path
		state.path.add(new PyString(site.toAbsolutePath().toString()));
		// Add subdirs of site-packages to path
		try (var list = Files.list(site)) {
			list.filter(Files::isDirectory).filter(dir -> !dir.getFileName().toString().endsWith("-info"))
					.forEach(d -> state.path.add(new PyString(d.toAbsolutePath().toString())));
		} catch (IOException e) {
			logger.warn("Error reading site-packages directory");
		}
	}

	/**
	 * Return a path to a bundle's directory
	 * Looks in OSGi platform if running otherwise makes best guess to construct
	 * relative to current working directory (non OSGi unit tests)
	 * @param bundleName
	 * @return Path
	 * @throws IOException if directory cannot be found
	 */
	private static Path getBundleLocation(final String bundleName) throws IOException {
		if (Platform.isRunning()) {
			final Bundle bundle = Platform.getBundle(bundleName);
			if (bundle != null) {
				Path bundlePath = FileLocator.getBundleFile(bundle).toPath();
				if (Files.isDirectory(bundlePath)) {
					return bundlePath;
				}
			}
		} else {
			Path dir = Paths.get("..", bundleName);
			if (Files.exists(dir) && Files.isDirectory(dir)) {
				return dir;
			}
			dir = Paths.get("..", "..", "scanning.git", bundleName);
			if (Files.exists(dir) && Files.isDirectory(dir)) {
				return dir;
			}
			// These paths refer to finding things in non OSGi tests
			dir = Paths.get("..", "..", "org.eclipse.scanning", bundleName);
			if (Files.exists(dir) && Files.isDirectory(dir)) {
				return dir;
			}
		}
		throw new IOException("Bundle directory for " + bundleName + " not found");
	}
}
