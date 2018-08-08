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

package gda.device.detector.uviewnew;

import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.detector.uviewnew.UViewController.ImageFile;
import gda.device.detector.uviewnew.UViewController.ImageFile.ImageContentsType;
import gda.device.detector.uviewnew.UViewController.ImageFile.ImageFormat;
import gda.device.detector.uviewnew.corba.impl.CorbaBridgeConnection;

import java.awt.Rectangle;
import java.io.IOException;
import java.util.Observable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A UView Image Client class
 */
public class UViewImageController extends Observable {
	private static final Logger logger = LoggerFactory.getLogger(UViewImageController.class);

	private Thread statusMonitor;

	double exposureTime = 0.1; // default exposureTime in unit second

	String corbaBridgeName = null;
	
	String address;
	int port;

	CorbaBridgeConnection bridge = null;

	UViewClient uvc = null;

	UViewFile uvf = null;

	ImageFormat format = ImageFormat.PNG;
	ImageContentsType imageContents = ImageContentsType.GRAYLEVEL16; // 16 bits graylevel x, y, z raw data

	String imageFileName;

	private int currentDetectorStatus = Detector.STANDBY;

	private int previousDetectorStatus = Detector.STANDBY;

	//Map<String,Short> mp=new HashMap<String, Short>();

	public UViewImageController(String address, int port) throws DeviceException {
		this.corbaBridgeName = address;
		statusMonitor = new Thread(this::runStatusMonitor);
		statusMonitor.start();
		this.port = port;
		this.address = address;
		this.connect();
		this.init();

		currentDetectorStatus = Detector.IDLE;
	}

	/**
	 * Connect UViewImageClient to Server by using the given Corba bridge
	 */
	private void connect() {
		uvc = new UViewClient();
		/*
		boolean uvcs = uvc.connect(corbaBridgeName);
		if (uvcs) {
			logger.info("UViewClient created!");
		} else {
			logger.error("UViewClient Can NOT be created!");
		}
		*/
		uvc.initializeTcpController(address, port);
	}
	
	public void reconnect() {
		uvc.disconnect();
		uvc.initializeTcpController(address, port);
	}

	/**
	 * Initialise image and create new image file
	 * @throws DeviceException 
	 */
	private void init() throws DeviceException {
		// adding or set elements in Map by put method key and value pair
		//mp.put("dat",  new Short( (short)0) );
		//mp.put("png",  new Short( (short)1) );
		//mp.put("tiff", new Short( (short)2) );
		//mp.put("bmp",  new Short( (short)3) );
		//mp.put("jpg",  new Short( (short)4) );
		//mp.put("tif",  new Short( (short)5) );

		uvc.setupImage(1); // use buffered image
		uvc.getGreyLevel(); // get current grey windows value without adjustment
		try {
			//uvf = new UViewFile("ui", "png");
			uvf = new UViewFile("ui", ImageFormat.PNG);
		} catch (IOException e) {
			throw new DeviceException(e);
		}
		
		setFileFormat(ImageFormat.PNG, ImageContentsType.GRAYLEVEL16 );
	}

	/**
	 * @return UViewClient
	 */
	public UViewClient getUViewClient() {
		return uvc;
	}

	
	public int getImageAverageNumber() throws DeviceException {
		return uvc.getAverageImageNumber();
	}

	public void setImageAverageNumber(int numberOfImages) throws DeviceException {
		uvc.setAverageImageNumber(numberOfImages);
	}
	
	
	/**
	 * Set detector exposure time
	 * 
	 * @param exposureTime
	 * @throws DeviceException 
	 */
	public void setExposureTime(double exposureTime) throws DeviceException {

		this.exposureTime = exposureTime;
		uvc.setCameraExposureTime(exposureTime);
	}

	public double getExposureTime() throws DeviceException {
		this.exposureTime = uvc.getCameraExposureTime();
		return this.exposureTime;
	}

	public String getImage() {
		imageFileName = uvf.getCurrentFullFileName();

		int len = uvc.getPixelData16();
		uvc.assembleImage(len);
		uvc.saveImage(imageFileName);

		return imageFileName;
	}

	public void setFileFormat(ImageFormat format, ImageContentsType imageContents) {
		uvf.setFileExtenstion(format);
		this.format = format;
		this.imageContents = imageContents;
	}

	/**
	 * Export UView image as file from UView Host directly
	 * 
	 * @return image file name
	 * @throws DeviceException 
	 */
	public String exportImage() throws DeviceException {
		imageFileName = uvf.getCurrentFullFileName();

		String imageFileNameDetector = uvf.getCurrentDetectorFileName();
		System.out.println("GDA File Name: " + imageFileName);
		System.out.println("Detector File Name: " + imageFileNameDetector);
		
		//uvc.exportImage(imageFileNameDetector, format, imagecontents);
		ImageFile fileDetails = new ImageFile(imageFileNameDetector, format, imageContents);
		uvc.exportImage(fileDetails);
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

	public void setROI(int id, Rectangle rect) throws DeviceException {
		this.setROI(id, rect.x, rect.y, rect.width, rect.height);
	}

	public void setROI(int id, int x, int y, int width, int height) throws DeviceException {
		uvc.setROI(id, x, y, width, height);
		// uvc.setROIPlot();
	}

	public double getROIData(int id) throws DeviceException {
		return uvc.getROIData(id);
	}

	/**
	 * To set camera acquisition submode sequential=true or simultaneous=false
	 * 
	 * @param bs
	 *            boolean true if sequential mode else false
	 * @throws DeviceException 
	 */
	public void setCameraSequentialMode(boolean bs) throws DeviceException {
		uvc.setCameraSequentialMode(bs);
	}


	/**
	 * To set camera recorder mode on/off using true/false
	 * 
	 * @param bs
	 *            boolean true for recorder ON, false for OFF
	 * @throws DeviceException 
	 */
	public void setCameraInProgress(boolean bs) throws DeviceException {
		uvc.setInProgress(bs);
	}

	
	
	/**
	 * Prepare by creating a new image directory based on the tracker
	 * @throws DeviceException 
	 */
	public void prepare(String tracker, String imageDir) throws DeviceException {
		try {
			uvf.newImageDir(tracker, imageDir);
		} catch (IOException e) {
			throw new DeviceException(e);
		}
		// Turn the continuous image acquisition OFF, the same as to click the RED button on UView Camera Control Panel)
		uvc.setCameraSequentialMode(true);
	}
	
	/**
	 * Prepare by creating a new image directory based on the scan run number
	 * @throws DeviceException 
	 */
	public void prepareForScan() throws DeviceException {
		try {
			uvf.setupScanDir();
		} catch (IOException e) {
			throw new DeviceException(e);
		}
		// Turn the continuous image acquisition OFF, the same as to click the RED button on UView Camera Control Panel)
		uvc.setCameraSequentialMode(true);
	}

	/**
	 * Setup camera and take single image
	 * @throws DeviceException 
	 */
	public void trigger() throws DeviceException {
//		logger.debug("Shot one image");
		currentDetectorStatus = Detector.BUSY;
		uvc.shotSingleImage(-1);
	}

	/**
	 * @return Returns the detector status (as per detector interface)
	 * @throws DeviceException 
	 */
	public int getDetectorStatus() throws DeviceException {
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
	 * @throws DeviceException 
	 */
	public boolean isImageReady() throws DeviceException {
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
	private void runStatusMonitor() {
		/*
		 * Find out if status changes have occured.if so keep a record and pass on the information.
		 */
		while (true) {
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
		}
	}

}