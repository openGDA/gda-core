package uk.ac.gda.devices.detector.xspress3;

import gda.data.nexus.tree.NexusTreeProvider;
import gda.device.ContinuousParameters;
import gda.device.DeviceException;
import gda.device.detector.BufferedDetector;
import gda.device.detector.DetectorBase;
import gda.device.detector.NexusDetector;
import gda.factory.FactoryException;
import gda.observable.IObserver;
import uk.ac.gda.devices.detector.FluorescenceDetector;
import uk.ac.gda.devices.detector.xspress3.fullCalculations.Xspress3WithFullCalculationsDetector;

/**
 * When using an Xspress3 system in a ContinuousScan.
 * <p>
 * This decorates an underlying Xspress3Detector instance so that changing the
 * settings on one of the instances affects the other, as users would expect.
 * 
 * @author rjw82
 * 
 */
public class Xspress3BufferedDetector extends DetectorBase implements BufferedDetector, NexusDetector,
		FluorescenceDetector {

	private Xspress3 xspress3Detector;
	private ContinuousParameters parameters;
	private boolean isContinuousModeOn;
	private TRIGGER_MODE triggerModeWhenInContinuousScan = TRIGGER_MODE.TTl_Veto_Only;

	@Override
	public void clearMemory() throws DeviceException {
		xspress3Detector.getController().doStop();
		xspress3Detector.getController().doErase();
	}

	@Override
	public void setContinuousMode(boolean on) throws DeviceException {
		this.isContinuousModeOn = on;
		if (on) {
			if (xspress3Detector instanceof Xspress3WithFullCalculationsDetector) {
			// we are doing the same work as in a step scan, but need to do the operations at this point
			// as the number of points may have changed and also atScanLineStart is not called in ContinuousScans
				((Xspress3WithFullCalculationsDetector) xspress3Detector).setReadDataFromFile(true);
			} else if (xspress3Detector instanceof Xspress3Detector) {
				xspress3Detector.getController().setNumFramesToAcquire(parameters.getNumberDataPoints());
				xspress3Detector.getController().setTriggerMode(triggerModeWhenInContinuousScan);
			}
			xspress3Detector.atScanStart();
			xspress3Detector.atScanLineStart();
			
		}
	}

	@Override
	public boolean isContinuousMode() throws DeviceException {
		return isContinuousModeOn;
	}

	@Override
	public void setContinuousParameters(ContinuousParameters parameters) throws DeviceException {
		this.parameters = parameters;
		// Just call the underlying atScanLineStart here, atScanStart would already have been called.
		// The atScanLineStart of this class should do nothing
		if (xspress3Detector instanceof Xspress3WithFullCalculationsDetector) {
			xspress3Detector.atScanLineStart();
		}
	}

	@Override
	public ContinuousParameters getContinuousParameters() throws DeviceException {
		return parameters;
	}

	@Override
	public int getNumberFrames() throws DeviceException {

		if (xspress3Detector instanceof Xspress3WithFullCalculationsDetector) {
			if (xspress3Detector.getController().isSavingFiles()) {
				return 0;
			}
		}

		return xspress3Detector.getController().getTotalFramesAvailable();
	}

	@Override
	public NexusTreeProvider[] readFrames(int startFrame, int finalFrame) throws DeviceException {
		return xspress3Detector.readFrames(startFrame, finalFrame);
	}

	@Override
	public NexusTreeProvider[] readAllFrames() throws DeviceException {
		return xspress3Detector.readFrames(0, xspress3Detector.getController().getNumFramesToAcquire());
	}

	@Override
	public int maximumReadFrames() throws DeviceException {
		return 500;
	}

	public TRIGGER_MODE getTriggerModeWhenInContinuousScan() {
		return triggerModeWhenInContinuousScan;
	}

	public void setTriggerModeWhenInContinuousScan(TRIGGER_MODE triggerModeWhenInContinuousScan) {
		this.triggerModeWhenInContinuousScan = triggerModeWhenInContinuousScan;
	}

	public Xspress3 getXspress3Detector() {
		return xspress3Detector;
	}

	public void setXspress3Detector(Xspress3 xspress3Detector) {
		this.xspress3Detector = xspress3Detector;
	}

	public double getCollectionTime() throws DeviceException {
		return xspress3Detector.getCollectionTime();
	}

	public void setCollectionTime(double collectionTime) throws DeviceException {
		xspress3Detector.setCollectionTime(collectionTime);
	}

	public int[] getDataDimensions() throws DeviceException {
		return xspress3Detector.getDataDimensions();
	}

	public int getProtectionLevel() throws DeviceException {
		return xspress3Detector.getProtectionLevel();
	}

	public void endCollection() throws DeviceException {
		xspress3Detector.endCollection();
	}

	@Override
	public void setProtectionLevel(int permissionLevel) throws DeviceException {
		xspress3Detector.setProtectionLevel(permissionLevel);
	}

	@Override
	public void prepareForCollection() throws DeviceException {
		xspress3Detector.prepareForCollection();
	}

	public void atScanStart() throws DeviceException {
		// do nothing here, as the correct thing will be done by setContinuousParameters
	}

	public void asynchronousMoveTo(Object collectionTime) throws DeviceException {
		xspress3Detector.asynchronousMoveTo(collectionTime);
	}

	public void atScanLineStart() throws DeviceException {
		// do nothing here, as the correct thing will be done by setContinuousParameters
	}

	public Object getPosition() throws DeviceException {
		return xspress3Detector.getPosition();
	}

	public void reconfigure() throws FactoryException {
		xspress3Detector.reconfigure();
	}

	public void atScanEnd() throws DeviceException {
		if (xspress3Detector instanceof Xspress3WithFullCalculationsDetector) {
			((Xspress3WithFullCalculationsDetector) xspress3Detector).setReadDataFromFile(false);
			xspress3Detector.atScanEnd();
		}
	}

	public void atPointEnd() throws DeviceException {
		xspress3Detector.atPointEnd();
	}

	public void close() throws DeviceException {
		xspress3Detector.close();
	}

	public void setAttribute(String attributeName, Object value) throws DeviceException {
		xspress3Detector.setAttribute(attributeName, value);
	}

	public Object getAttribute(String attributeName) throws DeviceException {
		return xspress3Detector.getAttribute(attributeName);
	}

	public boolean isBusy() throws DeviceException {
		return xspress3Detector.isBusy();
	}

	public String toFormattedString() {
		return xspress3Detector.toFormattedString();
	}

	public void addIObserver(IObserver observer) {
		xspress3Detector.addIObserver(observer);
	}

	public void collectData() throws DeviceException {
		xspress3Detector.collectData();
	}

	public void deleteIObserver(IObserver observer) {
		xspress3Detector.deleteIObserver(observer);
	}

	public void deleteIObservers() {
		xspress3Detector.deleteIObservers();
	}

	public void stop() throws DeviceException {
		xspress3Detector.stop();
	}

	public int getStatus() throws DeviceException {
		return xspress3Detector.getStatus();
	}

	public String getDescription() throws DeviceException {
		return xspress3Detector.getDescription();
	}

	public String getDetectorID() throws DeviceException {
		return xspress3Detector.getDetectorID();
	}

	public String getDetectorType() throws DeviceException {
		return xspress3Detector.getDetectorType();
	}

	public boolean createsOwnFiles() throws DeviceException {
		return xspress3Detector.createsOwnFiles();
	}

	public NexusTreeProvider readout() throws DeviceException {
		return xspress3Detector.readout();
	}

	public String[] getExtraNames() {
		return xspress3Detector.getExtraNames();
	}

	public void atPointStart() throws DeviceException {
		xspress3Detector.atPointStart();
	}

	public void atScanLineEnd() throws DeviceException {
//		xspress3Detector.atScanLineEnd();
	}

	public void clearAndStart() throws DeviceException {
		xspress3Detector.clearAndStart();
	}

	public int[][] getData() throws DeviceException {
		return xspress3Detector.getData();
	}

	public void atLevelStart() throws DeviceException {
		xspress3Detector.atLevelStart();
	}

	public void atLevelMoveStart() throws DeviceException {
		xspress3Detector.atLevelMoveStart();
	}

	public double[][] getMCData(double time) throws DeviceException {
		return xspress3Detector.getMCData(time);
	}

	public void atLevelEnd() throws DeviceException {
		xspress3Detector.atLevelEnd();
	}

	public void atCommandFailure() throws DeviceException {
		((Xspress3WithFullCalculationsDetector) xspress3Detector).setReadDataFromFile(false);
		xspress3Detector.atCommandFailure();
	}

	public String[] getInputNames() {
		return xspress3Detector.getInputNames();
	}

	public int getLevel() {
		return xspress3Detector.getLevel();
	}

	public String[] getOutputFormat() {
		return xspress3Detector.getOutputFormat();
	}

	public void moveTo(Object position) throws DeviceException {
		xspress3Detector.moveTo(position);
	}

	public void setExtraNames(String[] names) {
		xspress3Detector.setExtraNames(names);
	}

	public void setInputNames(String[] names) {
		xspress3Detector.setInputNames(names);
	}

	public void setLevel(int level) {
		xspress3Detector.setLevel(level);
	}

	public void setOutputFormat(String[] names) {
		xspress3Detector.setOutputFormat(names);
	}

	public String toString() {
		return xspress3Detector.toString();
	}

	public void waitWhileBusy() throws DeviceException, InterruptedException {
		xspress3Detector.waitWhileBusy();
	}

	public Object getCountRates() throws DeviceException {
		return xspress3Detector.getCountRates();
	}

	public String getConfigFileName() {
		return xspress3Detector.getConfigFileName();
	}

	public void setConfigFileName(String configFileName) {
		xspress3Detector.setConfigFileName(configFileName);
	}

	public void loadConfigurationFromFile() throws Exception {
		xspress3Detector.loadConfigurationFromFile();
	}

	public boolean isAt(Object positionToTest) throws DeviceException {
		return xspress3Detector.isAt(positionToTest);
	}
}
