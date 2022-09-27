/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

import java.util.List;

import gda.device.DeviceException;
import gda.device.EnumPositioner;
import gda.device.EnumPositionerStatus;
import uk.ac.gda.api.remoting.ServiceInterface;

@ServiceInterface(EnumPositioner.class)
public class JythonEnumPositionerWrapper extends JythonScannableWrapper implements EnumPositioner {

	@Override
	public String[] getPositions() throws DeviceException {
		return getPositioner().getPositions();
	}

	@Override
	public List<String> getPositionsList() throws DeviceException {
		return getPositioner().getPositionsList();
	}

	@Override
	public EnumPositionerStatus getStatus() throws DeviceException {
		return getPositioner().getStatus();
	}

	@Override
	public boolean isInPos() throws DeviceException {
		return getPositioner().isInPos();
	}

	private EnumPositioner getPositioner() {
		return (EnumPositioner) getScannable().orElseThrow();
	}

}
