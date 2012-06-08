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
import gda.device.corba.impl.DeviceImpl;
import gda.device.timer.corba.CorbaTimerPOA;
import gda.factory.corba.CorbaFactoryException;

/**
 * A server side implementation for a distributed Timer class
 */
public class TimerImpl extends CorbaTimerPOA {
	// Private reference to implementation object
	private Timer timer;

	private DeviceImpl deviceImpl;

	// Private reference to POA
	private org.omg.PortableServer.POA poa;

	/**
	 * Create server side implementation to the CORBA package.
	 * 
	 * @param timer
	 *            the Timer implementation object
	 * @param poa
	 *            the portable object adapter
	 */
	public TimerImpl(Timer timer, org.omg.PortableServer.POA poa) {
		this.timer = timer;
		this.poa = poa;
		deviceImpl = new DeviceImpl(timer, poa);
	}

	/**
	 * Get the implementation object
	 * 
	 * @return the Timer implementation object
	 */
	public Timer _delegate() {
		return timer;
	}

	/**
	 * Set the implementation object.
	 * 
	 * @param timer
	 *            set the Timer implementation object
	 */
	public void _delegate(Timer timer) {
		this.timer = timer;
	}

	@Override
	public org.omg.PortableServer.POA _default_POA() {
		return (poa != null) ? poa : super._default_POA();
	}

	@Override
	public int getStatus() throws CorbaDeviceException {
		try {
			return timer.getStatus();
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public int getMaximumFrames() throws CorbaDeviceException {
		try {
			return timer.getMaximumFrames();
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public int getCurrentFrame() throws CorbaDeviceException {
		try {
			return timer.getCurrentFrame();
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public int getCurrentCycle() throws CorbaDeviceException {
		try {
			return timer.getCurrentCycle();
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public void setCycles(int cycles) throws CorbaDeviceException {
		try {
			timer.setCycles(cycles);
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public void start() throws CorbaDeviceException {
		try {
			timer.start();
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public void stop() throws CorbaDeviceException {
		try {
			timer.stop();
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public void restart() throws CorbaDeviceException {
		try {
			timer.restart();
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public void addFrameSet(int frameCount, double requestedDeadTime, double requestedLiveTime)
			throws CorbaDeviceException {
		try {
			timer.addFrameSet(frameCount, requestedDeadTime, requestedLiveTime);
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public void addFrameSet2(int frameCount, double requestedDeadTime, double requestedLiveTime, int deadPort,
			int livePort, int deadPause, int livePause) throws CorbaDeviceException {
		try {
			timer.addFrameSet(frameCount, requestedDeadTime, requestedLiveTime, deadPort, livePort, deadPause,
					livePause);
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public void clearFrameSets() throws CorbaDeviceException {
		try {
			timer.clearFrameSets();
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public void loadFrameSets() throws CorbaDeviceException {
		try {
			timer.loadFrameSets();
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public void countAsync(double time) throws CorbaDeviceException {
		try {
			timer.countAsync(time);
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public void output(String file) throws CorbaDeviceException {
		try {
			timer.output(file);
			return;
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public void setAttribute(String attributeName, org.omg.CORBA.Any value) throws CorbaDeviceException {
		deviceImpl.setAttribute(attributeName, value);
	}

	@Override
	public org.omg.CORBA.Any getAttribute(String attributeName) throws CorbaDeviceException {
		return deviceImpl.getAttribute(attributeName);
	}

	@Override
	public void reconfigure() throws CorbaFactoryException {
		deviceImpl.reconfigure();
	}

	@Override
	public void close() throws CorbaDeviceException {
		deviceImpl.close();
	}
	
	@Override
	public int getProtectionLevel() throws CorbaDeviceException {
		return deviceImpl.getProtectionLevel();
	}

	@Override
	public void setProtectionLevel(int newLevel) throws CorbaDeviceException {
		deviceImpl.setProtectionLevel(newLevel);
	}
}
