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

import java.util.List;

import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.Scannable;
import uk.ac.gda.api.scan.IScanObject;

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
	 * @deprecated this method is not used. It calls {@link Scannable#getPosition()} on the
	 * 		scannables and {@link Detector#readout()} on the detectors, which arguably is not
	 * 		the job of a scan data point. Instead call {@link #addScannable(Scannable)}
	 * 		{@link #addScannablePosition(Object, String[])}, {@link #addDetector(Detector)}
	 * 		and {@link #addDetectorData(Object, String[])} (other methods are available.
	 *
	 * @param allScannables
	 * @param allDetectors
	 * @throws DeviceException
	 */
	@Deprecated(since = "9.33", forRemoval = true)
	void addScannablesAndDetectors(List<Scannable> allScannables, List<Detector> allDetectors)
			throws DeviceException;

	/**
	 * Gets a Scannables position and adds it to the data point. Does not add the Scannable itself.
	 *
	 * @deprecated use {@link #addScannablePosition(Object, String[])} instead
	 *
	 * @param scannable
	 * @throws DeviceException
	 */
	@Deprecated(since = "9.33", forRemoval = true)
	void addPositionFromScannable(Scannable scannable) throws DeviceException;

	/**
	 * Reads data from a detector and adds it to the point. Does not add the detector itself.
	 *
	 * @deprecated use {@link #addDetectorData(Object, String[])} instead
	 *
	 * @param detector
	 * @throws DeviceException
	 */
	@Deprecated(since = "9.33", forRemoval = true)
	void addDataFromDetector(Detector detector) throws DeviceException;

	/**
	 * Adds a Scannable with its current data(/position/value) and the format that the data should be presented in.
	 *
	 * @param scannable
	 * @param position
	 * @param format
	 */
	void addScannableWithPosition(Scannable scannable, Object position, String[] format);

	/**
	 * Add a piece of data to this object. Calls to this method must be made in the same order as calls to addDetector
	 * to associate the data with the detector.
	 * <p>
	 *
	 * @param data
	 */
	void addDetectorData(Object data, String[] format);

	/**
	 * Add a position to the array of positions. Calls to this method must be made in the same order as calls to
	 * addScannable to associate the array of numbers with the scannable.
	 * <p>
	 * It is recommended to call setScannables instead.
	 *
	 * @param data
	 */
	void addScannablePosition(Object data, String[] format);

	/**
	 * Add a detector to the list of detectors this object holds data from. This stores the name in the detectorHeader
	 * array and detectorNames array. If it's a countertimer then it is stored in the boolean array. The contents of the
	 * detectorHeader and detectorNames arrays will be different if the detector is a countertimer.
	 * <p>
	 * Note this does not readout the detector! Data must be added by using the addData method.
	 *
	 * @param det
	 */
	void addDetector(Detector det);

	/**
	 * Add a scannable to the list of scannables this object holds data on.
	 * <p>
	 * Note that this does not read the current position of the scannable.
	 *
	 * @param scannable
	 */
	void addScannable(Scannable scannable);

	/**
	 * @return the scan command entered to run the scan creating these ScanDataPoints
	 */
	String getCommand();

	/**
	 * @return the name of the data file being written
	 */
	String getCurrentFilename();

	/**
	 * @return the current point number in the scan
	 */
	int getCurrentPointNumber();

	/**
	 * Return the vector of detector data which this object is a carrier of.
	 *
	 * @return detector data
	 */
	List<Object> getDetectorData();

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
	Double[] getAllValuesAsDoubles() throws IllegalArgumentException, IndexOutOfBoundsException;

	/**
	 * Just returns array of detector data.
	 *
	 * @return all detector data.
	 */
	Double[] getDetectorDataAsDoubles();

	/**
	 * returns a list of expanded detector header string for each data point.
	 *
	 * @return a list of expanded detector header string for each data point.
	 */
	List<String> getDetectorHeader();

	/**
	 * Return the list of names of detectors which this object holds data from.
	 *
	 * @return list of detector names
	 */
	List<String> getDetectorNames();

	/**
	 * The list of detectors this object refers to.
	 *
	 * @return list of detectors this object refers to.
	 */
	List<Detector> getDetectors();

	/**
	 * @return <code>true</code> if the current scan has a nested child scan, <code>false</code> otherwise
	 */
	boolean getHasChild();

	/**
	 * Returns a string whose elements are separated by a mixture of tabs and spaces so that the columns are aligned
	 * with the output from {@link #toString()}
	 *
	 * @return header string, which could be used in an ascii print out of all the scan points from the same scan
	 */
	String getHeaderString();

	/**
	 * @param dataPointFormatter
	 * @return String - the header String formatted used the given Formatter object
	 */
	String getHeaderString(ScanDataPointFormatter dataPointFormatter);

	/**
	 * @return the header with each element separated by a tab
	 */
	String getDelimitedHeaderString();

	/**
	 * @return the beamline/instrument running the scan
	 */
	String getInstrument();

	/**
	 * @return list of scannable names
	 */
	List<String> getNames();

	/**
	 * @return number of nested inner (child) scans
	 */
	int getNumberOfChildScans();

	/**
	 * @return number of points in this scan
	 */
	int getNumberOfPoints();

	/**
	 * @return the part of the header from the Scannables (not Detectors) as a list of strings
	 */
	List<String> getPositionHeader();

	/**
	 * @return the part of the data from the Scannables (not Detectors) as a list of objects
	 * @apiNote This method is a synonym of {@link #getScannablePositions()}.
	 */
	List<Object> getPositions();

	/**
	 * @return unique ID of the scan
	 */
	int getScanIdentifier();

	/**
	 * @return list of the names of the Scannables in the scan
	 */
	List<String> getScannableNames();

	/**
	 * Just returns array of positions. Strings will be an empty element.
	 *
	 * @return all scannable positions.
	 */
	Double[] getPositionsAsDoubles();

	/**
	 * @return all Scannable positions as strings using the given format
	 */
	String[] getPositionsAsFormattedStrings();

	List<Scannable> getScannables();

	ScanPlotSettings getScanPlotSettings();

	void setScanPlotSettings(ScanPlotSettings scanPlotSettings);

	/**
	 * @return the dimensions of the nest of scans
	 */
	int[] getScanDimensions();

	void setScanDimensions(int[] scanDimensions);

	/**
	 * Returns a string whose elements are separated by a mixture of tabs and spaces so that the columns are aligned
	 * with the output from getHeaderString().
	 * <p>
	 * To be used to create an ascii version of the data held by this object for printing to terminals or to ascii files.
	 */
	String toFormattedString();

	String toFormattedString(ScanDataPointFormatter dataPointFormatter);

	/**
	 * Returns a string of the information held by this object delimited by the static variable.
	 *
	 * @return this point as a delimited string
	 */
	String toDelimitedString();

	List<IScanStepId> getStepIds();

	void setStepIds(List<IScanStepId> stepIds);

	/**
	 * @return the unique identifier for the scan.
	 */
	String getUniqueName();

	void setUniqueName(String uniqueName);

	void setCommand(String command);

	void setCurrentFilename(String currentFilename);

	void setCurrentPointNumber(int currentPointNumber);

	void setHasChild(boolean hasChild);

	void setInstrument(String instrument);

	void setNumberOfChildScans(int numberOfChildScans);

	void setNumberOfPoints(int numberOfPoints);

	void setScanIdentifier(int scanIdentifier);

	String[][] getScannableFormats();

	void setScannableFormats(String[][] scannableFormats);

	void setDetectorHeader(String[] detectorHeader);

	String[] getScannableHeader();

	void setScannableHeader(String[] scannableHeader);

	List<Object> getScannablePositions();

	void setScannablePositions(List<Object> scannablePositions);

	void setDetectorData(List<Object> detectorData);

	List<IScanObject> getScanObjects();

	void setScanObjects(List<IScanObject> scanObjects);

	String[][] getDetectorFormats();

	void setDetectorFormats(String[][] detectorFormats);

	/**
	 * Searches the scannables for one of a given name. Used to avoid searches being in many places.
	 *
	 * @param name
	 * @return the scannable with the given name in this point if it exists, otherwise <code>null</code>
	 */
	Scannable getScannable(final String name);

	/**
	 * Searches the scannables for one of a given name. Works for scannables where name is declared and the actual
	 * scannable is not sent over.
	 *
	 * @param name
	 * @return <code>true</code> if this point contains a scannable with this name, <code>false</code> otherwise
	 */
	boolean isScannable(final String name);

	/**
	 * Searches the detectors for one of a given name. Used to avoid searches being in many places.
	 *
	 * @param name
	 * @return the detector with the given name in this point if it exists, otherwise <code>null</code>
	 */
	Detector getDetector(final String name);

	/**
	 * Searches the detectors for one of a given name. Works for detectors where name is declared and the actual
	 * detector is not sent over.
	 *
	 * @param name
	 * @return <code>true</code> if this point contains a detector with the given name, <code>false</code> otherwise
	 */
	boolean isDetector(String name);

	ScanInformation getScanInformation();
}