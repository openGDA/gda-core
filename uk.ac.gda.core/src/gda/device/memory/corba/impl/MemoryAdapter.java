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

package gda.device.memory.corba.impl;

import gda.device.DeviceException;
import gda.device.Memory;
import gda.device.corba.CorbaDeviceException;
import gda.device.corba.impl.DeviceAdapter;
import gda.device.memory.corba.CorbaMemory;
import gda.device.memory.corba.CorbaMemoryHelper;
import gda.factory.corba.util.NetService;

import org.omg.CORBA.COMM_FAILURE;
import org.omg.CORBA.TRANSIENT;

/**
 * A client side implementation of the adapter pattern for the memory class
 */
public class MemoryAdapter extends DeviceAdapter implements Memory {
	private CorbaMemory corbaMemory;

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
	public MemoryAdapter(org.omg.CORBA.Object obj, String name, NetService netService) {
		super(obj, name, netService);
		corbaMemory = CorbaMemoryHelper.narrow(obj);
		this.netService = netService;
		this.name = name;
	}

	@Override
	public void clear() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaMemory.clear();
				return;
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			} catch (COMM_FAILURE cf) {
				corbaMemory = CorbaMemoryHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaMemory = CorbaMemoryHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void clear(int start, int count) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaMemory.clear2(start, count);
				return;
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			} catch (COMM_FAILURE cf) {
				corbaMemory = CorbaMemoryHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaMemory = CorbaMemoryHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void clear(int x, int y, int t, int dx, int dy, int dt) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaMemory.clear3(x, y, t, dx, dy, dt);
				return;
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			} catch (COMM_FAILURE cf) {
				corbaMemory = CorbaMemoryHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaMemory = CorbaMemoryHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void start() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaMemory.start();
				return;
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			} catch (COMM_FAILURE cf) {
				corbaMemory = CorbaMemoryHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaMemory = CorbaMemoryHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void stop() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaMemory.stop();
				return;
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			} catch (COMM_FAILURE cf) {
				corbaMemory = CorbaMemoryHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaMemory = CorbaMemoryHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public double[] read(int x, int y, int t, int dx, int dy, int dt) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaMemory.read(x, y, t, dx, dy, dt);
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			} catch (COMM_FAILURE cf) {
				corbaMemory = CorbaMemoryHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaMemory = CorbaMemoryHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public double[] read(int frame) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaMemory.read2(frame);
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			} catch (COMM_FAILURE cf) {
				corbaMemory = CorbaMemoryHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaMemory = CorbaMemoryHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void setDimension(int[] d) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaMemory.setDimension(d);
				return;
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			} catch (COMM_FAILURE cf) {
				corbaMemory = CorbaMemoryHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaMemory = CorbaMemoryHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public int[] getDimension() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaMemory.getDimension();
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			} catch (COMM_FAILURE cf) {
				corbaMemory = CorbaMemoryHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaMemory = CorbaMemoryHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void write(double[] data, int x, int y, int t, int dx, int dy, int dt) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaMemory.write(data, x, y, t, dx, dy, dt);
				return;
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			} catch (COMM_FAILURE cf) {
				corbaMemory = CorbaMemoryHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaMemory = CorbaMemoryHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void write(double[] data, int frame) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaMemory.write2(data, frame);
				return;
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			} catch (COMM_FAILURE cf) {
				corbaMemory = CorbaMemoryHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaMemory = CorbaMemoryHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void output(String file) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaMemory.output(file);
				return;
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			} catch (COMM_FAILURE cf) {
				corbaMemory = CorbaMemoryHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaMemory = CorbaMemoryHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public int getMemorySize() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaMemory.getMemorySize();
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			} catch (COMM_FAILURE cf) {
				corbaMemory = CorbaMemoryHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaMemory = CorbaMemoryHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public int[] getSupportedDimensions() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaMemory.getSupportedDimensions();
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			} catch (COMM_FAILURE cf) {
				corbaMemory = CorbaMemoryHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaMemory = CorbaMemoryHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}
}
