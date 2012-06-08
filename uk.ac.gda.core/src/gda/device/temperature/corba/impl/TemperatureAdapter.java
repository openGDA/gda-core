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

package gda.device.temperature.corba.impl;

import gda.device.DeviceException;
import gda.device.Temperature;
import gda.device.TemperatureRamp;
import gda.device.corba.CorbaDeviceException;
import gda.device.scannable.ScannableUtils;
import gda.device.scannable.corba.impl.ScannableAdapter;
import gda.device.temperature.corba.CorbaTemperature;
import gda.device.temperature.corba.CorbaTemperatureHelper;
import gda.device.temperature.corba.CorbaTemperatureRamp;
import gda.factory.Findable;
import gda.factory.corba.util.NetService;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

import org.omg.CORBA.COMM_FAILURE;
import org.omg.CORBA.TRANSIENT;
import org.python.core.PyObject;

/**
 * A client side implementation of the adapter pattern for the Temperature class
 */
public class TemperatureAdapter extends ScannableAdapter implements Temperature, Findable {
	private CorbaTemperature corbaTemperature;

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
	public TemperatureAdapter(org.omg.CORBA.Object obj, String name, NetService netService) {
		super(obj, name, netService);
		corbaTemperature = CorbaTemperatureHelper.narrow(obj);
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
				corbaTemperature.asynchronousMoveTo(any);
				return;
			} catch (COMM_FAILURE cf) {
				corbaTemperature = CorbaTemperatureHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaTemperature = CorbaTemperatureHelper.narrow(netService.reconnect(name));
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
				org.omg.CORBA.Any any = corbaTemperature.getPosition();
				return any.extract_Value();
			} catch (COMM_FAILURE cf) {
				corbaTemperature = CorbaTemperatureHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaTemperature = CorbaTemperatureHelper.narrow(netService.reconnect(name));
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
				return corbaTemperature.isBusy();
			} catch (COMM_FAILURE cf) {
				corbaTemperature = CorbaTemperatureHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaTemperature = CorbaTemperatureHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public double getCurrentTemperature() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaTemperature.getCurrentTemperature();
			} catch (COMM_FAILURE cf) {
				corbaTemperature = CorbaTemperatureHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaTemperature = CorbaTemperatureHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException e) {
				throw new DeviceException(e.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void setTargetTemperature(double temp) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaTemperature.setTargetTemperature(temp);
				return;
			} catch (COMM_FAILURE cf) {
				corbaTemperature = CorbaTemperatureHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaTemperature = CorbaTemperatureHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException e) {
				throw new DeviceException(e.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public double getTargetTemperature() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaTemperature.getTargetTemperature();
			} catch (COMM_FAILURE cf) {
				corbaTemperature = CorbaTemperatureHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaTemperature = CorbaTemperatureHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException e) {
				throw new DeviceException(e.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public double getUpperTemp() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaTemperature.getUpperTemp();
			} catch (COMM_FAILURE cf) {
				corbaTemperature = CorbaTemperatureHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaTemperature = CorbaTemperatureHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException e) {
				throw new DeviceException(e.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public double getLowerTemp() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaTemperature.getLowerTemp();
			} catch (COMM_FAILURE cf) {
				corbaTemperature = CorbaTemperatureHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaTemperature = CorbaTemperatureHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException e) {
				throw new DeviceException(e.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public ArrayList<String> getProbeNames() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				String[] result = corbaTemperature.getProbeNames();
				return new ArrayList<String>(Arrays.asList(result));
			} catch (COMM_FAILURE cf) {
				corbaTemperature = CorbaTemperatureHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaTemperature = CorbaTemperatureHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException e) {
				throw new DeviceException(e.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void setProbe(String probeName) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaTemperature.setProbe(probeName);
				return;
			} catch (COMM_FAILURE cf) {
				corbaTemperature = CorbaTemperatureHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaTemperature = CorbaTemperatureHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException e) {
				throw new DeviceException(e.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public boolean isAtTargetTemperature() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaTemperature.isAtTargetTemperature();
			} catch (COMM_FAILURE cf) {
				corbaTemperature = CorbaTemperatureHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaTemperature = CorbaTemperatureHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException e) {
				throw new DeviceException(e.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void setLowerTemp(double lowLimit) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaTemperature.setLowerTemp(lowLimit);
				return;
			} catch (COMM_FAILURE cf) {
				corbaTemperature = CorbaTemperatureHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaTemperature = CorbaTemperatureHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException e) {
				throw new DeviceException(e.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void setUpperTemp(double upperLimit) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaTemperature.setUpperTemp(upperLimit);
				return;
			} catch (COMM_FAILURE cf) {
				corbaTemperature = CorbaTemperatureHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaTemperature = CorbaTemperatureHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException e) {
				throw new DeviceException(e.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void waitForTemp() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaTemperature.waitForTemp();
				return;
			} catch (COMM_FAILURE cf) {
				corbaTemperature = CorbaTemperatureHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaTemperature = CorbaTemperatureHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException e) {
				throw new DeviceException(e.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void clearRamps() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaTemperature.clearRamps();
				return;
			} catch (COMM_FAILURE cf) {
				corbaTemperature = CorbaTemperatureHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaTemperature = CorbaTemperatureHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException e) {
				throw new DeviceException(e.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void addRamp(TemperatureRamp ramp) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaTemperature.addRamp(new CorbaTemperatureRamp(ramp.getRampNumber(), ramp.getStartTemperature(),
						ramp.getEndTemperature(), ramp.getRate(), ramp.getDwellTime(), ramp.getCoolingSpeed()));
				return;
			} catch (COMM_FAILURE cf) {
				corbaTemperature = CorbaTemperatureHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaTemperature = CorbaTemperatureHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException e) {
				throw new DeviceException(e.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void setRamps(ArrayList<TemperatureRamp> rampList) throws DeviceException {
		for (int j = 0; j < NetService.RETRY; j++) {
			try {
				int nramps = rampList.size();
				CorbaTemperatureRamp[] ramps = new CorbaTemperatureRamp[nramps];
				for (int i = 0; i < nramps; i++) {
					TemperatureRamp ramp = rampList.get(i);
					ramps[i] = new CorbaTemperatureRamp(ramp.getRampNumber(), ramp.getStartTemperature(), ramp
							.getEndTemperature(), ramp.getRate(), ramp.getDwellTime(), ramp.getCoolingSpeed());
				}
				corbaTemperature.setRamps(ramps);
				return;
			} catch (COMM_FAILURE cf) {
				corbaTemperature = CorbaTemperatureHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaTemperature = CorbaTemperatureHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException e) {
				throw new DeviceException(e.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void start() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaTemperature.start();
				return;
			} catch (COMM_FAILURE cf) {
				corbaTemperature = CorbaTemperatureHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaTemperature = CorbaTemperatureHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException e) {
				throw new DeviceException(e.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void stop() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaTemperature.stop();
				return;
			} catch (COMM_FAILURE cf) {
				corbaTemperature = CorbaTemperatureHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaTemperature = CorbaTemperatureHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException e) {
				throw new DeviceException(e.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void hold() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaTemperature.hold();
				return;
			} catch (COMM_FAILURE cf) {
				corbaTemperature = CorbaTemperatureHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaTemperature = CorbaTemperatureHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException e) {
				throw new DeviceException(e.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public boolean isRunning() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaTemperature.isRunning();
			} catch (COMM_FAILURE cf) {
				corbaTemperature = CorbaTemperatureHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaTemperature = CorbaTemperatureHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException e) {
				throw new DeviceException(e.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void end() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaTemperature.end();
				return;
			} catch (COMM_FAILURE cf) {
				corbaTemperature = CorbaTemperatureHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaTemperature = CorbaTemperatureHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException e) {
				throw new DeviceException(e.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");

	}

	@Override
	public void begin() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaTemperature.begin();
				return;
			} catch (COMM_FAILURE cf) {
				corbaTemperature = CorbaTemperatureHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaTemperature = CorbaTemperatureHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException e) {
				throw new DeviceException(e.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");

	}

	@Override
	public double getRampRate() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaTemperature.getRampRate();
			} catch (COMM_FAILURE cf) {
				corbaTemperature = CorbaTemperatureHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaTemperature = CorbaTemperatureHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException e) {
				throw new DeviceException(e.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void setRampRate(double rate) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaTemperature.setRampRate(rate);
				return;
			} catch (COMM_FAILURE cf) {
				corbaTemperature = CorbaTemperatureHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaTemperature = CorbaTemperatureHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException e) {
				throw new DeviceException(e.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");

	}
}
