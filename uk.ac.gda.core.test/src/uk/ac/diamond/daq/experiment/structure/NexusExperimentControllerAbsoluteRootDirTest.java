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
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.net.URL;

import org.junit.BeforeClass;
import org.junit.Test;


/**
 * Tests if the experiment is created using an absolute path
 *
 * @author Maurizio Nagni
 */
public class NexusExperimentControllerAbsoluteRootDirTest extends NexusExperimentControllerTestBase {

	@BeforeClass
	public static void beforeClass() {
		System.setProperty(GDA_CONFIG, "test/resources/absoluteContext");
        System.setProperty(GDA_PROPERTIES_FILE, "test/resources/absoluteContext/properties/_common/common_instance_java.properties");
	}

	@Test
	public void absoluteRootDirFromProperty() throws Exception {
		File file = new File("/tmp/nexusTest");
		if (file.exists()) file.delete();
		URL experimentUrl = getController().startExperiment(EXPERIMENT_NAME);
		assertThat(experimentUrl.getPath(), startsWith("/tmp/nexusTest"));
	}
}
