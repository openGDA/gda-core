/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package gda.device.detector.pixium;

import gda.device.DeviceException;

public interface IPixiumNXDetector {

	public static final String EARLY_FRAMES = "MotionBlur";
	public static final String EARLY_FRAMES_RBV = EARLY_FRAMES;
	public static final String BASE_EXPOSURE = "AcquireTime";
	public static final String BASE_EXPOSURE_RBV = "AcquireTime_RBV";
	public static final String BASE_ACQUIRE_PERIOD = "AcquirePeriod";
	public static final String BASE_ACQUIRE_PERIOD_RBV = "AcquirePeriod_RBV";
	public static final String EXPOSURES_PER_IMAGE = "NumExposures";
	public static final String EXPOSURES_PER_IMAGE_RBV = "NumExposures_RBV";
	public static final String NUM_IMAGES = "NumImages";
	public static final String NUM_IMAGES_RBV = "NumImages_RBV";
	public static final String PU_MODE = "PuMode";
	public static final String PU_MODE_RBV = "PuMode_RBV";
	public static final String DETECTOR_STATE_RBV = "DetectorState_RBV";
	public static final String ACQUIRE = "Acquire";
	public static final String CALIBRATE = "Calibrate";
	public static final String CALIBRATE_RBV = "Calibrate_RBV";
	public static final String CALIBRATION_REQUIRED_RBV = "CalibrationRequired_RBV";
	
	public abstract void includeEarlyFrames() throws Exception;

	public abstract void excludeEarlyFrames() throws Exception;

	public abstract void setBaseExposure(double expTime) throws Exception;

	public abstract double getBaseExposure() throws Exception;

	public abstract void setBaseAcquirePeriod(double acqTime) throws Exception;

	public abstract double getBaseAcquirePeriod() throws Exception;

	public abstract void setExposuresPerImage(int numExp) throws Exception;

	public abstract int getExposuresPerImage() throws Exception;

	public abstract void setNumImages(int numImg) throws Exception;

	public abstract int getNumImages() throws Exception;

	public abstract void setPUMode(int mode) throws Exception;

	public abstract int getPUMode() throws Exception;

	public abstract void calibrate() throws Exception;

	public abstract void acquire(double collectionTime, int numImages) throws Exception;

	public abstract void acquire(double collectionTime) throws Exception;

	public abstract void stop() throws DeviceException;

}