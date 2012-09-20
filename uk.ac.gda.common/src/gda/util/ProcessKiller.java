/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package gda.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Kills a process if it runs for too long.
 */
public class ProcessKiller {
	
	private static final Logger logger = LoggerFactory.getLogger(ProcessKiller.class);
	
	private final Process process;
	private final int timeoutInMs;
	
	private final TimerThread timerThread;
	private final WaitThread waitThread;
	
	public ProcessKiller(Process process, int timeoutInMs) {
		this.process = process;
		this.timeoutInMs = timeoutInMs;
		timerThread = new TimerThread();
		waitThread = new WaitThread();
		timerThread.setName(TimerThread.class.getName() + " for " + process);
		waitThread.setName(WaitThread.class.getName() + " for " + process);
	}
	
	public void start() {
		timerThread.start();
		waitThread.start();
	}
	
	private volatile boolean processRunning = true;
	
	private volatile boolean keepTiming = true;
	private volatile boolean keepWaiting = true;
	
	class TimerThread extends Thread {
		
		@Override
		public void run() {
			logger.debug("timing process...");
			final long startTime = System.currentTimeMillis();
			long elapsedTime = 0;
			while (keepTiming && elapsedTime < timeoutInMs) {
				try {
					Thread.sleep(timeoutInMs - elapsedTime);
				} catch (InterruptedException e) {
					logger.debug("process timer interrupted");
					// ignore and continue
				}
				elapsedTime = System.currentTimeMillis() - startTime;
			}
			
			if (processRunning && elapsedTime >= timeoutInMs) {
				waitThread.stopWaiting();
				logger.warn("process timed out, and will be killed");
				process.destroy();
			}
			
			logger.debug("process timer finished ({}ms)", elapsedTime);
		}
		
		public void stopTiming() {
			keepTiming = false;
			interrupt();
		}
	}
	
	class WaitThread extends Thread {
		
		@Override
		public void run() {
			while (keepWaiting && processRunning) {
				try {
					logger.debug("waiting for process to terminate...");
					process.waitFor();
					logger.debug("process has terminated");
					processRunning = false;
					timerThread.stopTiming();
				} catch (InterruptedException e) {
					logger.debug("interrupted while waiting for process to terminate");
					// ignore and continue
				}
			}
			
			logger.debug("wait thread finished (running={})", processRunning);
		}
		
		public void stopWaiting() {
			keepWaiting = false;
			interrupt();
		}
	}
	
}
