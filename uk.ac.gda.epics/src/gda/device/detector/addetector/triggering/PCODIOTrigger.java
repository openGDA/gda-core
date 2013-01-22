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
import gda.epics.LazyPVFactory;
import gda.epics.PV;
import gda.scan.ScanBase;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;
import gov.aps.jca.event.PutEvent;
import gov.aps.jca.event.PutListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Class of detector  to drive the PCO4000 camera using a TFG2
 * 
 * The TFG is configured for 1 cycle per image with each cycle containg the 3 frames:
 * 0 - wait for software continue
 * 1 - output trigger to camera for collection time
 * 2 - wait for busy signal from camera to go low
 * 
 * The tfg is started in prepareforCollection. So it is starts in frame 0
 * 
 * In collectData we call tfg.cont() to move to frame 1
 * IsBusy returns true if in frame 1. This allows the scannables in the next point to be moved
 * 
 * in following collectData we check that the tfg has completed the current cycle ( hence can now accept another trigger)
 * before we called tfg.cont()
 * 
 * The ADDetector should be set with usePipeline to not hold up the triggering through waiting/checking files.
 * 
 */
public class PCODIOTrigger extends SimpleAcquire {
	private static Logger logger = LoggerFactory.getLogger(PCODIOTrigger.class);
	private final ADDriverPco adDriverPco;
	private double collectionTime = 0.;

	public PCODIOTrigger(ADBase adBase, ADDriverPco adDriverPco) {
		super(adBase, 0.);
		this.adDriverPco = adDriverPco;

	}

	public ADDriverPco getAdDriverPco() {
		return adDriverPco;
	}


	PV<Integer> dioTrigger = LazyPVFactory.newIntegerPV("BL12I-EA-DET-02:DIO:CAPTURE");
	
	private boolean collectingData = false;
	private long expectedExposureEndTime = 0;
	private MonitorListener cameraUsageListener = null;
	protected Double cameraUsage;
	private boolean checkCameraUsage = true;
	private Double cameraUsageUpperLimit = 90.;
	private Double cameraUsageLowerLimit = 25.;
	
	private PutListener dioTriggerPutListener = new PutListener() {
		
		@Override
		public void putCompleted(PutEvent arg0) {
			collectingData = false;
		}
	};
	@Override
	public void prepareForCollection(double collectionTime, int numImagesIgnored) throws Exception {
		getAdBase().stopAcquiring();
		if (checkCameraUsage)
			adDriverPco.getCameraUsagePV().addMonitorListener(getCameraUsageListener());


		getAdBase().setAcquireTime(collectionTime);

		// we want 1 image per trigger - there will be multiple triggers per collection
		getAdBase().setNumImages(1);
		getAdBase().setImageModeWait(ImageMode.SINGLE);
		adDriverPco.getAdcModePV().put(1); //2 adcs
		adDriverPco.getTimeStampModePV().put(1); // BCD - if set to None then the image is blank. BCD means no timestamp on image
		// getAdBase().setAcquirePeriod(0.0); //this is needed for PCO to make sure delay=0 - do not use as it effects
		// delay
		getAdBase().setTriggerMode(PcoTriggerMode.EXTERNAL_AND_SOFTWARE.ordinal()); // exposure time set by camera
																					// rather than trigger
		adDriverPco.getArmModePV().putCallback(true);
		// the callback is coming back before the camera is ready as seen by the BUSY out is still high
		while (!adDriverPco.getArmModePV().get()) {//this is not working as armMode does not reflect true state of arm - check with oscilloscope
			Thread.sleep(50);
			ScanBase.checkForInterrupts();
		}
		this.collectionTime = collectionTime;
		Thread.sleep(2000); //without this the first trigger seems to be ignored
		enableOrDisableCallbacks();

	}

	private MonitorListener getCameraUsageListener() {
		if (cameraUsageListener == null) {
			cameraUsageListener = new MonitorListener() {

				@Override
				public void monitorChanged(MonitorEvent arg0) {
					cameraUsage = adDriverPco.getCameraUsagePV().extractValueFromDbr(arg0.getDBR());

				}
			};
		}
		return cameraUsageListener;
	}

	@Override
	public void completeCollection() throws Exception {
		if (checkCameraUsage)
			adDriverPco.getCameraUsagePV().removeMonitorListener(getCameraUsageListener());
		collectingData = false;
		getAdBase().stopAcquiring();
		getAdBase().setImageModeWait(ImageMode.SINGLE);
		getAdBase().setNumImages(1);
		adDriverPco.getArmModePV().putCallback(false);

	}

	@Override
	public void collectData() throws Exception {
		// if camera usage is above 90% wait until it goes down below 25%
		if (checkCameraUsage && cameraUsage > cameraUsageUpperLimit) {
			while (cameraUsage > cameraUsageLowerLimit) {
				logger.info("Waiting for camera usage to go below " + cameraUsageLowerLimit + "%");
				Thread.sleep(5000); // reading out the memory will take some time
				ScanBase.checkForInterrupts();
			}
		}
		
		collectingData = true;
		expectedExposureEndTime = System.currentTimeMillis() + (long) (collectionTime * 1000.);
		try{
			dioTrigger.put(1, dioTriggerPutListener);
		} catch(Exception ex){
			collectingData = false;
			throw ex;
		}
		
	}

	@SuppressWarnings("unused")
	private boolean isBusy() throws DeviceException {
		return collectingData;
	}

	@Override
	public void waitWhileBusy() throws InterruptedException, DeviceException {
		while (isBusy()) {
			long now = System.currentTimeMillis();
			if (now < expectedExposureEndTime) {
				Thread.sleep(expectedExposureEndTime - now);
			} else {
				Thread.sleep(10);
			}
			ScanBase.checkForInterrupts();
		}
	}

	@Override
	public int getStatus() throws DeviceException {
		return super.getStatus();
	}

	public boolean isCheckCameraUsage() {
		return checkCameraUsage;
	}

	/**
	 * @param checkCameraUsage
	 *            If true(default) the camera usage is checked. If usage goes above cameraUsageUpperLimit the scan
	 *            pauses until it goes below cameraUsageLowerLimit
	 */
	public void setCheckCameraUsage(boolean checkCameraUsage) {
		this.checkCameraUsage = checkCameraUsage;
	}

	public Double getCameraUsageUpperLimit() {
		return cameraUsageUpperLimit;
	}

	/**
	 * @param cameraUsageUpperLimit
	 *            If checkCameraUsage is true then this is the usage at which the scan pauses until it goes below
	 *            cameraUsageLowerLimit
	 */
	public void setCameraUsageUpperLimit(Double cameraUsageUpperLimit) {
		this.cameraUsageUpperLimit = cameraUsageUpperLimit;
	}

	public Double getCameraUsageLowerLimit() {
		return cameraUsageLowerLimit;
	}

	/**
	 * @param cameraUsageLowerLimit
	 *            If checkCameraUsage is true and it was gone above cameraUsageUpperLimit the scan pauses until it goes
	 *            below this value
	 */
	public void setCameraUsageLowerLimit(Double cameraUsageLowerLimit) {
		this.cameraUsageLowerLimit = cameraUsageLowerLimit;
	}

}
