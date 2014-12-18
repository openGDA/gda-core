package uk.ac.gda.devices.detector.xspress3.fullCalculations;

import gda.device.detector.DummyDAServer;
import gda.device.detector.NXDetectorData;
import gda.device.timer.Tfg;

import java.net.URL;

import org.junit.Test;

import uk.ac.gda.devices.detector.xspress3.controllerimpl.DummyXspress3Controller;

public class Xspress3DataOperationsTest {

	@Test
	public void testNexusChunkSingleFrame() throws Exception {
		
		DummyDAServer daserver = new DummyDAServer();
		Tfg tfg = new Tfg();
		tfg.setDaServer(daserver);
		
		DummyXspress3Controller controller = new DummyXspress3Controller(tfg,daserver);
		controller.setNumFramesToAcquire(1);
		controller.setNumberOfChannels(10);
		
		Xspress3DataOperations dataOps = new Xspress3DataOperations(controller,"xspress3",0);
		String configFile = Xspress3DataOperationsTest.class.getResource("Xspress3_ParametersCu_K.xml").getPath();
		dataOps.setConfigFileName(configFile);
		dataOps.loadConfigurationFromFile();
		
		URL nexusFile = Xspress3FileReader.class.getResource("46594_0003.hdf5");
		controller.setSimulationFileName(nexusFile.getPath());
		
		NXDetectorData treeProvider = (NXDetectorData) dataOps.readoutFrames(1, 1)[0];
		
		org.junit.Assert.assertEquals(11,treeProvider.getExtraNames().length);
		org.junit.Assert.assertEquals(11,treeProvider.getOutputFormat().length);
		org.junit.Assert.assertEquals(11,treeProvider.getDoubleVals().length);
		
		org.junit.Assert.assertTrue(treeProvider.getNexusTree() != null);

		org.junit.Assert.assertEquals(4096,((double[]) treeProvider.getNexusTree().getNode("xspress3/AllElementSum").getData().getBuffer()).length);
		
		org.junit.Assert.assertEquals(10,((double[]) treeProvider.getNexusTree().getNode("xspress3/Cu").getData().getBuffer()).length);
		
		org.junit.Assert.assertEquals(30387.2217,(Double) treeProvider.getNexusTree().getNode("xspress3/FF").getData().getBuffer(),0.001);
		
	}
}
