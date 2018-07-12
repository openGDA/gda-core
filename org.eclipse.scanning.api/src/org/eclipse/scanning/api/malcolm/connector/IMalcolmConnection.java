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

package org.eclipse.scanning.api.malcolm.connector;

import org.eclipse.scanning.api.malcolm.IMalcolmDevice;
import org.eclipse.scanning.api.malcolm.MalcolmDeviceException;
import org.eclipse.scanning.api.malcolm.message.MalcolmMessage;

/**
 * An instance of this interface represent a connection to a malcolm service and this
 * interface provides methods to communicate with malcolm devices. Note that a single
 * instance of this service is used to communicate with all malcolm devices on a particular
 * machine.
 */
public interface IMalcolmConnection {

	/**
	 * A listener to the state of the connection to the actual malcolm device, i.e.
	 * connected vs disconnected.
	 */
	@FunctionalInterface
	public interface IMalcolmConnectionStateListener {

		/**
		 * Called when the connection to the malcolm device connects or disconnects.
		 * @param connected the new state of the connection, where <code>true</code> means
		 *    the the malcolm device is connected, <code>false</code> means that it is disconnected
		 */
		public void connectionStateChanged(boolean connected);

	}

	/**
	 * A listener to malcolm events from the actual malcolm device.
	 */
	@FunctionalInterface
	public interface IMalcolmConnectionEventListener {

		/**
		 * Called when an event has occurred on the actual malcolm device corresponding to
		 * the endpoint for which this listener has been subscribed.
		 * @param message a message describing the change and the current state of the malcolm device
		 */
		public void eventPerformed(MalcolmMessage message);

	}

	/**
	 * Prefix for connection error messages.
	 */
	public static final String ERROR_MESSAGE_PREFIX_FAILED_TO_CONNECT = "Failed to connect to device";

	/**
	 * Returns the message generator.
	 * @return
	 */
	public IMalcolmMessageGenerator getMessageGenerator();

	/**
	 * Closes the connection.
	 *
	 * @throws MalcolmDeviceException
	 */
	public void disconnect() throws MalcolmDeviceException;


	/**
	 * Send the message and get one back, blocking, same as send(device, MalcolmMessage, true)
	 * @param message
	 * @return
	 * @throws MalcolmDeviceException
	 */
	public MalcolmMessage send(IMalcolmDevice<?> device, MalcolmMessage message) throws MalcolmDeviceException;


	/**
	 * Subscribe to a message, adding the listener to the list of listeners for this message
	 * @param message
	 * @return
	 * @throws MalcolmDeviceException
	 */
	public void subscribe(IMalcolmDevice<?> device, MalcolmMessage message, IMalcolmConnectionEventListener listener) throws MalcolmDeviceException;


	/**
	 * Unsubscribe to a message, if listeners is null all listeners will be unsubscribed, otherwise just those specified.
	 * @param message
	 * @return
	 * @throws MalcolmDeviceException
	 */
	public MalcolmMessage unsubscribe(IMalcolmDevice<?> device, MalcolmMessage message, IMalcolmConnectionEventListener... listeners) throws MalcolmDeviceException;


	/**
	 * Listens to connection state changes on the device, notifying the specified listener of any change.
	 * This method blocks with no timeout until the connection is made, therefore this method should be
	 * called in a different thread.
	 *
	 * @param device the device to listen to
	 * @param listener the listener to be notified of changes
	 * @throws MalcolmDeviceException
	 */
	public void subscribeToConnectionStateChange(IMalcolmDevice<?> device, IMalcolmConnectionStateListener listener)
			throws MalcolmDeviceException;

}
