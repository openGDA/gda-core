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

package gda.device.detector;

import gda.device.Detector;

import java.awt.Point;
import java.io.File;
import java.io.IOException;

/**
 * Interface for the PCODetector
 */
public interface IPCODetector extends Detector {

	/**
	 * Sets the file path depending on the parameters either locally or remotely , and also either in the hdf format or
	 * tiff format.
	 * 
	 * @param filePath
	 * @throws Exception
	 */
	void setFilePath(File filePath) throws Exception;

	/**
	 * Collects the number of dark images as requested.
	 * 
	 * @param numberOfDarks
	 * @throws Exception
	 */
	void collectDarkSet(int numberOfDarks) throws Exception;

	/**
	 * Collect flat images in sets as specified by the parameter and also how many images per set.
	 * 
	 * @param numberOfFlats
	 * @param flatSet
	 * @throws Exception
	 */
	void collectFlatSet(int numberOfFlats, int flatSet) throws Exception;

	/**
	 * plots the image from the file onto the detector configured plot.
	 * 
	 * @param imageFileName
	 */
	void plotImage(String imageFileName);

	/**
	 * creates the main file structure and returns the root of the file system.
	 * 
	 * @return {@link File}
	 * @throws IOException
	 */
	File createMainFileStructure() throws IOException;

	/**
	 * @return <code>true</code> if the writer is busy
	 * @throws Exception
	 */
	boolean isWriterBusy() throws Exception;

	/**
	 * @return {@link IPCOControllerV17} - controller associated with the detector.F
	 */
	IPCOControllerV17 getController();

	/**
	 * @return the local file path
	 */
	String getLocalFilePath();

	/**
	 * @return the projection folder name
	 */
	String getProjectionFolderName();

	/**
	 * @return the dark file name root
	 */
	String getDarkFileNameRoot();

	/**
	 * @return the flat file name root
	 */
	String getFlatFileNameRoot();

	/**
	 * @return number of dark images collected
	 */
	int getNumberOfDarkImages();

	/**
	 * @return the number of flat images collected.
	 */
	int getNumberOfFlatImages();

	/**
	 * @return the plot name the is associated with the detector.
	 */
	String getPlotName();

	/**
	 * In addition to its own resets, this method also calls reset on all plugins
	 * 
	 * @throws Exception
	 */
	void resetAll() throws Exception;

	/**
	 * Take flat after configuring required plugins.
	 * 
	 * @param expTime
	 * @param numberOfImages
	 * @param fileLocation
	 * @param fileName
	 * @param filePathTemplate
	 * @return the file name that the flat is saved to.
	 * @throws Exception
	 */
	String takeFlat(double expTime, int numberOfImages, String fileLocation, String fileName, String filePathTemplate)
			throws Exception;

	public abstract void collectDarkSet() throws Exception;

	String demandRaw(double acqTime, String demandRawFilePath, String demandRawFileName, boolean isHdf)
			throws Exception;

	boolean isHdfFormat();

	void setHdfFormat(boolean hdfFormat);

	void acquireSynchronously() throws Exception;

	/**
	 * Take dark images
	 * 
	 * @param numberOfImages
	 *            - number of dark images to be taken.
	 * @param acqTime
	 * @throws Exception
	 */
	String takeDark(int numberOfImages, double acqTime, String fileLocation, String fileName, String filePathTemplate)
			throws Exception;

	void stopCapture() throws Exception;

	/**
	 * Stop the areadetector and the filesaver are stopped.
	 * 
	 * @throws Exception
	 */
	void abort() throws Exception;

	/**
	 * Enables the recursive filter and
	 * 
	 * @param nFilterVal
	 * @param filterType
	 * @param roi2Size
	 * @throws Exception
	 */
	void prepareTiltRotationImage(int nFilterVal, int filterType, Point roi2Size) throws Exception;

	/**
	 * Resets only the file format
	 * 
	 * @throws Exception
	 */
	void resetFileFormat() throws Exception;

	/**
	 * @return tiff image name depending on the os env of the ioc.
	 * @throws Exception
	 */
	public String getTiffImageFileName() throws Exception;

	/**Set the scaling divisor on the ROI1
	 * @param divisor
	 * @throws Exception
	 */
	void setRoi1ScalingDivisor(double divisor) throws Exception;

	/**
	 * @param minY
	 * @param maxY
	 * @throws Exception 
	 */
	void setupForTilt(int minY, int maxY) throws Exception;

}
