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
import gda.device.detector.areadetector.v17.NDProcess;
import gda.device.detector.areadetector.v17.NDProcess.FilterTypeEnum;

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

	double maxExposureTime=Double.MAX_VALUE;
	int numberImagesPerCollection=1;
	NDProcess ndProcess=null;
	public MultipleExposureSoftwareTriggerAutoMode(ADBase adBase, double maxExposureTime) {
		super(adBase);
		this.maxExposureTime = maxExposureTime;
	}

	public NDProcess getNdProcess() {
		return ndProcess;
	}

	public void setNdProcess(NDProcess ndProcess) {
		this.ndProcess = ndProcess;
	}

	public double getMaxExposureTime() {
		return maxExposureTime;
	}

	public void setMaxExposureTime(double maxExposureTime) {
		this.maxExposureTime = maxExposureTime;
	}

	@Override
	public void prepareForCollection(double collectionTime, int numImagesIgnored) throws Exception {
		getAdBase().stopAcquiring(); 
		getAdBase().setTriggerMode(StandardTriggerMode.INTERNAL.ordinal());
		numberImagesPerCollection = calcNumberImagesPerCollection(collectionTime);
		getAdBase().setNumImages(numberImagesPerCollection);
		getAdBase().setImageModeWait(numberImagesPerCollection > 1 ? ImageMode.MULTIPLE : ImageMode.SINGLE);
		getAdBase().setAcquireTime(numberImagesPerCollection > 1 ?maxExposureTime : collectionTime);
//		getAdBase().setAcquirePeriod(0.0);
		
		if( ndProcess != null){
			ndProcess.setFilterType(FilterTypeEnum.RecursiveSum);
			ndProcess.setNumFilter(numberImagesPerCollection);
			ndProcess.setEnableFilter(1);
			ndProcess.getPluginBase().enableCallbacks();
			ndProcess.getPluginBase().setArrayCounter(0);
			ndProcess.getPluginBase().setDroppedArrays(0);
		}
	}

	@Override
	public void endCollection() throws Exception {
		getAdBase().stopAcquiring();
		getAdBase().setImageModeWait(ImageMode.SINGLE);
		getAdBase().setNumImages(1);
		if( ndProcess != null){
			ndProcess.setFilterType(FilterTypeEnum.RecursiveSum);
			ndProcess.setNumFilter(1);
			ndProcess.getPluginBase().disableCallbacks();
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
		getAdBase().startAcquiring();
	}

	@Override
	public int getStatus() throws DeviceException {
		return getAdBase().getStatus();
	}
	
	@Override
	public void waitWhileBusy() throws InterruptedException, DeviceException {
		getAdBase().waitWhileStatusBusy();
	}
	
	@Override
	public void stop() throws Exception {
		endCollection();
	}
	
	@Override
	public void atCommandFailure() throws Exception {
		endCollection();
	}

	protected int calcNumberImagesPerCollection(double collectionTime) {
		if (collectionTime > maxExposureTime)
			return (int)(collectionTime/maxExposureTime + 0.5);
		return 1;
	}
	
	@Override
	public int getNumberImagesPerCollection(double collectionTime) {
		return ndProcess == null ? calcNumberImagesPerCollection(collectionTime) : 1;
	}

}
