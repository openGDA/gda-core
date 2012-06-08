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

package gda.device.timer.corba.impl;

import gda.device.DeviceException;
import gda.device.Timer;
import gda.device.corba.CorbaDeviceException;
import gda.device.corba.impl.DeviceAdapter;
import gda.device.timer.corba.CorbaTimer;
import gda.device.timer.corba.CorbaTimerHelper;
import gda.factory.Findable;
import gda.factory.corba.util.NetService;

import org.omg.CORBA.COMM_FAILURE;
import org.omg.CORBA.TRANSIENT;

/**
 * A client side implementation of the adapter pattern for the timer class
 */
public class TimerAdapter extends DeviceAdapter implements Timer, Findable {
	private CorbaTimer corbaTimer;

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
	public TimerAdapter(org.omg.CORBA.Object obj, String name, NetService netService) {
		super(obj, name, netService);
		corbaTimer = CorbaTimerHelper.narrow(obj);
		this.netService = netService;
		this.name = name;
	}

	@Override
	public int getStatus() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaTimer.getStatus();
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			} catch (COMM_FAILURE cf) {
				corbaTimer = CorbaTimerHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaTimer = CorbaTimerHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public int getMaximumFrames() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaTimer.getMaximumFrames();
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			} catch (COMM_FAILURE cf) {
				corbaTimer = CorbaTimerHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaTimer = CorbaTimerHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public int getCurrentFrame() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaTimer.getCurrentFrame();
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			} catch (COMM_FAILURE cf) {
				corbaTimer = CorbaTimerHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaTimer = CorbaTimerHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public int getCurrentCycle() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaTimer.getCurrentCycle();
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			} catch (COMM_FAILURE cf) {
				corbaTimer = CorbaTimerHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaTimer = CorbaTimerHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void setCycles(int cycles) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaTimer.setCycles(cycles);
				return;
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			} catch (COMM_FAILURE cf) {
				corbaTimer = CorbaTimerHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaTimer = CorbaTimerHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void start() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaTimer.start();
				return;
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			} catch (COMM_FAILURE cf) {
				corbaTimer = CorbaTimerHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaTimer = CorbaTimerHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void stop() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaTimer.stop();
				return;
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			} catch (COMM_FAILURE cf) {
				corbaTimer = CorbaTimerHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaTimer = CorbaTimerHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void restart() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaTimer.restart();
				return;
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			} catch (COMM_FAILURE cf) {
				corbaTimer = CorbaTimerHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaTimer = CorbaTimerHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void addFrameSet(int frameCount, double requestedDeadTime, double requestedLiveTime) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaTimer.addFrameSet(frameCount, requestedDeadTime, requestedLiveTime);
				return;
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			} catch (COMM_FAILURE cf) {
				corbaTimer = CorbaTimerHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaTimer = CorbaTimerHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void addFrameSet(int frameCount, double requestedDeadTime, double requestedLiveTime, int deadPort,
			int livePort, int deadPause, int livePause) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaTimer.addFrameSet2(frameCount, requestedDeadTime, requestedLiveTime, deadPort, livePort,
						deadPause, livePause);
				return;
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			} catch (COMM_FAILURE cf) {
				corbaTimer = CorbaTimerHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaTimer = CorbaTimerHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void clearFrameSets() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaTimer.clearFrameSets();
				return;
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			} catch (COMM_FAILURE cf) {
				corbaTimer = CorbaTimerHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaTimer = CorbaTimerHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void loadFrameSets() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaTimer.loadFrameSets();
				return;
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			} catch (COMM_FAILURE cf) {
				corbaTimer = CorbaTimerHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaTimer = CorbaTimerHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void countAsync(double time) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaTimer.countAsync(time);
				return;
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			} catch (COMM_FAILURE cf) {
				corbaTimer = CorbaTimerHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaTimer = CorbaTimerHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void output(String file) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaTimer.output(file);
				return;
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			} catch (COMM_FAILURE cf) {
				corbaTimer = CorbaTimerHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaTimer = CorbaTimerHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}
}
