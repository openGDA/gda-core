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

package gda.device.detector.areadetector.v17;

import java.util.concurrent.TimeoutException;

import gda.device.detector.areadetector.AreaDetectorBin;
import gda.device.detector.areadetector.AreaDetectorROI;
import gda.device.detector.areadetector.v17.NDPluginBase.DataType;
import gda.observable.Observable;

/**
 * ADBase represents the AreaDetector driver - commonly the first tab in edm
 */
public interface ADBase extends ADCommon {

	public enum StandardTriggerMode {
		INTERNAL, EXTERNAL
	}

	/**
	 * List all the PVs
	 */

	public final String PortName_RBV = "PortName_RBV";

	public final String Manufacturer_RBV = "Manufacturer_RBV";

	public final String Model_RBV = "Model_RBV";

	public final String MaxSizeX_RBV = "MaxSizeX_RBV";

	public final String MaxSizeY_RBV = "MaxSizeY_RBV";

	public final String DataType = "DataType";

	public final String DataType_RBV = "DataType_RBV";

	public final String ColorMode = "ColorMode";

	public final String ColorMode_RBV = "ColorMode_RBV";

	public final String BinX = "BinX";

	public final String BinX_RBV = "BinX_RBV";

	public final String BinY = "BinY";

	public final String BinY_RBV = "BinY_RBV";

	public final String MinX = "MinX";

	public final String MinX_RBV = "MinX_RBV";

	public final String MinY = "MinY";

	public final String MinY_RBV = "MinY_RBV";

	public final String SizeX = "SizeX";

	public final String SizeX_RBV = "SizeX_RBV";

	public final String SizeY = "SizeY";

	public final String SizeY_RBV = "SizeY_RBV";

	public final String ReverseX = "ReverseX";

	public final String ReverseX_RBV = "ReverseX_RBV";

	public final String ReverseY = "ReverseY";

	public final String ReverseY_RBV = "ReverseY_RBV";

	public final String ArraySizeX_RBV = "ArraySizeX_RBV";

	public final String ArraySizeY_RBV = "ArraySizeY_RBV";

	public final String ArraySizeZ_RBV = "ArraySizeZ_RBV";

	public final String ArraySize_RBV = "ArraySize_RBV";

	public final String AcquireTime = "AcquireTime";

	public final String AcquireTime_RBV = "AcquireTime_RBV";

	public final String AcquirePeriod = "AcquirePeriod";

	public final String AcquirePeriod_RBV = "AcquirePeriod_RBV";

	public final String TimeRemaining_RBV = "TimeRemaining_RBV";

	public final String Gain = "Gain";

	public final String Gain_RBV = "Gain_RBV";

	public final String FrameType = "FrameType";

	public final String FrameType_RBV = "FrameType_RBV";

	public final String ImageMode = "ImageMode";

	public final String ImageMode_RBV = "ImageMode_RBV";

	public final String TriggerMode = "TriggerMode";

	public final String TriggerMode_RBV = "TriggerMode_RBV";

	public final String NumExposures = "NumExposures";

	public final String NumExposures_RBV = "NumExposures_RBV";

	public final String NumExposuresCounter_RBV = "NumExposuresCounter_RBV";

	public final String NumImages = "NumImages";

	public final String NumImages_RBV = "NumImages_RBV";

	public final String NumImagesCounter_RBV = "NumImagesCounter_RBV";

	public final String Acquire = "Acquire";

	public final String Acquire_RBV = "Acquire_RBV";

	public final String ArrayCounter = "ArrayCounter";

	public final String ArrayCounter_RBV = "ArrayCounter_RBV";

	public final String ArrayRate_RBV = "ArrayRate_RBV";

	public final String DetectorState_RBV = "DetectorState_RBV";

	public final String ArrayCallbacks = "ArrayCallbacks";

	public final String ArrayCallbacks_RBV = "ArrayCallbacks_RBV";

	public final String NDAttributesFile = "NDAttributesFile";

	public final String StatusMessage_RBV = "StatusMessage_RBV";

	public final String StringToServer_RBV = "StringToServer_RBV";

	public final String StringFromServer_RBV = "StringFromServer_RBV";

	public final String ReadStatus = "ReadStatus";

	public final String ShutterMode = "ShutterMode";

	public final String ShutterMode_RBV = "ShutterMode_RBV";

	public final String ShutterControl = "ShutterControl";

	public final String ShutterControl_RBV = "ShutterControl_RBV";

	public final String ShutterStatus_RBV = "ShutterStatus_RBV";

	public final String ShutterOpenDelay = "ShutterOpenDelay";

	public final String ShutterOpenDelay_RBV = "ShutterOpenDelay_RBV";

	public final String ShutterCloseDelay = "ShutterCloseDelay";

	public final String ShutterCloseDelay_RBV = "ShutterCloseDelay_RBV";

	public final String ShutterOpenEPICSPV_PVPOSTFIX = "ShutterOpenEPICS.OUT";

	public final String ShutterOpenEPICSPV_ELEMENTNAME = "ShutterOpenEPICSPV";

	public final String ShutterOpenEPICSCmd_PVPOSTFIX = "ShutterOpenEPICS.OCAL";
	public final String ShutterOpenEPICSCmd_ElEMENTNAME = "ShutterOpenEPICSCmd";

	public final String ShutterCloseEPICSPV_PVPOSTFIX = "ShutterCloseEPICS.OUT";
	public final String ShutterCloseEPICSPV_ELEMENTNAME = "ShutterCloseEPICSPV";

	public final String ShutterCloseEPICSCmd_PVPOSTFIX = "ShutterCloseEPICS.OCAL";
	public final String ShutterCloseEPICSCmd_ELEMENTNAME = "ShutterCloseEPICSCmd";

	public final String ShutterStatusEPICS_RBV = "ShutterStatusEPICS_RBV";

	public final String ShutterStatusEPICSPV_ELEMENTNAME = "ShutterStatusEPICSPV";
	public final String ShutterStatusEPICSPV_PVPOSTFIX = "ShutterStatusEPICS_RBV.INP";

	public final String ShutterStatusEPICSCloseVal_PVPOSTFIX = "ShutterStatusEPICS_RBV.ZRVL";
	public final String ShutterStatusEPICSCloseVal_ELEMENTNAME = "ShutterStatusEPICSCloseVal";
	public final String ShutterStatusEPICSOpenVal_PVPOSTFIX = "ShutterStatusEPICS_RBV.ONVL";// OpenVal";
	public final String ShutterStatusEPICSOpenVal_ELEMENTNAME = "ShutterStatusEPICSOpenVal";// OpenVal";

	public final String Temperature = "Temperature";

	public final String Temperature_RBV = "Temperature_RBV";

	/**
	 *
	 */
	String getManufacturer_RBV() throws Exception;

	/**
	 *
	 */
	String getModel_RBV() throws Exception;

	/**
	 *
	 */
	int getMaxSizeX_RBV() throws Exception;

	/**
	 *
	 */
	int getMaxSizeY_RBV() throws Exception;

	/**
	 * Returns the value of the PV - this is not the same as the NDDataType_t value. Use getDataType_RBV2 instead
	 */
	short getDataType() throws Exception;

	/**
	 *
	 */
	void setDataType(String datatype) throws Exception;

	/**
	 * Returns the value of the PV - this is not the same as the NDDataType_t value. Use getDataType_RBV2 instead
	 */
	short getDataType_RBV() throws Exception;


	DataType getDataType_RBV2() throws Exception;


	/**
	 *
	 */
	short getColorMode() throws Exception;

	/**
	 *
	 */
	void setColorMode(int colormode) throws Exception;

	/**
	 *
	 */
	short getColorMode_RBV() throws Exception;

	/**
	 *
	 */
	int getBinX() throws Exception;

	/**
	 *
	 */
	void setBinX(int binx) throws Exception;

	/**
	 *
	 */
	int getBinX_RBV() throws Exception;

	/**
	 *
	 */
	int getBinY() throws Exception;

	/**
	 *
	 */
	void setBinY(int biny) throws Exception;

	/**
	 *
	 */
	int getBinY_RBV() throws Exception;

	/**
	 *
	 */
	int getMinX() throws Exception;

	/**
	 *
	 */
	void setMinX(int minx) throws Exception;

	/**
	 *
	 */
	int getMinX_RBV() throws Exception;

	/**
	 *
	 */
	int getMinY() throws Exception;

	/**
	 *
	 */
	void setMinY(int miny) throws Exception;

	/**
	 *
	 */
	int getMinY_RBV() throws Exception;

	/**
	 *
	 */
	int getSizeX() throws Exception;

	/**
	 *
	 */
	void setSizeX(int sizex) throws Exception;

	/**
	 *
	 */
	int getSizeX_RBV() throws Exception;

	/**
	 *
	 */
	int getSizeY() throws Exception;

	/**
	 *
	 */
	void setSizeY(int sizey) throws Exception;

	/**
	 *
	 */
	int getSizeY_RBV() throws Exception;

	/**
	 *
	 */
	short getReverseX() throws Exception;

	/**
	 *
	 */
	void setReverseX(int reversex) throws Exception;

	/**
	 *
	 */
	short getReverseX_RBV() throws Exception;

	/**
	 *
	 */
	short getReverseY() throws Exception;

	/**
	 *
	 */
	void setReverseY(int reversey) throws Exception;

	/**
	 *
	 */
	short getReverseY_RBV() throws Exception;

	/**
	 *
	 */
	int getArraySizeX_RBV() throws Exception;

	/**
	 *
	 */
	int getArraySizeY_RBV() throws Exception;

	/**
	 *
	 */
	int getArraySizeZ_RBV() throws Exception;

	/**
	 *
	 */
	int getArraySize_RBV() throws Exception;

	/**
	 *
	 */
	double getAcquireTime() throws Exception;

	/**
	 *
	 */
	void setAcquireTime(double acquiretime) throws Exception;

	/**
	 *
	 */
	double getAcquireTime_RBV() throws Exception;

	/**
	 *
	 */
	double getAcquirePeriod() throws Exception;

	/**
	 *
	 */
	void setAcquirePeriod(double acquireperiod) throws Exception;

	/**
	 *
	 */
	double getAcquirePeriod_RBV() throws Exception;

	/**
	 *
	 */
	double getTimeRemaining_RBV() throws Exception;

	/**
	 *
	 */
	double getGain() throws Exception;

	/**
	 *
	 */
	void setGain(double gain) throws Exception;

	/**
	 *
	 */
	double getGain_RBV() throws Exception;

	/**
	 *
	 */
	short getFrameType() throws Exception;

	/**
	 *
	 */
	void setFrameType(int frametype) throws Exception;

	/**
	 *
	 */
	short getFrameType_RBV() throws Exception;

	/**
	 *
	 */
	short getImageMode() throws Exception;


	/**
	 *
	 */
	void setImageMode(int imagemode) throws Exception;

	/**
	 *
	 */
	short getImageMode_RBV() throws Exception;

	/**
	 *
	 */
	short getTriggerMode() throws Exception;

	/**
	 *
	 */
	void setTriggerMode(int triggermode) throws Exception;

	/**
	 *
	 */
	short getTriggerMode_RBV() throws Exception;

	/**
	 *
	 */
	int getNumExposures() throws Exception;

	/**
	 * From http://cars9.uchicago.edu/software/epics/areaDetectorDoc.html#ADDriver:
	 * "Number of exposures per image to acquire". Contrast with {@link #setNumImages(int)}.
	 * To quote Ulrik Pederson: "most detectors don't support this.
	 */
	void setNumExposures(int numexposures) throws Exception;

	/**
	 *
	 */
	int getNumExposures_RBV() throws Exception;

	/**
	 *
	 */
	int getNumExposuresCounter_RBV() throws Exception;

	/**
	 *
	 */
	int getNumImages() throws Exception;

	/**
	 * From http://cars9.uchicago.edu/software/epics/areaDetectorDoc.html#ADDriver:
	 * "Number of images to acquire in one acquisition sequence". Contrast to {@link #setNumExposures(int)}.
	 */
	void setNumImages(int numimages) throws Exception;

	/**
	 *
	 */
	int getNumImages_RBV() throws Exception;

	/**
	 *
	 */
	int getNumImagesCounter_RBV() throws Exception;

	/**
	 *
	 */
	int getAcquireState() throws Exception;

	/**
	 *
	 */
	void startAcquiring() throws Exception;

	/**
	 *
	 */
	void stopAcquiring() throws Exception;

	/**
	 *
	 */
	String getAcquire_RBV() throws Exception;

	/**
	 *
	 */
	int getArrayCounter() throws Exception;

	/**
	 *
	 */
	void setArrayCounter(int arraycounter) throws Exception;

	/**
	 *
	 */
	int getArrayCounter_RBV() throws Exception;

	/**
	 *
	 */
	double getArrayRate_RBV() throws Exception;

	/**
	 *
	 */
	short getDetectorState_RBV() throws Exception;

	/**
	 *
	 */
	short getDetectorStateLastMonitoredValue() throws Exception;

	/**
	 *
	 */
	short getArrayCallbacks() throws Exception;

	/**
	 *
	 */
	void setArrayCallbacks(int arraycallbacks) throws Exception;

	/**
	 *
	 */
	short getArrayCallbacks_RBV() throws Exception;

	/**
	 *
	 */
	String getNDAttributesFile() throws Exception;

	/**
	 *
	 */
	void setNDAttributesFile(String ndattributesfile) throws Exception;

	/**
	 *
	 */
	String getStatusMessage_RBV() throws Exception;

	/**
	 *
	 */
	String getStringToServer_RBV() throws Exception;

	/**
	 *
	 */
	String getStringFromServer_RBV() throws Exception;

	/**
	 *
	 */
	short getReadStatus() throws Exception;

	/**
	 *
	 */
	void setReadStatus(int readstatus) throws Exception;

	/**
	 *
	 */
	short getShutterMode() throws Exception;

	/**
	 *
	 */
	void setShutterMode(int shuttermode) throws Exception;

	/**
	 *
	 */
	short getShutterMode_RBV() throws Exception;

	/**
	 *
	 */
	short getShutterControl() throws Exception;

	/**
	 *
	 */
	void setShutterControl(int shuttercontrol) throws Exception;

	/**
	 *
	 */
	short getShutterControl_RBV() throws Exception;

	/**
	 *
	 */
	short getShutterStatus_RBV() throws Exception;

	/**
	 *
	 */
	double getShutterOpenDelay() throws Exception;

	/**
	 *
	 */
	void setShutterOpenDelay(double shutteropendelay) throws Exception;

	/**
	 *
	 */
	double getShutterOpenDelay_RBV() throws Exception;

	/**
	 *
	 */
	double getShutterCloseDelay() throws Exception;

	/**
	 *
	 */
	void setShutterCloseDelay(double shutterclosedelay) throws Exception;

	/**
	 *
	 */
	double getShutterCloseDelay_RBV() throws Exception;

	/**
	 *
	 */
	String getShutterOpenEPICSPV() throws Exception;

	/**
	 *
	 */
	void setShutterOpenEPICSPV(String shutteropenepicspv) throws Exception;

	/**
	 *
	 */
	String getShutterOpenEPICSCmd() throws Exception;

	/**
	 *
	 */
	void setShutterOpenEPICSCmd(String shutteropenepicscmd) throws Exception;

	/**
	 *
	 */
	String getShutterCloseEPICSPV() throws Exception;

	/**
	 *
	 */
	void setShutterCloseEPICSPV(String shuttercloseepicspv) throws Exception;

	/**
	 *
	 */
	String getShutterCloseEPICSCmd() throws Exception;

	/**
	 *
	 */
	void setShutterCloseEPICSCmd(String shuttercloseepicscmd) throws Exception;

	/**
	 *
	 */
	short getShutterStatusEPICS_RBV() throws Exception;

	/**
	 *
	 */
	String getShutterStatusEPICSPV() throws Exception;

	/**
	 *
	 */
	String getShutterStatusEPICSCloseVal() throws Exception;

	/**
	 *
	 */
	String getShutterStatusEPICSOpenVal() throws Exception;

	/**
	 *
	 */
	double getTemperature() throws Exception;

	/**
	 *
	 */
	void setTemperature(double temperature) throws Exception;

	/**
	 *
	 */
	double getTemperature_RBV() throws Exception;

	/**
	 * @return initialMinX
	 */
	int getInitialMinX();

	/**
	 * @return initialMinY
	 */
	int getInitialMinY();

	/**
	 * @return initialSizeX
	 */
	int getInitialSizeX();

	/**
	 * @return initialSizeY
	 */
	int getInitialSizeY();

	/**
	 * @return {@link AreaDetectorROI}
	 */
	AreaDetectorROI getAreaDetectorROI() throws Exception;

	/**
	 * @return {@link AreaDetectorBin}
	 */
	AreaDetectorBin getBinning() throws Exception;

	/**
	 * @return initialDataType
	 */
	String getInitialDataType();

	/**
	 *
	 */
	void reset() throws Exception;

	void setStatus(int status);

	int getStatus();

	void getEPICSStatus() throws Exception;

	void startAcquiringSynchronously() throws Exception;

	int waitWhileStatusBusy() throws InterruptedException;

	/**
	 * Waits for a certain exposure during a multiple=exposure acquisition to complete. Note that
	 * array callbacks must be enabled with {@link #setArrayCallbacks(int)} for this to return, and
	 * that it monitors the RBV field.
	 * @param exposureNumber
	 * @throws Exception
	 * @throws InterruptedException
	 * @throws TimeoutException if it takes more than timeoutMilliS for the counter to reach the required value
	 */
	void waitForArrayCounterToReach(int exposureNumber, double timeoutS) throws InterruptedException, Exception, java.util.concurrent.TimeoutException;

	void setImageModeWait(ImageMode imagemode) throws Exception;


	Observable<Short> createAcquireStateObservable() throws Exception;
	Observable<Double> createAcquireTimeObservable() throws Exception;

	void setImageMode(ImageMode imagemode) throws Exception;

	void setMinXWait(int minx, double timeout) throws Exception;

	void setMinYWait(int value, double timeout) throws Exception;

	void setSizeXWait(int sizex, double timeout) throws Exception;

	void setSizeYWait(int sizey, double timeout) throws Exception;

	void setNumExposures(int numexposures, double timeout) throws Exception;

	void setImageModeWait(ImageMode imagemode, double timeout) throws Exception;

	void waitForDetectorStateIDLE(double timeoutS) throws InterruptedException, Exception, TimeoutException;

	void startAcquiringWait() throws Exception;

	// General purpose getters and setters for arbitrary PV suffixes. Primarily for prototyping.

	int getIntBySuffix(String suffix) throws Exception;

	void setIntBySuffix(String suffix, int arraycounter) throws Exception;

	double getDoubleBySuffix(String suffix) throws Exception;

	void setDoubleBySuffix(String suffix, double acquiretime) throws Exception;

	String getStringBySuffix(String suffix) throws Exception;

	void setStringBySuffix(String suffix, String acquiretime) throws Exception;
}
