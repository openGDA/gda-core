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

package gda.device.filterarray.corba.impl;

import gda.device.Device;
import gda.device.DeviceException;
import gda.device.FilterArray;
import gda.device.corba.CorbaDeviceException;
import gda.device.corba.impl.DeviceAdapter;
import gda.device.filterarray.corba.CorbaFilterArray;
import gda.device.filterarray.corba.CorbaFilterArrayHelper;
import gda.factory.Findable;
import gda.factory.corba.util.NetService;

import org.omg.CORBA.COMM_FAILURE;
import org.omg.CORBA.TRANSIENT;

/**
 * A client side implementation of the adapter pattern for the FilterArray class
 */
public class FilterarrayAdapter extends DeviceAdapter implements FilterArray, Findable, Device {
	private CorbaFilterArray corbaFilterArray;
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
	public FilterarrayAdapter(org.omg.CORBA.Object obj, String name, NetService netService) {
		super(obj, name, netService);
		corbaFilterArray = CorbaFilterArrayHelper.narrow(obj);
		this.netService = netService;
		this.name = name;
	}

	@Override
	public double getAbsorption() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaFilterArray.getAbsorption();
			} catch (COMM_FAILURE cf) {
				corbaFilterArray = CorbaFilterArrayHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaFilterArray = CorbaFilterArrayHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void setAbsorption(double absorption) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaFilterArray.setAbsorption(absorption);
				return;
			} catch (COMM_FAILURE cf) {
				corbaFilterArray = CorbaFilterArrayHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaFilterArray = CorbaFilterArrayHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public double getTransmission() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaFilterArray.getTransmission();
			} catch (COMM_FAILURE cf) {
				corbaFilterArray = CorbaFilterArrayHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaFilterArray = CorbaFilterArrayHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void setTransmission(double transmission) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaFilterArray.setTransmission(transmission);
				return;
			} catch (COMM_FAILURE cf) {
				corbaFilterArray = CorbaFilterArrayHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaFilterArray = CorbaFilterArrayHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public double getCalculationEnergy() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaFilterArray.getCalculationEnergy();
			} catch (COMM_FAILURE cf) {
				corbaFilterArray = CorbaFilterArrayHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaFilterArray = CorbaFilterArrayHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void setCalculationEnergy(double energy) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaFilterArray.setCalculationEnergy(energy);
				return;
			} catch (COMM_FAILURE cf) {
				corbaFilterArray = CorbaFilterArrayHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaFilterArray = CorbaFilterArrayHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public double getCalculationWavelength() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaFilterArray.getCalculationWavelength();
			} catch (COMM_FAILURE cf) {
				corbaFilterArray = CorbaFilterArrayHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaFilterArray = CorbaFilterArrayHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void setCalculationWavelength(double wavelength) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaFilterArray.setCalculationWavelength(wavelength);
				return;
			} catch (COMM_FAILURE cf) {
				corbaFilterArray = CorbaFilterArrayHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaFilterArray = CorbaFilterArrayHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public boolean isUsingMonoEnergy() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaFilterArray.isUsingMonoEnergy();
			} catch (COMM_FAILURE cf) {
				corbaFilterArray = CorbaFilterArrayHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaFilterArray = CorbaFilterArrayHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void setUseMonoEnergy(boolean useEnergy) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaFilterArray.setUseMonoEnergy(useEnergy);
				return;
			} catch (COMM_FAILURE cf) {
				corbaFilterArray = CorbaFilterArrayHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaFilterArray = CorbaFilterArrayHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}
}
