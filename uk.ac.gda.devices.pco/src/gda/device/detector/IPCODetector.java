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

import java.io.File;
import java.io.IOException;

import gda.device.Detector;

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

	public abstract void collectDarkSet() throws Exception;

	boolean isHdfFormat();

	void setHdfFormat(boolean hdfFormat);

	void acquireSynchronously() throws Exception;

	void stopCapture() throws Exception;

	void setTiffFilePathBasedOnIocOS(String demandRawFilePath) throws Exception;

	String getTiffImageFileName() throws Exception;

	/**
	 * @param externalTriggered
	 */
	void setExternalTriggered(boolean externalTriggered);

	/**
	 * The ADC mode is set on the detector
	 *
	 * @param mode
	 * @throws Exception
	 */
	void setADCMode(int mode) throws Exception;


}
