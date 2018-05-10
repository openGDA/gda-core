/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package gda.device.detectorfilemonitor.impl;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import gda.device.detectorfilemonitor.HighestExistingFileMonitor;
import gda.device.detectorfilemonitor.HighestExistingFileMonitorData;
import gda.device.detectorfilemonitor.HighestExistingFileMonitorSettings;
import gda.observable.IObserver;
import gda.observable.ObservableComponent;
import uk.ac.gda.api.remoting.ServiceInterface;

/**
 * Implementation of HighestExistingFileMonitor that checks for file existence.
 */
@ServiceInterface(HighestExistingFileMonitor.class)
public class SimpleHighestExistingFileMonitor implements HighestExistingFileMonitor, InitializingBean {
	private static final Logger logger = LoggerFactory.getLogger(SimpleHighestExistingFileMonitor.class);
	ObservableComponent obsComp = new ObservableComponent();

	public SimpleHighestExistingFileMonitor(){}
	private long delay = 1000; // in ms

	HighestExistingFileMonitorSettings highestExistingFileMonitorSettings = null;
	HighestExistingFileMonitorData highestExistingFileMonitorData = null;

	@Override
	public HighestExistingFileMonitorSettings getHighestExistingFileMonitorSettings() {
		return highestExistingFileMonitorSettings;
	}

	@Override
	public void setHighestExistingFileMonitorSettings(HighestExistingFileMonitorSettings highestExistingFileMonitorSettings) {
		this.highestExistingFileMonitorSettings = highestExistingFileMonitorSettings;
	}

	@Override
	public long getDelayInMS() {
		return delay;
	}

	@Override
	public void setDelayInMS(long delay) {
		if (delay > 0) {
			this.delay = delay;
		}
	}

	Integer latestNumberFound = null;
	boolean running = false;
	private ScheduledExecutorService scheduler;

	Runnable runnable = new Runnable() {

		int numberToLookFor;
		String fileToLookFor;
		HighestExistingFileMonitorSettings highestExistingFileMonitorSettings_InUse = null;
		Integer numberFound = null;
		String templateInUse="";

		@Override
		public void run() {
			try {
				if (!running)
					return;
				if (highestExistingFileMonitorSettings_InUse != highestExistingFileMonitorSettings) {
					latestNumberFound = null;
					highestExistingFileMonitorSettings_InUse = highestExistingFileMonitorSettings;
					numberToLookFor = highestExistingFileMonitorSettings_InUse.startNumber;
					templateInUse = highestExistingFileMonitorSettings_InUse.getFullTemplate();
					numberFound = null;
					if (highestExistingFileMonitorSettings_InUse == null) {
						running = false;
						fileToLookFor = null;
					} else {
						fileToLookFor = String.format(templateInUse,numberToLookFor);
					}
				}
				if (fileToLookFor != null) {
					while ((new File(fileToLookFor)).exists()) {
						numberFound = numberToLookFor;
						numberToLookFor++;
						fileToLookFor = String.format(templateInUse,numberToLookFor);
					}
				}
				setLatestNumberFound(numberFound);
			} catch (Throwable th) {
				logger.error("Error looking for file using template `"
						+ highestExistingFileMonitorSettings_InUse.fileTemplate + "` number=" + numberToLookFor, th);
			} finally{
				if (running) {
					scheduler.schedule(runnable, delay, TimeUnit.MILLISECONDS);
				}
			}
		}
	};

	@Override
	public void addIObserver(IObserver anIObserver) {
		obsComp.addIObserver(anIObserver);
	}

	@Override
	public void deleteIObserver(IObserver anIObserver) {
		obsComp.deleteIObserver(anIObserver);
	}

	@Override
	public void deleteIObservers() {
		obsComp.deleteIObservers();
	}

	@Override
	public boolean isRunning() {
		return running;
	}

	@Override
	public void setRunning(boolean running) {
		if (!configured) {
			this.running = running;
			return;
		}
		if (isRunning() == running)
			return; // do nothing

		if (running) {
			start();
		} else {
			stop();
		}

	}

	private final Object stop_lock = new Object();

	private void stop() {
		synchronized (stop_lock) {
			running = false;
			if (scheduler != null) {
				scheduler.shutdownNow();
				scheduler = null;
			}
		}
	}

	private void start() {
		stop();
		if (scheduler == null) {
			scheduler = Executors.newScheduledThreadPool(1);
		}
		running = true;
		scheduler.submit(runnable);
	}

	protected void setLatestNumberFound(Integer numberFound) {
		if (numberFound != latestNumberFound){
			if (numberFound == null || !numberFound.equals(latestNumberFound)) {
				latestNumberFound = numberFound;
				obsComp.notifyIObservers(this, new HighestExistingFileMonitorData(highestExistingFileMonitorSettings,
						latestNumberFound));
			}
		}
	}

	boolean configured = false;

	@Override
	public void afterPropertiesSet() throws Exception {
		configured = true;
		boolean start = isRunning();
		if (start) {
			running = false;
			setRunning(true);
		}
	}

	String name;

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public HighestExistingFileMonitorData getHighestExistingFileMonitorData() {
		return new HighestExistingFileMonitorData(highestExistingFileMonitorSettings, latestNumberFound);
	}

}
