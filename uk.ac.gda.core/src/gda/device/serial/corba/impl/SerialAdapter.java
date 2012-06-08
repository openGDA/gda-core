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

package gda.device.serial.corba.impl;

import gda.device.DeviceException;
import gda.device.Serial;
import gda.device.corba.CorbaDeviceException;
import gda.device.corba.impl.DeviceAdapter;
import gda.device.serial.corba.CorbaSerial;
import gda.device.serial.corba.CorbaSerialHelper;
import gda.factory.corba.util.NetService;

import org.omg.CORBA.COMM_FAILURE;
import org.omg.CORBA.TRANSIENT;

/**
 * A client side implementation of the adapter pattern for the Serial class
 */
public class SerialAdapter extends DeviceAdapter implements Serial {
	private CorbaSerial corbaSerial;


	/**
	 * Create client side interface to the CORBA package.
	 * 
	 * @param obj
	 *            the CORBA object
	 * @param name
	 *            the name of the object
	 * @param netService
	 *            the CORBA naming service
	 */
	public SerialAdapter(org.omg.CORBA.Object obj, String name, NetService netService) {
		super(obj, name, netService);
		corbaSerial = CorbaSerialHelper.narrow(obj);
		this.netService = netService;
		this.name = name;
	}

	@Override
	public void setBaudRate(int baudRate) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaSerial.setBaudRate(baudRate);
				return;
			} catch (COMM_FAILURE cf) {
				corbaSerial = CorbaSerialHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaSerial = CorbaSerialHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void setByteSize(int byteSize) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaSerial.setByteSize(byteSize);
				return;
			} catch (COMM_FAILURE cf) {
				corbaSerial = CorbaSerialHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaSerial = CorbaSerialHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void setParity(String parity) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaSerial.setParity(parity);
				return;
			} catch (COMM_FAILURE cf) {
				corbaSerial = CorbaSerialHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaSerial = CorbaSerialHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void setStopBits(int stopBits) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaSerial.setStopBits(stopBits);
				return;
			} catch (COMM_FAILURE cf) {
				corbaSerial = CorbaSerialHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaSerial = CorbaSerialHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void setFlowControl(String flowControl) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaSerial.setFlowControl(flowControl);
				return;
			} catch (COMM_FAILURE cf) {
				corbaSerial = CorbaSerialHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaSerial = CorbaSerialHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public int getReadTimeout() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaSerial.getReadTimeout();
			} catch (COMM_FAILURE cf) {
				corbaSerial = CorbaSerialHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaSerial = CorbaSerialHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	// -------------------------------------------------
	// CharReadableDev methods ...
	// -------------------------------------------------

	@Override
	public char readChar() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaSerial.readChar();
			} catch (COMM_FAILURE cf) {
				corbaSerial = CorbaSerialHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaSerial = CorbaSerialHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void flush() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaSerial.flush();
				return;
			} catch (COMM_FAILURE cf) {
				corbaSerial = CorbaSerialHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaSerial = CorbaSerialHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void setReadTimeout(int time) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaSerial.setReadTimeout(time);
				return;
			} catch (COMM_FAILURE cf) {
				corbaSerial = CorbaSerialHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaSerial = CorbaSerialHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	// -------------------------------------------------
	// CharWritableDev methods ...
	// -------------------------------------------------

	@Override
	public void writeChar(char c) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaSerial.writeChar(c);
				return;
			} catch (COMM_FAILURE cf) {
				corbaSerial = CorbaSerialHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaSerial = CorbaSerialHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}
}
