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

package gda.scan;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.data.scan.datawriter.DataWriter;
import gda.device.DeviceException;

/**
 * An implementation of {@link ScanDataPointPipeline} that computes ScanDataPoints and broadcasts them using internally
 * managed threads.
 */
public class MultithreadedScanDataPointPipeline implements ScanDataPointPipeline {

	public class ScannableSpecificExecutorService{

		private ExecutorService executor;
		private final Map<String, ExecutorService> namedExecutors = new HashMap<>();
		private ThreadFactory threadFactory;

		public ScannableSpecificExecutorService(int positionCallableThreadPoolSize, ThreadFactory threadFactory) {
			this.threadFactory = threadFactory;
			executor = Executors.newFixedThreadPool(positionCallableThreadPoolSize, threadFactory);
		}

		public void shutdown() {
			executor.shutdown();
			for (ExecutorService executor : namedExecutors.values()) {
				executor.shutdown();
			}
		}

		public void shutdownNow() {
			executor.shutdownNow();

			for (ExecutorService executor : namedExecutors.values()) {
				executor.shutdownNow();
			}
		}

		public boolean isShutdown() {
			return executor.isShutdown() &&
					namedExecutors.values().stream().allMatch(ExecutorService::isShutdown);
		}

		public boolean isTerminated() {
			return executor.isTerminated() &&
					namedExecutors.values().stream().allMatch(ExecutorService::isTerminated);
		}

		public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
			boolean awaitTermination = executor.awaitTermination(timeout, unit);
			for(Entry<String, ExecutorService> es : namedExecutors.entrySet())
				awaitTermination &= es.getValue().awaitTermination(timeout,unit);
			return awaitTermination;

		}

		public <T> Future<T> submit(Callable<T> task) {
			if (task instanceof NamedQueueTask namedQueueTask) {
				String name = namedQueueTask.getExecutorServiceName();
				namedExecutors.computeIfAbsent(name,
						n -> Executors.newFixedThreadPool(namedQueueTask.getThreadPoolSize(), threadFactory));
			}
			return executor.submit(task);
		}

	}

	private static final Logger logger = LoggerFactory.getLogger(MultithreadedScanDataPointPipeline.class);

	/**
	 * Pool used to compute individual Scannable's positions from their position Callables.
	 */
	private ScannableSpecificExecutorService positionCallableService;

	/**
	 * Single threaded, fixed length service used to populate and broadcast ScanDataPoints.
	 */
	private NoExceptionThreadPoolExecutor broadcasterQueue;

	private ScanDataPointPublisher broadcaster;

	/**
	 * Creates a new MultithreadedScanDataPointPipeline and starts it up to accept points.
	 *
	 * @param broadcaster
	 * @param positionCallableThreadPoolSize
	 *            the number of threads used to process Callables
	 * @param scanDataPointPipelineLength
	 *            the number of points allowed in the Pipeline concurrently.
	 */
	public MultithreadedScanDataPointPipeline(ScanDataPointPublisher broadcaster, int positionCallableThreadPoolSize,
			int scanDataPointPipelineLength, String scanName) {

		this.broadcaster = broadcaster;

		if (scanDataPointPipelineLength == 0) {
			logger.warn("A zero length pipeline was requested but this would be unable to accept ScanDataPoints. A pipeline of length one has been created instead");
			scanDataPointPipelineLength = 1;
		}
		// TODO: Remove this configuration as it now unused
		logger.info("MultithreadedScanDataPointPipeline is ignoring the request to create a fixed length queue and is creating one with indefinite length");
		NamedThreadFactory threadFactory = new NamedThreadFactory(
				" scan-" + scanName + "-MSDPP.positionCallableService-%d of " + positionCallableThreadPoolSize);
		if (positionCallableThreadPoolSize > 0) {
			positionCallableService = new ScannableSpecificExecutorService(positionCallableThreadPoolSize, threadFactory);
		} // else leave it null.

		/**
		 * Uses a ThreadPoolExecutor with a custom queue designed to block rather than throw a RejectedExecutionException if
		 * the thread is busy and queue is full. The total number of points in the Pipeline is the number of points in the
		 * workQueue plus the one being worked on in the single thread.
		 * @param positionCallableService
		 */
		BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<Runnable>();
		broadcasterQueue = new NoExceptionThreadPoolExecutor(workQueue,
				new NamedThreadFactory(" scan-" + scanName + "-MSDPP.broadcaster"), positionCallableService )  ;
	}

	/**
	 * Computes ScanDataPoints and broadcasts them using internally managed threads.
	 * @throws DeviceException
	 */
	@Override
	public void put(IScanDataPoint point) throws DeviceException {
		logger.debug("'{}': added to pipeline. Points already waiting in queue: {}", point.toString(),
				broadcasterQueue.getQueue().size());

		//check if an exception has already been seen and if so throw it
		try {
			broadcasterQueue.raiseExceptionIfSeen();
		} catch (DeviceException e) {
			throw e;
		} catch (Throwable e) {
			throw new DeviceException(e);
		}

		if (positionCallableService != null) {
			// If this has not been created we need not look for Callables
			convertPositionCallablesToFutures(point);
		}

		try {
			broadcasterQueue.submit(new ScanDataPointPopulatorAndPublisher(getBroadcaster(),point));
		} catch (RejectedExecutionException e) {
			if (broadcasterQueue.isShutdown()) {
				throw new DeviceException(
						"Could not add new point to MultithreadedScanDataPointPipeline as it is shutdown.", e);
			}
			throw e;
		}
		logger.debug("'{}' added to executor", point);
	}

	private void convertPositionCallablesToFutures(IScanDataPoint point)  {
		point.setScannablePositions(convertPositionCallablesToFutures(point.getPositions()));
		point.setDetectorData(convertPositionCallablesToFutures(point.getDetectorData()));
	}

	private List<Object> convertPositionCallablesToFutures(List<Object> positions) {
		return positions.stream()
				.map(pos -> (pos instanceof Callable<?> callable) ? positionCallableService.submit(callable) : pos)
				.toList();
	}

	/**
	 * Politely shutdown the pipeline. Blocks until processing is complete. Calls shutdownNow if there is any problem or
	 * if interrupted.
	 *
	 * @throws DeviceException
	 */
	@Override
	public void shutdown(boolean waitForProcessingCompletion) throws Exception {

		try {
			broadcasterQueue.shutdown();//do not allow any more tasks are to be added
			if(waitForProcessingCompletion) {
				while (!broadcasterQueue.awaitTermination(1, TimeUnit.SECONDS)) {
					checkForException();
				}
			}
		} finally {

			//check for an existing exception as the code below may itself lead to an
			//interrupted exception which we can ignore as we cause it
			checkForException();


			// 2. Force shutdown
			try{
				int numberOfDumpedPoints = broadcasterQueue.shutdownNow().size();
				if(numberOfDumpedPoints > 0) {
					logger.error("Error in scan. The Pipeline has been stopped and {} points lost.", numberOfDumpedPoints);
				}

				if (positionCallableService != null) {
					positionCallableService.shutdownNow();
				}

				// 3. Shutdown the Broadcaster (DataWriter)
				getBroadcaster().shutdown();

				checkForException();

			} catch (InterruptedException e){
				//ignore as the calls to shutdownNow will be the cause
			}
		}
	}

	@Override
	public DataWriter getDataWriter() {
		return getBroadcaster().getDataWriter();
	}

	protected ScanDataPointPublisher getBroadcaster() {
		return broadcaster;
	}

	public class NamedThreadFactory implements ThreadFactory {

		private final ThreadFactory defaultThreadFactory;

		final AtomicInteger threadNumber = new AtomicInteger(1);

		private final String format;

		public NamedThreadFactory(String format) {
			this.format = format;
			defaultThreadFactory = Executors.defaultThreadFactory();
		}

		@Override
		public Thread newThread(Runnable r) {
			Thread newThread = defaultThreadFactory.newThread(r);
			newThread.setName(newThread.getName() + String.format(format, +threadNumber.getAndIncrement()));
			return newThread;
		}

	}

	@Override
	public void checkForException() throws Exception {
		broadcasterQueue.raiseExceptionIfSeen();
	}

}
