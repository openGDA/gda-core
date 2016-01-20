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

import gda.device.DeviceException;
import gda.device.detector.Mythen;
import gda.device.detector.mythen.data.MythenDataFileUtils.FileType;
import gda.device.detector.mythen.tasks.DataProcessingTask;
import gda.factory.FactoryException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public interface IMythenDetector extends Mythen {

	public abstract void configure() throws FactoryException;

	public abstract void afterPropertiesSet() throws Exception;

	@Override
	public abstract void atScanStart() throws DeviceException;

	@Override
	public abstract void setCollectionTime(double collectionTime) throws DeviceException;

	public abstract String buildFilename(String s, FileType type);

	/**
	 * rebuild Raw data file name added for Jython use as Jython does not support enum Java type yet.
	 * @param number
	 * @return filename
	 */
	public abstract String buildRawFilename(int number);

	/**
	 * collect data from detector using EPICS client.
	 * This method is non-blocking.
	 */
	@Override
	public abstract void collectData() throws DeviceException;

	@Override
	public abstract void stop() throws DeviceException;

	@Override
	public abstract Object readout() throws DeviceException;

	@Override
	public abstract void atScanEnd() throws DeviceException;

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
	public abstract void multi(int numFrames, double delayTime, double exposureTime, double delayAfterFrames)
			throws DeviceException;

	/**
	 * Captures multiple frames using a software trigger.
	 * @param numFrames
	 * @param delayTime
	 * @param exposureTime
	 * @throws DeviceException
	 */
	public abstract void multi(int numFrames, double delayTime, double exposureTime) throws DeviceException;

	/**
	 * Captures multiple frames using a single trigger to start acquisition of all frames.
	 * @param numFrames
	 * @param delayTime
	 * @param exposureTime
	 * @throws DeviceException
	 */
	public abstract void smulti(int numFrames, double delayTime, double exposureTime) throws DeviceException;

	/**
	 * Captures multiple frames using one trigger per frame.
	 * @param numCycles
	 * @param numFrames
	 * @param delayTime
	 * @param exposureTime
	 * @throws DeviceException
	 */
	public abstract void cmulti(int numCycles, int numFrames, double delayTime, double exposureTime)
			throws DeviceException;

	/**
	 * MYTHEN 2 API no longer supported. Please use alternative {@link #cmulti(int, int, double, double)}.
	 * @param numFrames
	 * @param delayTime
	 * @param exposureTime
	 * @throws DeviceException
	 */
	public abstract void cmulti(int numFrames, double delayTime, double exposureTime) throws DeviceException;

	/**
	 * gated multiple frames collection - one file per frame, numGates per frames
	 * @param numFrames - the number of frames to collect
	 * @param numGates - the number of gates per frame to expose
	 */
	public abstract void gated(int numFrames, int numGates) throws DeviceException;

	/**
	 * gated single frame collection.
	 * @param numGates the number of gates to expose.
	 */
	public abstract void gated(int numGates) throws DeviceException;

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
	public abstract void gated(int numFrames, int numGates, long scanNumber, File dataDirectory, int collectionNumber)
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
	public abstract void gated(int numFrames, int numGates, long scanNumber) throws DeviceException;

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
	public abstract void gated(int numFrames, int numGates, long scanNumber, int collectionNumber)
			throws DeviceException;

	public abstract MythenEpicsClient getMythenClient();

	public abstract void setMythenClient(MythenEpicsClient mythenClient);

	public abstract void autoMode() throws Exception;

	public abstract void triggerMode() throws Exception;

	public abstract void gatingMode() throws Exception;

	public abstract void ro_TriggerMode() throws Exception;

	public abstract void triggerredGatingMode() throws Exception;

	public abstract void setTriggerMode(int value) throws Exception;

	public abstract void setThreshold(double energy) throws Exception;

	public abstract double getThreshold() throws Exception;

	public abstract void setBeamEnergy(double energy) throws Exception;

	public abstract double getBeamEnergy() throws Exception;

	public abstract void standard() throws Exception;

	public abstract void fast() throws Exception;

	public abstract void highgain() throws Exception;

	public abstract void setBitDepth(int bitDepth) throws Exception;

	public abstract int getBitDepth() throws Exception;

	public abstract void setNumCycles(int value) throws Exception;

	public abstract int getNumCycles() throws Exception;

	public abstract void setNumFrames(int value) throws Exception;

	public abstract int getNumFrames() throws Exception;

	public abstract void setNumGates(int value) throws Exception;

	public abstract int getNumGates() throws Exception;

	public abstract void setDelayTime(double value) throws Exception;

	public abstract double getDelayTime() throws Exception;

	public abstract void setFilePath(String value) throws IOException;

	public abstract String getFilePath() throws Exception;

	public abstract void setFileName(String value) throws IOException;

	public abstract String getFileName() throws Exception;

	public abstract void setNextFileNumber(int value) throws IOException;

	public abstract int getNextFileNumber() throws Exception;

	public abstract void enableAutoIncrement() throws IOException;

	public abstract void disableAutoIncrement() throws IOException;

	public abstract boolean isAutoIncrement() throws IOException;

	public abstract void enableAutoSave() throws IOException;

	public abstract void disableAutoSave() throws IOException;

	public abstract boolean isAutoSave() throws IOException;

	public abstract void setFileTemplate(String value) throws IOException;

	public abstract String getFileTemplate() throws Exception;

	public abstract String getFullFileName() throws Exception;

	public abstract void setFlatFieldPath(String value) throws IOException;

	public abstract String getFlatFieldPath() throws Exception;

	public abstract void setFlatFieldFile(String value) throws IOException;

	public abstract String getFlatFieldFile() throws Exception;

	public abstract void enableFlatFieldCorrection() throws IOException;

	public abstract void disableFlatFieldCorrection() throws IOException;

	public abstract boolean isFlatFieldCorrected() throws IOException;

	public abstract void enableCountRateCorrection() throws IOException;

	public abstract void disableCountRateCorrection() throws IOException;

	public abstract boolean isCountRateCorrected() throws IOException;

	public abstract void enableBadChannelCorrection() throws IOException;

	public abstract void disableBadChannelCorrection() throws IOException;

	public abstract boolean isBadChannelCorrected() throws IOException;

	public abstract void enableAngularConversion() throws IOException;

	public abstract void disableAngularConversion() throws IOException;

	public abstract boolean isAngularConversionEnabled() throws IOException;

	public abstract void setConfigFile(String value) throws IOException;

	public abstract void loadConfigFile() throws IOException;

	public abstract void saveConfigFile() throws IOException;

	public abstract void setExposureTime(double exposureTime) throws Exception;

	public abstract double getExposureTime() throws Exception;

	public abstract void setAcquirePeriod(double acquireperiod) throws Exception;

	public abstract double getAcquirePeriod() throws Exception;

	public abstract void startWait() throws DeviceException;

	public abstract ArrayList<File> getProcessedDataFilesForThisScan();

	public abstract int getNumberOfModules();

	public abstract List<DataProcessingTask> getProcessingTasks();

	public abstract void setProcessingTasks(List<DataProcessingTask> processingTasks);

}