/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package gda.device.detector.areadetector.v17.impl;

import java.util.concurrent.TimeoutException;

import gda.device.detector.areadetector.AreaDetectorBin;
import gda.device.detector.areadetector.AreaDetectorROI;
import gda.device.detector.areadetector.v17.ADBase;
import gda.device.detector.areadetector.v17.ImageMode;
import gda.device.detector.areadetector.v17.NDPluginBase;
import gda.observable.Observable;
import gda.observable.ObservableUtil;

public class ADBaseSimulator implements ADBase {

	private ObservableUtil<Short> acquireStateObservable;
	private ObservableUtil<Double> createAcquireTimeObservable;

	private String model = "";

	@Override
	public String getPortName_RBV() throws Exception {
		return null;
	}

	@Override
	public String getManufacturer_RBV() throws Exception {
		return null;
	}

	@Override
	public String getModel_RBV() throws Exception {

		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	@Override
	public int getMaxSizeX_RBV() throws Exception {

		return 0;
	}

	@Override
	public int getMaxSizeY_RBV() throws Exception {

		return 0;
	}

	@Override
	public short getDataType() throws Exception {

		return 0;
	}

	@Override
	public void setDataType(String datatype) throws Exception {

	}

	@Override
	public short getDataType_RBV() throws Exception {

		return 0;
	}

	@Override
	public short getColorMode() throws Exception {

		return 0;
	}

	@Override
	public void setColorMode(int colormode) throws Exception {

	}

	@Override
	public short getColorMode_RBV() throws Exception {

		return 0;
	}

	@Override
	public int getBinX() throws Exception {

		return 0;
	}

	@Override
	public void setBinX(int binx) throws Exception {

	}

	@Override
	public int getBinX_RBV() throws Exception {

		return 0;
	}

	@Override
	public int getBinY() throws Exception {

		return 0;
	}

	@Override
	public void setBinY(int biny) throws Exception {

	}

	@Override
	public int getBinY_RBV() throws Exception {

		return 0;
	}

	@Override
	public int getMinX() throws Exception {

		return 0;
	}

	@Override
	public void setMinX(int minx) throws Exception {

	}

	@Override
	public int getMinX_RBV() throws Exception {

		return 0;
	}

	@Override
	public int getMinY() throws Exception {

		return 0;
	}

	@Override
	public void setMinY(int miny) throws Exception {

	}

	@Override
	public int getMinY_RBV() throws Exception {

		return 0;
	}

	@Override
	public int getSizeX() throws Exception {

		return 0;
	}

	@Override
	public void setSizeX(int sizex) throws Exception {

	}

	@Override
	public int getSizeX_RBV() throws Exception {

		return 0;
	}

	@Override
	public int getSizeY() throws Exception {

		return 0;
	}

	@Override
	public void setSizeY(int sizey) throws Exception {

	}

	@Override
	public int getSizeY_RBV() throws Exception {

		return 0;
	}

	@Override
	public short getReverseX() throws Exception {

		return 0;
	}

	@Override
	public void setReverseX(int reversex) throws Exception {

	}

	@Override
	public short getReverseX_RBV() throws Exception {

		return 0;
	}

	@Override
	public short getReverseY() throws Exception {

		return 0;
	}

	@Override
	public void setReverseY(int reversey) throws Exception {

	}

	@Override
	public short getReverseY_RBV() throws Exception {

		return 0;
	}

	@Override
	public int getArraySizeX_RBV() throws Exception {

		return 0;
	}

	@Override
	public int getArraySizeY_RBV() throws Exception {

		return 0;
	}

	@Override
	public int getArraySizeZ_RBV() throws Exception {

		return 0;
	}

	@Override
	public int getArraySize_RBV() throws Exception {

		return 0;
	}

	@Override
	public double getAcquireTime() throws Exception {

		return 0;
	}

	@Override
	public void setAcquireTime(double acquiretime) throws Exception {

	}

	double acquireTime = 0.0;

	@Override
	public double getAcquireTime_RBV() throws Exception {
		return acquireTime;
	}

	@Override
	public double getAcquirePeriod() throws Exception {
		return acquireTime;
	}

	@Override
	public void setAcquirePeriod(double acquireperiod) throws Exception {
		this.acquireTime = acquireperiod;
	}

	@Override
	public double getAcquirePeriod_RBV() throws Exception {
		return acquireTime;
	}

	@Override
	public double getTimeRemaining_RBV() throws Exception {
		return 0;
	}

	@Override
	public double getGain() throws Exception {

		return 0;
	}

	@Override
	public void setGain(double gain) throws Exception {

	}

	@Override
	public double getGain_RBV() throws Exception {

		return 0;
	}

	@Override
	public short getFrameType() throws Exception {

		return 0;
	}

	@Override
	public void setFrameType(int frametype) throws Exception {

	}

	@Override
	public short getFrameType_RBV() throws Exception {

		return 0;
	}

	@Override
	public short getImageMode() throws Exception {

		return 0;
	}

	@Override
	public void setImageMode(int imagemode) throws Exception {

	}

	@Override
	public short getImageMode_RBV() throws Exception {

		return 0;
	}

	@Override
	public short getTriggerMode() throws Exception {

		return 0;
	}

	@Override
	public void setTriggerMode(int triggermode) throws Exception {

	}

	@Override
	public short getTriggerMode_RBV() throws Exception {

		return 0;
	}

	@Override
	public int getNumExposures() throws Exception {

		return 0;
	}

	@Override
	public void setNumExposures(int numexposures) throws Exception {

	}

	@Override
	public int getNumExposures_RBV() throws Exception {

		return 0;
	}

	@Override
	public int getNumExposuresCounter_RBV() throws Exception {

		return 0;
	}

	@Override
	public int getNumImages() throws Exception {

		return 0;
	}

	@Override
	public void setNumImages(int numimages) throws Exception {

	}

	@Override
	public int getNumImages_RBV() throws Exception {

		return 0;
	}

	@Override
	public int getNumImagesCounter_RBV() throws Exception {

		return 0;
	}

	@Override
	public int getAcquireState() throws Exception {

		return 0;
	}

	@Override
	public void startAcquiring() throws Exception {

	}

	@Override
	public void stopAcquiring() throws Exception {

	}

	@Override
	public String getAcquire_RBV() throws Exception {

		return null;
	}

	@Override
	public int getArrayCounter() throws Exception {

		return 0;
	}

	@Override
	public void setArrayCounter(int arraycounter) throws Exception {

	}

	@Override
	public int getArrayCounter_RBV() throws Exception {

		return 0;
	}

	@Override
	public double getArrayRate_RBV() throws Exception {

		return 0;
	}

	@Override
	public short getDetectorState_RBV() throws Exception {

		return 0;
	}

	@Override
	public short getDetectorStateLastMonitoredValue() throws Exception {
		return 0;
	}

	@Override
	public short getArrayCallbacks() throws Exception {

		return 0;
	}

	@Override
	public void setArrayCallbacks(int arraycallbacks) throws Exception {

	}

	@Override
	public short getArrayCallbacks_RBV() throws Exception {

		return 0;
	}

	@Override
	public String getNDAttributesFile() throws Exception {

		return null;
	}

	@Override
	public void setNDAttributesFile(String ndattributesfile) throws Exception {

	}

	@Override
	public String getStatusMessage_RBV() throws Exception {

		return null;
	}

	@Override
	public String getStringToServer_RBV() throws Exception {

		return null;
	}

	@Override
	public String getStringFromServer_RBV() throws Exception {

		return null;
	}

	@Override
	public short getReadStatus() throws Exception {

		return 0;
	}

	@Override
	public void setReadStatus(int readstatus) throws Exception {

	}

	@Override
	public short getShutterMode() throws Exception {

		return 0;
	}

	@Override
	public void setShutterMode(int shuttermode) throws Exception {

	}

	@Override
	public short getShutterMode_RBV() throws Exception {

		return 0;
	}

	@Override
	public short getShutterControl() throws Exception {

		return 0;
	}

	@Override
	public void setShutterControl(int shuttercontrol) throws Exception {

	}

	@Override
	public short getShutterControl_RBV() throws Exception {

		return 0;
	}

	@Override
	public short getShutterStatus_RBV() throws Exception {

		return 0;
	}

	@Override
	public double getShutterOpenDelay() throws Exception {

		return 0;
	}

	@Override
	public void setShutterOpenDelay(double shutteropendelay) throws Exception {

	}

	@Override
	public double getShutterOpenDelay_RBV() throws Exception {

		return 0;
	}

	@Override
	public double getShutterCloseDelay() throws Exception {

		return 0;
	}

	@Override
	public void setShutterCloseDelay(double shutterclosedelay) throws Exception {

	}

	@Override
	public double getShutterCloseDelay_RBV() throws Exception {

		return 0;
	}

	@Override
	public String getShutterOpenEPICSPV() throws Exception {

		return null;
	}

	@Override
	public void setShutterOpenEPICSPV(String shutteropenepicspv) throws Exception {

	}

	@Override
	public String getShutterOpenEPICSCmd() throws Exception {

		return null;
	}

	@Override
	public void setShutterOpenEPICSCmd(String shutteropenepicscmd) throws Exception {

	}

	@Override
	public String getShutterCloseEPICSPV() throws Exception {

		return null;
	}

	@Override
	public void setShutterCloseEPICSPV(String shuttercloseepicspv) throws Exception {

	}

	@Override
	public String getShutterCloseEPICSCmd() throws Exception {

		return null;
	}

	@Override
	public void setShutterCloseEPICSCmd(String shuttercloseepicscmd) throws Exception {

	}

	@Override
	public short getShutterStatusEPICS_RBV() throws Exception {

		return 0;
	}

	@Override
	public String getShutterStatusEPICSPV() throws Exception {

		return null;
	}

	@Override
	public String getShutterStatusEPICSCloseVal() throws Exception {

		return null;
	}

	@Override
	public String getShutterStatusEPICSOpenVal() throws Exception {

		return null;
	}

	@Override
	public double getTemperature() throws Exception {

		return 0;
	}

	@Override
	public void setTemperature(double temperature) throws Exception {

	}

	@Override
	public double getTemperature_RBV() throws Exception {

		return 0;
	}

	@Override
	public int getInitialMinX() {

		return 0;
	}

	@Override
	public int getInitialMinY() {

		return 0;
	}

	@Override
	public int getInitialSizeX() {

		return 0;
	}

	@Override
	public int getInitialSizeY() {

		return 0;
	}

	@Override
	public AreaDetectorROI getAreaDetectorROI() throws Exception {

		return null;
	}

	@Override
	public AreaDetectorBin getBinning() throws Exception {

		return null;
	}

	@Override
	public String getInitialDataType() {

		return null;
	}

	@Override
	public void reset() throws Exception {

	}

	@Override
	public void setStatus(int status) {

	}

	@Override
	public int getStatus() {

		return 0;
	}

	@Override
	public void getEPICSStatus() throws Exception {

	}

	@Override
	public void startAcquiringSynchronously() throws Exception {

	}

	@Override
	public int waitWhileStatusBusy() throws InterruptedException {

		return 0;
	}

	@Override
	public void waitForArrayCounterToReach(int exposureNumber, double timeoutS) throws InterruptedException, Exception,
			TimeoutException {

	}

	@Override
	public void setImageModeWait(ImageMode imagemode) throws Exception {
	}

	@Override
	public Observable<Short> createAcquireStateObservable() throws Exception {
		if (acquireStateObservable == null) {
			acquireStateObservable = new ObservableUtil<Short>();
		}
		return acquireStateObservable;
	}

	@Override
	public Observable<Double> createAcquireTimeObservable() throws Exception {
		if (createAcquireTimeObservable == null) {
			createAcquireTimeObservable = new ObservableUtil<Double>();
		}
		return createAcquireTimeObservable;
	}

	@Override
	public void setImageMode(ImageMode imagemode) throws Exception {

	}

	@Override
	public void setMinXWait(int minx, double timeout) throws Exception {
	}

	@Override
	public void setMinYWait(int value, double timeout) throws Exception {
	}

	@Override
	public void setSizeXWait(int sizex, double timeout) throws Exception {
	}

	@Override
	public void setSizeYWait(int sizey, double timeout) throws Exception {
	}

	@Override
	public void setNumExposures(int numexposures, double timeout) throws Exception {
	}

	@Override
	public void setImageModeWait(ImageMode imagemode, double timeout)
			throws Exception {
	}

	@Override
	public void waitForDetectorStateIDLE(double timeoutS) throws InterruptedException, Exception, TimeoutException {
	}

	@Override
	public NDPluginBase.DataType getDataType_RBV2() throws Exception {
		return NDPluginBase.DataType.INT8;
	}

	@Override
	public void startAcquiringWait() throws Exception {
		// TODO Auto-generated method stub
	}

	// General purpose getters and setters for arbitrary PV suffixes. Primarily for prototyping.

	@Override
	public int getIntBySuffix(String suffix) throws Exception {
		return 0;
	}

	@Override
	public void setIntBySuffix(String suffix, int arraycounter) throws Exception {
	}

	@Override
	public double getDoubleBySuffix(String suffix) throws Exception {
		return 0;
	}

	@Override
	public void setDoubleBySuffix(String suffix, double acquiretime) throws Exception {
	}

	@Override
	public String getStringBySuffix(String suffix) throws Exception {
		return null;
	}

	@Override
	public void setStringBySuffix(String suffix, String acquiretime) throws Exception {
	}
}
