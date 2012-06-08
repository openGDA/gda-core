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

package gda.device.qbpm.corba.impl;

import gda.device.Device;
import gda.device.DeviceException;
import gda.device.Monitor;
import gda.device.Qbpm;
import gda.device.Scannable;
import gda.device.corba.CorbaDeviceException;
import gda.device.enumpositioner.corba.impl.EnumpositionerAdapter;
import gda.device.qbpm.corba.CorbaQbpm;
import gda.device.qbpm.corba.CorbaQbpmHelper;
import gda.factory.Findable;
import gda.factory.corba.util.NetService;

import org.omg.CORBA.COMM_FAILURE;
import org.omg.CORBA.TRANSIENT;

/**
 * A client side implementation of the adapter pattern for the Qbpm class
 */
public class QbpmAdapter extends EnumpositionerAdapter implements Monitor, Findable, Device, Scannable, Qbpm {
	private CorbaQbpm corbaQbpm;

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
	public QbpmAdapter(org.omg.CORBA.Object obj, String name, NetService netService) {
		super(obj, name, netService);
		corbaQbpm = CorbaQbpmHelper.narrow(obj);
		this.netService = netService;
		this.name = name;
	}

	@Override
	public int getElementCount() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaQbpm.getElementCount();
			} catch (COMM_FAILURE cf) {
				corbaQbpm = CorbaQbpmHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaQbpm = CorbaQbpmHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public String getUnit() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaQbpm.getUnit();
			} catch (COMM_FAILURE cf) {
				corbaQbpm = CorbaQbpmHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaQbpm = CorbaQbpmHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public String getBpmName() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaQbpm.getBpmName();
			} catch (COMM_FAILURE cf) {
				corbaQbpm = CorbaQbpmHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaQbpm = CorbaQbpmHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void setBpmName(String name) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaQbpm.setBpmName(name);
			} catch (COMM_FAILURE cf) {
				corbaQbpm = CorbaQbpmHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaQbpm = CorbaQbpmHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public String getCurrAmpQuadName() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaQbpm.getCurrAmpQuadName();
			} catch (COMM_FAILURE cf) {
				corbaQbpm = CorbaQbpmHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaQbpm = CorbaQbpmHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void setCurrAmpQuadName(String name) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaQbpm.setCurrAmpQuadName(name);
			} catch (COMM_FAILURE cf) {
				corbaQbpm = CorbaQbpmHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaQbpm = CorbaQbpmHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public double getCurrent1() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaQbpm.getCurrent1();
			} catch (COMM_FAILURE cf) {
				corbaQbpm = CorbaQbpmHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaQbpm = CorbaQbpmHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public double getCurrent2() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaQbpm.getCurrent2();
			} catch (COMM_FAILURE cf) {
				corbaQbpm = CorbaQbpmHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaQbpm = CorbaQbpmHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public double getCurrent3() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaQbpm.getCurrent3();
			} catch (COMM_FAILURE cf) {
				corbaQbpm = CorbaQbpmHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaQbpm = CorbaQbpmHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public double getCurrent4() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaQbpm.getCurrent4();
			} catch (COMM_FAILURE cf) {
				corbaQbpm = CorbaQbpmHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaQbpm = CorbaQbpmHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public String getRangeValue() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaQbpm.getRangeValue();
			} catch (COMM_FAILURE cf) {
				corbaQbpm = CorbaQbpmHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaQbpm = CorbaQbpmHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public double getIntensityTotal() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaQbpm.getIntensityTotal();
			} catch (COMM_FAILURE cf) {
				corbaQbpm = CorbaQbpmHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaQbpm = CorbaQbpmHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public double getXPosition() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaQbpm.getXPosition();
			} catch (COMM_FAILURE cf) {
				corbaQbpm = CorbaQbpmHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaQbpm = CorbaQbpmHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public double getYPosition() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaQbpm.getYPosition();
			} catch (COMM_FAILURE cf) {
				corbaQbpm = CorbaQbpmHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaQbpm = CorbaQbpmHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

}