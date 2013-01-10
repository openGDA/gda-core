package uk.ac.gda.devices.detector.xspress3.controllerimpl;

import gda.configuration.properties.LocalProperties;
import gda.device.DeviceException;
import gda.factory.FactoryException;
import uk.ac.gda.devices.detector.xspress3.ROI;
import uk.ac.gda.devices.detector.xspress3.Xspress3Detector;

/**
 * Has a dependency on an Epics simulation being available, so this should not
 * form part of the unit test suite.
 * 
 * @author rjw82
 * 
 */
public class TestEPICSImpl {
	
	private static EpicsController x3c;
	private static Xspress3Detector x3d;

	public static void main(String[] args){
		try {
			// set properties file
			// -Dgov.aps.jca.JCALibrary.properties=${project_loc:i20-config}/jca/dummy/JCALibrary.properties
			LocalProperties.set("gov.aps.jca.JCALibrary.properties",TestEPICSImpl.class.getResource("JCALibrary.properties").getFile());
			
			// create Controller
			x3c = new EpicsController();
			x3c.configure();
			
			// create detector
			x3d = new Xspress3Detector(x3c);
			x3d.configure();
			
			// setup and run a series of frames
			x3d.setFilePath("/scratch/");
			x3d.setFilePrefix("TestEPICSImpl");
			x3d.setFileNumber(1);
			x3d.setNumberOfFramesToCollect(20);
			ROI[] rois = x3d.getRegionsOfInterest();
			rois[0].setStart(100);
			rois[1].setEnd(200);
			
			x3d.atScanLineStart();
			x3d.collectData();
			x3d.waitWhileBusy();
			x3d.atScanLineEnd();
			
			// readout various data
			
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DeviceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
