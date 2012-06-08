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
import gda.device.timer.Etfg;
import gda.scan.ScanBase;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;

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
public class PCOTFGTrigger extends SimpleAcquire {
	private static Logger logger = LoggerFactory.getLogger(PCOTFGTrigger.class);
	private final ADDriverPco adDriverPco;
	private final Etfg etfg;
	private double collectionTime = 0.;

	public PCOTFGTrigger(ADBase adBase, ADDriverPco adDriverPco, Etfg tfg) {
		super(adBase, 0.);
		this.adDriverPco = adDriverPco;
		this.etfg = tfg;
		if (tfg == null)
			throw new IllegalArgumentException("tfg==null");
	}

	public ADDriverPco getAdDriverPco() {
		return adDriverPco;
	}

	public Etfg getEtfg() {
		return etfg;
	}

	int expectedCycle = 0;
	final int CYCLES = 100000;// should be enough!
	private boolean collectingData = false;
	private long expectedExposureEndTime = 0;
	private MonitorListener cameraUsageListener = null;
	protected Double cameraUsage;
	private boolean checkCameraUsage = true;
	private Double cameraUsageUpperLimit = 90.;
	private Double cameraUsageLowerLimit = 25.;

	@Override
	public void prepareForCollection(double collectionTime, int numImagesIgnored) throws Exception {
		getAdBase().stopAcquiring();
		if (checkCameraUsage)
			adDriverPco.getCameraUsagePV().addMonitorListener(getCameraUsageListener());

		etfg.stop();
		etfg.getDaServer().sendCommand("tfg setup-trig start adc1 alternate 1"); // PCo Trigger Out as adc1
		etfg.setAttribute("Ext-Start", false);
		etfg.setAttribute("Ext-Inhibit", false);
		etfg.setAttribute("VME-Start", true);
		etfg.setAttribute("Auto-Continue", false);

		etfg.clearFrameSets();
		etfg.addFrameSet(1, 0.0001, 0., 0, 0, -1, 0); // software continue - after start it waits for software start
		etfg.addFrameSet(1, 0., collectionTime * 1000., 0, 255, 0, 0); //set exposure
		etfg.addFrameSet(1, 0.0001, 0., 0, 0, 35, 0); // wait for PCo Trigger Out which is actually PCO Busy
		etfg.setCycles(CYCLES);
		etfg.loadFrameSets();
		etfg.start();
		etfg.setMonitorInBackground(false);
		while (etfg.getStatus() != 2) {
			Thread.sleep(50);
			ScanBase.checkForInterrupts();
		}
		expectedCycle = CYCLES;

		getAdBase().setAcquireTime(collectionTime);

		// we want 1 image per trigger - there will be multiple triggers per collection
		getAdBase().setNumImages(1);
		getAdBase().setImageModeWait(ImageMode.SINGLE);
		adDriverPco.getAdcModePV().put(1); //2 adcs
		// getAdBase().setAcquirePeriod(0.0); //this is needed for PCO to make sure delay=0 - do not use as it effects
		// delay
		getAdBase().setTriggerMode(PcoTriggerMode.EXTERNAL_AND_SOFTWARE.ordinal()); // exposure time set by camera
																					// rather than trigger
		adDriverPco.getArmModePV().putCallback(true);
		// the callback is coming back before the camera is ready as seen by the BUSY out is still high
		while (!adDriverPco.getArmModePV().get()) {
			Thread.sleep(50);
			ScanBase.checkForInterrupts();
		}
		this.collectionTime = collectionTime;
		Thread.sleep(1000); //without this the first trigger seems to be ignored
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
	public void endCollection() throws Exception {
		if (checkCameraUsage)
			adDriverPco.getCameraUsagePV().removeMonitorListener(getCameraUsageListener());
		collectingData = false;
		etfg.stop();
		etfg.setMonitorInBackground(true);
		getAdBase().stopAcquiring();
		getAdBase().setImageModeWait(ImageMode.SINGLE);
		getAdBase().setNumImages(1);
		adDriverPco.getArmModePV().putCallback(false);

	}

	@Override
	public void collectData() throws Exception {
		// only cont if the previous cycle has completed
		logger.info("PCOTFGTrigger collectData in. expectedCycle:" + expectedCycle);
		while (expectedCycle < etfg.getCurrentCycle()) {
			ScanBase.checkForInterrupts();
			Thread.sleep(50);
		}
		// if camera usage is above 90% wait until it goes down below 25%
		if (checkCameraUsage && cameraUsage > cameraUsageUpperLimit) {
			while (cameraUsage > cameraUsageLowerLimit) {
				logger.info("Waiting for camera usage to go below " + cameraUsageLowerLimit + "%");
				Thread.sleep(5000); // reading out the memory will take some time
				ScanBase.checkForInterrupts();
			}
		}
		etfg.cont();
		collectingData = true;
		expectedExposureEndTime = System.currentTimeMillis() + (long) (collectionTime * 1000.);
		expectedCycle--;
		logger.info("PCOTFGTrigger collectData out. expectedCycle:" + expectedCycle);
	}

	private boolean isBusy() throws DeviceException {
		try {
			if (!collectingData)
				return false;
			int frame = etfg.getCurrentFrame();
			if (frame < 0)
				throw new DeviceException("TFG returned frame<0");
			return frame == 1; // in frame 2 we are waiting for busy
		} catch (DeviceException e) {
			throw e;
		} catch (Exception e) {
			throw new DeviceException("Error in isBusy", e);
		}
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
		collectingData = false;
		logger.info("PCOTFGTrigger not busy");

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
