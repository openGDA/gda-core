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

package gda.device.scannable;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.TestHelpers;
import gda.configuration.properties.LocalProperties;
import gda.data.scan.datawriter.AsciiDataWriterConfiguration;
import gda.data.scan.datawriter.AsciiMetadataConfig;
import gda.factory.Factory;
import gda.factory.Finder;
import gda.scan.ConcurrentScan;
import gda.scan.Scan;

public class AsciiFileProcessorTest {

	 private static final Logger logger = LoggerFactory.getLogger(AsciiFileProcessorTest.class);

	private DummyScannable scannable1;
	private DummyScannable scannable2;
	private AsciiFileProcessor processor;

	@Before
	public void setup() throws Exception {
		TestHelpers.setUpTest(AsciiFileProcessorTest.class, "name", true);
		LocalProperties.set(LocalProperties.GDA_DATA_SCAN_DATAWRITER_DATAFORMAT, "AsciiDataWriter");
		LocalProperties.set(LocalProperties.GDA_BEAMLINE_NAME, "b18");
		LocalProperties.set("gda.nexus.createSRS", "true");

		scannable1 = new DummyScannable("PositionX");
		scannable2 = new DummyScannable("PositionY");
		scannable1.configure();
		scannable2.configure();

		scannable2.moveTo(0);
		scannable2.setIncrement(0.1);


		// Create some lines of metadata and for the datawriter config
		ArrayList<AsciiMetadataConfig> metaData  = new ArrayList<>();
		for(int i=0; i<10; i++) {
			AsciiMetadataConfig config = new AsciiMetadataConfig();
			config.setLabel("Line "+i);
			metaData.add(config);
		}

		AsciiDataWriterConfiguration dataWriterConfig = new AsciiDataWriterConfiguration();
		dataWriterConfig.setName("dataWriterConfig");
		dataWriterConfig.setHeader(metaData);

		final Factory factory = TestHelpers.createTestFactory();
		factory.addFindable(dataWriterConfig);
		Finder.getInstance().addFactory(factory);

		processor = new AsciiFileProcessor();
		processor.setName("processor");
	}

	@Test
	public void testProcessAfterScan() throws Exception {
		Scan scan = new ConcurrentScan(new Object[] {scannable1, 0, 10, 1, scannable2});
		scan.runScan();
		String filename = scan.getDataWriter().getCurrentFileName();
		logger.info("Scan file name : {}", filename);

		processor.setColumnNames(Arrays.asList("PositionY"));
		String newFile = processor.processFile(filename);
		checkFile(filename, newFile, Arrays.asList(1));

		processor.setColumnNames(Arrays.asList("PositionY", "PositionY"));
		newFile = processor.processFile(filename);
		checkFile(filename, newFile, Arrays.asList(1, 1));
	}

	@Test
	public void testProcessAtScanEnd() throws Exception {
		processor.setColumnNames(Arrays.asList("PositionY"));
		Scan scan = new ConcurrentScan(new Object[] {scannable1, 0, 10, 1, scannable2, processor});
		scan.runScan();
		String filename = scan.getDataWriter().getCurrentFileName();
		logger.info("Scan file name : {}", filename);

		String newFile = processor.getProcessedFileName();
		checkFile(filename, newFile, Arrays.asList(1));
	}


	private void checkFile(String source, String processed, List<Integer> indices) throws IOException {
		logger.info("Processed file name : {}", processed);
		List<String> sourceLines = Files.readAllLines(Paths.get(source));
		List<String> processedLines = Files.readAllLines(Paths.get(processed));
		assertEquals("Incorrect number of lines in processed file", sourceLines.size(), processedLines.size()-1);

		checkHeader(sourceLines, processedLines);
		checkData(sourceLines, processedLines, indices);
	}

	private void checkData(List<String> source, List<String> processed, List<Integer> indices) {
		List<String[]> sourceData = getData(source);
		List<String[]> processedData = getData(processed);
		assertEquals("Number of rows of processed data is incorrect", sourceData.size(), processedData.size());

		for(int i=0; i<indices.size(); i++) {
			for(int j=0; j<sourceData.size(); j++) {
				assertEquals("Processed data in row "+i+" column "+j+" is wrong",
						sourceData.get(i)[indices.get(i)], processedData.get(i)[i]);
			}
		}
	}

	private List<String[]> getData(List<String> dataFromFile) {
		return dataFromFile.stream()
				.filter(line -> !line.contains(AsciiFileProcessor.COMMENT_CHAR))
				.map(str -> str.trim().split("\\s+"))
				.collect(Collectors.toList());
	}

	private void checkHeader(List<String> source, List<String> processed) {
		for(int i=0; i<source.size(); i++) {
			String sourceLine = source.get(i);
			if ( !sourceLine.startsWith("#") ||
				 sourceLine.contains(processor.getColumnNames().get(0))) {
				break;
			}
			assertEquals("Header line "+(i+1)+" in processed file is incorrect", sourceLine, processed.get(i+1));
		}
	}
}
