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

package gda.device.amplifier.corba.impl;

import gda.device.Amplifier;
import gda.device.DeviceException;
import gda.device.amplifier.corba.CorbaAmplifier;
import gda.device.amplifier.corba.CorbaAmplifierHelper;
import gda.device.corba.CorbaDeviceException;
import gda.device.corba.impl.DeviceAdapter;
import gda.factory.corba.util.NetService;

import org.omg.CORBA.COMM_FAILURE;
import org.omg.CORBA.TRANSIENT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A client side implementation of the adapter pattern for the Amplifier class
 */
public class AmplifierAdapter extends DeviceAdapter implements Amplifier {
	
	private static final Logger logger = LoggerFactory.getLogger(AmplifierAdapter.class);
	
	private CorbaAmplifier corbaAmplifier;
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
	public AmplifierAdapter(org.omg.CORBA.Object obj, String name, NetService netService) {
		super(obj, name, netService);
		logger.debug("Amplifier adapter created");
		corbaAmplifier = CorbaAmplifierHelper.narrow(obj);
		this.netService = netService;
		this.name = name;
	}

	@Override
	public void autoCurrentSuppress() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaAmplifier.autoCurrentSuppress();
				return;
			} catch (COMM_FAILURE cf) {
				corbaAmplifier = CorbaAmplifierHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaAmplifier = CorbaAmplifierHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException e) {
				throw new DeviceException(e.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void autoZeroCorrect() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaAmplifier.autoZeroCorrect();
				return;
			} catch (COMM_FAILURE cf) {
				corbaAmplifier = CorbaAmplifierHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaAmplifier = CorbaAmplifierHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException e) {
				throw new DeviceException(e.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public double getCurrentSuppressValue() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaAmplifier.getCurrentSuppressValue();
			} catch (COMM_FAILURE cf) {
				corbaAmplifier = CorbaAmplifierHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaAmplifier = CorbaAmplifierHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException e) {
				throw new DeviceException(e.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public double getFilterRiseTime() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaAmplifier.getFilterRiseTime();
			} catch (COMM_FAILURE cf) {
				corbaAmplifier = CorbaAmplifierHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaAmplifier = CorbaAmplifierHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException e) {
				throw new DeviceException(e.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public double getGain() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaAmplifier.getGain();
			} catch (COMM_FAILURE cf) {
				corbaAmplifier = CorbaAmplifierHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaAmplifier = CorbaAmplifierHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException e) {
				throw new DeviceException(e.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public String getStatus() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaAmplifier.getStatus();
			} catch (COMM_FAILURE cf) {
				corbaAmplifier = CorbaAmplifierHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaAmplifier = CorbaAmplifierHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException e) {
				throw new DeviceException(e.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public double getVoltageBias() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaAmplifier.getVoltageBias();
			} catch (COMM_FAILURE cf) {
				corbaAmplifier = CorbaAmplifierHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaAmplifier = CorbaAmplifierHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException e) {
				throw new DeviceException(e.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void setAutoFilter(boolean onOff) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaAmplifier.setAutoFilter(onOff);
				return;
			} catch (COMM_FAILURE cf) {
				corbaAmplifier = CorbaAmplifierHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaAmplifier = CorbaAmplifierHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException e) {
				throw new DeviceException(e.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void setCurrentSuppress(boolean onOff) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaAmplifier.setCurrentSuppress(onOff);
				return;
			} catch (COMM_FAILURE cf) {
				corbaAmplifier = CorbaAmplifierHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaAmplifier = CorbaAmplifierHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException e) {
				throw new DeviceException(e.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void setCurrentSuppressionParams(double value) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaAmplifier.setCurrentSuppressionParams(value);
				return;
			} catch (COMM_FAILURE cf) {
				corbaAmplifier = CorbaAmplifierHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaAmplifier = CorbaAmplifierHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException e) {
				throw new DeviceException(e.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void setCurrentSuppressionParams(double value, int range) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaAmplifier.setCurrentSuppressionParams2(value, range);
				return;
			} catch (COMM_FAILURE cf) {
				corbaAmplifier = CorbaAmplifierHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaAmplifier = CorbaAmplifierHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException e) {
				throw new DeviceException(e.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void setEnlargeGain(boolean onOff) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaAmplifier.setEnlargeGain(onOff);
				return;
			} catch (COMM_FAILURE cf) {
				corbaAmplifier = CorbaAmplifierHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaAmplifier = CorbaAmplifierHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException e) {
				throw new DeviceException(e.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void setFilter(boolean onOff) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaAmplifier.setFilter(onOff);
				return;
			} catch (COMM_FAILURE cf) {
				corbaAmplifier = CorbaAmplifierHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaAmplifier = CorbaAmplifierHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException e) {
				throw new DeviceException(e.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void setFilterRiseTime(int level) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaAmplifier.setFilterRiseTime(level);
				return;
			} catch (COMM_FAILURE cf) {
				corbaAmplifier = CorbaAmplifierHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaAmplifier = CorbaAmplifierHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException e) {
				throw new DeviceException(e.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void setGain(int level) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaAmplifier.setGain(level);
				return;
			} catch (COMM_FAILURE cf) {
				corbaAmplifier = CorbaAmplifierHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaAmplifier = CorbaAmplifierHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException e) {
				throw new DeviceException(e.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void setVoltageBias(boolean voltageBias) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaAmplifier.setVoltageBias(voltageBias);
				return;
			} catch (COMM_FAILURE cf) {
				corbaAmplifier = CorbaAmplifierHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaAmplifier = CorbaAmplifierHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException e) {
				throw new DeviceException(e.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void setVoltageBias(double value) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaAmplifier.setVoltageBias2(value);
				return;
			} catch (COMM_FAILURE cf) {
				corbaAmplifier = CorbaAmplifierHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaAmplifier = CorbaAmplifierHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException e) {
				throw new DeviceException(e.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void setZeroCheck(boolean onOff) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaAmplifier.setZeroCheck(onOff);
				return;
			} catch (COMM_FAILURE cf) {
				corbaAmplifier = CorbaAmplifierHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaAmplifier = CorbaAmplifierHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException e) {
				throw new DeviceException(e.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}
}
