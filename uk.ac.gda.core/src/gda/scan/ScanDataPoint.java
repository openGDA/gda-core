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

import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.joining;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;
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

	private static final Double[] NO_DOUBLE_DATA = new Double[0];

	private static final String[] NO_FORMATTED_DATA = new String[0];

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
	private List<String> detectorHeader = new ArrayList<>();

	/**
	 * The {@link gda.device.Detector} detectors that participate in the scan.
	 * Note, this will be null once the point has been deserialized, i.e. on the client
	 */
	private final transient List<Detector> detectors = new ArrayList<>();

	/**
	 * Formatting information for the scannable positions - used in the toString method. If an element is "" or null
	 * then do not format that in the output.
	 */
	private List<String[]> detectorFormats = new ArrayList<>();

	/**
	 * The expanded header names for scannable position in which the names are composed of the scannable name plus its
	 * element name, i.e. scannable's InputNames and ExtraNames
	 */
	private List<String> scannableHeader = new ArrayList<>();

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
	private List<String[]> scannableFormats = new ArrayList<>();

	/**
	 * The {@link gda.device.Scannable} scannables that participate in the scan.
	 * Note, this will be null once the point has been deserialized, i.e. on the client
	 */
	private final transient List<Scannable> scannables = new ArrayList<>();

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

	// cached values
	private Double[] allValuesAsDoubles=null;
	private String delimitedString = null;

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
		this.addDetectorData(detector.readout(), ScannableUtils.getExtraNamesFormats(detector));
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
			if (zeroInputExtraNameDevicePosition instanceof Object[] position && position.length == 0) {
				return;
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
		if (data != null) {
			this.detectorData.add(data);
			if (format == null || format.length == 0) {
				format = new String[] { "%s" };
			}
			detectorFormats.add(format);
		}
	}

	/**
	 * Replaces the detector data held by the object. The replacement List must be the same length as the previous for
	 * this sdp to be self-consistent.
	 *
	 * @param newData
	 */
	protected void setDetectorData(List<Object> newData, String[][] format) {
		setDetectorData(newData, Arrays.asList(format));
	}

	protected void setDetectorData(List<Object> newData, List<String[]> formats) {
		this.detectorData = newData;
		detectorFormats = new ArrayList<>(formats);
	}

	@Override
	public void setDetectorData(List<Object> newData) {
		this.detectorData = newData;
	}

	@Override
	public void addScannablePosition(Object data, String[] format) {
		if (data != null) {
			scannablePositions.add(data);
			scannableFormats.add(format);
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
		setScannablePositions(positions);

		Objects.requireNonNull(formats);
		this.scannableFormats = new ArrayList<>(Arrays.asList(formats));
	}

	@Override
	public void addDetector(Detector det) {
		ScanInformationBuilder newInfo = ScanInformationBuilder.from(scanInfo);
		newInfo.detectorNames((String[]) ArrayUtils.add(scanInfo.getDetectorNames(), det.getName()));
		scanInfo = newInfo.build();
		String[] extraNames = det.getExtraNames();
		if (extraNames != null && extraNames.length > 0) {
			detectorHeader.addAll(Arrays.asList(extraNames));
		} else {
			detectorHeader.add(det.getName());
		}
		detectors.add(det);
	}

	@Override
	public void addScannable(Scannable scannable) {
		scanInfo = ScanInformationBuilder.from(scanInfo)
				.scannableNames((String[]) ArrayUtils.add(scanInfo.getScannableNames(), scannable.getName()))
				.build();
		scannableHeader.addAll(Arrays.asList(scannable.getInputNames()));
		scannableHeader.addAll(Arrays.asList(scannable.getExtraNames()));
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
		return unmodifiableList(detectorData);
	}

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
			final PlottableDetectorData wrapper = (data instanceof PlottableDetectorData pData) ? pData : new DetectorDataWrapper(data);
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
		return unmodifiableList(detectorHeader);
	}

	@Override
	public List<String> getDetectorNames() {
		return unmodifiableList(Arrays.asList(scanInfo.getDetectorNames()));
	}

	@Override
	public List<Detector> getDetectors() {
		return detectors;
	}

	public void setDetectors(List<Detector> detectors) {
		this.detectors.clear();
		this.detectors.addAll(detectors);
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

		final String header = getDelimitedHeaderString();
		final String data = toDelimitedString();
		final String[] headerElements = header.split(DELIMITER);
		final String[] dataElements = data.split(DELIMITER);

		if (headerElements.length != dataElements.length)
			throw new IllegalArgumentException("Number of parts in header '" + headerElements.length + "' != number of parts in data '" + dataElements.length + "'");
		for (int i = 0; i < headerElements.length; i++) {
			final int headerLength = headerElements[i].trim().length();
			final int dataLength = dataElements[i].trim().length();
			final int maxLength = Math.max(dataLength, headerLength);
			final String format = "%" + maxLength + "s";

			headerElements[i] = String.format(format, headerElements[i].trim());
			dataElements[i] = String.format(format, dataElements[i].trim());
		}

		if (dataPointFormatter == null || !dataPointFormatter.isValid(this)) {
			return String.join(DELIMITER, headerElements);
		}

		// Optionally a data formatter may be set on Scan data points.
		// This formats them in a custom format.
		return dataPointFormatter.getHeader(MapUtils.createLinkedMap(headerElements, dataElements));
	}

	@Override
	public String getDelimitedHeaderString() {
		String header = String.join(DELIMITER, scannableHeader);
		if (!detectorHeader.isEmpty()) {
			header += DELIMITER + String.join(DELIMITER, detectorHeader);
		}
		return header.trim();
	}

	private String createStringFromPositions() {
		return IntStream.range(0, scannablePositions.size())
				.mapToObj(this::getFormattedPositionForScannableIndex)
				.flatMap(Arrays::stream)
				.collect(joining(DELIMITER));
	}

	private String[] getFormattedPositionForScannableIndex(int index) {
		final Object position = scannablePositions.get(index);
		final String[] formats = scannableFormats.get(index);
		try {
			return ScannableUtils.getFormattedCurrentPositionArray(position, formats.length, formats);
		} catch (DeviceException e) {
			// ignore so will get a truncated string
			return NO_FORMATTED_DATA;
		}
	}

	private String createStringFromAllDetectorData() {
		return IntStream.range(0, detectorData.size())
				.mapToObj(i -> createStringFromDetectorData(detectorData.get(i), detectorFormats.get(i)))
				.collect(joining(DELIMITER));
	}

	private String createStringFromDetectorData(final Object dataItem,
			final String[] detFormats) {
		if (dataItem instanceof String || dataItem instanceof NexusTreeProvider
				|| dataItem instanceof PlottableDetectorDataClone) {
			return dataItem.toString();
		} else if (detFormats != null && detFormats.length > 0 && detFormats[0] != null && !detFormats[0].isEmpty()) {
			// else use the first format if it is not empty
			try {
				final String[] thisPosition = ScannableUtils.getFormattedCurrentPositionArray(dataItem, detFormats.length, detFormats);
				return String.join(DELIMITER, thisPosition).trim();
			} catch (Exception e) {
				logger.error("Error getting position", e);
				return "";
			}
		} else {
			// else give up and get detector to return the string
			return DataWriterBase.getDetectorData(dataItem, false);
		}
	}

	@Override
	public String getInstrument() {
		return scanInfo.getInstrument();
	}

	@Override
	public List<String> getNames() {
		final List<String> allNames = new ArrayList<>();
		allNames.addAll(getScannableNames());
		allNames.addAll(getDetectorNames());
		return unmodifiableList(allNames);
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
		return unmodifiableList(scannableHeader);
	}

	@Override
	public List<Object> getPositions() {
		return getScannablePositions();
	}

	@Override
	public int getScanIdentifier() {
		return scanInfo.getScanNumber();
	}

	@Override
	public List<String> getScannableNames() {
		return List.of(scanInfo.getScannableNames());
	}

	@Override
	public Double[] getPositionsAsDoubles() {
		final List<Object> positions = getPositions();
		final List<String> positionHeader = getPositionHeader();

		if (positions.isEmpty()) {
			if (!getPositionHeader().isEmpty()) {
				throw new IllegalArgumentException("Unexpected empty position list");
			}
			return NO_DOUBLE_DATA;
		}

		final Double[] positionsArr = positions.stream()
				.map(DetectorDataWrapper::new)
				.map(DetectorDataWrapper::getDoubleVals)
				.flatMap(Arrays::stream)
				.toArray(Double[]::new);
		if (positionsArr.length != positionHeader.size()) {
			throw new IllegalArgumentException("Position data does not hold the expected number of fields");
		}
		return positionsArr;
	}

	@Override
	public String[] getPositionsAsFormattedStrings() {
		final Double[] positionArr = getPositionsAsDoubles();
		final String[] formats = scannableFormats.stream()
				.flatMap(Arrays::stream)
				.toArray(String[]::new);

		if (formats.length != positionArr.length) {
			throw new IllegalArgumentException("Position data does not contains the same number of fields as the number of format strings");
		}
		return IntStream.range(0, positionArr.length)
				.mapToObj(i -> String.format(formats[i], positionArr[i]))
				.toArray(String[]::new);
	}

	/**
	 * The list of scannables this object refers to.
	 *
	 * @return list of scannables this object refers to.
	 */
	@Override
	public List<Scannable> getScannables() {
		return unmodifiableList(scannables);
	}

	public void setScannables(List<Scannable> scannables) {
		this.scannables.clear();
		this.scannables.addAll(scannables);
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
		return dataPointFormatter.getData(MapUtils.createLinkedMap(headerElements, dataElements));
	}

	@Override
	public String toDelimitedString() {
		if (delimitedString == null) {
			StringBuilder sb = new StringBuilder(createStringFromPositions());
			sb.append(DELIMITER);
			sb.append(createStringFromAllDetectorData());
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
		return unmodifiableList(stepIds);
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
		return scannableFormats.toArray(String[][]::new);
	}

	@Override
	public void setScannableFormats(String[][] scannableFormats) {
		setScannableFormats(Arrays.asList(scannableFormats));
	}

	public void setScannableFormats(List<String[]> scannableFormats) {
		this.scannableFormats = new ArrayList<>(scannableFormats);
	}

	@Override
	public void setDetectorHeader(String[] detectorHeader) {
		this.detectorHeader = new ArrayList<>(Arrays.asList(detectorHeader));
	}

	@Override
	public String[] getScannableHeader() {
		return scannableHeader.toArray(String[]::new);
	}

	@Override
	public void setScannableHeader(String[] scannableHeader) {
		this.scannableHeader = new ArrayList<>(Arrays.asList(scannableHeader));
	}

	@Override
	public List<Object> getScannablePositions() {
		return unmodifiableList(scannablePositions);
	}

	@Override
	public void setScannablePositions(List<Object> scannablePositions) {
		Objects.requireNonNull(scannablePositions);
		this.scannablePositions = scannablePositions;
	}

	@Override
	public List<IScanObject> getScanObjects() {
		return unmodifiableList(scanObjects);
	}

	@Override
	public void setScanObjects(List<IScanObject> scanObjects) {
		this.scanObjects = scanObjects;
	}

	@Override
	public String[][] getDetectorFormats() {
		return detectorFormats.toArray(String[][]::new);
	}

	@Override
	public void setDetectorFormats(String[][] detectorFormats) {
		setDetectorFormats(Arrays.asList(detectorFormats));
	}

	public void setDetectorFormats(List<String[]> detectorFormats) {
		this.detectorFormats = new ArrayList<>(detectorFormats);
	}

	@Override
	public Scannable getScannable(final String name) {
		return getScannables().stream()
				.filter(s -> s.getName().equals(name))
				.findFirst()
				.orElse(null);
	}

	@Override
	public boolean isScannable(final String name) {
		return getScannable(name) != null;
	}

	@Override
	public Detector getDetector(final String name) {
		return getDetectors().stream()
				.filter(s -> s.getName().equals(name))
				.findFirst()
				.orElse(null);
	}

	@Override
	public boolean isDetector(String name) {
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
