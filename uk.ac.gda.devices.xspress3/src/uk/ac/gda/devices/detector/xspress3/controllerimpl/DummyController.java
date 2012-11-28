package uk.ac.gda.devices.detector.xspress3.controllerimpl;

import gda.device.DeviceException;
import gda.factory.FactoryException;
import uk.ac.gda.devices.detector.xspress3.TRIGGER_MODE;
import uk.ac.gda.devices.detector.xspress3.Xspress3Controller;

/**
 * For unit testing / simulation when EPICS sim not available.
 * 
 * @author rjw82
 *
 */
public class DummyController implements Xspress3Controller {

	@Override
	public void doStart() throws DeviceException {
		// TODO Auto-generated method stub

	}

	@Override
	public void doStop() throws DeviceException {
		// TODO Auto-generated method stub

	}

	@Override
	public void doErase() throws DeviceException {
		// TODO Auto-generated method stub

	}

	@Override
	public void doReset() throws DeviceException {
		// TODO Auto-generated method stub

	}

	@Override
	public Integer getNumFramesToAcquire() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setNumFramesToAcquire(Integer numFrames) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setTriggerMode(TRIGGER_MODE mode) {
		// TODO Auto-generated method stub

	}

	@Override
	public TRIGGER_MODE getTriggerMode() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean isBusy() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean isConnected() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getStatusMessage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getStatus() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getTotalFramesAvailable() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Integer getMaxNumberFrames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Double[][] readoutDTCorrectedSCA1(int startFrame, int finalFrame,
			int startChannel, int finalChannel) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Double[][] readoutDTCorrectedSCA2(int startFrame, int finalFrame,
			int startChannel, int finalChannel) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer[][][] readoutScalerValues(int startFrame, int finalFrame,
			int startChannel, int finalChannel) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer[][] readoutDTCParameters(int startChannel, int finalChannel) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Double[][][] readoutDTCorrectedROI(int startFrame, int finalFrame,
			int startChannel, int finalChannel) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Double[][] readoutDTCorrectedLatestMCA(int startChannel,
			int finalChannel) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setROILimits(int channel, int roiNumber,
			int[] lowHighMCAChannels) {
		// TODO Auto-generated method stub

	}

	@Override
	public Integer[] getROILimits(int channel, int roiNumber) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setWindows(int channel, int roiNumber,
			int[] lowHighScalerWindowChannels) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Integer[] getWindows(int channel, int roiNumber) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setFilePath(String path) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setFileTemplate(String template) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getFilePath() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getFileTemplate() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void configure() throws FactoryException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getNumberROIToRead() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setNumberROIToRead(int numRoiToRead)
			throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return;
	}
}
