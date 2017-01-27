/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package gda.device.detector.pco;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.data.NumTracker;
import gda.data.PathConstructor;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.detector.DetectorBase;
import gda.device.detector.PCO4000;
import gda.device.detector.areadetector.AreaDetectorLiveView;
import gda.device.detector.areadetector.AreaDetectorROI;
import gda.device.detector.areadetector.EPICSAreaDetectorImage;
import gda.device.detector.areadetector.EpicsAreaDetector;
import gda.device.detector.areadetector.EpicsAreaDetectorFileSave;
import gda.device.detector.areadetector.EpicsAreaDetectorROI;
import gda.device.detector.areadetector.EpicsAreaDetectorROIElement;
import gda.epics.connection.EpicsChannelManager;
import gda.epics.connection.EpicsController;
import gda.factory.FactoryException;
import gda.jython.ITerminalPrinter;
import gda.jython.InterfaceProvider;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.TimeoutException;

public class PCOController extends DetectorBase implements PCO4000 {

	// Setup the logging facilities
	private static final Logger logger = LoggerFactory.getLogger(PCOController.class);

	// Values internal to the object for Channel Access
	private EpicsChannelManager ecm = new EpicsChannelManager();
	private EpicsController ecl = EpicsController.getInstance();

	// EPICS Channels
	private Channel channelPixRate_RBV;
	private Channel channelADCMode_RBV;
	private Channel channelTrigger;

	// Variables to hold the spring settings
	private EpicsAreaDetector           areaDetector;
	private EpicsAreaDetectorROI        areaDetectorROI;
	private EpicsAreaDetectorROIElement fullFrameROI;
	private EpicsAreaDetectorFileSave   dataSaver;
	private EpicsAreaDetectorFileSave   darkSaver;
	private EpicsAreaDetectorFileSave   flatSaver;
	private EpicsAreaDetectorROIElement previewROI;
	private EPICSAreaDetectorImage      image;
	private AreaDetectorLiveView		live;

	private String                      projectionFoldername	= "projections";
	private int							readout1ADC8Mhz;
	private int							readout1ADC32Mhz;
	private int							readout2ADC8Mhz;
	private int							readout2ADC32Mhz;

	// New Parameters
	private long aquisitionStartTime = 0;
	private long pauseTime = 0;
	private boolean saveLocal = false;
	private File scanSaveFolder;
	private int scanSaveNumber;
	private String localFilePath;
	private String numTrackerTag;
	private String triggerPV;

	// Getters and Setters for Spring
	public EpicsAreaDetector getAreaDetector() {
		return areaDetector;
	}

	public void setAreaDetector(EpicsAreaDetector areaDetector) {
		this.areaDetector = areaDetector;
	}

	public EpicsAreaDetectorROI getAreaDetectorROI() {
		return areaDetectorROI;
	}

	public void setAreaDetectorROI(EpicsAreaDetectorROI areaDetectorROI) {
		this.areaDetectorROI = areaDetectorROI;
	}

	public EpicsAreaDetectorROIElement getFullFrameROI() {
		return fullFrameROI;
	}

	public void setFullFrameROI(EpicsAreaDetectorROIElement fullFrameROI) {
		this.fullFrameROI = fullFrameROI;
	}

	public EpicsAreaDetectorFileSave getDataSaver() {
		return dataSaver;
	}

	public void setDataSaver(EpicsAreaDetectorFileSave dataSaver) {
		this.dataSaver = dataSaver;
	}

	public EpicsAreaDetectorFileSave getDarkSaver() {
		return darkSaver;
	}

	public void setDarkSaver(EpicsAreaDetectorFileSave darkSaver) {
		this.darkSaver = darkSaver;
	}

	public EpicsAreaDetectorFileSave getFlatSaver() {
		return flatSaver;
	}

	public void setFlatSaver(EpicsAreaDetectorFileSave flatSaver) {
		this.flatSaver = flatSaver;
	}

	public EPICSAreaDetectorImage getImage() {
		return image;
	}

	public void setImage(EPICSAreaDetectorImage image) {
		this.image = image;
	}

	public String getProjectionFoldername() {
		return projectionFoldername;
	}

	public void setProjectionFoldername(String projectionFoldername) {
		this.projectionFoldername = projectionFoldername;
	}

	public EpicsAreaDetectorROIElement getPreviewROI() {
		return previewROI;
	}

	public void setPreviewROI(EpicsAreaDetectorROIElement previewROI) {
		this.previewROI = previewROI;
	}

	public int getReadout1ADC8Mhz() {
		return readout1ADC8Mhz;
	}

	public void setReadout1ADC8Mhz(int readout1adc8Mhz) {
		readout1ADC8Mhz = readout1adc8Mhz;
	}

	public int getReadout1ADC32Mhz() {
		return readout1ADC32Mhz;
	}

	public void setReadout1ADC32Mhz(int readout1adc32Mhz) {
		readout1ADC32Mhz = readout1adc32Mhz;
	}

	public int getReadout2ADC8Mhz() {
		return readout2ADC8Mhz;
	}

	public void setReadout2ADC8Mhz(int readout2adc8Mhz) {
		readout2ADC8Mhz = readout2adc8Mhz;
	}

	public int getReadout2ADC32Mhz() {
		return readout2ADC32Mhz;
	}

	public void setReadout2ADC32Mhz(int readout2adc32Mhz) {
		readout2ADC32Mhz = readout2adc32Mhz;
	}

	public void setTriggerPV(String triggerPV) {
		this.triggerPV = triggerPV;
	}

	public String getTriggerPV() {
		return triggerPV;
	}

	public String getLocalFilePath() {
		return localFilePath;
	}

	public void setLocalFilePath(String localFilePath) {
		this.localFilePath = localFilePath;
	}

	public String getNumTrackerTag() {
		return numTrackerTag;
	}

	public void setNumTrackerTag(String numTrackerTag) {
		this.numTrackerTag = numTrackerTag;
	}

	public AreaDetectorLiveView getLive() {
		return live;
	}

	public void setLive(AreaDetectorLiveView live) {
		this.live = live;
	}

	// Methods for configurable interface and the reset method
	@Override
	public void configure() throws FactoryException{
		super.configure();

		try {
			// Set up all the CA channels
			channelADCMode_RBV = ecl.createChannel(areaDetector.getBasePVName() + "ADC_MODE");
			channelPixRate_RBV = ecl.createChannel(areaDetector.getBasePVName() + ":PIX_RATE");
			channelTrigger = ecl.createChannel(getTriggerPV());

			// acknowledge that creation phase is completed
			ecm.creationPhaseCompleted();

			// Set the initial Parameters
			reset();
		} catch (Exception e) {
			throw new FactoryException("Failure to initialise PCO camera EPICS connections", e);
		}

		try {
			reset();
		} catch (Exception e) {
			throw new FactoryException("Failed to configure the PCO camera", e);
		}
	}

	private void reset() throws CAException, InterruptedException {

		// on a reset, set all the file writers to zero
		dataSaver.setframeCounter(0);
		darkSaver.setframeCounter(0);
		flatSaver.setframeCounter(0);
	}

	public void resetAll() throws CAException, InterruptedException {
		areaDetector.reset();
		areaDetectorROI.reset();
		previewROI.reset();
		fullFrameROI.reset();
		dataSaver.reset();
		darkSaver.reset();
		flatSaver.reset();
		image.reset();
		reset();
	}

	private int getADCMode() throws TimeoutException, CAException, InterruptedException {
		return ecl.cagetInt(channelADCMode_RBV);
	}

	private int getPixRate() throws TimeoutException, CAException, InterruptedException {
		return ecl.cagetInt(channelPixRate_RBV);
	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		return true;
	}

	@Override
	public String getDescription() throws DeviceException {
		//TODO Parameterise this
		return "PCO4000 CCD camera" ;
	}

	@Override
	public String getDetectorID() throws DeviceException {
		//TODO Parameterise this
		return "ID";
	}

	@Override
	public String getDetectorType() throws DeviceException {
		return "CCD";
	}

	private void trigger() throws CAException, InterruptedException {
		ecl.caput(channelTrigger, 1);
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			logger.error("Error in trigger()", e);
		}
		ecl.caput(channelTrigger, 0);
	}

	// Methods which are required for the Detector Interface
	@Override
	public void collectData() throws DeviceException {

		// make sure that the new time is at least the acquisition Time plus the pause
		final long nextStartTime = (long) (aquisitionStartTime + pauseTime + (collectionTime*1000.0));
		final long currentTime = System.currentTimeMillis();
		//print(String.format("%d, %d", nextStartTime, currentTime));
		// wait for the correct amount of time, and then start
		if(nextStartTime > currentTime) {
			try {
				Thread.sleep(nextStartTime-currentTime);
			} catch (InterruptedException e) {
				throw new DeviceException("Failed to cause Thread.sleep",e);
			}
		}

		// trigger an acquisition
		/*try {
			areaDetector.acquire();
		} catch (CAException e) {
			throw new DeviceException("Failed to make PCO camera Aquire",e);
		}
		*/
		//now do this with the trigger method
		try {
			trigger();
			aquisitionStartTime = System.currentTimeMillis();
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new DeviceException("Failed to make PCO camera trigger",e);
		}
	}

	@Override
	public int getStatus() throws DeviceException {

		if (System.currentTimeMillis() < (aquisitionStartTime + (collectionTime*1000))) {
			return Detector.BUSY;
		}
		return Detector.IDLE;
	}

	@Override
	public boolean isBusy() {
		try {
			if (getStatus() == Detector.BUSY) {
				return true;
			}
		} catch (DeviceException e) {
			logger.warn("Exception in isBusy()", e);
		}
		return false;
	}

	@Override
	public Object readout() throws DeviceException {
		String output;
		try {
			output = String.format(dataSaver.getFileTemplate().trim(),scanSaveFolder.getAbsolutePath(),dataSaver.getFileName(),scanSaveNumber);
		} catch (Exception e) {
			throw new DeviceException("Failed to generate readout string",e);
		}
		scanSaveNumber++;
		return output.trim();
	}

	@Override
	public void setCollectionTime(double collectionTime) throws DeviceException {
		this.collectionTime = collectionTime;

		try {
			areaDetector.setExpTime(collectionTime);
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new DeviceException(getName() + " exception in setCollectionTime",e);
		}
	}

	// Methods for the Scannable interface
	@Override
	public void atScanStart() throws DeviceException {

		// first thing to do is stop the preview if its still running
		try {
			stopPreview();
		} catch (Exception e2) {
			throw new DeviceException("Failed to stop preview",e2);
		}

		pauseTime = 2000;
		try {
			if (getADCMode() < 2) {
				if (getPixRate() < 16000000) {
					pauseTime = getReadout1ADC8Mhz();
				} else {
					pauseTime = getReadout1ADC32Mhz();
				}
			} else {
				if (getPixRate() < 16000000) {
					pauseTime = getReadout2ADC8Mhz();
				} else {
					pauseTime = getReadout2ADC32Mhz();
				}
			}
			print(String.format("PauseTime is %d ADCmode is %d and Pixrate is %d", pauseTime, getADCMode(), getPixRate()));

		} catch (Exception e1) {
			throw new DeviceException("Failed to get the ADC and Pix rate information from the camera",e1);
		}

		try {
			// and sort out the real place to save to
			scanSaveFolder = createMainFileStructure();
			scanSaveNumber = 0;

			// set up the saving facilities
			dataSaver.setArrayPort(dataSaver.getInitialArrayPort());
			dataSaver.setArrayAddress(dataSaver.getInitialArrayAddress());
			darkSaver.setArrayPort(darkSaver.getInitialArrayPort());
			darkSaver.setArrayAddress(darkSaver.getInitialArrayAddress());
			flatSaver.setArrayPort(flatSaver.getInitialArrayPort());
			flatSaver.setArrayAddress(flatSaver.getInitialArrayAddress());
			if(saveLocal) {
				dataSaver.setFilePath(localFilePath);
				darkSaver.setFilePath(localFilePath);
				flatSaver.setFilePath(localFilePath);
			} else {
				dataSaver.setFilePath(scanSaveFolder.getAbsolutePath().replace("/dls/i12", "Z:"));
				darkSaver.setFilePath(scanSaveFolder.getAbsolutePath().replace("/dls/i12", "Z:"));
				flatSaver.setFilePath(scanSaveFolder.getAbsolutePath().replace("/dls/i12", "Z:"));
			}

			dataSaver.setEnable(false);
			darkSaver.setEnable(false);
			flatSaver.setEnable(false);

			previewROI.setUse(true);
			image.setEnable(true);
			live.start();

			// set to single images
			areaDetector.setImageMode(0);
			Thread.sleep(100);
			areaDetector.acquire();
			// wait a second to let the camera prepare itself.
			Thread.sleep(1000);

			// boolean notWorking = true;
			// now just check that the camera is armed properly, to do this acquire one image and make sure the counter has incremented
/*			while (notWorking) {
				int startCount = areaDetector.getArrayCounter();
				double acqTime = this.collectionTime;
				// set a quick exposure time
				areaDetector.setExpTime(0.1);
				//fire the detector
				areaDetector.acquire();
				// now wait so the image has time to appear
				Thread.sleep(pauseTime + 1000);
				// put the exposure time back
				areaDetector.setExpTime(acqTime);

				if (startCount == areaDetector.getArrayCounter()) {
					// so the detecotor has failed, there is some random issue, so lets stop and rearm
					areaDetector.stop();
					Thread.sleep(5000);
					// rearm
					areaDetector.acquire();
					Thread.sleep(2000);
				} else {
					// the acquisition succeeded, so lets carry on with the exposure
					notWorking = false;
				}
			}
*/
			// now set up all the collection things correctly
			dataSaver.setEnable(true);
			darkSaver.setEnable(false);
			flatSaver.setEnable(false);
			//dataSaver.setFileName(dataSaver.getInitialFileName());
			darkSaver.setFileName(darkSaver.getInitialFileName());
			flatSaver.setFileName(flatSaver.getInitialFileName());
			dataSaver.setFileTemplate(dataSaver.getInitialFileTemplate());
			darkSaver.setFileTemplate(darkSaver.getInitialFileTemplate());
			flatSaver.setFileTemplate(flatSaver.getInitialFileTemplate());
			dataSaver.setAutoSave("Yes");
			dataSaver.setAutoIncrement("Yes");
			dataSaver.setframeCounter(0);
			dataSaver.setWriteMode("Single");
			darkSaver.setAutoSave("Yes");
			darkSaver.setAutoIncrement("Yes");
			darkSaver.setframeCounter(0);
			darkSaver.setWriteMode("Single");
			flatSaver.setAutoSave("Yes");
			flatSaver.setAutoIncrement("Yes");
			flatSaver.setframeCounter(0);
			flatSaver.setWriteMode("Single");

		} catch (Exception e) {
			throw new DeviceException("Failed to cause PCO to setup for scan",e);
		}
	}

	@Override
	public void atScanEnd() throws DeviceException {
		stopAndCopy();
	}

	@Override
	public void stop() throws DeviceException {

		stopAndCopy();
		try {
			stopPreview();
		} catch (Exception e) {
			throw new DeviceException("Failed to cause PCO to setup for scan",e);
		}
	}

	private void print(final String message) {
		final ITerminalPrinter terminalPrinter = InterfaceProvider.getTerminalPrinter();
		if (terminalPrinter != null) {
			terminalPrinter.print(message);
		}
	}

	private void stopAndCopy() throws DeviceException {
		try {
			// stop the detector when the files have been colected.
			int fileNumber = 0;
			int newFileNumber = dataSaver.getFileNumber();
			while (fileNumber != newFileNumber) {
				Thread.sleep((long) ((pauseTime*2)+(collectionTime*1000)));
				fileNumber = newFileNumber;
				newFileNumber = dataSaver.getFileNumber();
			}
			// now stop the detector
			areaDetector.stop();

			// copy the files off
			if(saveLocal) {
				Runtime rt = Runtime.getRuntime();
				Process p = rt.exec(String.format("/dls_sw/i12/software/gda/config/bin/transfer.sh %s", scanSaveFolder.getAbsolutePath()));
				print("Copying data to Central Storage");
				// TODO this should be in a monitoring loop
				p.waitFor();
			}

		} catch (Exception e) {
			throw new DeviceException("Failed to run pco.stopandcopy scan",e);
		}

	}

	// Helper methods for dealing with the file system.
	public File createMainFileStructure() throws IOException {
		// set up the filename which will be the base directory for data to be saved to
		final File path = new File(PathConstructor.createFromDefaultProperty());
		final NumTracker nt = new NumTracker(numTrackerTag);
		final String filenumber = Long.toString(nt.getCurrentFileNumber());
		final File scanFolder = new File(path, filenumber);
		// Make the directory if required.
		if(!scanFolder.isDirectory()) {
			// create the directory
			scanFolder.mkdir();
		}

		final File projectionsFolder = new File(scanFolder,projectionFoldername);

		if(!projectionsFolder.isDirectory()) {
			// create the directory
			projectionsFolder.mkdir();
		}

		return projectionsFolder;
	}

	public void setFileSaverParameters(File filePath) throws CAException, InterruptedException {
		// make the change for the windows filesystem and mount
		// TODO generate this
		final String filePathString = filePath.getAbsolutePath().replace("/dls/i12", "Z:");
		dataSaver.setFilePath(filePathString);
		darkSaver.setFilePath(filePathString);
		flatSaver.setFilePath(filePathString);
	}

	public void resetAndEnableFullFrameCapture() throws CAException, TimeoutException, InterruptedException {

		// ROI Elements
		areaDetectorROI.setEnable(true);

		// ROI Full Frame enable, to the size of the camera
		fullFrameROI.setUse(true);
		final AreaDetectorROI roi = areaDetector.getROI();
		// modify this as the PCO is 1 based not zero
		roi.setMinX(roi.getMinX()-1);
		roi.setMinY(roi.getMinY()-1);
		fullFrameROI.setROI(roi);
		fullFrameROI.setDataType(areaDetector.getInitialDataType());

		// Frame Saver Elements, set all to capture, but dont enable them
		//dataSaver.setEnable(true);
		dataSaver.setframeCounter(0);
		dataSaver.startCapture();
		darkSaver.setframeCounter(0);
		darkSaver.startCapture();
		flatSaver.setframeCounter(0);
		flatSaver.startCapture();

		// Set the area Detector code
		areaDetector.setArrayCounter(0);
	}


	public void collectDarkSet(int numberOfDarks) throws CAException, DeviceException, InterruptedException, TimeoutException {

		final int tempFileNumber = scanSaveNumber;

		// wait for the camera to finish reading out

		// TODO make this more robust, for now though the collection speeds are reduced.
		Thread.sleep(1000);

		// de/enable the savers the savers
		dataSaver.setEnable(false);
		flatSaver.setEnable(false);
		darkSaver.setEnable(true);

		// capture the appropriate number of images
		darkSaver.setframeCounter(0);
		for(int i = 0; i < numberOfDarks; i++) {
			logger.debug("Collecting dark image from loop {}",i);
			print(String.format("Collecting dark image d_%05d",i));
			this.collectData();
			// wait for the collection to be complete
			Thread.sleep(100);
			while(this.isBusy()) {
				Thread.sleep(100);
			}
			this.readout();
		}

		// the last time we need to wait at least the delaytime to be sure of readout from the camera
		Thread.sleep(pauseTime);

		// Wait for the frames to be read out and then enable the data sever again
		int darkSaverFileNumber = darkSaver.getFileNumber();

		while (darkSaverFileNumber < numberOfDarks) {
			Thread.sleep(1000);
			darkSaverFileNumber = darkSaver.getFileNumber();
		}

		scanSaveNumber = tempFileNumber;

		// de/enable the savers the savers
		dataSaver.setEnable(true);
		flatSaver.setEnable(false);
		darkSaver.setEnable(false);

	}


	public void collectFlatSet(int numberOfFlats, int flatSet) throws CAException, DeviceException, InterruptedException, TimeoutException {
		// wait for the camera to finish reading out

		final int tempFileNumber = scanSaveNumber;

		// TODO make this more robust, for now though the collection speeds are reduced.
		Thread.sleep(1000);

		// de/enable the savers the savers
		dataSaver.setEnable(false);
		flatSaver.setEnable(true);
		darkSaver.setEnable(false);

		// capture the appropriate number of images
		flatSaver.setframeCounter(0);
		flatSaver.setFileName(String.format("f_%03d", flatSet));
		for(int i = 0; i < numberOfFlats; i++) {
			logger.debug("Collecting dark image from loop {}",i);
			print(String.format("Collecting flat image f_%03d_%05d",flatSet,i));
			this.collectData();
			// wait for the collection to be complete
			Thread.sleep(100);
			while(this.isBusy()) {
				Thread.sleep(100);
			}
			this.readout();
		}

		// the last time we need to wait at least the delaytime to be sure of readout from the camera
		Thread.sleep(pauseTime);

		// Wait for the frames to be read out and then enable the data sever again
		int flatSaverFileNumber = flatSaver.getFileNumber();

		while (flatSaverFileNumber < numberOfFlats) {
			Thread.sleep(1000);
			flatSaverFileNumber = flatSaver.getFileNumber();
		}

		scanSaveNumber = tempFileNumber;

		// de/enable the savers the savers
		dataSaver.setEnable(true);
		flatSaver.setEnable(false);
		darkSaver.setEnable(false);

	}


	public void preview(double collectionTime) throws CAException, TimeoutException, InterruptedException, DeviceException {
		// stop the camera first
		areaDetector.stop();

		// make sure all the data savers are off
		// disable the savers the savers
		dataSaver.setEnable(false);
		flatSaver.setEnable(false);
		darkSaver.setEnable(false);

		// make sure everything is set up
		areaDetectorROI.setEnable(true);
		previewROI.setDataType("UInt32");
		previewROI.setUse(true);
		image.setEnable(true);

		// set the acquisition time
		setCollectionTime(collectionTime);
		// set the preview thread running
		live.start();

		// set image mode to continuous and start the camera;
		areaDetector.setImageMode(2);
		// pause after the image mode is set to allow the camera to set the data correctly
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		//trigger();
		areaDetector.acquire();
	}

	private void stopPreview() throws CAException, InterruptedException, TimeoutException {
		// stop the camera
		areaDetector.stop();

		// turn off the live view
		live.stop();
	}

}
