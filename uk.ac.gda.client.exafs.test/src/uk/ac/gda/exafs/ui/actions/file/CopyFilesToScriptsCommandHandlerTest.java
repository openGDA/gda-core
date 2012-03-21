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

package uk.ac.gda.exafs.ui.actions.file;

import org.junit.Assert;
import gda.configuration.properties.LocalProperties;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import uk.ac.gda.client.experimentdefinition.ui.handlers.CopyFilesToScriptsCommandHandler;

@Ignore("2010/03/03 Test ignored as following r34622, it needs to be reworked. It passes when run as as a Plugin Test, but throws lots of exceptions.")
public class CopyFilesToScriptsCommandHandlerTest {

	private CopyFilesToScriptsCommandHandler handler;
	
	@Before
	public void setUp() {
		handler = new CopyFilesToScriptsCommandHandler();
		handler.setJythonContextForTesting(new MockJythonContext());
		LocalProperties.set("gda.jython.userScriptDir", "gda.jython.userScriptDir");
	}

	@After
	public void tearDown() {
		LocalProperties.clearProperty("gda.jython.userScriptDir");
		handler = null;
	}

	@Test
	public void testGetDestinationFolderForCopyUsesJythonContext() {
		Assert.assertFalse("shouldn't find userScriptDir in the destination folder", handler.getDestinationFolderForCopy().contains("gda.jython.userScriptDir"));
		Assert.assertTrue("should find JythonContext used to create destination folder", handler.getDestinationFolderForCopy().contains("IJythonContext"));
	}

}
