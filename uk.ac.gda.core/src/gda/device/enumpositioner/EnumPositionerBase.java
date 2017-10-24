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

import gda.device.DeviceException;
import gda.device.EnumPositioner;
import gda.device.EnumPositionerStatus;
import gda.device.Scannable;
import gda.device.scannable.ScannableBase;

import java.util.Arrays;
import java.util.Vector;

import org.python.core.PyException;
import org.python.core.PyString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for the EnumPositioner interface
 */
public abstract class EnumPositionerBase extends ScannableBase implements EnumPositioner, Scannable {

	private static final Logger logger = LoggerFactory.getLogger(EnumPositionerBase.class);

	protected Vector<String> positions = new Vector<String>();
	public volatile EnumPositionerStatus positionerStatus = EnumPositionerStatus.IDLE;
	protected String name;
	/**
	 * sets the OutputFormat
	 */
	public EnumPositionerBase() {
		this.setOutputFormat(new String[] { "%s" });
	}

	@Override
	public String[] getPositions() throws DeviceException{
		String[] array = new String[positions.size()];
		return positions.toArray(array);
	}

	/**
	 * Sets the positions of this positioner.
	 *
	 * @param positions
	 *            the positions
	 */
	public void setPositions(String[] positions) {
		this.positions = new Vector<String>(Arrays.asList(positions));
		this.notifyIObservers(this, positions);
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

	@Override
	public String toFormattedString() {
		try {

			// get the current position as an array of doubles
			Object position = getPosition();

			// if position is null then simply return the name
			if (position == null) {
				logger.warn("getPosition() from " + getName() + " returns NULL.");
				return getName() + " : Unknown";
			}
			String rr = createFormattedListAcceptablePositions();

			return getName() + " : " + position.toString() + " " + rr;

		} catch (PyException e) {
			logger.info(getName() + ": jython exception while getting position. " + e.toString());
			return getName() + " : NOT AVAILABLE";
		} catch (Exception e) {
			logger.info("{}: exception while getting position. ", getName(), e);
			return getName() + " : NOT AVAILABLE";
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
