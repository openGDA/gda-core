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

package gda.device.detector.areadetector.v17;

import gda.device.Detector;
import gda.device.DeviceException;

/**
 * Pilatus Epics AreaDetector Driver. Comments are based on http://cars9.uchicago.edu/software/epics/pilatusDoc.html.
 * The standard Pilatus {@link Detector} that uses this driver is {@link gda.device.detector.addetector.HardwareTriggerableADDetector}
 */
public interface ADDriverPilatus {

	public final static String SoftTrigger = "SoftTrigger";

	enum PilatusTriggerMode {
		/**
		 * External signal not used
		 */
		INTERNAL,
		/**
		 * Count while external trigger line is high, readout on high to low transition
		 */
		EXTERAL_ENABLE,
		/**
		 * Bbegin acquisition sequence on high to low transition of external trigger line
		 */
		EXTERNAL_TRIGGER,
		/**
		 * High to low transition on external signal triggers a single acquisition for the programmed exposure time
		 */
		MULTIPLE_EXTERNAL_TRIGGER,
		/**
		 * Collect images as fast as exposure time and readout permit, images written to a temporary file
		 */
		ALIGNMENT,
		/**
		 * Use separate PV to trigger acquisitions with callback when detector is ready to acquire next image.
		 */
		SOFTWARE_TRIGGER
	}

	enum Gain {
		/**
		 * ("Fast/Low") Fastest shaping time (~125ns) and lowest gain.
		 */
		LOW,
		/**
		 * ("Medium/Medium") Medium shaping time (~200 ns) and medium gain.
		 */
		MEDIUM,
		/**
		 * ("Slow/High") Slow shaping time (~400 ns) and high gain.
		 */
		HIGH,
		/**
		 * ("Slow/Ultrahigh") Slowest peaking time (? ns) and highest gain.
		 */
		ULTRAHIGH
	}

	/**
	 * Flag to indicate when the Pilatus is ready to accept external trigger signals (0=not ready, 1=ready). This should
	 * be used by clients to indicate when it is OK to start sending trigger pulses to the Pilatus. If pulses are send
	 * before Armed=1 then the Pilatus may miss them, leading to DMA timeout errors from camserver
	 *
	 * @return true if armed
	 */
	boolean isArmed() throws DeviceException;

	void waitForArmed(double timeoutS) throws DeviceException, java.util.concurrent.TimeoutException;
	/**
	 * Delay in seconds between the external trigger and the start of image acquisition. It only applies in External
	 * Trigger mode
	 *
	 * @param delayTimeSeconds
	 */
	void setDelayTime(float delayTimeSeconds) throws DeviceException;

	float getDelayTime_RBV() throws DeviceException;

	/**
	 * Set Threshold energy in keV
	 *
	 * @param thresholdEnergy
	 *            in keV
	 */
	void setThresholdEnergy(float thresholdEnergy) throws DeviceException;

	float getThresholdEnergy_RBV() throws DeviceException;

	/**
	 * Gain menu. Controls the value of Vrf, which determines the shaping time and gain of the input amplifiers.
	 *
	 * @param gain
	 */
	void setGain(Gain gain) throws DeviceException;

	Gain getGain() throws DeviceException;

	/**
	 * Timeout in seconds when reading a TIFF or CBF file. It should be set to several seconds, because there can be
	 * delays for various reasons. One reason is that there is sometimes a delay between when an External Enable
	 * acquisition is started and when the first external pulse occurs. Another is that it can take some time for
	 * camserver processes to finish writing the files.
	 *
	 * @param timeoutSeconds
	 */
	void setImageFileTmot(float timeoutSeconds) throws DeviceException;

	/**
	 * Name of a file to be used to replace bad pixels. If this record does not point to a valid bad pixel file then no
	 * bad pixel mapping is performed. The bad pixel map is used before making the NDArray callbacks. It does not modify
	 * the data in the files that camserver writes. This is a simple ASCII file with the following format: <code>
	 * badX1,badY1 replacementX1,replacementY1
	 * badX2,badY2 replacementX2,replacementY2
	 * ...
	 * </code> The X and Y coordinates range from 0 to NXPixels-1 and NYPixels-1. Up to 100 bad pixels can be defined.
	 * The bad pixel mapping simply replaces the bad pixels with another pixel's value. It does not do any averaging. It
	 * is felt that this is sufficient for the purpose for which this driver was written, namely fast on-line viewing of
	 * ROIs and image data. More sophisticated algorithms can be used for offline analysis of the image files
	 * themselves. The following is an example bad pixel file for a GSECARS detector: <code>
	 * 263,3   262,3
	 * 264,3   266,3
	 * 263,3   266,3
	 * 300,85  299,85
	 * 300,86  299,86
	 * 471,129 472,129
	 * </code>
	 *
	 * @param filename
	 */
	void setBadPixelFile(String filename) throws DeviceException;

	/**
	 * The number of bad pixels defined in the bad pixel file. Useful for seeing if the bad pixel file was read
	 * correctly.
	 *
	 * @return number of bad pixels
	 */
	int getNumBadPixels() throws DeviceException;

	/**
	 * Name of a file to be used to correct for the flat field. If this record does not point to a valid flat field file
	 * then no flat field correction is performed. The flat field file is simply a TIFF or CBF file collected by the
	 * Pilatus that is used to correct for spatial non-uniformity in the response of the detector. It should be
	 * collected with a spatially uniform intensity on the detector at roughly the same energy as the measurements being
	 * corrected. When the flat field file is read, the average pixel value (averageFlatField) is computed using all
	 * pixels with intensities >PilatusMinFlatField. All pixels with intensity <PilatusMinFlatField in the flat field
	 * are replaced with averageFlatField. When images are collected before the NDArray callbacks are performed the
	 * following per-pixel correction is applied: <code>
	 * ImageData[i] =
	 *    (averageFlatField *
	 *    ImageData[i])/flatField[i];
	 * </code>
	 *
	 * @param filename
	 */
	void setFlatFieldFile(String filename) throws DeviceException;

	/**
	 * The minimum valid intensity in the flat field. This value must be set > 0 to prevent divide by 0 errors. If the
	 * flat field was collected with some pixels having very low intensity then this value can be used to replace those
	 * pixels with the average response.
	 *
	 * @param minIntensity
	 */
	void setMinFlatField(int minIntensity) throws DeviceException;

	int getMinFlatField_RBV() throws DeviceException;

	/**
	 * This record indicates if a valid flat field file has been read. 0=No, 1=Yes.
	 *
	 * @return true if flat field file was read.
	 */
	boolean getFlatFieldValid() throws DeviceException;

	void reset() throws DeviceException;

	void sendSoftTrigger() throws Exception;

	void waitForSoftTriggerCallback() throws InterruptedException;
}
