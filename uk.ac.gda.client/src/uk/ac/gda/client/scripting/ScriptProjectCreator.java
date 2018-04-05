/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package uk.ac.gda.client.scripting;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.python.copiedfromeclipsesrc.JavaVmLocationFinder;
import org.python.pydev.core.IInterpreterInfo;
import org.python.pydev.core.IPythonNature;
import org.python.pydev.editor.codecompletion.revisited.ModulesManagerWithBuild;
import org.python.pydev.plugin.PydevPlugin;
import org.python.pydev.plugin.nature.PythonNature;
import org.python.pydev.runners.SimpleJythonRunner;
import org.python.pydev.shared_core.io.FileUtils;
import org.python.pydev.shared_core.structure.Tuple;
import org.python.pydev.ui.interpreters.JythonInterpreterManager;
import org.python.pydev.ui.pythonpathconf.InterpreterInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.jython.JythonServerFacade;
import gda.rcp.GDAClientActivator;
import uk.ac.gda.common.rcp.util.BundleUtils;
import uk.ac.gda.ui.utils.ProjectUtils;

/**
 * Class creates/removes script projects and XML projects if required to by preferences. Also can automatically create a Jython Interpreter
 * and assign PyDev nature to the script projects.
 */
public class ScriptProjectCreator {

	private static final String PYDEV_INTERPRETER_VERSION = IPythonNature.JYTHON_VERSION_2_7;
	private static final String JYTHON_MAJOR_VERSION = "2";
	private static final String JYTHON_MINOR_VERSION = "7";
	private static final String JYTHON_VERSION = JYTHON_MAJOR_VERSION + "." + JYTHON_MINOR_VERSION;
	private static final String JYTHON_BUNDLE = "uk.ac.diamond.jython";
	private static final String JYTHON_DIR = "jython" + JYTHON_VERSION;
	private static final String JYTHON_JAR = "jython.jar";

	/**
	 * Name of the Jython interpreter that will be created within PyDev for the client.
	 */
	private static final String INTERPRETER_NAME = "Jython" + JYTHON_MAJOR_VERSION;

	private static final Logger logger = LoggerFactory.getLogger(ScriptProjectCreator.class);
	private static Map<String, IProject> pathProjectMap = new HashMap<String, IProject>();

	private static String getProjectNameXMLConfig() {
		return getProjectName("gda.scripts.user.xml.project.name", "XML - Config");
	}

	private static String getProjectName(final String property, final String defaultValue) {
		String projectName = LocalProperties.get(property);
		if (projectName == null)
			projectName = defaultValue;
		return projectName;
	}

	public static void handleShowXMLConfig(IProgressMonitor monitor) throws CoreException {
		final IPreferenceStore store = GDAClientActivator.getDefault().getPreferenceStore();
		if (store.getBoolean(PreferenceConstants.SHOW_XML_CONFIG)) {
			ProjectUtils.createImportProjectAndFolder(getProjectNameXMLConfig(), "src",
					LocalProperties.get(LocalProperties.GDA_CONFIG) + "/xml", null, null, monitor);
		} else {
			final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			final IProject project = root.getProject(getProjectNameXMLConfig());
			if (project.exists()) {
				// exists so delete
				project.delete(false, true, monitor);
			}
		}

	}

	/**
	 * We programmatically create a Jython Interpreter so that the user does not have to.
	 */
	private static void createInterpreter(IProgressMonitor monitor) throws Exception {

		final IPreferenceStore preferenceStore = GDAClientActivator.getDefault().getPreferenceStore();

		// Horrible Hack warning: This code is copied from parts of Pydev to set up the interpreter and save it.
		// Code copies from Pydev when the user chooses a Jython interpreter - these are the defaults.

		final Path interpreterPath = Paths.get(BundleUtils.getBundleLocation(JYTHON_BUNDLE).getAbsolutePath(),
				JYTHON_DIR);
		final String executable = interpreterPath.resolve(JYTHON_JAR).toString();
		if (!(new File(executable)).exists())
			throw new Exception("Jython jar not found  :" + executable);

		final File script = PydevPlugin.getScriptWithinPySrc("interpreterInfo.py");
		if (!script.exists()) {
			throw new RuntimeException("The file specified does not exist: " + script);
		}
		monitor.subTask("Creating interpreter");
		// gets the info for the python side
		String encoding = null;
		Tuple<String, String> outTup = new SimpleJythonRunner().runAndGetOutputWithJar(
				FileUtils.getFileAbsolutePath(script), executable, null, null, null, monitor, encoding);

		InterpreterInfo info = null;
		try {
			// HACK Otherwise Pydev shows a dialog to the user.
			ModulesManagerWithBuild.IN_TESTS = true;
			info = InterpreterInfo.fromString(outTup.o1, false);
		} catch (Exception e) {
			logger.error("Something went wrong creating the InterpreterInfo.", e);
		} finally {
			ModulesManagerWithBuild.IN_TESTS = false;
		}

		if (info == null) {
			// cancelled
			return;
		}
		// the executable is the jar itself
		info.executableOrJar = executable;

		// we have to find the jars before we restore the compiled libs
		if (preferenceStore.getBoolean(PreferenceConstants.GDA_PYDEV_ADD_DEFAULT_JAVA_JARS)) {
			List<File> jars = JavaVmLocationFinder.findDefaultJavaJars();
			for (File jar : jars) {
				info.libs.add(FileUtils.getFileAbsolutePath(jar));
			}
		}

		// Defines all third party libs that can be used in scripts.
		if (preferenceStore.getBoolean(PreferenceConstants.GDA_PYDEV_ADD_GDA_LIBS_JARS)) {
			final List<String> gdaJars = LibsLocationFinder.findGdaLibs();
			info.libs.addAll(gdaJars);
		}

		// Defines gda classes which can be used in scripts.
		final String gdaInterfacePath = LibsLocationFinder.findGdaInterface();
		if (gdaInterfacePath != null) {
			info.libs.add(gdaInterfacePath);
		}

		List<String> allScriptProjectFolders = JythonServerFacade.getInstance().getAllScriptProjectFolders();
		for (String s : allScriptProjectFolders) {
			info.libs.add(s);
		}

		// java, java.lang, etc should be found now
		info.restoreCompiledLibs(monitor);
		info.setName(INTERPRETER_NAME);

		final JythonInterpreterManager man = (JythonInterpreterManager) PydevPlugin.getJythonInterpreterManager();
		HashSet<String> set = new HashSet<String>();
		set.add(INTERPRETER_NAME);
		man.setInfos(new IInterpreterInfo[] { info }, set, monitor);

		logger.info("Jython interpreter registered: {}", INTERPRETER_NAME);
	}


	/**
	 * Base method of this class for setting up script projects
	 *  which calls other class methods to: create a Jython interpreter if preferences are set,
	 * create script projects in the client, manage which projects are shown (created) based on preferences,
	 * add PyDev nature to these projects and add script projects to working set.
	 */
	public static void createProjects(IProgressMonitor monitor) throws Exception {
		monitor.subTask("Checking existence of projects");
		final IPreferenceStore store = GDAClientActivator.getDefault().getPreferenceStore();
		boolean chkGDASyntax = store.getBoolean(PreferenceConstants.CHECK_SCRIPT_SYNTAX);


		// The behaviour of the property: gda.client.jython.automatic.interpreter is to prevent auto interpreter set up
		// if set to anything. It is not set by default.
		if (chkGDASyntax && (System.getProperty("gda.client.jython.automatic.interpreter") == null)) {
			monitor.subTask("Checking if interpreter already exists");
			if (isInterpreterCreationRequired()) {
				createInterpreter(monitor);
			}
		}

		List<IAdaptable> scriptProjects = new ArrayList<IAdaptable>();

		for (String path : JythonServerFacade.getInstance().getAllScriptProjectFolders()) {
			String projectName = JythonServerFacade.getInstance().getProjectNameForPath(path);
			boolean shouldShowProject = checkShowProjectPref(path, store);
			if (shouldShowProject) {
				final IProject newProject = createJythonProject(projectName, path, chkGDASyntax, monitor);
				scriptProjects.add(newProject);
				pathProjectMap.put(path, newProject);
			} else {
				final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
				final IProject project = root.getProject(projectName);
				if (project.exists()) {
					// exists so delete rather than hide for efficiency reasons
					try {
						project.delete(false, true, monitor);
					} catch (CoreException e) {
						logger.warn("Error deleting project {}", projectName, e);
					}
				}
			}
		}

		IWorkingSetManager workingSetManager = PlatformUI.getWorkbench().getWorkingSetManager();
		IWorkingSet workingSet = workingSetManager.getWorkingSet("Scripts");
		if (workingSet == null) {
			monitor.subTask("Adding Scripts working set");
			workingSetManager.addWorkingSet(
					workingSetManager.createWorkingSet("Scripts", scriptProjects.toArray(new IAdaptable[] {})));
		} else {
			for (IAdaptable element : scriptProjects) {
				workingSetManager.addToWorkingSets(element, new IWorkingSet[] { workingSet });
			}
		}
	}

	/**
	 * Checks GDA script preferences to see if the project should be shown or hidden in the workspace
	 */
	private static boolean checkShowProjectPref(String path, IPreferenceStore store) throws RuntimeException {
		if (JythonServerFacade.getInstance().projectIsUserType(path)) {
			return true;
		}
		if (JythonServerFacade.getInstance().projectIsConfigType(path)) {
			return store.getBoolean(PreferenceConstants.SHOW_CONFIG_SCRIPTS);
		}
		if (JythonServerFacade.getInstance().projectIsCoreType(path)) {
			return store.getBoolean(PreferenceConstants.SHOW_GDA_SCRIPTS);
		}
		throw new RuntimeException("Unknown type of Jython Script Project: " + path + " = "
				+ JythonServerFacade.getInstance().getProjectNameForPath(path));
	}

	/**
	 * Uses ProjectUtils to create the script project in the workspace and also adds PyDev specific (.pydevproject) and
	 * PyDev Eclipse nature (in .project) when chkGDASyntax is true. Otherwise removes just the nature from .project
	 */
	private static IProject createJythonProject(final String projectName, final String importFolder,
			final boolean chkGDASyntax, IProgressMonitor monitor) throws CoreException {

		IProject project = ProjectUtils.createImportProjectAndFolder(projectName, "src", importFolder, null, null,
				monitor);
		boolean hasPythonNature = PythonNature.getPythonNature(project) != null;

		if (chkGDASyntax) {
			if (!hasPythonNature) {
				// Adds pydev nature of PYDEV_INTERPRETER_VERSION and set to use default interpreter for that version
				// NOTE Very important to start the name with a '/'
				// or pydev creates the wrong kind of nature.
				PythonNature.addNature(project, monitor, PYDEV_INTERPRETER_VERSION,
						"/" + project.getName() + "/src", null, IPythonNature.DEFAULT_INTERPRETER, null);
			}
		} else {
			if (hasPythonNature) {
				// This should do the same as removing the Pydev config but neither
				// prevent it being added when a python file is edited.
				PythonNature.removeNature(project, monitor);
				// This method removes just the nature in .project
			}
		}
		return project;
	}

	/**
	 * Checks the list of interpreters registered in PyDev for a Jython Interpreter matching the correct version. Used to
	 * determine whether a new one needs to be created.
	 *
	 * @return true if new interpreter required
	 */
	private static boolean isInterpreterCreationRequired() {
		logger.debug("Checking for any existing Jython Interpreters in Pydev");
		IInterpreterInfo[] infos = PydevPlugin.getJythonInterpreterManager().getInterpreterInfos();
		final boolean correctInterpreterVersionPresent = Arrays.stream(infos)
				.anyMatch(info -> (info.getInterpreterType() == IPythonNature.INTERPRETER_TYPE_JYTHON)
						&& info.getVersion().equals(JYTHON_VERSION));
		if (correctInterpreterVersionPresent) {
			logger.info("Found a PyDev Jython interpreter of version {})", JYTHON_VERSION);
		}
		// If present return false - creation not required
		return !correctInterpreterVersionPresent;
	}
}
