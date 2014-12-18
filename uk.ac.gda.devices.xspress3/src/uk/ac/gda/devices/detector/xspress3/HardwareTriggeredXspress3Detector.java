package uk.ac.gda.devices.detector.xspress3;

import gda.device.ContinuousParameters;
import gda.device.DeviceException;
import gda.device.detector.hardwaretriggerable.HardwareTriggerableDetectorBase;
import gda.device.detector.hardwaretriggerable.HardwareTriggeredDetector;

/**
 * A delegate for the Xspress3SystemBufferedDetector class so that it can
 * operate in AbstractContinuousScanLine (trajectory) scans instead of the
 * ContinuousScans which Xspress3SystemBufferedDetector runs in.
 * 
 * @author rjw82
 * 
 */
public class HardwareTriggeredXspress3Detector extends HardwareTriggerableDetectorBase implements
		HardwareTriggeredDetector {

	private Xspress3BufferedDetector xspress3;

	public Xspress3BufferedDetector getXspress3() {
		return xspress3;
	}

	public void setXspress3(Xspress3BufferedDetector xspress3) {
		this.xspress3 = xspress3;
	}

	@Override
	public void setNumberImagesToCollect(int numberImagesToCollect) {
		try {
			ContinuousParameters newParameters = new ContinuousParameters();
			newParameters.setNumberDataPoints(numberImagesToCollect); // the only attribute in this bean used by 
			xspress3.setContinuousParameters(newParameters);
		} catch (DeviceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	@Override
	public void collectData() throws DeviceException {
		xspress3.collectData();
	}

	@Override
	public void atScanStart() throws DeviceException {
		xspress3.atScanStart();
	}

	@Override
	public void atScanLineStart() throws DeviceException {
		xspress3.setContinuousMode(true);
//		xspress3.atScanStart();
		xspress3.atScanLineStart();
	}

	@Override
	public void atScanLineEnd() throws DeviceException {
		xspress3.atScanLineEnd();
	}

	@Override
	public void atScanEnd() throws DeviceException {
		xspress3.atScanEnd();
	}

	@Override
	public void stop() throws DeviceException {
		xspress3.stop();
	}

	@Override
	public int getStatus() throws DeviceException {
		return xspress3.getStatus();
	}

	@Override
	public Object readout() throws DeviceException {
		return xspress3.readout();
	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		return xspress3.createsOwnFiles();
	}

	@Override
	public String[] getInputNames() {
		return xspress3.getInputNames();
	}

	@Override
	public String[] getOutputFormat() {
		return xspress3.getOutputFormat();
	}

	@Override
	public void update(Object source, Object arg) {
		//
	}

	@Override
	public boolean integratesBetweenPoints() {
		return true;
	}

	public double getCollectionTime() throws DeviceException {
		return xspress3.getCollectionTime();
	}

	public int getProtectionLevel() throws DeviceException {
		return xspress3.getProtectionLevel();
	}

	public void endCollection() throws DeviceException {
		xspress3.endCollection();
	}

	public void prepareForCollection() throws DeviceException {
		xspress3.prepareForCollection();
	}

	public void close() throws DeviceException {
		xspress3.close();
	}

	public boolean isBusy() throws DeviceException {
		return xspress3.isBusy();
	}

	public String getDescription() throws DeviceException {
		return xspress3.getDescription();
	}

	public String getDetectorID() throws DeviceException {
		return xspress3.getDetectorID();
	}

	public String getDetectorType() throws DeviceException {
		return xspress3.getDetectorType();
	}

	public String[] getExtraNames() {
		return xspress3.getExtraNames();
	}

	public void clearAndStart() throws DeviceException {
		xspress3.clearAndStart();
	}

	public void setLevel(int level) {
		xspress3.setLevel(level);
	}

	public void waitWhileBusy() throws DeviceException, InterruptedException {
		xspress3.waitWhileBusy();
	}

	public String getConfigFileName() {
		return xspress3.getConfigFileName();
	}

	public void clearMemory() throws DeviceException {
		xspress3.clearMemory();
	}

	public Xspress3 getXspress3Detector() {
		return xspress3.getXspress3Detector();
	}

	public void atPointEnd() throws DeviceException {
		xspress3.atPointEnd();
	}

	public void atPointStart() throws DeviceException {
		xspress3.atPointStart();
	}

	public void atLevelStart() throws DeviceException {
		xspress3.atLevelStart();
	}

	public void atLevelMoveStart() throws DeviceException {
		xspress3.atLevelMoveStart();
	}

	public void atLevelEnd() throws DeviceException {
		xspress3.atLevelEnd();
	}

	public void atCommandFailure() throws DeviceException {
		xspress3.atCommandFailure();
	}

	public void waitWhileBusy(double timeoutInSeconds) throws DeviceException, InterruptedException {
		xspress3.waitWhileBusy(timeoutInSeconds);
	}
	
}
