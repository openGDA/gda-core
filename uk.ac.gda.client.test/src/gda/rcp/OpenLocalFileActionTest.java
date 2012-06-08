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

package gda.rcp;

import gda.configuration.properties.LocalProperties;
import gda.jython.IJythonContext;
import gda.rcp.OpenLocalFileAction;

import org.eclipse.ui.IWorkbenchWindow;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import uk.ac.gda.client.test.MockJythonContext;

public class OpenLocalFileActionTest {

	private OpenLocalFileAction action;
	private IWorkbenchWindow window;
	private IJythonContext context;
	
	@Before
	public void setUp() {
		LocalProperties.set("gda.jython.userScriptDir", "gda.jython.userScriptDir");
		window = new MockWorkbenchWindow();
		context = new NullJythonContext();
		action = new OpenLocalFileAction();
		action.setJythonContextForTesting(context);
		action.init(window);
	}

	@After
	public void tearDown() {
		LocalProperties.clearProperty("gda.jython.userScriptDir");
		window = null;
		action = null;
	}

	@Test
	public void testFilterPathNotBasedOnUserScriptDir() {
		Assert.assertFalse("the action's filter path came from gda.jython.userScriptDir", action.getFilterPathForTesting().startsWith("gda.jython.userScriptDir"));
	}
	
	@Test
	public void testFilterPathUsesHomeFolderIfNoScriptPaths() {
		Assert.assertEquals(System.getProperty("user.home"), action.getFilterPathForTesting());
	}
	
	@Test
	public void testFilterPathUsesScriptPathWhereAvailable() {
		IJythonContext context = new MockJythonContext();
		action.setJythonContextForTesting(context);
		// note assumption that we can call init() again
		action.init(window);
		Assert.assertEquals("baz/", action.getFilterPathForTesting());
	}
}
