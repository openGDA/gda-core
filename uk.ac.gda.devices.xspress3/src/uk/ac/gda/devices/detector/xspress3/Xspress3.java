package uk.ac.gda.devices.detector.xspress3;

import gda.data.nexus.tree.NexusTreeProvider;
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

	/**
	 * Perform a 'snapshot' data collection and return the MCAs. No file writing is involved.
	 *
	 * @param time
	 * @return
	 * @throws DeviceException
	 */
	@Deprecated
	public int[][] getMCData(double time) throws DeviceException;

	public Xspress3Controller getController();

	public double readoutFF() throws DeviceException;

	public NexusTreeProvider[] readFrames(int startFrame, int finalFrame) throws DeviceException;

	public void clearAndStart() throws DeviceException;

	public String getConfigFileName();

	public void setConfigFileName(String configFileName);

	public void loadConfigurationFromFile() throws Exception;
}
