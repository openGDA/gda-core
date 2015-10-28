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

package gda.device.detector.addetector.triggering;

import gda.device.DeviceException;
import gda.device.detector.areadetector.v17.ADBase;
import gda.device.detector.areadetector.v17.ADBase.StandardTriggerMode;
import gda.device.detector.areadetector.v17.ImageMode;
import gda.device.detector.areadetector.v17.NDFile;
import gda.device.detector.areadetector.v17.NDProcess;
import gda.scan.ScanInformation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//TODO: This class is far specific than its generic name suggests. See MultipleExposurePerCollectionStandard
/*
 * Implementation of ADTriggeringStrategy that is used when multiple exposures are to be taken per
 * acquisition.
 * This can be required when the exposure time defined in a scan is greater than the maximum exposure time of
 * the camera. In which case several images are taken.
 *
 * If ndProcess is supplied this is configured to sum the images together to that the output of the combined system
 * is a single image per collection

 */
public class MultipleExposureSoftwareTriggerAutoMode extends AbstractADTriggeringStrategy {
	private static final Logger logger = LoggerFactory.getLogger(MultipleExposureSoftwareTriggerAutoMode.class);

	/**
	 * maxExposureTime  - maximum exposure time(s) that the camera is capable of.
	 */
	double maxExposureTime=Double.MAX_VALUE;

	/**
	 * exposureTime  - exposure time(s) that the camera is run at.
	 *
	 * If GDA Detector collection time > exposureTime then multiple exposures are taken and added together using the NDProcess plugin
	 */
	double exposureTime=maxExposureTime;

	int numberImagesPerCollection=1;
	NDProcess ndProcess=null;

	//If the detector runs continuously we have to use the tiff file to grab the images
	NDFile ndFile=null;

	/**
	 * To allow for detectors with prefix acquire give option to read it at prepareForCollection
	 */
	private boolean readAcquireTimeFromHardware=false;

	/**
	 * If we disable the MultipleExposure summing process after collection then anything which relies on it, such as the array
	 * plugin, used for the array view, will stop getting images, so this option leaves the plugin running after data collection.
	 */
	private boolean filterEnabledAfterCollection = false;

	private int procDataTypeOut=5; // UINT32

	public int getProcDataTypeOut() {
		return procDataTypeOut;
	}

	public void setProcDataTypeOut(int procDataTypeOut) {
		this.procDataTypeOut = procDataTypeOut;
	}

	public boolean isReadAcquireTimeFromHardware() {
		return readAcquireTimeFromHardware;
	}

	public void setReadAcquireTimeFromHardware(boolean readAcquireTimeFromHardware) {
		this.readAcquireTimeFromHardware = readAcquireTimeFromHardware;
	}

	public MultipleExposureSoftwareTriggerAutoMode(ADBase adBase, double maxExposureTime) {
		setAdBase(adBase);
		this.maxExposureTime = maxExposureTime;
		exposureTime = maxExposureTime;
	}

	public MultipleExposureSoftwareTriggerAutoMode(ADBase adBase) {
		setAdBase(adBase);
		exposureTime = this.maxExposureTime;
	}

	public NDProcess getNdProcess() {
		return ndProcess;
	}

	public void setNdProcess(NDProcess ndProcess) {
		this.ndProcess = ndProcess;
	}

	public NDFile getNdFile() {
		return ndFile;
	}

	public void setNdFile(NDFile ndFile) {
		this.ndFile = ndFile;
	}

	public double getMaxExposureTime() {
		return maxExposureTime;
	}

	public void setMaxExposureTime(double maxExposureTime) {
		this.maxExposureTime = maxExposureTime;
	}

	public double getExposureTime() throws Exception {
		return readAcquireTimeFromHardware ? getAdBase().getAcquireTime_RBV() : exposureTime;
	}

	public void setExposureTime(double exposureTime) {
		if( exposureTime > maxExposureTime)
			throw new IllegalArgumentException("Unable to set exposure time to " + exposureTime + ". max value = " + maxExposureTime);
		this.exposureTime = exposureTime;
	}


	@Override
	public void prepareForCollection(double collectionTime, int numImagesIgnored, ScanInformation scanInfo) throws Exception {
		double localExposureTime = ndProcess != null ? getExposureTime() : collectionTime;
		numberImagesPerCollection = calcNumberImagesPerCollection(collectionTime, localExposureTime);
		getAdBase().setTriggerMode(StandardTriggerMode.INTERNAL.ordinal());
		if( getNdFile() != null){
			getAdBase().setImageMode(ImageMode.CONTINUOUS.ordinal());
			getAdBase().setNumImages(1);
		} else {
			getAdBase().stopAcquiring();
			getAdBase().setNumImages(numberImagesPerCollection);
			getAdBase().setImageModeWait(numberImagesPerCollection > 1 ? ImageMode.MULTIPLE : ImageMode.SINGLE);
		}
		if( !readAcquireTimeFromHardware)
			getAdBase().setAcquireTime(numberImagesPerCollection > 1 ?localExposureTime : collectionTime);


		if( ndProcess != null){
			ndProcess.setFilterType(NDProcess.FilterTypeV1_8_Sum);
			ndProcess.setNumFilter(numberImagesPerCollection);
			ndProcess.setAutoResetFilter(1);
			ndProcess.setFilterCallbacks(NDProcess.FilterCallback_ArrayNOnly);
			ndProcess.setEnableFilter(1);
			ndProcess.setEnableHighClip(0);
			ndProcess.setEnableLowClip(0);
			ndProcess.setEnableOffsetScale(0);
			ndProcess.setEnableFlatField(0);
			ndProcess.setEnableBackground(0);
			ndProcess.getPluginBase().setArrayCounter(0);
			ndProcess.getPluginBase().setDroppedArrays(0);
			ndProcess.setDataTypeOut(procDataTypeOut);
			ndProcess.getPluginBase().disableCallbacks();
		}
		enableOrDisableCallbacks();
	}

	@Override
	public void completeCollection() throws Exception {
		if( getNdFile() == null){
			getAdBase().stopAcquiring();
			getAdBase().setImageModeWait(ImageMode.SINGLE);
			getAdBase().setNumImages(1);
		}
		if( ndProcess != null && !filterEnabledAfterCollection){
			// Set it to what it was already set to in prepareForCollection!
			ndProcess.setFilterType(NDProcess.FilterTypeV1_8_Sum);
			// Disable plugin and zero out filter, so we can't continue to use it!
			ndProcess.setNumFilter(1);
			ndProcess.getPluginBase().disableCallbacks(); // Disable callbacks, so we can't continue to use it!
		}
	}

	@Override
	public double getAcquireTime() throws Exception {
		return getAdBase().getAcquireTime_RBV()*numberImagesPerCollection;
	}

	@Override
	public double getAcquirePeriod() throws Exception {
		return getAdBase().getAcquirePeriod_RBV()*numberImagesPerCollection;
	}

	@Override
	public void collectData() throws Exception {
		double acquirePeriod_RBV = getAdBase().getAcquirePeriod_RBV();
		if( getNdFile() != null){
			if( getAdBase().getAcquireState() != 1){
				getAdBase().startAcquiring();
			}
			//when running continuously we need to allow the current frame ( that started before now) to get out of the camera.
			//this means waiting ofr the exposure time
			Thread.sleep((long) (acquirePeriod_RBV * 1000));
			//if use proc we now reset the filter
			if( ndProcess != null){
				ndProcess.setResetFilter(1);
				ndProcess.getPluginBase().enableCallbacks();
				// autoreset only works in numFiltered== numFilter which is not the case as we have just reset numFilter
			}
			getNdFile().startCapture();
		} else {
			if( ndProcess != null){
				ndProcess.setResetFilter(1);
				ndProcess.getPluginBase().enableCallbacks();
				// autoreset only works in numFiltered== numFilter which is not the case as we have just reset numFilter
			}
			getAdBase().startAcquiring();
		}
	}

	@Override
	public int getStatus() throws DeviceException {
		if (getNdFile() != null ){
			return getNdFile().getStatus();
		}
		return getAdBase().getStatus();
	}

	@Override
	public void waitWhileBusy() throws InterruptedException, DeviceException {
		if( getNdFile() != null){
			getNdFile().waitWhileStatusBusy();
		}else {
			getAdBase().waitWhileStatusBusy();
		}
	}

	@Override
	public void stop() throws Exception {
		completeCollection();
	}

	@Override
	public void atCommandFailure() throws Exception {
		completeCollection();
	}

	/**
	 * @see gda.device.detector.nxdetector.NXCollectionStrategyPlugin#getNumberImagesPerCollection
	 */
	protected int calcNumberImagesPerCollection(double collectionTime, double exposureTime) {
		if (collectionTime > exposureTime)
			// This rounds off! Which means that we could acquire for less time than is requested!
			// For example, if we have a exposureTime of 8s and request an 18s collectionTime, we get just 16s!
			//return (int)(collectionTime/exposureTime + 0.5);
			// We really should just round up!
			return (int)Math.ceil(collectionTime/exposureTime);
		return 1;
	}

	@Override
	public int getNumberImagesPerCollection(double collectionTime){
		try{
			double localExposureTime = getExposureTime();
			return ndProcess == null ? calcNumberImagesPerCollection(collectionTime, localExposureTime) : 1;
		} catch (Exception e){
			throw new IllegalArgumentException("Error in getNumberImagesPerCollection",e);
		}
	}

	public boolean isFilterEnabledAfterCollection() {
		return filterEnabledAfterCollection;
	}

	public void setFilterEnabledAfterCollection(boolean filterEnabledAfterCollection) {
		this.filterEnabledAfterCollection = filterEnabledAfterCollection;
		logger.info("filterEnabledAfterCollection=" + filterEnabledAfterCollection);
	}
}
