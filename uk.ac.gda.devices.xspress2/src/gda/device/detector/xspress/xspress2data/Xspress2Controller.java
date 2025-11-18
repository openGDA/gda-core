package gda.device.detector.xspress.xspress2data;

import gda.device.DeviceException;
import gda.factory.FactoryException;
import uk.ac.gda.beans.vortex.DetectorElement;

public interface Xspress2Controller {

	void configure() throws FactoryException;

	void configureDetectorFromParameters() throws DeviceException;

	void checkIsConnected();

	void start() throws DeviceException;

	void stop() throws DeviceException;

	void close() throws DeviceException;

	void clear() throws DeviceException;

	void reconfigure() throws FactoryException;

	void collectData() throws DeviceException;

	int getStatus() throws DeviceException;

	void setResolutionGrade(String resGrade) throws DeviceException;

	void setResolutionGrade(String resGrade, int numberOfBits) throws DeviceException;

	int[] runOneFrame(int time) throws DeviceException;

	void setFullMCABits(int fullMCABits) throws DeviceException;

	void doSetWindowsCommand(DetectorElement detector) throws DeviceException;

	void doSetROICommand(DetectorElement detector) throws DeviceException;

	Double getI0();

	int getTotalFrames() throws NumberFormatException, DeviceException;

	/**
	 * Readout full mca for every detector element and specified time frame
	 *
	 * @param startFrame
	 *            time frame to read
	 * @param numberOfFrames
	 * @return mca data
	 * @throws DeviceException
	 */
	int[] readoutMca(int startFrame, int numberOfFrames, int mcaSize) throws DeviceException;

	int[] readoutHardwareScalers(int startFrame, int numberOfFrames) throws DeviceException;

	int getNumberFrames() throws DeviceException;

	void setCurrentSettings(Xspress2CurrentSettings settings);
}