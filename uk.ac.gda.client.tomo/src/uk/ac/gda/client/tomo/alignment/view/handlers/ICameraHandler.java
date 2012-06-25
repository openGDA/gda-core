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

package uk.ac.gda.client.tomo.alignment.view.handlers;

import gda.device.DeviceException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.graphics.Point;

import uk.ac.gda.client.tomo.TiffFileInfo;
import uk.ac.gda.client.tomo.alignment.view.controller.TomoAlignmentViewController;
import uk.ac.gda.ui.components.ZoomButtonComposite.ZOOM_LEVEL;

/**
 * This interface mediates the communication between the GUI and the detector that is doing the tomography alignment.
 * For a usage example - please follow 'uk.ac.gda.client.tomo.view.handlers.impl.TomoDetectorHandler'.
 * 
 * @author rsr31645 - Ravi Somayaji
 */
public interface ICameraHandler extends ITomoHandler{

	/**
	 * @param exposureTime
	 *            - exposure time that needs to be set on the detector.
	 * @param amplifierValue
	 * @throws Exception
	 */
	void setExposureTime(double exposureTime, int amplifierValue) throws Exception;

	/**
	 * @param acqTime
	 *            - the exposure time the acquisition needs to start with
	 * @param amplifierValue
	 *            - the exposure time set on the detector is the value of the <code>acqTime/amplifierValue</code>
	 * @throws Exception
	 */
	void startAcquiring(double acqTime, int amplifierValue) throws Exception;

	/**
	 * Stops the acquisition and any captures.
	 * 
	 * @throws DeviceException
	 * @throws Exception
	 */
	void stopAcquiring() throws DeviceException, Exception;

	/**
	 * @param monitor
	 * @param numFlat
	 *            - number of images that needs to recursed through -
	 * @param expTime
	 *            - the exposure time to be set on the detector.
	 * @throws Exception
	 */
	void takeFlat(IProgressMonitor monitor, int numFlat, double expTime) throws Exception;

	/**
	 * Sets the detector up to capture a raw image - the tiff is stored in the location provided.
	 * 
	 * @param monitor
	 * @param acqTime
	 *            - the exposure t
	 * @param isFlatFieldRequired
	 *            - flag to indicate whether flat field is required.
	 * @return filename of the tiff file captured.
	 * @throws Exception
	 */
	String demandRaw(IProgressMonitor monitor, double acqTime, boolean isFlatFieldRequired) throws Exception;

	/**
	 * Sets the detector up to capture a raw image while the continuous stream is ON - the tiff is stored in the
	 * location provided.
	 * 
	 * @param monitor
	 * @param flatCorrectionSelected
	 *            - flag to indicate whether flat field is required.
	 * @return filename of the tiff file captured.
	 * @throws Exception
	 */
	String demandRawWithStreamOn(IProgressMonitor monitor, boolean flatCorrectionSelected) throws Exception;

	/**
	 * The zoom roi location provided the points
	 * 
	 * @param roiStart
	 *            - the x, y coordinates to set the roi for the zoomed image.
	 * @throws Exception
	 */
	void setZoomRoiLocation(Point roiStart) throws Exception;

	/**
	 * sets up the detector to acquire zoomed images -
	 * 
	 * @param zoomLevel
	 *            - this will provide information on which zoom level is requested
	 * @throws Exception
	 */
	void setupZoom(ZOOM_LEVEL zoomLevel) throws Exception;

	/**
	 * @return exposure time that is already set on the detector
	 * @throws Exception
	 */
	double getAcqExposureRBV() throws Exception;

	/**
	 * @return readback value of the acquisition period already set on the detector.
	 * @throws Exception
	 */
	double getAcqPeriodRBV() throws Exception;

	/**
	 * @return the acquisition state from the detector
	 * @throws Exception
	 */
	int getAcquireState() throws Exception;

	/**
	 * @return the readback value of the array counter.
	 * @throws Exception
	 */
	int getArrayCounter_RBV() throws Exception;

	/**
	 * @return the readback value of the array rate
	 * @throws Exception
	 */
	double getArrayRate_RBV() throws Exception;

	/**
	 * @return the counter for number of exposures
	 * @throws Exception
	 */
	int getNumExposuresCounter_RBV() throws Exception;

	/**
	 * @return the RBV value of the number of images counter
	 * @throws Exception
	 */
	int getNumImagesCounter_RBV() throws Exception;

	/**
	 * @return RBV value of the time remaining
	 * @throws Exception
	 */
	double getTimeRemaining_RBV() throws Exception;

	/**
	 * @return the datatype the detector is configured with
	 * @throws Exception
	 */
	String getDatatype() throws Exception;

	/**
	 * In the PCO for I12 there are 2 Mjpegs configured. One for the full image and another for the zoomed image. This
	 * method returns the URL for the full image.
	 * 
	 * @return url string for the full image MJpeg
	 * @throws Exception
	 */
	String getFullMJpegURL() throws Exception;

	/**
	 * In the PCO for I12 there are 2 Mjpegs configured. One for the full image and another for the zoomed image. This
	 * method returns the URL for the zoomed image.
	 * 
	 * @return url string for the zoomed image MJpeg
	 * @throws Exception
	 */
	String getZoomImgMJPegURL() throws Exception;

	/**
	 * This object communicates between the GUI and the back-end.
	 * 
	 * @param tomoAlignmentViewController
	 */
	void setViewController(TomoAlignmentViewController tomoAlignmentViewController);

	/**
	 * @return the pixel size on the detector.
	 */
	Double getDetectorPixelSize();

	/**
	 * @return binX value on the roi1 plug-in of the detector
	 * @throws Exception
	 */
	Integer getRoi1BinX() throws Exception;

	/**
	 * @return the full file name of the tiff image captured.
	 * @throws Exception
	 */
	String getTiffFullFileName() throws Exception;

	/**
	 * delegate method to set the flat field method on the proc plugins to enabled.
	 * 
	 * @throws Exception
	 */
	void enableFlatCorrection() throws Exception;

	/**
	 * delegate method to set the flat field method on the proc plugins to disabled.
	 * 
	 * @throws Exception
	 */
	void disableFlatCorrection() throws Exception;

	/**
	 * @return the full file name of the flat field image captured.
	 */
	String getFlatImageFullFileName();

	/**
	 * Delegate method to ask the detector to set itself up and capture and save a dark image.
	 * 
	 * @param monitor
	 * @param acqTime
	 *            - the exposure time at which the dark image needs to be taken.
	 * @throws Exception
	 */
	void takeDark(IProgressMonitor monitor, double acqTime) throws Exception;

	/**
	 * @return true if the proc1 flat field correction is enabled.
	 * @throws Exception
	 */
	Boolean getProc1FlatFieldCorrection() throws Exception;

	/**
	 * @return the number of flat field images that will be taken and recursed through
	 */
	int getTakeFlatNumImages();

	/**
	 * @return the preferred exposure time for the sample. this is generally set in the xml configuration file or could
	 *         be set in the preferences for the GUI.
	 */
	double getPreferredSampleExposureTime();

	/**
	 * @return the preferred exposure time for the flat field. this is generally set in the xml configuration file or
	 *         could be set in the preferences for the GUI.
	 */
	double getPreferredFlatExposureTime();

	/**
	 * Sets the sample preferred exposure time - this does not set the exposure time on the detector along with caching
	 * it in the preference.
	 * 
	 * @param exposureTime
	 */
	void setPreferredSampleExposureTime(double exposureTime);

	/**
	 * Sets the preferred exposure time for sample - this does not set the exposure time on the detector along with
	 * caching it in the preference.
	 * 
	 * @param exposureTime
	 */
	void setPreferredFlatExposureTime(double exposureTime);

	/**
	 * @return the saturation threshold which is setup in the xml configuration file. this is used when the saturation
	 *         button is clicked.
	 */
	Integer getSaturationThreshold();

	/**
	 * @return the minimum intensity value on the detector. This is generally extracted from the stat plug-in of the
	 *         detector.
	 * @throws Exception
	 */
	double getStatMin() throws Exception;

	/**
	 * @return the maximum intensity value on the detector. This is generally extracted from the stat plug-in of the
	 *         detector.
	 * @throws Exception
	 */
	double getStatMax() throws Exception;

	/**
	 * @return the mean intensity value on the detector. This is generally extracted from the stat plug-in of the
	 *         detector.
	 * @throws Exception
	 */
	double getStatMean() throws Exception;

	/**
	 * @return the sigma intensity value on the detector. This is generally extracted from the stat plug-in of the
	 *         detector.
	 * @throws Exception
	 */
	double getStatSigma() throws Exception;

	/**
	 * Initialize method to initialize all listeners, and other member variables.
	 */
	void init();

	/**
	 * @return the bin X value of the roi2 plug-in of the detector.
	 * @throws Exception
	 */
	Integer getRoi2BinX() throws Exception;

	/**
	 * This sets the exposure time and the scaling factor on the proc plug-in to the value as mentioned by the factor.
	 * 
	 * @param newExpTime
	 *            - the preferred exposure time divided by the amplification factor
	 * @param factor
	 *            - the amplification factor that is set on the proc plug-in of the detector.
	 * @throws Exception
	 */
	void setAmplifiedValue(double newExpTime, int factor) throws Exception;

	/**
	 * Stop capturing and acquisition.
	 * 
	 * @throws Exception
	 */
	void stopDemandRaw() throws Exception;

	/**
	 * @return the name of the detector used.
	 */
	String getCameraName();

	/**
	 * @return the array data of from the array plug-in of the detector.
	 * @throws Exception
	 */
	short[] getArrayData() throws Exception;

	/**
	 * Resets the file format.
	 * 
	 * @throws Exception
	 */
	void resetFileFormat() throws Exception;

	/**
	 * @return the file information(filename, filepath etc) of the most recent capture
	 * @throws Exception
	 */
	TiffFileInfo getTiffFileInfo() throws Exception;

	/**
	 * Sets the file number on the tiff plug-in. the next time a capture is requested this number will be appended to
	 * the end.
	 * 
	 * @param fileNumber
	 * @throws Exception
	 */
	void setTiffFileNumber(int fileNumber) throws Exception;

	/**
	 * Calls resetall on the detector.
	 * 
	 * @throws Exception
	 */
	void reset() throws Exception;

	/**
	 * @return the configured bin X value. This is NOT the value retrieved from the detector.
	 */
	int getRoi1BinValue();

	/**
	 * Sets the detector up so that the tilt operation can collect images that are cropped which will save post
	 * processing the need for memory and other computational issues.
	 * 
	 * @param minY
	 * @param maxY
	 * @throws Exception
	 */
	void setUpForTilt(int minY, int maxY, int minX, int maxX) throws Exception;

	/**
	 * @return the full file name of the dark image captured.
	 */
	String getDarkImageFullFileName();

	/**
	 * Reset the detector after the tilt operation is complete.
	 * 
	 * @throws Exception
	 */
	void resetAfterTilt() throws Exception;

	/**
	 * Dark subtraction will be disabled on the area detector proc plugins
	 * 
	 * @throws Exception
	 */
	void disableDarkSubtraction() throws Exception;

	/**
	 * Dark subtraction will be enabled on the area detector proc plugins
	 * 
	 * @throws Exception
	 */
	void enableDarkSubtraction() throws Exception;

	/**
	 * @return the dark field file name of the dark field image captured.
	 */
	String getDarkFieldImageFullFileName();

	/**
	 * @return the full image width of the detector
	 */
	int getFullImageWidth();

	/**
	 * @return the full image height of the detector
	 */
	int getFullImageHeight();

}
