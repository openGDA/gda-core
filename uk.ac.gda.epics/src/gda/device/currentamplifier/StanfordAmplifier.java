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
import gda.device.Scannable;

/**
 * Interface used for Stanford current amplifier - used by
 * {@link StanfordScannable} and {@link DummyStanfordScannable}.
 *
 * @since 12/10/2016
 */
public interface StanfordAmplifier extends Scannable, CurrentAmplifier {

	public int getSensitivity() throws DeviceException;

	public int getSensitivityUnit() throws DeviceException;

	public boolean isOffsetCurrentOn() throws DeviceException;

	public void setOffsetCurrentOn(boolean switchOn) throws DeviceException;

	public int getOffset() throws DeviceException;

	public int getOffsetUnit() throws DeviceException;

	public void setSensitivity(int sensitivity) throws DeviceException;

	public void setSensitivityUnit(int unit) throws DeviceException;

	public void setOffset(int offset) throws DeviceException;

	public void setOffsetUnit(int unit) throws DeviceException;

	public String[] getOffsetUnits();

	@Override
	public String[] getGainUnits();

	public String[] getAllowedPositions();
}
