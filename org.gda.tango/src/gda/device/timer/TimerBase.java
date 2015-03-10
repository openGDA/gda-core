/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

import gda.device.DeviceBase;
import gda.device.DeviceException;
import gda.device.Timer;
import gda.device.TimerStatus;
import gda.factory.FactoryException;

import java.util.Date;
import java.util.List;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A timer class for the VME time frame generator card implemented using DA.Server
 */
public abstract class TimerBase extends DeviceBase implements Timer, Runnable {

	public static final String AUTO_CONTINUE_ATTR_NAME = "Auto-Continue";
	public static final String EXT_START_ATTR_NAME = "Ext-Start";
	public static final String AUTO_REARM_ATTR_NAME = "Auto-Rearm";
	public static final String EXT_INHIBIT_ATTR_NAME = "Ext-Inhibit";
	public static final String SOFTWARE_START_AND_TRIG_ATTR_NAME = "software triggering";

	private static final Logger logger = LoggerFactory.getLogger(Tfg.class);

	//if true that tfg.start sends "tfg arm", otherwise sends "tfg start"
	protected boolean extStart = false;
	// if true then send tfg arm, tfg start. For the system to work correctly in 
	// this situation then there must be a pause in every frame, including the first frame.
	protected boolean softwareTriggering = false;
	
	protected boolean extInh = false;
	protected int cycles = 1;
	protected int totalCycles = 0;
	protected int totalFrames = 0;
	protected Vector<FrameSet> timeFrameProfile = new Vector<FrameSet>();
	protected Thread runner;
	protected boolean started = false;
	protected long startTime = 0;
	protected long totalExptTime;
	protected long elapsedTime = 0;
	protected boolean framesLoaded = false;
	protected boolean waitingForExtStart = false;
	private int updateInterval = 900;
	private boolean showArmed = false;
	private static boolean once = false;

	private boolean monitorInBackground  = true;

	protected abstract String getAcqStatus() throws DeviceException;
	
	@Override
	public void configure() throws FactoryException{
		if (!once) {
			runner = uk.ac.gda.util.ThreadManager.getThread(this, getClass().getName());
			runner.start();
			once = true;
		}
	}

	@Override
	public void reconfigure() throws FactoryException {
		if (!configured) {
			configure();
		}
	}

	@Override
	public void close() {
		// we don't actually close as other devices may use the same connection
		configured = false;
		framesLoaded = false;
	}

	/**
	 * @return the update interval in milliseconds
	 */
	public int getUpdateInterval() {
		return updateInterval;
	}

	/**
	 * @param updateInterval
	 */
	public void setUpdateInterval(int updateInterval) {
		this.updateInterval = updateInterval;
	}

	/**
	 * @return the total number of frames
	 */
	public int getTotalFrames() {
		return totalFrames;
	}

	public int getTotalCycles() {
		return totalCycles;
	}

	public long getTotalExptTime() {
		return totalExptTime;
	}

	@Override
	public void setCycles(int cycles) {
		this.cycles = cycles;
	}

	@Override
	public void addFrameSet(int frameCount, double requestedDeadTime, double requestedLiveTime) {
		addFrameSet(frameCount, requestedDeadTime, requestedLiveTime, 0, 0, 0, 0);
	}

	@Override
	public void addFrameSet(int frameCount, double requestedDeadTime, double requestedLiveTime, int deadPort,
			int livePort, int deadPause, int livePause) {
		timeFrameProfile.addElement(new FrameSet(frameCount, requestedDeadTime, requestedLiveTime, deadPort, livePort,
				deadPause, livePause));
	}

	@Override
	public void clearFrameSets() {
		timeFrameProfile.removeAllElements();
		totalFrames = 0;
		framesLoaded = false;
	}

	public List<FrameSet> getFramesets() {
		return timeFrameProfile;
	}


	public double getCurrentLiveTime(int currentFrame) {
		int frameCount = 0;
		double liveTime = 0.0;
		for (FrameSet frameSet : timeFrameProfile) {
			frameCount += frameSet.getFrameCount();
			if (currentFrame <= frameCount) {
				liveTime = frameSet.getRequestedLiveTime();
				break;
			}
		}
		return liveTime / 1000; // return in seconds
	}

	public double getCurrentDeadTime(int currentFrame) {
		int frameCount = 0;
		double deadTime = 0.0;
		for (FrameSet frameSet : timeFrameProfile) {
			frameCount += frameSet.getFrameCount();
			if (currentFrame <= frameCount) {
				deadTime = frameSet.getRequestedDeadTime();
				break;
			}
		}
		return deadTime / 1000; // return in seconds
	}

	public int getCurrentFrames(int currentFrame) {
		int frameCount = 0;
		int nframes = 0;
		for (FrameSet frameSet : timeFrameProfile) {
			frameCount += frameSet.getFrameCount();
			if (nframes > 0 && frameSet.getDeadPause() != 0) {
				break;
			}
			if (currentFrame <= frameCount) {
				nframes += frameSet.getFrameCount();
			}
		}
		return nframes;
	}

	/**
	 * Do not use this method - it is used by the TFG object to create a monitor thread. Instead add an observer to the object
	 */
	@Override
	public synchronized void run() {
		while (true) {
			try {
				wait();
				while (started && monitorInBackground) {
					String status = getAcqStatus();
					String currentStatus;
					TimerStatus timerStatus;
					if (status.equals("IDLE")) {
						if (waitingForExtStart) {
							currentStatus = "WAITING";
						} else {
							totalCycles += cycles;
							currentStatus = status;
							started = false;
						}
						timerStatus = new TimerStatus(0, 0, 0, currentStatus, totalCycles, 0);
					} else {
						int percentComplete = 0;
						Date d = new Date();
						long timeNow = d.getTime();
						if (waitingForExtStart) {
							waitingForExtStart = false;
							startTime = timeNow;
						} else {
							if (!status.equals("PAUSED")) {
								elapsedTime += timeNow - startTime;
							}
							if (totalExptTime > 0.0) {
								percentComplete = (int) ((elapsedTime * 100) / totalExptTime);
							}
							startTime = timeNow;
						}
						int frame = getCurrentFrame();
						if ((frame / 2) * 2 == frame) {
							if (status.equals("PAUSED")) {
								currentStatus = "DEAD PAUSE";
							} else {
								currentStatus = "DEAD FRAME";
							}
						} else {
							if (status.equals("PAUSED")) {
								currentStatus = "LIVE PAUSE";
							} else {
								currentStatus = "LIVE FRAME";
							}
						}
						int currentFrame = (frame / 2) + 1;
						int currentCycle = cycles - getCurrentCycle();
						int cycleCount = totalCycles + currentCycle;

						timerStatus = new TimerStatus(elapsedTime, currentFrame, currentCycle, currentStatus,
								cycleCount, percentComplete);
					}
					notifyIObservers(this, timerStatus);
					wait(updateInterval);
				}
			} catch (Exception iox) {
				logger.info("tfg run thread interrupted",iox);
			}
		}
	}

	public boolean isShowArmed() {
		return showArmed;
	}

	public void setShowArmed(boolean showArmed) {
		this.showArmed = showArmed;
	}

	public boolean isMonitorInBackground() {
		return monitorInBackground;
	}

	/**
	 * 
	 * @param monitorInBackground if true (default) the tfg state is monitored regularly and observers are notified of changes.
	 */
	public void setMonitorInBackground(boolean monitorInBackground) {
		this.monitorInBackground = monitorInBackground;
	}
}
