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
import gda.device.Scannable;
import gda.device.detector.areadetector.v17.ADBase;
import gda.device.detector.areadetector.v17.ADBase.ImageMode;
import gda.device.detector.areadetector.v17.ADDriverPco;
import gda.device.detector.areadetector.v17.ADDriverPco.PcoTriggerMode;
import gda.device.timer.Etfg;
import gda.device.timer.Tfg;
import gda.epics.LazyPVFactory;
import gda.epics.PV;
import gda.scan.ScanInformation;
import gov.aps.jca.dbr.DBR;
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
	private String shutterPVName = "BL13I-EA-FSHTR-01:CONTROL";
	private static Logger logger = LoggerFactory.getLogger(PCOTFGTrigger.class);
	private final ADDriverPco adDriverPco;
	private final Etfg etfg;
	private double collectionTime = 0.;
	PV<Integer> shutterPV;

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

	public short getExposeTriggerOutVal() {
		return exposeTriggerOutVal;
	}

	public void setExposeTriggerOutVal(short exposeTriggerOutVal) {
		this.exposeTriggerOutVal = exposeTriggerOutVal;
	}

	public short getNoLongerBusyTriggerInVal() {
		return noLongerBusyTriggerInVal;
	}

	public void setNoLongerBusyTriggerInVal(short noLongerBusyTriggerInVal) {
		this.noLongerBusyTriggerInVal = noLongerBusyTriggerInVal;
	}

	public String getNoLongerBusyTriggerSetupCommand() {
		return noLongerBusyTriggerSetupCommand;
	}

	public void setNoLongerBusyTriggerSetupCommand(String noLongerBusyTriggerSetupCommand) {
		this.noLongerBusyTriggerSetupCommand = noLongerBusyTriggerSetupCommand;
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

	// The port value used to trigger the camera in live mode
	private short exposeTriggerOutVal = 64; // TFG2 USER6 PCO TriggerIn

	// The pause value used to detect that the camera is no longer busy
	private short noLongerBusyTriggerInVal = 39; // Falling edge on adc5

	// command to setup the trigger used to detect that the camera is no longer busy
	private String noLongerBusyTriggerSetupCommand = "tfg setup-trig start adc5 alternate 1"; // // PCO BUSY Out on TFg2
	private int shutterSleep=100;
	private Scannable shutterDarkScannable;
	private Integer adcMode=1;//2 adcs
	private boolean useShutterPV=false;
	private Integer timeStamp=1; //BCD
																								// TF3_OUT5

	public Integer getAdcMode() {
		return adcMode;
	}

	public void setAdcMode(Integer adcMode) {
		this.adcMode = adcMode;
	}

	public boolean isUseShutterPV() {
		return useShutterPV;
	}

	public void setUseShutterPV(boolean useShutterPV) {
		this.useShutterPV = useShutterPV;
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
		if (checkCameraUsage)
			adDriverPco.getCameraUsagePV().addMonitorListener(getCameraUsageListener());

		etfg.stop();
		etfg.getDaServer().sendCommand(noLongerBusyTriggerSetupCommand);
		etfg.setAttribute(Tfg.EXT_START_ATTR_NAME, false);
		etfg.setAttribute(Tfg.EXT_INHIBIT_ATTR_NAME, false);
		etfg.setAttribute(Tfg.VME_START_ATTR_NAME, true);
		etfg.setAttribute(Tfg.AUTO_CONTINUE_ATTR_NAME, false);
		etfg.setAttribute(Tfg.AUTO_REARM_ATTR_NAME, false);

		etfg.clearFrameSets();
		etfg.addFrameSet(1, 0.0001, 0., 0, 0, -1, 0); // software continue - after start it waits for software start
		etfg.addFrameSet(1, 0., collectionTime * 1000., 0, exposeTriggerOutVal, 0, 0); // set exposure trigger
		etfg.addFrameSet(1, 0.0001, 0., 0, 0, noLongerBusyTriggerInVal, 0); // wait for PCo Trigger Out which is
																			// actually PCO Busy
		etfg.setCycles(CYCLES);
		etfg.loadFrameSets();
		etfg.setMonitorInBackground(false);

		getAdBase().setAcquireTime(collectionTime);
		getAdBase().setAcquirePeriod(collectionTime);

		// we want 1 image per trigger - there will be multiple triggers per collection
		getAdBase().setNumImages(1);
		getAdBase().setNumExposures(1);
		getAdBase().setImageModeWait(ImageMode.SINGLE);
		adDriverPco.getAdcModePV().putWait(adcMode); // 2 adcs
		adDriverPco.getTimeStampModePV().putWait(timeStamp); // BCD - if set to None then the image is blank. BCD means no timestamp
													// on image
		// getAdBase().setAcquirePeriod(0.0); //this is needed for PCO to make sure delay=0 - do not use as it effects
		// delay
		getAdBase().setTriggerMode(PcoTriggerMode.EXTERNAL_AND_SOFTWARE.ordinal()); // exposure time set by camera
																					// rather than trigger
		adDriverPco.getArmModePV().putWait(true);
		// the callback is coming back before the camera is ready as seen by the BUSY out is still high
		while (!adDriverPco.getArmModePV().get()) {// this is not working as armMode does not reflect true state of arm
													// - check with oscilloscope
			Thread.sleep(50);
		}
		this.collectionTime = collectionTime;
		Thread.sleep(2000); // without this the first trigger seems to be ignored
		etfg.start();
		while (etfg.getStatus() != 2) {
			Thread.sleep(50);
		}
		expectedCycle = CYCLES;
		enableOrDisableCallbacks();

	}

	private MonitorListener getCameraUsageListener() {
		if (cameraUsageListener == null) {
			cameraUsageListener = new MonitorListener() {

				@Override
				public void monitorChanged(MonitorEvent arg0) {
					DBR dbr = arg0.getDBR();
					if( dbr != null)
						cameraUsage = adDriverPco.getCameraUsagePV().extractValueFromDbr(dbr);
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
		etfg.stop();
		etfg.setMonitorInBackground(true);
		getAdBase().stopAcquiring();
		getAdBase().setImageModeWait(ImageMode.SINGLE);
		getAdBase().setNumImages(1);
		adDriverPco.getArmModePV().putWait(false);

	}

	@Override
	public void collectData() throws Exception {
		// only cont if the previous cycle has completed
		logger.info("PCOTFGTrigger collectData in. expectedCycle:" + expectedCycle);
		while (expectedCycle < etfg.getCurrentCycle()) {
			Thread.sleep(50);
		}
		// if camera usage is above 90% wait until it goes down below 25%
		if (checkCameraUsage && cameraUsage > cameraUsageUpperLimit) {
			while (cameraUsage > cameraUsageLowerLimit) {
				logger.info("Waiting for camera usage to go below " + cameraUsageLowerLimit + "%");
				Thread.sleep(5000); // reading out the memory will take some time
			}
		}
		// open the shutter
		openShutter(true);
		logger.error("etg.cont");
		etfg.cont(); 
		collectingData = true;
		expectedExposureEndTime = System.currentTimeMillis() + (long) (collectionTime * 1000.);
		expectedCycle--;
		logger.info("PCOTFGTrigger collectData out. expectedCycle:" + expectedCycle);
	}

	public int getShutterSleep() {
		return shutterSleep;
	}

	public void setShutterSleep(int shutterSleep) {
		this.shutterSleep = shutterSleep;
	}

	public String getShutterPVName() {
		return shutterPVName;
	}

	public void setShutterPVName(String shutterPVName) {
		this.shutterPVName = shutterPVName;
	}

	private void openShutter(Boolean open) throws DeviceException {
		try{
			if( !shutterPVName.isEmpty() && useShutterPV){
				if( shutterPV == null ){
					shutterPV = LazyPVFactory.newIntegerPV(shutterPVName);
					shutterPV.get();
				}
				if( open && ( shutterDarkScannable != null) && ( !shutterDarkScannable.getPosition().equals("Open"))){
					open = false;
				}
				shutterPV.putNoWait(open ? 1 : 0);
				if(open && (shutterSleep > 0))
					Thread.sleep(shutterSleep);
			}
		}
		catch (Exception e) {
			throw new DeviceException("Error controlling the shutter", e);
		}
	}

	public Scannable getShutterDarkScannable() {
		return shutterDarkScannable;
	}

	public void setShutterDarkScannable(Scannable shutterDarkScannable) {
		this.shutterDarkScannable = shutterDarkScannable;
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
		}
		// close the shutter
		openShutter(false);
		collectingData = false;
		logger.info("PCOTFGTrigger not busy");

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
	@Override
	public boolean requiresAsynchronousPlugins() {
		return true; //there is no synchronisation between this collection strategy and reading of the data in to the areaDetector plugins
	}
}
