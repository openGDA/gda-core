/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ScriptPathsTest {

	private ScriptPaths defaultPaths, testfilesPaths;
	private List<ScriptProject> emptyProjects;
	private String testfilesPath = "testfiles/gda/jython/JythonServerTest";
	private String[] emptyFilesPathsList = new String[] {testfilesPath + "/Hello", testfilesPath + "/Test"};
	private String badPath = "not a valid path";
	private ScriptProject testfilesProject;
	private String startupScript = "/some/folder/localStation.py";
	private File existsScriptLocationFile = new File("testfiles/gda/jython/JythonServerTest" + File.separator + "exists.py");

	@Before
	public void setUp() throws IOException {
		defaultPaths = new ScriptPaths();
		testfilesProject = new ScriptProject(testfilesPath, "Test Project", ScriptProjectType.USER);
		testfilesPaths = new ScriptPaths(Collections.singletonList(testfilesProject));
		testfilesPaths.setStartupScript(startupScript);
		emptyProjects = new ArrayList<ScriptProject>();
		for (String path : emptyFilesPathsList) {
			emptyProjects.add(new ScriptProject(path, "Project: "+ path, ScriptProjectType.CONFIG));
		}
	}

	@After
	public void tearDown() {
		defaultPaths = null;
		testfilesPaths = null;
		testfilesProject = null;
		emptyProjects = null;
	}

	@Test
	public void testDefaultPathsListIsEmpty() {
		List<String> paths = defaultPaths.getPaths();
		Assert.assertEquals(0, paths.size());
	}

	@Test
	public void testListConstructorKeepsList() throws IOException {
		ScriptPaths fromList = new ScriptPaths(emptyProjects);
		for (int i = 0; i < emptyFilesPathsList.length; i++) {
			Assert.assertEquals(new File(emptyFilesPathsList[i]).getCanonicalPath(), new File(fromList.getPaths().get(i)).getPath());
		}
	}

	@Test
	public void testListCanBePassedToObject() throws IOException {
		defaultPaths.setProjects(emptyProjects);
		for (int i = 0; i < emptyFilesPathsList.length; i++) {
			Assert.assertEquals(new File(emptyFilesPathsList[i]).getCanonicalPath(), new File(defaultPaths.getPaths().get(i)).getPath());
		}
	}

	@Test
	public void testScriptCannotBeFoundInEmptyPathList() {
		Assert.assertNull(defaultPaths.pathToScript("hello"));
	}

	@Test
	public void testNoNameScriptCannotBeFound() {
		Assert.assertNull(testfilesPaths.pathToScript(""));
	}

	@Test
	public void testRealScriptCanBeFound() throws IOException {
		String scriptPath = new File(testfilesPaths.pathToScript("exists.py")).getPath();
		Assert.assertEquals(existsScriptLocationFile.getCanonicalPath(), scriptPath);
	}

	@Test
	public void testNonexistentScriptCannotBeFound() {
		String scriptPath = testfilesPaths.pathToScript("doesnotexist.py");
		Assert.assertNull(scriptPath);
	}

	@Test
	public void testAutomaticAdditionOfDotpyExtension() throws IOException {
		String scriptPath = new File(testfilesPaths.pathToScript("exists")).getPath();
		Assert.assertEquals(existsScriptLocationFile.getCanonicalPath(), scriptPath);
	}

	@Test
	public void testThatDescriptionContainsAllOfThePaths() {
		defaultPaths.setProjects(emptyProjects);
		String description = defaultPaths.description();
		Assert.assertTrue(description.contains("Hello"));
		Assert.assertTrue(description.contains("Test"));
	}

	@Test
	public void testDefaultStartupScriptIsUnknown() {
		Assert.assertNull(defaultPaths.getStartupScript());
	}

	@Test
	public void testScriptPathsObjectCanReportWhatStartupScriptToUse() {
		Assert.assertEquals(startupScript, testfilesPaths.getStartupScript());
	}

	@Test
	public void badPathIsDetectedAndMarked() {
		ScriptProject fail = new ScriptProject(badPath, "Fail", ScriptProjectType.CONFIG);
		Assert.assertTrue(fail.getPath().startsWith("UNRESOLVED:"));
	}
}
