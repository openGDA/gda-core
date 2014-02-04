/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

package gda.device;

import java.io.Serializable;

/**
 * An Exception class specifically for {@link Device}s.
 */
public class DeviceException extends Exception implements Serializable {
	
	private String deviceName;
	private transient Device device;

	
	/**
	 * Constructs a device exception.
	 * 
	 * @param message
	 *            the exception message to be passed to the receiver
	 */
	public DeviceException(String message) {
		super(message);
	}

	/**
	 * Constructs a device exception with another Throwable as the cause.
	 * 
	 * @param message
	 *            the message for this Exception
	 * @param cause
	 *            the cause (will become the detail message).
	 */
	public DeviceException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructs a new DeviceException with the specified cause and a detail message of
	 * <tt>(cause==null ? null : cause.toString())</tt> (which typically contains the class and detail message of
	 * <tt>cause</tt>). This constructor is useful for exceptions that are little more than wrappers for other
	 * throwables.
	 * 
	 * @param cause
	 *            the cause (which is saved for later retrieval by the {@link #getCause()} method). (A <tt>null</tt>
	 *            value is permitted, and indicates that the cause is nonexistent or unknown.)
	 */
	public DeviceException(Throwable cause) {
		super(cause);
	}
	
	/**
	 * Constructs a DeviceException with a name of the Device which was the cause of the exception.
	 * 
	 * @param deviceName
	 * @param message
	 */
	public DeviceException(String deviceName, String message) {
		this(message);
		this.deviceName = deviceName;
	}

	/**
	 * Constructs a DeviceException with a name of the Device which was the cause of the exception.
	 * 
	 * @param deviceName
	 * @param message
	 * @param cause
	 */
	public DeviceException(String deviceName, String message, Throwable cause) {
		this(message, cause);
		this.deviceName = deviceName;
	}
	
	/**
	 * Constructs a DeviceException with the Device which was the cause of the exception.
	 * <p>
	 * The object reference will be lost if this exception is serialized, but the name will be kept.
	 * 
	 * @param device
	 * @param message
	 */
	public DeviceException(Device device, String message) {
		this(device.getName(),message);
		this.device = device;
	}

	/**
	 * Constructs a DeviceException with a name of the Device which was the cause of the exception.
	 * <p>
	 * The object reference will be lost if this exception is serialized, but the name will be kept.
	 * 
	 * @param device
	 * @param message
	 * @param cause
	 */
	public DeviceException(Device device, String message, Throwable cause) {
		this(device.getName(),message, cause);
		this.device = device;
	}

	/**
	 * May be not necessarily have been set.
	 * 
	 * @return - the Device reference
	 */
	public String getDeviceName() {
		return deviceName;
	}

	/**
	 * Will be null if the exception has been sent over CORBA - use getDeviceName() instead.
	 * 
	 * @return - the Device reference
	 */
	public Device getDevice() {
		return device;
	}

}
