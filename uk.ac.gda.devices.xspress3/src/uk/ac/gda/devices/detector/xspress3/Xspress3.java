package uk.ac.gda.devices.detector.xspress3;

import gda.device.DeviceException;
import gda.device.detector.NXDetectorData;
import gda.device.detector.NexusDetector;
import uk.ac.gda.devices.detector.FluorescenceDetector;

/**
 * Interface for all classes representing the Xspress3 electronics.
 * 
 * @author rjw82
 * 
 */
public interface Xspress3 extends FluorescenceDetector, NexusDetector {

	public Xspress3Controller getController();

	public double readoutFF() throws DeviceException;

	public NXDetectorData[] readFrames(int startFrame, int finalFrame) throws DeviceException;

	public void clearAndStart() throws DeviceException;

}
