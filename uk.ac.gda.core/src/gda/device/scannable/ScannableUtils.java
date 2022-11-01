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
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.measure.Quantity;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.python.core.Py;
import org.python.core.PyException;
import org.python.core.PyFloat;
import org.python.core.PyInteger;
import org.python.core.PyList;
import org.python.core.PyNone;
import org.python.core.PyObject;
import org.python.core.PySequence;
import org.python.core.PyString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
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
		String output = "";
		String[] positionArray = ScannableUtils.getFormattedCurrentPositionArray(scannable);
		if (scannable.getInputNames().length + scannable.getExtraNames().length == 1) {
			output += positionArray[0];
		} else {
			output += "<html><dl>";
			int i = 0;
			for (; i < scannable.getInputNames().length; i++) {
				output += "<dt><b>" + scannable.getInputNames()[i] + "</b></dt><dd>" + positionArray[i] + "</dd>";
			}

			for (int j = 0; j < scannable.getExtraNames().length; j++) {
				output += "<dt><i>" + scannable.getExtraNames()[j] + "</i></dt><dd>" + positionArray[i + j] + "</dd>";
			}
			output += "</dl><html>";
		}
		return output.trim();
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
		String output = scannable.getName() + " : ";
		String[] formatedPositionArray;
		String[] formatedOffsetArray = createFormattedOffsetArray(offsetArray, scannable);
		try {

			formatedPositionArray = getFormattedCurrentPositionArray(position, scannable.getInputNames().length
					+ scannable.getExtraNames().length, scannable.getOutputFormat());
		} catch (Exception e) {
			throw new DeviceException("error formatting position string of '" + scannable.getName() + "'", e);
		}

		// most of the time it's a single number
		if (scannable.getInputNames().length == 1
				&& scannable.getExtraNames().length == 0
				&& (scannable.getName().equals(scannable.getInputNames()[0]) || scannable.getInputNames()[0]
						.equals(Scannable.DEFAULT_INPUT_NAME))) {
			output += formatedPositionArray[0];
			if (unitStringArray != null) {
				output += unitStringArray[0];
			}
			output += formatedOffsetArray[0];
		} else if (scannable.getInputNames().length == 0
				&& scannable.getExtraNames().length == 1
				&& (scannable.getName().equals(scannable.getExtraNames()[0]) || scannable.getExtraNames()[0]
						.equals(Scannable.DEFAULT_INPUT_NAME))) {
			output += formatedPositionArray[0];
			if (unitStringArray != null) {
				output += unitStringArray[0];
			}
			output += formatedOffsetArray[0];
		} else {
			String[] fieldNames = getAllFieldNames(scannable);
			for (int i = 0; i < fieldNames.length; i++) {
				output += fieldNames[i] + ": " + formatedPositionArray[i];
				if (unitStringArray != null) {
					output += unitStringArray[i];
				}
				output += formatedOffsetArray[i];
				output += " ";
			}
		}
		return output.trim();
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
				if (formatedOffsetArray[i] == "unknown") {
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
			final String splitLine[] = line.split("(?<!:):(?!:)", 2);// only split on first colon, ignore double colon
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
				(scannable instanceof Monitor) ? ((Monitor) scannable).getElementCount()
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
			} else if (currentPositionObj instanceof PySequence) {
				currentPosition[i] = Double.parseDouble(((PySequence) currentPositionObj).__finditem__(i).toString());
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
				previousValue = new Double(0);
			}
			Double stepValue = step[i];
			if (stepValue == null) {
				stepValue = new Double(0);
			}

			outputArray[i] = previousValue + stepValue;
		}
		return outputArray;
	}

	private static Double getDouble(Object val, int index) {
		if (val instanceof Number[]) {
			return ((Number[]) val)[index].doubleValue();
		}
		if (val.getClass().isArray()) {
			return Array.getDouble(val, index);
		}
		if (val instanceof PySequence) {
			PyObject item = ((PySequence) val).__finditem__(index);
			if (item instanceof PyNone) {
				return null;
			}
			if (item instanceof PyString) {
				return Double.parseDouble(item.toString());
			}
			return Py.tojava(item, Number.class).doubleValue();
		}
		if (val instanceof List<?>) {
			return Double.parseDouble(((List<?>) val).get(index).toString());
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
		} else if (start instanceof PySequence) {
			numArrayParam = ((PySequence) start).__len__();
		}

		// if position objects are a single value, or if no inputNames
		if (numArgs <= 1 && numArrayParam == 0) {
			Double startValue = Double.valueOf(start.toString());
			Double stopValue = Double.valueOf(stop.toString());
			Double stepValue = Math.abs(Double.valueOf(step.toString()));
			if (stepValue == 0)
				throw new Exception("Step size is zero so number of points cannot be calculated");
			int numSteps = getNumberSteps(startValue, stopValue, stepValue);
			return numSteps;
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
		} else if (position instanceof PySequence) {
			positionSize = ((PySequence) position).__len__();
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
		} else if (value instanceof Number) {
			return ((Number)value).doubleValue();
		} else if (value instanceof String) {
			try {
				return Double.parseDouble((String)value);
			} catch (NumberFormatException e) {
				// either quantity or null - either can be handled
				return objectToDouble(QuantityFactory.createFromString((String)value));
			}
		} else if (value instanceof PyObject) {
			Object pynumber = ((PyObject)value).__tojava__(Double.class);
			if (pynumber instanceof Number) {
				return ((Number)pynumber).doubleValue();
			} // else fallback to string version below
		} else if (value instanceof Quantity<?>) {
			return ((Quantity<?>)value).getValue().doubleValue();
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
		if (position instanceof Double[]) {
			return (Double[]) position;
		} else if (position == null) {
			return new Double[0];
		}

		if (position instanceof Object[]) {
			// Object array
			return Stream.of((Object[])position)
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
		} else if (position instanceof PySequence) {
			// Python Tuple/List etc
			PySequence seq = (PySequence)position;
			return range(0, seq.__len__())
					.mapToObj(seq::__getitem__)
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
	 * Converts a Jython PyObject into its Java equivalent if that is possible. This only works on the sorts of objects
	 * dealt with in the Jython environment i.e. Strings, integers, floats (doubles) and arrays of these.
	 * <P>
	 * If this fails or cannot work for any reason then null is returned.
	 *
	 * @param object
	 * @return Java equivalent object
	 */
	public static Object convertToJava(PyObject object) {

		Object output = null;
		if (object instanceof PyFloat) {
			output = object.__tojava__(Double.class);
		} else if (object instanceof PyInteger) {
			output = object.__tojava__(Integer.class);
		} else if (object instanceof PyString) {
			output = object.__tojava__(String.class);
		} else if (object instanceof PySequence || object instanceof PyList) {
			// create a Java array of PyObjects
			// ArrayList<PyObject> theList = (ArrayList<PyObject>)
			// object.__tojava__(ArrayList.class);

			// loop through and convert each item into its Java equivilent
			output = new Object[0];
			int length;

			if (object instanceof PySequence) {
				length = ((PySequence) object).__len__();
			} else {
				length = ((PyList) object).__len__();
			}
			for (int i = 0; i < length; i++) {

				PyObject item = null;

				if (object instanceof PySequence) {
					item = ((PySequence) object).__finditem__(i);
				} else {
					item = ((PyList) object).__finditem__(i);
				}

				if (item instanceof PyFloat) {
					Double thisItem = (Double) item.__tojava__(Double.class);
					output = ArrayUtils.add((Object[]) output, thisItem);
				} else if (item instanceof PyInteger) {
					Integer thisItem = (Integer) item.__tojava__(Integer.class);
					output = ArrayUtils.add((Object[]) output, thisItem);
				} else if (item instanceof PyString) {
					String thisItem = (String) item.__tojava__(String.class);
					output = ArrayUtils.add((Object[]) output, thisItem);
				}
			}
		}

		if (output == org.python.core.Py.NoConversion) {
			output = null;
		}

		return output;
	}

	/**
	 * @param scannables
	 * @return list of Names from the provided list of scannables/detectors
	 */
	public static List<String> getScannableNames(List<? extends Scannable> scannables) {
		Vector<String> names = new Vector<String>();
		if( scannables !=null){
			for (Scannable s : scannables) {
				names.add(s.getName());
			}
		}
		return names;

	}

	/**
	 * @param scannables
	 * @return list of InputNames from the provided list of scannables
	 */
	public static List<String> getScannableInputFieldNames(List<Scannable> scannables) {
		Vector<String> fieldNames = new Vector<String>();
		for (Scannable s : scannables) {
			fieldNames.addAll(Arrays.asList(s.getInputNames()));
		}
		return fieldNames;
	}

	/**
	 * @param scannables
	 * @return list of ExtraNames from the provided list of scannables
	 */
	public static List<String> getScannableExtraFieldNames(List<Scannable> scannables) {
		Vector<String> fieldNames = new Vector<String>();
		for (Scannable s : scannables) {
			fieldNames.addAll(Arrays.asList(s.getExtraNames()));
		}
		return fieldNames;
	}

	/**
	 * @param scannables
	 * @return list of Input and ExtraNames from the provided list of Scannable
	 */
	public static List<String> getScannableFieldNames(List<Scannable> scannables) {
		Vector<String> fieldNames = new Vector<String>();
		for (Scannable s : scannables) {
			// if detector the inputNames are not returned in ScanDataPoint so do not add
			String[] extraNames = s.getExtraNames();
			if (s instanceof Detector) {
				if (extraNames.length > 0) {
					fieldNames.addAll(Arrays.asList(extraNames));
				} else {
					fieldNames.add(s.getName());
				}
			} else {
				fieldNames.addAll(Arrays.asList(s.getInputNames()));
				fieldNames.addAll(Arrays.asList(extraNames));
			}
		}
		return fieldNames;
	}

	/**
	 * @param Detector
	 * @return list of names ( channel names in case of CounterTimer ) from the provided list of Detector
	 */
	public static List<String> getDetectorFieldNames(List<Detector> Detector) {
		Vector<String> fieldNames = new Vector<String>();
		for (Detector d : Detector) {
			if (d.getExtraNames().length > 0) {
				for (int j = 0; j < d.getExtraNames().length; j++) {
					fieldNames.add(d.getExtraNames()[j]);
				}
			} else {
				fieldNames.add(d.getName());
			}
		}
		return fieldNames;
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

	private final static int paddingWidth = 15;

	public static String prettyPrintScannable(Scannable theScannable) {
		return prettyPrintScannable(theScannable, paddingWidth);
	}

	public static String prettyPrintScannable(Scannable obj, int paddingSize) {
		// If we are passed a scannable group handle it separately
		if (obj instanceof IScannableGroup) {
			return prettyPrintScannableGroup((IScannableGroup) obj);
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
}
