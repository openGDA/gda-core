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

package gda.device.detector.mythen;

import static org.junit.Assert.assertEquals;

import gda.configuration.properties.LocalProperties;
import gda.device.Scannable;
import gda.device.detector.mythen.client.DummyMythenClient;
import gda.device.detector.mythen.client.MythenClient;
import gda.device.detector.mythen.data.AngularCalibrationParametersFile;
import gda.device.detector.mythen.data.DataConverter;
import gda.device.scannable.DummyScannable;
import gda.jython.InterfaceProvider;
import gda.jython.MockJythonServerFacade;
import gda.util.TestUtils;

import java.io.File;

import org.junit.Test;
/**
 * Tests {@link MythenDetectorImpl}.
 */
public class MythenDetectorImplTest {

	/**
	 * Tests that the detector is reading the delta position each time a data collection is performed.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testDelta() throws Exception {
		final File scratchDir = TestUtils.createClassScratchDirectory(MythenDetectorImplTest.class);
		System.out.println(scratchDir);
		LocalProperties.set(LocalProperties.GDA_VAR_DIR, scratchDir.getAbsolutePath());
		LocalProperties.set(LocalProperties.GDA_DATAWRITER_DIR, scratchDir + "/Data");
		InterfaceProvider.setTerminalPrinterForTesting(new MockJythonServerFacade());

		Scannable delta = new DummyScannable();

		DataConverter dataConverter = new DataConverter();
		File angCalParamsFile = new File("testfiles/gda/device/detector/mythen/data/ang.off");
		dataConverter.setAngularCalibrationParameters(new AngularCalibrationParametersFile(angCalParamsFile));

		MythenClient mythenClient = new DummyMythenClient(18);

		MythenDetectorImpl mythen = new MythenDetectorImpl();
		mythen.configure();
		mythen.setCollectionTime(1.0);
		mythen.setDetectorID("Mythen");
		mythen.setMythenClient(mythenClient);
		mythen.setDataConverter(dataConverter);
		mythen.setDeltaScannable(delta);

		delta.asynchronousMoveTo(0);
		mythen.collectData(); // change collectData() method to Non-blocking as interface defined
		while (mythen.isBusy()){
			Thread.sleep(100);
		}
		double[][] data1 = mythen.readoutProcessedData().toDoubleArray();
		assertEquals(0.0017567, data1[0][0], 0.001);

		delta.asynchronousMoveTo(10);
		mythen.collectData();
		while (mythen.isBusy()){
			Thread.sleep(100);
		}
		double[][] data2 = mythen.readoutProcessedData().toDoubleArray();
		assertEquals(10.0 + data1[0][0], data2[0][0], 0.001);
	}

}
