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

import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import gda.TestHelpers;
import gda.configuration.properties.LocalProperties;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.detector.DummyDetector;
import gda.device.scannable.DummyScannable;
import gda.device.scannable.ScannableUtils;
import gda.device.scannable.scannablegroup.ScannableGroup;
import gda.factory.FactoryException;
import gda.scan.ScanDataPoint;

/**
 * Test the configurable format of this data writer. This uses DummyScannable and DummyDetector so indirectly tests
 * those.
 */
public class AsciiDataWriterTest {
	private ArrayList<AsciiWriterExtenderConfig> columns;
	private ArrayList<AsciiMetadataConfig> header;
	private AsciiDataWriterConfiguration config;

	@Before
	public void setup() {
		// build the configuration object
		columns = new ArrayList<AsciiWriterExtenderConfig>();
		AsciiWriterExtenderConfig col1 = new AsciiWriterExtenderConfig();
		col1.setExpression("energy");
		col1.setFormat("7.2f");
		col1.setLabel("energy");
		columns.add(col1);
		AsciiWriterExtenderConfig col2 = new AsciiWriterExtenderConfig();
		col2.setExpression("counts");
		col2.setFormat("10.2f");
		col2.setLabel("counts");
		columns.add(col2);

		header = new ArrayList<AsciiMetadataConfig>();
		AsciiMetadataConfig header1 = new AsciiMetadataConfig();
		header1.setLabel("Diamond Light Source");
		header.add(header1);
		AsciiMetadataConfig header2 = new AsciiMetadataConfig();
		header2.setLabel("Beamline I01");
		header.add(header2);
		AsciiMetadataConfig header4 = new AsciiMetadataConfig();
		header4.setLabel("Beam current - start: %5.2f");
		header4.setLabelValues(new Scannable[] {new DummyScannable("beamcurrent", 298.9)});
		header.add(header4);
		AsciiMetadataConfig header5 = new AsciiMetadataConfig();
		header5.setLabel("");
		header.add(header5);

		ArrayList<AsciiMetadataConfig> footer = new ArrayList<AsciiMetadataConfig>();
		AsciiMetadataConfig footer1 = new AsciiMetadataConfig();
		footer1.setLabel("Beam current - end: %5.2f");
		footer1.setLabelValues(new Scannable[] {new DummyScannable("beamcurrent", 300.0)});
		footer.add(footer1);

		config = new AsciiDataWriterConfiguration();
		config.setColumns(columns);
		config.setHeader(header);
		config.setFooter(footer);
	}

	@Test
	public void testConfiguration() {
		try {
			// create a datawriter
			String testDir = TestHelpers.setUpTest(AsciiDataWriterTest.class, "testConfiguration", true);
			System.setProperty("gda.testDir", testDir);
			LocalProperties.set(LocalProperties.GDA_DATAWRITER_DIR, testDir);
			LocalProperties.set(LocalProperties.GDA_DATA_SCAN_DATAWRITER_DATAFORMAT, "AsciiNexusDataWriter");

			AsciiDataWriter writer = new AsciiDataWriter(config);

			// create some scandatapoints and give them to the datawriter
			Vector<Scannable> scannables = new Vector<Scannable>();
			Scannable scannable1 = new DummyScannable("energy", 1000.0);
			scannables.add(scannable1);
			Vector<Detector> detectors = new Vector<Detector>();
			DummyDetector det1 = new DummyDetector("counts");
			det1.setRandomSeed(34L);
			detectors.add(det1);

			for (double i = 1000; i <= 1100; i += 100) {
				scannable1.moveTo(i);
				det1.collectData();
				ScanDataPoint point = new ScanDataPoint();
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
				point.setScanIdentifier(1000);
				try {
					writer.addData(point);
				} catch (Exception e) {
					File file = new File(writer.fileUrl);
					if (file.exists())
						file.delete();
					writer.addData(point);
				}
			}

			// test the file written
			writer.completeCollection();
			FileReader file = new FileReader(writer.getCurrentFileName());
			FileReader cfile = new FileReader("testfiles/gda/data/scan/datawriter/AsciiDataWriterTest/0.dat");

			String contents = "";
			String contents1 = "";
			String z, y;

			try (BufferedReader br = new BufferedReader(file); BufferedReader br1 = new BufferedReader(cfile);) {
				while ((z = br1.readLine()) != null)
					contents += z;
				while ((y = br.readLine()) != null)
					contents1 += y;
				Assert.assertEquals(contents, contents1);
			}
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testDataWriterColumnHeadings() {
		XasScanDataPointFormatter df = new XasScanDataPointFormatter();
		Map<String, String> XAS_SCAN_VARIABLES = new LinkedHashMap<>();
		XAS_SCAN_VARIABLES.put("Time", "Time");
		XAS_SCAN_VARIABLES.put("bragg1", "Energy");
		XAS_SCAN_VARIABLES.put("bragg1WithOffset", "Energy");
		XAS_SCAN_VARIABLES.put("FFI0", "FF/I0");
		XAS_SCAN_VARIABLES.put("FFI1", "FF/I1");

		String[] header = df.getHeader(XAS_SCAN_VARIABLES).split("\t");
		String firstHeader = header[0].trim();
		String lastHeader = header[header.length-1].trim();

		// Check that the first column is energy and the last column in Time
		Assert.assertTrue(firstHeader.equalsIgnoreCase("energy"));
		Assert.assertTrue(lastHeader.equalsIgnoreCase("Time"));
	}

	@Test
	public void testDataWriterDataColumns() {
		Map<String, String> dataMap = new LinkedHashMap<>();
		dataMap.put("energy", "10723.5");
		dataMap.put("mot1", "11.2");
		dataMap.put("Time", "today");
		dataMap.put("mot2", "12.3");

		XasScanDataPointFormatter df = new XasScanDataPointFormatter();
		String[] data = df.getData(dataMap).split("\t");
		String firstData = data[0].trim();
		String lastData = data[data.length-1].trim();

		// Check that the first column is energy and the last column in Time
		Assert.assertTrue(firstData.equalsIgnoreCase("10723.5"));
		Assert.assertTrue(lastData.equalsIgnoreCase("today"));

	}

	private Scannable[] getTestScannables() throws FactoryException {
		Scannable scn1 = new DummyScannable("beamcurrent", 298.9);
		Scannable scn2 = new DummyScannable("topup", 566.7);
		Scannable scn3 = new DummyScannable("diode", 12874);
		Scannable scn4 = new DummyScannable("sam1theta", 44.81);

		Scannable scnGroup = new ScannableGroup("group", Arrays.asList(scn1, scn2, scn3));
		scnGroup.configure();
		return new Scannable[] {scn1, scn2, scn3, scn4, scnGroup};
	}

	@Test
	public void testMetadataFromScannables() throws FactoryException, DeviceException {
		Scannable[] testScannables = getTestScannables();

		String stringFormat = "Test values : topup = %.4f, diode readout = %.4f";
		AsciiMetadataConfig testMetaConfig = new AsciiMetadataConfig();
		testMetaConfig.setLabel(stringFormat);
		testMetaConfig.setLabelValues(new Scannable[] {testScannables[1], testScannables[2]});
		String result = testMetaConfig.toString();
		Assert.assertEquals(testMetaConfig.getLabel().formatted(testScannables[1].getPosition(), testScannables[2].getPosition()), result);
	}

	@Test
	public void testMetadataFromScannableGroup() throws FactoryException, DeviceException {
		Scannable[] testScannables = getTestScannables();
		Scannable scnGroup = testScannables[4];
		String stringFormat = "Test values : beamcurrent = %.4f, topup = %.4f, diode readout = %.4f";
		AsciiMetadataConfig testMetaConfig = new AsciiMetadataConfig();
		testMetaConfig.setLabel(stringFormat);
		testMetaConfig.setLabelValues(new Scannable[] {scnGroup});
		String result = testMetaConfig.toString();
		Assert.assertEquals(testMetaConfig.getLabel().formatted(testScannables[0].getPosition(), testScannables[1].getPosition(), testScannables[2].getPosition()), result);
	}

	@Test
	public void testMetadataFromScannableMixture() throws FactoryException, DeviceException {
		Scannable[] testScannables = getTestScannables();
		Scannable scnGroup = testScannables[4];
		String stringFormat = "Test values : rotation = %.4f, beamcurrent = %.4f, topup = %.4f, diode readout = %.4f";
		AsciiMetadataConfig testMetaConfig = new AsciiMetadataConfig();
		testMetaConfig.setLabel(stringFormat);
		testMetaConfig.setLabelValues(new Scannable[] {testScannables[3], scnGroup});
		String result = testMetaConfig.toString();
		Assert.assertEquals(testMetaConfig.getLabel().formatted(testScannables[3].getPosition(), testScannables[0].getPosition(), testScannables[1].getPosition(), testScannables[2].getPosition()), result);
	}
}
