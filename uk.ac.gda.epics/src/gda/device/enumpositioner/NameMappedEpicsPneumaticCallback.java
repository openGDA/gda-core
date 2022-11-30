/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

import java.util.Map;

import gda.device.DeviceException;

/**
 * Version of EpicsPneumaticCallback in which the EPICS names are mapped to different names at the GDA level.
 * <p>
 * For situations where 'Open' and 'Close' are 'too confusing'...
 * <p>
 *
 * @see EpicsSimplePositioner
 */
public class NameMappedEpicsPneumaticCallback extends EpicsPneumaticCallback {

	private Map<String, String> controlValues; // <GDA,EPICS>
	private Map<String, String> statusValues; // <EPICS,GDA>

	/**
	 * @return Returns the values.
	 */
	public Map<String, String> getControlValues() {
		return controlValues;
	}

	/**
	 * Map<String, String> - means <GDA name, EPICS name>
	 *
	 * @param values
	 *            The values to set.
	 */
	public void setControlValues(Map<String, String> values) {
		this.controlValues = values;
		// positions.addAll(values.keySet());
	}

	public Map<String, String> getStatusValues() {
		return statusValues;
	}

	public void setStatusValues(Map<String, String> statusValues) {
		this.statusValues = statusValues;
	}

	@Override
	public String[] getPositions() throws DeviceException {
		String[] array = new String[controlValues.size()];
		return controlValues.keySet().toArray(array);
	}

	@Override
	public String toFormattedString() {
		try {
			return getName() + " : " + getPosition() + " " + createFormattedListAcceptablePositions();
		} catch (DeviceException e) {
			return valueUnavailableString();
		}
	}

	@Override
	public String getPosition() throws DeviceException {
		try {
			return statusValues.get(super.getPosition());
		} catch (Exception e) {
			throw new DeviceException("failed to get position", e);
		}
	}

	@Override
	public void rawAsynchronousMoveTo(Object position) throws DeviceException {

		String positionString = position.toString();

		// find in the positionNames array the index of the string
		if (controlValues.containsKey(positionString)) {
			final String value = controlValues.get(positionString);
			super.rawAsynchronousMoveTo(value);
		} else {
			// if get here then wrong position name supplied
			throw new DeviceException("Position called: " + positionString + " not found.");
		}
	}

	@Override
	public String checkPositionValid(Object position) {
		if (!controlValues.containsKey(position.toString())) {
			return position.toString() + "not in list of acceptable values";
		}
		return null;
	}

}
