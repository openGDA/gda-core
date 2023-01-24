/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package uk.ac.gda.devices.detector.xspress3.fullCalculations;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assume.assumeThat;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import gda.device.detector.DummyDAServer;
import gda.device.detector.NXDetectorData;
import gda.device.timer.Tfg;
import gda.util.TestUtils;
import uk.ac.gda.devices.detector.xspress3.controllerimpl.DummyXspress3Controller;

public class Xspress3DataOperationsTest {

	private File nexusFile;

	@Before
	public void setup() {
		var testFileLoc = TestUtils.getGDALargeTestFilesLocation();
		assumeThat("GDALargeTestFilesLocation system property not set", testFileLoc, is(notNullValue()));
		nexusFile = new File(testFileLoc + "uk.ac.gda.devices.xspress3.test/46594_0003.hdf5");
	}

	@Test
	public void testNexusChunkSingleFrame() throws Exception {

		DummyDAServer daserver = new DummyDAServer();
		Tfg tfg = new Tfg();
		tfg.setDaServer(daserver);

		DummyXspress3Controller controller = new DummyXspress3Controller(tfg,daserver);
		controller.setNumFramesToAcquire(1);
		controller.setNumberOfChannels(10);

		Xspress3DataOperations dataOps = new Xspress3DataOperations(controller);
		String configFile = TestUtils.getResourceAsFile(this.getClass(), "Xspress3_ParametersCu_K.xml").getPath();
		dataOps.setConfigFileName(configFile);
		dataOps.loadConfigurationFromFile();

		controller.setSimulationFileName(nexusFile.getPath());

		NXDetectorData treeProvider = dataOps.readoutFrames(1, 1,"xspress3")[0];

		org.junit.Assert.assertEquals(11,treeProvider.getExtraNames().length);
		org.junit.Assert.assertEquals(12,treeProvider.getOutputFormat().length);  // + 1 for the input name
		org.junit.Assert.assertEquals(11,treeProvider.getDoubleVals().length);

		org.junit.Assert.assertTrue(treeProvider.getNexusTree() != null);

		org.junit.Assert.assertEquals(4096,((double[]) treeProvider.getNexusTree().getNode("xspress3/AllElementSum").getData().getBuffer()).length);

		org.junit.Assert.assertEquals(10,((double[]) treeProvider.getNexusTree().getNode("xspress3/Cu").getData().getBuffer()).length);

		org.junit.Assert.assertEquals(1,((double[]) treeProvider.getNexusTree().getNode("xspress3/FF").getData().getBuffer()).length,0.001);


	}
}
