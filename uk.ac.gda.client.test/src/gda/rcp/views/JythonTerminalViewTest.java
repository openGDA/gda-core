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

package gda.rcp.views;

import gda.configuration.properties.LocalProperties;

import java.io.File;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import uk.ac.gda.client.test.MockJythonContext;

public class JythonTerminalViewTest {

	private JythonTerminalView terminalView;
	private String testPath = "files";
	private File testDir;
	private File bar;
	private File baz;

	@Before
	public void setUp() throws Exception {
		terminalView = new JythonTerminalView();
		terminalView.setJythonContextForTesting(new MockJythonContext());
		testDir = new File(testPath);
		if(!(testDir.mkdir())) {
			throw new RuntimeException("Couldn't create folder at " + testPath);
		}
		baz = new File("baz");
		if(!(baz.mkdir())) {
			throw new RuntimeException("Couldn't create folder at baz");
		}
		bar = new File("bar");
		if(!(bar.mkdir())) {
			throw new RuntimeException("Couldn't create folder at bar");
		}
		LocalProperties.set("gda.jython.terminalOutputDir", "foo");
		LocalProperties.set("gda.jython.userScriptDir", "bar");
	}

	@After
	public void tearDown() throws Exception{
		terminalView = null;
		if(!(testDir.delete())) {
			throw new RuntimeException("Couldn't delete folder at " + testPath);
		}
		testDir = null;
		if(!(baz.delete())) {
			throw new RuntimeException("Couldn't delete folder at baz");
		}
		baz = null;
		if(!(bar.delete())) {
			throw new RuntimeException("Couldn't delete folder at bar");
		}
		bar = null;
		LocalProperties.clearProperty("gda.jython.terminalOutputDir");
		LocalProperties.clearProperty("gda.jython.userScriptDir");
	}

	@Test
	public void testInstantiation() {
		Assert.assertNotNull(terminalView);
	}

	@Test
	public void testUserScriptDirNotUsedIfTerminalOutputFolderPropertyDefined() {
		File foo = new File("foo");
		foo.mkdir();
		Assert.assertEquals("foo/", terminalView.getTerminalOutputDirName());
		foo.delete();
	}

	@Test
	public void testUserScriptDirNotUsedForTerminalOutputFolder() {
		String tod = LocalProperties.get("gda.jython.terminalOutputDir");
		LocalProperties.clearProperty("gda.jython.terminalOutputDir");
		Assert.assertFalse("Shouldn't get the folder 'bar'", terminalView.getTerminalOutputDirName().equals("bar/"));
		LocalProperties.set("gda.jython.terminalOutputDir", tod);
	}

	@Test
	public void testUserScriptDirNotUsedForCommandHistoryFile() {
		Assert.assertFalse("The folder 'bar' comes from gda.jython.userScriptDir", terminalView.getCommandFilename().startsWith("bar"));
	}
}
