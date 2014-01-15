/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

import java.io.IOException;

import gda.device.DeviceException;
import gda.device.detector.areadetector.v17.ADBase;
import gda.device.detector.areadetector.v17.ADBase.ImageMode;
import gda.epics.LazyPVFactory;
import gda.epics.PV;
import gda.jython.InterfaceProvider;
import gda.scan.ScanInformation;


public class PixiumSimpleAcquire extends SimpleAcquire {
	
	double maxExposureTime = Double.MAX_VALUE;
	double exposureTime = maxExposureTime;
	
	int numberExposuresPerImage = 1;
	/**
	 * To allow for detectors with prefix acquire give option to read it at prepareForCollection
	 */
	private boolean readAcquireTimeFromHardware = true;
	
	private String calibrationRequiredPVName = "BL12I-EA-DET-10:CAM:CalibrationRequired_RBV";
	private PV<Integer> calibrationRequiredPV;
	
	private String numberExposuresPerImagePVName = "BL12I-EA-DET-10:CAM:NumExposures";
	private PV<Integer> numberExposuresPerImagePV;
	
	public PixiumSimpleAcquire(ADBase adBase, double readoutTime) {
		super(adBase, readoutTime);
		
		if (calibrationRequiredPV == null) {
			calibrationRequiredPV = LazyPVFactory.newIntegerPV(calibrationRequiredPVName);
		}
		if (numberExposuresPerImagePV == null) {
			numberExposuresPerImagePV = LazyPVFactory.newIntegerPV(numberExposuresPerImagePVName);
		}
	}
	
	public PV<Integer> getCalibrationRequiredPV() {
		if (calibrationRequiredPV == null) {
			calibrationRequiredPV = LazyPVFactory.newIntegerPV(calibrationRequiredPVName);
		}
		return calibrationRequiredPV;
	}
	
	public PV<Integer> getNumberExposuresPerImagePV() {
		if (numberExposuresPerImagePV == null) {
			numberExposuresPerImagePV = LazyPVFactory.newIntegerPV(numberExposuresPerImagePVName);
		}
		return numberExposuresPerImagePV;
	}

	@Override
	public void prepareForCollection(double collectionTime, int numImages_IGNORED, ScanInformation scanInfo_IGNORED) throws Exception {
		if (getCalibrationRequiredPV().get() != 0) {
			throw new DeviceException("Detector calibration required!");
		} 
		enableOrDisableCallbacks();
		double localExposureTime = getExposureTime();
		numberExposuresPerImage = calcNumberExposuresPerImage(collectionTime, localExposureTime);
		getAdBase().stopAcquiring(); 
		setNumExposuresPerImage(numberExposuresPerImage);
		getAdBase().setNumImages(1);
		getAdBase().setImageModeWait(numberExposuresPerImage > 1 ? ImageMode.MULTIPLE : ImageMode.SINGLE);
	}
	
	public boolean isReadAcquireTimeFromHardware() {
		return readAcquireTimeFromHardware;
	}

	public void setReadAcquireTimeFromHardware(boolean readAcquireTimeFromHardware) {
		this.readAcquireTimeFromHardware = readAcquireTimeFromHardware;
	}
	
	public double getMaxExposureTime() {
		return maxExposureTime;
	}

	public void setMaxExposureTime(double maxExposureTime) {
		this.maxExposureTime = maxExposureTime;
	}

	public double getExposureTime() throws Exception {
		return readAcquireTimeFromHardware ? getAdBase().getAcquireTime_RBV() : exposureTime;
	}

	public void setExposureTime(double exposureTime) {
		if( exposureTime > maxExposureTime)
			throw new IllegalArgumentException("Unable to set exposure time to " + exposureTime + ". max value = " + maxExposureTime);
		this.exposureTime = exposureTime;
	}
	
	@Override
	public double getAcquireTime() throws Exception {
		return getAdBase().getAcquireTime_RBV()*numberExposuresPerImage;
	}

	@Override
	public double getAcquirePeriod() throws Exception {
		return getAdBase().getAcquirePeriod_RBV()*numberExposuresPerImage;
	}
	
	public int getNumberExposuresPerImage(double collectionTime){
		try{
			double localExposureTime = getExposureTime();
			return calcNumberExposuresPerImage(collectionTime, localExposureTime);
		} catch (Exception e){
			throw new IllegalArgumentException("Error in getNumberExposuresPerImage",e);
		}
	}
	
	protected int calcNumberExposuresPerImage(double collectionTime, double exposureTime) {
		if (exposureTime > 0 && collectionTime > exposureTime)
			return (int)(collectionTime/exposureTime + 0.5);
		return 1;
	}
	
	protected void setNumExposuresPerImage(int numExposuresPerImage) {
		if (numExposuresPerImage > 0) {
			try {
				getNumberExposuresPerImagePV().putWait(numExposuresPerImage);
			} catch (IOException e) {
				throw new IllegalArgumentException("Error in setNumExposuresPerImage",e);
			}
		} else {
			throw new IllegalArgumentException("Unable to set exposures-per-image to " + numExposuresPerImage);
		}
	}
	
	/**
	 * method to print message to the Jython Terminal console.
	 * 
	 * @param msg
	 */
	private void print(String msg) {
		if (InterfaceProvider.getTerminalPrinter() != null) {
			InterfaceProvider.getTerminalPrinter().print(msg);
		}
	}
}
