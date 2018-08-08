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
import gda.device.DeviceException;
import gda.device.UView;
import gda.device.detector.DetectorBase;

import java.awt.Rectangle;
import java.io.IOException;
import java.util.Hashtable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * UViewImageDetector Class
 */
public class UViewImageDetector extends DetectorBase implements UView {

	private static final Logger logger = LoggerFactory.getLogger(UViewImageDetector.class);

	String corbaBridgeName = null;

	UViewImageController uic = null;
	
	protected String tracker = null;
	protected String imageDir = null;
	

	/**
	 * 
	 */
	public static Hashtable<String, UViewImageROI> hashROIs = new Hashtable<String, UViewImageROI>(10, 10);

	/**
	 * 
	 */
	public static int numberROIs = 0;

	/**
	 * @return UViewImageController
	 */
	public UViewImageController getUViewImageController() {
		return uic;
	}

	/**
	 * @return String Corba bridge name
	 */
	public String getCorbaBridgeName() {
		return corbaBridgeName;
	}

	/**
	 * @param corbaBridgeName
	 *            String
	 */
	public void setCorbaBridgeName(String corbaBridgeName) {
		this.corbaBridgeName = corbaBridgeName;
	}

	
	public String getTracker() {
		return this.tracker;
	}
	
	public void setTracker(String tracker) {
		this.tracker = tracker;
	}
	public String getImageDir() {
		return this.imageDir;
	}
	
	public void setImageDir(String imageDir) {
		this.imageDir = imageDir;
	}
	
	@Override
	public void configure() {
		uic = new UViewImageController(corbaBridgeName);
		this.collectionTime = uic.getExposureTime();
		this.prepare();
	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		return true;
	}

	@Override
	public void setCollectionTime(double collectionTime) {
		this.collectionTime = collectionTime;
		uic.setExposureTime(collectionTime);
	}

	@Override
	public double getCollectionTime() {
		this.collectionTime = uic.getExposureTime();
		return this.collectionTime;
	}

	@Override
	public void collectData() throws DeviceException {
//		logger.debug("Uview Start to collect");
		uic.trigger();
	}

	@Override
	public int getStatus() throws DeviceException {
		return uic.getDetectorStatus();
	}

	@Override
	public Object readout() throws DeviceException {
		return uic.exportImage();
	}

	@Override
	public void prepareForCollection() throws DeviceException {
		uic.prepareForScan();
	}
	
	@Override
	public void endCollection() throws DeviceException {
		uic.prepare(tracker, imageDir);
	}
	
	@Override
	public void prepare(){
		uic.prepare(tracker, imageDir);
	}
	
	public void setCameraSequentialMode(boolean bs) {
		uic.setCameraSequentialMode(bs);
	}

	public void setCameraInProgress(boolean bs) {
		uic.setCameraInProgress(bs);
	}


	/**
	 *  imageNumber:
	 *  	-1: Sliding average
	 *  	 0: No average
	 *  	1 ~ 99: Number of frames to be averaged
	 */
	public void setImageAverageNumber(int imageNumber){
		uic.setImageAverageNumber(imageNumber);
	}
	
	public int getImageAverageNumber(){
		return uic.getImageAverageNumber();
	}

	/**
	 *  fileExtension:
	 *  	dat: Raw data uncompressed
	 *  	png: PNG compressed
	 *  	tiff: TIFF compressed
	 *  	bmp: BMP uncompressed
	 *  	jpg: JPG compressed 
	 *  	tif: TIFF uncompressed
	 *  
	 *  imagecontent:
	 *  	0: RGB 8+8+8 bits x,y,z, as seen on screen
	 *  	1: RGB 8+8+8 bits,x,y raw, z as seen on screen
	 *  	2: RAW 16 bits graylevel x, y, z raw data
	 */
	public void setFileFormat(String fileExtension, int imagecontents){
		uic.setFileFormat(fileExtension, (short)imagecontents);
	}
	
	
	
	@Override
	public String shotSingleImage() throws IOException {
		int status;
		try {
			switch (status = this.getStatus()) {
			case Detector.IDLE:
				this.collectData();
				break;
			case Detector.BUSY:
				this.collectData();
				logger.error("UView camera is currently busy and the ongoing activity is disrupted.");
				break;
			default:
			}

			do {
				status = this.getStatus();
				if (status == Detector.IDLE) {
					// return (String)uic.getImage();
					return uic.exportImage();
				}
				Thread.sleep(100);

			} while (status == Detector.BUSY);

		} catch (DeviceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return "Aaa! Wrong Detector Status. Not working!";
	}

	@Override
	public void connect(String host) throws IOException {
		// TODO Auto-generated method stub
	}

	@Override
	public void disconnect() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isConnected() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public Object getHashROIs() throws IOException {

		return hashROIs;
	}

	@Override
	public int createROI(String nameROI) {
		int idROI = 0;

		if (hashROIs.containsKey(nameROI)) {
			logger.info(nameROI + " already exists.");
			
		} else {
			if (numberROIs >= 5) {
				logger.info("No more ROI available");
				return 0;
			}

			idROI = ++numberROIs;
			int gap = 80;
			Rectangle rectROI = new Rectangle(idROI * 10 + (idROI - 1) * gap, 0, 50, 50);

			// key: nameROI;
			// value: UViewROI;
			hashROIs.put(nameROI, new UViewImageROI(idROI, rectROI));

			uic.setROI(idROI, rectROI);
		}
		
		notifyROIChange(nameROI);
		return idROI;
	}

	@Override
	public void setBoundsROI(String nameROI, int x, int y, int width, int height) {

		if (hashROIs.containsKey(nameROI)) {
			int idROI = hashROIs.get(nameROI).getID();
			hashROIs.get(nameROI).setROI(x, y, width, height);
			uic.setROI(idROI, x, y, width, height);

			notifyROIChange(nameROI);

		} else {
			System.out.println("Wrong name. " + nameROI + " does not exist.");
		}

	}

	@Override
	public Object getBoundsROI(String nameROI) throws IOException {
		Rectangle rect = null;
		if (hashROIs.containsKey(nameROI)) {
			rect = hashROIs.get(nameROI).getROI();
		} else {
			System.out.println("Wrong name. " + nameROI + " does not exist.");
		}
		return rect;
	}

	@Override
	public Object readoutROI(String nameROI) {
		double dataROI = -100;

		if (hashROIs.containsKey(nameROI)) {
			int idROI = hashROIs.get(nameROI).getID();

			dataROI = uic.getROIData(idROI);
			System.out.println("Reading from " + nameROI + " is: " + dataROI);
		} else
			System.out.println("Wrong name. " + nameROI + " does not exist.");

		return dataROI;
	}

	/**
	 * @param nameROI
	 */
	public void notifyROIChange(String nameROI) {
		System.out.println("From UViewDetector: ROI changed");
		this.notifyIObservers(this, nameROI);
	}

	@Override
	public String getDescription() throws DeviceException {
		return "UView Image Detector";
	}

	@Override
	public String getDetectorID() throws DeviceException {
		return "unknown";
	}

	@Override
	public String getDetectorType() throws DeviceException {
		return "unknown";
	}

}
