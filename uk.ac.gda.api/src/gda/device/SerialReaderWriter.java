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


/**
 * Class to communicate  with a  Serial device
 */
public interface SerialReaderWriter {
	
	public void close();

	/**
	 * Handles commands which do not need a reply.
	 * 
	 * @param command
	 *            the command to send
	 */
	public void handleCommand(String command);

	/**
	 * Sends a command to the hardware and reads back a data reply.
	 * 
	 * @param command the command sent to the hardware
	 * @return the reply
	 * @throws DeviceException
	 */
	public String sendCommandAndGetReply(String command) throws DeviceException;
}