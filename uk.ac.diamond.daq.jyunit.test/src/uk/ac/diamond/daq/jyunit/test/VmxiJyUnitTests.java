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

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;

import gda.configuration.properties.LocalProperties;
import uk.ac.diamond.daq.jyunit.test.framework.JyUnitTestRunner;

@Ignore("DAQ-3977 Tests currently failing")
public class VmxiJyUnitTests extends JyUnitTestRunner {

	@Before
	public void setConfig() {
		var vmxiConfig = JyUnitTestRunner.getWorkspaceGit() + "/" + "gda-mx.git/configurations/i02-2-config";
		System.setProperty(GDA_CONFIG, vmxiConfig);
		LocalProperties.set(GDA_CONFIG, vmxiConfig);
	}

	@After
	public void clearConfig() {
		System.clearProperty(GDA_CONFIG);
		LocalProperties.clearProperty(GDA_CONFIG);
	}

	@Override
	protected List<String> getScriptProjectPaths() {
		return List.of("gda-core.git/uk.ac.gda.core/scripts", "scisoft-core.git/uk.ac.diamond.scisoft.python/src",
				"gda-mx.git/configurations/i02-2-config/scripts", "gda-mx.git/configurations/mx-config/scripts");
	}

	@Override
	protected String getTestScriptPath() {
		return "gda-mx.git/configurations/i02-2-config/scripts/unit_testing/testing.py";
	}

}
