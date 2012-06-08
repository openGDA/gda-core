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

package gda.device.detector.countertimer.corba.impl;

import gda.device.CounterTimer;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.corba.CorbaDeviceException;
import gda.device.detector.corba.impl.DetectorAdapter;
import gda.device.detector.countertimer.corba.CorbaCounterTimer;
import gda.device.detector.countertimer.corba.CorbaCounterTimerHelper;
import gda.factory.Findable;
import gda.factory.corba.util.NetService;

import org.omg.CORBA.COMM_FAILURE;
import org.omg.CORBA.TRANSIENT;

/**
 * A client side implementation of the adapter pattern for the CounterTimer class
 */
public class CountertimerAdapter extends DetectorAdapter implements CounterTimer, Detector, Findable, Scannable {
	private CorbaCounterTimer corbaCounterTimer;

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
	public CountertimerAdapter(org.omg.CORBA.Object obj, String name, NetService netService) {
		super(obj, name, netService);
		corbaCounterTimer = CorbaCounterTimerHelper.narrow(obj);
	}

	@Override
	public int getMaximumFrames() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaCounterTimer.getMaximumFrames();
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			} catch (TRANSIENT ct) {
				corbaCounterTimer = CorbaCounterTimerHelper.narrow(netService.reconnect(name));
			} catch (COMM_FAILURE cf) {
				corbaCounterTimer = CorbaCounterTimerHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public int getCurrentFrame() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaCounterTimer.getCurrentFrame();
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			} catch (TRANSIENT ct) {
				corbaCounterTimer = CorbaCounterTimerHelper.narrow(netService.reconnect(name));
			} catch (COMM_FAILURE cf) {
				corbaCounterTimer = CorbaCounterTimerHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public int getCurrentCycle() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaCounterTimer.getCurrentCycle();
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			} catch (TRANSIENT ct) {
				corbaCounterTimer = CorbaCounterTimerHelper.narrow(netService.reconnect(name));
			} catch (COMM_FAILURE cf) {
				corbaCounterTimer = CorbaCounterTimerHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void setCycles(int cycles) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaCounterTimer.setCycles(cycles);
				return;
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			} catch (TRANSIENT ct) {
				corbaCounterTimer = CorbaCounterTimerHelper.narrow(netService.reconnect(name));
			} catch (COMM_FAILURE cf) {
				corbaCounterTimer = CorbaCounterTimerHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void start() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaCounterTimer.start();
				return;
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			} catch (TRANSIENT ct) {
				corbaCounterTimer = CorbaCounterTimerHelper.narrow(netService.reconnect(name));
			} catch (COMM_FAILURE cf) {
				corbaCounterTimer = CorbaCounterTimerHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void stop() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaCounterTimer.stop();
				return;
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			} catch (TRANSIENT ct) {
				corbaCounterTimer = CorbaCounterTimerHelper.narrow(netService.reconnect(name));
			} catch (COMM_FAILURE cf) {
				corbaCounterTimer = CorbaCounterTimerHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void restart() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaCounterTimer.restart();
				return;
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			} catch (TRANSIENT ct) {
				corbaCounterTimer = CorbaCounterTimerHelper.narrow(netService.reconnect(name));
			} catch (COMM_FAILURE cf) {
				corbaCounterTimer = CorbaCounterTimerHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void addFrameSet(int frameCount, double requestedLiveTime, double requestedDeadTime) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaCounterTimer.addFrameSet(frameCount, requestedLiveTime, requestedDeadTime);
				return;
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			} catch (TRANSIENT ct) {
				corbaCounterTimer = CorbaCounterTimerHelper.narrow(netService.reconnect(name));
			} catch (COMM_FAILURE cf) {
				corbaCounterTimer = CorbaCounterTimerHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void addFrameSet(int frameCount, double requestedLiveTime, double requestedDeadTime, int deadPort,
			int livePort, int deadPause, int livePause) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaCounterTimer.addFrameSet2(frameCount, requestedLiveTime, requestedDeadTime, deadPort, livePort,
						deadPause, livePause);
				return;
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			} catch (TRANSIENT ct) {
				corbaCounterTimer = CorbaCounterTimerHelper.narrow(netService.reconnect(name));
			} catch (COMM_FAILURE cf) {
				corbaCounterTimer = CorbaCounterTimerHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void clearFrameSets() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaCounterTimer.clearFrameSets();
				return;
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			} catch (TRANSIENT ct) {
				corbaCounterTimer = CorbaCounterTimerHelper.narrow(netService.reconnect(name));
			} catch (COMM_FAILURE cf) {
				corbaCounterTimer = CorbaCounterTimerHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void loadFrameSets() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaCounterTimer.loadFrameSets();
				return;
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			} catch (TRANSIENT ct) {
				corbaCounterTimer = CorbaCounterTimerHelper.narrow(netService.reconnect(name));
			} catch (COMM_FAILURE cf) {
				corbaCounterTimer = CorbaCounterTimerHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public double[] readChannel(int startFrame, int frameCount, int channel) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaCounterTimer.readChannel(startFrame, frameCount, channel);
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			} catch (TRANSIENT ct) {
				corbaCounterTimer = CorbaCounterTimerHelper.narrow(netService.reconnect(name));
			} catch (COMM_FAILURE cf) {
				corbaCounterTimer = CorbaCounterTimerHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public double[] readFrame(int startChannel, int channelCount, int frame) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaCounterTimer.readFrame(startChannel, channelCount, frame);
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			} catch (TRANSIENT ct) {
				corbaCounterTimer = CorbaCounterTimerHelper.narrow(netService.reconnect(name));
			} catch (COMM_FAILURE cf) {
				corbaCounterTimer = CorbaCounterTimerHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public boolean isSlave() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaCounterTimer.isSlave();
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			} catch (TRANSIENT ct) {
				corbaCounterTimer = CorbaCounterTimerHelper.narrow(netService.reconnect(name));
			} catch (COMM_FAILURE cf) {
				corbaCounterTimer = CorbaCounterTimerHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void setSlave(boolean slave) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaCounterTimer.setSlave(slave);
				return;
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			} catch (TRANSIENT ct) {
				corbaCounterTimer = CorbaCounterTimerHelper.narrow(netService.reconnect(name));
			} catch (COMM_FAILURE cf) {
				corbaCounterTimer = CorbaCounterTimerHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}
}
