package gda.device.detector.xspress.xspress2data;

import gda.device.DeviceException;
import gda.factory.FactoryException;
import uk.ac.gda.beans.vortex.DetectorElement;

public interface Xspress2Controller {

	public abstract void configure() throws FactoryException;

	public abstract void configureDetectorFromParameters() throws DeviceException;

	public abstract void checkIsConnected();

	public abstract void start() throws DeviceException;

	public abstract void stop() throws DeviceException;

	public abstract void close() throws DeviceException;

	public abstract void clear() throws DeviceException;

	public abstract void reconfigure() throws FactoryException;

	public abstract void collectData() throws DeviceException;

	public abstract int getStatus() throws DeviceException;

	public abstract void setResolutionGrade(String resGrade) throws DeviceException;

	public abstract void setResolutionGrade(String resGrade, int numberOfBits) throws DeviceException;

	public abstract int[] runOneFrame(int time) throws DeviceException;

	public abstract void setFullMCABits(int fullMCABits) throws DeviceException;

	public abstract void doSetWindowsCommand(DetectorElement detector) throws DeviceException;
	
	public void doSetROICommand(DetectorElement detector) throws DeviceException;

	public abstract Double getI0();

	public abstract int getTotalFrames() throws NumberFormatException, DeviceException;

	/**
	 * Readout full mca for every detector element and specified time frame
	 * 
	 * @param startFrame
	 *            time frame to read
	 * @param numberOfFrames
	 * @return mca data
	 * @throws DeviceException
	 */
	public abstract int[] readoutMca(int startFrame, int numberOfFrames, int mcaSize) throws DeviceException;

	public abstract int[] readoutHardwareScalers(int startFrame, int numberOfFrames) throws DeviceException;

	public abstract int getNumberFrames() throws DeviceException;

	public abstract void setCurrentSettings(Xspress2CurrentSettings settings);

}