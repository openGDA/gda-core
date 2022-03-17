/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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
package uk.ac.diamond.daq.jyunit.test.framework;

import static gda.configuration.properties.LocalProperties.GDA_GIT_LOC;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.Platform;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.python.core.Py;
import org.python.core.PyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.jython.GDAJythonInterpreter;
import gda.jython.InterfaceProvider;
import gda.jython.MockJythonServerFacade;
import gda.jython.ScriptPaths;
import gda.jython.ScriptProject;
import gda.jython.ScriptProjectType;

/**
 * An entry point to allow JyUnit tests to be run as part of a JUnit test configuration.
 * <p>
 * An instance of {@link GDAJythonInterpreter} is used to execute Jython.
 * <p>
 * Tests reports are written to a file as defined by the {@code GdaTestRunner} instance, this can be picked up by
 * Jenkins or opened manually in the workspace to check individual results.
 * <p>
 * To run in the IDE the test should be run as a Plugin Test.
 * <p>
 * This wrapper test will have the status of the worst case of all the individual tests. E.g. if a test is unsuccessful
 * with Error this test will report as Error.
 * <p>
 * Additional script project dependencies are provided by {@link #getScriptProjectPaths()}, if additional Java plugins
 * are required, they are added as dependencies of this plugin.
 */
public abstract class JyUnitTestRunner {
	private static final Logger logger = LoggerFactory.getLogger(JyUnitTestRunner.class);
	private static final String WORKSPACE_GIT = getWorkspaceGit();
	private GDAJythonInterpreter interpreter;
	private List<String> scriptProjectPaths;

	@BeforeClass
	public static void setProperties() {
		// LocalProperties and System properties set here
		// because ConfigurationDefaults sets this as a system property
		// so there can be conflict
		LocalProperties.set(GDA_GIT_LOC, WORKSPACE_GIT);
		System.setProperty(GDA_GIT_LOC, WORKSPACE_GIT);
	}

	@AfterClass
	public static void clearProperties() {
		LocalProperties.clearProperty(GDA_GIT_LOC);
		System.clearProperty(GDA_GIT_LOC);
	}

	protected static String getWorkspaceGit() {
		Path pathFragment = Paths.get("").toAbsolutePath();
		while (pathFragment != null && !pathFragment.getFileName().toString().equals("workspace_git")) {
			pathFragment = pathFragment.getParent();
		}
		if (pathFragment == null) {
			throw new UncheckedIOException(new IOException("Could not find workspace_git directory"));
		}
		return pathFragment.toString();
	}

	@Before
	public void setup() {
		assertTrue("JyUnit tests must be run as plugin tests", Platform.isRunning());
		scriptProjectPaths = getScriptProjectPaths();
		interpreter = setupJython();
		logApplicationBundles();
	}

	private void logApplicationBundles() {
		var bundles = FrameworkUtil.getBundle(getClass()).getBundleContext().getBundles();
		logger.info("Bundles loaded:");
		Arrays.stream(bundles).map(Bundle::getSymbolicName).forEach(b -> logger.info("\t{}", b));

	}

	protected abstract List<String> getScriptProjectPaths();

	protected abstract String getTestScriptPath();

	@Test
	public void runTestScript() throws IOException {
		//TODO check script exists
		executeTestFile(getTestScriptPath());
	}

	private void executeTestFile(String pathWithinWorkspaceGit) throws IOException {
		var absScriptPath = Paths.get(WORKSPACE_GIT, pathWithinWorkspaceGit);
		List<String> fileLines = Files.readAllLines(absScriptPath);
		String fileContents = fileLines.stream().collect(Collectors.joining("\n"));
		logger.info("Executing test script: {}", absScriptPath);
		try {
			// __file__ is used sometimes to discover tests
			interpreter.getInterp().exec(String.format("__file__ = '%s'", absScriptPath));
			interpreter.getInterp().exec(fileContents);
		} catch (PyException e) {
			if (e.type.equals(Py.SystemExit)) {
				int returnCode = e.value.asInt();
				handleReturnCode(returnCode);
			} else {
				// log the Jython trace as this is not visible in test report
				// (only info is a failure due to a PyException)
				logger.info(e.toString());
				throw e;
			}
		}
	}

	private void handleReturnCode(int returnCode) {
		switch (returnCode) {
		case 0:
			logger.info("All tests passed");
			break;
		case 1:
			Assert.fail("There are test failures, see individual test reports");
			break;
		case 2:
			throw new IllegalStateException("There are test errors, see individual test reports");
		default:
			throw new IllegalStateException("Unknown Jython return code");
		}
	}

	private GDAJythonInterpreter setupJython() {
		var absoluteScriptPaths = scriptProjectPaths.stream().map(p -> WORKSPACE_GIT + "/" + p)
				.filter(this::checkPathIsDirectory).collect(Collectors.toList());
		var sProjects = absoluteScriptPaths.stream()
				.map(p -> new ScriptProject(p, "Proj: " + p, ScriptProjectType.CORE)).collect(Collectors.toList());
		GDAJythonInterpreter interp = new GDAJythonInterpreter(new ScriptPaths(sProjects));

		// interp.setTranslator(new NoopTranslator());
		logger.info("Configuring GDAJythonInterpreter for tests");
		initialiseLoggingRedirection(interp);
		logger.info("Jython configured");
		return interp;
	}

	private boolean checkPathIsDirectory(String loc) {
		var path = Paths.get(loc);
		if (Files.isDirectory(path)) {
			return true;
		} else {
			logger.warn("Script path {} does not exist", loc);
			return false;
		}
	}


	/**
	 * Copied from GDAJythonInterpreter
	 */
	private void initialiseLoggingRedirection(GDAJythonInterpreter interp) {
		InterfaceProvider.setTerminalPrinterForTesting(new MockJythonServerFacade());
		String logInit = "import logging\n"
				+ "from loghandling import JythonLogRedirector, JythonTerminalPrinter\n"
				+ "_root_logger = logging.getLogger()\n"
				+ "_root_logger.name = 'gda.jython.root'\n"
				+ "_root_logger.level = 0\n" // set levels to 0 as slf4j filters logging
				+ "_root_logger.addHandler(JythonLogRedirector())\n"
				+ "_root_logger.addHandler(JythonTerminalPrinter(logging.ERROR))\n"
				+ "del logging\n"
				+ "del JythonLogRedirector\n"
				+ "del JythonTerminalPrinter\n"
				+ "del _root_logger\n\n";
		interp.getInterp().exec(logInit);
	}

}