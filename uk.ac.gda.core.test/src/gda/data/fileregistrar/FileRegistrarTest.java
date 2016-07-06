/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

package gda.data.fileregistrar;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import gda.TestHelpers;
import gda.configuration.properties.LocalProperties;
import gda.data.PathConstructor;
import gda.data.metadata.Metadata;
import gda.data.scan.datawriter.DefaultDataWriterFactory;
import gda.data.scan.datawriter.IDataWriterExtender;
import gda.device.Detector;
import gda.device.Scannable;
import gda.scan.ConcurrentScan;

import java.io.File;

import org.eclipse.january.dataset.Dataset;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to test writing of nexus files during a scan
 */
public class FileRegistrarTest {

	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(FileRegistrarTest.class);

	static String testScratchDirectoryName = null;

	/**
	 * Setups of environment for the tests
	 *
	 * @param name
	 *            of test
	 * @param makedir
	 *            if true the scratch dir is deleted and constructed
	 * @throws Exception
	 *             if setup fails
	 */
	public static void setUp(String name, boolean makedir) throws Exception {
		testScratchDirectoryName = new File(TestHelpers.setUpTest(FileRegistrarTest.class, name, makedir))
				.getAbsolutePath();
		LocalProperties.set(PathConstructor.getDefaultPropertyName(), testScratchDirectoryName);
	}

	static void runScanToCreateFile(IDataWriterExtender dataWriterExtender) throws InterruptedException, Exception {
		Scannable simpleScannable1 = TestHelpers.createTestScannable("SimpleScannable1", 0., new String[] {},
				new String[] { "simpleScannable1" }, 0, new String[] { "%5.2g" }, null);

		Scannable simpleScannable2 = TestHelpers.createTestScannable("SimpleScannable2", 0., new String[] {},
				new String[] { "simpleScannable2" }, 0, new String[] { "%5.2g" }, null);

		int[] dims1 = new int[] { 10 };
		Detector simpleDetector = TestHelpers.createTestDetector("SimpleDetector", 0.,
				new String[] { "simpleDetector1" }, new String[] {}, 0, new String[] { "%5.2g", "%5.2g", "%5.2g",
						"%5.2g", "%5.2g", "%5.2g", "%5.2g", "%5.2g", "%5.2g", "%5.2g" }, TestHelpers
						.createTestNexusGroupData(dims1, Dataset.FLOAT64, true), null, "description1",
				"detectorID1", "detectorType1");

		Detector simpleFileDetector = TestHelpers.createTestFileDetector("simpleDetector1", 0, "HLK%05X.mp3",
				new int[] { 4, 4 }, "description1", "detectorID1", "detectorType2");

		Object[] args = new Object[] { simpleScannable1, 0., 10., 1., simpleScannable2, simpleDetector,
				simpleFileDetector, };
		ConcurrentScan scan = new ConcurrentScan(args);
		scan.setDataWriter(DefaultDataWriterFactory.createDataWriterFromFactory());
		scan.getDataWriter().addDataWriterExtender(dataWriterExtender);
		scan.runScan();
	}

	/**
	 * @throws Exception
	 *             if the test fails
	 */
	@Test
	public void testCreateXML() throws Exception {
		setUp("testCreateXML", true);

		Metadata m = mock(Metadata.class);
		when(m.getMetadataValue("visit")).thenReturn("nt20-17");
		when(m.getMetadataValue("instrument")).thenReturn("p45");
		IcatXMLCreator ixmlc = new IcatXMLCreator();
		ixmlc.setMetadata(m);
		FileRegistrar fr = new FileRegistrar();
		fr.setIcatXMLCreator(ixmlc);
		LocalProperties.set(LocalProperties.GDA_DATA_SCAN_DATAWRITER_DATAFORMAT, "DummyDataWriter");
		runScanToCreateFile(fr);
		// FIXME this should actually test something eventually
	}
}