package uk.ac.gda.devices.detector.xspress3;

import gda.data.nexus.tree.NexusTreeProvider;
import gda.device.ContinuousParameters;
import gda.device.DeviceException;
import gda.device.detector.BufferedDetector;
import gda.device.detector.DetectorBase;
import gda.device.detector.NexusDetector;
import gda.factory.FactoryException;
import gda.observable.IObserver;

import org.python.core.PyException;
import org.python.core.PyObject;
import org.python.core.PyString;

import uk.ac.gda.devices.detector.FluorescenceDetector;

/**
 * When using an Xspress3 system in a ContinuousScan.
 * <p>
 * This decorates an underlying Xspress3Detector instance so that changing the
 * settings on one of the instances affects the other, as users would expect.
 * 
 * @author rjw82
 * 
 */
public class Xspress3BufferedDetector extends DetectorBase implements BufferedDetector, NexusDetector, FluorescenceDetector {

	private Xspress3Detector xspress3Detector;
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
			xspress3Detector.getController().setNumFramesToAcquire(parameters.getNumberDataPoints());
			xspress3Detector.getController().setTriggerMode(triggerModeWhenInContinuousScan);
			// Epics needs us to clear memory again after setting trig mode and num frames
			clearMemory();
			xspress3Detector.getController().doStart();
		}
	}

	@Override
	public boolean isContinuousMode() throws DeviceException {
		return isContinuousModeOn;
	}

	@Override
	public void setContinuousParameters(ContinuousParameters parameters) throws DeviceException {
		this.parameters = parameters;
	}

	@Override
	public ContinuousParameters getContinuousParameters() throws DeviceException {
		return parameters;
	}

	@Override
	public int getNumberFrames() throws DeviceException {
		return xspress3Detector.getController().getTotalFramesAvailable();
	}

	@Override
	public Object[] readFrames(int startFrame, int finalFrame) throws DeviceException {
		return xspress3Detector.readoutFrames(startFrame, finalFrame);
	}

	@Override
	public Object[] readAllFrames() throws DeviceException {
		return xspress3Detector.readoutFrames(0, xspress3Detector.getController().getNumFramesToAcquire());
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

	public Xspress3Detector getXspress3Detector() {
		return xspress3Detector;
	}

	public void setXspress3Detector(Xspress3Detector xspress3Detector) {
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

	public String checkPositionValid(Object position) {
		return xspress3Detector.checkPositionValid(position);
	}

	public boolean isLocal() {
		return xspress3Detector.isLocal();
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
		xspress3Detector.atScanStart();
	}

	public void setLocal(boolean local) {
		xspress3Detector.setLocal(local);
	}

	public void asynchronousMoveTo(Object collectionTime) throws DeviceException {
		xspress3Detector.asynchronousMoveTo(collectionTime);
	}

	public boolean isConfigureAtStartup() {
		return xspress3Detector.isConfigureAtStartup();
	}

	public void atScanLineStart() throws DeviceException {
		xspress3Detector.atScanLineStart();
	}

	public void setConfigureAtStartup(boolean configureAtStartup) {
		xspress3Detector.setConfigureAtStartup(configureAtStartup);
	}

	public Object getPosition() throws DeviceException {
		return xspress3Detector.getPosition();
	}

	public void reconfigure() throws FactoryException {
		xspress3Detector.reconfigure();
	}

	public void atScanEnd() throws DeviceException {
		xspress3Detector.atScanEnd();
	}

	public boolean isConfigured() {
		return xspress3Detector.isConfigured();
	}

	public void atPointEnd() throws DeviceException {
		xspress3Detector.atPointEnd();
	}

	public void setConfigured(boolean configured) {
		xspress3Detector.setConfigured(configured);
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

	public void notifyIObservers(Object source, Object arg) {
		xspress3Detector.notifyIObservers(source, arg);
	}

	public int getStatus() throws DeviceException {
		return xspress3Detector.getStatus();
	}

	public boolean equals(Object obj) {
		return xspress3Detector.equals(obj);
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

	public int hashCode() {
		return xspress3Detector.hashCode();
	}

	public NexusTreeProvider readout() throws DeviceException {
		return xspress3Detector.readout();
	}

	public void rawAsynchronousMoveTo(Object position) throws DeviceException {
		xspress3Detector.rawAsynchronousMoveTo(position);
	}

	public NexusTreeProvider[] readoutFrames(int firstFrame, int lastFrame) throws DeviceException {
		return xspress3Detector.readoutFrames(firstFrame, lastFrame);
	}

	public Object rawGetPosition() throws DeviceException {
		return xspress3Detector.rawGetPosition();
	}

	public Object externalToInternal(Object externalPosition) {
		return xspress3Detector.externalToInternal(externalPosition);
	}

	public Object internalToExternal(Object internalPosition) {
		return xspress3Detector.internalToExternal(internalPosition);
	}

	public Double[] readoutFF() throws DeviceException {
		return xspress3Detector.readoutFF();
	}

	public String[] getExtraNames() {
		return xspress3Detector.getExtraNames();
	}

	public void setRegionsOfInterest(ROI[] regionList) throws DeviceException {
		xspress3Detector.setRegionsOfInterest(regionList);
	}

	public ROI[] getRegionsOfInterest() throws DeviceException {
		return xspress3Detector.getRegionsOfInterest();
	}

	public void atPointStart() throws DeviceException {
		xspress3Detector.atPointStart();
	}

	public void atScanLineEnd() throws DeviceException {
		xspress3Detector.atScanLineEnd();
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

	public Double[][] getMCData(double time) throws DeviceException {
		return xspress3Detector.getMCData(time);
	}

	public void atLevelEnd() throws DeviceException {
		xspress3Detector.atLevelEnd();
	}

	public void atCommandFailure() throws DeviceException {
		xspress3Detector.atCommandFailure();
	}

	public int getFirstChannelToRead() {
		return xspress3Detector.getFirstChannelToRead();
	}

	public void setFirstChannelToRead(int firstChannelToRead) {
		xspress3Detector.setFirstChannelToRead(firstChannelToRead);
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

	public int getNumberOfFramesToCollect() throws DeviceException {
		return xspress3Detector.getNumberOfFramesToCollect();
	}

	public void setExtraNames(String[] names) {
		xspress3Detector.setExtraNames(names);
	}

	public void setInputNames(String[] names) {
		xspress3Detector.setInputNames(names);
	}

	public void setNumberOfFramesToCollect(int numberOfFramesToCollect) throws DeviceException {
		xspress3Detector.setNumberOfFramesToCollect(numberOfFramesToCollect);
	}

	public void setLevel(int level) {
		xspress3Detector.setLevel(level);
	}

	public void setOutputFormat(String[] names) {
		xspress3Detector.setOutputFormat(names);
	}

	public int getSummingMethod() {
		return xspress3Detector.getSummingMethod();
	}

	public void setSummingMethod(int summingMethod) {
		xspress3Detector.setSummingMethod(summingMethod);
	}

	public String getSumLabel() {
		return xspress3Detector.getSumLabel();
	}

	public void setSumLabel(String sumLabel) {
		xspress3Detector.setSumLabel(sumLabel);
	}

	public String getUnitsLabel() {
		return xspress3Detector.getUnitsLabel();
	}

	public String toString() {
		return xspress3Detector.toString();
	}

	public void setUnitsLabel(String unitsLabel) {
		xspress3Detector.setUnitsLabel(unitsLabel);
	}

	public boolean isWriteHDF5Files() {
		return xspress3Detector.isWriteHDF5Files();
	}

	public void setWriteHDF5Files(boolean writeHDF5Files) {
		xspress3Detector.setWriteHDF5Files(writeHDF5Files);
	}

	public void waitWhileBusy() throws DeviceException, InterruptedException {
		xspress3Detector.waitWhileBusy();
	}

	public String getFilePath() {
		return xspress3Detector.getFilePath();
	}

	public void setFilePath(String filePath) {
		xspress3Detector.setFilePath(filePath);
	}

	public String getFilePrefix() {
		return xspress3Detector.getFilePrefix();
	}

	public void setFilePrefix(String filePrefix) {
		xspress3Detector.setFilePrefix(filePrefix);
	}

	public String getNumTrackerExtension() {
		return xspress3Detector.getNumTrackerExtension();
	}

	public void waitWhileBusy(double timeoutInSeconds) throws DeviceException, InterruptedException {
		xspress3Detector.waitWhileBusy(timeoutInSeconds);
	}

	public void setNumTrackerExtension(String numTrackerExtension) {
		xspress3Detector.setNumTrackerExtension(numTrackerExtension);
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

	public PyObject __call__() {
		return xspress3Detector.__call__();
	}

	public PyObject __call__(PyObject new_position) {
		return xspress3Detector.__call__(new_position);
	}

	public int __len__() {
		return xspress3Detector.__len__();
	}

	public PyObject __getitem__(PyObject index) throws PyException {
		return xspress3Detector.__getitem__(index);
	}

	public PyString __str__() {
		return xspress3Detector.__str__();
	}

	public PyString __repr__() {
		return xspress3Detector.__repr__();
	}

	public PyString __doc__() {
		return xspress3Detector.__doc__();
	}

}
