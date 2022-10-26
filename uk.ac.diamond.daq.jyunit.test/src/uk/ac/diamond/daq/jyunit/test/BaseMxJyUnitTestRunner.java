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

package uk.ac.diamond.daq.jyunit.test;

import static gda.configuration.properties.LocalProperties.GDA_CONFIG;

import java.nio.file.Paths;
import java.util.List;

import org.junit.After;
import org.junit.Before;

import gda.configuration.properties.LocalProperties;
import uk.ac.diamond.daq.jyunit.test.framework.JyUnitTestRunner;

abstract class BaseMxJyUnitTestRunner extends JyUnitTestRunner {

	static final List<String> CORE_SCRIPT_PATHS =
			new GdaCoreJyUnitTests().getScriptProjectPaths();

	abstract String getSpecifiedConfiguration();

	@Before
	public void setConfig() {
		var specifiedConfig = getSpecifiedConfiguration();
		var repoConfigPath = MxPathsUtils.mxRepoConfigPathOf(specifiedConfig).toString();
		var sharedConfig = Paths.get(getWorkspaceGit(), repoConfigPath)
								.toString();
		System.setProperty(GDA_CONFIG, sharedConfig);
		LocalProperties.set(GDA_CONFIG, sharedConfig);
	}

	@After
	public void clearConfig() {
		System.clearProperty(GDA_CONFIG);
		LocalProperties.clearProperty(GDA_CONFIG);
	}

	@Override
	protected String getTestScriptPath() {
		var specifiedConfig = getSpecifiedConfiguration();
		return MxPathsUtils.unitTestingScriptPath(specifiedConfig);
	}

	@Override
	protected List<String> getScriptProjectPaths() {
		var specifiedConfig = getSpecifiedConfiguration();
		var sharedScriptPath = MxPathsUtils.mxRepoScriptPathOf(specifiedConfig);
		var mxScriptPath = MxPathsUtils.mxRepoScriptPath();
		return MxPathsUtils.collectUniquePathList(CORE_SCRIPT_PATHS, sharedScriptPath, mxScriptPath);
	}
}
