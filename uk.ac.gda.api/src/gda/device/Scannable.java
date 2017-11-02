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

package gda.device;

import java.util.Collections;
import java.util.Set;

import gda.jython.accesscontrol.MethodAccessProtected;

/**
 * An interface operated primarily by scans and the "pos" command..
 * <p>
 * The interface has a series of standard methods which need to be fulfilled by any object operating within a scan.
 */
public interface Scannable extends Device {

	Object DEFAULT_INPUT_NAME = "value";

	/**
	 * When a {@link Scannable} is used in a GDA9 scan, the value of the scan attribute
	 * (as returned by {@link #getScanMetadataAttribute(String)} is used to determine the
	 * NeXus base class to use for the NeXus object for this scannable.
	 */
	String ATTR_NX_CLASS = "NXclass";

	/**
	 * If {@link #toFormattedString()} cannot get the current value/position of a scannable, it should return this
	 * string.
	 */
	String VALUE_UNAVAILABLE = "UNAVAILABLE";

	/**
	 * Returns the current position of the Scannable. Called by ConcurentScan at the end of the point.
	 *
	 * @return Current position with an element for each input and extra field. null if their are no fields.
	 * @throws DeviceException
	 */
	public Object getPosition() throws DeviceException;

	/**
	 * Trigger a move/operation and block until completion.
	 *
	 * @param position
	 * @throws DeviceException
	 */
	@MethodAccessProtected(isProtected = true)
	public void moveTo(Object position) throws DeviceException;

	/**
	 * Trigger a move/operation and return immediately. Implementations of this method should be non-blocking to allow
	 * concurrent motion; the isBusy method will be used to determine when the move has completed.
	 *
	 * @param position
	 *            Position to move to should have an element for each input field.
	 * @throws DeviceException
	 */
	@MethodAccessProtected(isProtected = true)
	public void asynchronousMoveTo(Object position) throws DeviceException;

	/**
	 * Tests if the given object is meaningful to this Scannable and so could be used by one of the move commands. May
	 * check limits and other things too.
	 *
	 * This is the method called by scans on all points before the scan is run.
	 *
	 * @param position
	 * @return null if position is valid, or returns a description if not.
	 * @throws DeviceException
	 */
	public String checkPositionValid(Object position) throws DeviceException;

	/**
	 * Stop the current move/operation.
	 *
	 * @throws DeviceException
	 */
	public void stop() throws DeviceException;

	/**
	 * Check if the Scannable is moving/operating.
	 *
	 * @return true - if operation carried out by moveTo has not completed yet
	 * @throws DeviceException
	 */
	public boolean isBusy() throws DeviceException;

	/**
	 * Returns when operation carried out by moveTo has completed
	 *
	 * @throws DeviceException
	 * @throws InterruptedException
	 */
	public void waitWhileBusy() throws DeviceException, InterruptedException;

	/**
	 * Tests if the scannable is at (or if appropriate, close to) the value positionToTest.
	 *
	 * @param positionToTest
	 *            The position to compare the scannable's position to.
	 * @return true if scannable is at positionToTest
	 * @throws DeviceException
	 */
	public boolean isAt(Object positionToTest) throws DeviceException;

	/**
	 * Used for ordering the operations of Scannables during scans
	 *
	 * @param level
	 */
	public void setLevel(int level);

	/**
	 * get the operation level of this scannable.
	 *
	 * @return int - the level
	 */
	public int getLevel();

	/**
	 * gets a array of InputNames used by moveTo of this scannable.
	 *
	 * @return array of the names of the elements of the object returned by getPosition
	 */
	public String[] getInputNames();

	/**
	 * sets the array of names returned by getInputNames method of this scannable.
	 *
	 * @param names
	 */
	public void setInputNames(String[] names);

	/**
	 * Additional names for extra values that returned by getPosition().
	 *
	 * @return array of names of the extra elements if the array returned by getPosition is larger than the array
	 *         required by moveTo
	 */
	public String[] getExtraNames();

	/**
	 * Sets the array of names returned by getExtraNames of this scannable.
	 *
	 * @param names
	 */
	public void setExtraNames(String[] names);

	/**
	 * Sets the array of strings describing how best to format the positions from this scannable
	 *
	 * @param names
	 */
	public void setOutputFormat(String[] names);

	/**
	 * Returns an array of strings which are the format strings to use when pretty printing parts of the output
	 *
	 * @return string array
	 */
	public String[] getOutputFormat();

	/**
	 * Replaced by atScanStart
	 *
	 * @throws DeviceException
	 * @deprecated
	 */
	@Deprecated
	public void atStart() throws DeviceException;

	/**
	 * Replaced by atScanEnd
	 *
	 * @throws DeviceException
	 * @deprecated
	 */
	@Deprecated
	public void atEnd() throws DeviceException;

	/**
	 * Called for every Scannable at the start of a group of nested scans (or a single scan if that is the case)
	 *
	 * @throws DeviceException
	 */
	public void atScanStart() throws DeviceException;

	/**
	 * Called for every Scannable at the end of a group of nested scans (or a single scan if that is the case).
	 * <p>
	 * Note that this is only called if the Scan finishes normally, or has been requested to finish early. This will not
	 * be called if the scan finishes due to an exception of any kind.  See {@link #atCommandFailure()}
	 *
	 * @throws DeviceException
	 */
	public void atScanEnd() throws DeviceException;

	/**
	 * Called for every Scannable at the start of every scan
	 *
	 * @throws DeviceException
	 */
	public void atScanLineStart() throws DeviceException;

	/**
	 * Called for every Scannable at the end of every scan
	 *
	 * @throws DeviceException
	 */
	public void atScanLineEnd() throws DeviceException;

	/**
	 * Called on every Scannable at every data point, for scans which are broken down into individual points (i.e.
	 * non-continuous scans)
	 *
	 * @throws DeviceException
	 */
	public void atPointStart() throws DeviceException;

	/**
	 * Called on every Scannable at the end of every data point, for scans which are broken down into individual points
	 * (i.e. non-continous scans)
	 *
	 * @throws DeviceException
	 */
	public void atPointEnd() throws DeviceException;

	/**
	 * Called by both the pos and scan commands at the start of each subsequent level move only on Scannables that will
	 * be moved as part that level's movement.
	 * <p>
	 * For example "pos a 1 b 2 c 3", will, if a and b have the same level and c a higher level will result in:
	 * <blockquote>
	 * <pre>
	 * a.atLevelMoveStart()   <----
	 * b.atLevelMoveStart()   <----
	 * a.asynchronousMoveTo()
	 * b.asynchronousMoveTo()
	 * a.waitWhileBusy()
	 * b.waitWhileBusy()
	 * c.atLevelMoveStart()   <----
	 * c.asynchronousMoveTo()
	 * c.waitWhileBusy()
	 * </pre>
	 * </blockquote>
	 * This hook is used by CoordinatedMotionScannables.
	 *
	 * @throws DeviceException
	 */
	public void atLevelMoveStart() throws DeviceException;

	/**
	 * Called by both the pos and scan commands at the start of each subsequent level move on all Scannables that are
	 * part that level's movement.
	 * <p>
	 * This provides a useful mechanism for e.g. creating a Scannable that opens a shutter after motors have moved but
	 * before a detector is exposed.
	 *
	 * @throws DeviceException
	 */
	public void atLevelStart() throws DeviceException;

	public void atLevelEnd() throws DeviceException;

	/**
	 * Hook to be used by Scan and pos commands to inform the Scannable that an exception, such as a DeviceExcpetion,
	 * has occurred. However not called when the command is aborted using an InterruptionException. If a Scan is aborted
	 * then only {@link #stop()} will be called by the Scan or pos command.
	 * <p>
	 * Useful for telling Scannables which hold state during a scan for example, to reset themselves. Used for example
	 * by CoordinatedMotionScannables. This hook should be used not in the same way as the stop hook.
	 *
	 * @throws DeviceException
	 */
	public void atCommandFailure() throws DeviceException;

	/**
	 * Returns a string representation of the Scannable and its current position/value/status
	 * <p>
	 * Typically should return:
	 * <p>
	 * name : position <units> <limits>
	 * <p>
	 * or for detectors, name : status
	 * <p>
	 * If the position/status cannot be determined, the function should return {@link #VALUE_UNAVAILABLE} in its place.
	 *
	 * @return string as defined above
	 */
	public String toFormattedString();

	/**
	 * Sets the scan attribute with the given name to the given value. A scan attribute is an attribute that should be written into the scan output (e.g. a
	 * NeXus file).
	 * The type of the value can be:<ul>
	 *   <li>a dataset;</li>
	 *   <li>a primitive type supported by datasets;</li>
	 *   <li>an array whose component type that is supported by datasets;</li>
	 *   <li>a list whose element type is supported by datasets.</li>
	 * </ul>
	 *
	 * <p>
	 * <em>Note: this is a temporary mechanism to allow GDA8 devices to work with the new
	 * scanning framework</em>
	 *
	 * @param attributeName
	 *            attribute name
	 * @param value
	 *            value of attribute
	 * @throws DeviceException
	 *             if the attribute could not be set for any reason
	 */
	@SuppressWarnings("unused")
	public default void setScanMetadataAttribute(String attributeName, Object value) throws DeviceException {
		// default implementation: do nothing
	}

	/**
	 * Returns the value of the scan attribute, or <code>null</code> if no such attribute exists (or if the value of the attribute is <code>null</code>)
	 * The type of the value returned can be:<ul>
	 *   <li>a dataset;</li>
	 *   <li>a primitive type supported by datasets;</li>
	 *   <li>an array whose component type that is supported by datasets;</li>
	 *   <li>a list whose element type is supported by datasets.</li>
	 * </ul>
	 *
	 * <p>
	 * <em>Note: this is a temporary mechanism to allow GDA8 devices to work with the new
	 * scanning framework</em>
	 *
	 * @param attributeName
	 *            the name of the attribute
	 * @throws DeviceException
	 *             if the value of the attribute could not be retrieved for any reason
	 */
	@SuppressWarnings("unused")
	public default Object getScanMetadataAttribute(String attributeName) throws DeviceException {
		// default implementation: return null
		return null;
	}

	/**
	 * Returns the name of the scan attributes. These are the attributes that should be written
	 * into the scan output (e.g. a NeXus file).
	 *
	 * <p>
	 * <em>Note: this is a temporary mechanism to allow GDA8 devices to work with the new
	 * scanning framework</em>
	 *
	 * @return names of scan attributes
	 * @throws DeviceException
	 *             if the names of the scan attributes could not be retrieved for any reason
	 */
	@SuppressWarnings("unused")
	public default Set<String> getScanMetadataAttributeNames() throws DeviceException {
		// default implementation: return empty set
		return Collections.emptySet();
	}

}
