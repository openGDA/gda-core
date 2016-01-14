/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

package gda.device.detector.areadetector;


import gda.configuration.properties.LocalProperties;
import gda.data.NumTracker;
import gda.data.PathConstructor;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.detector.DetectorBase;
import gda.device.scannable.ScannableUtils;
import gda.factory.FactoryException;
import gov.aps.jca.CAException;
import gov.aps.jca.TimeoutException;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AreaDetectorController extends DetectorBase implements Detector {

	// Setup the logging facilities
	transient private static final Logger logger = LoggerFactory.getLogger(AreaDetectorController.class);

	// Variables to hold the spring settings
	protected EpicsAreaDetector           areaDetector;
	protected EpicsAreaDetectorROI        areaDetectorROI;
	protected EpicsAreaDetectorFileSave   fullFrameSaver;
	protected EPICSAreaDetectorImage      image;

	// New Parameters
	long aquisitionStartTime = 0;
	long pauseTime = 0;

	boolean saveLocal = false;

	protected File fullImageLocation;
	protected int nextImageNumber=0;
	protected String localFilePath;

	protected String newImageDir = null;

	protected Integer idlePollTime_ms	= 100;

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

	public EpicsAreaDetectorFileSave getFullFrameSaver() {
		return fullFrameSaver;
	}

	public void setFullFrameSaver(EpicsAreaDetectorFileSave fullFrameSaver) {
		this.fullFrameSaver = fullFrameSaver;
	}

	public EPICSAreaDetectorImage getImage() {
		return image;
	}

	public void setImage(EPICSAreaDetectorImage image) {
		this.image = image;
	}

	public Integer getIdlePollTime_ms() {
		return idlePollTime_ms;
	}

	public void setIdlePollTime_ms(Integer idlePollTimeMs) {
		idlePollTime_ms = idlePollTimeMs;
	}

	public String getLocalFilePath() {
		return localFilePath;
	}

	public void setLocalFilePath(String localFilePath) {
		this.localFilePath = localFilePath;
	}

	// Methods for configurable interface and the reset method
	@Override
	public void configure() throws FactoryException{
		try {
			reset();
		} catch (Exception e) {
			throw new FactoryException("Failed to configure the Area Detector", e);
		}
	}

	public void reset() throws CAException, InterruptedException {

		// set up the saving facilities
		fullFrameSaver.setArrayPort(fullFrameSaver.getInitialArrayPort());
		fullFrameSaver.setArrayAddress(fullFrameSaver.getInitialArrayAddress());
		fullFrameSaver.setEnable(true);
		fullFrameSaver.setFileName(fullFrameSaver.getInitialFileName());
		fullFrameSaver.setFileTemplate(fullFrameSaver.getInitialFileTemplate());
		fullFrameSaver.setAutoSave("Yes");
		fullFrameSaver.setAutoIncrement("Yes");
		fullFrameSaver.setframeCounter(0);
		fullFrameSaver.setWriteMode("Single");

		// Set the area Detector code
		areaDetector.setArrayCounter(0);
		// set to single images
		areaDetector.setImageMode(0);


	}

	public void resetAll() throws CAException, InterruptedException{
		areaDetector.reset();
		areaDetectorROI.reset();
		fullFrameSaver.reset();
		image.reset();
		reset();
	}

	public void enableAll() throws CAException, InterruptedException {
		// Frame Saver
		fullFrameSaver.setEnable(true);
		fullFrameSaver.startCapture();

		// Make sure the preview channel is also available
		image.setEnable(true);

		// ROI Elements
		areaDetectorROI.setEnable(true);
	}


	public void trigger() throws CAException, InterruptedException {
		areaDetector.acquire();
	}

	public void prepare() throws DeviceException {
		try {
			// and sort out the real place to save to
			enableAll();
			fullImageLocation = getImageFolder();
			setFileSaverParameters(fullImageLocation);
		} catch (Exception e) {
			throw new DeviceException("Failed to prepare the Area Detector",e);
		}

	}

	public void singleShot() throws DeviceException {
		int status;
		try {
			status = this.getStatus();
			if (status != Detector.IDLE) {
				logger.error("AreaDetector is currently not idle.");
				return;
				}
			prepare();
			Thread.sleep(10);// wait a while to let the camera prepare itself.
			this.collectData();

			do {
				status = this.getStatus();
				if (status == Detector.IDLE) {
					stopAndCopy();
					return;
				}
				Thread.sleep(100);
			} while (status == Detector.BUSY);

		} catch (Exception e) {
			throw new DeviceException("Failed to use AreaDetector to take a single image ",e);
		}

	}


	@Override
	public void prepareForCollection() throws DeviceException {
		this.prepare();
	}

	@Override
	public void asynchronousMoveTo(Object collectionTime) throws DeviceException {
		throwExceptionIfInvalidTarget(collectionTime);
		Double[] collectionTimeArray = ScannableUtils.objectToArray(collectionTime);

		setCollectionTime(collectionTimeArray[0]);

		prepare();

		collectData();
	}

	// Methods which are required for the Detector Interface
	@Override
	public boolean createsOwnFiles() throws DeviceException {
		return true;
	}

	@Override
	public String getDescription() throws DeviceException {
		//TODO Parameterise this
		return "Area Detector" ;
	}

	@Override
	public String getDetectorID() throws DeviceException {
		return "Area Detector" ;
	}

	@Override
	public String getDetectorType() throws DeviceException {
		return "Area Detector" ;
	}

	@Override
	public double getCollectionTime() {
		try {
			this.collectionTime = areaDetector.getExpTime();
		} catch (Exception e) {
			logger.error("AreaDetector getCollectionTime failed with error",e);
		}
		return collectionTime;
	}

	@Override
	public void setCollectionTime(double collectionTime) throws DeviceException {
		this.collectionTime = collectionTime;
		try {
			areaDetector.setExpTime(collectionTime);
		} catch (Exception e) {
			if( e instanceof RuntimeException)
				throw (RuntimeException)e;
			throw new DeviceException(getName() + " exception in setCollectionTime",e);
		}
	}

	@Override
	public int getStatus() throws DeviceException {
		try {
			if(areaDetector.getState().equalsIgnoreCase("1")) {
				return Detector.BUSY;
			}
		} catch (Exception e) {
			throw new DeviceException("Failure to get Area Detector Status",e);
		}

		return Detector.IDLE;
	}


	@Override
	public void collectData() throws DeviceException {

		// make sure that the new time is at least the acquisition Time plus the pause
		long nextStartTime = (long) (aquisitionStartTime + pauseTime + (collectionTime*1000));
		long currentTime = System.currentTimeMillis();
		//InterfaceProvider.getTerminalPrinter().print(String.format("%d, %d", nextStartTime, currentTime));
		// wait for the correct amount of time, and then start
		if(nextStartTime > currentTime) {
			try {
				Thread.sleep(nextStartTime-currentTime);
			} catch (InterruptedException e) {
				throw new DeviceException("Failed to cause Thread.sleep",e);
			}
		}

		// trigger an acquisition
		try {
			// first make sure the detector is idle
			String state = areaDetector.getState();
			while(!state.equalsIgnoreCase("0")) {
				logger.debug("State of the camera is {}",state);
				state = areaDetector.getState();
				Thread.sleep(idlePollTime_ms);
			}

			// Having some issues with the camera, so adding in a small sleep here,
			// running the camera more slowly seems to help with EPICS so might be
			// an issue of the commands coming through too quickly with the new 10G
			// Card

//			areaDetector.acquire();
			trigger();

			nextImageNumber++;

			// now sleep for a small period to let the detector set itself to busy
			Thread.sleep(100);

		} catch (Exception e) {
			throw new DeviceException("Failed to collec data from the Area Detector",e);
		}


		aquisitionStartTime = System.currentTimeMillis();

	}


	@Override
	public Object readout() throws DeviceException {

		String output;

		try {
			String absolutePath = fullImageLocation.getAbsolutePath();
			String fileName = fullFrameSaver.getFileName();
			fileName = fileName.trim();

			output = String.format(fullFrameSaver.getFileTemplate(), absolutePath, fileName, nextImageNumber-1);

			// To register the file for auto backup
			//FileRegistrarHelper.registerFile(output);

		} catch (Exception e) {
			throw new DeviceException("Failure to readout PIXIS detector",e);
		}

		return output.trim();
	}

	// Methods for the Scannable interface
	@Override
	public void atScanStart() throws DeviceException {

		logger.debug("Starting Area Detector.AtScanStart");

		// first thing to do is stop the preview if its still running
		try {
			stopPreview();
		} catch (Exception e) {
			throw new DeviceException("Failed to stop preview",e);
		}

		pauseTime = 2000;

		try {
			prepare();

			// wait a while to let the camera prepare itself.
			Thread.sleep(100);

		} catch (Exception e) {
			throw new DeviceException("Failed to cause Area Detector to setup for scan",e);
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

	private void stopAndCopy() throws DeviceException {
		try {
			// stop the detector
//			Thread.sleep((pauseTime*2)+5000);
			areaDetector.stop();

			// copy the files off
//			if(saveLocal) {
//				Runtime rt = Runtime.getRuntime();
//				Process p = rt.exec(String.format("/dls_sw/i12/software/gda/config/bin/transfer.sh %s", fullImageLocation.getAbsolutePath()));
//				InterfaceProvider.getTerminalPrinter().print("Copying data to Central Storage");
//				p.waitFor();
//			}

		} catch (Exception e) {
			throw new DeviceException("Failed to run pco.stopandcopy scan",e);
		}

	}

	// Helper methods for dealing with the file system.
	public File createMainFileStructure() throws IOException {

		// set up the filename which will be the base directory for data to be saved to
		File path = new File(PathConstructor.createFromDefaultProperty());
		NumTracker nt;
		nt = new NumTracker("pixis");
		String filenumber = Long.toString(nt.getCurrentFileNumber());
		File scanFolder = new File(path, filenumber);
		// Make the directory if required.
		if(!scanFolder.isDirectory()) {
			// create the directory
			scanFolder.mkdir();
		}

		return scanFolder;
	}

	/**
	 * Find the image folder based on the current scan run number. If necessary create a new one.
	 * @throws InterruptedException
	 * @throws CAException
	 * @throws IOException
	 */
	public File getImageFolder() throws CAException, InterruptedException, IOException {
		NumTracker runs=null;
		runs = new NumTracker("tmp");
		long nextNum = 0L + runs.getCurrentFileNumber();


		newImageDir = PathConstructor.createFromDefaultProperty() + File.separator + nextNum + "_PixisImage";

		File fd = new File(newImageDir);
		if (!fd.exists()) {
			fd.mkdir();
			nextImageNumber = 0;
			fullFrameSaver.setframeCounter(0);
			logger.debug("New directory for Area Detector image created: " + newImageDir);
		}
		return fd;
	}


	public void setFileSaverParameters(File filePath) throws CAException, InterruptedException {
		// make the change for the windows filesystem and mount
		// TODO Definatly parameterise this

		String dataDirGDA = LocalProperties.get("gda.data.directoryMappingGDA");
		String dataDirDet = LocalProperties.get("gda.data.directoryMappingDetector");

		String filePathString = filePath.getAbsolutePath().replace(dataDirGDA, dataDirDet);
		fullFrameSaver.setFilePath(filePathString);
	}


	public void waitForIdle() throws TimeoutException, CAException, InterruptedException {
		String state = areaDetector.getState();
		while(!state.equalsIgnoreCase("0")) {
			logger.debug("State of the camera is {}",state);
			state = areaDetector.getState();
			Thread.sleep(idlePollTime_ms);
		}
	}


	public void preview(double collectionTime) throws CAException, InterruptedException, DeviceException {
		// stop the camera first
		areaDetector.stop();

		// make sure all the data savers are off
		// disable the savers the savers
		fullFrameSaver.setEnable(false);

		// make sure everything is set up
		areaDetectorROI.setEnable(true);
		image.setEnable(true);

		// set the acquisition time
		setCollectionTime(collectionTime);

		// set the preview thread running
//		live.start();

		// set image mode to continuous and start the camera;
		areaDetector.setImageMode(2);
		// pause after the image mode is set to allow the camera to set the data correctly
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		trigger();
	}


	private void stopPreview() throws CAException, InterruptedException{
		// stop the camera
		areaDetector.stop();

		// turn off the live view
//		live.stop();
	}


}

