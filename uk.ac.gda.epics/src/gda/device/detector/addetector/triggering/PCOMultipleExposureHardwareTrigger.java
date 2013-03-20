/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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
import gda.device.detector.areadetector.v17.ADDriverPco;
import gda.device.detector.areadetector.v17.ADDriverPco.PcoTriggerMode;
import gda.device.detector.areadetector.v17.NDProcess;
import gda.device.timer.Etfg;
import gda.device.timer.Tfg;
import gda.scan.ScanBase;
import gda.scan.ScanInformation;

/*
 * Class of detector used to take multiple exposures that are then added together to make a single collection image
 * The TFG receives a trigger from a signal generator that is also used to vibrate the sample
 * The TFG responds by sending a trigger to the camera after a certain delay
 * Each TFG trigger results in a single exposure. Multilple exposures are added together in the proc plugin
 * and the result is the image to be saved.
 * The trigger is busy until N images are summed together by the proc plugin
 * 
 */
public class PCOMultipleExposureHardwareTrigger extends MultipleExposureSoftwareTriggerAutoMode {

	private final ADDriverPco adDriverPco;
	private Etfg etfg;
	
	//time in s between detecting trigger (PCO Trigger Out) and sending trigger to PCO (PCO Trigger In)
	private double delayTime=0;
	private Integer adcMode=1; //2 ADCs
	private Integer timeStamp=1; // BCD
	
	public double getDelayTime() {
		return delayTime;
	}

	public void setDelayTime(double delayTime) {
		this.delayTime = delayTime;
	}

	public PCOMultipleExposureHardwareTrigger(ADBase adBase, double maxExposureTime, ADDriverPco adDriverPco) {
		super(adBase, maxExposureTime);
		this.adDriverPco = adDriverPco;
	}

	public ADDriverPco getAdDriverPco() {
		return adDriverPco;
	}

	public Etfg getEtfg() {
		return etfg;
	}

	public void setEtfg(Etfg etfg) {
		this.etfg = etfg;
	}

	public Integer getAdcMode() {
		return adcMode;
	}

	public void setAdcMode(Integer adcMode) {
		this.adcMode = adcMode;
	}

	public Integer getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(Integer timeStamp) {
		this.timeStamp = timeStamp;
	}

	@Override
	public void prepareForCollection(double collectionTime, int numImagesIgnored, ScanInformation scanInfo) throws Exception {
		getAdBase().stopAcquiring();
		double localExposureTime = ndProcess != null ? getExposureTime() : collectionTime;
		numberImagesPerCollection = calcNumberImagesPerCollection(collectionTime, localExposureTime);
		if( !isReadAcquireTimeFromHardware())
			getAdBase().setAcquireTime(numberImagesPerCollection > 1 ? localExposureTime : collectionTime);

		if (ndProcess != null) {

			ndProcess.setFilterType(NDProcess.FilterTypeV1_8_Sum);
			ndProcess.setNumFilter(numberImagesPerCollection);
			ndProcess.setFilterCallbacks(NDProcess.FilterCallback_ArrayNOnly);
			ndProcess.setEnableFilter(0); // enable in collectData
			// do not use autoreset - rather reset for every collection
			// need to set Callbacks Array N only - version 1.8 of AD
			ndProcess.setAutoResetFilter(0);
			ndProcess.setEnableHighClip(0);
			ndProcess.setEnableLowClip(0);
			ndProcess.setEnableOffsetScale(0);
			ndProcess.setEnableFlatField(0);
			ndProcess.setEnableBackground(0);
			ndProcess.getPluginBase().enableCallbacks();
			ndProcess.getPluginBase().setArrayCounter(0);
			ndProcess.getPluginBase().setDroppedArrays(0);
			ndProcess.setDataTypeOut(5); // UINT32
			/*
			 * if tfg is present we can use autoreset
			 */
		}
		
		if( etfg != null){
			etfg.stop();

			etfg.setAttribute(Tfg.EXT_START_ATTR_NAME, true);
			etfg.setAttribute(Tfg.AUTO_REARM_ATTR_NAME, true);
			etfg.setAttribute(Tfg.VME_START_ATTR_NAME, false);
			etfg.setAttribute(Tfg.EXT_INHIBIT_ATTR_NAME, false);
			etfg.setAttribute(Tfg.AUTO_CONTINUE_ATTR_NAME, false);

			etfg.clearFrameSets();
			etfg.getDaServer().sendCommand("tfg setup-trig start adc4 alternate 1"); // PCO Trigger Out on TFg2 TF3_OUT4 
			etfg.addFrameSet(1, delayTime*1000, localExposureTime * 1000., 0, 64, 0, 0); //set exposure TFG2 User Out 6
			etfg.setCycles(1); //the number of cycles is not used
			etfg.loadFrameSets();
			
		}
		// we want 1 image per trigger - there will be multiple triggers per collection
		getAdBase().setNumImages(1);
		getAdBase().setImageModeWait(ImageMode.SINGLE);
		// getAdBase().setAcquirePeriod(0.0); //this is needed for PCO to make sure delay=0 - do not use as it effects
		// delay
		adDriverPco.getAdcModePV().put(adcMode); // 2 adcs
		adDriverPco.getTimeStampModePV().put(timeStamp); // BCD - if set to None then the image is blank. BCD means no timestamp
		
		getAdBase().setTriggerMode(PcoTriggerMode.EXTERNAL_AND_SOFTWARE.ordinal()); // exposure time set by camera
																					// rather than trigger
		enableOrDisableCallbacks();
	}

	@Override
	public void completeCollection() throws Exception {
		if( etfg != null)
			etfg.stop();
		getAdBase().stopAcquiring();
		if (ndProcess != null) {
			ndProcess.setEnableFilter(0);
			ndProcess.getPluginBase().disableCallbacks();
			// ndProcess.setFilterType(FilterTypeEnum.RecursiveSum);
			// ndProcess.setNumFilter(1);
		}
		getAdBase().setImageModeWait(ImageMode.SINGLE);
		getAdBase().setNumImages(1);
		adDriverPco.getArmModePV().putCallback(false);

	}

	@Override
	public void collectData() throws Exception {
		//toggle armed state to clear memeory of images taken at last position
		if( etfg != null)
			etfg.stop(); 
		adDriverPco.getArmModePV().putCallback(false);
		adDriverPco.getArmModePV().putCallback(true);
		// the callback is coming back before the camera is ready as seen by the BUSY out is still high
		while (!adDriverPco.getArmModePV().get()) {// this is not working as armMode does not reflect true state of arm
													// - check with oscilloscope
			Thread.sleep(50);
			ScanBase.checkForInterrupts();
		}

		Thread.sleep(2000); // without this the first trigger seems to be ignored		

		if (ndProcess != null) {
			ndProcess.setResetFilter(1);
			ndProcess.setEnableFilter(1);
			while (ndProcess.getEnableFilter_RBV() == 0) {
				Thread.sleep(50); // should use wait in setFilter
			}
		}
		if( etfg != null)
			etfg.start();

	}

	private boolean isBusy() throws DeviceException {
		try {
			if (ndProcess != null) {
				return (ndProcess.getEnableFilter_RBV() == 1)
						&& (ndProcess.getResetFilter_RBV() != 0 || ndProcess.getNumFiltered_RBV() != numberImagesPerCollection);
			}
			return false;
		} catch (DeviceException e) {
			throw e;
		} catch (Exception e) {
			throw new DeviceException("Error in isBusy", e);
		}
	}

	@Override
	public void waitWhileBusy() throws InterruptedException, DeviceException {
		while (isBusy()) {
			Thread.sleep(50);
		}
	}

}
