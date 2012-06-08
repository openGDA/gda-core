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

package gda.device.currentamplifier.corba.impl;

import gda.device.CurrentAmplifier;
import gda.device.DeviceException;
import gda.device.corba.CorbaDeviceException;
import gda.device.currentamplifier.corba.CorbaCurrentAmplifier;
import gda.device.currentamplifier.corba.CorbaCurrentAmplifierHelper;
import gda.device.scannable.ScannableUtils;
import gda.device.scannable.corba.impl.ScannableAdapter;
import gda.factory.corba.util.NetService;

import java.io.Serializable;

import org.omg.CORBA.COMM_FAILURE;
import org.omg.CORBA.TRANSIENT;
import org.python.core.PyObject;

/**
 * A client side implementation of the adapter pattern for the motor class
 */
public class CurrentamplifierAdapter extends ScannableAdapter implements CurrentAmplifier {
	private CorbaCurrentAmplifier corbaCurrAmp;
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
	public CurrentamplifierAdapter(org.omg.CORBA.Object obj, String name, NetService netService) {
		super(obj, name, netService);
		corbaCurrAmp = CorbaCurrentAmplifierHelper.narrow(obj);
		this.netService = netService;
		this.name = name;
	}

	@Override
	public void asynchronousMoveTo(Object position) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				if (position instanceof PyObject) {
					position = ScannableUtils.convertToJava((PyObject) position);
				}
				org.omg.CORBA.Any any = org.omg.CORBA.ORB.init().create_any();
				any.insert_Value((Serializable) position);
				corbaCurrAmp.asynchronousMoveTo(any);
				return;
			} catch (COMM_FAILURE cf) {
				corbaCurrAmp = CorbaCurrentAmplifierHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaCurrAmp = CorbaCurrentAmplifierHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public Object getPosition() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				org.omg.CORBA.Any any = corbaCurrAmp.getPosition();
				return any.extract_Value();
			} catch (COMM_FAILURE cf) {
				corbaCurrAmp = CorbaCurrentAmplifierHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaCurrAmp = CorbaCurrentAmplifierHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public boolean isBusy() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaCurrAmp.isBusy();
			} catch (COMM_FAILURE cf) {
				corbaCurrAmp = CorbaCurrentAmplifierHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaCurrAmp = CorbaCurrentAmplifierHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void atScanStart() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaCurrAmp.atScanStart();
			} catch (COMM_FAILURE cf) {
				corbaCurrAmp = CorbaCurrentAmplifierHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaCurrAmp = CorbaCurrentAmplifierHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void atScanEnd() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaCurrAmp.atScanEnd();
			} catch (COMM_FAILURE cf) {
				corbaCurrAmp = CorbaCurrentAmplifierHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaCurrAmp = CorbaCurrentAmplifierHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public double getCurrent() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaCurrAmp.getCurrent();
			} catch (COMM_FAILURE cf) {
				corbaCurrAmp = CorbaCurrentAmplifierHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaCurrAmp = CorbaCurrentAmplifierHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}

		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public String getGain() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaCurrAmp.getGain();
			} catch (COMM_FAILURE cf) {
				corbaCurrAmp = CorbaCurrentAmplifierHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaCurrAmp = CorbaCurrentAmplifierHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}

		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public String[] getGainPositions() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaCurrAmp.getGainPositions();
			} catch (COMM_FAILURE cf) {
				corbaCurrAmp = CorbaCurrentAmplifierHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaCurrAmp = CorbaCurrentAmplifierHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}

		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public String getMode() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaCurrAmp.getMode();
			} catch (COMM_FAILURE cf) {
				corbaCurrAmp = CorbaCurrentAmplifierHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaCurrAmp = CorbaCurrentAmplifierHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}

		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public String[] getModePositions() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaCurrAmp.getModePositions();
			} catch (COMM_FAILURE cf) {
				corbaCurrAmp = CorbaCurrentAmplifierHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaCurrAmp = CorbaCurrentAmplifierHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}

		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public Status getStatus() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return CurrentAmplifier.Status.from_int(corbaCurrAmp.getStatus().value());
			} catch (COMM_FAILURE cf) {
				corbaCurrAmp = CorbaCurrentAmplifierHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaCurrAmp = CorbaCurrentAmplifierHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}

		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void setGain(String position) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaCurrAmp.setGain(position);
				return;
			} catch (COMM_FAILURE cf) {
				corbaCurrAmp = CorbaCurrentAmplifierHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaCurrAmp = CorbaCurrentAmplifierHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}

		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void setMode(String mode) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaCurrAmp.setMode(mode);
				return;
			} catch (COMM_FAILURE cf) {
				corbaCurrAmp = CorbaCurrentAmplifierHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaCurrAmp = CorbaCurrentAmplifierHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}

		}
		throw new DeviceException("Communication failure: retry failed");

	}

	/**
	 * @see gda.device.CurrentAmplifier#listGains()
	 */
	@Override
	public void listGains() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaCurrAmp.listGains();
				return;
			} catch (COMM_FAILURE cf) {
				corbaCurrAmp = CorbaCurrentAmplifierHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaCurrAmp = CorbaCurrentAmplifierHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}

		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public String getGainUnit() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaCurrAmp.getGainUnit();
			} catch (COMM_FAILURE cf) {
				corbaCurrAmp = CorbaCurrentAmplifierHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaCurrAmp = CorbaCurrentAmplifierHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}

		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public String[] getGainUnits() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaCurrAmp.getGainUnits();
			} catch (COMM_FAILURE cf) {
				corbaCurrAmp = CorbaCurrentAmplifierHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaCurrAmp = CorbaCurrentAmplifierHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}

		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void setGainUnit(String unit) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaCurrAmp.setGainUnit(unit);
				return;
			} catch (COMM_FAILURE cf) {
				corbaCurrAmp = CorbaCurrentAmplifierHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaCurrAmp = CorbaCurrentAmplifierHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}

		}
		throw new DeviceException("Communication failure: retry failed");
	}
}
