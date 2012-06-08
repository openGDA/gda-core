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

package gda.device.scannable;

import gda.device.DeviceException;

import java.io.Serializable;

/**
 * A bare bones scannable that places no limitation on the type of the position object
 */
public class SimpleScannable extends ScannableBase {

	private Object currentPosition;

	/**
	 * Constructor
	 */
	public SimpleScannable() {
	}

	/**
	 * @param currentPosition  Used at instantiation only. From then use asynchronousMoveTo
	 * @throws DeviceException 
	 */
	public void setCurrentPosition(Object currentPosition)  throws DeviceException {
		asynchronousMoveTo(currentPosition);
	}

	@Override
	public void asynchronousMoveTo(Object position) throws DeviceException {
		if(currentPosition == null || !currentPosition.equals(position)){
			currentPosition = position;
			notifyIObservers(this, new ScannablePositionChangeEvent((Serializable)currentPosition));
		}
		
	}

	@Override
	public Object getPosition() throws DeviceException {
		return currentPosition;
	}

	@Override
	public boolean isBusy() throws DeviceException {
		return false;
	}
}
