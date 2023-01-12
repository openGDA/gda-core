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

package gda.device.detector;

import gda.device.detector.areadetector.v17.ADBase;
import gda.device.detector.areadetector.v17.FfmpegStream;
import gda.device.detector.areadetector.v17.NDArray;
import gda.device.detector.areadetector.v17.NDFile;
import gda.device.detector.areadetector.v17.NDFileHDF5;
import gda.device.detector.areadetector.v17.NDFileNexus;
import gda.device.detector.areadetector.v17.NDOverlay;
import gda.device.detector.areadetector.v17.NDProcess;
import gda.device.detector.areadetector.v17.NDROI;
import gda.device.detector.areadetector.v17.NDStats;
import gda.device.detector.pco.PCOControllerV17.TriggerMode;
import gda.factory.Findable;
import gov.aps.jca.CAException;
import gov.aps.jca.TimeoutException;

/**
 * Interface defining the PCO controller that respects v1.7
 */
public interface IPCOControllerV17 extends Findable {
	// Getters and Setters for Spring
	ADBase getAreaDetector();

	String getBasePVName();

	/**
	 * reset the camera and all assciated plugins - enable NDArray callback, stop any acquiring, set acquisition
	 * parameters, initialise plugins' array dimensions
	 * 
	 * @throws Exception
	 */
	void resetAll() throws Exception;

	/**
	 * to provide a software simulated/controlled trigger signal to the detector trigger input
	 * 
	 * @throws Exception
	 */
	void trigger() throws Exception;

	/**
	 * stop camera acquiring, disarm the camera (automatically called by EPICS)
	 * 
	 * @throws Exception
	 */
	void stop() throws Exception;

	FfmpegStream getMJpeg1();

	FfmpegStream getMJpeg2();

	int getADCMode() throws TimeoutException, CAException, InterruptedException, Exception;

	int getPixRate() throws TimeoutException, CAException, InterruptedException, Exception;

	/**
	 * returns the readout time for one ADC at 8mHz
	 * 
	 * @return readout time
	 */
	int getReadout1ADC8Mhz();

	/**
	 * returns the readout time for one ADC at 32mHz
	 * 
	 * @return readout time
	 */
	int getReadout1ADC32Mhz();

	/**
	 * returns the readout time for two ADC at 32mHz
	 * 
	 * @return readout time
	 */
	int getReadout2ADC32Mhz();

	/**
	 * returns the readout time for two ADC at 8mHz
	 * 
	 * @return readout time
	 */
	int getReadout2ADC8Mhz();

	/**
	 * set the image mode for acquisition: - Single, Multiple, or Continuous when camera is disarmed.
	 * 
	 * @param imageMode
	 * @throws Exception
	 */
	void setImageMode(int imageMode) throws Exception;

	/**
	 * start to acquire image
	 * 
	 * @throws Exception
	 */
	void acquire() throws Exception;

	NDFile getTiff();

	/**
	 * returns the number of exposures set in EPICS
	 * 
	 * @return number of exposures
	 * @throws Exception
	 */
	int getExposures() throws Exception;

	NDFileNexus getNxs();

	NDOverlay getDraw();

	NDStats getStat();

	NDProcess getProc1();

	NDProcess getProc2();

	NDROI getRoi1();

	NDROI getRoi2();

	NDArray getArray();

	/**
	 * set camera ADC mode - one ADC or two ADC when camera is disarmed
	 * 
	 * @param value
	 * @throws Exception
	 */
	void setADCMode(int value) throws Exception;

	/**
	 * sets camera pixel rate - 32mHz or 8mHz when camera is disarmed
	 * 
	 * @param value
	 * @throws Exception
	 */
	void setPixRate(int value) throws Exception;

	double getCamRamUsage() throws Exception;

	double getElectronicTemperature() throws Exception;

	double getPowerSupplyTemperature() throws Exception;

	int getStorageMode() throws Exception;

	/**
	 * set camera storage mode - recorder or FIFO buffer, when camera id disarmed
	 * 
	 * @param value
	 * @throws Exception
	 */
	void setStorageMode(int value) throws Exception;

	int getRecorderMode() throws Exception;

	/**
	 * set camera recorder mode - Sequence or Ring buffer when camera id disarmed
	 * 
	 * @param value
	 * @throws Exception
	 */
	void setRecorderMode(int value) throws Exception;

	int getTimestampMode() throws Exception;

	/**
	 * sets camera timestamp - None, BCD, BCD+ASCII, or ACSII, when camera is disarmed
	 * 
	 * @param value
	 * @throws Exception
	 */
	void setTimestampMode(int value) throws Exception;

	/**
	 * set camera acquire mode - Auto, Ext. enable, or Ext. trigger when camera id disarmed
	 * 
	 * @param value
	 * @throws Exception
	 */
	void setAcquireMode(int value) throws Exception;

	int getAcquireMode() throws Exception;

	int getArmMode() throws Exception;

	void setArmMode(int value) throws Exception;

	double getDelayTime() throws Exception;

	/**
	 * set camera delay time when camera is disarmed
	 * 
	 * @param value
	 * @throws Exception
	 */
	void setDelayTime(double value) throws Exception;

	/**
	 * sets number of image to collect per point or step when camera is disarmed
	 * 
	 * @param numimages
	 * @throws Exception
	 */
	void setNumImages(int numimages) throws Exception;

	NDFileHDF5 getHdf();

	int getNumImages() throws Exception;

	int getNumCaptured() throws Exception;

	String getFullFileName() throws Exception;

	/**
	 * sets camera trigger mode - Auto, Soft, Ext + Soft, or Ext Pulse when camera is disarmed.
	 * 
	 * @param value
	 * @throws Exception
	 */
	void setTriggerMode(TriggerMode value) throws Exception;

	TriggerMode getTriggerMode();

	void listTriggerModes();

	/**
	 * sets camera trigger mode - 0-Auto, 1-Soft, 2-Ext + Soft, or 3-Ext Pulse when camera is disarmed.
	 * 
	 * @param value
	 * @throws Exception
	 */
	void setTriggerMode(int value) throws Exception;

	void disableTiffSaver() throws Exception;

	void enableTiffSaver() throws Exception;

	void disableHdfSaver() throws Exception;

	void enableHdfSaver() throws Exception;

	int getReadoutTime() throws Exception;

	void setScanDimensions(int[] dimensions) throws Exception;

	void startRecording() throws Exception;

	String getHDFFileName() throws Exception;

	void endRecording() throws Exception;

	String getTiffFullFileName() throws Exception;

	int getNextFileNumber() throws Exception;

	void armCamera() throws Exception;

	void disarmCamera() throws Exception;

	boolean isArmed() throws Exception;

	void makeDetectorReadyForCollection() throws Exception;

	void setTriggerPV(String triggerPV);

	String getTriggerPV();
}
