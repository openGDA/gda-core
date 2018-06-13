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

package org.eclipse.scanning.api.malcolm.event;

import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.malcolm.IMalcolmDevice;

public final class MalcolmStateChangedEvent extends MalcolmEvent {

	private final DeviceState deviceState;
	private final DeviceState previousState;

	protected MalcolmStateChangedEvent(IMalcolmDevice<?> malcolmDevice, DeviceState deviceState, DeviceState prevState, String message) {
		super(malcolmDevice, MalcolmEventType.STATE_CHANGED, message);
		this.deviceState = deviceState;
		this.previousState = prevState;
	}

	protected MalcolmStateChangedEvent(MalcolmStateChangedEvent event) {
		super(event.getMalcolmDevice(), MalcolmEventType.STATE_CHANGED, event.getMessage());
		this.deviceState = event.deviceState;
		this.previousState = event.previousState;
	}

	public DeviceState getDeviceState() {
		return deviceState;
	}

	public DeviceState getPreviousState() {
		return previousState;
	}

	@Override
	public String toString() {
		return "MalcolmStateChangedEvent [deviceState=" + deviceState + ", previousState=" + previousState
				+ ", getMalcolmDeviceName()=" + getMalcolmDeviceName() + ", getMessage()=" + getMessage() + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((deviceState == null) ? 0 : deviceState.hashCode());
		result = prime * result + ((previousState == null) ? 0 : previousState.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		MalcolmStateChangedEvent other = (MalcolmStateChangedEvent) obj;
		if (deviceState != other.deviceState)
			return false;
		if (previousState != other.previousState)
			return false;
		return true;
	}

}
