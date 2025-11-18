/*-
 * Copyright © 2015 Diamond Light Source Ltd.
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

package uk.ac.gda.devices.mythen.epics;

import java.io.File;
import java.io.IOException;
import java.util.List;

import gda.device.DeviceException;
import gda.device.detector.Mythen;
import gda.device.detector.mythen.data.MythenDataFileUtils.FileType;
import gda.device.detector.mythen.tasks.DataProcessingTask;

public interface IMythenDetector extends Mythen {

	void afterPropertiesSet() throws Exception;

	String buildFilename(String s, FileType type);

	/**
	 * rebuild Raw data file name added for Jython use as Jython does not support enum Java type yet.
	 * @param number
	 * @return filename
	 */
	String buildRawFilename(int number);

	//############### special methods for multiple frames, triggered, gated collections
	/**
	 * Captures multiple frames using a software trigger.
	 * Deprecated, please use {@link #multi(int, double, double)}.
	 * @param numFrames
	 * @param delayTime
	 * @param exposureTime
	 * @param delayAfterFrames - redundant input kept only for backward compatibility.
	 * @throws DeviceException
	 */
	void multi(int numFrames, double delayTime, double exposureTime, double delayAfterFrames)
			throws DeviceException;

	/**
	 * Captures multiple frames using a software trigger.
	 * @param numFrames
	 * @param delayTime
	 * @param exposureTime
	 * @throws DeviceException
	 */
	void multi(int numFrames, double delayTime, double exposureTime) throws DeviceException;

	/**
	 * Captures multiple frames using a single trigger to start acquisition of all frames.
	 * @param numFrames
	 * @param delayTime
	 * @param exposureTime
	 * @throws DeviceException
	 */
	void smulti(int numFrames, double delayTime, double exposureTime) throws DeviceException;

	/**
	 * Captures multiple frames using one trigger per frame.
	 * @param numCycles
	 * @param numFrames
	 * @param delayTime
	 * @param exposureTime
	 * @throws DeviceException
	 */
	void cmulti(int numCycles, int numFrames, double delayTime, double exposureTime)
			throws DeviceException;

	/**
	 * MYTHEN 2 API no longer supported. Please use alternative {@link #cmulti(int, int, double, double)}.
	 * @param numFrames
	 * @param delayTime
	 * @param exposureTime
	 * @throws DeviceException
	 */
	void cmulti(int numFrames, double delayTime, double exposureTime) throws DeviceException;

	/**
	 * gated multiple frames collection - one file per frame, numGates per frames
	 * @param numFrames - the number of frames to collect
	 * @param numGates - the number of gates per frame to expose
	 */
	void gated(int numFrames, int numGates) throws DeviceException;

	/**
	 * gated single frame collection.
	 * @param numGates the number of gates to expose.
	 */
	void gated(int numGates) throws DeviceException;

	/**
	 * gated multiple frames collection - one frame per file, numGates per frame, single cycle only
	 * Mythen detector controls the frame number increment starting from 0.
	 * exposure time is controlled by gate signal length
	 * Delay time = 0,
	 * this acquisition waits for data correction and angular conversion to complete.
	 *
	 * @param numFrames Number of frames to collects, i.e. number of data files to create
	 * @param numGates Number of gates for each frame
	 * @param scanNumber this acquisition number
	 * @param dataDirectory the data directory to save data to
	 * @param collectionNumber the index number for this acquisition - if <0 Mythen detector controls the frame number increment starting from 1.
	 * @throws DeviceException
	 */
	void gated(int numFrames, int numGates, long scanNumber, File dataDirectory, int collectionNumber)
			throws DeviceException;

	/**
	 * gated multiple frames collection - one frame per file, numGates per frame, only one cycle -
	 * Mythen detector controls the frame number increment starting from 0.
	 * exposure time is controlled by gate signal length
	 * Delay time = 0,
	 * does not wait for data correction and angular conversion, returns immediately after RAW data are collected.
	 *
	 * @param numFrames Number of frames to collects, i.e. number of data files to create
	 * @param numGates Number of gates for each frame
	 * @param scanNumber this acquisition number
	 * @throws DeviceException
	 */
	void gated(int numFrames, int numGates, long scanNumber) throws DeviceException;

	/**
	 * gated multiple or single frame collection - one frame per file, numGates per frame - where GDA controls the collection
	 * number increment. This acquisition does not wait for data correction and angular conversion to complete before return.
	 *
	 * @param numFrames
	 * @param numGates
	 * @param scanNumber
	 * @param collectionNumber
	 * @throws DeviceException
	 */
	void gated(int numFrames, int numGates, long scanNumber, int collectionNumber)
			throws DeviceException;

	MythenEpicsClient getMythenClient();

	void setMythenClient(MythenEpicsClient mythenClient);

	void autoMode() throws Exception;

	void triggerMode() throws Exception;

	void gatingMode() throws Exception;

	void ro_TriggerMode() throws Exception;

	void triggerredGatingMode() throws Exception;

	void setTriggerMode(int value) throws Exception;

	void setThreshold(double energy) throws Exception;

	double getThreshold() throws Exception;

	void setBeamEnergy(double energy) throws Exception;

	double getBeamEnergy() throws Exception;

	void standard() throws Exception;

	void fast() throws Exception;

	void highgain() throws Exception;

	void setBitDepth(int bitDepth) throws Exception;

	int getBitDepth() throws Exception;

	void setNumCycles(int value) throws Exception;

	int getNumCycles() throws Exception;

	void setNumFrames(int value) throws Exception;

	int getNumFrames() throws Exception;

	void setNumGates(int value) throws Exception;

	int getNumGates() throws Exception;

	void setDelayTime(double value) throws Exception;

	double getDelayTime() throws Exception;

	void setFilePath(String value) throws IOException;

	String getFilePath() throws Exception;

	void setFileName(String value) throws IOException;

	String getFileName() throws Exception;

	void setNextFileNumber(int value) throws IOException;

	int getNextFileNumber() throws Exception;

	void enableAutoIncrement() throws IOException;

	void disableAutoIncrement() throws IOException;

	boolean isAutoIncrement() throws IOException;

	void enableAutoSave() throws IOException;

	void disableAutoSave() throws IOException;

	boolean isAutoSave() throws IOException;

	void setFileTemplate(String value) throws IOException;

	String getFileTemplate() throws Exception;

	String getFullFileName() throws Exception;

	void setFlatFieldPath(String value) throws IOException;

	String getFlatFieldPath() throws Exception;

	void setFlatFieldFile(String value) throws IOException;

	String getFlatFieldFile() throws Exception;

	void enableFlatFieldCorrection() throws IOException;

	void disableFlatFieldCorrection() throws IOException;

	boolean isFlatFieldCorrected() throws IOException;

	void enableCountRateCorrection() throws IOException;

	void disableCountRateCorrection() throws IOException;

	boolean isCountRateCorrected() throws IOException;

	void enableBadChannelCorrection() throws IOException;

	void disableBadChannelCorrection() throws IOException;

	boolean isBadChannelCorrected() throws IOException;

	void enableAngularConversion() throws IOException;

	void disableAngularConversion() throws IOException;

	boolean isAngularConversionEnabled() throws IOException;

	void setConfigFile(String value) throws IOException;

	void loadConfigFile() throws IOException;

	void saveConfigFile() throws IOException;

	void setExposureTime(double exposureTime) throws Exception;

	double getExposureTime() throws Exception;

	void setAcquirePeriod(double acquireperiod) throws Exception;

	double getAcquirePeriod() throws Exception;

	void startWait() throws DeviceException;

	List<File> getProcessedDataFilesForThisScan();

	int getNumberOfModules();

	List<DataProcessingTask> getProcessingTasks();

	void setProcessingTasks(List<DataProcessingTask> processingTasks);
}