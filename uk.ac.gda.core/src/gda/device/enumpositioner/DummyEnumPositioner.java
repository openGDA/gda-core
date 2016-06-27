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
import java.util.List;
import java.util.Vector;

import gda.device.DeviceException;
import gda.device.EditableEnumPositioner;
import gda.device.EnumPositionerStatus;
import gda.device.Scannable;
import gda.device.scannable.ScannablePositionChangeEvent;

/**
 * A dummy class implementing the EnumPositioner for testing.
 */
public class DummyEnumPositioner extends EnumPositionerBase implements EditableEnumPositioner, Scannable {
	private int currentPositionIndex = 0;

	/**
	 * Constructor.
	 */
	public DummyEnumPositioner() {

	}

	@Override
	public void configure() {
		this.inputNames = new String[]{getName()};
		if (positions.size() > 0) {
			currentPositionIndex = 0;
		}
	}

	/**
	 * Add a possible position to the list of positions.
	 *
	 * @param position
	 */
	public void addPosition(String position) {
		if (!positions.contains(position)) {
			positions.add(position);
		}
	}

	/**
	 * @return List<String> the positions this device can move to.
	 */
	public List<String> getPositionArrayList() {
		ArrayList<String> array = new ArrayList<String>();

		for (String position : positions) {
			array.add(position);
		}
		return array;
	}

	public void setPositions(List<String> positionsArray ) {
		Vector<String> array = new Vector<String>();
		for (String position : positionsArray) {
			array.add(position);
		}
		this.positions = array;
	}

	@Override
	public void rawAsynchronousMoveTo(Object position) throws DeviceException {
		moveTo(position);
	}

	@Override
	public String getPosition() throws DeviceException {
		return positions.get(currentPositionIndex);
	}

	@Override
	public EnumPositionerStatus getStatus() throws DeviceException {
		return EnumPositionerStatus.IDLE;
	}

	@Override
	public void stop() throws DeviceException {
		// do nothing
	}

	@Override
	public void moveTo(Object position) throws DeviceException {

		String positionString = position.toString();

		// find in the positionNames array the index of the string
		if (positions.contains(positionString) ) {
			if( !getPosition().equals(positionString)){
				currentPositionIndex = positions.indexOf(positionString);
				this.notifyIObservers(this, getStatus());
				this.notifyIObservers(this, getPosition());
				this.notifyIObservers(this, new ScannablePositionChangeEvent(getPosition()));
			}
			return;
		}
		// if get here then wrong position name supplied
		throw new DeviceException("Position called: " + position + " not found.");

	}

}
