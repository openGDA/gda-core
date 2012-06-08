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

package gda.device.gpib.corba.impl;

import gda.device.DeviceException;
import gda.device.Gpib;
import gda.device.corba.CorbaDeviceException;
import gda.device.corba.impl.DeviceAdapter;
import gda.device.gpib.corba.CorbaGpib;
import gda.device.gpib.corba.CorbaGpibHelper;
import gda.factory.corba.util.NetService;

import org.omg.CORBA.COMM_FAILURE;
import org.omg.CORBA.TRANSIENT;

/**
 * A client side implementation of the adapter pattern for the Gpib class
 */
public class GpibAdapter extends DeviceAdapter implements Gpib {
	private CorbaGpib corbaGpib;

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
	public GpibAdapter(org.omg.CORBA.Object obj, String name, NetService netService) {
		super(obj, name, netService);
		corbaGpib = CorbaGpibHelper.narrow(obj);
		this.netService = netService;
		this.name = name;
	}

	@Override
	public int findDevice(String deviceName) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaGpib.findDevice(deviceName);
			} catch (COMM_FAILURE cf) {
				corbaGpib = CorbaGpibHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaGpib = CorbaGpibHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public int getSerialPollByte(String deviceName) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaGpib.getSerialPollByte(deviceName);
			} catch (COMM_FAILURE cf) {
				corbaGpib = CorbaGpibHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaGpib = CorbaGpibHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void sendDeviceClear(String deviceName) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaGpib.sendDeviceClear(deviceName);
				return;
			} catch (COMM_FAILURE cf) {
				corbaGpib = CorbaGpibHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaGpib = CorbaGpibHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void sendInterfaceClear(String interFaceName) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaGpib.sendInterfaceClear(interFaceName);
				return;
			} catch (COMM_FAILURE cf) {
				corbaGpib = CorbaGpibHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaGpib = CorbaGpibHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void setTimeOut(String deviceName, int timeout) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaGpib.setTimeOut(deviceName, timeout);
				return;
			} catch (COMM_FAILURE cf) {
				corbaGpib = CorbaGpibHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaGpib = CorbaGpibHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public int getTimeOut(String deviceName) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaGpib.getTimeOut(deviceName);
			} catch (COMM_FAILURE cf) {
				corbaGpib = CorbaGpibHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaGpib = CorbaGpibHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void setTerminator(String deviceName, char term) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaGpib.setTerminator(deviceName, term);
				return;
			} catch (COMM_FAILURE cf) {
				corbaGpib = CorbaGpibHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaGpib = CorbaGpibHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public char getTerminator(String deviceName) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaGpib.getTerminator(deviceName);
			} catch (COMM_FAILURE cf) {
				corbaGpib = CorbaGpibHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaGpib = CorbaGpibHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void setReadTermination(String deviceName, boolean terminate) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaGpib.setReadTermination(deviceName, terminate);
				return;
			} catch (COMM_FAILURE cf) {
				corbaGpib = CorbaGpibHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaGpib = CorbaGpibHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void setWriteTermination(String deviceName, boolean terminate) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaGpib.setWriteTermination(deviceName, terminate);
				return;
			} catch (COMM_FAILURE cf) {
				corbaGpib = CorbaGpibHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaGpib = CorbaGpibHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public boolean getReadTermination(String deviceName) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaGpib.getReadTermination(deviceName);
			} catch (COMM_FAILURE cf) {
				corbaGpib = CorbaGpibHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaGpib = CorbaGpibHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public boolean getWriteTermination(String deviceName) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaGpib.getWriteTermination(deviceName);
			} catch (COMM_FAILURE cf) {
				corbaGpib = CorbaGpibHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaGpib = CorbaGpibHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public String read(String deviceName) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaGpib.read(deviceName);
			} catch (COMM_FAILURE cf) {
				corbaGpib = CorbaGpibHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaGpib = CorbaGpibHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public String read(String deviceName, int strLength) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaGpib.read2(deviceName, strLength);
			} catch (COMM_FAILURE cf) {
				corbaGpib = CorbaGpibHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaGpib = CorbaGpibHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void write(String deviceName, String buffer) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaGpib.write(deviceName, buffer);
				return;
			} catch (COMM_FAILURE cf) {
				corbaGpib = CorbaGpibHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaGpib = CorbaGpibHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}
}
