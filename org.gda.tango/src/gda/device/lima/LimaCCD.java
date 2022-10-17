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
	/**
	 * Prepare the camera for a new acquisition, have to be called each time a parameter is set.
	 * @throws DevFailed
	 */
	void prepareAcq() throws DevFailed;
	/** 
	 * start acquisition
	 * @throws DevFailed
	 */
	void startAcq() throws DevFailed;
	/**
	 * stop acquisition
	 * @throws DevFailed
	 */
	void stopAcq() throws DevFailed;
	/** 
	 * set image header
	 * @param headers
	 * @throws DevFailed
	 */
	void setImageHeader(String[] headers) throws DevFailed;
	/**
	 * return image data in raw format - i.e. byte array
	 * @param imageNumber
	 * @return image data
	 * @throws DevFailed
	 */
	byte[] getImage(int imageNumber) throws DevFailed;
	/**
	 * close the shutter on  this call.
	 * @throws DevFailed
	 */
	void closeShutterManual() throws DevFailed;
	/**
	 * open shutter on this call.
	 * @throws DevFailed
	 */
	void closeOpenManual() throws DevFailed;
	/**
	 * reset the camera to factory setting
	 * @throws DevFailed
	 */
	void reset() throws DevFailed;
	/**
	 * get Lima camera type
	 * @return Frelon, Pilatus, ...
	 * @throws DevFailed
	 */
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
	/**
	 * the detector width in pixel
	 * @return width size in pixel
	 * @throws DevFailed
	 */
	long getImageWidth() throws DevFailed;
	/**
	 * the detector height in pixel
	 * @return height size in pixel
	 * @throws DevFailed
	 */
	long getImageHeight() throws DevFailed;
	/**
	 * return region of interest on image. default is no ROI [0,0,0,0]
	 * @return ROI
	 * @throws DevFailed
	 */
	LimaROIInt getImageROIInt() throws DevFailed;
	/** 
	 * set the region of interest, in the units of binning
	 * @param limaROIInt
	 * @throws DevFailed
	 */
	void setImageROIInt(LimaROIInt limaROIInt) throws DevFailed;
	/**
	 * get binning factor on image in [x-bin, y-bin]
	 * @return Lima bin
	 * @throws DevFailed
	 */
	LimaBin getImageBin() throws DevFailed;
	/**
	 * set the bin factors for a image.
	 * @param limaBin
	 * @throws DevFailed
	 */
	void setImageBin(LimaBin limaBin) throws DevFailed;
	/**
	 * set the bin factors for a image.
	 * @param xBinValue
	 * @param yBinValue
	 * @throws DeviceException
	 */
	void setImageBin(long xBinValue, long yBinValue) throws DeviceException;
	/**
	 * get flip on the image, default is false for both X and Y.
	 * @return lima flip
	 * @throws DevFailed
	 */
	LimaFlip getImageFlip() throws DevFailed;
	/**
	 * set flip on image, X, Y axis independent.
	 * @param limaFlip
	 * @throws DevFailed
	 */
	void setImageFlip(LimaFlip limaFlip) throws DevFailed;
	/**
	 * get the image rotation: "0", "90","180" or "270"
	 * @return "0", "90","180" or "270"
	 * @throws DevFailed
	 */
	String getImageRotation() throws DevFailed;
	/**
	 * set image rotation to "0", "90","180" or "270".
	 * @param degree
	 * @throws DevFailed
	 */
	void setImageRotation(int degree) throws DevFailed;

	enum ShutterMode {
		MANUAL, 
		AUTO_FRAME, // the output signal is activated for each individual frame of a sequence
		AUTO_SEQUENCE //the output signal is activated during the whole sequence
	}

	ShutterMode getShutterMode() throws DevFailed;

	void setShutterMode(ShutterMode shutterMode) throws DevFailed;
	/**
	 * returns the delay in second between the output shutter signal and the beginning of the acquisition,
	 * if not null the shutter signal is set on before the acquisition is started.
	 * @return the delay time
	 * @throws DevFailed
	 */
	double getShutterOpenTime() throws DevFailed;
	/**
	 * sets the delay in second between the output shutter signal and the beginning of the acquisition,
	 * if not null the shutter signal is set on before the acquisition is started.
	 * @param shutterOpenTime
	 * @throws DevFailed
	 */
	void setShutterOpenTime(double shutterOpenTime) throws DevFailed;
	
	/**
	 * returns the delay in second between the shutter signal and the end of the acquisition,
	 * if not null the shutter signal is set on before the end of the acquisition.
	 * @return the delay time
	 * @throws DevFailed
	 */
	double getShutterCloseTime() throws DevFailed;
	/**
	 * sets the delay in second between the shutter signal and the end of the acquisition,
	 * if not null the shutter signal is set on before the end of the acquisition.
	 * @param shutterCloseTime
	 * @throws DevFailed
	 */
	void setShutterCloseTime(double shutterCloseTime) throws DevFailed;
	/**
	 * get the open/close state of the shutter if manual mode is supported.
	 * @return open or close
	 * @throws DevFailed
	 */
	String getShutterManualState() throws DevFailed;
	/**
	 * set to open or close the shutter manually if the Manual mode is supported.
	 * @param value - open or close
	 * @throws DevFailed
	 */
	void setShutterManualState(String value) throws DevFailed;
	enum SavingMode {
		MANUAL, //no automatic saving, a command will be implemented in a next release to be able to save an acquired image 
		AUTO_FRAME, // frames are automatically saved according to the saving parameters
		AUTO_HEADER //frames are only saved when the setImageHeader() is called in order to set header information with image data.
	}
	/** 
	 * returns the current saving mode setting.
	 * @return saving mode
	 * @throws DevFailed
	 */
	SavingMode getSavingMode() throws DevFailed;
	/**
	 * sets the saving mode
	 * @param savingMode
	 * @throws DevFailed
	 */
	void setSavingMode(SavingMode savingMode) throws DevFailed;
	/**
	 * return the current directory where the image files are saved.
	 * @return image data directory
	 * @throws DevFailed
	 */
	String getSavingDirectory() throws DevFailed;
	/**
	 * sets the the directory where to save the image files.
	 * @param savingDirectory
	 * @throws DevFailed
	 */
	void setSavingDirectory(String savingDirectory) throws DevFailed;
	/**
	 * get the current image file prefix.
	 * @return file prefix
	 * @throws DevFailed
	 */
	String getSavingPrefix() throws DevFailed;

	/**
	 * sets the image file prefix to be saved next.
	 * 
	 * Changing savingPrefix will cause the saving_next_number to be set to 0. You then need to change the
	 * saving_next_number to 1 to get the value to increment. Fails if the prefix has already been used - if files exist
	 * which match
	 *
	 * @param savingPrefix
	 * @throws DevFailed
	 */
	void setSavingPrefix(String savingPrefix) throws DevFailed;
	/**
	 * gets the current image file suffix
	 * @return the image file suffix
	 * @throws DevFailed
	 */
	String getSavingSuffix() throws DevFailed;

	/**
	 * The suffix is set implicitly by setting the SavingFormat e.g. EDF = .edf 
	 * 
	 * @param savingSuffix
	 * @throws DevFailed
	 */
	void setSavingSuffix(String savingSuffix) throws DevFailed;
	/**
	 * gets the next number for the image to be saved
	 * @return the next svae number
	 * @throws DevFailed
	 */
	int getSavingNextNumber() throws DevFailed;

	/**
	 * sets the next saving number.
	 * 
	 * Changing savingPrefix will cause the saving_next_number to be set to 0. You then need to change the
	 * saving_next_number to 1 to get the value to increment.
	 *
	 * @param savingNextNumber
	 * @throws DevFailed
	 */
	void setSavingNextNumber(int savingNextNumber) throws DevFailed;

	enum SavingFormat {
		RAW, //save in binary format
		EDF, //save in ESRF data format
		CBF //save in CBF format (a compressed format for crystallography)
	}
	/**
	 * gets the current data format for saving image files
	 * @return data format
	 * @throws DevFailed
	 */
	SavingFormat getSavingFormat() throws DevFailed;
	/**
	 * sets the data format for saving image files
	 * 
	 * @param savingFormat
	 * @throws DevFailed
	 */
	void setSavingFormat(SavingFormat savingFormat) throws DevFailed;
	/**
	 * In case of existing files, an overwrite policy is mandatory
	 */
	enum SavingOverwritePolicy {
		ABORT, // if the file exists, the saving is aborted
		OVERWRITE, //if the file exists, it is overwritten
		APPEND //if the file exists, the image is appended to the file.
	}
	/**
	 * get current saving  overwrite policy
	 * @return saving overwrite policy
	 * @throws DevFailed
	 */
	SavingOverwritePolicy getSavingOverwritePolicy() throws DevFailed;
	/**
	 * sets the saving overwrite policy
	 * @param savingOverwritePolicy
	 * @throws DevFailed
	 */
	void setSavingOverwritePolicy(SavingOverwritePolicy savingOverwritePolicy) throws DevFailed;
	/**
	 * get the current number of frames to be saved in a single file
	 * @return frames per file
	 * @throws DevFailed
	 */
	int getSavingFramePerFile() throws DevFailed;
	/**
	 * set the number of frames permitted to save in a single file
	 * @param savingFramePerFile
	 * @throws DevFailed
	 */
	void setSavingFramePerFile(int savingFramePerFile) throws DevFailed;
	/**
	 * get common header with multiple entries
	 * @return common header
	 * @throws DevFailed
	 */
	String[] getSavingCommonHeader() throws DevFailed;
	/**
	 * set the common header with multiple entries
	 * @param savingCommonHeader
	 * @throws DevFailed
	 */
	void setSavingCommonHeader(String[] savingCommonHeader) throws DevFailed;
	/**
	 * get the current saving header delimiter
	 * @return LimaSavingHeaderDelimiter, default is ["=", "n", ";"]
	 * @throws DevFailed
	 */
	LimaSavingHeaderDelimiter getLimaSavingHeaderDelimiter() throws DevFailed;
	/**
	 * set the saving header delimiter for key, entry and image number
	 * @param limaSavingHeaderDelimiter
	 * @throws DevFailed
	 */
	void setLimaSavingHeaderDelimiter(LimaSavingHeaderDelimiter limaSavingHeaderDelimiter) throws DevFailed;
	/**
	 * return the last acquired image number, ready for reading
	 * @return 0 is ready for reading, -1 if not
	 * @throws DevFailed
	 */
	int getLastImageReady() throws DevFailed;

	/**
	 * the last saved image number 
	 * @return 0 is saved OK, -1 if not
	 * @throws DevFailed
	 */
	int getLastImageSaved() throws DevFailed;
	/**
	 * query if camera ready for next image
	 * Can be used for fast synchronization with trigger mode (internal or external).
	 * 
	 * @return True after a camera readout, otherwise false.
	 * @throws DevFailed
	 */
	boolean getReadyForNextImage() throws DevFailed;
	/**
	 * query if ready for the next acquisition
	 * @return true after the end of acquisition, otherwise false
	 * @throws DevFailed
	 */
	boolean getReadyForNextAcq() throws DevFailed;
	/**
	 * get the performance writing time for the last images in seconds.
	 * @return writing time.
	 * @throws DevFailed
	 */
	double[] getWriteStatistics() throws DevFailed;
	
	String[] getDebugModules() throws DevFailed;

	void setDebugModules(String[] debugModules) throws DevFailed;

	String[] getDebugTypes() throws DevFailed;

	void setDebugTypes(String[] debugTypes) throws DevFailed;
	int getLastCounterReady() throws DevFailed;
	int getLastImageAcquired() throws DevFailed;

}
