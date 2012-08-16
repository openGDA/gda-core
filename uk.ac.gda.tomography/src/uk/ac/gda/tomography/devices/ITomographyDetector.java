/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package uk.ac.gda.tomography.devices;

import gda.device.DeviceException;

import java.awt.Point;
import java.awt.Rectangle;

/**
 * This class needs to be implements in order to get the Tomography alignment client working. The requirements by the
 * graphical elements are such that the detector sends or sets the EPICS elements as asked. A good place to look for an
 * implementation is "gda.device.detector.pco.PCOTomography" in the "uk.ac.gda.devices.pco" plug-in.
 * 
 * @author rsr31645 - Ravi Somayaji
 */
public interface ITomographyDetector {
	/**
	 * Exposure time on the detector to be set when this method is invoked.
	 * 
	 * @param collectionTime
	 * @throws Exception
	 */
	void setExposureTime(double collectionTime) throws Exception;

	/**
	 * The MJpeg streamer is set-up and made ready. The detector acquisition is started in "Continuous" mode. The proc
	 * scale factor is set on the proc plugin
	 * 
	 * @param acqTime
	 * @param acqPeriod
	 * @param procScaleFactor
	 * @param binX
	 * @param binY
	 * @throws Exception
	 */
	void acquireMJpeg(Double acqTime, Double acqPeriod, Double procScaleFactor, int binX, int binY) throws Exception;

	/**
	 * Set roi2 roiStart to the values provided.
	 * 
	 * @param roiStart
	 * @throws Exception
	 */
	void setZoomRoiStart(Point roiStart) throws Exception;

	/**
	 * This sets up the MJpeg streamer for the zoomed images. On the tomography alignment GUI the zoomed images are
	 * displayed on the right. The display ROI is set and the bin values are set.
	 * 
	 * @param roi
	 * @param bin
	 * @throws Exception
	 */
	void setupZoomMJpeg(Rectangle roi, Point bin) throws Exception;

	/**
	 * @return roi1BinX - the bin X value from the roi1 plugin of the detector.
	 * @throws Exception
	 */
	Integer getRoi1BinX() throws Exception;

	/**
	 * @return roi2BinX - the bin X value from the roi1 plugin of the detector.
	 * @throws Exception
	 */
	Integer getRoi2BinX() throws Exception;

	/**
	 * Sets the flat field correction on the proc plug-ins of the detector to "enabled".
	 * 
	 * @throws Exception
	 */
	void enableFlatField() throws Exception;

	/**
	 * Sets the flat field correction on the proc plug-ins of the detector to "disabled".
	 * 
	 * @throws Exception
	 */
	void disableFlatField() throws Exception;

	/**
	 * @return tillFilePath - the file location of the most recent tiff collected. The path is generally calculated
	 *         using the location and the template.
	 * @throws Exception
	 */
	String getTiffFilePath() throws Exception;

	/**
	 * @return tiff file name - the file name of the most recent tiff collected.
	 * @throws Exception
	 */
	String getTiffFileName() throws Exception;

	/**
	 * @return tiff file template - the template for writing a tiff file.
	 * @throws Exception
	 */
	String getTiffFileTemplate() throws Exception;

	/**
	 * Sets the file number on the tiff plugin - this will be used when the plugin writes a tiff file the next time.
	 * 
	 * @param fileNumber
	 * @throws Exception
	 */
	void setTiffFileNumber(int fileNumber) throws Exception;

	/**
	 * @param acqTime
	 *            - the exposure time
	 * @param demandRawFilePath
	 *            - the file path (folder) where the file needs to be saved - this is set on the tiff filepath
	 * @param demandRawFileName
	 *            - the filename that needs to be set on the tiff plugin.
	 * @param isHdf
	 *            - flag to say whether the file should be saved as a hdf or not.
	 * @param isFlatFieldCorrectionRequired
	 *            - flag to say whether flat field correction is required or not.
	 * @param demandWhileStreaming
	 *            - flag to say whether the demand raw must happen while the detector is in continuous streaming mode.
	 * @return fileName of the raw image - the complete file path of the file written by the file saver plugin of the
	 *         detector.
	 * @throws Exception
	 */
	String demandRaw(Double acqTime, String demandRawFilePath, String demandRawFileName, Boolean isHdf,
			Boolean isFlatFieldCorrectionRequired, Boolean demandWhileStreaming) throws Exception;

	/**
	 * This method is invoked when a flat image needs to be taken
	 * 
	 * @param expTime
	 *            - exposure time to be set on the detector.
	 * @param numberOfImages
	 *            - number of images to recurse through. Only the last should be saved to the file system.
	 * @param fileLocation
	 *            - Filepath of the tiff file that is saved after a flat image is captured.
	 * @param fileName
	 *            - file name of the tiff file that needs to be captured.
	 * @param filePathTemplate
	 *            - template of the tiff file that needs to be saved.
	 * @return the file name that the flat is saved to - the full file name(filePath + filename) of the tiff file that
	 *         is saved to the file system.
	 * @throws Exception
	 */
	String takeFlat(double expTime, int numberOfImages, String fileLocation, String fileName, String filePathTemplate)
			throws Exception;

	/**
	 * @return tiff image name depending on the os env of the ioc.
	 * @throws Exception
	 */
	public String getTiffImageFileName() throws Exception;

	/**
	 * This method is invoked when dark images need to be taken.
	 * 
	 * @param numberOfImages
	 *            - number of dark images to be taken.
	 * @param acqTime
	 *            - exposure time at which the dark image should be captured.
	 * @throws Exception
	 */
	String takeDark(int numberOfImages, double acqTime, String fileLocation, String fileName, String filePathTemplate)
			throws Exception;

	/**
	 * This is called when the acquisition and any file capture activities need to be stopped.
	 * 
	 * @throws Exception
	 */
	void abort() throws Exception;

	/**
	 * Sets the file saver format to hdf. The detector then delegates any file saving activity to the hdf plugin on the
	 * detector.
	 * 
	 * @param hdfFormat
	 */
	void setHdfFormat(boolean hdfFormat);

	/**
	 * Resets only the file format
	 * 
	 * @throws Exception
	 */
	void resetFileFormat() throws Exception;

	/**
	 * @return true if the file saving format is 'hdf'.
	 */
	boolean isHdfFormat();

	/**
	 * In addition to its own resets, this method also calls reset on all plugins
	 * 
	 * @throws Exception
	 */
	void resetAll() throws Exception;

	/**
	 * The tilt alignment is generally done on a known sample - the data that needs to be collected can be cropped on
	 * the y axis so that data processing can be relieved of memory issues.
	 * 
	 * @param minY
	 *            - the minimum y value
	 * @param maxY
	 *            - the maximum y value
	 * @param minX
	 * @param maxX
	 * @throws Exception
	 */
	void setupForTilt(int minY, int maxY, int minX, int maxX) throws Exception;

	/**
	 * Reset the values of minY and maxY the initial values
	 * 
	 * @throws Exception
	 */
	void resetAfterTiltToInitialValues() throws Exception;

	/**
	 * Sets the proc scale factor to the given value
	 * 
	 * @param factor
	 *            - value to which the proc scale should be set
	 * @throws Exception
	 */
	void setProcScale(double factor) throws Exception;

	/**
	 * Set the scaling divisor on the ROI1
	 * 
	 * @param divisor
	 * @throws Exception
	 */
	void setRoi1ScalingDivisor(double divisor) throws Exception;

	/**
	 * @return name - name of the detector.
	 */
	String getDetectorName();

	/**
	 * @return true if the detector is busy; false otherwise.
	 * @throws DeviceException
	 */
	boolean isAcquiring() throws DeviceException;

	/**
	 * The PCO detector can be controlled using an external hardware trigger - however, for the tomography alignment it
	 * is important that this is set to <code>false</code>
	 * 
	 * @param val
	 */
	void setExternalTriggered(Boolean val);

	/**
	 * Invoked to initialise the detector properties - in case of the PCO detector, an image needs to be taken so that
	 * the array values are passed to all the plugins.
	 * 
	 * @throws Exception
	 */
	void initDetector() throws Exception;

	/**
	 * Request to disable the dark subtraction flag on the area detector.
	 * 
	 * @throws Exception
	 */
	void disableDarkSubtraction() throws Exception;

	/**
	 * Request to enable the dark subtraction flag on the area detector.
	 * 
	 * @throws Exception
	 */
	void enableDarkSubtraction() throws Exception;

	/**
	 * @return scale value from the proc1 plugin.
	 * @throws Exception
	 */
	double getProc1Scale() throws Exception;

	/**
	 * Update the scale value on the proc1 plugin.
	 * 
	 * @param newScale
	 * @throws Exception
	 */
	void setProc1Scale(double newScale) throws Exception;

	/**
	 * Set offset and scale on the proc plugins of the detector.
	 * 
	 * @param offset
	 * @param scale
	 * @throws Exception 
	 */
	void setOffsetAndScale(double offset, double scale) throws Exception;
}
