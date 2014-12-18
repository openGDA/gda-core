package uk.ac.gda.devices.detector.xspress3;

import gda.device.DeviceException;
import gda.device.detector.NexusDetector;
import uk.ac.gda.devices.detector.FluorescenceDetector;

/**
 * Interface for all classes representing the Xspress3 electronics.
 * 
 * @author rjw82
 * 
 */
public interface Xspress3 extends FluorescenceDetector, NexusDetector {

	Xspress3Controller getController();

	// TODO review how this is used with the Xspress3FFoverI0Detector
	Double[] readoutFF() throws DeviceException;

}
