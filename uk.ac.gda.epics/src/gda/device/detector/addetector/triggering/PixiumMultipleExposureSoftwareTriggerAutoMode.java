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
import gda.device.detector.areadetector.v17.ADBase.ImageMode;
import gda.device.detector.areadetector.v17.ADBase.StandardTriggerMode;
import gda.device.detector.areadetector.v17.NDPluginBase;
import gda.device.detector.areadetector.v17.NDProcess;
import gda.epics.LazyPVFactory;
import gda.epics.PV;
import gda.scan.ScanInformation;

import java.io.IOException;

/*
 * Implementation of ADTriggeringStrategy that is used when multiple exposures are to be taken per
 * acquisition.
 * This can be required when the exposure time defined in a scan is greater than the maximum exposure time of
 * the camera. In which case several images are taken.
 * 
 * If ndProcess is supplied this is configured to sum the images together to that the output of the combined system
 * is a single image per collection

 */
public class PixiumMultipleExposureSoftwareTriggerAutoMode extends MultipleExposureSoftwareTriggerAutoMode {

	int numberExposuresPerImage = 1;
	
	private String calibrationRequiredPVName = "BL12I-EA-DET-10:CAM:CalibrationRequired_RBV";
	private PV<Integer> calibrationRequiredPV;
	
	private String numberExposuresPerImagePVName = "BL12I-EA-DET-10:CAM:NumExposures";
	private PV<Integer> numberExposuresPerImagePV;
	
	public PV<Integer> getCalibrationRequiredPV() {
		if (calibrationRequiredPV == null) {
			calibrationRequiredPV = LazyPVFactory.newIntegerPV(calibrationRequiredPVName);
		}
		return calibrationRequiredPV;
	}
	
	public PV<Integer> getNumberExposuresPerImagePV() {
		if (numberExposuresPerImagePV == null) {
			numberExposuresPerImagePV = LazyPVFactory.newIntegerPV(numberExposuresPerImagePVName);
		}
		return numberExposuresPerImagePV;
	}
	
	public PixiumMultipleExposureSoftwareTriggerAutoMode(ADBase adBase, double maxExposureTime) {
		super(adBase, maxExposureTime);
		this.maxExposureTime = maxExposureTime;
		exposureTime = maxExposureTime;
		
		if (calibrationRequiredPV == null) {
			calibrationRequiredPV = LazyPVFactory.newIntegerPV(calibrationRequiredPVName);
		}
	}

	
	@Override
	public void prepareForCollection(double collectionTime, int numImages_IGNORED, ScanInformation scanInfo_IGNORED) throws Exception {
		if (getCalibrationRequiredPV().get() != 0) {
			throw new DeviceException("Detector calibration required!");
		} 
		double localExposureTime = ndProcess != null ? getExposureTime() : collectionTime;
		numberImagesPerCollection = calcNumberImagesPerCollection(collectionTime, localExposureTime);
		getAdBase().setTriggerMode(StandardTriggerMode.INTERNAL.ordinal());
		numberExposuresPerImage = calcNumberExposuresPerImage(collectionTime, getExposureTime());
		if( getNdFile() != null){
			getAdBase().setImageMode(ImageMode.CONTINUOUS.ordinal());
			setNumExposuresPerImage(numberExposuresPerImage);
			getAdBase().setNumImages(1);
		} else {
			getAdBase().stopAcquiring(); 
			getAdBase().setNumImages(numberImagesPerCollection);
			getAdBase().setImageModeWait(numberImagesPerCollection > 1 ? ImageMode.MULTIPLE : ImageMode.SINGLE);
		}
		if( !isReadAcquireTimeFromHardware())
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
			ndProcess.setDataTypeOut(getProcDataTypeOut());		
			ndProcess.getPluginBase().disableCallbacks();
		}
		enableOrDisableCallbacks();
	}

	@Override
	public void completeCollection() throws Exception {
		super.completeCollection();
		NDPluginBase filePluginBase = getNdFile().getPluginBase();
		if (filePluginBase != null) {
			filePluginBase.enableCallbacks();
		}
	}
	
	@Override
	public double getAcquireTime() throws Exception {
		return getAdBase().getAcquireTime_RBV()*numberExposuresPerImage;
	}

	@Override
	public double getAcquirePeriod() throws Exception {
		return getAdBase().getAcquirePeriod_RBV()*numberExposuresPerImage;
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
			//Thread.sleep((long) (acquirePeriod_RBV * 1000));
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
	
	
	
	
	
	
	protected int calcNumberExposuresPerImage(double collectionTime, double exposureTime) {
		if (exposureTime > 0 && collectionTime > exposureTime)
			return (int)(collectionTime/exposureTime + 0.5);
		return 1;
	}
	
	protected void setNumExposuresPerImage(int numExposuresPerImage) {
		if (numExposuresPerImage > 0) {
			try {
				getNumberExposuresPerImagePV().putWait(numExposuresPerImage);
			} catch (IOException e) {
				throw new IllegalArgumentException("Error in setNumExposuresPerImage",e);
			}
		} else {
			throw new IllegalArgumentException("Unable to set exposures-per-image to " + numExposuresPerImage);
		}
	}
}
