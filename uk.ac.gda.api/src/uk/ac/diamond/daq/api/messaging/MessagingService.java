/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.api.messaging;

/**
 * Interface to allow simple objects to be sent as messages for non-GDA applications to listen to.
 *
 * @since GDA 9.12
 * @author James Mudd
 */
public interface MessagingService {

	/**
	 * Sends the provided message. The message object is serialised to a cross-language format and sent to the
	 * destination provided by the {@link Destination} annotation. If the {@link Destination} annotation is not
	 * specified an exception will be thrown.
	 *
	 * @param message
	 *            the message to be sent
	 * @throws IllegalArgumentException
	 *             if the {@link Destination} annotation is not present
	 */
	public void sendMessage(Message message);

	/**
	 * Sends the provided message to the specified destination. The message object is serialised to a cross-language format and sent to the
	 * If the {@link Destination} annotation is present it will be ignored and the message sent to the specified destination.
	 *
	 * @param message
	 *            the message to be sent
	 */
	public void sendMessage(Message message, String destination);
}
