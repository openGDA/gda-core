/*-
 * Copyright © 2009 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.gda.devices.detector.xspress3;

import gda.data.nexus.tree.NexusTreeProvider;
import gda.device.ContinuousParameters;
import gda.device.DeviceException;
import gda.device.detector.BufferedDetector;
import gda.device.detector.DetectorBase;
import gda.device.detector.NXDetectorData;
import gda.device.detector.NexusDetector;
import gda.factory.FactoryException;
import gda.observable.IObserver;
import uk.ac.gda.beans.DetectorROI;
import uk.ac.gda.devices.detector.FluorescenceDetector;
import uk.ac.gda.devices.detector.FluorescenceDetectorParameters;
import uk.ac.gda.devices.detector.xspress3.fullCalculations.Xspress3WithFullCalculationsDetector;

/**
 * When using an Xspress3 system in a ContinuousScan.
 * <p>
 * This decorates an underlying Xspress3Detector instance so that changing the
 * settings on one of the instances affects the other, as users would expect.
 */
public class Xspress3BufferedDetector extends DetectorBase implements BufferedDetector, NexusDetector,
		FluorescenceDetector, Xspress3 {

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
			} else {
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
	public NXDetectorData[] readFrames(int startFrame, int finalFrame) throws DeviceException {
		if (xspress3Detector instanceof Xspress3WithFullCalculationsDetector) {
			return ((Xspress3WithFullCalculationsDetector) xspress3Detector).readFrames(startFrame, finalFrame, getName());
		}
		return xspress3Detector.readFrames(startFrame, finalFrame);
	}

	@Override
	public NXDetectorData[] readAllFrames() throws DeviceException {
		if (xspress3Detector instanceof Xspress3WithFullCalculationsDetector) {
			return ((Xspress3WithFullCalculationsDetector) xspress3Detector).readFrames(0, xspress3Detector.getController().getNumFramesToAcquire(), getName());
		}
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

	@Override
	public double getCollectionTime() throws DeviceException {
		return xspress3Detector.getCollectionTime();
	}

	@Override
	public void setCollectionTime(double collectionTime) throws DeviceException {
		xspress3Detector.setCollectionTime(collectionTime);
	}

	@Override
	public int[] getDataDimensions() throws DeviceException {
		return xspress3Detector.getDataDimensions();
	}

	@Override
	public int getProtectionLevel() throws DeviceException {
		return xspress3Detector.getProtectionLevel();
	}

	@Override
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

	@Override
	public void atScanStart() throws DeviceException {
		// do nothing here, as the correct thing will be done by setContinuousParameters
	}

	@Override
	public void asynchronousMoveTo(Object collectionTime) throws DeviceException {
		xspress3Detector.asynchronousMoveTo(collectionTime);
	}

	@Override
	public void atScanLineStart() throws DeviceException {
		// do nothing here, as the correct thing will be done by setContinuousParameters
	}

	@Override
	public Object getPosition() throws DeviceException {
		return xspress3Detector.getPosition();
	}

	@Override
	public void reconfigure() throws FactoryException {
		xspress3Detector.reconfigure();
	}

	@Override
	public void atScanEnd() throws DeviceException {
		if (xspress3Detector instanceof Xspress3WithFullCalculationsDetector) {
			((Xspress3WithFullCalculationsDetector) xspress3Detector).setReadDataFromFile(false);
		}
		xspress3Detector.atScanEnd();
	}

	@Override
	public void atPointEnd() throws DeviceException {
		xspress3Detector.atPointEnd();
	}

	@Override
	public void close() throws DeviceException {
		xspress3Detector.close();
	}

	@Override
	public void setAttribute(String attributeName, Object value) throws DeviceException {
		xspress3Detector.setAttribute(attributeName, value);
	}

	@Override
	public Object getAttribute(String attributeName) throws DeviceException {
		return xspress3Detector.getAttribute(attributeName);
	}

	@Override
	public boolean isBusy() throws DeviceException {
		return xspress3Detector.isBusy();
	}

	@Override
	public String toFormattedString() {
		return xspress3Detector.toFormattedString();
	}

	@Override
	public void addIObserver(IObserver observer) {
		xspress3Detector.addIObserver(observer);
	}

	@Override
	public void collectData() throws DeviceException {
		xspress3Detector.collectData();
	}

	@Override
	public void deleteIObserver(IObserver observer) {
		xspress3Detector.deleteIObserver(observer);
	}

	@Override
	public void deleteIObservers() {
		xspress3Detector.deleteIObservers();
	}

	@Override
	public void stop() throws DeviceException {
		xspress3Detector.stop();
	}

	@Override
	public int getStatus() throws DeviceException {
		return xspress3Detector.getStatus();
	}

	@Override
	public String getDescription() throws DeviceException {
		return xspress3Detector.getDescription();
	}

	@Override
	public String getDetectorID() throws DeviceException {
		return xspress3Detector.getDetectorID();
	}

	@Override
	public String getDetectorType() throws DeviceException {
		return xspress3Detector.getDetectorType();
	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		return xspress3Detector.createsOwnFiles();
	}

	@Override
	public NexusTreeProvider readout() throws DeviceException {
		return xspress3Detector.readout();
	}

	@Override
	public String[] getExtraNames() {
		return xspress3Detector.getExtraNames();
	}

	@Override
	public void atPointStart() throws DeviceException {
		xspress3Detector.atPointStart();
	}

	@Override
	public void atScanLineEnd() throws DeviceException {
//		xspress3Detector.atScanLineEnd();
	}

	@Override
	public void clearAndStart() throws DeviceException {
		xspress3Detector.clearAndStart();
	}

	@Override
	public void atLevelStart() throws DeviceException {
		xspress3Detector.atLevelStart();
	}

	@Override
	public void atLevelMoveStart() throws DeviceException {
		xspress3Detector.atLevelMoveStart();
	}

	@Override
	@Deprecated
	public int[][] getMCData(double time) throws DeviceException {
		return xspress3Detector.getMCData(time);
	}

	@Override
	public double[][] getMCAData(double time) throws DeviceException {
		return xspress3Detector.getMCAData(time);
	}

	@Override
	public void atLevelEnd() throws DeviceException {
		xspress3Detector.atLevelEnd();
	}

	@Override
	public void atCommandFailure() throws DeviceException {
		if (xspress3Detector instanceof Xspress3WithFullCalculationsDetector) {
			((Xspress3WithFullCalculationsDetector) xspress3Detector).setReadDataFromFile(false);
		}
		xspress3Detector.atCommandFailure();
	}

	@Override
	public String[] getInputNames() {
		return xspress3Detector.getInputNames();
	}

	@Override
	public int getLevel() {
		return xspress3Detector.getLevel();
	}

	@Override
	public String[] getOutputFormat() {
		return xspress3Detector.getOutputFormat();
	}

	@Override
	public void moveTo(Object position) throws DeviceException {
		xspress3Detector.moveTo(position);
	}

	@Override
	public void setExtraNames(String[] names) {
		xspress3Detector.setExtraNames(names);
	}

	@Override
	public void setInputNames(String[] names) {
		xspress3Detector.setInputNames(names);
	}

	@Override
	public void setLevel(int level) {
		xspress3Detector.setLevel(level);
	}

	@Override
	public void setOutputFormat(String[] names) {
		xspress3Detector.setOutputFormat(names);
	}

	@Override
	public String toString() {
		return xspress3Detector.toString();
	}

	@Override
	public void waitWhileBusy() throws DeviceException, InterruptedException {
		xspress3Detector.waitWhileBusy();
	}

	@Override
	public String getConfigFileName() {
		return xspress3Detector.getConfigFileName();
	}

	@Override
	public void setConfigFileName(String configFileName) {
		xspress3Detector.setConfigFileName(configFileName);
	}

	@Override
	public void loadConfigurationFromFile() throws Exception {
		xspress3Detector.loadConfigurationFromFile();
	}

	@Override
	public boolean isAt(Object positionToTest) throws DeviceException {
		return xspress3Detector.isAt(positionToTest);
	}

	@Override
	public Xspress3Controller getController() {
		return xspress3Detector.getController();
	}

	@Override
	public double readoutFF() throws DeviceException {
		return xspress3Detector.readoutFF();
	}

	/**
	 * @deprecated Use getConfigurationParameters() instead
	 */
	@Override
	@Deprecated
	public DetectorROI[] getRegionsOfInterest() throws DeviceException {
		return xspress3Detector.getRegionsOfInterest();
	}

	/**
	 * @deprecated Use applyConfigurationParameters() instead
	 */
	@Override
	@Deprecated
	public void setRegionsOfInterest(DetectorROI[] regionList)
			throws DeviceException {
		xspress3Detector.setRegionsOfInterest(regionList);
	}

	@Override
	public int getNumberOfElements() {
		return xspress3Detector.getNumberOfElements();
	}

	@Override
	public int getMCASize() {
		return xspress3Detector.getMCASize();
	}

	@Override
	public int getMaxNumberOfRois() {
		return xspress3Detector.getMaxNumberOfRois();
	}

	@Override
	public void applyConfigurationParameters(FluorescenceDetectorParameters parameters) throws Exception {
		xspress3Detector.applyConfigurationParameters(parameters);
	}

	@Override
	public FluorescenceDetectorParameters getConfigurationParameters() {
		return xspress3Detector.getConfigurationParameters();
	}
}
