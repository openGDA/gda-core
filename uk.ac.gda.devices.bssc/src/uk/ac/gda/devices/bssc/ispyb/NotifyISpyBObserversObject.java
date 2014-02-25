/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package uk.ac.gda.devices.bssc.ispyb;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NotifyObserversObject {
	private static final Logger logger = LoggerFactory.getLogger(NotifyObserversObject.class);
	private DatagramSocket socket;
	private int port;

	public NotifyObserversObject(int port) {
		try {
			this.port = port;
			socket = new DatagramSocket(port);
		} catch (SocketException e) {
			logger.error("Error creating datagram socket", e);
		}
	}

	public void notifyObservers(long dataCollectionId) {
		byte[] buf = new byte[1024];
		InetAddress inetAddress;
		try {
			inetAddress = InetAddress.getByName("ws141.diamond.ac.uk");
			buf = ("simpleUDPServer:" + dataCollectionId).getBytes();
			DatagramPacket packet = new DatagramPacket(buf, buf.length, inetAddress, port);
			socket.send(packet);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			logger.error("TODO put description of error here", e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error("TODO put description of error here", e);
		}
	}

	public static void main(String[] args) {
		NotifyObserversObject notifyObject = new NotifyObserversObject(9877);
		notifyObject.notifyObservers(0);
	}
}
