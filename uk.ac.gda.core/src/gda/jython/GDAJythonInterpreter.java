/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

import static uk.ac.gda.common.rcp.util.EclipseUtils.PLATFORM_BUNDLE_PREFIX;
import static uk.ac.gda.common.rcp.util.EclipseUtils.URI_SEPARATOR;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Stream;

import org.eclipse.core.runtime.FileLocator;
import org.python.core.Py;
import org.python.core.PyException;
import org.python.core.PyModule;
import org.python.core.PyNone;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.core.PySystemState;
import org.python.core.PyUnicode;
import org.python.core.imp;
import org.python.util.InteractiveConsole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import gda.configuration.properties.LocalProperties;
import gda.factory.FactoryException;
import gda.factory.Findable;
import gda.factory.Finder;
import gda.jython.translator.Translator;
import gda.observable.ObservableComponent;
import gda.util.exceptionUtils;
import uk.ac.gda.common.rcp.util.EclipseUtils;

/**
 * <p>
 * Wrapper for the JythonInterpreter class.
 * <P>
 * IMPORTANT: for the classes in this package to work properly jython must be fully installed on the machine and the
 * jython.jar located in the jython installation folder must be referenced in the java classpath. Jython.jar must NOT be
 * located anywhere else. This is because there is more to jython than just the files in jython.jar(!).****
 */
public class GDAJythonInterpreter extends ObservableComponent {
	private static final Logger logger = LoggerFactory.getLogger(GDAJythonInterpreter.class);
	private static final String JYTHON_VERSION = "2.7";
	private static final String JYTHON_BUNDLE_PATH = "uk.ac.diamond.jython/jython%s";
	private static final String UTF_8 = "UTF-8";
	private static final Properties sysProps;

	private static File cacheDir;

	// the Jython interpreter
	private InteractiveConsole interp;

	// to avoid running the initialise method more than once
	private boolean configured = false;

	// the translator object used to convert GDA syntax into 'true' jython
	private static Translator translator = null;

	// folders where beamline and user scripts are held
	private ScriptPaths _jythonScriptPaths;
	private String _gdaVarDir;

	private final GDAJythonClassLoader classLoader = new GDAJythonClassLoader();

	/**
	 * Static initializer bock to set all the static parameters on the PySystemState class
	 * to be used in all instantiations, i.e. those that persist through reset_namespace
	 */
	static {
		// If not already specified, work out the Jython cache directory and
		// create if required
		String cacheDirName = LocalProperties.getVarDir() + "jythonCache";
		cacheDir = new File(cacheDirName);
		if (!cacheDir.exists()) {
			cacheDir.mkdir();
		}

		// custom properties for the GDA
		logger.info("determining Jython properties...");
		final Properties gdaCustomProperties = new Properties();
		gdaCustomProperties.setProperty("python.console.encoding", UTF_8);
		gdaCustomProperties.setProperty("python.cachedir", cacheDir.getAbsolutePath());

		try {
			final File jythonRoot = EclipseUtils.resolveBundleFolderFile(String.format(JYTHON_BUNDLE_PATH, JYTHON_VERSION));
			gdaCustomProperties.setProperty("python.home", jythonRoot.getAbsolutePath());
		} catch (IOException e) {
			throw new RuntimeException("Jython bundle not found", e);
		}

		if (LocalProperties.check("python.options.showJavaExceptions", false)) {
			gdaCustomProperties.setProperty("python.options.showJavaExceptions", "true");
		}

		gdaCustomProperties.setProperty("python.options.includeJavaStackInExceptions", "false");

		if (LocalProperties.check("python.options.showPythonProxyExceptions", false)) {
			gdaCustomProperties.setProperty("python.options.showPythonProxyExceptions", "true");
		}
		String verbose = LocalProperties.get("python.verbose", "");
		if (!verbose.isEmpty()) {
			gdaCustomProperties.setProperty("python.verbose", verbose);
		}

		sysProps = System.getProperties();

		// Initialise the Jython 'sys' class statics for use when constructing instances of it
		PySystemState.initialize(sysProps, gdaCustomProperties);
	}

	public ScriptPaths getJythonScriptPaths() {
		return _jythonScriptPaths;
	}

	public void setJythonScriptPaths(ScriptPaths scriptPaths) {
		_jythonScriptPaths = scriptPaths;
	}

	/**
	 * @return string - the full path of the beamline's initialisation script.
	 */
	public String getGdaStationScript() {
		return _jythonScriptPaths != null ? _jythonScriptPaths.getStartupScript() : "";
	}

	/**
	 * @return string - the full path of the gda var directory
	 */
	public String getGdaVarDir() {
		if (_gdaVarDir == null) {
			_gdaVarDir = LocalProperties.getVarDir();
		}
		return _gdaVarDir;
	}

	/**
	 * Sets the 'var' directory used by this interpreter.
	 *
	 * @param gdaVarDirectory
	 *            the 'var' directory
	 */
	public void setGdaVarDirectory(File gdaVarDirectory) {
		if (gdaVarDirectory != null) {
			_gdaVarDir = appendSeparator(gdaVarDirectory.getAbsolutePath());
		}
	}

	/**
	 * Sets the cache directory used by this interpreter.
	 *
	 * @param cacheDirectory
	 *            the cache directory
	 */
	public void setCacheDirectory(File cacheDirectory) {
		cacheDir = cacheDirectory;
	}

	private static String appendSeparator(String file) {
		if (!file.endsWith(System.getProperty("file.separator"))) {
			return file + System.getProperty("file.separator");
		}
		return file;
	}

	/**
	 * Configures this interpreter.
	 */
	public void configure() {
		// Check the OSGi aware Jython Classloader is properly initialized before proceeding
		if (!GDAJythonClassLoader.useGDAClassLoader()) {
			logger.error("GDAJythonClassLoader not initialized properly GDA-Server cannot Start");
			throw new UnsupportedOperationException();
		}

		logger.info("adding GDA package locations to Jython path...");

		// Create a new Jython 'sys' instance to be used by the Py infrastructure based on the settings
		// supplied to PySystemState.initialize in the initializer block above.
		// Enclose in a try/catch so that any underlying Jython errors are not lost to the log.
		try {
			PySystemState pss = new PySystemState();
			Py.setSystemState(pss);
			pss.setClassLoader(classLoader);
			pss.setdefaultencoding(UTF_8);		// cannot be done before Py.setSystemState
		} catch (Exception e) {
			if (e instanceof PyException) {
				logger.error("Jython initialisation problem: " + e);     // Since PyException puts message in value member
			} else {
				logger.error("Jython initialisation problem: ", e);
			}
			throw e;
		}

		// In an OSGi environment Jython's normal CLASSPATH based automatic way of
		// discovering which packages are available in the JVM does not
		// work. Therefore to support "from XXXX import *" Jython has to be
		// told about the bundle locations.
		final boolean eclipseLaunch = Boolean.valueOf(sysProps.getProperty("gda.eclipse.launch"));
		final String bundlesRoot;
		if (eclipseLaunch) {
			bundlesRoot = LocalProperties.get(LocalProperties.GDA_GIT_LOC);
			iterateWorkspace(classLoader);
		} else {
			bundlesRoot = sysProps.getProperty("osgi.syspath");
			iteratePluginsDirectory(bundlesRoot);
		}
		// Add the paths for the standard script folders to the existing _jythonScriptPaths
		// (the instance config scripts folders are handled by Spring injection into the JythonServer bean)
		int index= 1;
		for (Entry<String, String> scriptEntry : classLoader.getStandardFolders().entrySet()) {
			String pathFragment = scriptEntry.getKey();
			if (pathFragment.endsWith(URI_SEPARATOR)) {
				pathFragment = pathFragment.substring(0, pathFragment.length() -1);
			}
			File scriptFolder = Paths.get(bundlesRoot,  pathFragment).toFile();                       // Default to non-plugin folder under workspace_git
			String frag = pathFragment;
			URL scriptFolderURL = null;
			try {
				while (scriptFolderURL == null && frag.contains(URI_SEPARATOR)) {
					scriptFolderURL = FileLocator.find(new URL(String.format(PLATFORM_BUNDLE_PREFIX, frag)));
					frag = frag.substring(frag.indexOf(URI_SEPARATOR) + 1);
				}
				if (scriptFolderURL != null) {
					scriptFolder = EclipseUtils.resolveFileFromPlatformURL(scriptFolderURL);
				} else if (!eclipseLaunch) {
					scriptFolder = Paths.get(bundlesRoot, "..", "utilities", pathFragment).toFile();  // Add in non-plugin folder offset for exported product
				}
				if (scriptFolder.exists() && scriptFolder.isDirectory()) {
					String title = scriptEntry.getValue() == null ? "Scripts: Std" + index++ : scriptEntry.getValue();
					_jythonScriptPaths.addProject(new ScriptProject(scriptFolder.getAbsolutePath(), title, ScriptProjectType.CORE));
				} else {
					throw new IOException(String.format("Script folder %s does not exist", scriptFolder));
				}
			} catch (IOException e) {
				logger.error(String.format("Unable to locate plugin for script location %s, these scripts will not be accessible", pathFragment), e);
			}
		}

		// then get instance of interpreter
		this.interp = new GDAInteractiveConsole();

		// force it to be the main module with proper name
		PyModule mod = imp.addModule("__main__");
		this.interp.setLocals(mod.__dict__);
	}

	/**
	 * Retrieve the set of server plugin directories when running from the eclipse workspace and register
	 * them with Jython as sources for full package import.
	 *
	 * @param classLoader	The class loader being used by the interpreter to allow non-server bundle filtering
	 */
	private void iterateWorkspace(final GDAJythonClassLoader classLoader) {
		logger.info("Retrieving eclipse workspace server plugin paths for Jython");

		final String unwanted = "^.*?(.feature|.releng|.test|.site|.git).*$";
		final File workspaceGit = new File(System.getProperties().getProperty("gda.install.git.loc"));

		final File[]repoDirectories = workspaceGit.listFiles(f -> f.isDirectory() && f.getName().endsWith(".git"));
		for (File repoDir : repoDirectories) {
			// cope with 'group' repos that have their plugins in a 'plugins' sub directory
			final Path pluginsDir = Paths.get(repoDir.getAbsolutePath(), "plugins");
			if (Files.isDirectory(pluginsDir)) {
				repoDir = pluginsDir.toFile();
			}
			// filter 'non-bundle' and test directories
			final File[]pluginDirectories = repoDir.listFiles(f -> f.isDirectory() && f.getName().contains(".") && !f.getName().matches(unwanted));

			// Store only the paths for directories corresponding to server plugins from the workspace via isMappedBundle
			Arrays.stream(pluginDirectories).filter(dir -> classLoader.isMappedBundle(dir.getName()))
                                            .map(dir-> Paths.get(dir.getAbsolutePath(), getCorrectClassFilesLocation(dir)))
                                            .forEach(classesPath -> PySystemState.add_classdir(classesPath.toString()));
		}
	}

	/**
	 * Deal with the gda.core bin directory anomaly until we can get rid of the gda python script and make it consistent
	 *
	 * @param dir		The File object corresponding to a workspace plugin dir
	 * @return			classes/main for gda.core, "bin" for everything else
	 */
	private String getCorrectClassFilesLocation(final File dir) {
		return dir.getName().equals("uk.ac.gda.core") ? "classes" + File.separatorChar + "main" : "bin";
	}

	/**
	 * Retrieve the set of plugin directories when running from the exported product and
	 * register them with Jython as sources for full package import filtering out TP plugins.
	 * If parsing the artifacts.xml file fails an error is logged but execution will proceed
	 * without further restricting the plugins that will allow import *.
	 *
	 * @param osgiSysPath	The path to the plugins directory in the exported product
	 */
	private void iteratePluginsDirectory(final String osgiSysPath) {
		logger.info("Retrieving server product plugin paths for Jython");

		// Read in the target platform plugin names so that they can be skipped
		final Set<String> unwanted = new HashSet<>();
		try (Stream<String> stream = Files.lines(Paths.get(osgiSysPath, "..", "configuration", "target_platform_artifacts.xml"))) {
			stream.forEach(line -> {
				if (line.contains(" classifier='osgi.bundle'")) {
					unwanted.add(getTPArtifactAttributeValue(line, "id") + "_" + getTPArtifactAttributeValue(line, "version"));
					}
				});
		} catch (IOException | UncheckedIOException e) {
			logger.error("Unable to successfully read target platform artifacts file, import * will not be fully restricted", e);
		}
		// Initialise the Jython registry skipping target platform bundles.
		// The 'from xx import *' syntax will be supported for the remaining bundles
		for(File file : new File(osgiSysPath).listFiles()) {
			String name = file .getName();
			if (name.endsWith(".jar")) {
				name = name.substring(0, name.indexOf(".jar"));
			}
			if (!unwanted.contains(name)) {
				if (file.isDirectory()) {
					PySystemState.add_classdir(file.getAbsolutePath());
				} else {
					PySystemState.packageManager.addJar(file.getAbsolutePath(), false);
				}
			}
		}
	}

	/**
	 * Extract the value of an attribute from one of the artifact tags read in from the target platform
	 * artifact.xml file.
	 *
	 * @param artifactTag		The artifact tag string
	 * @param attributeName		The name of the attribute whose value it to be retrieved
	 * @return					The value requested if found successfully
	 * @throws					UncheckedIOException if the attribute name or its terminating quote cannot be found
	 */
	private String getTPArtifactAttributeValue(final String artifactTag, final String attributeName) throws UncheckedIOException {
		final String match = " " + attributeName + "='";
		final int matchLength = match.length();
		final int from = artifactTag.indexOf(match) + matchLength;	// indexOf will return -1 if not found in which case from will be < matchLength
		final int to = artifactTag.indexOf("'", from);
		if (from < matchLength || to == -1) {
			throw new UncheckedIOException(new IOException("Cannot read " + attributeName + " in artifact tag of target_platform_artifacts.xml file"));
		}
		return artifactTag.substring(from, to);
	}

	private void fakeSysExecutable(PySystemState sys) {
		// fake an executable path to get around problem in pydoc.Helper.__init__
		File f;
		if (!(sys.executable instanceof PyNone)) {
			f = new File(((PyString) sys.executable).asString());
			if (f.exists()) {
				return;
			}
		}

		int n = sys.path.size() - 1;
		for (int i = 0; i < n; i++) {
			f = new File((String) sys.path.get(i));
			if (f.exists()) {
				sys.executable = new PyString(f.getPath());
				return;
			}
		}
		String home = System.getProperty("java.home");
		sys.executable = new PyString(home);
		logger.warn("Setting sys.executable to java.home: {}", home);
	}

	/**
	 * Set up the Jython interpreter and run Jython scripts to connect to the ObjectServer. This must be run once by the
	 * calling program after the interpreter instance has been created.
	 */
	protected void initialise(JythonServer jythonServer) throws Exception {
		if (!configured) {
			try {
				final String translatorClassName = LocalProperties.get("gda.jython.translator.class", "GeneralTranslator");
				final Class<?> translatorClass = Class.forName("gda.jython.translator." + translatorClassName);
				translator = (Translator) translatorClass.newInstance();

				// append the folders where standard scripts will be located to jython path
				// by this point _jythonScriptPaths should contain a List of these folder paths
				final PySystemState sys = Py.getSystemState();
				if (_jythonScriptPaths == null) {
					logger.warn("no jython script paths defined");
				} else {
					logger.info("clearing old Jython class files...");
					// Remove any previously compiled Jython class files from the script folders
					for (ScriptProject scriptProject : _jythonScriptPaths.getProjects()) {
						final PyString scriptFolderName = new PyString(scriptProject.getPath());
						final File scriptDir = new File(scriptFolderName.toString());
						if (!scriptDir.exists()){
							throw new FactoryException(String.format("Configured Jython script location %s does not exist.", scriptFolderName));
						}
						logger.info(String.format("Adding %s to the Command Server Jython path with name %s", scriptDir, scriptProject.getName()));

						if (!sys.path.contains(scriptFolderName)) {
							removeAllJythonClassFiles(new File(scriptFolderName.getString()));
							sys.path.append(scriptFolderName);
						}
					}
				}
				fakeSysExecutable(sys);

				// set the console output
				final Writer terminalWriter = jythonServer.getTerminalWriter();
				interp.setOut(terminalWriter);
				interp.setErr(terminalWriter);

				// dynamic configuration using Castor
				logger.info("performing standard Jython interpreter imports...");

				this.interp.runsource("import sys");
				this.interp.runsource("import gda.jython");
				this.interp.runsource("sys.displayhook=gda.jython.GDAInteractiveConsole.displayhook");

				// give Jython the reference to this wrapper object
				this.interp.set("GDAJythonInterpreter", this);

				this.interp.set("command_server", jythonServer);
				this.interp.runsource("import gda.jython");

				// standard imports
				this.interp.runsource("import java");
				this.interp.runsource("from java.lang import Thread");
				this.interp.runsource("from java.lang import Runnable");
				this.interp.runsource("from java.lang import InterruptedException");

				// gda imports
				this.interp.runsource("from gda.scan import *");
				this.interp.runsource("from gda.device import *");
				this.interp.runsource("from gda.jython import JythonServer");
				this.interp.runsource("from gda.jython import ScriptBase");
				this.interp.runsource("from gda.device.monitor import BeamMonitor");

				this.interp.runsource("from gda.factory import Finder");
				this.interp.runsource("from gda.device.detector import DetectorBase");
				this.interp.runsource("from gda.device import Scannable");
				this.interp.runsource("from gda.device.scannable.scannablegroup import IScannableGroup");
				this.interp.runsource("finder = Finder.getInstance();");
				this.interp.runsource("from gda.device.scannable import ScannableBase");
				this.interp.runsource("from gda.device.scannable import DummyScannable");
				this.interp.runsource("from gda.device.scannable import ContinuouslyScannable");
				this.interp.runsource("from gda.device.scannable import SimulatedContinuouslyScannable");
				this.interp.runsource("from gda.device.scannable import PseudoDevice");
				this.interp.runsource("from gda.jython.commands import ScannableCommands");
				this.interp.runsource("from gda.jython.commands.ScannableCommands import *");
				this.interp.runsource("from gda.jython.commands import GeneralCommands");
				this.interp.runsource("from gda.jython.commands.GeneralCommands import *");
				this.interp.runsource("from gda.jython.commands import InputCommands");
				this.interp.runsource("from gda.jython.commands.InputCommands import *");

				// oe plugin commands
				try {
					Class.forName("gda.oe.OE");
					this.interp.runsource("from gda.device.scannable import OEAdapter");
					this.interp.runsource("from gda.device.scannable import DOFAdapter");
					this.interp.runsource("from gda.oe import OE");
					this.interp.runsource("from gda.oe.dofs import DOF");
					this.interp.runsource("from gda.oe import OE");
				} catch (Exception e1) {
					// ignore
				}

				// persistence
				this.interp.runsource("from gda.util.persistence import LocalParameters");
				this.interp.runsource("from gda.util.persistence import LocalObjectShelfManager");

				// import other interfaces to use with list command
				this.interp.runsource("from gda.device import ScannableMotion");
				this.interp.runsource("import gda.device.scannable.ScannableUtils");
				this.interp.runsource("from gda.util.converters import IReloadableQuantitiesConverter");

				// Create the completer object to allow command line tab completion
				this.interp.runsource("from gda_completer import Completer");
				this.interp.runsource("completer = Completer(globals())");

				// scisoftpy
				this.interp.runsource("import scisoftpy as dnp");
				// inform translator what the built-in commands are by
				// aliasing them -- i.e. reserved words
				this.exec("alias ls");
				this.exec("alias ls_names");
				this.exec("vararg_alias pos");
				this.exec("vararg_alias upos");
				this.exec("vararg_alias inc");
				this.exec("vararg_alias uinc");
				this.exec("alias help");
				this.exec("alias list_defaults");
				this.exec("vararg_alias add_default");
				this.exec("vararg_alias remove_default");
				this.exec("vararg_alias level");
				this.exec("alias pause");
				this.exec("alias reset_namespace");
				this.exec("alias run");
				this.exec("vararg_alias scan");
				this.exec("vararg_alias pscan");
				this.exec("vararg_alias cscan");
				this.exec("vararg_alias zacscan");
				this.exec("vararg_alias testscan");
				this.exec("vararg_alias gscan");
				this.exec("vararg_alias tscan");
				this.exec("vararg_alias timescan");
				this.exec("vararg_alias staticscan");
				this.exec("alias lastScanDataPoint");

				// define a function that can check a java object for a field or method called
				// __doc__ and print it out
				this.exec("def _gdahelp(obj=None):\n" + "    if obj is None:\n"
						+ "        GeneralCommands.gdahelp()\n" + "        return\n"
						+ "    if hasattr(obj, '__class__'):\n"
						+ "        if issubclass(obj.__class__, java.lang.Object):\n" + "            helptext = None\n"
						+ "            if hasattr(obj, '__doc__'):\n" + "                helptext = obj.__doc__\n"
						+ "                if not isinstance(helptext, str):\n"
						+ "                    if hasattr(helptext, '__call__'):\n"
						+ "                        helptext = helptext()\n" + "                    else:\n"
						+ "                        helptext = None\n" + "            if helptext is not None:\n"
						+ "                print helptext\n" + "                return\n" + "    import pydoc\n"
						+ "    pydoc.help(obj)\n" + "    print\n");

				populateNamespace();
				runStationStartupScript();

			} catch (Exception ex) {
				String message = "GDAJythonInterpreter: error while initialising " + ex.getMessage();
				logger.error(message, ex);
				throw ex;
			} finally {
				configured = true;
			}
		}
	}

	/**
	 * Adds OEs (and their DOFs) and Scannables to the Jython namespace.
	 */
	private void populateNamespace() {
		logger.info("populating Jython namespace...");
		Finder finder = Finder.getInstance();

		// we need all OEs and their DOFs to be available in the namespace, by wrapping references to them in Adapter
		// objects
		ArrayList<Findable> OEs = finder.listAllObjects("OE");
		for (Findable findable : OEs) {
			try {
				// get object from OE facory
				this.interp.runsource(findable.getName() + "=finder.find('" + findable.getName() + "')");
				// create an OE adapter object
				this.interp.runsource(findable.getName() + "= OEAdapter(" + findable.getName() + ")");
				this.interp.runsource(findable.getName() + ".setName(\"" + findable.getName() + "\")");
				// get array of the DOFNames
				this.interp.runsource("tempArray=" + findable.getName() + ".getDOFNames()");
				// run a for loop which creates a DOFAdapter object associated with each DOF (assumes that all DOFs have
				// unique names)
				String command = "exec(\"for i in range(len(tempArray)):";
				command += "exec(tempArray[i]+\\\"";
				command += "=DOFAdapter(" + findable.getName();
				command += ",'\\\"+tempArray[i]+\\\"')\\\")\")";
				this.interp.runsource(command);
			} catch (Exception e) {
				logger.debug(e.getStackTrace().toString());
			}
		}

		// all Scannable objects should be also placed into the namespace.
		ArrayList<Findable> scannables = finder.listAllObjects("Scannable");
		for (Findable findable : scannables) {
			try {
				this.interp.runsource(findable.getName() + "=finder.find('" + findable.getName() + "')");
			} catch (Exception e) {
				logger.error("Error adding " + findable.getName() + " to namespace", e);
			}
		}
	}

	/**
	 * Runs the station startup script, {@code localStation.py}.
	 */
	private void runStationStartupScript() {
		// import the station startup script
		// run this last as it may use variables set up above
		String gdaStationScript = getGdaStationScript();
		// File localStation = new File(getGdaScriptDir() + "localStation.py");
		if (StringUtils.hasText(gdaStationScript)) {
			logger.info("Running startupScript:" + gdaStationScript );
			try{
				File localStation = new File(gdaStationScript);
				this.runscript(localStation);
				logger.info("Completed startupScript");
			} catch(Exception e){
				logger.error("Error running startupScript",e);
			}
		} else {
			logger.info("No startupScript defined");
		}
	}

	/**
	 * Translates and then runs the given file through the Jython interpreter.
	 *
	 * @param input
	 *            File
	 */
	protected void exec(String input) throws PyException {
		// translate script into true Jython line by line
		PyString tempFile = translateScriptToGDA(input);
		// pass entire script to interpreter
		try {
			interp.exec(tempFile);
		} catch (PyException e) {
			interp.showexception(e);
			throw e;
		}

	}

	/**
	 * Runs the script updating the CommandServer status as it goes.
	 *
	 * @param input
	 *            File
	 * @throws IOException
	 */
	protected void runscript(File input) throws IOException {
		// pass entire script to interpreter
		JythonServerFacade.getInstance().setScriptStatus(Jython.RUNNING);
		try{
			String lines = JythonServerFacade.slurp(input);
			this.exec(lines);
		} finally{
			JythonServerFacade.getInstance().setScriptStatus(Jython.IDLE);
		}
	}

	/**
	 * Gives the command to the JythonInterpreter's runsource method
	 *
	 * @param command
	 *            String
	 * @return boolean
	 */
	protected boolean runsource(String command) {
		try {
			logger.debug("GDA command: " + command);
			command = translator.translate(command);
			logger.debug("Jython command: " + command);
			return interp.runsource(command);
		} catch (Exception e) {
			exceptionUtils.logException(logger, "Error calling runsource for command:" + command, e);
			return false;
		}
	}

	/**
	 * Get the object from the Jython namespace known by the given string.
	 *
	 * @param objectName
	 * @return Object
	 */
	protected Object getFromJythonNamespace(String objectName) {
		return interp.get(objectName, Object.class);
	}

	/**
	 * Returns the contents of the top-level namespace.
	 * <p>
	 * This returns object references so cannot be distributed.
	 *
	 * @return PyObject
	 */
	public PyObject getAllFromJythonNamepsace() {
		return interp.getLocals();
	}

	/**
	 * Place an object into the Jython namespace.
	 *
	 * @param objectName
	 *            What the object is to be known as.
	 * @param obj
	 */
	protected void placeInJythonNamespace(String objectName, Object obj) {
		interp.set(objectName, obj);
	}

	/**
	 * Runs a Jython command which returns some output. As the Jython engine is in a distributed environment, only
	 * strings are returned. Object references will also be converted to strings.
	 *
	 * @param command
	 *            String - must be python code - cannot run import javaclass - this results in fixParseError - unknown
	 *            source
	 * @return String
	 */
	protected String evaluate(String command) {
		String output = null;
		try {
			command = translator.translate(command);
			PyObject result = interp.eval(command);
			output = result.toString();
		} catch (PyException e) {
			// simplify what is logged for namespace errors - otherwise too much information is sent to users
			if (!e.type.toString().contains("exceptions.NameError")) {
				exceptionUtils.logException(logger, "Error evaluating command " + command, e);
			}
		}
		return output;
	}

	/**
	 * Changes dynamically the translator being used.
	 *
	 * @param myTranslator
	 */
	protected void setTranslator(Translator myTranslator) {
		if (myTranslator != null) {
			GDAJythonInterpreter.translator = myTranslator;
		}
	}

	/**
	 * @return the translator
	 */
	protected static Translator getTranslator() {
		return translator;
	}

	/**
	 * Creates a script in a format compatible with the GDAJythonInterpreter runcode method. The main part of the script
	 * is from the input object. All lines are translated from GDA syntax into true Jython from GDA syntax where
	 * necessary. A header and footer are then added to make the script run in a separate thread, so that the GUI does
	 * not freeze up.
	 *
	 * @param input
	 *            a Jython script
	 * @return a string of the same Jython code (translated to true Jython where required) which will run in its own
	 *         thread
	 */
	public static PyString translateScriptToGDA(String input) {
		String output = "";
		String[] lines = input.split("\n");

		for (String line : lines) {
			line = translator.translate(line);

			// check if the line is in fact multiple lines (i.e. contains
			// \n's)
			// this would be not matter if 'line' was being passed directly
			// to
			// the interpreter
			// but as we are building a script file, it is important to keep
			// the
			// correct
			// indention level

			String[] subLines = line.split("\n");
			for (String subLine : subLines) {
				output += subLine + "\n";
			}
		}

		return new PyUnicode(output);
	}

	/**
	 * Recursively moves through a directory and removes all Jython class files. This should be done during the
	 * initialise stage to ensure that a restart forces all Jython bytecode to be refreshed.
	 * <p>
	 * This shouldn't really be needed, and in future Jython releases may not be a problem. But for now it solves an
	 * observed problem on all beamlines that occasionally after a Command Server restart old versions of Jython modules
	 * are in use rather than the latest ones.
	 *
	 * @param dir
	 */
	private void removeAllJythonClassFiles(File dir) {

		// find all .class files and remove them
		FilenameFilter filter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith("$py.class");
			}
		};
		try {
			File[] filesToRemove = dir.listFiles(filter);
			for (File file : filesToRemove) {
				file.delete();
			}
		} catch (NullPointerException e) {
			logger.warn("not a directory or i/o error on: " + dir.toString());
		}

		// find all directories and recursively operate on them
		FileFilter fileFilter = new FileFilter() {
			@Override
			public boolean accept(File file) {
				return file.isDirectory();
			}
		};
		File[] files = dir.listFiles(fileFilter);
		try {
			for (File file : files) {
				removeAllJythonClassFiles(file);
			}
		} catch (RuntimeException e) {
			logger.warn("Could not remove compiled class files from " + dir.toString());
		}
	}

	public InteractiveConsole getInterp() {
		return interp;
	}

}
