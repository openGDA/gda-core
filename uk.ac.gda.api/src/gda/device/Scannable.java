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

import gda.jython.accesscontrol.MethodAccessProtected;

/**
 * An interface operated primarily by scans and the "pos" command..
 * <p>
 * The interface has a series of standard methods which need to be fulfilled by any object operating within a scan.
 */
public interface Scannable extends Device {

	Object DEFAULT_INPUT_NAME = "value";

	/**
	 * Returns the current position of the Scannable. Called by ConcurentScan at the end of the point. 
	 * 
	 * @return Current position with an element for each input and extra field. null if their are no fields.
	 * @throws DeviceException
	 */
	public Object getPosition() throws DeviceException;

	/**
	 * Pretty print version of getPosition output. This may include the name of the object and any units it uses. If you
	 * require simply the position as a string, use getPosition().toString()
	 * 
	 * @return string
	 */
	@Override
	public String toString();

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
	 * Called for every Scannable at the end of a group of nested scans (or a single scan if that is the case)
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
	 * Hook to be used by commands moving the scannable if the command fails. Used by pos and scan. Useful for telling
	 * scannables which hold state during a scan for example, to reset themselves. Used for example by
	 * CoordinatedMotionScannables. This hook should be used not in the same way as the stop hook.
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
	 * 
	 * @return string
	 */
	public String toFormattedString();

}
