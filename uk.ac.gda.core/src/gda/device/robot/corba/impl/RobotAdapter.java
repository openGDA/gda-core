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

package gda.device.robot.corba.impl;

import gda.device.Device;
import gda.device.DeviceException;
import gda.device.Robot;
import gda.device.Scannable;
import gda.device.corba.CorbaDeviceException;
import gda.device.robot.SampleState;
import gda.device.robot.corba.CorbaRobot;
import gda.device.robot.corba.CorbaRobotHelper;
import gda.device.scannable.ScannableUtils;
import gda.device.scannable.corba.impl.ScannableAdapter;
import gda.factory.Findable;
import gda.factory.corba.util.NetService;

import java.io.Serializable;

import org.omg.CORBA.COMM_FAILURE;
import org.omg.CORBA.TRANSIENT;
import org.python.core.PyObject;

/**
 * A client side implementation of the adapter pattern for the motor class
 */
public class RobotAdapter extends ScannableAdapter implements Robot, Scannable, Device, Findable {
	private CorbaRobot corbaRobot;

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
	public RobotAdapter(org.omg.CORBA.Object obj, String name, NetService netService) {
		super(obj, name, netService);
		corbaRobot = CorbaRobotHelper.narrow(obj);
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
				corbaRobot.asynchronousMoveTo(any);
				return;
			} catch (COMM_FAILURE cf) {
				corbaRobot = CorbaRobotHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaRobot = CorbaRobotHelper.narrow(netService.reconnect(name));
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
				org.omg.CORBA.Any any = corbaRobot.getPosition();
				return any.extract_Value();
			} catch (COMM_FAILURE cf) {
				corbaRobot = CorbaRobotHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaRobot = CorbaRobotHelper.narrow(netService.reconnect(name));
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
				return corbaRobot.isBusy();
			} catch (COMM_FAILURE cf) {
				corbaRobot = CorbaRobotHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaRobot = CorbaRobotHelper.narrow(netService.reconnect(name));
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
				corbaRobot.atScanStart();
			} catch (COMM_FAILURE cf) {
				corbaRobot = CorbaRobotHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaRobot = CorbaRobotHelper.narrow(netService.reconnect(name));
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
				corbaRobot.atScanEnd();
			} catch (COMM_FAILURE cf) {
				corbaRobot = CorbaRobotHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaRobot = CorbaRobotHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void clearSample() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaRobot.clearSample();
				return;
			} catch (COMM_FAILURE cf) {
				corbaRobot = CorbaRobotHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaRobot = CorbaRobotHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}

		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void finish() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaRobot.finish();
			} catch (COMM_FAILURE cf) {
				corbaRobot = CorbaRobotHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaRobot = CorbaRobotHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}

		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public String getError() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaRobot.getError();
			} catch (COMM_FAILURE cf) {
				corbaRobot = CorbaRobotHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaRobot = CorbaRobotHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}

		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public double getSamplePosition() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaRobot.getSamplePosition();
			} catch (COMM_FAILURE cf) {
				corbaRobot = CorbaRobotHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaRobot = CorbaRobotHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}

		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public SampleState getSampleState() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				SampleState state = SampleState.from_int(corbaRobot.getSampleState().value());
				return state;
			} catch (COMM_FAILURE cf) {
				corbaRobot = CorbaRobotHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaRobot = CorbaRobotHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}

		}
		throw new DeviceException("Communication failure: retry faithrow ");
	}

	@Override
	public void nextSample() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaRobot.next();
				return;
			} catch (COMM_FAILURE cf) {
				corbaRobot = CorbaRobotHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaRobot = CorbaRobotHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void nextSample(double n) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaRobot.nextSample(n);
				return;
			} catch (COMM_FAILURE cf) {
				corbaRobot = CorbaRobotHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaRobot = CorbaRobotHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void recover() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaRobot.recover();
				return;
			} catch (COMM_FAILURE cf) {
				corbaRobot = CorbaRobotHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaRobot = CorbaRobotHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void start() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaRobot.start();
				return;
			} catch (COMM_FAILURE cf) {
				corbaRobot = CorbaRobotHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaRobot = CorbaRobotHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}
}
