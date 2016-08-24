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

import java.util.Arrays;
import java.util.List;

import gda.device.DeviceException;
import gda.device.EnumPositionerStatus;
import gda.device.scannable.ScannablePositionChangeEvent;
import gda.device.scannable.corba.impl.ScannableAdapter;
import gda.device.scannable.corba.impl.ScannableImpl;
import gda.factory.corba.util.CorbaAdapterClass;
import gda.factory.corba.util.CorbaImplClass;

/**
 * A dummy fast shutter. The status of the shutter (as read using {@link #getPosition()}) is always "Open" or "Closed",
 * but the position can be set to one of three values: "Open", "Close" or "AUTO". A real fast shutter in "AUTO" mode
 * will open and close as the gonio rotates past preset values, but this dummy shutter doesn't support this.
 */
@CorbaImplClass(ScannableImpl.class)
@CorbaAdapterClass(ScannableAdapter.class)
public class DummyFastShutter extends EnumPositionerBase {

	private boolean shutterOpen;

	private static final List<String> VALID_POSITIONS = Arrays.asList(new String[] {"Open", "Close", "AUTO"});

	@Override
	public Object getPosition() throws DeviceException {
		return shutterOpen ? "Open" : "Closed";
	}

	@Override
	public String checkPositionValid(Object illDefinedPosObject) {
		if (VALID_POSITIONS.contains(illDefinedPosObject)) {
			return null;
		}

		return illDefinedPosObject.toString() + "not a valid position string";

	}

	@Override
	public void asynchronousMoveTo(Object position) throws DeviceException {
		throwExceptionIfInvalidTarget(position);

		if (position.equals("Open")) {
			shutterOpen = true;
		} else if (position.equals("Close")) {
			shutterOpen = false;
		}
		notifyIObservers(this, new ScannablePositionChangeEvent(position.toString()));
	}

	@Override
	public boolean isBusy() throws DeviceException {
		return false;
	}

	@Override
	public String[] getPositions() throws DeviceException {
		return VALID_POSITIONS.toArray(new String[3]);
	}

	@Override
	public EnumPositionerStatus getStatus() throws DeviceException {
		return EnumPositionerStatus.IDLE;
	}

}
