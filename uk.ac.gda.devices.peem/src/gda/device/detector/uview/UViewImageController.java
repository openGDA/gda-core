/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

package gda.device.detector.uview;

import gda.device.Detector;
import gda.device.detector.uview.corba.impl.CorbaBridgeConnection;

import java.awt.Rectangle;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A UView Image Client class
 */
public class UViewImageController extends Observable implements Runnable {
	private static final Logger logger = LoggerFactory.getLogger(UViewImageController.class);

	private Thread statusMonitor;

	double exposureTime = 0.1; // default exposureTime in unit second

	String corbaBridgeName = null;

	CorbaBridgeConnection bridge = null;

	UViewClient uvc = null;

	UViewFile uvf = null;

	short format = 1; // Default file format PNG format
	short imagecontents = 2; // 16 bits graylevel x, y, z raw data

	String imageFileName;

	private int currentDetectorStatus = Detector.STANDBY;

	private int previousDetectorStatus = Detector.STANDBY;
	
	
	
    Map<String,Short> mp=new HashMap<String, Short>();

	/**
	 * Construct a new UViewImageClient using the Corba bridge name supplied
	 * 
	 * @param corbaBridgeName
	 */
	public UViewImageController(String corbaBridgeName) {
		this.corbaBridgeName = corbaBridgeName;
		statusMonitor = uk.ac.gda.util.ThreadManager.getThread(this);
		statusMonitor.start();

		this.connect();
		this.init();

		currentDetectorStatus = Detector.IDLE;
	}

	/**
	 * Connect UViewImageClient to Server by using the given Corba bridge
	 */
	private void connect() {
		uvc = new UViewClient();
		boolean uvcs = uvc.connect(corbaBridgeName);
		if (uvcs) {
			logger.info("UViewClient created!");
		} else {
			logger.error("UViewClient Can NOT be created!");
		}
	}

	/**
	 * Initialise image and create new image file
	 */
	private void init() {
	    // adding or set elements in Map by put method key and value pair
	    mp.put("dat",  new Short( (short)0) );
	    mp.put("png",  new Short( (short)1) );
	    mp.put("tiff", new Short( (short)2) );
	    mp.put("bmp",  new Short( (short)3) );
	    mp.put("jpg",  new Short( (short)4) );
	    mp.put("tif",  new Short( (short)5) );

		uvc.setupImage(1); // use buffered image
		uvc.adjustGreyLevel((short) 0); // get current grey windows value without adjustment
		uvf = new UViewFile("ui", "png");
		
		setFileFormat("png", (short)2 );
	    
	}

	/**
	 * @return UViewClient
	 */
	public UViewClient getUViewClient() {
		return uvc;
	}

	
	public int getImageAverageNumber() {
		return uvc.getAverageImageNumber();
	}

	public void setImageAverageNumber(int numberOfImages) {
		uvc.setAverageImageNumber(numberOfImages);
		
	}
	
	
	/**
	 * Set detector exposure time
	 * 
	 * @param exposureTime
	 */
	public void setExposureTime(double exposureTime) {

		this.exposureTime = exposureTime;
		uvc.setCameraExposureTime(exposureTime);
	}

	/**
	 * Get exposure time
	 * 
	 * @return double exposure time
	 */
	public double getExposureTime() {
		this.exposureTime = uvc.getCameraExposureTime();
		return this.exposureTime;
	}

	/**
	 * Get image from UViewClient
	 * 
	 * @return String image file name
	 */
	public String getImage() {
		imageFileName = uvf.getCurrentFullFileName();

		// Get the image, assemble it and save
		int len = uvc.getPixelData16();
		uvc.assembleImage(len);
		uvc.saveImage(imageFileName);

		return imageFileName;
	}

	public void setFileFormat(String fileExtension, short imagecontents){
		uvf.setFileExtenstion(fileExtension);
		format = mp.get(fileExtension).shortValue();
		this.imagecontents = imagecontents;

		
	}

	/**
	 * Export UView image as file from UView Host directly
	 * 
	 * @return image file name
	 */
	public String exportImage() {
		imageFileName = uvf.getCurrentFullFileName();

		String imageFileNameDetector = uvf.getCurrentDetectorFileName();
		System.out.println("GDA File Name: " + imageFileName);
		System.out.println("Detector File Name: " + imageFileNameDetector);
		
		uvc.exportImage(imageFileNameDetector, format, imagecontents);
		return imageFileName;
	}

	/**
	 * Get ROI image from UViewClient
	 * 
	 * @return String image file name
	 */
	public String getImageROI() {
		imageFileName = uvf.getCurrentFullFileName();

		// Get the image, assemble it and save
		int len = uvc.getPixelData16();
		uvc.assembleImage(len);
		uvc.saveImage(imageFileName);

		return imageFileName;
	}

	/**
	 * @param id
	 * @param rect
	 */
	public void setROI(int id, Rectangle rect) {
		this.setROI(id, rect.x, rect.y, rect.width, rect.height);
	}

	/**
	 * @param id
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	public void setROI(int id, int x, int y, int width, int height) {
		uvc.setROI(id, x, y, width, height);
		// uvc.setROIPlot();
	}

	/**
	 * @param id
	 * @return ROI data
	 */
	public double getROIData(int id) {
		return uvc.getROIData(id);
	}

	/**
	 * To set camera acquisition submode sequential=true or simulataneous=false
	 * 
	 * @param bs
	 *            boolean true if sequential mode else false
	 */
	public void setCameraSequentialMode(boolean bs) {
		uvc.setCameraSequentialMode(bs);
	}


	/**
	 * To set camera recorder mode on/off using true/false
	 * 
	 * @param bs
	 *            boolean true for recorder ON, false for OFF
	 */
	public void setCameraInProgress(boolean bs) {
		uvc.setInProgress(bs);
	}

	
	
	/**
	 * Prepare by creating a new image directory based on the tracker
	 */
	public void prepare(String tracker, String imageDir) {
		uvf.newImageDir(tracker, imageDir);
		// Turn the continuous image acquisition OFF, the same as to click the RED button on UView Camera Control Panel)
		uvc.setCameraSequentialMode(true);
	}
	
	/**
	 * Prepare by creating a new image directory based on the scan run number
	 */
	public void prepareForScan() {
		uvf.setupScanDir();
		// Turn the continuous image acquisition OFF, the same as to click the RED button on UView Camera Control Panel)
		uvc.setCameraSequentialMode(true);
	}

	/**
	 * Setup camera and take single image
	 */
	public void trigger() {
//		logger.debug("Shot one image");
		currentDetectorStatus = Detector.BUSY;
		uvc.setInProgress(false);
		uvc.shotSingleImage(-1);
	}

	/**
	 * @return Returns the detector status (as per detector interface)
	 */
	public int getDetectorStatus() {
		boolean inProgress = uvc.isInProgress();
		if (inProgress) { //Detector Status: the UView camera is IN Progress"
			currentDetectorStatus = Detector.BUSY;
		} else {//Detector Status: the UView camera is NOT IN Progress"
			currentDetectorStatus = Detector.IDLE;
		}

		return currentDetectorStatus;
	}

	/**
	 * @return boolean true if image ready
	 */
	public boolean isImageReady() {
		boolean imageReady = uvc.isImageReady();
		if (imageReady) {
			logger.debug("UView image data is ready for collection.");
		} else {
			logger.debug("UView image data is NOT ready for collection.");
		}
		return imageReady;
	}

	/**
	 * Thread which checks on the status of the detector
	 */
	@Override
	public void run() {
		/*
		 * Find out if status changes have occured.if so keep a record and pass on the information.
		 */
		do {
			// this.sim();
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}

			if (currentDetectorStatus != previousDetectorStatus) {
				previousDetectorStatus = currentDetectorStatus;
				setChanged();
				notifyObservers(currentDetectorStatus);
			}
		} while (true);
	}

}