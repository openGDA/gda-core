/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

import gda.device.DeviceException;
import gda.io.BidiAsciiCommunicator;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;

/**
 * Class to make bidirectional ascii communicator over a socket simplyt create the class, set properties if default is
 * not appropriate, call afterPropertiesSet and use methods send, sendCmdNoReply and closeConnection. The connection is
 * made when it is needed if not already opened.
 */
public class SocketBidiAsciiCommunicator implements BidiAsciiCommunicator, InitializingBean {
	protected static final Logger logger = LoggerFactory.getLogger(SocketBidiAsciiCommunicator.class);

	protected OutputStream writer;
	protected InputStream reader;
	private String address = "";
	private int port = -1;
	private Socket socket;
	protected final ReentrantLock lock = new ReentrantLock();
	// properties
	private String cmdTerm = "\r\n";
	private String replyTerm = "\r\n";
	private int timeout = 5000;

	@Override
	public String send(String cmd) throws DeviceException {
		lock.lock();
		try {
			sendCmdNoReply(cmd);
			StringBuffer sb = new StringBuffer();
			while (true) {
				byte read[] = new byte[100];
				int bytesRead = 0;
				try {
					bytesRead = reader.read(read);
				} catch (IOException e) {
					throw new DeviceException("Error in reading reply to '" + cmd + "'", e);
				}
				try {
					String str = new String(read, 0, bytesRead, "US-ASCII");
					sb.append(str);
				} catch (UnsupportedEncodingException e) {
					throw new DeviceException("Error in reading reply to '" + cmd + "' char=" + read, e);
				}
				if (sb.length() >= replyTerm.length()) {
					String substring2 = sb.substring(sb.length() - replyTerm.length(), sb.length());
					if (substring2.equals(replyTerm)) {
						String substring = sb.substring(0, sb.length() - replyTerm.length());
						if (logger.isDebugEnabled())
							logger.debug("reply = '" + substring + "'");
						return substring;

					}
				}
			}
		} finally {
			lock.unlock();
		}
	}

	public void closeConnection() throws IOException {
		//do not use lock here as the point of this method is to allow another thread to forcibly close the connection
		if (socket != null) {
			reader = null;
			writer = null;
			socket.close();
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (!StringUtils.hasText(address))
			throw new Exception("Address is empty");
		if (port <= 0)
			throw new Exception("Port is <=0");

	}

	public String getCmdTerm() {
		return cmdTerm;
	}

	public void setCmdTerm(String cmdTerm) {
		this.cmdTerm = cmdTerm;
	}

	public String getReplyTerm() {
		return replyTerm;
	}

	public void setReplyTerm(String replyTerm) {
		this.replyTerm = replyTerm;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	@Override
	public void sendCmdNoReply(String cmd) throws DeviceException {
		lock.lock();
		try {
			connectIfRequired();
			if (logger.isDebugEnabled())
				logger.debug("cmd = '" + cmd + "'");
			try {
				// we need to pack the data into a buffer and send it for speed.
				byte[] buf = new byte[cmd.length() + cmdTerm.length()];
				for (int i = 0; i < cmd.length(); i++) {
					buf[i] = (byte) cmd.charAt(i);
				}
				for (int i = 0; i < cmdTerm.length(); i++) {
					buf[cmd.length() + i] = (byte) cmdTerm.charAt(i);
				}
				writer.write(buf);
				writer.flush();
			} catch (IOException e1) {
				throw new DeviceException("Error writing cmd '" + cmd + "'", e1);
			}
		} finally {
			lock.unlock();
		}
	}

	protected void connectIfRequired() throws DeviceException {
		if (reader == null || writer == null) {
			try {
				socket = new Socket(address, port);
				socket.setKeepAlive(true);
				socket.setSoTimeout(timeout);
				reader = socket.getInputStream();
				writer = socket.getOutputStream();
			} catch (Exception e) {
				throw new DeviceException("Error connecting to '" + address + ":" + port +"'", e);
			}
		}
	}

}
