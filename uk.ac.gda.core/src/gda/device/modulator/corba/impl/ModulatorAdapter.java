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

package gda.device.modulator.corba.impl;

import gda.device.DeviceException;
import gda.device.Modulator;
import gda.device.corba.CorbaDeviceException;
import gda.device.corba.impl.DeviceAdapter;
import gda.device.modulator.corba.CorbaModulator;
import gda.device.modulator.corba.CorbaModulatorHelper;
import gda.factory.Findable;
import gda.factory.corba.util.NetService;
import gda.jscience.physics.quantities.Wavelength;

import org.jscience.physics.quantities.Frequency;
import org.jscience.physics.quantities.Quantity;
import org.omg.CORBA.COMM_FAILURE;
import org.omg.CORBA.TRANSIENT;

/**
 * A client side implementation of the adapter pattern for the Modulator class
 */
public class ModulatorAdapter extends DeviceAdapter implements Modulator, Findable {
	private CorbaModulator corbaModulator;

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
	public ModulatorAdapter(org.omg.CORBA.Object obj, String name, NetService netService) {
		super(obj, name, netService);
		corbaModulator = CorbaModulatorHelper.narrow(obj);
		this.netService = netService;
		this.name = name;
	}

	@Override
	public Wavelength getWaveLength() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return Quantity.valueOf(corbaModulator.getWaveLength());
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			} catch (COMM_FAILURE cf) {
				corbaModulator = CorbaModulatorHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaModulator = CorbaModulatorHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void setWaveLength(double waveLength) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaModulator.setWaveLength(waveLength);
				return;
			} catch (COMM_FAILURE cf) {
				corbaModulator = CorbaModulatorHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaModulator = CorbaModulatorHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("ModulatorAdapter: Communication failure: retry failed");
	}

	@Override
	public int getRetardation() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaModulator.getRetardation();
			} catch (COMM_FAILURE cf) {
				corbaModulator = CorbaModulatorHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaModulator = CorbaModulatorHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void setRetardation(double retardation) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaModulator.setRetardation(retardation);
				return;
			} catch (COMM_FAILURE cf) {
				corbaModulator = CorbaModulatorHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaModulator = CorbaModulatorHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("ModulatorAdapter: Communication failure: retry failed");
	}

	@Override
	public void reset() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaModulator.reset();
				return;
			} catch (COMM_FAILURE cf) {
				corbaModulator = CorbaModulatorHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaModulator = CorbaModulatorHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void setEcho(boolean echo) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaModulator.setEcho(echo);
				return;
			} catch (COMM_FAILURE cf) {
				corbaModulator = CorbaModulatorHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaModulator = CorbaModulatorHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public Frequency readFrequency(int noOfTimes) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				String valueString = corbaModulator.readFrequency(noOfTimes);
				return Quantity.valueOf(valueString);
			} catch (COMM_FAILURE cf) {
				corbaModulator = CorbaModulatorHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaModulator = CorbaModulatorHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void setInhibit(boolean inhibit) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaModulator.setInhibit(inhibit);
				return;
			} catch (COMM_FAILURE cf) {
				corbaModulator = CorbaModulatorHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaModulator = CorbaModulatorHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}
}
