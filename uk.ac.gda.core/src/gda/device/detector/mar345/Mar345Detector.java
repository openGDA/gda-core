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

package gda.device.detector.mar345;

import gda.data.PathConstructor;
import gda.device.DeviceException;
import gda.device.Mar345;
import gda.device.Scannable;
import gda.device.detector.DetectorBase;
import gda.factory.Configurable;
import gda.factory.FactoryException;
import gda.factory.Findable;
import gda.jython.JythonServerFacade;

import java.io.File;
import java.io.Serializable;

/**
 * MAR345 Detector Class.
 */
public class Mar345Detector extends DetectorBase implements Configurable, Serializable, Findable, Mar345, Scannable {
	/** The mar controller object */
	private Mar345Controller m345 = null;

	/** Default file format */
	private String defaultMarFormat = "MAR345";

	/** Default collection mode */
	private int defaultMarMode = 4;

	/** Directory to store mar files to */
	private String defaultMarDirectory = null;//PathConstructor.createFromDefaultProperty();

	/** Root name to be used on output files */
	private String defaultRootName = "mar";

	/** A keyword list which van be sent to the MAR controller for processing */
	private StringBuffer keywordList = new StringBuffer();

	/**
	 * Initialize the detector
	 */
	public Mar345Detector() {
	}
	/**
	 * @see gda.device.DeviceBase#configure()
	 */
	@Override
	public void configure() throws FactoryException {
		m345 = new Mar345Controller();//taken from constructor because of problems detecting when the mar345 is idle.
		defaultMarDirectory = PathConstructor.createFromDefaultProperty();
	}
	/**
	 * Initialize the detector passing mar log directory
	 * 
	 * @param logDir
	 *            mar log directory
	 */
	public Mar345Detector(@SuppressWarnings("unused") String logDir) {
		//m345 = new Mar345Controller(logDir);
	}

	@Override
	public void collectData() throws DeviceException {
		// The mar collects automatically
	}

	@Override
	public int getStatus() throws DeviceException {
		return m345.getDetectorStatus();
	}

	@Override
	public Object readout() throws DeviceException {
		m345.sendKeywords("COMMAND SCAN");
		return null;
	}

	/**
	 * Append keyowrds to the current keyword buffer
	 * 
	 * @param keywords
	 */
	@Override
	public void appendToKeywordList(String keywords) {

		keywordList.append(keywords + " \n");

	}

	/**
	 * Clear current keyword buffer
	 */
	@Override
	public void clearKeywordList() {

		keywordList = new StringBuffer();

	}

	/**
	 * Send the keyword list to the mar controller See mar documentation
	 */
	@Override
	public void sendKeywordList() {

		m345.sendKeywords(keywordList.toString());
		clearKeywordList();
	}

	/**
	 * Send a string to the mar controller
	 * 
	 * @param keywords
	 */
	@Override
	public void sendKeywords(String keywords) {
		m345.sendKeywords(keywords);
	}

	/**
	 * Set the format for the mar files
	 * 
	 * @param format
	 */
	@Override
	public void setFormat(String format) {
		String proposedFormat = format.toUpperCase().trim();
		if (proposedFormat == "MAR345" || proposedFormat == "IMAGE" || proposedFormat == "SPIRAL"
				|| proposedFormat == "CBF" || proposedFormat == "CIF") {
			this.defaultMarFormat = format;
		} else {
			JythonServerFacade.getInstance().print("Unknown MAR format : Selecting MAR345");
		}
	}

	/**
	 * @return Get the format for the mar files
	 */
	@Override
	public String getFormat() {
		return this.defaultMarFormat;
	}

	/**
	 * Set the scan mode Values from 0 - 7 see the mar documentation
	 * 
	 * @param mode
	 */
	@Override
	public void setMode(int mode) {
		if (mode < 0 || mode > 7) {
			StringBuffer output = new StringBuffer();
			output.append("Unknown MAR mode\n");
			output.append("Please choose from :\n");
			output.append("Mode 0 = 2300 pixels, 345 mm diameters, 0.15 pixelsize \n");
			output.append("Mode 1 = 2000 pixels, 300 mm diameters, 0.15 pixelsize \n");
			output.append("Mode 2 = 1600 pixels, 240 mm diameters, 0.15 pixelsize \n");
			output.append("Mode 3 = 1200 pixels, 180 mm diameters, 0.15 pixelsize \n");
			output.append("Mode 4 = 3450 pixels, 345 mm diameters, 0.10 pixelsize \n");
			output.append("Mode 5 = 3000 pixels, 300 mm diameters, 0.10 pixelsize \n");
			output.append("Mode 6 = 2400 pixels, 240 mm diameters, 0.10 pixelsize \n");
			output.append("Mode 7 = 1800 pixels, 180 mm diameters, 0.10 pixelsize \n");
			JythonServerFacade.getInstance().print(output.toString());
		} else {
			defaultMarMode = mode;
		}
	}

	/**
	 * @return The mar mode
	 */
	@Override
	public int getMode() {
		return this.defaultMarMode;
	}

	/**
	 * Set the directory into which the mar files will be stored
	 * 
	 * @param directory
	 */
	@Override
	public void setDirectory(String directory) {
		if (directory == null) {
			StringBuffer output = new StringBuffer();
			output.append("Null directory : Using default\n");
			JythonServerFacade.getInstance().print(output.toString());
		} else {
			boolean exists = (new File(directory)).exists();
			if (exists) {
				defaultMarDirectory = directory;
			} else {
				StringBuffer output = new StringBuffer();
				output.append("Specified directory does not exist\n");
				JythonServerFacade.getInstance().print(output.toString());
			}
		}
	}

	/**
	 * @return The data storage directory name
	 */
	@Override
	public String getDirectory() {
		return this.defaultMarDirectory;
	}

	/**
	 * Set the root name of the mar files
	 * 
	 * @param rootName
	 */
	@Override
	public void setRootName(String rootName) {
		if (rootName == null) {
			StringBuffer output = new StringBuffer();
			output.append("Null rootname : Using default" + this.defaultRootName + "\n");
			JythonServerFacade.getInstance().print(output.toString());
		} else {
			this.defaultRootName = rootName;
		}
	}

	/**
	 * @return The root name for the mar files
	 */
	@Override
	public String getRootName() {
		return this.defaultRootName;
	}

	/**
	 * ERASE THE MAR 345
	 */
	@Override
	public void erase() {
		m345.sendKeywords("COMMAND ERASE");
	}

	/**
	 * SCAN THE MAR 345
	 */
	@Override
	public void scan() {
		appendToKeywordList("DIRECTORY " + this.defaultMarDirectory);
		appendToKeywordList("ROOT " + this.defaultRootName);
		appendToKeywordList("MODE " + this.defaultMarMode);
		appendToKeywordList("FORMAT " + this.defaultMarFormat);
		appendToKeywordList("COMMAND SCAN");
		sendKeywordList();
	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		// readout() doesn't return a filename.
		return false;
	}

	@Override
	public String getDescription() throws DeviceException {
		return "MAR345";
	}

	@Override
	public String getDetectorID() throws DeviceException {
		return "unknown";
	}

	@Override
	public String getDetectorType() throws DeviceException {
		return "ImagePlate";
	}
}