/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.gasrig.api;

import java.util.List;
import java.util.Map;

import gda.device.DeviceException;
import gda.factory.Findable;
import gda.observable.IObservable;

public interface IGasRig extends Findable, IObservable {

	/**
	 * Gets all the gases the aren't assigned to a cabinet
	 *
	 * @return The Gases
	 */
	public List<? extends IGas> getNonCabinetGases();

	/**
	 * Gets all the cabinets that have been configured
	 * @return The cabinets
	 */
	public List<? extends ICabinet> getCabinets();

	/**
	 * Gets the gas mix object for a specific line
	 *
	 * @param lineNumber
	 * @return The gas mix for the specified line
	 * @throws GasRigException If that line number does not exist
	 */
	public IGasMix getGasMix(int lineNumber) throws GasRigException;

	/**
	 * Gets a Map where the keys are the line numbers, and the values
	 * are the gasmixes for those line numbers
	 *
	 * @return The map of gas mixes
	 */
	public Map<Integer, ? extends IGasMix> getGasMixes();

	/**
	 * Executes the dummy sequence on the PLC
	 *
	 * @throws GasRigException
	 */
	public void runDummySequence() throws GasRigException;

	/**
	 * Asks EPICS to pump out the endstation
	 *
	 * @throws GasRigException
	 */
	public void evacuateEndStation() throws GasRigException;

	/**
	 * Asks EPICS to evacute the specified line
	 *
	 * @param lineNumber
	 * @throws GasRigException
	 */
	public void evacuateLine(int lineNumber) throws GasRigException;

	/**
	 * Admits the specified line to the endstation
	 *
	 * @param lineNumber
	 * @throws GasRigException
	 */
	public void admitLineToEndStation(int lineNumber) throws GasRigException;

	/**
	 * Evacuates line 1 and then listens for callback
	 * to evacuate line 2
	 *
	 * @throws GasRigException
	 */
	void evacuateLines() throws GasRigException;

	/**
	 * Admits the specified gas to the specified line
	 *
	 * @param gasId The gas ID (which matches the MFC number)
	 * @param lineNumber The line to admit it to
	 * @throws GasRigException
	 * @throws DeviceException
	 */
	public void admitGasToLine(int gasId, int lineNumber) throws GasRigException, DeviceException;

	/**
	 * Admits the specified gas to the specified line
	 *
	 * @param gasName The gas name (which matches the gas name in EPICS)
	 * @param lineNumber The line to admit it to
	 * @throws GasRigException
	 * @throws DeviceException
	 */
	public void admitGasToLine(String gasName, int lineNumber) throws GasRigException, DeviceException;

	/**
	 * Configure a mix of gases for the specified line
	 *
	 * @param gasMix The GasMix object
	 * @param lineNumber The line number to configure the mix for
	 * @throws GasRigException
	 * @throws DeviceException
	 */
	public void configureGasMixForLine(IGasMix gasMix, int lineNumber) throws GasRigException, DeviceException;

	/**
	 * Asks EPICS to run the initialisation procedure
	 *
	 * @throws DeviceException
	 */
	public void initialise() throws DeviceException;

	/**
	 * Asks EPICS to admit both lines to endstation
	 *
	 * @throws GasRigException
	 */
	public void admitLinesToEndstation() throws GasRigException;

	/**
	 * Asks EPICS to admit both lines to exhaust
	 *
	 * @throws GasRigException
	 */
	public void admitLinesToExhaust() throws GasRigException;

	/**
	 * Set pressure on butterfly valve
	 *
	 * @param value
	 * @throws DeviceException
	 */
	void setButterflyValvePressure(double value) throws DeviceException;

	/**
	 * Set position on butterfly valve
	 *
	 * @param value
	 * @throws DeviceException
	 */
	void setButterflyValvePosition(double value) throws DeviceException;

	// Non permanent
	public boolean isRemoveLiveControls();

	void settleUnusedGases(IGasMix gasMix1, IGasMix gasMix2) throws GasRigException, DeviceException;
}
