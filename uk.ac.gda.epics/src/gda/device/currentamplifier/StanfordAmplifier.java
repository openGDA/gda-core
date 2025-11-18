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

package gda.device.currentamplifier;

import gda.device.CurrentAmplifier;
import gda.device.DeviceException;

/**
 * Interface used for Stanford current amplifier - used by
 * {@link StanfordScannable} and {@link DummyStanfordScannable}.
 *
 * @since 12/10/2016
 */
public interface StanfordAmplifier extends CurrentAmplifier {

	int getSensitivity() throws DeviceException;

	int getSensitivityUnit() throws DeviceException;

	boolean isOffsetCurrentOn() throws DeviceException;

	void setOffsetCurrentOn(boolean switchOn) throws DeviceException;

	int getOffset() throws DeviceException;

	int getOffsetUnit() throws DeviceException;

	void setSensitivity(int sensitivity) throws DeviceException;

	void setSensitivityUnit(int unit) throws DeviceException;

	void setOffset(int offset) throws DeviceException;

	void setOffsetUnit(int unit) throws DeviceException;

	String[] getOffsetUnits();

	@Override
	String[] getGainUnits();

	String[] getAllowedPositions();
}
