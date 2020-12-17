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

package gda.device.enumpositioner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.python.core.PyString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.EnumPositioner;
import gda.device.EnumPositionerStatus;
import gda.device.scannable.ScannableBase;

/**
 * Base class for the EnumPositioner interface
 */
public abstract class EnumPositionerBase extends ScannableBase implements EnumPositioner {

	private static final Logger logger = LoggerFactory.getLogger(EnumPositionerBase.class);

	private List<String> positions = new ArrayList<>();
	private volatile EnumPositionerStatus positionerStatus = EnumPositionerStatus.IDLE;

	/**
	 * sets the OutputFormat
	 */
	public EnumPositionerBase() {
		this.setOutputFormat(new String[] { "%s" });
	}

	@Override
	public String[] getPositions() throws DeviceException {
		return getPositionsInternal();
	}

	private synchronized String[] getPositionsInternal(){
		return positions.toArray(new String[positions.size()]);
	}

	@Override
	public List<String> getPositionsList() {
		return getPositionsListInternal();
	}

	private synchronized List<String> getPositionsListInternal() {
		return new ArrayList<>(positions);
	}

	protected synchronized String getPosition(int index) {
		return positions.get(index);
	}

	protected synchronized boolean containsPosition(String position) {
		return positions.contains(position);
	}

	protected synchronized int getPositionIndex(String position) {
		return positions.indexOf(position);
	}

	protected synchronized int getNumberOfPositions() {
		return positions.size();
	}

	protected synchronized void clearPositions() {
		positions.clear();
	}

	// Some subclasses currently set their own positions
	// We may wish to review this in future.
	protected synchronized void setPositionsInternal(Collection<String> positions) {
		this.positions.clear();
		this.positions.addAll(positions);
		this.notifyIObservers(this, positions);
	}

	protected synchronized void addPosition(String position) {
		positions.add(position);
	}

	protected synchronized void addPositions(Collection<String> positions) {
		this.positions.addAll(positions);
	}

	/**
	 * Set the positioner status
	 * <p>
	 * Ideally, this function should be protected, but this causes runtime exceptions because of the interaction of
	 * three factors:
	 * <ul>
	 * <li>Some of this class's subclasses reside in a different plugin (uk.ac.gda.epics)</li>
	 * <li>The run-time package of a class or interface is determined by the package name AND its class loader (see
	 * https://docs.oracle.com/javase/specs/jvms/se11/html/jvms-5.html#jvms-5.3-300)</li>
	 * <li>In OSGi each plugin has a different class loader.</li>
	 * </ul>
	 * So a subclass in gda.epics, with a different class loader, trying to call this function in a package-private way
	 * (from an inner class within the subclass) will fail with a runtime exception.<br>
	 * See DAQ-1598 and linked tickets, and also https://bugs.eclipse.org/bugs/show_bug.cgi?id=152568
	 * <p>
	 * TODO: Make protected if/when we have a solution the above-mentioned issue.
	 *
	 * @param positionerStatus
	 *            The new value to set
	 */
	public void setPositionerStatus(EnumPositionerStatus positionerStatus) {
		this.positionerStatus = positionerStatus;
	}

	/**
	 * Get the current positioner status<br>
	 * This should also be made protected: see comment on {@link #setPositionerStatus(EnumPositionerStatus)}
	 *
	 * @return Current status
	 */
	public EnumPositionerStatus getPositionerStatus() {
		return positionerStatus;
	}

	@Override
	public EnumPositionerStatus getStatus() throws DeviceException {
		return getPositionerStatus();
	}

	@Override
	public boolean isBusy() throws DeviceException {
		try {
			return this.getStatus() == EnumPositionerStatus.MOVING;
		} catch (DeviceException e) {
			logger.warn("{}: error fetching status", getName(), e);
			return false;
		}
	}

	@Override
	public String checkPositionValid(Object position) {
		if (position instanceof String || position instanceof PyString) {
			return null;
		}
		return "position not a string";
	}

	/**
	 * Simple implementation that may not be reliable.<br>
	 * Devices with a PV that indicates whether the device is in position should use this instead.
	 */
	@Override
	public boolean isInPos() throws DeviceException {
		logger.debug("Default isInPos() called");
		return getPositionerStatus() == EnumPositionerStatus.IDLE;
	}

	@Override
	public String toFormattedString() {
		try {
			// get the current position as an array of doubles
			final Object position = getPosition();

			// if position is null then simply return the name
			if (position == null) {
				logger.warn("getPosition() from {} returns NULL.", getName());
				return valueUnavailableString();
			} else {
				final String rr = createFormattedListAcceptablePositions();
				return String.format("%s : %s %s", getName(), position, rr);
			}
		} catch (Exception e) {
			logger.warn("{} : exception while getting position", getName(), e);
			return valueUnavailableString();
		}
	}

	/**
	 * Formats the available positions into a list e.g ('pos1' 'pos2' 'pos3')
	 *
	 * @return formatted positions list
	 */
	protected String createFormattedListAcceptablePositions() {
		return getPositionsList().stream().collect(Collectors.joining("' '", "('", "')"));
	}

}
