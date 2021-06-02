/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.experiment.structure;

import static gda.configuration.properties.LocalProperties.GDA_CONFIG;
import static gda.configuration.properties.LocalProperties.GDA_PROPERTIES_FILE;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.BeforeClass;
import org.junit.Test;

import uk.ac.gda.core.tool.spring.ExperimentContextFile;

/**
 * Tests if the experiment is created using an relative path
 *
 * @author Maurizio Nagni
 */
public class NexusExperimentControllerRelativeRootDirTest extends NexusExperimentControllerTestBase {

	@BeforeClass
	public static void beforeClass() {
		System.setProperty(GDA_CONFIG, "test/resources/relativeContext");
        System.setProperty(GDA_PROPERTIES_FILE, "test/resources/relativeContext/properties/_common/common_instance_java.properties");
	}

	@Test
	public void relativeRootDirFromProperty() throws Exception {
		String experimentPath = getController().startExperiment(EXPERIMENT_NAME).getPath();
		assertThat(experimentPath, startsWith(getContext().getExperimentContext().getContextFile(ExperimentContextFile.EXPERIMENTS_DIRECTORY).getPath()));
		assertThat(experimentPath, containsString("some/subpath"));
	}
}
