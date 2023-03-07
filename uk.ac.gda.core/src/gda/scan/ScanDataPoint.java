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

package gda.scan;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.data.DetectorDataWrapper;
import gda.data.PlottableDetectorData;
import gda.data.PlottableDetectorDataClone;
import gda.data.nexus.tree.NexusTreeProvider;
import gda.data.scan.datawriter.DataWriterBase;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.scannable.ScannableUtils;
import gda.scan.ScanInformation.ScanInformationBuilder;
import uk.ac.gda.api.scan.IScanObject;
import uk.ac.gda.util.map.MapUtils;

/**
 * This class holds information about the data collected at a single point on a scan. It is to be passed around between
 * objects for display \ recording of data.
 */
public class ScanDataPoint implements Serializable, IScanDataPoint {

	private static final Logger logger = LoggerFactory.getLogger(ScanDataPoint.class);

	private ScanInformation scanInfo = ScanInformation.EMPTY;

	/**
	 * The delimiter used by toString() method.
	 */
	public static final String DELIMITER = "\t";

	/**
	 * The command typed to start the scan that this point is part of.
	 */
	private String command = "";

	/**
	 * The current point number.
	 */
	private int currentPointNumber = -1;

	/**
	 * List<Object> of data from the detectors. Each element represents the data from the detector in the
	 * corresponding element of the 'detectors' List;
	 */
	private List<Object> detectorData = new ArrayList<>();

	/**
	 * The expanded header names for detectors in which the names are composed as detector name plus its element name.
	 */
	private String[] detectorHeader = new String[0];

	/**
	 * The {@link gda.device.Detector} detectors that participate in the scan.
	 * Note, this will be null once the point has been deserialized, i.e. on the client
	 */
	private final transient List<Detector> detectors = new ArrayList<>();

	/**
	 * Formatting information for the scannable positions - used in the toString method. If an element is "" or null
	 * then do not format that in the output.
	 */
	private String[][] detectorFormats = new String[0][];

	/**
	 * The expanded header names for scannable position in which the names are composed of the scannable name plus its
	 * element name, i.e. scannable's InputNames and ExtraNames
	 */
	private String[] scannableHeader = new String[0];

	/**
	 * The current positions of the scannables. Each element represents the scannable in the corresponding element of
	 * the 'scannables' List;
	 */
	private List<Object> scannablePositions = new ArrayList<>();

	/**
	 * the list of movements that this scan will perform in the context of the a multi-dimensional set of nested scans.
	 * Note, this will be null once the point has been deserialized, i.e. on the client
	 */
	private transient List<IScanObject> scanObjects = new ArrayList<>();

	/**
	 * Formatting information for the scannable positions - used in the toString method.
	 */
	private String[][] scannableFormats = new String[0][];

	/**
	 * The {@link gda.device.Scannable} scannables that participate in the scan.
	 * Note, this will be null once the point has been deserialized, i.e. on the client
	 */
	private transient List<Scannable> scannables = new ArrayList<>();

	/**
	 * Unique identifier for the scan.
	 */
	private String uniqueName = "";

	// values useful for plotting tools
	private List<IScanStepId> stepIds = new ArrayList<>();
	private int numberOfChildScans = 0;
	private ScanPlotSettings scanPlotSettings;

	/**
	 * Is this data point contain child scan
	 */
	private boolean hasChild = false;

	/**
	 *
	 */
	public ScanDataPoint() {
	}

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
	@Override
	public void addScannablesAndDetectors(List<Scannable> allScannables, List<Detector> allDetectors)
			throws DeviceException {

		for (Scannable scannable : allScannables) {
			if (scannable.getOutputFormat().length == 0) {
				handleZeroInputExtraNameDevice(scannable);
			} else {
				this.addScannable(scannable);
				addPositionFromScannable(scannable);
			}
		}
		for (Detector scannable : allDetectors) {
			this.addDetector(scannable);
			addDataFromDetector(scannable);
		}
	}

	@Override
	public void addPositionFromScannable(Scannable scannable) throws DeviceException {
		this.addScannablePosition(scannable.getPosition(), scannable.getOutputFormat());
	}

	@Override
	public void addDataFromDetector(Detector detector) throws DeviceException {
		Object data = detector.readout();
		this.addDetectorData(data, ScannableUtils.getExtraNamesFormats(detector));
	}

	/**
	 * Call getPosition on the zero-input-extra-names scannable. The zie should return null. This hook is used by some
	 * Scannables to perform a task in a scan but return nothing.
	 *
	 * @param zie
	 * @throws DeviceException
	 */
	static void handleZeroInputExtraNameDevice(Scannable zie) throws DeviceException {
		Object zeroInputExtraNameDevicePosition = zie.getPosition();
		if (zeroInputExtraNameDevicePosition != null) {
			if (zeroInputExtraNameDevicePosition instanceof Object[]) {
				Object[] position = (Object[]) zeroInputExtraNameDevicePosition;
				if (position.length == 0) {
					return;
				}
			}
			final String msg = String.format(
					"Scannable %s has no input or extra names defined. Its getPosition method should return null/None but returned: '%s'.", zie.getName(),
					zeroInputExtraNameDevicePosition.toString());
			throw new DeviceException(msg);
		}
	}

	@Override
	public void addScannableWithPosition(Scannable scannable, Object position, String[] format) {
		this.addScannable(scannable);
		this.addScannablePosition(position, format);
	}

	@Override
	public void addDetectorData(Object data, String[] format) {
		if (data != null){
			this.detectorData.add(data);
			if (format == null || format.length == 0) {
				format = new String[] { "%s" };
			}
			detectorFormats = (String[][]) ArrayUtils.add(detectorFormats, format);
		}
	}



	/**
	 * Replaces the detector data held by the object. The replacement List must be the same length as the previous for
	 * this sdp to be self-consistent.
	 *
	 * @param newdata
	 */
	protected void setDetectorData(List<Object> newdata, String[][] format) {
		this.detectorData = newdata;
		detectorFormats = format;
	}

	@Override
	public void addScannablePosition(Object data, String[] format) {
		if (data != null) {
			scannablePositions.add(data);
			scannableFormats = (String[][]) ArrayUtils.add(scannableFormats, format);
		}
	}

	/**
	 * Replaces the scannable positions data held by the object. The replacement array must be the same length as the
	 * previous for this sdp to be self-consistent.
	 *
	 * @param positions
	 * @param formats
	 */
	protected void setScannablePositions(List<Object> positions, String[][] formats) {
		this.scannablePositions = positions;
		this.scannableFormats = formats;
	}

	@Override
	public void addDetector(Detector det) {
		ScanInformationBuilder newInfo = ScanInformationBuilder.from(scanInfo);
		newInfo.detectorNames((String[]) ArrayUtils.add(scanInfo.getDetectorNames(), det.getName()));
		scanInfo = newInfo.build();
		String[] extraNames = det.getExtraNames();
		if (extraNames != null && extraNames.length > 0) {
			detectorHeader = (String[]) ArrayUtils.addAll(detectorHeader, extraNames);
		} else {
			detectorHeader = (String[]) ArrayUtils.add(detectorHeader, det.getName());
		}
		detectors.add(det);
	}

	@Override
	public void addScannable(Scannable scannable) {
		scanInfo = ScanInformationBuilder.from(scanInfo)
				.scannableNames((String[]) ArrayUtils.add(scanInfo.getScannableNames(), scannable.getName()))
				.build();
		scannableHeader = (String[]) ArrayUtils.addAll(scannableHeader, scannable.getInputNames());
		scannableHeader = (String[]) ArrayUtils.addAll(scannableHeader, scannable.getExtraNames());
		scannables.add(scannable);
	}

	@Override
	public String getCommand() {
		return command;
	}

	@Override
	public String getCurrentFilename() {
		return scanInfo.getFilename();
	}

	@Override
	public int getCurrentPointNumber() {
		return currentPointNumber;
	}

	@Override
	public List<Object> getDetectorData() {
		return detectorData;
	}

	private Double[] allValuesAsDoubles=null;

	@Override
	public Double[] getAllValuesAsDoubles() throws IllegalArgumentException, IndexOutOfBoundsException {
		if (allValuesAsDoubles == null) {
			allValuesAsDoubles = Stream.concat(Arrays.stream(getPositionsAsDoubles()), Arrays.stream(getDetectorDataAsDoubles()))
					.toArray(Double[]::new);
		}
		return allValuesAsDoubles;
	}

	@Override
	public Double[] getDetectorDataAsDoubles() {
		final List<Object> detectorData = getDetectorData();
		if (detectorData == null) return new Double[0];

		final List<Detector> detectors = getDetectors();
		final List<Double> values = new ArrayList<>();
		for (int i = 0; i < detectorData.size(); i++) {
			final Object data = detectorData.get(i);
			final PlottableDetectorData wrapper = (data instanceof PlottableDetectorData) ? (PlottableDetectorData) data
					: new DetectorDataWrapper(data);
			Double[] dvals = wrapper.getDoubleVals();

			// in the case that the detector has no extract names (so would be written by NexusDataWriter.writeGenericDetector) but an array value
			// we only have one header entry for the detector, so we return a null, indicating to not write this field
			if (dvals.length > 1 && detectors != null && detectors.size() > i &&  detectors.get(i).getExtraNames().length == 0) {
				dvals = new Double[] { null };
			}
			values.addAll(Arrays.asList(dvals));
		}

		if (values.size() != getDetectorHeader().size()) {
			throw new IllegalArgumentException("Detector data does not hold the expected number of fields actual:" + values.size() + " expected:" + getDetectorHeader().size());
		}

		return values.toArray(Double[]::new);
	}

	@Override
	public List<String> getDetectorHeader() {
		return new ArrayList<>(Arrays.asList(detectorHeader));
	}

	@Override
	public List<String> getDetectorNames() {
		return new ArrayList<>(Arrays.asList(scanInfo.getDetectorNames()));
	}

	@Override
	public List<Detector> getDetectors() {
		return detectors;
	}

	@Override
	public boolean getHasChild() {
		return hasChild;
	}

	@Override
	public String getHeaderString() {
		return getHeaderString(null);
	}

	@Override
	public String getHeaderString(ScanDataPointFormatter dataPointFormatter) {
		// work out the lengths of the header string and the lengths of each element from the toString method
		// and pad each to adjust

		String header = getDelimitedHeaderString();

		String data = toDelimitedString();

		String[] headerElements = header.split(DELIMITER);
		String[] dataElements = data.split(DELIMITER);

		if(headerElements.length != dataElements.length )
			throw new IllegalArgumentException("Number of parts in header '" + headerElements.length + "' != number of parts in data '" + dataElements.length + "'");
		for (int i = 0; i < headerElements.length; i++) {
			int headerLength = headerElements[i].trim().length();
			int dataLength = dataElements[i].trim().length();

			int maxLength = dataLength > headerLength ? dataLength : headerLength;
			String format = "%" + maxLength + "s";

			headerElements[i] = String.format(format, headerElements[i].trim());
			dataElements[i] = String.format(format, dataElements[i].trim());
		}

		if (dataPointFormatter == null || !dataPointFormatter.isValid(this)) {
			return String.join(DELIMITER, headerElements);
		}

		// Optionally a data formatter may be set on Scan data points.
		// This formats them in a custom format.
		return dataPointFormatter.getHeader(this, MapUtils.createLinkedMap(headerElements, dataElements));
	}

	@Override
	public String getDelimitedHeaderString() {
		String header = String.join(DELIMITER, scannableHeader);
		if (detectorHeader.length > 0) {
			header += DELIMITER + String.join(DELIMITER, detectorHeader);
		}
		return header.trim();
	}

	private String createStringFromPositions() {
		StringBuilder sb = new StringBuilder();
		sb.append("");
		int i = 0;
		for (Object position : scannablePositions) {
			String[] thisPosition;
			try {
				thisPosition = ScannableUtils.getFormattedCurrentPositionArray(position, scannableFormats[i].length,
						scannableFormats[i]);
				for (String part : thisPosition) {
					sb.append(part);
					sb.append(DELIMITER);
				}
			} catch (Exception e) {
				// ignore so will get a truncated string
			}
			i++;
		}
		return sb.toString().trim();
	}

	private String createStringFromDetectorData() {
		StringBuilder sb = new StringBuilder("");
		int i = 0;
		for (Object dataItem : this.detectorData) {

			if (dataItem instanceof String || dataItem instanceof NexusTreeProvider
					|| dataItem instanceof PlottableDetectorDataClone) {
				sb.append(dataItem.toString());
				sb.append(DELIMITER);
			}
			/*
			 * else use the first format if it is not empty
			 */
			else if (this.detectorFormats[i] != null && this.detectorFormats[i].length > 0
					&& this.detectorFormats[i][0] != null && !this.detectorFormats[i][0].isEmpty()) {
				String[] thisPosition;
				try {
					thisPosition = ScannableUtils.getFormattedCurrentPositionArray(dataItem, detectorFormats[i].length,
							detectorFormats[i]);
					for (String part : thisPosition) {
						sb.append(part);
						sb.append(DELIMITER);
					}
				} catch (Exception e) {
					logger.error("Error getting position", e);
				}
			}
			/*
			 * else give up and get detector to return the string
			 */
			else {
				sb.append(DataWriterBase.getDetectorData(dataItem, false));
				sb.append(DELIMITER);
			}
			i++;
		}
		return sb.toString().trim();

	}

	@Override
	public String getInstrument() {
		return scanInfo.getInstrument();
	}

	@Override
	public List<String> getNames() {
		List<String> allNames = new ArrayList<>();
		allNames.addAll(getScannableNames());
		allNames.addAll(getDetectorNames());
		return allNames;
	}

	@Override
	public int getNumberOfChildScans() {
		return numberOfChildScans;
	}

	@Override
	public int getNumberOfPoints() {
		return scanInfo.getNumberOfPoints();
	}

	@Override
	public List<String> getPositionHeader() {
		return new ArrayList<>(Arrays.asList(scannableHeader));
	}

	@Override
	public List<Object> getPositions() {
		return scannablePositions;
	}

	@Override
	public int getScanIdentifier() {
		return scanInfo.getScanNumber();
	}

	@Override
	public List<String> getScannableNames() {
		return new ArrayList<>(Arrays.asList(scanInfo.getScannableNames()));
	}

	@Override
	public Double[] getPositionsAsDoubles() {
		final List<Double> vals = new ArrayList<>();
		if (getPositions() != null) {
			for (Object data : getPositions()) {
				PlottableDetectorData wrapper = new DetectorDataWrapper(data);
				Double[] dvals = wrapper.getDoubleVals();
				vals.addAll(Arrays.asList(dvals));
			}
		}
		if (vals.size() != getPositionHeader().size()) {
			throw new IllegalArgumentException("Position data does not hold the expected number of fields");
		}
		return vals.toArray(new Double[] {});
	}

	@Override
	public String[] getPositionsAsFormattedStrings() {

		String[] strings = new String[0];
		if (getPositions() != null) {
			int index = 0;
			for (Object data : getPositions()) {
				PlottableDetectorData wrapper = new DetectorDataWrapper(data);
				Double[] dvals = wrapper.getDoubleVals();
				String[] formattedVals = new String[dvals.length];
				String[] formats = this.scannableFormats[index];
				for (int j = 0; j < dvals.length; j++) {
					formattedVals[j] = String.format(formats[j], dvals[j]);
				}
				strings = (String[]) ArrayUtils.addAll(strings, formattedVals);
				index++;
			}
		}
		if (strings.length != getPositionHeader().size()) {
			throw new IllegalArgumentException("Position data does not hold the expected number of fields");
		}
		return strings;
	}

	/**
	 * The list of scannables this object refers to.
	 *
	 * @return list of scannables this object refers to.
	 */
	@Override
	public List<Scannable> getScannables() {
		return scannables;
	}

	@Override
	public ScanPlotSettings getScanPlotSettings() {
		return scanPlotSettings;
	}

	@Override
	public void setScanPlotSettings(ScanPlotSettings scanPlotSettings) {
		this.scanPlotSettings = scanPlotSettings;
	}

	@Override
	public int[] getScanDimensions() {
		return scanInfo.getDimensions();
	}

	@Override
	public void setScanDimensions(int[] scanDimensions) {
		scanInfo = ScanInformationBuilder.from(scanInfo)
				.dimensions(scanDimensions)
				.build();
	}

	@Override
	public String toString() {
		final String identifier = getCurrentFilename() != null ?  getCurrentFilename() : uniqueName;
		return "ScanDataPoint [point=" + (currentPointNumber+1) + "/" + scanInfo.getNumberOfPoints() + ", scan=" + identifier + "]";
	}

	@Override
	public String toFormattedString() {
		return toFormattedString(null);
	}

	@Override
	public String toFormattedString(ScanDataPointFormatter dataPointFormatter) {
		// work out the lengths of the header string and the lengths of each element from the toString method
		// and pad each to adjust

		String header = getDelimitedHeaderString();

		String data = toDelimitedString();

		String[] headerElements = header.split(DELIMITER);
		String[] dataElements = data.split(DELIMITER);

		for (int i = 0; i < headerElements.length; i++) {
			int headerLength = headerElements[i].trim().length();
			int dataLength = dataElements[i].trim().length();

			int maxLength = dataLength > headerLength ? dataLength : headerLength;
			String format = "%" + maxLength + "s";

			headerElements[i] = String.format(format, headerElements[i].trim());
			dataElements[i] = String.format(format, dataElements[i].trim());
		}

		if (dataPointFormatter == null || !dataPointFormatter.isValid(this)) {
			return String.join(DELIMITER, dataElements);
		}

		// Optionally a data formatter may be set on Scan data points.
		// This formats them in a custom format.
		return dataPointFormatter.getData(this, MapUtils.createLinkedMap(headerElements, dataElements));
	}

	String delimitedString=null;

	@Override
	public String toDelimitedString() {
		if( delimitedString == null){
			StringBuilder sb = new StringBuilder(createStringFromPositions());
			sb.append(DELIMITER);
			sb.append(createStringFromDetectorData());
			delimitedString = sb.toString().trim();
		}
		return delimitedString;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + currentPointNumber;
		result = prime * result + ((stepIds == null) ? 0 : stepIds.hashCode());
		result = prime * result + ((uniqueName == null) ? 0 : uniqueName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ScanDataPoint other = (ScanDataPoint) obj;
		if (currentPointNumber != other.currentPointNumber)
			return false;
		if (stepIds == null) {
			if (other.stepIds != null)
				return false;
		} else if (!stepIds.equals(other.stepIds))
			return false;
		if (uniqueName == null) {
			if (other.uniqueName != null)
				return false;
		} else if (!uniqueName.equals(other.uniqueName))
			return false;
		return true;
	}

	@Override
	public List<IScanStepId> getStepIds() {
		return stepIds;
	}

	@Override
	public void setStepIds(List<IScanStepId> stepIds) {
		this.stepIds = stepIds;
	}

	@Override
	public String getUniqueName() {
		return uniqueName;
	}

	@Override
	public void setUniqueName(String uniqueName) {
		this.uniqueName = uniqueName;
	}

	@Override
	public void setCommand(String command) {
		this.command = command;
	}

	@Override
	public void setCurrentFilename(String currentFilename) {
		scanInfo = ScanInformationBuilder.from(scanInfo)
				.filename(currentFilename)
				.build();
	}

	@Override
	public void setCurrentPointNumber(int currentPointNumber) {
		this.currentPointNumber = currentPointNumber;
	}

	@Override
	public void setHasChild(boolean hasChild) {
		this.hasChild = hasChild;
	}

	@Override
	public void setInstrument(String instrument) {
		scanInfo = ScanInformationBuilder.from(scanInfo)
				.instrument(instrument)
				.build();
	}

	/**
	 * @param numberOfChildScans
	 *            The numberOfChildScans to set.
	 */
	@Override
	public void setNumberOfChildScans(int numberOfChildScans) {
		this.numberOfChildScans = numberOfChildScans;
	}

	@Override
	public void setNumberOfPoints(int numberOfPoints) {
		scanInfo = ScanInformationBuilder.from(scanInfo)
				.numberOfPoints(numberOfPoints)
				.build();
	}

	@Override
	public void setScanIdentifier(int scanIdentifier) {
		scanInfo = ScanInformationBuilder.from(scanInfo)
				.scanNumber(scanIdentifier)
				.build();
	}

	@Override
	public String[][] getScannableFormats() {
		return scannableFormats;
	}

	@Override
	public void setScannableFormats(String[][] scannableFormats) {
		this.scannableFormats = scannableFormats;
	}

	@Override
	public void setDetectorHeader(String[] detectorHeader) {
		this.detectorHeader = detectorHeader;
	}

	@Override
	public String[] getScannableHeader() {
		return scannableHeader;
	}

	@Override
	public void setScannableHeader(String[] scannableHeader) {
		this.scannableHeader = scannableHeader;
	}

	@Override
	public List<Object> getScannablePositions() {
		return scannablePositions;
	}

	@Override
	public void setScannablePositions(List<Object> scannablePositions) {
		this.scannablePositions = scannablePositions;
	}

	@Override
	public List<IScanObject> getScanObjects() {
		return scanObjects;
	}

	@Override
	public void setScanObjects(List<IScanObject> scanObjects) {
		this.scanObjects = scanObjects;
	}

	@Override
	public String[][] getDetectorFormats() {
		return detectorFormats;
	}

	@Override
	public void setDetectorFormats(String[][] detectorFormats) {
		this.detectorFormats = detectorFormats;
	}

	@Override
	public Scannable getScannable(final String name) {
		final Iterator<Scannable> it = getScannables().iterator();
		while (it.hasNext()) {
			final Scannable s = it.next();
			if (s.getName().equals(name))
				return s;
		}
		return null;
	}

	@Override
	public boolean isScannable(final String name) {
		final Iterator<String> it = getScannableNames().iterator();
		while (it.hasNext()) {
			final String n = it.next();
			if (n.equals(name))
				return true;
		}
		return getScannable(name) != null;
	}

	@Override
	public Detector getDetector(final String name) {
		final Iterator<Detector> it = getDetectors().iterator();
		while (it.hasNext()) {
			final Detector s = it.next();
			if (s.getName().equals(name))
				return s;
		}
		return null;
	}

	@Override
	public boolean isDetector(String name) {
		final Iterator<String> it = getDetectorNames().iterator();
		while (it.hasNext()) {
			final String n = it.next();
			if (n.equals(name))
				return true;
		}
		return getDetector(name) != null;
	}

	@Override
	public ScanInformation getScanInformation() {
		return scanInfo;
	}

	public void setScanInformation(ScanInformation newScanInfo){
		scanInfo = newScanInfo;
	}
}
