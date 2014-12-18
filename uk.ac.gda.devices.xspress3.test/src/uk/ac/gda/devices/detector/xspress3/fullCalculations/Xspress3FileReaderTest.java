package uk.ac.gda.devices.detector.xspress3.fullCalculations;

import gda.data.nexus.extractor.NexusExtractorException;

import org.junit.Test;
import org.nexusformat.NexusException;

public class Xspress3FileReaderTest {

	@Test
	public void testFileUnpackedCorrectly() throws NexusException, NexusExtractorException {
		String nexusFile = Xspress3FileReaderTest.class.getResource("46594_0003.hdf5").getPath();
		Xspress3FileReader reader = new Xspress3FileReader(nexusFile, 10, 4096);
		double[][][] data = reader.readFrames(0, 40);
		org.junit.Assert.assertTrue(data.length == 41);
		org.junit.Assert.assertTrue(data[0].length == 10);
		org.junit.Assert.assertTrue(data[0][0].length == 4096);
	}

	@Test
	public void testSingleFrameRead() throws NexusException, NexusExtractorException {
		String nexusFile = Xspress3FileReaderTest.class.getResource("46594_0003.hdf5").getPath();
		Xspress3FileReader reader = new Xspress3FileReader(nexusFile, 10, 4096);
		double[][][] data = reader.readFrames(9, 9);
		org.junit.Assert.assertTrue(data.length == 1);
		org.junit.Assert.assertTrue(data[0].length == 10);
		org.junit.Assert.assertTrue(data[0][0].length == 4096);
	}

	@Test
	public void testMultipleFrameRead() throws NexusException, NexusExtractorException {
		String nexusFile = Xspress3FileReaderTest.class.getResource("46594_0003.hdf5").getPath();
		Xspress3FileReader reader = new Xspress3FileReader(nexusFile, 10, 4096);
		double[][][] data = reader.readFrames(5, 7);
		org.junit.Assert.assertTrue(data.length == 3);
		org.junit.Assert.assertTrue(data[0].length == 10);
		org.junit.Assert.assertTrue(data[0][0].length == 4096);
	}
}
