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

import static java.lang.String.format;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.python.core.Py;
import org.python.core.PyArray;
import org.python.core.PyException;
import org.python.core.PyFloat;
import org.python.core.PyInteger;
import org.python.core.PyList;
import org.python.core.PyLong;
import org.python.core.PyObject;
import org.python.core.PySlice;
import org.python.core.PyString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.device.Device;
import gda.device.DeviceBase;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.factory.FactoryException;
import gda.jython.accesscontrol.MethodAccessProtected;

/**
 * A base implementation for a {@link Scannable} {@link Device}.
 * <p>
 * Routes calls to asynchronousMoveTo through an externalToInternal method to rawAsynchronousMoveTo; and, vice-versa, calls to
 * getPosition through an internalToExternal method from rawGetPosition. The protected methods externalArrayToInternal
 * and internalArraytoExternal should be overridden to provide automated support for things such as offsets and unit
 * conversion. By default they do nothing.
 */
public abstract class ScannableBase extends DeviceBase implements Scannable {

	private static final Logger logger = LoggerFactory.getLogger(ScannableBase.class);

	private static final long pollTimeMillis = LocalProperties.getAsInt(LocalProperties.GDA_SCANNABLEBASE_POLLTIME, 100);

	public static final String DEFAULT_INPUT_NAME = "value";

	/**
	 * This is the Jython documentation. Use it in the GDA Jython via the help command.
	 */
	public static String __doc__ = "This is the user documentation for ScannableBase. If you see this then documentation for the specific class is missing.";

	/**
	 * This performs a series of tests to check that the internal setup of the Scannable is consistent. It does not and
	 * should not operate the scannable by calling getPosition().
	 * <p>
	 * It will set the internal arrays of strings to defaults to prevent NPE where appropriate.
	 *
	 * @param scannable
	 * @throws DeviceException
	 */
	public static void validateScannable(Scannable scannable) throws DeviceException {

		if (scannable.getName() == null) {
			throw new DeviceException("getName() returns null");
		}

		//create sensible defaults if set to null
		if (scannable.getExtraNames() == null) {
			scannable.setExtraNames(new String[0]);
		}
		if (scannable.getInputNames() == null) {
			scannable.setInputNames(new String[0]);
		}
		if (scannable.getOutputFormat() == null) {
			int formatLength = scannable.getInputNames().length + scannable.getExtraNames().length;
			String[] newFormats = new String[formatLength];
			for (int i = 0; i < formatLength; i++) {
				newFormats[i] = "%5.1g";
			}
			scannable.setOutputFormat(newFormats);
		}

		// if only one format string specified but more needed
		if (scannable.getOutputFormat().length == 1 && scannable.getExtraNames().length + scannable.getInputNames().length > 1) {
			String format = scannable.getOutputFormat()[0];
			int formatLength = scannable.getInputNames().length + scannable.getExtraNames().length;
			String[] newFormats = new String[formatLength];
			for (int i = 0; i < formatLength; i++) {
				newFormats[i] = format;
			}
			scannable.setOutputFormat(newFormats);
		}

		//check final array lengths are consistent
		int length_diff = scannable.getInputNames().length + scannable.getExtraNames().length - scannable.getOutputFormat().length;
		if (length_diff != 0) {
			String message = scannable.getName() +".getOutputFormat().length " +
					( length_diff > 0 ? "<" : ">") + " getInputNames().length + getExtraNames().length";
			message += format("\ninputNames: (%d) %s", scannable.getInputNames().length, Arrays.toString(scannable.getInputNames()));
			message += format("\nextraNames: (%d) %s", scannable.getExtraNames().length, Arrays.toString(scannable.getExtraNames()));
			message += format("\noutputFormat: (%d) %s", scannable.getOutputFormat().length, Arrays.toString(scannable.getOutputFormat()));
			throw new DeviceException(message);
		}
	}

	/**
	 * The move priority for this Scannable
	 */
	protected int level = 5;

	/**
	 * Array names for the positioner elements in this Scannable.
	 */
	protected String[] inputNames = new String[] { DEFAULT_INPUT_NAME };

	/**
	 * Array names for additional readout elements of this Scannable.
	 */
	protected String[] extraNames = new String[0];

	/**
	 * Array of strings which specify the format to output when getting the position of this Scannable.
	 */
	protected String[] outputFormat = new String[] { "%5.5g" };

	/**
	 * Map of scan attribute names to values. Scan attributes are attributes that should be written to the scan output (e.g. NeXus file). Note that some
	 * subclasses may wish to override getScanAttributeValue() to return additional values other than the ones
	 */
	private final Map<String, Object> scanMetadataAttributes = new HashMap<>();

	/**
	 * Empty default implementation for Scannables.
	 * <p>
	 * This implementation is provided so authors of Scannables do not have to write this method - it is not always
	 * required for Scannables.
	 * @throws FactoryException
	 *
	 * @see gda.factory.Configurable#configure()
	 */
	@Override
	public void configure() throws FactoryException {
	}

	/**
	 * {@inheritDoc} Converts the external (user) position to an internal position and passes this to
	 * rawAsynchronousMoveTo.
	 */
	// TODO: make final
	@Override
	public void asynchronousMoveTo(Object externalPosition) throws DeviceException {

		try {
			rawAsynchronousMoveTo(externalToInternal(externalPosition));
		} catch (DeviceException e) {
			throw new DeviceException(format("Problem triggering %s move to %s: ", getName(), externalPosition.toString()) + e.getMessage(), e);
		}
	}

	/**
	 * {@inheritDoc} Reads an internal (hardware) position from rawGetPosition, converts this to an external (user)
	 * position and returns it.
	 */
	// TODO: make final
	@Override
	public Object getPosition() throws DeviceException {
		try {
			return internalToExternal(rawGetPosition());
		}
		catch (Exception e) {
			throw new DeviceException("exception in " + getName() + ".getPosition()", e);
		}
	}

	/**
	 * [Consider abstract] Trigger a move/operation to an internal/hardware position and return immediately.
	 *
	 * @see gda.device.Scannable#asynchronousMoveTo(java.lang.Object) asynchronousMoveTo
	 * @param position
	 *            Position in its internal/hardware representation. e.g. with units and offsets removed
	 * @throws DeviceException
	 */
	// TODO: make abstract when people are prepared to rewrite Jython scannables.
	@SuppressWarnings("unused")
	public void rawAsynchronousMoveTo(Object position) throws DeviceException {
		// Suppress warnings as an inheriting class may throw DeviceExceptions
		throw new RuntimeException("The scannable " + getName() + " must override rawAsynchronousMoveTo.");
	}

	/**
	 * [Consider abstract] Read the position in its internal (user) representation.
	 *
	 * @return the value represented by this Scannable
	 * @throws DeviceException
	 * @see gda.device.Scannable#getPosition()
	 */
	// TODO: make abstract when people are prepared to rewrite Jython scannables.
	@SuppressWarnings("unused")
	public Object rawGetPosition() throws DeviceException {
		// Suppress warnings as an inheriting class may throw DeviceExceptions
		throw new RuntimeException("The scannable " + getName() + " must override rawGetPosition.");

	}

	/**
	 * Converts a position in its external representation to its internal representation.
	 * <p>
	 * Verifies that the object (represented as an array) has the same number of fields as the scannable has input
	 * fields, or the total number of fields of the scannable. The later allows scripts to provide the output of
	 * getPosition to moveTo methods, e.g. "pos scannable_with_extra_fields scannable_with_extra_fields()".
	 *
	 * @param externalPosition
	 *            an object array if the Scannable has multiple input fields, otherwise an object. The length must match
	 *            the number of the Scannables input fields.
	 * @return an object array if the Scannable has multiple input fields, otherwise an object. The length will match
	 *         the number of the Scannables input fields.
	 */
	public Object externalToInternal(final Object externalPosition) {
		if (externalPosition == null) {
			return null;
		}
		checkPositionLength(PositionConvertorFunctions.length(externalPosition));
		return externalPosition;
	}

	/**
	 * Converts a position in its internal representation to its external representation.
	 * <p>
	 * Verifies that the object (represented as an array) has the same number of fields as the scannable.
	 *
	 * @param internalPosition
	 * @return an object array if the scannable has multiple fields, otherwise an object.
	 */
	public Object internalToExternal(final Object internalPosition) {
		if (internalPosition == null) {
			return null;
		}
		checkPositionLength(PositionConvertorFunctions.length(internalPosition));
		return internalPosition;
	}

	final private void checkPositionLength(int length) {
		final int numInputNames = getInputNames().length;
		final int numExtraNames = getExtraNames().length;
		final int numTotalNames = numInputNames + numExtraNames;

		if (numExtraNames == 0) {
			if (length != numInputNames) {
				throw new IllegalArgumentException(
					String.format("Expected position of length %d but got position of length %d on scannable %s",
						numInputNames,
						length,
						getName()));
			}
		}

		else {
			if ((length != numInputNames) && (length != numTotalNames)) {
				throw new IllegalArgumentException(
					String.format("Expected position of length %d or %d but got position of length %d on scannable %s" ,
						numInputNames,
						numTotalNames,
						length,
						getName()));
			}
		}
	}

//	/**
//	 * Converts and array of objects representing an external position, to an array of objects representing an internal
//	 * position. Called by {@link ScannableBase#externalToInternal(Object)}.
//	 *
//	 * @param externalPositionArray
//	 * @return internal position array
//	 */
//	protected Object[] externalArrayToInternal(Object[] externalPositionArray) {
//		return externalPositionArray;
//	}
//
//	/**
//	 * Converts and array of objects representing an internal position, to an array of objects representing an external
//	 * position. Called by {@link ScannableBase#internalToExternal(Object)}.
//	 *
//	 * @param internalPositionArray
//	 * @return external position array
//	 */
//	protected Object[] internalArraytoExternal(Object[] internalPositionArray) {
//		return internalPositionArray;
//	}

	/**
	 * Replaced by atScanGroupStart Default behaviour is to do nothing. Inheriting classes have the option to implement
	 * this if their specific behaviour requires it.
	 *
	 * @throws DeviceException
	 * @deprecated
	 */
	@Override
	@Deprecated
	public void atStart() throws DeviceException {
		this.atScanLineStart();
	}

	/**
	 * {@inheritDoc} Default behaviour is to do nothing. Inheriting classes have the option to implement this if their
	 * specific behaviour requires it.
	 *
	 * @see gda.device.Scannable#atEnd()
	 */
	@Override
	@Deprecated
	public void atEnd() throws DeviceException {
		this.atScanLineEnd();
	}

	/**
	 * {@inheritDoc} Default behaviour is to do nothing. Inheriting classes have the option to implement this if their
	 * specific behaviour requires it.
	 *
	 * @see gda.device.Scannable#atPointEnd()
	 */
	@Override
	public void atPointEnd() throws DeviceException {
	}

	/**
	 * {@inheritDoc} Default behaviour is to do nothing. Inheriting classes have the option to implement this if their
	 * specific behaviour requires it.
	 *
	 * @see gda.device.Scannable#atPointStart()
	 */
	@Override
	public void atPointStart() throws DeviceException {
	}

	/**
	 * {@inheritDoc} Default behaviour is to do nothing. Inheriting classes have the option to implement this if their
	 * specific behaviour requires it.
	 *
	 * @see gda.device.Scannable#atScanLineEnd()
	 */
	@Override
	public void atScanLineEnd() throws DeviceException {

	}

	/**
	 * {@inheritDoc} Default behaviour is to do nothing. Inheriting classes have the option to implement this if their
	 * specific behaviour requires it.
	 *
	 * @see gda.device.Scannable#atScanEnd()
	 */
	@Override
	public void atScanEnd() throws DeviceException {
	}

	/**
	 * {@inheritDoc} Default behaviour is to do nothing. Inheriting classes have the option to implement this if their
	 * specific behaviour requires it.
	 *
	 * @see gda.device.Scannable#atLevelStart()
	 */
	@Override
	public void atLevelStart() throws DeviceException {
	}

	/**
	 * {@inheritDoc} Default behaviour is to do nothing. Inheriting classes have the option to implement this if their
	 * specific behaviour requires it.
	 *
	 * @see gda.device.Scannable#atLevelMoveStart()
	 */
	@Override
	public void atLevelMoveStart() throws DeviceException {
	}

	/**
	 * {@inheritDoc} Default behaviour is to do nothing. Inheriting classes have the option to implement this if their
	 * specific behaviour requires it.
	 *
	 * @see gda.device.Scannable#atLevelEnd()
	 */
	@Override
	public void atLevelEnd() throws DeviceException {
	}

	/**
	 * {@inheritDoc} Default behaviour is to do nothing. Inheriting classes have the option to implement this if their
	 * specific behaviour requires it.
	 *
	 * @see gda.device.Scannable#atCommandFailure()
	 */
	@Override
	public void atCommandFailure() throws DeviceException {
	}

	/**
	 * {@inheritDoc} Default behaviour is to do nothing. Inheriting classes have the option to implement this if their
	 * specific behaviour requires it.
	 *
	 * @see gda.device.Scannable#atScanStart()
	 */
	@Override
	public void atScanStart() throws DeviceException {
	}

	/**
	 * {@inheritDoc} Default behaviour is to do nothing. Inheriting classes have the option to implement this if their
	 * specific behaviour requires it.
	 *
	 * @see gda.device.Scannable#atScanLineStart()
	 */
	@Override
	public void atScanLineStart() throws DeviceException {
	}

	@Override
	public String[] getExtraNames() {
		return this.extraNames;
	}

	@Override
	public String[] getInputNames() {
		return this.inputNames;
	}

	@Override
	public int getLevel() {
		return this.level;
	}

	@Override
	public String[] getOutputFormat() {
		return this.outputFormat;
	}
	/**
	 * {@inheritDoc} If this is overridden, asynchronousMoveTo must also be valid, and the externalToInternal conversion must be applied.
	 */
	@Override
	public void moveTo(Object position) throws DeviceException {
		try {
			this.asynchronousMoveTo(position);
			this.waitWhileBusy();
		} catch (Exception e) {
			// convert to a device exception
			throw new DeviceException(String.format("Move failed for %s.moveTo(%s)\n\t%s", getName(), position, e.getMessage()), e);
		}
	}

	@Override
	public void setExtraNames(String[] names) {
		this.extraNames = names;
	}

	@Override
	public void setInputNames(String[] names) {
		this.inputNames = names;
	}

	@Override
	public void setLevel(int level) {
		this.level = level;
	}

	@Override
	public void setOutputFormat(String[] names) {
		outputFormat = names;
	}

	/**
	 * {@inheritDoc} Default behaviour is to do nothing. Inheriting classes have the option to implement this if their
	 * specific behaviour requires it.
	 *
	 * @see gda.device.Scannable#stop()
	 */
	@Override
	public void stop() throws DeviceException {
	}


	@Override
	public String toString() {
		return getName() + "<" + this.getClass().toString() + ">";
	}

	@Override
	public String toFormattedString() {
		try {
			return ScannableUtils.getFormattedCurrentPosition(this);
		} catch (Exception e) {
			throw new RuntimeException("Exception in " + getName() + ".toFormattedString()", e);
		}
	}
	/**
	 * {@inheritDoc} If this is to be overriden, isBusy must also be valid. Although the pos and scan command currently
	 * use this method to determine if the Scannable is busy, this must not be relied upon.
	 */
	@Override
	public void waitWhileBusy() throws DeviceException, InterruptedException {
		while (isBusy()) {
			Thread.sleep(pollTimeMillis);
		}
	}

	/**
	 * Its like waitWhileBusy, has a timeout and will throws a DeviceException if the time limit is reached.
	 *
	 * @param timeoutInSeconds
	 * @throws DeviceException
	 * @throws InterruptedException
	 */
	public void waitWhileBusy(double timeoutInSeconds) throws DeviceException, InterruptedException {
		// TODO should this method go into the interface???
		double timeoutInMilliSeconds = timeoutInSeconds * 1000;
		double timeWaitedInMilliSeconds = 0.0;
		while (timeWaitedInMilliSeconds < timeoutInMilliSeconds && isBusy()) {
			Thread.sleep(pollTimeMillis);
			timeWaitedInMilliSeconds += pollTimeMillis;
		}

		if (timeWaitedInMilliSeconds >= timeoutInMilliSeconds) {
			throw new DeviceException("Timeout moving scannable '" + getName() + "' after " + timeoutInSeconds + "s");
		}
	}

	/**
	 * This default behaviour should be extended by most subclasses! {@inheritDoc}
	 * @throws DeviceException
	 *
	 * @see gda.device.Scannable#checkPositionValid(java.lang.Object)
	 */
	@Override
	public String checkPositionValid(Object illDefinedPosObject) throws DeviceException {
		return null;
	}

	protected void throwExceptionIfInvalidTarget(Object position) throws DeviceException {
		String reason = checkPositionValid(position);
		if (reason != null){
			throw new DeviceException("position unacceptable: " + reason);
		}
	}

	/**
	 * {@inheritDoc} If positionToTest is a string (as will be the case for valves or pneumatics for example), this is
	 * compared to the value obtained from getPosition(). An exception is thrown if this is not also a string.
	 * <p>
	 * Otherwise if positionToTest is not a string the object is compared to the value from getPosition(), after having
	 * first pushed both values through objectToArray() from ScannableUtils.
	 * <p>
	 * This behaviour should be extended where possible, and has been in ScannableMotionBase
	 *
	 * @see gda.device.Scannable#isAt(java.lang.Object)
	 */
	@Override
	public boolean isAt(Object positionToTest) throws DeviceException {
		// if string
		if (positionToTest instanceof String) {
				Object pos = this.getPosition();
				return positionToTest.equals(pos);
		}

		// implicit else: positionToTest is not instanceof String
		Double[] posToTestAsArray = ScannableUtils.objectToArray(positionToTest);
		Double[] posAsArray = ScannableUtils.objectToArray(this.getPosition());
		posAsArray = java.util.Arrays.copyOf(posAsArray, posToTestAsArray.length);
		return java.util.Arrays.equals(posToTestAsArray, posAsArray);


	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Sets the entry for this attribute name in the map. Subclasses may override.
	 */
	@Override
	public void setScanMetadataAttribute(String attributeName, Object value) throws DeviceException {
		scanMetadataAttributes.put(attributeName, value);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Sets the entry for this attribute name in the map. Subclasses may override.
	 */
	@Override
	public Object getScanMetadataAttribute(String attributeName) throws DeviceException {
		Object value = scanMetadataAttributes.get(attributeName);
		if (value == null) {
			logger.debug(getName() + ".getScanMetadataAttribute - unable to get value for " + attributeName);
		}

		return value;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Sets the entry for this attribute name in the map. Subclasses may override.
	 */
	@Override
	public Set<String> getScanMetadataAttributeNames() throws DeviceException {
		return scanMetadataAttributes.keySet();
	}

	// methods which are not operated by scans, but make interaction with
	// these objects in the Jython environment much easier

	/**
	 * @see org.python.core.PyObject#__call__()
	 * @return the position of this object as a native or array of natives
	 */
	public PyObject __call__() throws DeviceException {

		Object position = getPosition();

		// if no input or extra names
		if (position == null) {
			return null;
		}

		// convert internal value into a PyObject
		if (position instanceof Double) {
			return new PyFloat((Double) position);
		} else if (position instanceof Integer) {
			return new PyInteger((Integer) position);
		} else if (position instanceof Short) {
			return new PyInteger((Short) position);
		} else if (position instanceof Float) {
			return new PyFloat((Float) position);
		} else if (position instanceof Long) {
			return new PyLong((Long) position);
		} else if (position instanceof String) {
			return new PyString((String) position);
		} else if (position instanceof PyObject) {

			return (PyObject) (position);
		}
		// if its an array, return an array of floats or strings
		else if (position.getClass().isArray()) {
			try {
				if (position instanceof String[]) {
					double[] currentPosition = ScannableUtils.positionToArray(position, this);
					PyArray pycurrentPosition = new PyArray(PyString.class, currentPosition.length);

					for (double element : currentPosition) {
						pycurrentPosition.__add__(new PyFloat(element));
					}

					return pycurrentPosition;

				}
				// else
				double[] currentPosition = ScannableUtils.positionToArray(position, this);
				PyArray pycurrentPosition = new PyArray(PyFloat.class, currentPosition.length);

				for (int i = 0; i < currentPosition.length; i++) {
					pycurrentPosition.__setitem__(i, new PyFloat(currentPosition[i]));
				}

				return pycurrentPosition;
			} catch (Exception e) {
				logger.info(getName() + ": exception while converting array of positions to a string. "
						+ e.getMessage());
				return this.__str__();
			}
		}
		// at the very least, create a String and return that
		else {
			return this.__str__();
		}
	}

	/**
	 * Moves the scannable to new_position when a scannable(new_position) call is made from Python
	 *
	 * @see org.python.core.PyObject#__call__(org.python.core.PyObject)
	 * @param new_position
	 * @return a message explaining what happened
	 */
	@MethodAccessProtected(isProtected = true)
	public PyObject __call__(PyObject new_position) throws DeviceException {
		moveTo(new_position);
		return new PyString("Move complete: " + ScannableUtils.getFormattedCurrentPosition(this));
	}

	/**
	 * @return the size of input names. This is intentional as slicing should only work over input parameters. (A design
	 *         request)
	 */
	public int __len__() {
		return getInputNames().length;
	}

	// methods to allow interaction with Matrices
	/**
	 * @param index
	 *            a number or a PySlice object
	 * @return the part of the objects array of position as defined by index
	 */
	public PyObject __getitem__(PyObject index) throws PyException {
		double[] currentPosition;
		try {
			currentPosition = ScannableUtils.getCurrentPositionArray(this);
		} catch (Exception e) {
			logger.info(getName() + ": exception while converting array of positions to a string. " + e.getMessage());
			PyException ex = new PyException();
			ex.value = new PyString("could not convert positions array");
			ex.type = Py.TypeError;
			throw ex;
		}

		if (index instanceof PyInteger) {
			final PyInteger pyIntValue = (PyInteger) index;
			final int intIndex = pyIntValue.getValue();
			if (intIndex >= 0 && intIndex < __len__()) {
				return new PyFloat(currentPosition[intIndex]);
			}
			PyException ex = new PyException();
			ex.value = new PyString( String.format("index out of range: %d", intIndex) );
			ex.type = Py.IndexError;
			throw ex;
		} else if (index instanceof PySlice) {
			// only react if the command was [0] or [:]
			PySlice slice = (PySlice) index;

			int start, stop, step;

			// start
			if (slice.start == null || slice.start.equals(Py.None)) {
				start = 0;
			} else {
				start = Math.max(((PyInteger) slice.start).getValue(), 0);
			}

			// stop
			if (slice.stop == null || slice.stop.equals(Py.None)) {
				stop = this.__len__() - 1;
			} else {
				stop = Math.min(((PyInteger) slice.stop).getValue(), this.__len__() - 1);
			}

			// step
			if (slice.step == null || slice.step.equals(Py.None)) {
				step = 1;
			} else {
				step = ((PyInteger) slice.step).getValue();
			}

			PyList output = new PyList();
			for (int i = start; i <= stop; i += step) {
				output.append(new PyFloat(currentPosition[i]));
			}

			return output;
		}
		PyException ex = new PyException();
		ex.value = new PyString("__getitem()__ parameter was not PyInteger or PySlice");
		ex.type = Py.TypeError;
		throw ex;
	}

	/**
	 * Jython method to return string description of the object
	 *
	 * @return the result of the toString method
	 */
	public PyString __str__() {
		return new PyString(toFormattedString());
	}

	/**
	 * Jython method to return a string representation of the object
	 *
	 * @return the result of the toString method
	 */
	public PyString __repr__() {
		return __str__();
	}

	/**
	 * @return PyString -the name of the object
	 */
	public PyString __doc__() {
		return new PyString(getName());
	}
}
