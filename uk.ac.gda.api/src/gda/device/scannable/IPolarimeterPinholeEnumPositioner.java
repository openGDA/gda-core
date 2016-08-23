/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

import java.util.ArrayList;
import java.util.Vector;

import gda.device.DeviceException;
import gda.device.EnumPositionerStatus;
import gda.device.Scannable;
import gda.factory.Configurable;

public interface IPolarimeterPinholeEnumPositioner extends Scannable, Configurable {

	/**
	 * Returns the physcal motor position for the supplied position string Note
	 * cannot read them directly from the EPICS positioner
	 *
	 * @param position
	 * @return monitorLabel
	 */
	Double getPositionValue(String position);

	/**
	 * Sets the physical motor value for the supplied position string
	 *
	 * @param position
	 * @param value
	 * @throws DeviceException
	 */
	void setPositionValue(String position, String value) throws DeviceException;

	/**
	 * @return EnumPositionerStatus
	 * @throws DeviceException
	 */
	@Deprecated
	EnumPositionerStatus getPositionerStatus() throws DeviceException;

	/**
	 * Returns detector channel label for flux monitoring.
	 *
	 * @return monitorLabel
	 */
	String getFluxMonitorChannelLabel();

	/**
	 * Returns detector channel label for flux monitoring.
	 *
	 * @param monitorLabel
	 */
	void setFluxMonitorChannelLabel(String monitorLabel);

	/**
	 * Returns number of piholes
	 *
	 * @return numberPinholes
	 */
	int getNumPinholes();

	/**
	 * Returns detector channel label for flux monitoring.
	 *
	 * @param numPinholes
	 */
	void setNumPinholes(int numPinholes);

	/**
	 * Add a possible position to the list of positions.
	 *
	 * @param position
	 */
	void addPosition(String position);

	/**
	 * Add a physical position value to the list of values.
	 *
	 * @param value
	 */
	void addValue(String value);

	/**
	 * Sets the values for this positioner.
	 *
	 * @param values the values
	 */
	void setValues(Vector<String> values);

	/**
	 * values
	 *
	 * @return ArrayList<String> the values this device can move to.
	 */
	ArrayList<String> getValueArrayList();

	/**
	 * positions
	 *
	 * @return ArrayList<String> the positions this device can move to.
	 */
	ArrayList<String> getPositionArrayList();

	/**
	 * Add a possible label to the list of labels.
	 *
	 * @param label
	 */
	void addLabel(String label);

	/**
	 * Sets the list of labels for this positioner.
	 *
	 * @param labels the labels
	 */
	void setLabels(Vector<String> labels);

	/**
	 * @return ArrayList<String> the labels to display which this device can
	 *         move to.
	 */
	ArrayList<String> getLabelArrayList();

}