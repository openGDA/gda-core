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

package uk.ac.diamond.daq.gasrig;

import gda.device.DeviceException;
import gda.observable.IObservable;
import uk.ac.diamond.daq.gasrig.api.GasRigException;

public interface IGasRigController extends IObservable {

	public String getGasName(int gasId) throws DeviceException, GasRigException;

	public double getMaximumMassFlow(int gasId) throws DeviceException, GasRigException;

	public void runDummySequence() throws DeviceException;

	public void evacuateEndStation() throws DeviceException;

	public void evacuateLine(int lineNumber) throws DeviceException;

	void admitLineToEndStation(int lineNumber) throws DeviceException;

	void admitGasToLine(String gasName, int lineNumber) throws DeviceException;

	void setMassFlow(int gasId, double massFlow) throws DeviceException, GasRigException;

	void initialise() throws DeviceException;

	public void closeLineValvesForGas(int gasId) throws DeviceException, GasRigException;

	void admitLinesToEndStation() throws DeviceException;

	void admitLinesToExhaust() throws DeviceException;

	boolean isGasFlowingToLine(int gasId, int lineNumber) throws DeviceException, GasRigException;

	boolean isLineFlowingToEndstation(int lineNumber) throws DeviceException;
}
