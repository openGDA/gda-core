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

package gda.device.timer;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.DeviceData;
import gda.device.DeviceException;
import gda.device.TangoDeviceProxy;
import gda.device.Timer;
import gda.factory.FactoryException;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * A timer class for the VME time frame generator card
 */
public class TangoTfg1 extends TimerBase implements InitializingBean {

	private static final Logger logger = LoggerFactory.getLogger(TangoTfg1.class);
	protected TangoDeviceProxy tangoDeviceProxy;
	private int version;
	
	@Override
	public void configure() throws FactoryException {
		try {
			tangoDeviceProxy.isAvailable();
			super.configure();
			configured = true;
		} catch(DeviceException e) {
			throw new FactoryException(e.getMessage(), e);
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (tangoDeviceProxy == null) {
			throw new IllegalArgumentException("TangoDeviceProxy is not set");
		}
	}

	/**
	 * @return Returns the Tango device proxy.
	 */
	public TangoDeviceProxy getTangoDeviceProxy() {
		return tangoDeviceProxy;
	}

	/**
	 * @param dev The Tango device proxy to set.
	 */
	public void setTangoDeviceProxy(TangoDeviceProxy dev) {
		this.tangoDeviceProxy = dev;
	}

	@Override
	public int getStatus() throws DeviceException {
		int state = Timer.IDLE;
		String status = null;
		try {
			String statusCommand = isShowArmed() ? "ArmedStatus" : "AcqStatus";
			tangoDeviceProxy.isAvailable();
			status = tangoDeviceProxy.getAttributeAsString(statusCommand);
			if ("RUNNING".equals(status)) {
				state = Timer.ACTIVE;
			} else if ("PAUSED".equals(status)) {
				state = Timer.PAUSED;
			} else if ("EXT-ARMED".equals(status)) {
				state = Timer.ARMED;
			}
		} catch (DevFailed e) {
			DeviceException ex = new DeviceException(e.errors[0].desc);
			logger.error(ex.getMessage());
		}
		return state;
	}

	@Override
	protected String getAcqStatus() throws DeviceException {
		String status = "IDLE";
		try {
			tangoDeviceProxy.isAvailable();
			status = tangoDeviceProxy.getAttributeAsString("AcqStatus");
		} catch (DevFailed e) {
			DeviceException ex = new DeviceException(e.errors[0].desc);
			logger.error(ex.getMessage());
		}
		return status;
	}

	@Override
	public void stop() throws DeviceException {
		waitingForExtStart = false;
		try {
			tangoDeviceProxy.isAvailable();
			tangoDeviceProxy.command_inout("stop");
		} catch (DevFailed e) {
			DeviceException ex = new DeviceException(e.errors[0].desc);
			logger.error(ex.getMessage());
			throw ex;
		}
	}

	@Override
	public int getMaximumFrames() throws DeviceException {
		int maxFrames = 0;
		try {
			tangoDeviceProxy.isAvailable();
			maxFrames = tangoDeviceProxy.getAttributeAsInt("maximumFrames");
		} catch (DevFailed e) {
			DeviceException ex = new DeviceException(e.errors[0].desc);
			logger.error(ex.getMessage());
			throw ex;
		}
		return maxFrames;
	}

	public void getVersion() throws DeviceException {
		try {
			tangoDeviceProxy.isAvailable();
			version = tangoDeviceProxy.getAttributeAsInt("version");
		} catch (DevFailed e) {
			DeviceException ex = new DeviceException(e.errors[0].desc);
			logger.error(ex.getMessage());
			throw ex;
		}
	}

	@Override
	public int getCurrentFrame() throws DeviceException {
		int frame = 0;
		try {
			tangoDeviceProxy.isAvailable();
			frame = tangoDeviceProxy.getAttributeAsInt("CurrentFrame");
		} catch (DevFailed e) {
			DeviceException ex = new DeviceException(e.errors[0].desc);
			logger.error(ex.getMessage());
		}
		return frame;
	}

	@Override
	public int getCurrentCycle() throws DeviceException {
		int cycle = 0;
		try {
			tangoDeviceProxy.isAvailable();
			cycle = tangoDeviceProxy.getAttributeAsInt("CurrentLap");
		} catch (DevFailed e) {
			DeviceException ex = new DeviceException(e.errors[0].desc);
			logger.error(ex.getMessage());
		}
		return cycle;
	}

	@Override
	public void restart() throws DeviceException {
		if (!framesLoaded) {
			throw new DeviceException(getName() + " no frames loaded");
		}
		try {
			tangoDeviceProxy.isAvailable();
			tangoDeviceProxy.command_inout("cont");
		} catch (DevFailed e) {
			DeviceException ex = new DeviceException(e.errors[0].desc);
			logger.error(ex.getMessage());
			throw ex;
		}
	}

	@Override
	public synchronized void start() throws DeviceException {
		if (!framesLoaded) {
			throw new DeviceException(getName() + " no frames loaded");
		}
		try {
			tangoDeviceProxy.isAvailable();
			if (!extStart) {
				tangoDeviceProxy.command_inout("start");
			} else {
				tangoDeviceProxy.command_inout("arm");
				waitingForExtStart = true;
			}
		} catch (DevFailed e) {
			DeviceException ex = new DeviceException(e.errors[0].desc);
			logger.error(ex.getMessage());
			throw ex;
		}
		Date d = new Date();
		startTime = d.getTime();
		elapsedTime = 0;
		started = true;
		notify();
	}

	@Override
	public void loadFrameSets() throws DeviceException {
		String[] argin = new String[timeFrameProfile.size()];
		totalExptTime = 0;
		totalFrames = 0;
		int index = 0;
		
		for (FrameSet frameSet : timeFrameProfile) {
			totalExptTime += (int) (frameSet.getRequestedLiveTime() + frameSet.getRequestedDeadTime())
					* frameSet.getFrameCount();
			totalFrames += frameSet.getFrameCount();
			argin[index++] = "" + frameSet.getFrameCount() + " " + frameSet.getRequestedDeadTime() / 1000 + " "
					+ frameSet.getRequestedLiveTime() / 1000 + " " + frameSet.getDeadPort() + " "
					+ frameSet.getLivePort() + " " + frameSet.getDeadPause() + " " + frameSet.getLivePause();
		}
		try {
			DeviceData args = new DeviceData();
			args.insert(argin);
			tangoDeviceProxy.isAvailable();
			tangoDeviceProxy.setAttribute("laps", cycles);
			tangoDeviceProxy.setAttribute("externalStart", (extStart) ? true : false);
			tangoDeviceProxy.setAttribute("externalInhibit", (extInh) ? true : false);
			tangoDeviceProxy.command_inout("setupGroups", args);
		} catch (DevFailed e) {
			DeviceException ex = new DeviceException(e.errors[0].desc);
			logger.error(ex.getMessage());
			throw ex;
		}
		totalExptTime *= cycles;
		framesLoaded = true;
		notifyIObservers(this, timeFrameProfile);
	}


/**
 * Count the specified time (in ms)
 */
	@Override
	public synchronized void countAsync(double time) throws DeviceException {
		totalExptTime = (int) time * cycles;
		try {
			double[] argin = new double[4];
			argin[0] = 1; // frames
			argin[1] = 0.001; // dead time (seconds)
			argin[2] = time / 1000.0; // live time (seconds)
			argin[3] = 0; // pause
			DeviceData args = new DeviceData();
			args.insert(argin);
			tangoDeviceProxy.isAvailable();
			tangoDeviceProxy.setAttribute("laps", 1);
			tangoDeviceProxy.command_inout("generate", args);
			tangoDeviceProxy.command_inout("start");
		} catch (DevFailed e) {
			DeviceException ex = new DeviceException(e.errors[0].desc);
			logger.error(ex.getMessage());
		}
		Date d = new Date();
		startTime = d.getTime();
		elapsedTime = 0;
		started = true;
		notify();
	}

	@Override
	public void setAttribute(String attributeName, Object value) throws DeviceException {
		if (EXT_START_ATTR_NAME.equals(attributeName)) {
			extStart = ((Boolean) value).booleanValue();
		} else if (EXT_INHIBIT_ATTR_NAME.equals(attributeName)) {
			extInh = ((Boolean) value).booleanValue();
		}
	}

	@Override
	public Object getAttribute(String attributeName) throws DeviceException {
		Object obj = null;
		if (EXT_START_ATTR_NAME.equals(attributeName)) {
			obj = new Boolean(extStart);
		} else if (EXT_INHIBIT_ATTR_NAME.equals(attributeName)) {
			obj = new Boolean(extInh);
		} else if ("TotalFrames".equals(attributeName)) {
			obj = getTotalFrames();
		} else if ("TotalExptTime".equals(attributeName)) {
			obj = getTotalExptTime();
		} else if ("Version".equals(attributeName)) {
			return version;
		} else if ("FrameSets".equals(attributeName)) {
			return getFramesets();
		} else if ("FramesLoaded".equals(attributeName)) {
			return framesLoaded;
		} else if ("Cycles".equals(attributeName)) {
			return cycles;
		}
		return obj;
	}

	@Override
	public void output(String file) throws DeviceException {
		// not implemented/not required
		
	}
}