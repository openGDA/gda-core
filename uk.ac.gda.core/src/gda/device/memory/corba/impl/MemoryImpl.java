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
import gda.device.corba.impl.DeviceImpl;
import gda.device.memory.corba.CorbaMemoryPOA;
import gda.factory.corba.CorbaFactoryException;

/**
 * A server side implementation for a distributed Memory class
 */
public class MemoryImpl extends CorbaMemoryPOA {
	//
	// Private reference to implementation object
	//
	private Memory memory;

	private DeviceImpl deviceImpl;

	//
	// Private reference to POA
	//
	private org.omg.PortableServer.POA poa;

	/**
	 * Create server side implementation to the CORBA package.
	 * 
	 * @param memory
	 *            the Memory implementation object
	 * @param poa
	 *            the portable object adapter
	 */
	public MemoryImpl(Memory memory, org.omg.PortableServer.POA poa) {
		this.memory = memory;
		this.poa = poa;
		deviceImpl = new DeviceImpl(memory, poa);
	}

	/**
	 * Get the implementation object
	 * 
	 * @return the Memory implementation object
	 */
	public Memory _delegate() {
		return memory;
	}

	/**
	 * Set the implementation object.
	 * 
	 * @param memory
	 *            set the Memory implementation object
	 */
	public void _delegate(Memory memory) {
		this.memory = memory;
	}

	@Override
	public org.omg.PortableServer.POA _default_POA() {
		return (poa != null) ? poa : super._default_POA();
	}

	@Override
	public void clear() throws CorbaDeviceException {
		try {
			memory.clear();
			return;
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public void clear2(int start, int count) throws CorbaDeviceException {
		try {
			memory.clear(start, count);
			return;
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public void clear3(int x, int y, int t, int dx, int dy, int dt) throws CorbaDeviceException {
		try {
			memory.clear(x, y, t, dx, dy, dt);
			return;
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public void start() throws CorbaDeviceException {
		try {
			memory.start();
			return;
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public void stop() throws CorbaDeviceException {
		try {
			memory.stop();
			return;
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public double[] read(int x, int y, int t, int dx, int dy, int dt) throws CorbaDeviceException {
		try {
			return memory.read(x, y, t, dx, dy, dt);
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public double[] read2(int frame) throws CorbaDeviceException {
		try {
			return memory.read(frame);
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public void setDimension(int[] d) throws CorbaDeviceException {
		try {
			memory.setDimension(d);
			return;
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public int[] getDimension() throws CorbaDeviceException {
		try {
			return memory.getDimension();
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public void write(double[] data, int x, int y, int t, int dx, int dy, int dt) throws CorbaDeviceException {
		try {
			memory.write(data, x, y, t, dx, dy, dt);
			return;
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public void write2(double[] data, int frame) throws CorbaDeviceException {
		try {
			memory.write(data, frame);
			return;
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public void output(String file) throws CorbaDeviceException {
		try {
			memory.output(file);
			return;
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public int getMemorySize() throws CorbaDeviceException {
		try {
			return memory.getMemorySize();
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public void setAttribute(String attributeName, org.omg.CORBA.Any value) throws CorbaDeviceException {
		deviceImpl.setAttribute(attributeName, value);
	}

	@Override
	public org.omg.CORBA.Any getAttribute(String attributeName) throws CorbaDeviceException {
		return deviceImpl.getAttribute(attributeName);
	}

	@Override
	public void reconfigure() throws CorbaFactoryException {
		deviceImpl.reconfigure();
	}

	@Override
	public void close() throws CorbaDeviceException {
		deviceImpl.close();
	}

	@Override
	public int[] getSupportedDimensions() throws CorbaDeviceException {
		try {
			return memory.getSupportedDimensions();
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}
	
	@Override
	public int getProtectionLevel() throws CorbaDeviceException {
		return deviceImpl.getProtectionLevel();
	}

	@Override
	public void setProtectionLevel(int newLevel) throws CorbaDeviceException {
		deviceImpl.setProtectionLevel(newLevel);
	}
}
