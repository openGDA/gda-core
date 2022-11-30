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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.detector.areadetector.v17.ADBase;
import gda.device.detector.areadetector.v17.ImageMode;
import gda.epics.LazyPVFactory;
import gda.epics.PV;
import gda.scan.ScanInformation;

public class PixiumSimpleAcquire extends SimpleAcquire {

	private static final Logger logger = LoggerFactory.getLogger(PixiumSimpleAcquire.class);

	private double maxExposureTime = Double.MAX_VALUE;
	private double exposureTime = maxExposureTime;

	private int numberExposuresPerImage = 1;
	/**
	 * To allow for detectors with prefix acquire give option to read it at prepareForCollection
	 */
	private boolean readAcquireTimeFromHardware = true;
	private String prefix;

	private Integer backupDataType = 3; //Uint16
	private Integer backupEarlyFrames = 0; //Off

	private String calibrationRequiredPVName = "CalibrationRequired_RBV";
	private PV<Integer> calibrationRequiredPV;

	private String numberExposuresPerImagePVName = "NumExposures";
	private PV<Integer> numberExposuresPerImagePV;

	private boolean forceExcludeEarlyFramesToOn = false;
	private String earlyFramesPVName = "MotionBlur";
	private PV<Integer> earlyFramesPV;

	private String detectorStatePVName = "DetectorState_RBV";
	private PV<Integer> detectorStatePV;

	private String dataTypePVName = "DataType";
	private PV<Integer> dataTypePV;

	public PixiumSimpleAcquire(ADBase adBase, double readoutTime) {
		super(adBase, readoutTime);
	}

	public PV<Integer> getCalibrationRequiredPV() {
		if (calibrationRequiredPV == null) {
			calibrationRequiredPV = LazyPVFactory.newIntegerPV(getPrefix()+calibrationRequiredPVName);
		}
		return calibrationRequiredPV;
	}

	public PV<Integer> getNumberExposuresPerImagePV() {
		if (numberExposuresPerImagePV == null) {
			numberExposuresPerImagePV = LazyPVFactory.newIntegerPV(getPrefix()+numberExposuresPerImagePVName);
		}
		return numberExposuresPerImagePV;
	}

	public PV<Integer> getEarlyFramesPV() {
		if (earlyFramesPV == null) {
			earlyFramesPV = LazyPVFactory.newIntegerPV(getPrefix()+earlyFramesPVName);
		}
		return earlyFramesPV;
	}

	public PV<Integer> getDataTypePV() {
		if (dataTypePV == null) {
			dataTypePV = LazyPVFactory.newIntegerPV(getPrefix()+dataTypePVName);
		}
		return dataTypePV;
	}

	public PV<Integer> getDetectorStatePV() {
		if (detectorStatePV == null) {
			detectorStatePV = LazyPVFactory.newIntegerPV(getPrefix()+detectorStatePVName);
		}
		return detectorStatePV;
	}

	@Override
	public void prepareForCollection(double collectionTime, int numImages, ScanInformation scanInfo_IGNORED) throws Exception {
		if (getCalibrationRequiredPV().get() != 0) {
			throw new DeviceException("Detector calibration required!");
		}
		enableOrDisableCallbacks();
		double localExposureTime = getExposureTime();
		numberExposuresPerImage = calcNumberExposuresPerImage(collectionTime, localExposureTime);
		getAdBase().stopAcquiring();
		setNumExposuresPerImage(numberExposuresPerImage);
		getAdBase().setNumImages(numImages);
		//getAdBase().setNumImages(getTotalNumberScanImages(scanInfo_IGNORED));
		//getAdBase().setNumImages(getTotalNumberScanImages(InterfaceProvider.getCurrentScanInformationHolder().getCurrentScanInformation()));
		backupDataType = getDataTypePV().get();
		backupEarlyFrames = getEarlyFramesPV().get();
		if (numberExposuresPerImage > 1) {
			getAdBase().setDataType("UInt32");
			if (isForceExcludeEarlyFramesToOn())
			{
				excludeEarlyFrames();
			}
		} else {
			getAdBase().setDataType("UInt16");
			if (isForceExcludeEarlyFramesToOn())
			{
				excludeEarlyFrames();
			}
		}
		getAdBase().setImageModeWait(numImages > 1 ? ImageMode.MULTIPLE : ImageMode.SINGLE);
	}

    public boolean isReadAcquireTimeFromHardware() {
		return readAcquireTimeFromHardware;
	}

	public void setReadAcquireTimeFromHardware(boolean readAcquireTimeFromHardware) {
		this.readAcquireTimeFromHardware = readAcquireTimeFromHardware;
	}

	public boolean isForceExcludeEarlyFramesToOn() {
		return forceExcludeEarlyFramesToOn;
	}

	public void setForceExcludeEarlyFramesToOn(boolean forceExcludeEarlyFramesToOn) {
		this.forceExcludeEarlyFramesToOn = forceExcludeEarlyFramesToOn;
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
	@Override
	public void afterPropertiesSet() throws Exception {
		if (calibrationRequiredPV == null) {
			calibrationRequiredPV = LazyPVFactory.newIntegerPV(getPrefix()+calibrationRequiredPVName);
		}
		if (numberExposuresPerImagePV == null) {
			numberExposuresPerImagePV = LazyPVFactory.newIntegerPV(getPrefix()+numberExposuresPerImagePVName);
		}
		if (earlyFramesPV == null) {
			earlyFramesPV = LazyPVFactory.newIntegerPV(getPrefix()+earlyFramesPVName);
		}
		if (detectorStatePV == null) {
			detectorStatePV = LazyPVFactory.newIntegerPV(getPrefix()+detectorStatePVName);
		}
		if (dataTypePV == null) {
			dataTypePV = LazyPVFactory.newIntegerPV(getPrefix()+dataTypePVName);
		}
		super.afterPropertiesSet();
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	@Override
	public void completeCollection() throws Exception {
		super.completeCollection();
		restoreBackupValues();
	}

	private void excludeEarlyFrames() throws DeviceException {
		try {
			waitForIdle();
			getEarlyFramesPV().putWait(1);
		} catch (IOException e) {
			throw new DeviceException("Exception in excludeEarlyFrames", e);
		}
	}

	private void restoreBackupValues() throws DeviceException {
		try {
			if (getDetectorStatePV().get() == 0) {
				getDataTypePV().putWait(backupDataType);
				getEarlyFramesPV().putWait(backupEarlyFrames);
			} else {
				throw new DeviceException("restoreBackupValues Failed to set value because detector was found in a state different from Idle!");
			}
		} catch (IOException e) {
			throw new DeviceException("Failed to restore backup values", e);
		}
	}

	private void waitForIdle() throws DeviceException {
		final int totMillis = 60 * 1000;
		final int grain = 25;

		for (int i = 0; i < totMillis / grain; i++) {
			try {
				if (getDetectorStatePV().get() == 0) {
					return;
				}
				Thread.sleep(grain);
			} catch (IOException e) {
				throw new DeviceException("Exception waiting for device to be idle", e);
			} catch (InterruptedException e) {
				logger.warn("Exception waiting for idle state", e);
				Thread.currentThread().interrupt();
			}
		}
		throw new DeviceException("Timeout waiting for detector to be in the Idle state.");
	}
}
