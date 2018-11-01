/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package gda.device.evaporator;

import gda.device.DeviceException;

/**
 * Interface providing device specific control of an Evaporator device to GDA.
 * <br/>
 * Instances should not be used in GDA directly but wrapped in an {@link Evaporator}
 * instead.
 */
public interface EvaporatorController {
	void setEnabled(boolean enabled) throws DeviceException;
	boolean isEnabled() throws DeviceException;

	void setRemote(boolean remote) throws DeviceException;
	boolean isRemote() throws DeviceException;

	void setHighVoltage(double hv) throws DeviceException;
	double getHighVoltage() throws DeviceException;

	void setHighVoltageEnabled(boolean enable) throws DeviceException;
	boolean isHighVoltageEnabled() throws DeviceException;

	void setShutter(String shutterPosition) throws DeviceException;
	String getShutter() throws DeviceException;
	String[] getShutterPositions() throws DeviceException;

	int getNumberOfPockets();
	EvaporatorPocket getPocket(int pocket);
	void clearError() throws DeviceException;
}
