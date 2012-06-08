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

package gda.device.digitalio.corba.impl;

import gda.device.DeviceException;
import gda.device.DigitalIO;
import gda.device.corba.CorbaDeviceException;
import gda.device.corba.impl.DeviceAdapter;
import gda.device.digitalio.corba.CorbaDigitalIO;
import gda.device.digitalio.corba.CorbaDigitalIOHelper;
import gda.factory.corba.util.NetService;

import org.omg.CORBA.COMM_FAILURE;
import org.omg.CORBA.TRANSIENT;

/**
 * A client side implementation of the adapter pattern for the digitalio class
 */
public class DigitalioAdapter extends DeviceAdapter implements DigitalIO {
	private CorbaDigitalIO corbaDigitalIO;

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
	public DigitalioAdapter(org.omg.CORBA.Object obj, String name, NetService netService) {
		super(obj, name, netService);
		corbaDigitalIO = CorbaDigitalIOHelper.narrow(obj);
		this.netService = netService;
		this.name = name;
	}

	@Override
	public int getState(String channelName) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaDigitalIO.getState(channelName);
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			} catch (COMM_FAILURE cf) {
				corbaDigitalIO = CorbaDigitalIOHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaDigitalIO = CorbaDigitalIOHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void setState(String channelName, int state) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaDigitalIO.setState(channelName, state);
				return;
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			} catch (TRANSIENT ct) {
				corbaDigitalIO = CorbaDigitalIOHelper.narrow(netService.reconnect(name));
			} catch (COMM_FAILURE cf) {
				corbaDigitalIO = CorbaDigitalIOHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void setNegativeEdgeSync(String channelName) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaDigitalIO.setNegativeEdgeSync(channelName);
				return;
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			} catch (TRANSIENT ct) {
				corbaDigitalIO = CorbaDigitalIOHelper.narrow(netService.reconnect(name));
			} catch (COMM_FAILURE cf) {
				corbaDigitalIO = CorbaDigitalIOHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void setPositiveEdgeSync(String channelName) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaDigitalIO.setPositiveEdgeSync(channelName);
				return;
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			} catch (TRANSIENT ct) {
				corbaDigitalIO = CorbaDigitalIOHelper.narrow(netService.reconnect(name));
			} catch (COMM_FAILURE cf) {
				corbaDigitalIO = CorbaDigitalIOHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void setNegative2LineSync(String inputChannelName, String outputChannelName) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaDigitalIO.setNegative2LineSync(inputChannelName, outputChannelName);
				return;
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			} catch (TRANSIENT ct) {
				corbaDigitalIO = CorbaDigitalIOHelper.narrow(netService.reconnect(name));
			} catch (COMM_FAILURE cf) {
				corbaDigitalIO = CorbaDigitalIOHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void setPositive2LineSync(String inputChannelName, String outputChannelName) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaDigitalIO.setPositive2LineSync(inputChannelName, outputChannelName);
				return;
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			} catch (TRANSIENT ct) {
				corbaDigitalIO = CorbaDigitalIOHelper.narrow(netService.reconnect(name));
			} catch (COMM_FAILURE cf) {
				corbaDigitalIO = CorbaDigitalIOHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public int getTwoLineSyncTimeout() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaDigitalIO.getTwoLineSyncTimeout();
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			} catch (TRANSIENT ct) {
				corbaDigitalIO = CorbaDigitalIOHelper.narrow(netService.reconnect(name));
			} catch (COMM_FAILURE cf) {
				corbaDigitalIO = CorbaDigitalIOHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void setTwoLineSyncTimeout(int msecs) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaDigitalIO.setTwoLineSyncTimeout(msecs);
				return;
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			} catch (TRANSIENT ct) {
				corbaDigitalIO = CorbaDigitalIOHelper.narrow(netService.reconnect(name));
			} catch (COMM_FAILURE cf) {
				corbaDigitalIO = CorbaDigitalIOHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public int getEdgeSyncDelayTime() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaDigitalIO.getEdgeSyncDelayTime();
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			} catch (TRANSIENT ct) {
				corbaDigitalIO = CorbaDigitalIOHelper.narrow(netService.reconnect(name));
			} catch (COMM_FAILURE cf) {
				corbaDigitalIO = CorbaDigitalIOHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void setEdgeSyncDelayTime(int msecs) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaDigitalIO.setEdgeSyncDelayTime(msecs);
				return;
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			} catch (TRANSIENT ct) {
				corbaDigitalIO = CorbaDigitalIOHelper.narrow(netService.reconnect(name));
			} catch (COMM_FAILURE cf) {
				corbaDigitalIO = CorbaDigitalIOHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}
}
