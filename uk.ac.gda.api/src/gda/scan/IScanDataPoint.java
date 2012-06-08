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

package gda.scan;

import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.Scannable;

import java.util.List;
import java.util.Vector;

/**
 * Interface for ScanDataPoint - the data for a single point in a scan.
 * <p>
 * These are created by Scan objects and distributed via the Command Server (Jython Server) to Data Writers and the UI.
 * 
 * @author rjw82
 * 
 */
public interface IScanDataPoint {

	/**
	 * An alternative for populating this object. Can be used instead of repeated calls to
	 * addScannable,addScannablePosition,addDetector,addDetectorData.
	 * <p>
	 * Note this makes calls to getPosition() in the scannables and readout() in the detectors.
	 * 
	 * @param allScannables
	 * @param allDetectors
	 * @throws DeviceException
	 */
	public void addScannablesAndDetectors(Vector<Scannable> allScannables, Vector<Detector> allDetectors)
			throws DeviceException;

	/**
	 * Gets a Scannables position and adds it to the data point. Does not ass the Scannable itself.
	 * 
	 * @param scannable
	 * @throws DeviceException
	 */
	public void addPositionFromScannable(Scannable scannable) throws DeviceException;

	/**
	 * Reads data from a detector and adds it to the point. Does not add the detector itself.
	 * 
	 * @param detector
	 * @throws DeviceException
	 */
	public void addDataFromDetector(Detector detector) throws DeviceException;

	/**
	 * Adds a Scannable with its current data(/position/value) and the format that the data should be presented in.
	 * 
	 * @param scannable
	 * @param position
	 * @param format
	 */
	public void addScannableWithPosition(Scannable scannable, Object position, String[] format);

	/**
	 * Add a piece of data to this object. Calls to this method must be made in the same order as calls to addDetector
	 * to associate the data with the detector.
	 * <p>
	 * 
	 * @param data
	 */
	public void addDetectorData(Object data, String[] format);

	/**
	 * Add a position to the array of positions. Calls to this method must be made in the same order as calls to
	 * addScannable to associate the array of numbers with the scannable.
	 * <p>
	 * It is recommended to call setScannables instead.
	 * 
	 * @param data
	 */
	public void addScannablePosition(Object data, String[] format);

	/**
	 * Add a detector to the list of detectors this object holds data from. This stores the name in the detectorHeader
	 * array and detectorNames array. If its a countertimer is stored in the boolean array. The contents of the
	 * detectorHeader and detectorNames arrays will be different if the detector is a countertimer.
	 * <p>
	 * Note this does not readout the detector! Data must be added by using the addData method.
	 * 
	 * @param det
	 */
	public void addDetector(Detector det);

	/**
	 * Add a scannable to the list of scannables this object holds data on.
	 * <p>
	 * Note that this does not read the current position of the scannable.
	 * 
	 * @param scannable
	 */
	public void addScannable(Scannable scannable);

	/**
	 * @return the scan command entered to run the scan creating these ScanDataPoints
	 */
	public String getCommand();

	/**
	 * Set the name of the panel which requested the scan which created this data point.
	 * <p>
	 * Used to selectively send ScanDataPoints to only certain UI parts on the client-side.
	 * <p>
	 * If not set then all registered Data observers will recieve the data.
	 * 
	 * @return String
	 */
	public String getCreatorPanelName();

	/**
	 * @return String - the name of the data file being written
	 */
	public String getCurrentFilename();

	/**
	 * @return the current point number in the scan
	 */
	public int getCurrentPointNumber();

	/**
	 * Return the vector of detector data which this object is a carrier of.
	 * 
	 * @return Vector<Object>
	 */
	public Vector<Object> getDetectorData();

	/**
	 * Returns the values held by this ScanDataPoint of Scannables, Monitors and Detectors.
	 * 
	 * @return an array of Double of length getMonitorHeader().size() + getPositionHeader().size() +
	 *         getDetectorHeader().size() if the conversion of a field to Double is not possible then the element of the
	 *         array will be null
	 * @throws IllegalArgumentException
	 *             if the fields convert to too few values
	 * @throws IndexOutOfBoundsException
	 *             if the fields convert to too many values
	 */
	public Double[] getAllValuesAsDoubles() throws IllegalArgumentException, IndexOutOfBoundsException;

	/**
	 * Just returns array of detector data.
	 * 
	 * @return all detector data.
	 */
	public Double[] getDetectorDataAsDoubles();

	/**
	 * returns a vector of expanded detector header string for each data point.
	 * 
	 * @return a vector of expanded detector header string for each data point.
	 */
	public Vector<String> getDetectorHeader();

	/**
	 * Return the list of names of detectors which this object holds data from.
	 * 
	 * @return Vector<String>
	 */
	public Vector<String> getDetectorNames();

	/**
	 * The list of detectors this object refers to.
	 * 
	 * @return list of detectors this object refers to.
	 */
	public Vector<Detector> getDetectors();

	/**
	 * @return hasChild - true if the current scan has a nexted child scan
	 */
	public boolean getHasChild();

	/**
	 * Returns a string whose elements are separated by a mixture of tabs and spaces so that the columns are aligned
	 * with the output from toString()
	 * 
	 * @return String - which could be used in an ascii print out of all the scan points from the same scan
	 */
	public String getHeaderString();

	/**
	 * @param dataPointFormatter
	 * @return String - the header String formatted used the given Formmatter object
	 */
	public String getHeaderString(ScanDataPointFormatter dataPointFormatter);

	/**
	 * @return String - the header with each element separated by a tab
	 */
	public String getDelimitedHeaderString();

	/**
	 * @return String - the beamline/instrument running the scan
	 */
	public String getInstrument();

	/**
	 * @return Vector<String> - Scannable names
	 */
	public Vector<String> getNames();

	/**
	 * @return int - number of nested innner (child) scans
	 */
	public int getNumberOfChildScans();

	/**
	 * @return int - number of points in this scan
	 */
	public int getNumberOfPoints();

	/**
	 * @return Vector<String> - the part of the header from the Scannables (not Detectors)
	 */
	public Vector<String> getPositionHeader();

	/**
	 * @return Vector<String> - the part of the data from the Scannables (not Detectors)
	 */
	public Vector<Object> getPositions();

	/**
	 * @return String - unique ID of the scan
	 */
	public String getScanIdentifier();

	/**
	 * @return Vector<String> - the names of the Scannables in the scan
	 */
	public Vector<String> getScannableNames();

	/**
	 * Just returns array of positions. Strings will be an empty element.
	 * 
	 * @return all scannable positions.
	 */
	public Double[] getPositionsAsDoubles();

	/**
	 * @return all Scannable positions as strings using the given format
	 */
	public String[] getPositionsAsFormattedStrings();

	public Vector<Scannable> getScannables();

	public ScanPlotSettings getScanPlotSettings();

	public void setScanPlotSettings(ScanPlotSettings scanPlotSettings);

	/**
	 * @return - int[] the dimensions of the nest of scans
	 */
	public int[] getScanDimensions();

	public void setScanDimensions(int[] scanDimensions);

	/**
	 * Returns a string whose elements are separated by a mixture of tabs and spaces so that the columns are aligned
	 * with the output from getHeaderString().
	 * <p>
	 * To be used to create an ascii version of the data held by this object for priting to terminals or to ascii files.
	 */
	public String toFormattedString();

	public String toFormattedString(ScanDataPointFormatter dataPointFormatter);

	/**
	 * Returns a string of the information held by this object delimited by the static variable.
	 * 
	 * @return String
	 */
	public String toDelimitedString();

	public List<IScanStepId> getStepIds();

	public void setStepIds(List<IScanStepId> stepIds);

	/**
	 * @return Returns the uniqueName.
	 */
	public String getUniqueName();

	public void setUniqueName(String uniqueName);

	public void setCommand(String command);

	public void setCreatorPanelName(String creatorPanelName);

	public void setCurrentFilename(String currentFilename);

	public void setCurrentPointNumber(int currentPointNumber);

	public void setHasChild(boolean hasChild);

	public void setInstrument(String instrument);

	public void setNumberOfChildScans(int numberOfChildScans);

	public void setNumberOfPoints(int numberOfPoints);

	public void setScanIdentifier(String scanIdentifier);

	public String[][] getScannableFormats();

	public void setScannableFormats(String[][] scannableFormats);

	public void setDetectorHeader(String[] detectorHeader);

	public String[] getScannableHeader();

	public void setScannableHeader(String[] scannableHeader);

	public Vector<Object> getScannablePositions();

	public void setScannablePositions(Vector<Object> scannablePositions);

	public String[][] getDetectorFormats();

	public void setDetectorFormats(String[][] detectorFormats);

	/**
	 * Searches the scannables for one of a given name. Used to avoid searches being in many places.
	 * 
	 * @param name
	 * @return Scannable if it exists or null
	 */
	public Scannable getScannable(final String name);

	/**
	 * Searches the scannables for one of a given name. Works for scannables where name is declared and the actual
	 * scannable is not sent over.
	 * 
	 * @param name
	 * @return true if it exists or false
	 */
	public boolean isScannable(final String name);

	/**
	 * Searches the detectors for one of a given name. Used to avoid searches being in many places.
	 * 
	 * @param name
	 * @return Detector if it exists or null
	 */
	public Detector getDetector(final String name);

	/**
	 * Searches the detectors for one of a given name. Works for detectors where name is declared and the actual
	 * detector is not sent over.
	 * 
	 * @param name
	 * @return true if it exists or false
	 */
	public boolean isDetector(String name);

}