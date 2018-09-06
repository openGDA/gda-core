package uk.ac.gda.devices.detector.xspress3;

import gda.device.DeviceException;
import gda.device.detector.NXDetectorData;
import gda.device.detector.NexusDetector;
import uk.ac.gda.beans.DetectorROI;
import uk.ac.gda.devices.detector.FluorescenceDetector;

/**
 * Interface for all classes representing the Xspress3 electronics.
 *
 * @author rjw82
 *
 */
public interface Xspress3 extends FluorescenceDetector, NexusDetector {

	/**
	 * Perform a 'snapshot' data collection and return the MCAs. No file writing is involved.
	 */
	@Deprecated
	public int[][] getMCData(double time) throws DeviceException;

	public Xspress3Controller getController();

	public double readoutFF() throws DeviceException;

	public NXDetectorData[] readFrames(int startFrame, int finalFrame) throws DeviceException;

	public void clearAndStart() throws DeviceException;

	public String getConfigFileName();

	public void setConfigFileName(String configFileName);

	public void loadConfigurationFromFile() throws Exception;

	/** Return ROIs for a channel (detector element). **/
	public DetectorROI[] getRegionsOfInterest(int channel) throws DeviceException;

	/** Set ROIs for all channels (i.e. same ROIs for all detector elements) */
	public void setRegionsOfInterest(DetectorROI[] regionList) throws DeviceException;
}
