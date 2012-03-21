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

package gda.data.scan.datawriter;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import gda.configuration.properties.LocalProperties;
import gda.device.Detector;
import gda.device.Scannable;
import gda.device.detector.DummyDetector;
import gda.device.scannable.DummyScannable;
import gda.device.scannable.ScannableUtils;
import gda.jython.InterfaceProvider;
import gda.jython.MockJythonServer;
import gda.jython.MockJythonServerFacade;
import gda.scan.ScanDataPoint;
import gda.util.TestsUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Vector;

import org.junit.Test;
import org.junit.Ignore;

/**
 * Test the configurable format of this data writer. This uses DummyScannable and DummyDetector so indirectly tests
 * those.
 */
public class AsciiDataWriterTest {

	/**
	 * 
	 */
	@Test
	@Ignore("2010/01/20 Test ignored since not passing in Hudson")
	public void testConfiguration() {

		// build the configuration object
		ArrayList<AsciiWriterExtenderConfig> columns = new ArrayList<AsciiWriterExtenderConfig>();
		AsciiWriterExtenderConfig col1 = new AsciiWriterExtenderConfig();
		col1.expression = "energy";
		col1.format = "7.2f";
		col1.label = "energy";
		columns.add(col1);
		AsciiWriterExtenderConfig col2 = new AsciiWriterExtenderConfig();
		col2.expression = "counts";
		col2.format = "10.2f";
		col2.label = "counts";
		columns.add(col2);

		ArrayList<AsciiMetadataConfig> header = new ArrayList<AsciiMetadataConfig>();
		AsciiMetadataConfig header1 = new AsciiMetadataConfig();
		header1.label = "Diamond Light Source";
		header.add(header1);
		AsciiMetadataConfig header2 = new AsciiMetadataConfig();
		header2.label = "Beamline I01";
		header.add(header2);
		AsciiMetadataConfig header4 = new AsciiMetadataConfig();
		header4.label = "Beam current - start: %5.2f";
		header4.labelValues = new Scannable[] { new DummyScannable("beamcurrent", 298.9) };
		header.add(header4);
		AsciiMetadataConfig header5 = new AsciiMetadataConfig();
		header5.label = "";
		header.add(header5);

		ArrayList<AsciiMetadataConfig> footer = new ArrayList<AsciiMetadataConfig>();
		AsciiMetadataConfig footer1 = new AsciiMetadataConfig();
		footer1.label = "Beam current - end: %5.2f";
		footer1.labelValues = new Scannable[] { new DummyScannable("beamcurrent", 300.0) };
		footer.add(footer1);

		AsciiDataWriterConfiguration config = new AsciiDataWriterConfiguration();
		config.setColumns(columns);
		config.setHeader(header);
		config.setFooter(footer);

		try {
			// create a datawriter
			String testDir = TestsUtil.constructTestPath("", AsciiDataWriter.class);
			System.setProperty("gda.testDir", testDir);
			LocalProperties.set("gda.data.scan.datawriter.datadir", testDir);
			LocalProperties.set("gda.data.scan.datawriter.dataFormat", "AsciiNexusDataWriter");
			MockJythonServer mockServer = new MockJythonServer();
			MockJythonServerFacade mockFacade = new MockJythonServerFacade();
			InterfaceProvider.setCurrentScanInformationHolderForTesting(mockServer);
			InterfaceProvider.setDefaultScannableProviderForTesting(mockServer);
			InterfaceProvider.setCommandRunnerForTesting(mockFacade);
			InterfaceProvider.setTerminalPrinterForTesting(mockFacade);
			InterfaceProvider.setScanStatusHolderForTesting(mockFacade);
			InterfaceProvider.setJythonNamespaceForTesting(mockFacade);
			InterfaceProvider.setJythonServerNotiferForTesting(mockServer);

			AsciiDataWriter writer = new AsciiDataWriter(config);

			// create some scandatapoints and give them to the datawriter
			Vector<Scannable> scannables = new Vector<Scannable>();
			Scannable scannable1 = new DummyScannable("energy", 1000.0);
			scannables.add(scannable1);
			Vector<Detector> detectors = new Vector<Detector>();
			Detector det1 = new DummyDetector("counts", new int[] { 1 });
			detectors.add(det1);

			for (double i = 1000; i <= 1100; i += 100) {
				scannable1.moveTo(i);
				det1.collectData();
				ScanDataPoint point = new ScanDataPoint();
				point.setScanIdentifier("testscan");
				// do the getPosition/readout here as work should not be done inside the SDP.
				// This should be the only place these methods are called in the scan.
				for (Scannable scannable : scannables){
					point.addScannable(scannable);
					point.addScannablePosition(scannable.getPosition(),scannable.getOutputFormat());
				}
				for (Detector scannable : detectors){
					point.addDetector(scannable);
					point.addDetectorData(scannable.readout(),ScannableUtils.getExtraNamesFormats(scannable));
				}

				point.setCurrentFilename(testDir + "1.dat");
				point.setCurrentPointNumber((int) i);
				point.setNumberOfPoints(11);
				point.setInstrument("I01");
				point.setCommand("scan energy 1000 1100 100 counts 1");
				point.setScanIdentifier("testscanid");
				try {
					writer.addData(point);
				} catch (Exception e) {
					File file = new File(writer.fileUrl);
					if (file.exists()) {
						file.delete();
					}
					writer.addData(point);
				}
			}

			// test the file written
			writer.completeCollection();
			FileReader file = new FileReader(writer.fileUrl);
			FileReader cfile = new FileReader(this.getClass().getResource("0.dat").getFile());

			String contents = "";
			String contents1 = "";
			String z, y;
			BufferedReader br = new BufferedReader(file);
			BufferedReader br1 = new BufferedReader(cfile);

			while ((z = br1.readLine()) != null) {
				contents += z;
			}

			while ((y = br.readLine()) != null) {
				contents1 += y;
			}

			assertTrue(contents.equals(contents1));

		} catch (Exception e) {
			fail(e.getMessage());
		}

	}
}
