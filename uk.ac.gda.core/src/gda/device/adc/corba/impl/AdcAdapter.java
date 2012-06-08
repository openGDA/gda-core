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

package gda.device.adc.corba.impl;

import gda.device.Adc;
import gda.device.DeviceException;
import gda.device.adc.corba.CorbaAdc;
import gda.device.adc.corba.CorbaAdcHelper;
import gda.device.corba.CorbaDeviceException;
import gda.device.corba.impl.DeviceAdapter;
import gda.factory.corba.util.NetService;

import org.omg.CORBA.COMM_FAILURE;
import org.omg.CORBA.TRANSIENT;

/**
 * A client side implementation of the adapter pattern for the Adc class
 */
public class AdcAdapter extends DeviceAdapter implements Adc {
	private CorbaAdc corbaAdc;

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
	public AdcAdapter(org.omg.CORBA.Object obj, String name, NetService netService) {
		super(obj, name, netService);
		corbaAdc = CorbaAdcHelper.narrow(obj);
		this.netService = netService;
		this.name = name;
	}

	@Override
	public double getVoltage(int channel) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaAdc.getVoltage(channel);
			} catch (COMM_FAILURE cf) {
				corbaAdc = CorbaAdcHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaAdc = CorbaAdcHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public double[] getVoltages() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaAdc.getVoltages();
			} catch (COMM_FAILURE cf) {
				corbaAdc = CorbaAdcHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaAdc = CorbaAdcHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void setRange(int channel, int range) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaAdc.setRange(channel, range);
				return;
			} catch (COMM_FAILURE cf) {
				corbaAdc = CorbaAdcHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaAdc = CorbaAdcHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public int getRange(int channel) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaAdc.getRange(channel);
			} catch (COMM_FAILURE cf) {
				corbaAdc = CorbaAdcHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaAdc = CorbaAdcHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void setUniPolar(int channel, boolean polarity) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaAdc.setUniPolar(channel, polarity);
				return;
			} catch (COMM_FAILURE cf) {
				corbaAdc = CorbaAdcHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaAdc = CorbaAdcHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public int[] getRanges() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaAdc.getRanges();
			} catch (COMM_FAILURE cf) {
				corbaAdc = CorbaAdcHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaAdc = CorbaAdcHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public boolean isUniPolarSettable() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaAdc.isUniPolarSettable();
			} catch (COMM_FAILURE cf) {
				corbaAdc = CorbaAdcHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaAdc = CorbaAdcHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void setSampleCount(int count) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaAdc.setSampleCount(count);
				return;
			} catch (COMM_FAILURE cf) {
				corbaAdc = CorbaAdcHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaAdc = CorbaAdcHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}
}
