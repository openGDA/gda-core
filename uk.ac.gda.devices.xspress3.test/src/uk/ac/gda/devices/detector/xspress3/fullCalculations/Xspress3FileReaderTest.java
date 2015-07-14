package uk.ac.gda.devices.detector.xspress3.fullCalculations;

import org.eclipse.dawnsci.hdf5.nexus.NexusException;
import org.junit.Test;

import gda.data.nexus.extractor.NexusExtractorException;

public class Xspress3FileReaderTest {

	@Test
	public void testFileUnpackedCorrectly() throws NexusException, NexusExtractorException {
		String nexusFile = Xspress3FileReaderTest.class.getResource("46594_0003.hdf5").getPath();
		Xspress3FileReader reader = new Xspress3FileReader(nexusFile, 10, 4096);
		reader.readFile();
		double[][] data = reader.getFrame(0);
		org.junit.Assert.assertTrue(data.length == 10);
		org.junit.Assert.assertTrue(data[0].length == 4096);
	}

	@Test
	public void testSingleFrameRead() throws NexusException, NexusExtractorException {
		String nexusFile = Xspress3FileReaderTest.class.getResource("46594_0003.hdf5").getPath();
		Xspress3FileReader reader = new Xspress3FileReader(nexusFile, 10, 4096);
		reader.readFile();
		double[][] data = reader.getFrame(9);
		org.junit.Assert.assertTrue(data.length == 10);
		org.junit.Assert.assertTrue(data[0].length == 4096);
	}
}
