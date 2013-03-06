/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package gda.images.camera;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that can keep track of frames being 'processed' (for example, received from a network stream, decoded, or
 * dispatched to listeners).
 */
public class FrameStatistics {
	
	private static final Logger logger = LoggerFactory.getLogger(FrameStatistics.class);
	
	private String name;
	
	private int settleFrames;
	
	private int reportInterval;
	
	/**
	 * Creates a new statistics gathering/reporting object.
	 * 
	 * @param name name for this object
	 * @param settleFrames number of frames to ignore before beginning statistics gathering
	 * @param reportInterval the interval (in frames) at which statistics will be logged
	 */
	public FrameStatistics(String name, int settleFrames, int reportInterval) {
		this.name = name;
		this.settleFrames = settleFrames;
		this.reportInterval = reportInterval;
	}
	
	protected boolean firstFrameProcessed;
	
	protected long firstFrameProcessedTime;
	
	protected int framesProcessed;
	
	protected ThreadLocal<Long> startTimes;
	
	protected long totalProcessingTime;
	
	protected long processingHwm;
	
	/**
	 * Resets this counter.
	 */
	public synchronized void reset() {
		firstFrameProcessed = false;
		firstFrameProcessedTime = 0;
		framesProcessed = 0;
		startTimes = new ThreadLocal<Long>();
	}
	
	public void startProcessingFrame() {
		startTimes.set(System.nanoTime());
	}
	
	/**
	 * Signals that a frame has been processed.
	 */
	public synchronized void finishProcessingFrame() {
		
		final long frameProcessingEndTime = System.nanoTime();
		final long frameProcessingStartTime = startTimes.get();
		final long frameProcessingTime = (frameProcessingEndTime - frameProcessingStartTime);
		
		if (!firstFrameProcessed) {
			framesProcessed++;
			if (framesProcessed == settleFrames) {
				firstFrameProcessedTime = System.currentTimeMillis();
				firstFrameProcessed = true;
				framesProcessed = 0;
				totalProcessingTime = 0;
			}
		}
		
		else {
			framesProcessed++;
			long currentTime = System.currentTimeMillis();
			double elapsedTimeInSeconds = (currentTime - firstFrameProcessedTime) / 1000.0;
			double framesPerSecond = framesProcessed / elapsedTimeInSeconds;
			totalProcessingTime += frameProcessingTime;
			double totalProcessingTimeInMs = totalProcessingTime / 1.0E6;
			processingHwm = Math.max(processingHwm, frameProcessingTime);
			double processingHwmInMs = processingHwm / 1.0E6;
			double averageProcessingTimePerFrameInMs = totalProcessingTime / framesProcessed / 1.0E6;
			if ((framesProcessed % reportInterval) == 0) {
				logger.debug(String.format("%s: processed %d frames in %.1fs = %.2ffps (total processing time = %.3fms, per frame = %.3fms), HWM = %.3fms", name, framesProcessed, elapsedTimeInSeconds, framesPerSecond, totalProcessingTimeInMs, averageProcessingTimePerFrameInMs, processingHwmInMs));
			}
		}
	}

}
