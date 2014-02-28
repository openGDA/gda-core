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

package uk.ac.gda.devices.bssc.ispyb;

import gda.device.DeviceBase;
import gda.device.corba.impl.DeviceAdapter;
import gda.device.corba.impl.DeviceImpl;
import gda.factory.Configurable;
import gda.factory.FactoryException;
import gda.factory.corba.util.CorbaAdapterClass;
import gda.factory.corba.util.CorbaImplClass;
import gda.observable.IObserver;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
	This class receives UDP datagrams containing a string of format prefix:message, which is
	broadcast to objects observing this to inform then a table in ISPyB has been updated
*/
@CorbaAdapterClass(DeviceAdapter.class)
@CorbaImplClass(DeviceImpl.class)
public class UDPListener extends DeviceBase implements Configurable {
	private static final Logger logger = LoggerFactory.getLogger(UDPListener.class);
	boolean running = true;
	private int port=9876;
	private String prefix="";

	
	@Override
	public void configure() throws FactoryException {
		if(isRunning())
			startReading();
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
						if(fields.length==2 && fields[0].equals(prefix))
							notifyIObservers(this, fields[1]);
					}
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
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

	/**
	 * @param args
	 * @throws FactoryException 
	 */
	public static void main(String[] args) throws FactoryException {
		UDPListener simpleUDPServer = new UDPListener();
		simpleUDPServer.setName("simpleUDPServer");
		simpleUDPServer.setRunning(true);
		simpleUDPServer.setPort(9877);
		simpleUDPServer.setPrefix("simpleUDPServer");
		simpleUDPServer.configure();
		simpleUDPServer.addIObserver(new SimpleUDPReceiver());
	}
}

class SimpleUDPReceiver implements IObserver {
	@Override
	public void update(Object theObserved, Object changeCode) {
		System.out.println(changeCode.toString());
	}
}