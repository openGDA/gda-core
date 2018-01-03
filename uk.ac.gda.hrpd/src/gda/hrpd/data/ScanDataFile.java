/*-
 * Copyright © 2010 Diamond Light Source Ltd., Science and Technology
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

package gda.hrpd.data;

import gda.data.scan.datawriter.DataWriter;
import gda.device.Detector;
import gda.device.Scannable;
import gda.scan.IScanStepId;
import gda.scan.ScanPlotSettings;

import java.io.File;
import java.io.Serializable;
import java.util.List;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class holds information about the data collected at a single point on a scan. It is to be passed around between
 * objects for display \ recording of data.
 */
public class ScanDataFile implements Serializable {

	protected static final Logger logger = LoggerFactory.getLogger(ScanDataFile.class);
	/**
	 * The delimiter used in the data file.
	 */
	public String delimiter = "\t";

	/**
	 * The command typed to start the scan that this data is part of.
	 */
	protected String command = "";

	/**
	 * The string from the getCurrentFileName method of the {@link DataWriter} object owned by the scan
	 * which created this file. Could be used to uniquely identify where this data is being recorded by the
	 * {@link DataWriter} object.
	 */
	protected String currentFilename = "";

	/**
	 * The current point number.
	 */
	protected int currentPointNumber = -1;

	/**
	 * beamline name
	 */
	protected String instrument = "";
	/**
	 * The type of file format
	 */
	protected enum Formatter {
		SRS, MAC, PSD
	}
	
	protected boolean hasMetadata = false;
	
	public boolean isHasMetadata() {
		return hasMetadata;
	}

	public void setHasMetadata(boolean hasMetadata) {
		this.hasMetadata = hasMetadata;
	}

	public boolean isHasHeaders() {
		return hasHeaders;
	}

	public void setHasHeaders(boolean hasHeaders) {
		this.hasHeaders = hasHeaders;
	}

	public boolean isHasFooters() {
		return hasFooters;
	}

	public void setHasFooters(boolean hasFooters) {
		this.hasFooters = hasFooters;
	}

	public String getMetadataStartDelimiter() {
		return metadataStartDelimiter;
	}

	public void setMetadataStartDelimiter(String metadataStartDelimiter) {
		this.metadataStartDelimiter = metadataStartDelimiter;
	}

	public String getMetadataEndDelimiter() {
		return metadataEndDelimiter;
	}

	public void setMetadataEndDelimiter(String metadataEndDelimiter) {
		this.metadataEndDelimiter = metadataEndDelimiter;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public void setNumberOfFiles(int numberOfFiles) {
		this.numberOfFiles = numberOfFiles;
	}

	protected boolean hasHeaders = false;
	
	protected boolean hasFooters = false;
	
	protected String metadataStartDelimiter = "&SRS";
	
	protected String metadataEndDelimiter = "&END";

	/**
	 * The scan identifier, such as the scan number.
	 */
	protected String scanIdentifier = "";

	/**
	 * The scannables which are part of the scan.
	 */
	protected Vector<String> scannableNames = new Vector<String>();

	/**
	 * The {@link gda.device.Scannable} scannables that participate in the scan.
	 */
	protected Vector<Scannable> scannables = new Vector<Scannable>();

	/**
	 * used to plot data from file on "Scan Plot" panel.
	 */
	public ScanPlotSettings scanPlotSettings;

	private List<IScanStepId> stepIds;

	/**
	 * Unique identifier for the scan.
	 */
	protected String uniqueName = "";
	private Vector<Detector> detectors;
	private int numberOfFiles = 1;
	private File file;

	/**
	 * 
	 */
	public ScanDataFile() {
	}

	/**
	 * constructor. This constructor is used by GDA server only. 
	 * 
	 * @param scanName
	 * @param _scannables
	 * @param detectors
	 * @param currentFilename
	 * @param numberOfFile 
	 */
	public ScanDataFile(String scanName, Vector<Scannable> _scannables, Vector<Detector> detectors, 
			String currentFilename, int numberOfFile) {

		
		// Strip scannables with neither input nor extra fields as these cause problems both when plotting
		// and writing to Nexus.
		this.uniqueName = scanName;
		this.currentFilename = currentFilename;
		this.detectors = detectors;
		this.numberOfFiles = numberOfFile;
		this.scannables = _scannables;
		this.numberOfFiles = numberOfFile;
	}

	/**
	 * constructor. This constructor is used by GDA server only. 
	 * 
	 * @param scanName
	 * @param scannables
	 * @param detectors
	 * @param currentFilename
	 * @param hasChild
	 * @param stepIds
	 *            The ids of steps of each scan that created the point
	 */
	public ScanDataFile(String scanName, Vector<Scannable> scannables, Vector<Detector> detectors,
			String currentFilename, int hasChild, List<IScanStepId> stepIds) {
		this(scanName, scannables, detectors, currentFilename, hasChild);
		this.stepIds = stepIds;
	}

	/**
	 * Add a detector to the list of detectors this object holds data from.
	 * 
	 * @param det
	 */
	public void addDetector(Detector det) {
		this.detectors.add(det);
	}

	/**
	 * Add a scannable to the list of scannables this object holds data on.
	 * 
	 * @param scannable
	 */
	public void addScannable(Scannable scannable) {
		this.scannables.add(scannable);
	}

	/**
	 * @return the gda command entered
	 */
	public String getCommand() {
		return command;
	}

	/**
	 * @return String
	 */
	public String getCurrentFilename() {
		return file.getName();
	}
	/**
	 * @return String
	 */
	public String getCurrentFileURL() {
		return file.getPath();
	}

	/**
	 * @return the current point number
	 */
	public int getCurrentPointNumber() {
		return currentPointNumber;
	}

	/**
	 * Return the vector of data which this object is a carrier of.
	 * 
	 * @return Vector<Object>
	 */
	public File getCurrentDataFile() {
		return file;
	}

	/**
	 * The list of detectors this object refers to.
	 * 
	 * @return list of detectors this object refers to.
	 */
	public Vector<Detector> getDetectors() {
		return detectors;
	}

	/**
	 * @return hasChild
	 */
	public int getNumberOfFiles() {
		return numberOfFiles;
	}

	/**
	 * @return the instrument/beamline name
	 */
	public String getInstrument() {
		return instrument;
	}


	/**
	 * @return the scan identifier (eg scan number)
	 */
	public String getScanIdentifier() {
		return scanIdentifier;
	}

	/**
	 * The list of scannables this object refers to.
	 * 
	 * @return list of scannables this object refers to.
	 */
	public Vector<Scannable> getScannables() {
		return scannables;
	}

	/**
	 * @return ScanPlotSettings
	 */
	public ScanPlotSettings getScanPlotSettings() {
		return scanPlotSettings;
	}

	/**
	 * @return The ids of the scans involved in generating this point
	 */
	public List<IScanStepId> getStepIds() {
		return stepIds;
	}

	/**
	 * The unique name of the scan which created this data point.
	 * 
	 * @return String
	 */
	public String getUniqueName() {
		return uniqueName;
	}

	/**
	 * @param command
	 *            the command entered.
	 */
	public void setCommand(String command) {
		this.command = command;
	}

	/**
	 * (For gui panels).The filename that this data is going to be written to.
	 * 
	 * @param currentFilename
	 */
	public void setCurrentFilename(String currentFilename) {
		this.currentFilename = currentFilename;
	}

	/**
	 * Sets the current point number
	 * 
	 * @param currentPointNumber
	 *            the current point number
	 */
	public void setCurrentPointNumber(int currentPointNumber) {
		this.currentPointNumber = currentPointNumber;
	}

	/**
	 * @param instrument
	 *            the beamline/instrument name
	 */
	public void setInstrument(String instrument) {
		this.instrument = instrument;
	}

	/**
	 * Set the scan identifier (eg scan number)
	 * 
	 * @param scanIdentifier
	 *            the scan identifier
	 */
	public void setScanIdentifier(String scanIdentifier) {
		this.scanIdentifier = scanIdentifier;
	}

	/**
	 * The ids of the scans involved in generating this point
	 * 
	 * @param stepIds
	 */
	public void setStepIds(List<IScanStepId> stepIds) {
		this.stepIds = stepIds;
	}

	/**
	 * Sets the unique identifier of the scan which created this point.
	 * 
	 * @param uniqueName
	 */
	public void setUniqueName(String uniqueName) {
		this.uniqueName = uniqueName;
	}
}