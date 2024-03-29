/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package gda.device.scannable;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.factory.FactoryException;

/**
Scannable to receive UDP datagrams containing a string of format prefix:message.
The position is set the message part of the string.
This is used to inform a gda server when a table in ISpy has been updated by a process on the network
*/
public class SimpleUDPServerScannable extends ScannableBase {
	private static final Logger logger = LoggerFactory.getLogger(SimpleUDPServerScannable.class);
	boolean running = true;
	private int port=9876;
	private String position="";
	private String prefix="";

	@Override
	public void configure() throws FactoryException {
		if (isConfigured()) {
			return;
		}
		super.configure();
		setInputNames(new String[]{});
		setExtraNames(new String[]{getName()});
		if(isRunning())
			startReading();
		setConfigured(true);
	}

	@Override
	public void asynchronousMoveTo(Object data) throws DeviceException {
		if( data instanceof String){
			position = (String)data;
			notifyIObservers(this, new ScannablePositionChangeEvent(position));
		}
	}

	@Override
	public Object getPosition() throws DeviceException {
		return position;
	}

	@Override
	public boolean isBusy() throws DeviceException {
		return false;
	}

	/**
	 * @return true if the file is being monitored
	 */
	public boolean isRunning() {
		return running;
	}

	/**
	 * @param running
	 */
	public void setRunning(boolean running) {
		if (running && !this.running) {
			this.running = true;
			startReading();
		} else if (!running) {
			this.running = false;
		}
	}

	protected void startReading() {
		Thread t = new Thread() {
			@Override
			public void run() {
				try (DatagramSocket serverSocket = new DatagramSocket(port)) {
					byte[] receiveData = new byte[1024];
					while (running) {
						DatagramPacket receivePacket = new DatagramPacket(
								receiveData, receiveData.length);
						serverSocket.receive(receivePacket);
						String s = new String(receivePacket.getData(),0,receivePacket.getLength(), "UTF-8");
						String [] fields = s.split(":",2);
						if (fields.length == 2) {
							if (fields[0].equals(prefix)) {
								asynchronousMoveTo(fields[1]);
							}
						} else {
							logger.error("Invalid message received via UDP");
						}
					}
				} catch (Exception e) {
					logger.error("Error in read thread", e);
				}
			}
		};
		t.start();
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public static void main(String[] args) throws FactoryException {
		SimpleUDPServerScannable simpleUDPServer = new SimpleUDPServerScannable();
		simpleUDPServer.setName("simpleUDPServer");
		simpleUDPServer.setRunning(true);
		simpleUDPServer.setPort(9877);
		simpleUDPServer.setPrefix("simpleUDPServer");
		simpleUDPServer.configure();
		simpleUDPServer.addIObserver(SimpleUDPServerScannable::onUpdate);
	}

	private static void onUpdate(@SuppressWarnings("unused") Object theObserved, Object changeCode) {
		if (changeCode instanceof ScannablePositionChangeEvent) {
			Object pos = ((ScannablePositionChangeEvent) changeCode).newPosition;
			if (pos instanceof String) {
				System.out.println((String) pos);
			}
		}
	}
}
