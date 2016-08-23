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

package gda.device.enumpositioner;

import java.util.ArrayList;
import java.util.Vector;

import gda.device.DeviceException;
import gda.device.scannable.IPolarimeterPinholeEnumPositioner;
import gda.epics.connection.EpicsChannelManager;
import gda.epics.connection.EpicsController;
import gda.epics.connection.InitializationListener;
import gda.epics.xml.EpicsRecord;
import gda.factory.FactoryException;
import gda.factory.Finder;
import gov.aps.jca.Channel;

/**
 * A class implementing the EnumPositioner for a combined pinhole/flux monitor
 * present on multilayer polarimeter end station.
 */
public class PolarimeterPinholeEnumPositioner extends EpicsPositioner implements
		InitializationListener, IPolarimeterPinholeEnumPositioner {

	protected String fluxMonitorChannelLabel = null;
	protected Vector<String> positionValues = new Vector<String>();
	protected String templateName;
	private int numberPinholes;
	protected Vector<String> labels = new Vector<String>();
	String valStrings[] = { "VALA", "VALB", "VALC", "VALD", "VALE", "VALF",
			"VALG", "VALH", "VALI", "VALJ", "VALK", "VALL" };
	String setStrings[] = { "SETA", "SETB", "SETC", "SETD", "SETE", "SETF",
			"SETG", "SETH", "SETI", "SETJ", "SETK", "SETL" };
	private static int MAXPOSITIONS = 12;
	private Channel setChannel;
	private EpicsChannelManager channelManager;

	@Override
	public void configure() throws FactoryException {
		super.configure();
		channelManager = new EpicsChannelManager(this);
		controller = EpicsController.getInstance();
		// Overide existing positions with values from server xml file
		for (int i = 0; i < ((numberPinholes * 2)); i++) {
			try {
				setPositionValue(positions.get(i), positionValues.get(i));
			} catch (Exception e) {
				if( e instanceof RuntimeException)
					throw (RuntimeException)e;
				throw new FactoryException(getName() + " exception in configure",e);
			}
		}
	}

	/**
	 * Returns the physcal motor position for the supplied position string Note
	 * cannot read them directly from the EPICS positioner
	 *
	 * @param position
	 * @return monitorLabel
	 */
	@Override
	public Double getPositionValue(String position) {
		Double value = 0.0;

		for (int i = 0; i < MAXPOSITIONS; i++) {
			if (positions.get(i).equals(position)) {
				return Double.valueOf(positionValues.get(i));
			}
		}

		return value;
	}

	/**
	 * lect Sets the physical motor value for the supplied position string
	 *
	 * @param position
	 * @param value
	 * @throws DeviceException
	 */
	@Override
	public void setPositionValue(String position, String value) throws DeviceException {

		EpicsRecord epicsRecord = (EpicsRecord) Finder.getInstance().find(
				getEpicsRecordName());
		String pChannelName = epicsRecord.getFullRecordName();
		pChannelName = pChannelName.substring(0, pChannelName.lastIndexOf(":"))
				+ ":P:";

		for (int i = 0; i < ((numberPinholes * 2)); i++) {
			if (positions.get(i).equals(position)) {
				// Need to create a channel for the :P.VAL value, set it then
				// destroy it
				try {
					setChannel = channelManager.createChannel(pChannelName
							+ valStrings[i], false);
					Thread.sleep(100);
					controller.caput(setChannel, value, channelManager);
					controller.destroy(setChannel);
				} catch (Exception e) {
					if( e instanceof RuntimeException)
						throw (RuntimeException)e;
					throw new DeviceException(getName() + " exception in setPositionValue",e);
				}
			}
		}
	}

	/**
	 * Returns detector channel label for flux monitoring.
	 *
	 * @return monitorLabel
	 */
	@Override
	public String getFluxMonitorChannelLabel() {
		return this.fluxMonitorChannelLabel;
	}

	/**
	 * Returns detector channel label for flux monitoring.
	 *
	 * @param monitorLabel
	 */
	@Override
	public void setFluxMonitorChannelLabel(String monitorLabel) {
		this.fluxMonitorChannelLabel = monitorLabel;
	}

	/**
	 * Returns number of piholes
	 *
	 * @return numberPinholes
	 */
	@Override
	public int getNumPinholes() {
		return this.numberPinholes;
	}

	/**
	 * Returns detector channel label for flux monitoring.
	 *
	 * @param numPinholes
	 */
	@Override
	public void setNumPinholes(int numPinholes) {
		this.numberPinholes = numPinholes;
	}

	/**
	 * Add a possible position to the list of positions.
	 *
	 * @param position
	 */
	@Override
	public void addPosition(String position) {
		if (!positions.contains(position)) {
			positions.add(position);
		}
	}

	/**
	 * Add a physical position value to the list of values.
	 *
	 * @param value
	 */
	@Override
	public void addValue(String value) {
		if (!positionValues.contains(value)) {
			positionValues.add(value);
		}
	}

	/**
	 * Sets the values for this positioner.
	 *
	 * @param values the values
	 */
	@Override
	public void setValues(Vector<String> values) {
		this.positionValues = new Vector<String>();
		for (String value : values) {
			addValue(value);
		}
	}

	/**
	 * values
	 *
	 * @return ArrayList<String> the values this device can move to.
	 */
	@Override
	public ArrayList<String> getValueArrayList() {
		ArrayList<String> array = new ArrayList<String>();

		for (String value : positionValues) {
			array.add(value);
		}
		return array;
	}

	/**
	 * positions
	 *
	 * @return ArrayList<String> the positions this device can move to.
	 */
	@Override
	public ArrayList<String> getPositionArrayList() {
		ArrayList<String> array = new ArrayList<String>();
		// Returns empty array as positions are picked up from the EPICS
		// database
		return array;
	}

	/**
	 * Add a possible label to the list of labels.
	 *
	 * @param label
	 */
	@Override
	public void addLabel(String label) {
		if (!labels.contains(label)) {
			labels.add(label);
		}
	}

	/**
	 * Sets the list of labels for this positioner.
	 *
	 * @param labels the labels
	 */
	@Override
	public void setLabels(Vector<String> labels) {
		this.labels = new Vector<String>();
		for (String label : labels) {
			addLabel(label);
		}
	}

	/**
	 * @return ArrayList<String> the labels to display which this device can
	 *         move to.
	 */
	@Override
	public ArrayList<String> getLabelArrayList() {
		ArrayList<String> array = new ArrayList<String>();

		for (String label : labels) {
			array.add(label);
		}
		return array;
	}

}
