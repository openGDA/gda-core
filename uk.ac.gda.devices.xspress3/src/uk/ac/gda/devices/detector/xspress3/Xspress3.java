package uk.ac.gda.devices.detector.xspress3;

import gda.device.DeviceException;
import gda.device.detector.NXDetectorData;
import gda.device.detector.NexusDetector;
import uk.ac.gda.beans.exafs.DetectorROI;
import uk.ac.gda.devices.detector.DetectorWithConfigurationFile;
import uk.ac.gda.devices.detector.FluorescenceDetector;

/**
 * Interface for all classes representing the Xspress3 electronics.
 *
 * @author rjw82
 *
 */
public interface Xspress3 extends FluorescenceDetector, NexusDetector, DetectorWithConfigurationFile {

	/**
	 * Perform a 'snapshot' data collection and return the MCAs. No file writing is involved.
	 */
	@Deprecated(since="GDA 8.48")
	int[][] getMCData(double time) throws DeviceException;

	Xspress3Controller getController();

	double readoutFF() throws DeviceException;

	NXDetectorData[] readFrames(int startFrame, int finalFrame) throws DeviceException;

	void clearAndStart() throws DeviceException;

	void loadConfigurationFromFile() throws Exception;

	/** Return ROIs for a channel (detector element). **/
	DetectorROI[] getRegionsOfInterest(int channel) throws DeviceException;

	/** Set ROIs for all channels (i.e. same ROIs for all detector elements) */
	void setRegionsOfInterest(DetectorROI[] regionList) throws DeviceException;
}
