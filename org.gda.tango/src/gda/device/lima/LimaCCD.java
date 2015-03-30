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
	
	double[] getCameraPixelSize() throws DevFailed;

	String getCameraModel() throws DevFailed;

	enum AcqStatus {
		READY, RUNNING, FAULT, CONFIGURATION
	}

	AcqStatus getAcqStatus() throws DevFailed;

	enum AcqMode {
		SINGLE, //the default mode, one frame per image 
		CONCATENATION, // frames are concatenated in the image
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
		 * without saturating the detector, as you may know the maxipix detector is
		 * limited in depth (11 bits).
		 */
		ACCUMULATION // the exposure is shared by multiple frames to avoid pixel saturation
	}

	AcqMode getAcqMode() throws DevFailed;

	void setAcqMode(AcqMode mode) throws DevFailed;

	int getAcqNbFrames() throws DevFailed;
	/**
	 * set number of frames to be acquired. default is 1 frame.
	 * @param acqNbFrames
	 * @throws DevFailed
	 */
	void setAcqNbFrames(Integer acqNbFrames) throws DevFailed;

	enum AcqTriggerMode {
		INTERNAL_TRIGGER,// software trigger, start immediately after acqStart() called, all acq_nb_frames are acquired in a sequence 
		EXTERNAL_TRIGGER,//wait for an external trigger signal to start acquisition for the acq_nb_frames number of frames
		EXTERNAL_TRIGGER_MULTI,// wait for multiple external triggers, one for each frame of the acquisition
		INTERNAL_TRIGGER_MULTI,// software triggers need to call acqStart() for each frame
		EXTERNAL_GATE,//wait for a gate signal for each frame, the gate period is the exposure time
		EXTERNAL_START_STOP //???
	}

	AcqTriggerMode getAcqTriggerMode() throws DevFailed;

	void setAcqTriggerMode(AcqTriggerMode mode) throws DevFailed;
	/**
	 * return the latency time between 2 frame acquired.
	 * @return the latency time
	 * @throws DevFailed
	 */
	double getLatencyTime() throws DevFailed;
	/**
	 * set the latency time between 2 frames to be acquired. This cannot be zero, the minimum time is the readout time of the detector.
	 * @param latencyTime
	 * @throws DevFailed
	 */
	void setLatencyTime(double latencyTime) throws DevFailed;
	/**
	 * valid ranges for exposure and latency times as array of double in the format of 
	 * [min_exposure,max_exposure, min_latency, max_latency]
	 * @return [min_exposure,max_exposure, min_latency, max_latency]
	 * @throws DevFailed
	 */
	double[] getValidRanges() throws DevFailed;
	/**
	 * return the expsoure time of a image, default is 1 second.
	 * @return exposure time
	 * @throws DevFailed
	 */
	double getAcqExpoTime() throws DevFailed;
	/**
	 * set the exposure time of a image in the detector, default is 1 second.
	 * @param acqExpoTime
	 * @throws DevFailed
	 */
	void setAcqExpoTime(double acqExpoTime) throws DevFailed;
	/**
	 * return the effective accumulation total exposure time.
	 * @return effective accumulation time
	 * @throws DevFailed
	 */
	double getAccExpoTime() throws DevFailed;
	/**
	 * return the calculated accumulation number of frames per image. 
	 * @return the calculated accumulation number
	 * @throws DevFailed
	 */
	int getAccNbFrames() throws DevFailed;
	/**
	 * get the maximum exposure time per frame for accumulation.
	 * @return time
	 * @throws DevFailed
	 */
	double getAccMaxExpoTime() throws DevFailed;
	/**
	 * set the maximum exposure time per frame for accumulation.
	 * @param accMaxExpoTime
	 * @throws DevFailed
	 */
	void setAccMaxExpoTime(double accMaxExpoTime) throws DevFailed;

	enum AccTimeMode {
		LIVE, //acq_expo_time=acc_live_time, acc_dead_time is extra
		REAL  //acq_expo_time=acc_live_time + acc_dead_time
	}

	AccTimeMode getAccTimeMode() throws DevFailed;
	/**
	 * set the accumulation time mode.
	 * @param accTimeMode
	 * @throws DevFailed
	 */
	void setAccTimeMode(AccTimeMode accTimeMode) throws DevFailed;
	/**
	 * get the total accumulation dead time in a acquisition.
	 * @return total accumulation dead time
	 * @throws DevFailed
	 */
	double getAccDeadTime() throws DevFailed;
	/**
	 * get the total accumulation live time, which is the detector total counting time.
	 * @return total accumulation live time - the actual detector exposure time.
	 * @throws DevFailed
	 */
	double getAccLiveTime() throws DevFailed;

	enum ImageType {
		BPP8, BPP8S, BPP10, BPP10S, BPP12, BPP12S, BPP14, BPP14S, BPP16, BPP16S, BPP32, BPP32S
	}
	/**
	 * returns the current image data type - bit per pixel, signed or unsigned.
	 * @return {@link ImageType}
	 * @throws DevFailed
	 */
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
