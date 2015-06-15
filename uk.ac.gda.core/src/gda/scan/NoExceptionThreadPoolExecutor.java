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

package gda.scan;

import gda.scan.MultithreadedScanDataPointPipeline.ScannableSpecificExecutorService;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Subclass of ThreadPoolExecutor that rejects any new task submissions following observation of an
 * exception a task.
 *
 * The method raiseExceptionIfSeen will raise the Exception observed in the task.
 */
public class NoExceptionThreadPoolExecutor extends ThreadPoolExecutor {

	private ScannableSpecificExecutorService positionCallableService;

	public NoExceptionThreadPoolExecutor(BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, ScannableSpecificExecutorService positionCallableService) {
		super(1, 1, 0L, TimeUnit.MILLISECONDS, workQueue, threadFactory);
		this.positionCallableService = positionCallableService;
	}

	public void raiseExceptionIfSeen() throws Exception {
		if (exceptionRaisedInTask != null) {
			throw exceptionRaisedInTask;
		}
	}

	private static final Logger logger = LoggerFactory.getLogger(NoExceptionThreadPoolExecutor.class);

	private Exception exceptionRaisedInTask = null;

	@SuppressWarnings("rawtypes")
	@Override
	protected void afterExecute(Runnable r, Throwable t) {
		super.afterExecute(r, t);
		if (exceptionRaisedInTask != null)
			return; // we only want the first exception
		Throwable throwable = null;
		if (t != null) {
			throwable = t;
		}
		if (t == null && r instanceof Future) {
			try {
				((Future) r).get();
			} catch (ExecutionException ce) {
				throwable = ce.getCause();
			} catch (Exception ce) {
				throwable = ce;
			}
		}
		if (throwable != null) {
			shutdown();
			if( positionCallableService != null)
				positionCallableService.shutdown();

			int numberOfDumpedPoints = shutdownNow().size();
			if (numberOfDumpedPoints > 0) {
				logger.error("BroadcastQueue shutdown due following detection of exception. " + numberOfDumpedPoints
						+ " points dumped", exceptionRaisedInTask);
			}
			if( positionCallableService != null)
				positionCallableService.shutdownNow();
			exceptionRaisedInTask = (throwable instanceof Exception) ? (Exception) throwable : new Exception(throwable);
		}
	}
}
