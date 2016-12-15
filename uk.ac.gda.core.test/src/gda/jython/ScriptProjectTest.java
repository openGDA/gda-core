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


import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ScriptProjectTest {

	private ScriptProject userProject;
	private ScriptProject configProject;
	private ScriptProject coreProject;

	@Before
	public void setUp() throws IOException {
		userProject = new ScriptProject("/tmp", "Scripts - User", ScriptProjectType.USER);
		configProject = new ScriptProject("/tmp", "Scripts - Config", ScriptProjectType.CONFIG);
		coreProject = new ScriptProject("/tmp", "Scripts - Core", ScriptProjectType.CORE);
	}

	@After
	public void tearDown() {
		userProject = null;
		configProject = null;
		coreProject = null;
	}

	@Test
	public void testAccessors() {
		assertThat(userProject.getPath(), is("/tmp"));
		assertThat(userProject.getName(), is("Scripts - User"));
	}

	@Test
	public void testUserProjectHasUserBit() {
		assertThat(userProject.isUserProject(), is(true));
	}

	@Test
	public void testNonUserProjectsHaveNoUserBit() {
		assertThat(configProject.isUserProject(), is(false));
		assertThat(coreProject.isUserProject(), is(false));
	}

	@Test
	public void testConfigProjectBit() {
		assertThat(userProject.isConfigProject(), is(false));
		assertThat(configProject.isConfigProject(), is(true));
		assertThat(coreProject.isConfigProject(), is(false));
	}

	@Test
	public void testCoreProjectBit() {
		assertThat(userProject.isCoreProject(), is(false));
		assertThat(configProject.isCoreProject(), is(false));
		assertThat(coreProject.isCoreProject(), is(true));
	}
}
