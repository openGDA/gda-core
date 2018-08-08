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

package uk.ac.gda.devices.pixium;

import gda.device.detector.areadetector.AreaDetectorROI;
import gda.device.detector.areadetector.v17.ADBase;
import gda.device.detector.areadetector.v17.FfmpegStream;
import gda.device.detector.areadetector.v17.NDFile;
import gda.device.detector.areadetector.v17.NDFileHDF5;
import gda.device.detector.areadetector.v17.NDFileNexus;
import gda.device.detector.areadetector.v17.NDOverlay;
import gda.device.detector.areadetector.v17.NDProcess;
import gda.device.detector.areadetector.v17.NDROI;
import gda.device.detector.areadetector.v17.NDStats;
import gda.factory.Findable;

public interface IPixiumController extends Findable{

	// Getters and Setters for Spring
	ADBase getAreaDetector();

	NDFile getTiff();

	//NDFile getImage();

	String getBasePVName();

	void resetAll() throws Exception;

	int getExposures() throws Exception;

	void resetAndEnableCameraControl() throws Exception;

	String getAcquisitionMode() throws Exception;

	String setMode(int logicalMode, int offset) throws Exception;

	String report() throws Exception;

	void acquire() throws Exception;

	short getDetectorState() throws Exception;

	int getAcquireState() throws Exception;

	void setNumImages(int numberOfImage) throws Exception;

	int getArrayCounter() throws Exception;

	void setAcquirePeriod(double acquirePeriod) throws Exception;

	void stopAcquiring() throws Exception;

	void setImageMode(int imageMode) throws Exception;

	AreaDetectorROI getAreaDetectorROI() throws Exception;

	void startTiffCapture() throws Exception;

	int getImageMode() throws Exception;

	void setTiffNumCapture(int numcapture) throws Exception;

	String getTiffFullFileName() throws Exception;

	void resetAndStartFilesRecording() throws Exception;

	int getTiffCaptureState() throws Exception;
	
	void setIdlePollTime_ms(Double idlePollTime_ms);

	Double getIdlePollTime_ms();

	void setExposures(int numberOfExposures) throws Exception;

	FfmpegStream getMjpeg();

	NDFileNexus getNxs();

	NDOverlay getDraw();

	NDStats getStat();

	NDProcess getProc();

	NDROI getRoi();
	
	void stopTiffCapture() throws Exception;

	NDFileHDF5 getHdf();

	int getLogicalMode() throws Exception;

	void setLogicalMode(int value) throws Exception;

	int getPUMode() throws Exception;

	void setPUMode(int value) throws Exception;

	int getNumberOfOffsets() throws Exception;

	void setNumberOfOffsets(int value) throws Exception;

	int getOffsetReferenceNumber() throws Exception;

	void setOffsetReferenceNumber(int value) throws Exception;

	int getOffsetReference() throws Exception;

	void setOffsetReference(int value) throws Exception;

	void startOffsetCalibration() throws Exception;

	void abortOffsetCalibration() throws Exception;

	int getStatus();

	int getFrameRate() throws Exception;

	void setFrameRate(int value) throws Exception;

	int getXRayWindow() throws Exception;

	void setXRayWindow(int value) throws Exception;

	int getLogicalModeStatus() throws Exception;

	int getDeltaFreq() throws Exception;

	void setDeltaFreq(int value) throws Exception;

	void connect() throws Exception;

	void disconnect() throws Exception;

	void changeMode() throws Exception;

	String getConnectionState() throws Exception;

	String getChangeModeState() throws Exception;

	void applyOffsetReference() throws Exception;

	String getApplyOffsetReferenceState() throws Exception;

	void defineMode() throws Exception;

	String getDefineModeState() throws Exception;

	void deleteMode() throws Exception;

	String getDeleteModeState() throws Exception;

	void loadMode() throws Exception;

	String getLoadModeState() throws Exception;

	void unloadMode() throws Exception;

	String getUnloadModeState() throws Exception;

	void activateMode() throws Exception;

	String getActivateModeState() throws Exception;

	void deactivateMode() throws Exception;

	String getDeactivateModeState() throws Exception;

	int getThreshold() throws Exception;

	void setThreshold(int value) throws Exception;

	int getABCMinVoltage() throws Exception;

	void setABCMinVoltage(int value) throws Exception;

	int getABCMaxVoltage() throws Exception;

	void setABCMaxVoltage(int value) throws Exception;

	void startHdfCapture() throws Exception;

	void setHdfNumCapture(int numcapture) throws Exception;

	String getHdfFullFileName() throws Exception;

	int getHdfCaptureState() throws Exception;

	void stopHdfCapture() throws Exception;

	void stop() throws Exception;

	void disableTiffSaver() throws Exception;

	void enableTiffSaver() throws Exception;

	void disableHdfSaver() throws Exception;

	void enableHdfSaver() throws Exception;

	void setScanDimensions(int[] dimensions) throws Exception;

	String getHDFFileName() throws Exception;

	void startRecording() throws Exception;

	void endRecording() throws Exception;

	double getAcquireTime() throws Exception;

	double getAcquirePeriod() throws Exception;

	void startOffsetCalibration(double timeout) throws Exception;

}