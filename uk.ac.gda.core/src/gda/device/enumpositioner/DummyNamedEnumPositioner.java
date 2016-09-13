/*-
 * Copyright © 2011 Diamond Light Source Ltd., Science and Technology
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
import gda.device.NamedEnumPositioner;
import gda.device.scannable.ScannablePositionChangeEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Be able to move to a position whose name is not necessarily the position name
 */
public class DummyNamedEnumPositioner extends DummyEnumPositioner implements NamedEnumPositioner {
	private static final Logger logger = LoggerFactory.getLogger(DummyNamedEnumPositioner.class);


	private String currentPositionName = "";
	private String startPositionName;
	protected HashMap<String, Object> positionsMap = new HashMap<String, Object>();

	@Override
	public void configure() {
		this.inputNames = new String[]{getName()};
		if ((positionsMap.size() > 0) & (startPositionName!= null)) {
			assert(positionsMap.containsKey(startPositionName));
			currentPositionName = startPositionName;
		}
		else if (startPositionName == null) {
			logger.error("Start position name should be defined");
		}
	}


	public ArrayList<String> getPositionNameArrayList() {
		ArrayList<String> positionNamearray = new ArrayList<String>();

		for (String position : positionsMap.keySet()) {
			positionNamearray.add(position);
		}
		return positionNamearray;
	}

	@Override
	public ArrayList<String> getPositionArrayList() {
		ArrayList<String> positionArray = new ArrayList<String>();

		ArrayList<Object> values = (ArrayList<Object>) positionsMap.values();
		for (Object value : values) {
			positionArray.add(value.toString());
		}
		return positionArray;
	}

	public void setValues(Map<String, Object> values) {
		for (Entry<String, Object> entry : values.entrySet()) {
			positionsMap.put(entry.getKey(), entry.getValue());
			positions.add(entry.getKey());
		}
	}

	public void setStartPositionName(String positionName) {
		this.startPositionName = positionName;
	}

	@Override
	public String getPosition() throws DeviceException {
		if (positionsMap.containsKey(currentPositionName)) {
			return positionsMap.get(currentPositionName).toString();
		}
		throw new DeviceException("No position called " + currentPositionName + "found");
	}

	@Override
	public void moveTo(Object position) throws DeviceException {

		String positionString = position.toString();

		// find in the positionNames array the index of the string
		if (positionsMap.containsKey(positionString) ) {
			if( !currentPositionName.equals(positionString)){
				currentPositionName = positionString;
				this.notifyIObservers(this, currentPositionName);
				this.notifyIObservers(this, new ScannablePositionChangeEvent(getPosition()));
			}
			return;
		}
		// if get here then wrong position name supplied
		throw new DeviceException("Position called: " + position + " not found.");

	}

	@Override
	public String getPositionName() {
		return currentPositionName;
	}

	@Override
	public Object getPositionValue() throws DeviceException {
		return getPosition();
	}
}