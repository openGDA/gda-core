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
	public volatile EnumPositionerStatus positionerStatus = EnumPositionerStatus.IDLE;
	protected String name;
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

	// Some superclasses currently set their own positions
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
		positions.addAll(positions);
	}

	@Override
	public EnumPositionerStatus getStatus() throws DeviceException {
		return positionerStatus;
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
		return positionerStatus == EnumPositionerStatus.IDLE;
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

	protected String createFormattedListAcceptablePositions() throws DeviceException {
		String[] posLables = getPositions();
		String rr = "(";
		for (String s : posLables) {
			rr += "'" + s + "' ";
		}
		rr = rr.trim() + ")";
		return rr;
	}

}
