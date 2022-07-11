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

package gda.device.enumpositioner;

import gda.device.DeviceException;
import gda.device.EnumPositionerStatus;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.DBR_Enum;
import gov.aps.jca.event.MonitorEvent;

public class EpicsLocum4Controller extends EpicsCurrAmpQuadController {

	private volatile int targetPositionIndex;

	@Override
	protected void monitorRange(MonitorEvent mev) {
		short value = -1;
		DBR dbr = mev.getDBR();
		if (dbr.isENUM()) {
			value = ((DBR_Enum) dbr).getEnumValue()[0];
		}
		if (value == targetPositionIndex) {
			synchronized (this) {
				setPositionerStatus(EnumPositionerStatus.IDLE);
			}
		}
	}

	@Override
	public void rawAsynchronousMoveTo(Object position) throws DeviceException {
		// EpicsPositioner moveTo
		// find in the positionNames array the index of the string
		if (containsPosition(position.toString())) {
			targetPositionIndex = getPositionIndex(position.toString());
			setPositionerStatus(EnumPositionerStatus.MOVING);
		}
		super.rawAsynchronousMoveTo(position);
	}
}
