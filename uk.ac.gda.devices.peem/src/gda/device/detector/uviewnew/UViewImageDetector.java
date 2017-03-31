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
import gda.device.UViewNew;
import gda.device.detector.DetectorBase;
import gda.device.detector.uviewnew.UViewController.ImageFile.ImageContentsType;
import gda.device.detector.uviewnew.UViewController.ImageFile.ImageFormat;
import gda.device.detector.uviewnew.UViewController.RegionOfInterest;

import java.awt.Rectangle;
import java.util.Hashtable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * UViewImageDetector Class
 */
public class UViewImageDetector extends DetectorBase implements UViewNew {

	private static final Logger logger = LoggerFactory.getLogger(UViewImageDetector.class);

	String address = null;
	int port;

	UViewImageController uic = null;

	protected String tracker = null;
	protected String imageDir = null;
	

	public Hashtable<String, UViewImageROI> hashROIs = new Hashtable<String, UViewImageROI>(10, 10);

	public int numberROIs = 0;

	public UViewImageController getUViewImageController() {
		return uic;
	}

	public String getAddress() {
		return this.address;
	}
	
	public void setAddress(String address) {
		this.address = address;
	}
	
	public void setPort(int port) {
		this.port = port;
	}
	public int getPort() {
		return this.port;
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
		try {
			uic = new UViewImageController(address, port);
			this.collectionTime = uic.getExposureTime();
			this.prepare();
		} catch (DeviceException e) {
			logger.error("Cannot configure connection to UView.", e);
		}
	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		return true;
	}

	@Override
	public void setCollectionTime(double collectionTime) throws DeviceException {
		this.collectionTime = collectionTime;
		uic.setExposureTime(collectionTime);
	}

	@Override
	public double getCollectionTime() {
		try {
			this.collectionTime = uic.getExposureTime();
		} catch (DeviceException e) {
			// TODO Auto-generated catch block
			logger.error("TODO put description of error here", e);
		}
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
	public void prepare() throws DeviceException{
		uic.prepare(tracker, imageDir);
	}
	
	public void setCameraSequentialMode(boolean bs) throws DeviceException {
		uic.setCameraSequentialMode(bs);
	}

	public void setCameraInProgress(boolean bs) throws DeviceException {
		uic.setCameraInProgress(bs);
	}


	/**
	 *  imageNumber:
	 *  	-1: Sliding average
	 *  	 0: No average
	 *  	1 ~ 99: Number of frames to be averaged
	 * @throws DeviceException 
	 */
	public void setImageAverageNumber(int imageNumber) throws DeviceException{
		uic.setImageAverageNumber(imageNumber);
	}
	
	public int getImageAverageNumber() throws DeviceException{
		return uic.getImageAverageNumber();
	}

	public void setFileFormat(ImageFormat format, ImageContentsType imageContents){
		//uic.setFileFormat(fileExtension, (short)imagecontents);
		uic.setFileFormat(format, imageContents);
	}



	@Override
	public String shotSingleImage() throws InterruptedException, DeviceException {
		if (this.getStatus() == Detector.BUSY) {
			logger.error("UView camera is currently busy and the ongoing activity is disrupted.");
		}
		this.collectData();

		while (this.getStatus() == Detector.BUSY) {
			Thread.sleep(100);
		}
		return uic.exportImage();
	}

	@Override
	public void connect(String host) throws DeviceException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void disconnect() {
		throw new UnsupportedOperationException();

	}
	
	public void reconnect() {
		uic.reconnect();
	}

	@Override
	public boolean isConnected() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object getHashROIs() {
		return hashROIs;
	}
	
	@Override
	public int createROI(String nameROI) throws DeviceException {
//		int idROI = 0;
//
//		if (hashROIs.containsKey(nameROI)) {
//			logger.info(nameROI + " already exists.");
//			idROI = hashROIs.get(nameROI).id;
//		} else {
//			if (numberROIs >= 5) {
//				logger.info("No more ROI available");
//				return 0;
//			}
//
//			idROI = ++numberROIs;
//			int gap = 80;
//			Rectangle rectROI = new Rectangle(idROI * 10 + (idROI - 1) * gap, 0, 50, 50);
//
//			hashROIs.put(nameROI, new UViewImageROI(idROI, rectROI));
//
//			uic.setROI(idROI, rectROI);
//		}
//
//		notifyROIChange(nameROI);
//		return idROI;
		int gap = 80;
		return createROI(nameROI, numberROIs * 10 + (numberROIs - 1) * gap, 0, 50, 50);
	}
	
	public int createROI(String nameROI, int x, int y, int width, int height) throws DeviceException {
		int idROI = 0;
		UViewImageROI roi = hashROIs.get(nameROI);
		if (roi == null) {
			if (numberROIs >= 5) {
				logger.info("No more ROI available");
				return 0;
			}

			idROI = ++numberROIs;
			Rectangle rectROI = new Rectangle(x, y, width, height);

			hashROIs.put(nameROI, new UViewImageROI(idROI, rectROI));

			uic.setROI(idROI, rectROI);
		} else {
			idROI = roi.id;
			roi.setROI(x, y, width, height);
			uic.setROI(roi.id, x, y, width, height);
		}
		notifyROIChange(nameROI);
		return idROI;
	}
	public void activateROI(String nameROI) throws DeviceException {
		UViewImageROI uviroi=hashROIs.get(nameROI);
		if (uviroi!= null) {
			uic.uvc.activateROI(new RegionOfInterest(uviroi.getROI(), uviroi.getID()));
		} else {
			throw new DeviceException("Regin of interest '"+nameROI+"' is not defined.");
		}
	}
	public void deactivateROI(String nameROI) throws DeviceException {
		UViewImageROI uviroi=hashROIs.get(nameROI);
		if (uviroi!= null) {
			uic.uvc.deactivateROI(new RegionOfInterest(uviroi.getROI(), uviroi.getID()));
		} else {
			throw new DeviceException("Regin of interest '"+nameROI+"' is not defined.");
		}
	}
	public boolean isROIActive(String nameROI) throws DeviceException {
		UViewImageROI uviroi=hashROIs.get(nameROI);
		if (uviroi!= null) {
			return uic.uvc.isROIActive(new RegionOfInterest(uviroi.getROI(), uviroi.getID()));
		} 
		throw new DeviceException("Regin of interest '"+nameROI+"' is not defined.");
	}

	public UViewImageROI getROI(String nameROI) {
		return hashROIs.get(nameROI);
	}

	@Override
	public void setBoundsROI(String nameROI, int x, int y, int width, int height) throws DeviceException {

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
	public Object getBoundsROI(String nameROI) throws DeviceException {
		Rectangle rect = null;
		if (hashROIs.containsKey(nameROI)) {
			rect = hashROIs.get(nameROI).getROI();
		} else {
			System.out.println("Wrong name. " + nameROI + " does not exist.");
		}
		return rect;
	}

	@Override
	public Double readoutROI(String nameROI) throws DeviceException {
		double dataROI = -100;

		if (hashROIs.containsKey(nameROI)) {
			int idROI = hashROIs.get(nameROI).getID();

			dataROI = uic.getROIData(idROI);
			System.out.println("Reading from " + nameROI + " is: " + dataROI);
		} else
			System.out.println("Wrong name. " + nameROI + " does not exist.");

		return dataROI;
	}

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

	public String getVersion() throws DeviceException {
		return uic.getUViewClient().getVersion();
	}

	public void setPixelClock(int MHz) throws DeviceException {
		uic.uvc.setPixelClock(MHz);
	}
	public int getPixelClock() throws DeviceException {
		return uic.uvc.getPixelClock();
	}
	
	public enum TriggerMode {
		AUTO,
		SOFT,
		EXT_EXP_START,
		EXT_EXP_CNTRL
	}

	public void setTriggerMode(TriggerMode mode) throws DeviceException {
		int triggerMode = 0; 
		switch (mode) {
		case AUTO:
			triggerMode = 0;
			break;
		case SOFT:
			triggerMode = 1;
			break;
		case EXT_EXP_START:
			triggerMode=2;
			break;
		case EXT_EXP_CNTRL:
			triggerMode=3;
			break;
		}
		uic.uvc.setTriggerMode(triggerMode);
	}
	public TriggerMode getTriggerMode() throws DeviceException {
		int triggerMode = uic.uvc.getTriggerMode();
		switch (triggerMode) {
		case 0:
			return TriggerMode.AUTO;
		case 1:
			return TriggerMode.SOFT;
		case 2:
			return TriggerMode.EXT_EXP_START;
		case 3:
			return TriggerMode.EXT_EXP_CNTRL;
		default:
			throw new DeviceException("Unknow U-View Trigger Mode: "+triggerMode);
		}
	}
	
	public void setCameraADC(int adc) throws DeviceException {
		uic.uvc.setCameraADC(adc);
	}
	public int getCameraADC() throws DeviceException {
		return uic.uvc.getCameraADC();
	}
}
