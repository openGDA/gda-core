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

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.net.SocketFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mindprod.ledatastream.LEDataInputStream;

/**
 * <p>
 * <b>Title: </b>Socket wrapper with little to big endian conversion read methods.
 * </p>
 * <p>
 * <b>Description: </b>This class is a general purpose socket wrapper. It is particulary useful for reading from little
 * endian byte streams (native Intel format). Some methods are provided to read java big endian data types from the
 * little endian byte stream. Some methods are also provided to just read bytes or a line of bytes (eg. a ASCII string).
 * The output stream is a Java buffered <code>DataOutputStream</code>.
 * </p>
 */
public class NativeSock {
	private static final Logger logger = LoggerFactory.getLogger(NativeSock.class);

	/** Socket reference. */
	public Socket socket;

	/** The socket factory to use. */
	private SocketFactory socketFactory = null;

	/** The socket time out to use when connecting to IS. */
	private int socketTimeOut = 50000;

	/** Output stream. */
	protected DataOutputStream outputStream;

	/** Input stream. */
	private LEDataInputStream inputStream;

	/** Host name. */
	private String host = null;

	/** Port number. */
	private int port = 0;

	/**
	 * Connect to the remote host
	 *
	 * @param host
	 *            The remote hostname
	 * @param port
	 * @throws IOException
	 * @throws UnknownHostException
	 * @throws ConnectException
	 */
	public void connect(String host, int port) throws IOException, UnknownHostException, ConnectException {
		this.host = host;
		this.port = port;
		if (socketFactory == null) {
			socketFactory = new GdaSocketFactory();
		}

		socket = socketFactory.createSocket(host, port);
		socket.setSoTimeout(socketTimeOut);
		outputStream = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
		inputStream = new LEDataInputStream(socket.getInputStream());

		if (!((socket.isConnected()) && (!socket.isClosed()))) {
			throw new IOException("Socket failed to connect to remote host " + host + " on port " + port);
		}
	}

	/**
	 * Use this to disconnect from the remote host. It closes the socket and the input/output streams.
	 */
	public void disconnect() {
		try {
			if ((outputStream != null) && (inputStream != null) && (socket != null)) {
				logger.info("Disconnecting from " + host);
				outputStream.close();
				inputStream.close();
				socket.close();
				outputStream = null;
				inputStream = null;
				socket = null;
			}
		} catch (IOException e) {
			logger.warn("Failed to disconnect NativeSock from host " + host + " on port " + port);
		}
	}

	/**
	 * Write a string (it will be terinputStreamated with a carriage return and end-of-line). This will only work with
	 * ASCII characters.
	 *
	 * @param s
	 *            The string to write.
	 * @throws IOException
	 */
	public void write(String s) throws IOException {
		outputStream.writeBytes(s);
		outputStream.writeBytes("\012\015");
		outputStream.flush();
	}

	/**
	 * Read a line of text. This assumes the text is in ASCII format. The method returns the result if a carriage return
	 * <code>\n</code> is reached.
	 *
	 * @return The resulting string.
	 * @throws IOException
	 */
	public String readLine() throws IOException {
		StringBuffer buf = new StringBuffer();
		char c = '0';
		int i = 0;
		// int count = 0;
		do {
			i = inputStream.readByte();
			// System.out.println("count: " + count + " i: " + i);
			// System.out.println(i);
			// count++;
			c = (char) i;
			buf.append(c);
		} while (c != '\n');

		return (buf.toString());
	}

	public byte readByte() throws IOException {
		return inputStream.readByte();
	}


	/**
	 * This reads the input stream byte-by-byte searching for a pattern. This only works with ASCII characters (8 bits).
	 *
	 * @param pattern
	 *            The pattern to read until.
	 * @return The input stream in string format upto the end of the pattern.
	 * @throws IOException
	 */
	public String readUntil(String pattern) throws IOException {

		StringBuffer sb = null;
		try {
			char lastChar = pattern.charAt(pattern.length() - 1);
			sb = new StringBuffer();
			char ch = (char) inputStream.readByte();
			// Loop until finding the pattern. Then return.
			while (true) {
				sb.append(ch);
				if (ch == lastChar) {
					if (sb.toString().endsWith(pattern)) {
						return (sb.toString());
					}
				}
				ch = (char) inputStream.readByte();
			}
		} catch (IOException e) {
			logger.warn("Exception caught when trying to readUntil(" + pattern + ") in NativeSock.");
			throw e;
		}
	}

	/**
	 * Use this to set the socket timeout
	 *
	 * @param timeout
	 */
	public void setSocketTimeOut(int timeout) {
		socketTimeOut = timeout;
		if (socket != null) {
			try {
				socket.setSoTimeout(socketTimeOut);
			} catch (Exception e) {
				throw new RuntimeException("Error in setSocketTimeOut", e);
			}
		}
	}

	/**
	 * Use this to read the socket timeout
	 *
	 * @return int
	 */
	public int getSocketTimeOut() {
		return socketTimeOut;
	}
}
