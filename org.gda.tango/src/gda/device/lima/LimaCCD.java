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

package gda.device.lima;

import fr.esrf.Tango.DevFailed;
import gda.device.DeviceException;
import gda.device.base.Base;

public interface LimaCCD extends Base {

	void prepareAcq() throws DevFailed;

	void startAcq() throws DevFailed;

	void stopAcq() throws DevFailed;

	void setImageHeader(String[] headers) throws DevFailed;

	byte[] getImage(int imageNumber) throws DevFailed;

	void closeShutterManual() throws DevFailed;

	void closeOpenManual() throws DevFailed;

	void reset() throws DevFailed;

	String getLimaType() throws DevFailed;

	String getCameraType() throws DevFailed;

	String getCameraModel() throws DevFailed;

	enum AcqStatus {
		READY, RUNNING, FAULT, CONFIGURATION
	}

	AcqStatus getAcqStatus() throws DevFailed;

	enum AcqMode {
		SINGLE, CONCATENATION, 
		/**
		 * ACCUMULATION mode 
		 * To use this mode you have to:
		 * set acq_mode to ACCUMULATION
		 * set acc_max_expotime to the maximum exposure time per frame
		 * set acq_expo_time to desired exposure time ( greater than acc_max_expotime)
		 * that all !
		 * other attributes like acc_nb_frames is "read-only" and tell you how
		 * many real frames will be taken to fit with the exposure time and max
		 * exposure time per accumulated frames.
		 * 
		 * Then if you specify an total exposure time for instance of 1 second and
		 * with acc_max_expotime set to 0.05 second, the detector will take 20
		 * frames and accumulate them in one single image.
		 * Now if you set the acq_nb_frames to 2 that just means you want to take 2
		 * images which means the detector will take 20 x 2 frames but accumulated
		 * into 2 images.
		 * The accumulation mode has been implemented to allow long exposure time
		 * without satured  the detector, as you may know the maxipix detector is
		 * limited in depth (11 bits).
		 */
		ACCUMULATION
	}

	AcqMode getAcqMode() throws DevFailed;

	void setAcqMode(AcqMode mode) throws DevFailed;

	int getAcqNbFrames() throws DevFailed;

	void setAcqNbFrames(Integer acqNbFrames) throws DevFailed;

	enum AcqTriggerMode {
		INTERNAL_TRIGGER, EXTERNAL_TRIGGER, EXTERNAL_TRIGGER_MULTI, INTERNAL_TRIGGER_MULTI, EXTERNAL_GATE, EXTERNAL_START_STOP
	}

	AcqTriggerMode getAcqTriggerMode() throws DevFailed;

	void setAcqTriggerMode(AcqTriggerMode mode) throws DevFailed;

	double getLatencyTime() throws DevFailed;

	void setLatencyTime(double latencyTime) throws DevFailed;

	double getAcqExpoTime() throws DevFailed;

	void setAcqExpoTime(double acqExpoTime) throws DevFailed;

	double getAccExpoTime() throws DevFailed;

	int getAccNbFrames() throws DevFailed;

	double getAccMaxExpoTime() throws DevFailed;

	void setAccMaxExpoTime(double accMaxExpoTime) throws DevFailed;

	enum AccTimeMode {
		LIVE, REAL
	}

	AccTimeMode getAccTimeMode() throws DevFailed;

	void setAccTimeMode(AccTimeMode accTimeMode) throws DevFailed;

	double getAccDeadTime() throws DevFailed;

	double getAccLiveTime() throws DevFailed;

	enum ImageType {
		BPP8, BPP8S, BPP10, BPP10S, BPP12, BPP12S, BPP14, BPP14S, BPP16, BPP16S, BPP32, BPP32S
	}

	ImageType getImageType() throws DevFailed;

	long getImageWidth() throws DevFailed;

	long getImageHeight() throws DevFailed;

	LimaROIInt getImageROIInt() throws DevFailed;

	void setImageROIInt(LimaROIInt limaROIInt) throws DevFailed;

	LimaBin getImageBin() throws DevFailed;

	void setImageBin(LimaBin limaBin) throws DevFailed;
	
	void setImageBin(long xBinValue, long yBinValue) throws DeviceException;

	LimaFlip getImageFlip() throws DevFailed;

	void setImageFlip(LimaFlip limaFlip) throws DevFailed;

	enum ShutterMode {
		MANUAL, AUTO_FRAME, AUTO_SEQUENCE
	}

	ShutterMode getShutterMode() throws DevFailed;

	void setShutterMode(ShutterMode shutterMode) throws DevFailed;

	double getShutterOpenTime() throws DevFailed;

	void setShutterOpenTime(double shutterOpenTime) throws DevFailed;

	enum SavingMode {
		MANUAL, AUTO_FRAME, AUTO_HEADER
	}

	SavingMode getSavingMode() throws DevFailed;

	void setSavingMode(SavingMode savingMode) throws DevFailed;

	String getSavingDirectory() throws DevFailed;

	void setSavingDirectory(String savingDirectory) throws DevFailed;

	String getSavingPrefix() throws DevFailed;

	/**
	 * Changing savingPrefix will cause the saving_next_number to be set to 0. You then need to change the
	 * saving_next_number to 1 to get the value to increment. Fails if the prefix has already been used - if files exist
	 * which match
	 */
	void setSavingPrefix(String savingPrefix) throws DevFailed;

	String getSavingSuffix() throws DevFailed;

	/**
	 * The suffix is set implicitly by setting the SavingFormat e.g. EDF = .edf
	 * 
	 * @param savingSuffix
	 * @throws DevFailed
	 */
	void setSavingSuffix(String savingSuffix) throws DevFailed;

	int getSavingNextNumber() throws DevFailed;

	/**
	 * Changing savingPrefix will cause the saving_next_number to be set to 0. You then need to change the
	 * saving_next_number to 1 to get the value to increment.
	 */
	void setSavingNextNumber(int savingNextNumber) throws DevFailed;

	enum SavingFormat {
		RAW, EDF, CBF
	}

	SavingFormat getSavingFormat() throws DevFailed;

	void setSavingFormat(SavingFormat savingFormat) throws DevFailed;

	enum SavingOverwritePolicy {
		ABORT, OVERWRITE, APPEND
	}

	SavingOverwritePolicy getSavingOverwritePolicy() throws DevFailed;

	void setSavingOverwritePolicy(SavingOverwritePolicy savingOverwritePolicy) throws DevFailed;

	int getSavingFramePerFile() throws DevFailed;

	void setSavingFramePerFile(int savingFramePerFile) throws DevFailed;

	String[] getSavingCommonHeader() throws DevFailed;

	void setSavingCommonHeader(String[] savingCommonHeader) throws DevFailed;

	LimaSavingHeaderDelimiter getLimaSavingHeaderDelimiter() throws DevFailed;

	void setLimaSavingHeaderDelimiter(LimaSavingHeaderDelimiter limaSavingHeaderDelimiter) throws DevFailed;

	int getLastImageReady() throws DevFailed;

	/*
	 * 0 is saved OK, -1 if not
	 */
	int getLastImageSaved() throws DevFailed;

	boolean getReadyForNextImage() throws DevFailed;

	boolean getReadyForNextAcq() throws DevFailed;

	double[] getWriteStatistics() throws DevFailed;

	String[] getDebugModules() throws DevFailed;

	void setDebugModules(String[] debugModules) throws DevFailed;

	String[] getDebugTypes() throws DevFailed;

	void setDebugTypes(String[] debugTypes) throws DevFailed;

}
