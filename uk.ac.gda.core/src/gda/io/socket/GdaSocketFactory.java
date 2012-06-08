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

package gda.io.socket;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * <b>Title: </b>The GDA socket factory.
 * </p>
 * <p>
 * <b>Description: </b>This class extends <code>javax.net.SocketFactory</code> to provide a socket factory for the
 * GDA. Each socket opened is logged and any exceptions are caught and logged. This class provides a central location to
 * set socket parameters. To ensure future flexibility the user of objects of this class should use a reference of type
 * <code>javax.net.SocketFactory</code>.
 * </p>
 */
public class GdaSocketFactory extends javax.net.SocketFactory {
	private static final Logger logger = LoggerFactory.getLogger(GdaSocketFactory.class);

	/** Private reference to the most recent socket created. */
	private Socket socket = null;

	/**
	 * Creates a socket and connects it to the specified remote host at the specified remote port.
	 * 
	 * @param host
	 *            The hostname.
	 * @param port
	 *            The port number.
	 * @return Socket
	 * @throws UnknownHostException
	 */
	@Override
	public Socket createSocket(String host, int port) throws UnknownHostException {
		return this.createSocket(InetAddress.getByName(host), port);
	}

	/**
	 * Creates a socket and connects it to the specified remote host on the specified remote port. The socket will also
	 * be bound to the local address and port supplied.
	 * 
	 * @param host
	 *            The hostname.
	 * @param port
	 *            The port number.
	 * @param localHost
	 *            The local hostname.
	 * @param localPort
	 *            The local port number.
	 * @return Socket
	 * @throws UnknownHostException
	 */
	@Override
	public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws UnknownHostException {
		this.createSocket(InetAddress.getByName(host), port);
		this.bindSocketToLocalHost(localHost, localPort);
		return socket;
	}

	/**
	 * Creates a socket and connects it to the specified port number at the specified address.
	 * 
	 * @param host
	 *            The hostname.
	 * @param port
	 *            The port number.
	 * @return Socket
	 * @throws UnknownHostException
	 */
	@Override
	public Socket createSocket(InetAddress host, int port) throws UnknownHostException {
		try {
			socket = new Socket(host, port);
			logger.info("Opening socket to remote host:Host: " + host + " Port: " + port);
		} catch (UnknownHostException e) {
			logger.error("Could not find host " + host);
			throw e;
		} catch (IOException e) {
			logger.error("IOException occured when trying to create socket to host " + host + " on port " + port);
			return null;
		} catch (SecurityException e) {
			logger.error("The SecurityManager does not allow connections to host " + host + " on port " + port);
			return null;
		}

		// This is the place to set any socket options that should be universal.

		return socket;
	}

	/**
	 * Creates a socket and connect it to the specified remote address on the specified remote port. The socket will
	 * also be bound to the local address and port suplied.
	 * 
	 * @param host
	 *            The hostname.
	 * @param port
	 *            The port number.
	 * @param localHost
	 *            The local hostname.
	 * @param localPort
	 *            The local port number.
	 * @return Socket
	 * @throws UnknownHostException
	 */
	@Override
	public Socket createSocket(InetAddress host, int port, InetAddress localHost, int localPort)
			throws UnknownHostException {
		this.createSocket(host, port);
		this.bindSocketToLocalHost(localHost, localPort);
		return socket;
	}

	/**
	 * Bind the socket to local host and port.
	 * 
	 * @param localHost
	 * @param localPort
	 */
	private void bindSocketToLocalHost(InetAddress localHost, int localPort) {
		try {
			socket.bind(this.createSocketAddress(localHost, localPort));
			logger.info("Bound the socket to local Host: " + localHost + " Port: " + localPort);
		} catch (IOException e) {
			logger.warn("IOException occured when trying to bind socket to local host.");
		} catch (IllegalArgumentException e) {
			logger.warn("An invalid SocketAddress has been used to bind socket to local host.");
		}
	}

	/**
	 * Private socket address factory method.
	 * 
	 * @param host
	 *            The host.
	 * @param port
	 *            The port.
	 * @return A SocketAddress object.
	 */
	private SocketAddress createSocketAddress(InetAddress host, int port) {
		// Change SocketAddress type to the one desired here.
		return new InetSocketAddress(host, port);
	}
}
