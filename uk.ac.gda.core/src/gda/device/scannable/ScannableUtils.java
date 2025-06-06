/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package gda.device.scannable;

import static gda.data.scan.nexus.device.GDADeviceNexusConstants.PROPERTY_VALUE_WRITE_DECIMALS;
import static java.util.stream.Collectors.joining;
import static java.util.stream.IntStream.range;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.measure.Quantity;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.python.core.Py;
import org.python.core.PyException;
import org.python.core.PyFloat;
import org.python.core.PyInteger;
import org.python.core.PyLong;
import org.python.core.PyNone;
import org.python.core.PyObject;
import org.python.core.PySequence;
import org.python.core.PyString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Streams;

import gda.configuration.properties.LocalProperties;
import gda.data.PlottableDetectorData;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.Monitor;
import gda.device.Scannable;
import gda.device.ScannableMotion;
import gda.device.ScannableMotionUnits;
import gda.device.scannable.scannablegroup.IScannableGroup;
import gda.factory.Finder;
import gda.util.QuantityFactory;

/**
 * A collection of tools for Scannables and packages which use them
 */
public final class ScannableUtils {

	private static final Logger logger = LoggerFactory.getLogger(ScannableUtils.class);

	// add a small amount to values to ensure that the final point in the scan is included
	private static final double FUDGE_FACTOR = 1e-10;

	private static final String INDENT = "  ";

	private ScannableUtils() {
		// Private constructor to prevent instantiation
	}

	/**
	 * Exception thrown when a call to ScannableBase.validate() fails.
	 */
	public static class ScannableValidationException extends Exception {
		/**
		 * the message
		 */
		final String message;

		/**
		 * Constructor.
		 *
		 * @param message
		 */
		public ScannableValidationException(String message) {
			this.message = message;
		}
	}

	/**
	 * Returns the current position of the Scannable as an HTML string. This is mainly used in gda.gui.utils.WatchPanel.
	 * <p>
	 * Use this for pretty-printing Scannables.
	 *
	 * @param scannable
	 * @return string pretty representation of this scannable's position
	 * @throws DeviceException
	 */
	public static String getHTMLFormattedCurrentPosition(Scannable scannable) throws DeviceException {
		final StringBuilder output = new StringBuilder();
		final String[] positionArray = ScannableUtils.getFormattedCurrentPositionArray(scannable);
		if (scannable.getInputNames().length + scannable.getExtraNames().length == 1) {
			output.append(positionArray[0]);
		} else {
			output.append("<html><dl>");
			int i = 0;
			for (; i < scannable.getInputNames().length; i++) {
				output.append("<dt><b>");
				output.append(scannable.getInputNames()[i]);
				output.append("</b></dt><dd>");
				output.append(positionArray[i]);
				output.append("</dd>");
			}

			for (int j = 0; j < scannable.getExtraNames().length; j++) {
				output.append("<dt><i>");
				output.append(scannable.getExtraNames()[j]);
				output.append("</i></dt><dd>");
				output.append(positionArray[i + j]);
				output.append("</dd>");
			}
			output.append("</dl><html>");
		}
		return output.toString().trim();
	}

	/**
	 * Returns the current position of the Scannable as a string.
	 * <p>
	 * Use this for pretty-printing Scannables.
	 *
	 * @param scannable
	 * @return string representation of this Scannable
	 * @throws DeviceException
	 */
	public static String getFormattedCurrentPosition(Scannable scannable) throws DeviceException {
		Double[] offsetArray = new Double[scannable.getInputNames().length + scannable.getExtraNames().length];
		return getFormattedCurrentPosition(scannable, null, offsetArray);
	}

	public static String getFormattedCurrentPosition(ScannableMotion scannable) throws DeviceException {
		Double[] offsetArray = new Double[scannable.getInputNames().length + scannable.getExtraNames().length];
		if (scannable.getOffset() != null) {
			// Complication - the offset array may not have offsets for the extra fields.
			System.arraycopy(scannable.getOffset(), 0, offsetArray, 0, scannable.getOffset().length);
		}
		return getFormattedCurrentPosition(scannable, null, offsetArray);
	}

	public static String getFormattedCurrentPosition(ScannableMotionUnits scannable) throws DeviceException {
		Double[] offsetArray = new Double[scannable.getInputNames().length + scannable.getExtraNames().length];
		if (scannable.getOffset() != null) {
			// Complication - the offset array may not have offsets for the extra fields.
			System.arraycopy(scannable.getOffset(), 0, offsetArray, 0, scannable.getOffset().length);
		}
		String[] unitStringArray = new String[scannable.getInputNames().length + scannable.getExtraNames().length];
		for (int i = 0; i < unitStringArray.length; i++) {
			unitStringArray[i] = scannable.getUserUnits();
		}
		return getFormattedCurrentPosition(scannable, unitStringArray, offsetArray);
	}

	/**
	 * Returns the current position of the Scannable as a string including units.
	 * <p>
	 * Use this for pretty-printing Scannables.
	 *
	 * @param scannable
	 * @param unitStringArray
	 *            may be null otherwise must match length of input and extra fields
	 * @return string representation of this Scannable
	 * @throws DeviceException
	 */
	public static String getFormattedCurrentPosition(Scannable scannable, String[] unitStringArray, Double[] offsetArray)
			throws DeviceException {
		Object position = null;
		try {
			position = scannable.getPosition();
		} catch (Exception e) {
			throw new DeviceException("error fetching " + scannable.getName() + " position", e);
		}
		return getFormattedPosition(position, scannable, unitStringArray, offsetArray);
	}

	/**
	 * Returns the current position of the Scannable as a string including units.
	 * <p>
	 * Use this for pretty-printing Scannables.
	 *
	 * @param position
	 * @param scannable
	 * @param unitStringArray
	 *            may be null otherwise must match length of input and extra fields
	 * @param offsetArray
	 * @return string representation of this Scannable
	 * @throws DeviceException
	 */
	public static String getFormattedPosition(Object position, Scannable scannable, String[] unitStringArray,
			Double[] offsetArray) throws DeviceException {
		if (position == null && scannable.getInputNames().length == 0 && scannable.getExtraNames().length == 0
				&& scannable.getOutputFormat().length == 0) {
			return scannable.getName() + " : ---";
		}

		final String[] fieldNames = getAllFieldNames(scannable);
		final String[] formattedPositionArray = getFormattedCurrentPositionArray(scannable, position);
		final String[] formattedOffsetArray = createFormattedOffsetArray(offsetArray, scannable);
		if (fieldNames.length == 1 && (fieldNames[0].equals(scannable.getName()) || fieldNames[0].equals(Scannable.DEFAULT_INPUT_NAME))) {
			// most common case: a single field whose name is either the name of the scannable or 'value', don't write the field name
			return scannable.getName() + " : " + formattedPositionArray[0] + (unitStringArray == null ? "" : unitStringArray[0]) + formattedOffsetArray[0];
		}

		return scannable.getName() + " : " +
					IntStream.range(0, fieldNames.length)
					.mapToObj(i -> fieldNames[i] + ": " + formattedPositionArray[i] + (unitStringArray == null ? "" : unitStringArray[i]) + formattedOffsetArray[i])
					.collect(joining(" "));
	}

	/**
	 * Returns the number of decimal places for each field of the scannable, as an array
	 * @param scannable the scannable
	 * @return number of decimal places for each field
	 */
	public static int[] getNumDecimalsArray(final Scannable scannable) {
		if (!LocalProperties.check(PROPERTY_VALUE_WRITE_DECIMALS, false)) return null; // NOSONAR
		if (scannable.getOutputFormat() == null) return null; // NOSONAR

		// note, scannable outputFormat must be set to an array of the same length as the scannable position
		return Arrays.stream(scannable.getOutputFormat()).mapToInt(ScannableUtils::getNumDecimals).toArray();
	}

	// copied from java.util.Formatter
	private static final Pattern FORMAT_PATTERN = Pattern.compile(
			"%(\\d+\\$)?([-#+ 0,(\\<]*)?(\\d+)?(\\.\\d+)?([tT])?([a-zA-Z%])"); // NOSONAR - pattern is correct
	private static final String FLOAT_CONVERSIONS = "fgeFGE"; // conversion characters for floating-point values for java.util.Formatter

	/**
	 * Returns the number of decimals specified in the given output format
	 * @param outputFormat
	 * @return number of decimals
	 */
	public static int getNumDecimals(String outputFormat) {
		// the output format is a format string for the String.format() method, e.g. "%5.3g", where the
		// (optional) first digit is the number of digits required before the decimal point in the output string,
		// the second digit is the number of digits after. The final letter will be either e, f, or g
		// for floating point numbers - the only type we are concerned with
		final Matcher matcher = FORMAT_PATTERN.matcher(outputFormat);

		if (matcher.find(0)) {
			final char conversion = outputFormat.charAt(matcher.start(6));
			if (FLOAT_CONVERSIONS.indexOf(conversion) == -1) {
				return -1; // not a float
			}
			final int precisionStart = matcher.start(4); // group 4 is the precision group (\\.\\d+)?
			if (precisionStart >= 0) {
				// parse the precision as an int (start + 1 to skip the leading '.')
				final int precisionEnd = matcher.end(4);
				final int precision = Integer.parseInt(outputFormat, precisionStart + 1, precisionEnd, 10);
				if (precision > 0) {
					return precision;
				} else {
					logger.warn("Invalid precision in output format ''{}''", outputFormat);
				}
			}
		} else {
			logger.warn("Invalid output format ''{}''", outputFormat);
		}

		return -1;
	}

	private static String[] getAllFieldNames(Scannable scannable) {
		String[] fieldNames = new String[scannable.getInputNames().length + scannable.getExtraNames().length];
		System.arraycopy(scannable.getInputNames(), 0, fieldNames, 0, scannable.getInputNames().length);
		System.arraycopy(scannable.getExtraNames(), 0, fieldNames, scannable.getInputNames().length,
				scannable.getExtraNames().length);
		return fieldNames;

	}

	private static String[] createFormattedOffsetArray(Double[] offsetArray, Scannable scannable)
			throws DeviceException {
		String[] formatedOffsetArray;
		try {

			formatedOffsetArray = getFormattedCurrentPositionArray(offsetArray, scannable.getInputNames().length
					+ scannable.getExtraNames().length, scannable.getOutputFormat());
			for (int i = 0; i < formatedOffsetArray.length; i++) {
				if (formatedOffsetArray[i].equals("unknown")) {
					formatedOffsetArray[i] = "";
				} else {
					if (offsetArray[i] > 0) {
						formatedOffsetArray[i] = "+" + formatedOffsetArray[i];
					}
					formatedOffsetArray[i] = "(" + formatedOffsetArray[i] + ")";
				}
			}
		} catch (Exception e) {
			throw new DeviceException("error formatting offset string of '" + scannable.getName() + "'", e);
		}
		return formatedOffsetArray;
	}

	/**
	 * Breaks currentPositionObj into the required number of parts. For each part if formatString given return
	 * String.format(formatString, part) else if String return as as else return toString value
	 *
	 * @param positionObject
	 * @param numberOfParts
	 * @param formats
	 * @return array of position values taken from the currentPosition object
	 * @throws DeviceException
	 */

	public static String[] getFormattedCurrentPositionArray(Object positionObject, int numberOfParts, String[] formats)
			throws DeviceException {
		if (positionObject == null) {
			throw new DeviceException("current Position object is null");
		}
		ScannableGetPosition wrapper = new ScannableGetPositionWrapper(positionObject, formats);
		String[] strs = wrapper.getStringFormattedValues();
		if (strs.length != numberOfParts) {
			throw new DeviceException("position given was: " + positionObject.toString() + ", which had " + strs.length
					+ " part(s), but input/extraNames had " + numberOfParts + " part(s) and outputFormats had "
					+ formats.length + " part(s).");
		}
		return strs;
	}

	public static String[] getFormattedCurrentPositionArray(Scannable scannable, Object position)
			throws DeviceException {
		try {
			return getFormattedCurrentPositionArray(position, scannable.getInputNames().length
					+ scannable.getExtraNames().length, scannable.getOutputFormat());
		} catch (Exception e) {
			throw new DeviceException("error formatting position string of '" + scannable.getName() + "'", e);
		}
	}

	/**
	 * Returns the current position of the Scannable as a formatted array of Strings.
	 * <p>
	 * Use this for pretty-printing Scannables.
	 *
	 * @param scannable
	 * @return formatted array of Strings
	 * @throws DeviceException
	 */
	public static String[] getFormattedCurrentPositionArray(Scannable scannable) throws DeviceException {
		Object position = null;
		try {
			position = scannable.getPosition();
		} catch (Exception e) {
			throw new DeviceException("error fetching " + scannable.getName() + " position", e);
		}
		try {
			return getFormattedCurrentPositionArray(position,
					scannable.getInputNames().length + scannable.getExtraNames().length, scannable.getOutputFormat());
		} catch (Exception e) {
			throw new DeviceException("error formatting position string of '" + scannable.getName() + "'", e);
		}
	}

	/**
	 * Formats a scannable which contains child scannables, such as ScannableGroup & CoupledScannable.
	 *
	 * @param parent Overall scannable
	 * @param children Collection of child scannables
	 * @param includeParentPosition Controls whether position of parent scannable should be included in output
	 * @return Name and position of parent and child scannables indented for terminal output.
	 */
	public static String formatScannableWithChildren(Scannable parent, Collection<Scannable> children, boolean includeParentPosition) {
		final StringBuilder positionString = new StringBuilder();
		if (includeParentPosition) {
			try {
				positionString.append(getFormattedCurrentPosition(parent));
			} catch (Exception ex) {
				logger.warn("Error formatting device position for {}", parent.getName(), ex);
				positionString.append(String.format("%s : %s", parent.getName(), Scannable.VALUE_UNAVAILABLE));
			}
		} else {
			positionString.append(parent.getName());
		}
		positionString.append(" ::");
		for (final Scannable s : children) {
			String pos = null;
			try {
				pos = s.toFormattedString();
			} catch (Exception ex) {
				logger.warn("Error formatting device position for {}", s.getName(), ex);
				pos = String.format("%s : %s", s.getName(), Scannable.VALUE_UNAVAILABLE);
			}
			for (String line : pos.split("\n")) {
				positionString.append('\n');
				positionString.append(INDENT);
				positionString.append(line);
			}
		}
		return alignOutput(positionString.toString());
	}

	private static String alignOutput(String unaligned) {
		int longest = 0;
		for (final String line : unaligned.split("\n")) {
			int colon = line.indexOf(':');
			if (colon > longest) {
				longest = colon;
			}
		}

		final StringBuilder outputString = new StringBuilder();
		for (String line : unaligned.split("\n")) {
			final String[] splitLine = line.split("(?<!:):(?!:)", 2);// only split on first colon, ignore double colon
			if (splitLine.length > 1) {
				int colon = splitLine[0].length();
				int padding = longest - colon + 1;
				outputString.append(splitLine[0]);
				outputString.append(String.format("%" + padding + "s", ":"));
				outputString.append(splitLine[1]);
			} else {
				// no colon - just print as is
				outputString.append(line);
			}
			outputString.append('\n');
		}
		return outputString.toString().trim();
	}

	/**
	 * Returns the current position of the given Scannable as an array of doubles
	 *
	 * @param scannable
	 * @return the position as an array of doubles
	 * @throws DeviceException
	 */
	public static double[] getCurrentPositionArray(Scannable scannable) throws DeviceException {
		// get object returned by getPosition
		Object currentPositionObj = scannable.getPosition();
		return positionToArray(currentPositionObj, scannable);
	}

	private static double[] positionToArray(String name, Object currentPositionObj, int numberOfParts)
			throws DeviceException {
		if (currentPositionObj == null || numberOfParts == 0) {
			return null;
		}

		Double[] objectToArray = objectToArray(currentPositionObj);
		if (objectToArray.length == numberOfParts) {
			return ArrayUtils.toPrimitive(objectToArray);
		}
		throw new DeviceException("positionToArray error (" + currentPositionObj.toString() + ") of " + name
				+ " as array sizes inconsistent. objectToArray.length = " + Integer.toString(objectToArray.length)
				+ ", numberOfParts = " + Integer.toString(numberOfParts));

	}

	/**
	 * Returns the current position of the given Scannable as an array of doubles. This will not call the scannable's
	 * getPosition() method.
	 *
	 * @param currentPositionObj
	 * @param scannable
	 * @return the position as an array of doubles
	 * @throws DeviceException
	 */
	public static double[] positionToArray(Object currentPositionObj, Scannable scannable) throws DeviceException {
		return positionToArray(scannable.getName(), currentPositionObj,
				(scannable instanceof Monitor monitor) ? monitor.getElementCount()
						: scannable.getInputNames().length + scannable.getExtraNames().length);
	}

	/**
	 * Returns an array which is the current position, but with the 'extra' fields removed
	 *
	 * @param scannable
	 * @return the position as an array of doubles
	 * @throws Exception
	 */
	public static double[] getCurrentPositionArray_InputsOnly(Scannable scannable) throws Exception {

		// get object returned by getPosition
		Object currentPositionObj = scannable.getPosition();

		// if it's null or we're expecting it to be null from the arrays, return
		// null
		if (currentPositionObj == null || scannable.getInputNames().length == 0) {
			return null;
		}

		// else create an array of the expected size and fill it
		double[] currentPosition = new double[scannable.getInputNames().length];
		for (int i = 0; i < currentPosition.length; i++) {
			if (currentPositionObj.getClass().isArray()) {
				currentPosition[i] = Array.getDouble(currentPositionObj, i);
			} else if (currentPositionObj instanceof PySequence pySeq) {
				currentPosition[i] = Double.parseDouble(pySeq.__finditem__(i).toString());
			} else {
				currentPosition[i] = Double.parseDouble(currentPositionObj.toString());
			}
		}
		return currentPosition;

	}

	/**
	 * Given a current position and an incremental move, calculates the target position.
	 * <p>
	 * This assumes that both objects are of the same type and are one of: number, Java array of numbers, Jython array
	 * of numbers
	 *
	 * @param previousPoint
	 * @param step
	 * @return the target position of the relative move
	 */
	public static Object calculateNextPoint(Object previousPoint, Object step) {

		// check we have valid values
		if (previousPoint == null) {
			return null;
		}

		if (step == null) {
			return previousPoint;
		}

		// convert both inputs to arrays of doubles
		Double[] previousArray = objectToArray(previousPoint);
		Double[] stepArray = objectToArray(step);

		// then add the arrays
		int length = Array.getLength(previousArray);
		Double[] output = addLists(length, previousArray, stepArray);

		if (output.length == 1) {
			return output[0];
		}
		// else
		return output;

	}

	/**
	 * Element by element addition of two arrays
	 *
	 * @param length
	 * @param previous
	 * @param step
	 * @return the sum of the arrays
	 */
	private static Double[] addLists(int length, Double[] previous, Double[] step) {
		Double[] outputArray = new Double[length];

		for (int i = 0; i < length; i++) {

			Double previousValue = previous[i];
			if (previousValue == null) {
				previousValue = 0.0;
			}
			Double stepValue = step[i];
			if (stepValue == null) {
				stepValue = 0.0;
			}

			outputArray[i] = previousValue + stepValue;
		}
		return outputArray;
	}

	private static Double getDouble(Object val, int index) {
		if (val instanceof Number[] numberArr) {
			return numberArr[index].doubleValue();
		}
		if (val.getClass().isArray()) {
			return Array.getDouble(val, index);
		}
		if (val instanceof PySequence pySeq) {
			PyObject item = pySeq.__finditem__(index);
			if (item instanceof PyNone) {
				return null;
			}
			if (item instanceof PyString pyString) {
				return Double.parseDouble(pyString.toString());
			}
			return Py.tojava(item, Number.class).doubleValue();
		}
		if (val instanceof List<?> list) {
			return Double.parseDouble(list.get(index).toString());
		}
		throw new IllegalArgumentException("getDouble. Object cannot be converted to Double");
	}

	/**
	 * Assuming the objects can be converted into doubles, this calculates the number of steps for the given Scannables
	 * based on the given start, stop and step values.
	 *
	 * @param theScannable
	 *            Scannable
	 * @param start
	 *            Object
	 * @param stop
	 *            Object
	 * @param step
	 *            Object
	 * @return int
	 * @throws Exception
	 */
	public static int getNumberSteps(Scannable theScannable, Object start, Object stop, Object step) throws Exception {

		// the expected size of the start, stop and step objects
		int numArgs = theScannable.getInputNames().length;

		int numArrayParam = 0;

		if (start.getClass().isArray()) {
			numArrayParam = ((Object[]) start).length;
		} else if (start instanceof PySequence pySeq) {
			numArrayParam = pySeq.__len__();
		}

		// if position objects are a single value, or if no inputNames
		if (numArgs <= 1 && numArrayParam == 0) {
			Double startValue = Double.valueOf(start.toString());
			Double stopValue = Double.valueOf(stop.toString());
			Double stepValue = Math.abs(Double.valueOf(step.toString()));
			if (stepValue == 0)
				throw new Exception("Step size is zero so number of points cannot be calculated");
			return getNumberSteps(startValue, stopValue, stepValue);
		}

		// if there is a mismatch to the position object and the Scannable, throw an error
		if (numArgs != numArrayParam) {
			throw new Exception("Position arguments do not match size of Pseudo Device: " + theScannable.getName()
					+ ". Check size of inputNames array for this object.");
		}


		// ELSE position objects are an array
		int maxSteps = 0;
		int minSteps = java.lang.Integer.MAX_VALUE;
		// Loop through each field
		for (int i = 0; i < numArgs; i++) {
			Double startValue = getDouble(start, i);
			Double stopValue = getDouble(stop, i);
			Double stepValue = getDouble(step, i);
			if (stepValue == null) {
				if (startValue == null && stopValue == null) {
					// stepSize is null, but this is okay with no start/stop values
					continue;
				}
				throw new Exception(
						"a step field is None/null despite there being a corresponding start and/or stop value.");
			}

			if (startValue == null || stopValue == null) {
				throw new Exception("a start or end field is None/null without a corresponding None/null step size.");
			}

			double difference = Math.abs(stopValue - startValue);
			if (stepValue == 0.) {
				if (difference < FUDGE_FACTOR) {
					// zero step value okay as there is no distance to move
					continue;
				}
				throw new Exception("a step field is zero despite there being a distance to move in that direction.");
			}

			double fudgeValue = stepValue * FUDGE_FACTOR;
			int steps = (int) Math.abs((difference + Math.abs(fudgeValue)) / stepValue);
			if (steps > maxSteps) {
				maxSteps = steps;
			}
			if (steps < minSteps) {
				minSteps = steps;
			}
		}

		if (maxSteps - minSteps > 1) {
			throw new Exception("The step-vector does not connect the start and end points within the allowed\n"
					+ "tolerance of one step: in one basis direction " + maxSteps + " steps are required, but\n"
					+ "in another only " + minSteps + " steps are required.");
		}

		return minSteps;

	}

	public static int getNumberSteps(Double startValue, Double stopValue, Double stepValue) {
		int maxSteps = 0;

		if (stepValue == 0) {
			return 0;
		}
		double fudgeValue = stepValue * FUDGE_FACTOR;
		double difference = Math.abs(stopValue - startValue);
		maxSteps = (int) Math.abs((difference + Math.abs(fudgeValue)) / stepValue);
		return maxSteps;
	}

	/**
	 * Performs some basic validation of the given Scannable to check for internal consistency. This should be called at
	 * the end of every Scannable's constructor.
	 * <p>
	 * If the validation fails then an exception with an explanatory message is thrown.
	 *
	 * @param theScannable
	 * @throws ScannableUtils.ScannableValidationException
	 */
	public static void validate(Scannable theScannable) throws ScannableUtils.ScannableValidationException {
		int inputNamesSize = theScannable.getInputNames().length;
		int extraNamesSize = theScannable.getExtraNames().length;
		int outputFormatSize = theScannable.getOutputFormat().length;

		int positionSize = 0;
		Object position;
		try {
			position = theScannable.getPosition();
		} catch (PyException e) {
			throw new ScannableUtils.ScannableValidationException("validation error. Cannot get position string of "
					+ theScannable.getName() + ": " + e.toString());
		} catch (DeviceException e) {
			throw new ScannableUtils.ScannableValidationException(theScannable.getName()
					+ ": validation error. Cannot get position: " + e.getMessage());
		}

		if (position == null) {
			// do nothing as it could be a zero output scannable
		} else if (position.getClass().isArray()) {
			positionSize = Array.getLength(position);
		} else if (position instanceof PySequence pySeq) {
			positionSize = pySeq.__len__();
		} else {
			positionSize = 1;
		}

		// test that inputNames and extraNames are the same size as the
		// outputFormat array
		if ((inputNamesSize + extraNamesSize) != outputFormatSize) {
			throw new ScannableUtils.ScannableValidationException("outputFormat array of size: " + outputFormatSize
					+ " but inputNames and extraNames have " + (inputNamesSize + extraNamesSize) + " elements");
		}

		// test that getPosition returns an array which is the same size
		// as the inputNames and extraNames
		if ((inputNamesSize + extraNamesSize) != positionSize) {
			throw new ScannableUtils.ScannableValidationException("position is an array of size: " + outputFormatSize
					+ " but inputNames and extraNames have " + (inputNamesSize + extraNamesSize) + " elements");
		}
	}

	/**
	 * Convert an object to a Double
	 * <dl>
	 * <dt>Number</dt>
	 *     <dd>🡆 Result of doubleValue()</dd>
	 * <dt>String</dt>
	 *     <dd>🡆 Try parsing as either double or quantity</dd>
	 * <dt>Quantity</dt>
	 *     <dd>🡆 Value component - no unit conversions are made</dd>
	 * <dt>PyObject</dt>
	 *     <dd>🡆 Attempt conversion to number or string and try again</dd>
	 * <dt>Object</dt>
	 *     <dd>🡆 Call toString and try to parse the result</dd>
	 * <dt>null</dt>
	 *     <dd>🡆 null</dd>
	 * <dl>
	 * If no conversion is possible return null to differentiate the case where
	 * "NaN" or {@link Double#NaN} is passed in.
	 * @param value Anything that might be useful as a number
	 * @return Some kind of double value - null if no conversion is possible
	 */
	public static Double objectToDouble(Object value) {
		if (value == null) {
			return null;
		} else if (value instanceof Number number) {
			return number.doubleValue();
		} else if (value instanceof String string) {
			try {
				return Double.parseDouble(string);
			} catch (NumberFormatException e) {
				// either quantity or null - either can be handled
				return objectToDouble(QuantityFactory.createFromString(string));
			}
		} else if (value instanceof PyObject pyObject) {
			Object pynumber = pyObject.__tojava__(Double.class);
			if (pynumber instanceof Number number) {
				return number.doubleValue();
			} // else fallback to string version below
		} else if (value instanceof Quantity<?> quantity) {
			return quantity.getValue().doubleValue();
		}

		// last attempt, see if it looks like a double if you squint
		return objectToDouble(value.toString());
	}

	/**
	 * Converts an object to an array of Doubles.
	 * <br/>
	 * If it is an array or list, each value is converted according to
	 * {@link #objectToDouble(Object)} and returned in an array. Otherwise,
	 * attempt to convert the object to a single double and return an array of
	 * a single value.
	 *
	 * @param position Any object representing a double or sequence of doubles
	 * @return array of Doubles
	 */
	public static Double[] objectToArray(Object position) {
		if (position instanceof Double[] doubleArr) {
			return doubleArr;
		} else if (position == null) {
			return new Double[0];
		}

		if (position instanceof Object[] objectArr) {
			// Object array
			return Stream.of(objectArr)
					.map(ScannableUtils::objectToDouble)
					.toArray(Double[]::new);
		} else if (position.getClass().isArray()) {
			// primitive array
			return range(0, Array.getLength(position))
					.mapToObj(i -> Array.get(position, i))
					.map(ScannableUtils::objectToDouble)
					.toArray(Double[]::new);
		} else if (position instanceof String || position instanceof PyString) {
			// Single String value
			return new Double[] { objectToDouble(position.toString()) };
		} else if (position instanceof PySequence pySeq) {
			// Python Tuple/List etc
			return range(0, pySeq.__len__())
					.mapToObj(pySeq::__getitem__)
					.map(ScannableUtils::objectToDouble)
					.toArray(Double[]::new);
		} else if (position instanceof List<?>) {
			return ((List<?>)position).stream()
					.map(ScannableUtils::objectToDouble)
					.toArray(Double[]::new);
		} else { // Try and get a single value out of it
			Double value = objectToDouble(position);
			if (value == null) {
				throw new NumberFormatException("Cannot convert " + position.getClass() + " to double");
			}
			return new Double[] { value };
		}
	}

	/**
	 * Converts an object to an array of Objects.
	 * <br/>
	 * If it is an array, or iterable (including PySequence), it is returned as an array.
	 * Otherwise, an array is returned with a single value.
	 *
	 * Unlike {@link #objectToArray(Object)}, this method does not attempt to
	 * convert objects to double values, but it does convert Jython objects to their
	 * Java equivalents.
	 *
	 * @param object any object to convert
	 * @return the object as an array of Java objects
	 */
	public static Object[] toObjectArray(Object object) {
		if (object instanceof PyObject pyObject) {
			final Object javaObj = convertToJava(pyObject);
			return javaObj instanceof Object[] objectArr ? objectArr : new Object[] { javaObj };
		} else if (object instanceof Double[] doubleArr) {
			return doubleArr; // saves using streams and creating a new array
		} else if (object instanceof Object[] objectArr) {
			return Arrays.stream(objectArr).map(ScannableUtils::convertToJava).toArray();
		} else if (object instanceof Iterable<?> iterable) {
			return Streams.stream(iterable).map(ScannableUtils::convertToJava).toArray();
		} else if (object instanceof PlottableDetectorData detData) {
			return detData.getDoubleVals();
		} else if (object != null && object.getClass().isArray()) {
			// object must be a primitive array as Object[] case is above
			return range(0, Array.getLength(object))
					.mapToObj(i -> Array.get(object, i))
					.toArray();
		}

		return new Object[] { object };
	}

	/**
	 * @see #convertToJava(PyObject)
	 * @param object
	 * @return null
	 */
	public static Object convertToJava(Object object) {
		return object instanceof PyObject pyObject ? convertToJava(pyObject) : object;
	}

	/**
	 * Converts a Jython PyObject into its Java equivalent if that is possible. This only works on the sorts of objects
	 * dealt with in the Jython environment i.e. Strings, integers, floats (doubles) and arrays of these.
	 * <P>
	 * If this fails or cannot work for any reason then null is returned.
	 *
	 * @param object
	 * @return Java equivalent object
	 */
	public static Object convertToJava(PyObject object) {
		if (object instanceof PyFloat) {
			return object.__tojava__(Double.class);
		} else if (object instanceof PyInteger) {
			return object.__tojava__(Integer.class);
		} else if (object instanceof PyLong) {
			return object.__tojava__(Long.class);
		} else if (object instanceof PyString) {
			return object.__tojava__(String.class);
		} else if (object instanceof PySequence pySeq) {
			return IntStream.range(0, pySeq.__len__())
					.mapToObj(pySeq::__finditem__)
					.map(ScannableUtils::convertToJava)
					.toArray();
		}

		return null;
	}

	/**
	 * @param scannables
	 * @return list of Names from the provided list of scannables/detectors
	 */
	public static List<String> getScannableNames(List<? extends Scannable> scannables) {
		return scannables.stream().map(Scannable::getName).toList();
	}

	/**
	 * @param scannables
	 * @return list of InputNames from the provided list of scannables
	 */
	public static List<String> getScannableInputFieldNames(List<Scannable> scannables) {
		return scannables.stream().map(Scannable::getInputNames).flatMap(Arrays::stream).toList();
	}

	/**
	 * @param scannables
	 * @return list of extraNames from the provided list of scannables
	 */
	public static List<String> getScannableExtraFieldNames(List<Scannable> scannables) {
		return scannables.stream().map(Scannable::getExtraNames).flatMap(Arrays::stream).toList();
	}

	/**
	 * @param scannables
	 * @return list of input and extraNames from the provided list of scannables
	 */
	public static List<String> getScannableFieldNames(List<Scannable> scannables) {
		return scannables.stream().map(ScannableUtils::getScannableFieldNames).flatMap(List::stream).toList();
	}

	public static final List<String> getScannableFieldNames(Scannable scannable) {
		if (scannable instanceof Detector det) {
			return getDetectorFieldNames(det);
		}
		return Stream.concat(Arrays.stream(scannable.getInputNames()), Arrays.stream(scannable.getExtraNames())).toList();
	}

	/**
	 * @param detectors
	 * @return list of names (extra names in case of CounterTimer) from the provided list of detectors
	 */
	public static final List<String> getDetectorFieldNames(List<Detector> detectors) {
		return detectors.stream().flatMap(ScannableUtils::getDetectorFieldNamesStream).toList();
	}

	/**
	 * @param detector
	 * @return list of names for the detector (extra names in case of CounterTimer, otherwise just the detector name)
	 */
	public static final List<String> getDetectorFieldNames(Detector detector) {
		return getDetectorFieldNamesStream(detector).toList();
	}

	private static final Stream<String> getDetectorFieldNamesStream(Detector detector) {
		final String[] extraNames = detector.getExtraNames();
		return extraNames.length == 0 ? Stream.of(detector.getName()) : Arrays.stream(extraNames);
	}

	/**
	 * Call to get the position with unit as a string. The units, if any, will be added on without a space.
	 *
	 * @param name
	 * @return String position
	 * @throws DeviceException
	 */
	public static String getScannablePosition(String name) throws DeviceException {
		final Scannable s = (Scannable) Finder.find(name);

		// If there is a getUnit method we try that
		String unit = null;
		try {
			final Method getUnit = s.getClass().getMethod("getUnit");
			unit = getUnit.invoke(s).toString();
		} catch (Exception ignored) {
			// Not an error
		}

		if (unit != null) {
			try {
				Object userUnit = s.getAttribute(ScannableMotionUnits.USERUNITS);
				if (userUnit != null) {
					unit = userUnit.toString();
				}

			} catch (Exception ignored) {
				// Scannables are allowed to have no units
				unit = null;
			}
		}

		return unit != null ? s.getPosition().toString() + unit : s.getPosition().toString();
	}

	/**
	 * Returns of the scannable is busy if it can. Otherwise returns false without an exception being thrown.
	 *
	 * @param scannableName
	 * @return true if busy
	 */
	public static boolean isScannableBusy(String scannableName) {
		final Scannable s = (Scannable) Finder.find(scannableName);
		try {
			return s.isBusy();
		} catch (DeviceException e) {
			return false;
		}
	}

	/**
	 * NOTE: Instead of giving an exception of the EpicsMonitor is not in a valid state, this methods returns the
	 * message from the exception.
	 *
	 * @param name
	 * @return value or exception message or null.
	 */
	public static String getScannablePositionNoException(final String name) {
		try {
			return getScannablePosition(name);
		} catch (Exception ne) {
			return name + " has no value.";
		}
	}

	/**
	 * Returns an array of strings describing the formatting information for the output names of this scannable. I.e. it
	 * strips off the formatting strings for the input names.
	 * <p>
	 * This is useful for adding detector data formatting information into the ScanDataPoint
	 *
	 * @param scannable
	 * @return String[]
	 */
	public static String[] getExtraNamesFormats(Scannable scannable) {
		final int numInputNames = (scannable.getInputNames() != null) ? scannable.getInputNames().length : 0;
		final int numExtraNames = (scannable.getExtraNames() != null) ? scannable.getExtraNames().length : 0;
		final int numInputAndExtraNames = numInputNames + numExtraNames;

		String[] outputFormat = new String[] {};
		if (numInputAndExtraNames > 0) {
			// outputFormat should contain one format for each input name and each extra name
			outputFormat = scannable.getOutputFormat();
			if (numInputNames > 0) {
				if (outputFormat.length < numInputAndExtraNames)
					throw new IllegalStateException(
							String.format(
									"Scannable %s has %d input name(s) and %d extra name(s); it should have %d (%d+%d) output format(s), but has %d",
									scannable.getName(), numInputNames, numExtraNames, numInputAndExtraNames,
									numInputNames, numExtraNames, outputFormat.length));
				// Remove the leading output formats that correspond to the input names,
				// leaving just those corresponding to the extra names
				outputFormat = Arrays.copyOfRange(outputFormat, numInputNames, outputFormat.length);

			}
		}
		return outputFormat;
	}

	private static final int PADDING_WIDTH = 15;

	public static String prettyPrintScannable(Scannable theScannable) {
		return prettyPrintScannable(theScannable, PADDING_WIDTH);
	}

	public static String prettyPrintScannable(Scannable obj, int paddingSize) {
		// If we are passed a scannable group handle it separately
		if (obj instanceof IScannableGroup scannableGroup) {
			return prettyPrintScannableGroup(scannableGroup);
		}

		String formattedString = obj.toFormattedString();

		int index = formattedString.indexOf(":");
		if (index == -1) {
			return formattedString;
		}

		String output = StringUtils.rightPad(formattedString.substring(0, index), paddingSize);
		output += formattedString.substring(index, formattedString.length());
		return output;
	}

	public static String prettyPrintScannableGroup(IScannableGroup args) {
		return args.toFormattedString();
	}

	/**
	 * Serializes and encodes into Base64 a snapshot of a scannable
	 * @param scn
	 * @return Base64 encoded ScannableSnapshot
	 * @throws Exception
	 */
	public static String getSerializedScannableSnapshot(Scannable scn) throws Exception {
		// We don't want to serialize the whole scannable - only the relevant information about it
		// Don't want to send information we don't care about (references to motors etc)
		ScannableSnapshot si = new ScannableSnapshot(scn);
		ByteArrayOutputStream bstream = new ByteArrayOutputStream();
		ObjectOutputStream ostream = new ObjectOutputStream(bstream);
		ostream.writeObject(si);
		ostream.close();
		return Base64.getEncoder().encodeToString(bstream.toByteArray());
	}

	/**
	 * Deserializes a Base64 encoded ScannableSnapshot
	 * @param serialized
	 * @return Deserialized snapshot
	 * @throws Exception
	 */
	public static ScannableSnapshot deserializeScannableSnapshot(String serialized) throws Exception {
		ByteArrayInputStream bstream = new ByteArrayInputStream(Base64.getDecoder().decode(serialized));
		ObjectInputStream ostream = new ObjectInputStream(bstream);
		return (ScannableSnapshot) ostream.readObject();
	}

	/**
	 * Check to see if scannable has any units to return. If not, return null.
	 * Does not work with IScannableGroup because it returns an array of strings where as this
	 * only returns a single string Use {@link #getScannableUnitsArray(Scannable ...)} instead.
	 * @param scannable
	 * @return units
	 */
	public static String getScannableUnits(Scannable scannable) {
		if (scannable instanceof ScannableMotionUnits smu) {
			return smu.getUserUnits();
		} else if (scannable instanceof Monitor monitor) {
			try {
				return monitor.getUnit();
			} catch (DeviceException e) {
				logger.error("{}: Could not get units for scannable", scannable.getName(), e);
			}
		}
		return null;
	}

	/**
	 * Return an array of scannable units which map on to the scannable field names.
	 * @throw DeviceException
	 * @param scannables
	 * @return units
	 */
	public static String[] getScannableUnitsArray(Scannable ... scannables) throws DeviceException {
		final List<String> totalScannableFieldNames = getScannableFieldNames(Arrays.asList(scannables));
		final String[] totalUnits = new String[totalScannableFieldNames.size()];
		for (final Scannable scannable : scannables) {
			final List<String> scannableFieldNames = getScannableFieldNames(scannable);
			//Map units to the scannable field names.
			final String[] scannableUnits = (scannable instanceof IScannableGroup scannableGroup) ?
				scannableGroup.getUnits() :
				Collections.nCopies(scannableFieldNames.size(), getScannableUnits(scannable)).toArray(String[]::new);
			//Sanity check that scannable group returns correct size of units.
			if (scannableUnits.length != scannableFieldNames.size()) {
				throw new IllegalStateException("Scannable " + scannable.getName() + " scannableUnits.length = " + scannableUnits.length + ", scannableFieldNames.size() = " + scannableFieldNames.size() + ". They must be the same size!");
			}
			//Loop through scannable field names and add mapped scannable unit to the correct index of the total units.
			for (int i = 0; i < scannableFieldNames.size(); i++) {
				final String fieldName = scannableFieldNames.get(i);
				final int fieldNameIndex = totalScannableFieldNames.indexOf(fieldName);
				totalUnits[fieldNameIndex] = scannableUnits[i];
			}
		}
		return totalUnits;
	}
}
