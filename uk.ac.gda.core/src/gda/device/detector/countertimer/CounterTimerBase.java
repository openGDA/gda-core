/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

package gda.device.detector.countertimer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.CounterTimer;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.Timer;
import gda.device.detector.DetectorBase;
import gda.factory.FactoryException;
import gda.factory.Finder;

public abstract class CounterTimerBase extends DetectorBase implements CounterTimer {

	private static final Logger logger = LoggerFactory.getLogger(TFGCounterTimer.class);

	protected boolean slave = false;
	
	protected gda.device.Timer timer = null;
	
	private String timerName;
	
	@Override
	public void configure() throws FactoryException {
		if (timer == null) {
			logger.debug("Finding: " + timerName);
			if ((timer = (gda.device.Timer) Finder.getInstance().find(timerName)) == null) {
				logger.error("Tfg " + timerName + " not found");
			}
		}
		super.configure();
		configured = true;
	}

	@Override
	public boolean isSlave() {
		return slave;
	}

	@Override
	public void setSlave(boolean slave) {
		this.slave = slave;
	}
	
	@Override
	public int getMaximumFrames() throws DeviceException {
		return timer.getMaximumFrames();
	}

	@Override
	public int getCurrentFrame() throws DeviceException {
		return timer.getCurrentFrame();
	}

	@Override
	public int getCurrentCycle() throws DeviceException {
		return timer.getCurrentCycle();
	}

	@Override
	public void setCycles(int cycles) throws DeviceException {
		if (!slave){
			timer.setCycles(cycles);
		}
	}
	
	@Override
	public void addFrameSet(int frameCount, double requestedLiveTime, double requestedDeadTime) throws DeviceException {
		if (!slave){
			timer.addFrameSet(frameCount, requestedDeadTime, requestedLiveTime);
		}
	}

	@Override
	public void addFrameSet(int frameCount, double requestedLiveTime, double requestedDeadTime, int deadPort,
			int livePort, int deadPause, int livePause) throws DeviceException {
		if (!slave){
			timer.addFrameSet(frameCount, requestedDeadTime, requestedLiveTime, deadPort, livePort, deadPause, livePause);
		}
	}

	@Override
	public void clearFrameSets() throws DeviceException {
		if (!slave){
			timer.clearFrameSets();
		}
	}


	@Override
	public void loadFrameSets() throws DeviceException {
		if (!slave){
			timer.loadFrameSets();
		}
	}

	@Override
	public void restart() throws DeviceException {
		if (!slave){
			timer.restart();
		}
	}


	@Override
	public void start() throws DeviceException {
		if (!slave){
			timer.start();
		}
	}
	
	@Override
	public void stop() throws DeviceException {
		if (!slave){
			timer.stop();
		}
	}

	/**
	 * @see gda.device.Detector#getStatus()
	 */
	@Override
	public int getStatus() throws DeviceException {
		int tfgTimerStatus = timer.getStatus();
		if (tfgTimerStatus == Timer.IDLE || tfgTimerStatus == Timer.ACTIVE || tfgTimerStatus == Timer.PAUSED) {
			return tfgTimerStatus;
		}
		return Detector.FAULT;
	}

	public void setTimerName(String tfgName) {
		this.timerName = tfgName;
	}

	public String getTimerName() {
		return timerName;
	}
	
	public gda.device.Timer getTimer() {
		return timer;
	}

	public void setTimer(gda.device.Timer tfg) {
		this.timer = tfg;
	}
}
