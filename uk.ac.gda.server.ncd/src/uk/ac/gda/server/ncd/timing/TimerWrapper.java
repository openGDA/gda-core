/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package uk.ac.gda.server.ncd.timing;

import gda.device.DeviceException;
import gda.device.Timer;
import gda.factory.FactoryException;
import gda.observable.IObserver;

/** Delegating wrapper to allow additional functionality to be added to an existing Timer */
public class TimerWrapper implements Timer {
	private final Timer timer;

	public TimerWrapper(Timer timer) {
		this.timer = timer;
	}

	@Override
	public void setName(String name) {
		timer.setName(name);
	}

	@Override
	public void configure() throws FactoryException {
		timer.configure();
	}

	@Override
	public String getName() {
		return timer.getName();
	}

	@Override
	public void addIObserver(IObserver observer) {
		timer.addIObserver(observer);
	}

	@Override
	public void setAttribute(String attributeName, Object value) throws DeviceException {
		timer.setAttribute(attributeName, value);
	}

	@Override
	public int getStatus() throws DeviceException {
		return timer.getStatus();
	}

	@Override
	public void deleteIObserver(IObserver observer) {
		timer.deleteIObserver(observer);
	}

	@Override
	public boolean isConfigured() {
		return timer.isConfigured();
	}

	@Override
	public void deleteIObservers() {
		timer.deleteIObservers();
	}

	@Override
	public void reconfigure() throws FactoryException {
		timer.reconfigure();
	}

	@Override
	public Object getAttribute(String attributeName) throws DeviceException {
		return timer.getAttribute(attributeName);
	}

	@Override
	public int getMaximumFrames() throws DeviceException {
		return timer.getMaximumFrames();
	}

	@Override
	public boolean isConfigureAtStartup() {
		return timer.isConfigureAtStartup();
	}

	@Override
	public int getCurrentFrame() throws DeviceException {
		return timer.getCurrentFrame();
	}

	@Override
	public void close() throws DeviceException {
		timer.close();
	}

	@Override
	public int getCurrentCycle() throws DeviceException {
		return timer.getCurrentCycle();
	}

	@Override
	public void setProtectionLevel(int newLevel) throws DeviceException {
		timer.setProtectionLevel(newLevel);
	}

	@Override
	public void setCycles(int cycles) throws DeviceException {
		timer.setCycles(cycles);
	}

	@Override
	public int getProtectionLevel() throws DeviceException {
		return timer.getProtectionLevel();
	}

	@Override
	public void start() throws DeviceException {
		timer.start();
	}

	@Override
	public void stop() throws DeviceException {
		timer.stop();
	}

	@Override
	public void restart() throws DeviceException {
		timer.restart();
	}

	@Override
	public void addFrameSet(int frameCount, double requestedDeadTime, double requestedLiveTime) throws DeviceException {
		timer.addFrameSet(frameCount, requestedDeadTime, requestedLiveTime);
	}

	@Override
	public void addFrameSet(int frameCount, double requestedDeadTime, double requestedLiveTime, int deadPort, int livePort, int deadPause, int livePause)
			throws DeviceException {
		timer.addFrameSet(frameCount, requestedDeadTime, requestedLiveTime, deadPort, livePort, deadPause, livePause);
	}

	@Override
	public void clearFrameSets() throws DeviceException {
		timer.clearFrameSets();
	}

	@Override
	public void loadFrameSets() throws DeviceException {
		timer.loadFrameSets();
	}

	@Override
	public void countAsync(double time) throws DeviceException {
		timer.countAsync(time);
	}

	@Override
	public void output(String file) throws DeviceException {
		timer.output(file);
	}

}
