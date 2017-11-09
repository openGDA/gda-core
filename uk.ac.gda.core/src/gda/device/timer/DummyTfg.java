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

import static gda.device.timer.Tfg.EXT_INHIBIT_ATTR_NAME;
import static gda.device.timer.Tfg.EXT_START_ATTR_NAME;
import static gda.device.timer.Tfg.TOTAL_FRAMES;
import static gda.device.timer.Tfg.VME_START_ATTR_NAME;

import java.util.Date;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceBase;
import gda.device.Timer;
import gda.device.TimerStatus;

/**
 * A timer class for the VME time frame generator card implemented using DA.Server
 */
public class DummyTfg extends DeviceBase implements Timer, Runnable {

	private static final Logger logger = LoggerFactory.getLogger(DummyTfg.class);

	private static final int EC740_MAXFRAME = 1024;

	private boolean extStart = false;

	private boolean vmeStart = true;

	private boolean extInh = false;

	private int cycles = 1;

	private int totalCycles = 0;

	private int totalFrames = 0;

	private Vector<FrameSet> timeFrameProfile = new Vector<FrameSet>();

	private Thread runner;

	private TimeFrameGenerator timeFrameGenerator;

	private boolean started = false;

	private long startTime = 0;

	private long totalExptTime;

	private volatile String currentState = "IDLE";

	private int currentFrameNumber = 0;

	private int currentCycleNumber = 0;

	private boolean completed = false;

	private boolean stopRun = false;

	private boolean framesLoaded = false;

	@Override
	public void configure() {
		timeFrameGenerator = new TimeFrameGenerator();
		runner = uk.ac.gda.util.ThreadManager.getThread(this, getClass().getName());
		runner.start();
		logger.debug("Configuring dummy tfg " + getName());
		configured = true;
	}

	@Override
	public void reconfigure() {
		if (!configured) {
			logger.debug("Reconfiguring dummy tfg " + getName());
			configure();
		}
	}

	@Override
	public void close() {
		runner = null;
		timeFrameGenerator = null;
		configured = false;
		logger.debug("DummyTfg " + getName() + " closed");
	}

	@Override
	public synchronized int getStatus() {
		int state = IDLE;
		if (currentState.equals("RUNNING"))
			state = ACTIVE;
		else if (currentState.equals("PAUSED"))
			state = PAUSED;

		return state;
	}

	@Override
	public synchronized void stop() {
		stopRun = true;
		// timeFrameGenerator.stop();
	}

	@Override
	public int getMaximumFrames() {
		return EC740_MAXFRAME;
	}

	/**
	 * @return the total number of frames
	 */
	public int getTotalFrames() {
		return totalFrames;
	}

	@Override
	public synchronized int getCurrentFrame() {
		return currentFrameNumber;
	}

	@Override
	public synchronized int getCurrentCycle() {
		return currentCycleNumber;
	}

	@Override
	public void setCycles(int cycles) {
		this.cycles = cycles;
	}

	/**
	 * Zero the total cycle count
	 */
	public void setTotalCyclesToZero() {
		totalCycles = 0;
	}

	@Override
	public synchronized void start() {
		if (configured && framesLoaded) {
			doStart();
		} else {
			countAsync(360000);
		}
	}

	public void cont() {
		// what to do ????????????????
	}


	@Override
	public void restart() {
		// what to do ????????????????
	}

	/**
	 * {@inheritDoc} Create a single frameSet object for a specified live and dead time. A count for identical frames is
	 * specified by the frameCount.
	 *
	 * @param requestedDeadTime
	 *            the requested frame dead time in milliseconds
	 * @param requestedLiveTime
	 *            the requested frame live time in milliseconds
	 * @param frameCount
	 *            the requested number of frames required of this type
	 * @see gda.device.Timer#addFrameSet(int, double, double)
	 */
	@Override
	public void addFrameSet(int frameCount, double requestedDeadTime, double requestedLiveTime) {
		addFrameSet(frameCount, requestedDeadTime, requestedLiveTime, 0, 0, 0, 0);
	}

	/**
	 * {@inheritDoc} Create a single frameSet object for a specified live and dead time. A count for identical frames is
	 * specified by the frameCount.
	 *
	 * @param frameCount
	 *            the requested number of frames required of this type
	 * @param requestedDeadTime
	 *            the requested frame dead time in milliseconds
	 * @param requestedLiveTime
	 *            the requested frame live time in milliseconds
	 * @param deadPort
	 *            the wait period output level 0 or 1
	 * @param livePort
	 *            the run period output level 0 or 1
	 * @param deadPause
	 *            the pause before wait period 0 or 1
	 * @param livePause
	 *            the pause before run period 0 or 1
	 * @see gda.device.Timer#addFrameSet(int, double, double, int, int, int, int)
	 */
	@Override
	public void addFrameSet(int frameCount, double requestedDeadTime, double requestedLiveTime, int deadPort,
			int livePort, int deadPause, int livePause) {
		timeFrameProfile.addElement(new FrameSet(frameCount, requestedDeadTime, requestedLiveTime, deadPort, livePort,
				deadPause, livePause));
	}

	@Override
	public void clearFrameSets() {
		timeFrameProfile.removeAllElements();
	}

	@Override
	public void loadFrameSets() {
		totalExptTime = 0;
		totalFrames = 0;
		for (FrameSet frameSet : timeFrameProfile) {
			// These restrictions commented out because the interfere with
			// testing
			// of ContinuousScans. Was there a reason for them?
			/*
			 * if ((int) frameSet.getRequestedDeadTime() < 1) { frameSet.setRequestedDeadTime(1.0);
			 * Message.info("Resetting dead time to minimum 1msec"); } if ((int) frameSet.getRequestedLiveTime() < 1) {
			 * frameSet.setRequestedLiveTime(1.0); Message.info("Resetting live time to minimum 1msec"); }
			 */
			totalExptTime += (int) (frameSet.getRequestedLiveTime() + frameSet.getRequestedDeadTime())
					* frameSet.getFrameCount();
			totalFrames += frameSet.getFrameCount();
		}
		totalExptTime *= cycles;
		framesLoaded = true;
	}

	/**
	 * Initiates a single specified timing period and allows the timer to proceed asynchronously. The end of period can
	 * be determined by calls to getStatus() returning IDLE.
	 *
	 * @param time
	 *            the requested counting time in milliseconds
	 */
	@Override
	public synchronized void countAsync(double time) {
		clearFrameSets();
		addFrameSet(1, 1, time);
		loadFrameSets();
		doStart();
	}

	private void doStart() {
		Date d = new Date();
		startTime = d.getTime();
		started = true;
		stopRun = false;
		timeFrameGenerator.start();
		currentState = "RUNNING";
		currentFrameNumber = 0;
		notifyAll();
	}

	@Override
	public void output(String file) {
		// do nothing for now
	}

	/**
	 * {@inheritDoc} Set attribute values for "Ext-Start", "Ext-Inhibit", "VME-Start".
	 *
	 * @param attributeName
	 *            the attribute name to set.
	 * @param value
	 *            the attribute value to set.
	 * @see gda.device.DeviceBase#setAttribute(java.lang.String, java.lang.Object)
	 */
	@Override
	public void setAttribute(String attributeName, Object value) {
		if (attributeName.equals(EXT_START_ATTR_NAME) && ((Boolean) value).booleanValue())
			logger.error("Ext-Start option not available in DummyTFG implementation. Setting VMEStart");

		else if (attributeName.equals(EXT_INHIBIT_ATTR_NAME) && ((Boolean) value).booleanValue())
			logger.error("Ext-Inhibit option not available in DummyTFG implementation. Setting VMEStart");

		else if (attributeName.equals(VME_START_ATTR_NAME))
			vmeStart = true;

	}

	/**
	 * {@inheritDoc} Get attribute values for "Ext-Start", "Ext-Inhibit".
	 *
	 * @param attributeName
	 *            the attribute name to get.
	 * @return the attribute
	 * @see gda.device.DeviceBase#getAttribute(java.lang.String)
	 */
	@Override
	public Object getAttribute(String attributeName) {
		Object obj = null;

		if (attributeName.equals(EXT_START_ATTR_NAME))
			obj = new Boolean(extStart);

		else if (attributeName.equals(EXT_INHIBIT_ATTR_NAME))
			obj = new Boolean(extInh);

		else if (attributeName.equals(VME_START_ATTR_NAME))
			obj = new Boolean(vmeStart);

		else if (attributeName.equals(TOTAL_FRAMES))
			obj = new Integer(totalFrames);

		return obj;
	}

	@Override
	public synchronized void run() {
		while (true) {
			completed = false;
			try {
				while (!started)
					wait();
				while (started) {
					String currentStatus;
					TimerStatus timerStatus;
					if (completed) {
						totalCycles += cycles;
						started = false;
						completed = false;
						timerStatus = new TimerStatus(0, 0, 0, "IDLE", totalCycles, 0);
					} else {
						if (!currentState.equals("IDLE")) {
							Date d = new Date();
							long timeNow = d.getTime();
							long elapsedTime = timeNow - startTime;
							int percentComplete = (int) ((elapsedTime * 100) / totalExptTime);
							int frame = getCurrentFrame();
							currentStatus = ((frame / 2) * 2 == frame) ? "DEAD FRAME" : "LIVE FRAME";
							int currentFrame = (frame / 2) + 1;
							int currentCycle = cycles - getCurrentCycle();
							int cycleCount = totalCycles + currentCycle;

							timerStatus = new TimerStatus(elapsedTime, currentFrame, currentCycle, currentStatus,
									cycleCount, percentComplete);
						} else {
							timerStatus = new TimerStatus(0, 0, 0, "RUNNING", totalCycles, 0);
						}
					}

					notifyIObservers(this, timerStatus);

					wait(10);

				}
			} catch (InterruptedException iox) {
				logger.debug("tfg run thread interrupted");
			}
		}
	}

	private class TimeFrameGenerator implements Runnable {
		/**
		 *
		 */
		public TimeFrameGenerator() {
			runner = uk.ac.gda.util.ThreadManager.getThread(this, getClass().getName());
			runner.start();
		}

		/**
		 *
		 */
		public synchronized void start() {
			notify();
		}

		@Override
		public synchronized void run() {
			while (true) {
				try {
					wait();
					currentState = "RUNNING";
					for (currentCycleNumber = cycles; currentCycleNumber > 0; currentCycleNumber--) {
						currentFrameNumber = 0;
						for (FrameSet frameSet : timeFrameProfile) {
							for (int i = 0; i < frameSet.getFrameCount(); i++) {
								// waitDouble(frameSet.getRequestedDeadTime());
								if (stopRun)
									throw new InterruptedException("Stopping run");
								waitDouble(frameSet.getRequestedDeadTime());
								currentFrameNumber++;
								if (stopRun)
									throw new InterruptedException("Stopping run");
								waitDouble(frameSet.getRequestedLiveTime());
								currentFrameNumber++;
								if (stopRun)
									throw new InterruptedException("Stopping run");
							}
						}
					}
				} catch (InterruptedException e) {
					// do nothing
				} finally {
					currentState = "IDLE";
					completed = true;
					logger.debug("DummyTfg stopped");
				}
			}
		}
	}

	/**
	 * Waits for a non-integral number of milli seconds by converting the value to be used in the two parameter version
	 * of wait()
	 *
	 * @param milliSeconds
	 * @throws InterruptedException
	 */
	private void waitDouble(double milliSeconds) throws InterruptedException {
		double mS = Math.floor(milliSeconds);
		double nS = (milliSeconds - mS) * 1.0E6;
		synchronized (this) {
			wait((int) mS, (int) nS);
		}
	}
}