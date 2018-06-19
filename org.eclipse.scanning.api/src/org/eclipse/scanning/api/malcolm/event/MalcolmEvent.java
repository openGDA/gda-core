/*-
 *******************************************************************************
 * Copyright (c) 2011, 2016 Diamond Light Source Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthew Gerring - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.scanning.api.malcolm.event;

import static org.eclipse.scanning.api.malcolm.event.MalcolmEvent.MalcolmEventType.STATE_CHANGED;

import java.util.EventObject;

import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.malcolm.IMalcolmDevice;


/**
 * Abstract superclass of events from a malcolm device.
 *
 */
public abstract class MalcolmEvent extends EventObject {

	public enum MalcolmEventType {
		STATE_CHANGED; //, STEPS_PERFORMED - TODO add in future change
	}

	// General Information
	private final String message;
	private MalcolmEventType type;

	protected MalcolmEvent(IMalcolmDevice<?> malcolmDevice, MalcolmEventType type, String message) {
		super(malcolmDevice);
		this.message = message;
		this.type = type;
	}

	public MalcolmEvent copy() {
		if (this.getEventType() == MalcolmEventType.STATE_CHANGED) {
			return new MalcolmStateChangedEvent((MalcolmStateChangedEvent) this);
		}

		throw new AssertionError("Unknown event type " + getEventType());
	}

	public static MalcolmStateChangedEvent forStateChange(IMalcolmDevice<?> malcolmDevice,
			DeviceState deviceState, DeviceState prevState, String message) {
		return new MalcolmStateChangedEvent(malcolmDevice, deviceState, prevState, message);
	}

	public IMalcolmDevice<?> getMalcolmDevice() {
		return (IMalcolmDevice<?>) getSource();
	}

	public String getMalcolmDeviceName() {
		return getMalcolmDevice().getName();
	}

	public MalcolmEventType getEventType() {
		return type;
	}

	public String getMessage() {
		return message;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((message == null) ? 0 : message.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MalcolmEvent other = (MalcolmEvent) obj;
		if (message == null) {
			if (other.message != null)
				return false;
		} else if (!message.equals(other.message))
			return false;
		if (type != other.type)
			return false;
		return true;
	}

}
