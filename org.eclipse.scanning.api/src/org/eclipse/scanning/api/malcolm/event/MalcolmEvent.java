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

import java.util.EventObject;

import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.malcolm.IMalcolmDevice;


/**
 * This bean is used to disseminate messages about what has happened
 * to the scan while it is being written.
 *
 * Do not extend this class to allow arbitrary information to be sent.
 * The event encapsulated by this bean should be sending just the information
 * defined here, metadata that cannot circumvent the nexus file.
 *
 * For instance adding a dynamic set of information, a map perhaps, would
 * allow information which should be saved in the Nexus file to circumvent
 * the file and be set in the event. It was decided in various meetings
 * that doing this could mean that some data is not recorded as it should be
 * in nexus.
 *
 * @author Matthew Gerring
 *
 */
public final class MalcolmEvent extends EventObject {

	// General Information
	private String deviceName;
	private double percentComplete;
	private String message;

	// State information
	private DeviceState deviceState;
	private DeviceState previousState;

	public MalcolmEvent(IMalcolmDevice<?> malcolmDevice) {
		super(malcolmDevice);
	}

	public MalcolmEvent(IMalcolmDevice<?> malcolmDevice, DeviceState state) {
		super(malcolmDevice);
		this.deviceState = state;
	}

	public MalcolmEvent(IMalcolmDevice<?> malcolmDevice, DeviceState state, String message) {
		this(malcolmDevice, state);
		this.message = message;
	}

	public MalcolmEvent(MalcolmEvent bean) {
		super(bean.getMalcolmDevice());
		this.deviceName = bean.deviceName;
		this.percentComplete = bean.percentComplete;
		this.message = bean.message;
		this.deviceState = bean.deviceState;
		this.previousState = bean.previousState;
	}

	public IMalcolmDevice<?> getMalcolmDevice() {
		return (IMalcolmDevice<?>) getSource();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((deviceName == null) ? 0 : deviceName.hashCode());
		result = prime * result + ((message == null) ? 0 : message.hashCode());
		long temp;
		temp = Double.doubleToLongBits(percentComplete);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((previousState == null) ? 0 : previousState.hashCode());
		result = prime * result + ((deviceState == null) ? 0 : deviceState.hashCode());
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
		if (deviceName == null) {
			if (other.deviceName != null)
				return false;
		} else if (!deviceName.equals(other.deviceName))
			return false;
		if (message == null) {
			if (other.message != null)
				return false;
		} else if (!message.equals(other.message))
			return false;
		if (Double.doubleToLongBits(percentComplete) != Double.doubleToLongBits(other.percentComplete))
			return false;
		if (previousState != other.previousState)
			return false;
		if (deviceState != other.deviceState)
			return false;
		return true;
	}

	public String getDeviceName() {
		return deviceName;
	}

	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	public double getPercentComplete() {
		return percentComplete;
	}

	public void setPercentComplete(double percentComplete) {
		this.percentComplete = percentComplete;
	}

	public DeviceState getDeviceState() {
		return deviceState;
	}

	public void setDeviceState(DeviceState state) {
		this.deviceState = state;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public String toString() {
		return "MalcolmEventBean [deviceName="
				+ deviceName + ", percentComplete="
				+ percentComplete + ", message=" + message + ", state=" + deviceState
				+ ", previousState=" + previousState;
	}

	public DeviceState getPreviousState() {
		return previousState;
	}

	public void setPreviousState(DeviceState previousState) {
		this.previousState = previousState;
	}

	public boolean isScanStart() {
		return getDeviceState()==DeviceState.RUNNING && getDeviceState()!=getPreviousState();
	}

	public boolean isScanEnd() {
		return getDeviceState()!=getPreviousState() && getPreviousState()== DeviceState.RUNNING;
	}
}
