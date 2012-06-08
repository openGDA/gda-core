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


import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ScriptPathsTest {

	private ScriptPaths defaultPaths, testfilesPaths;
	private String[] nonsensePathsList = new String[] {"Hello", "Test"};
	private List<ScriptProject> nonsenseProjects;
	private String testfilesPath = "test/gda/jython/testfiles";
	private ScriptProject testfilesProject;
	private String existsScriptLocation = "test/gda/jython/testfiles" + File.separator + "exists.py";
	private String startupScript = "/some/folder/localStation.py";
	
	@Before
	public void setUp() {
		defaultPaths = new ScriptPaths();
		testfilesProject = new ScriptProject(testfilesPath, "Test Project", ScriptProjectType.USER);
		testfilesPaths = new ScriptPaths(Collections.singletonList(testfilesProject));
		testfilesPaths.setStartupScript(startupScript);
		nonsenseProjects = new ArrayList<ScriptProject>();
		for (String path : nonsensePathsList) {
			nonsenseProjects.add(new ScriptProject(path, "Project: "+ path, ScriptProjectType.CONFIG));
		}
	}

	@After
	public void tearDown() {
		defaultPaths = null;
		testfilesPaths = null;
		testfilesProject = null;
		nonsenseProjects = null;
	}

	@Test
	public void testDefaultPathsListIsEmpty() {
		List<String> paths = defaultPaths.getPaths();
		Assert.assertEquals(0, paths.size());
	}
	
	@Test
	public void testListConstructorKeepsList() {
		ScriptPaths fromList = new ScriptPaths(nonsenseProjects);
		Assert.assertArrayEquals(nonsensePathsList, fromList.getPaths().toArray(new String[0]));
	}
	
	@Test
	public void testListCanBePassedToObject() {
		defaultPaths.setProjects(nonsenseProjects);
		Assert.assertArrayEquals(nonsensePathsList, defaultPaths.getPaths().toArray(new String[0]));
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
	public void testRealScriptCanBeFound() {
		String scriptPath = testfilesPaths.pathToScript("exists.py");
		Assert.assertEquals(existsScriptLocation, scriptPath);
	}
	
	@Test
	public void testNonexistentScriptCannotBeFound() {
		String scriptPath = testfilesPaths.pathToScript("doesnotexist.py");
		Assert.assertNull(scriptPath);
	}
	
	@Test
	public void testAutomaticAdditionOfDotpyExtension() {
		String scriptPath = testfilesPaths.pathToScript("exists");
		Assert.assertEquals(existsScriptLocation, scriptPath);
	}
	
	@Test
	public void testThatDescriptionContainsAllOfThePaths() {
		defaultPaths.setProjects(nonsenseProjects);
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
}
