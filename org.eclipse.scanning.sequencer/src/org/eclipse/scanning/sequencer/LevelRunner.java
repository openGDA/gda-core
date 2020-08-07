/*-
 *******************************************************************************
 * Copyright (c) 2011, 2016 Diamond Light Source Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthew Gerring - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.scanning.sequencer;

import static java.util.stream.Collectors.toList;

import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinPool.ForkJoinWorkerThreadFactory;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.eclipse.scanning.api.ILevel;
import org.eclipse.scanning.api.INameable;
import org.eclipse.scanning.api.annotation.scan.AnnotationManager;
import org.eclipse.scanning.api.annotation.scan.LevelEnd;
import org.eclipse.scanning.api.annotation.scan.LevelStart;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.MapPosition;
import org.eclipse.scanning.api.scan.LevelInformation;
import org.eclipse.scanning.api.scan.LevelRole;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.event.IPositionListener;
import org.eclipse.scanning.api.scan.event.PositionDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Runs any collection of objects using an executor service by reading their levels. On service runs all the devices at
 * each level and waits for them to finish.
 *
 * The implementing class provides the Callable which runs the actual task. For instance setting a position.
 *
 * @author Matthew Gerring
 *
 */
abstract class LevelRunner<L extends ILevel> {

	private static Logger logger = LoggerFactory.getLogger(LevelRunner.class);

	protected final ForkJoinPool threadPool;
	private final PositionDelegate pDelegate;

	protected IPosition position;

	private volatile Exception abortException;

	private SortedMap<Integer, List<L>> devicesByLevel;
	private SortedMap<Integer, AnnotationManager> annotationManagers;

	/**
	 * The timeout (in seconds) is overridden by some subclasses.
	 */
	private long timeout = Long.getLong("org.eclipse.scanning.sequencer.default.timeout", 10);

	private boolean cachingEnabled;

	protected LevelRunner(INameable device) {
		pDelegate = new PositionDelegate(device);
		threadPool = createThreadPool();
		cachingEnabled = true;
	}

	private ForkJoinPool createThreadPool() {
		final UncaughtExceptionHandler uncaughtExceptionHandler =
				(t, e) -> logger.error("Unhandled exception from thread: {}", t.getName(), e);

	    class LevelTaskThread extends ForkJoinWorkerThread {

			protected LevelTaskThread(ForkJoinPool pool, String name) {
				super(pool);
				setName(name);
			}

	    }

	    final String namePrefix = String.valueOf(getLevelRole()).toLowerCase() + "runner-worker-";
	    final AtomicInteger threadNumCounter = new AtomicInteger(0);
		ForkJoinWorkerThreadFactory threadFactory = pool -> new LevelTaskThread(pool, namePrefix + threadNumCounter.incrementAndGet());

		// a ForkJoinPool is required as it is the only executor that has an awaitQuiescence method
		return new ForkJoinPool(Runtime.getRuntime().availableProcessors(),
				threadFactory, uncaughtExceptionHandler, false);
	}

	/**
	 * @return the objects which we would like to order by level, cannot be <code>null</code>
	 */
	protected abstract Collection<L> getDevices() throws ScanningException;

	/**
	 * Implement this method to create a callable which will be run by the executor service. If a given level object and
	 * position return null, no work will be done for that object at that level.
	 *
	 * @param levelObject
	 *            a scannable object to run at this level e.g. a motor
	 * @param position
	 *            the position to move to. Note that this object can be a map giving the desired positions of several
	 *            scannables, not just the one in the levelObject parameter
	 * @return a callable that returns the position reached once it has finished running. May return null to do no work
	 *         for a given create level.
	 */
	protected abstract Callable<IPosition> createTask(L levelObject, IPosition position);

	/**
	 * @return The role of the objects at this level. See {@link org.eclipse.scanning.api.scan.LevelRole}
	 */
	protected abstract LevelRole getLevelRole();

	/**
	 * Runs the devices for this level runner by level for the given {@link IPosition}.
	 * How this behaves is determined by the concrete subclass of this class that the instance
	 * belongs to, specifically how {@link #createTask(ILevel, IPosition)} is implemented.
	 * <p>
	 * Calling this method is equivalent to calling calling @{code run(position, true)}.
	 *
	 * @param position the position to perform the run for
	 * @throws InterruptedException if a call to {@link #abort()} was made while this method
	 *     was running, not that this is not guaranteed
	 * @throws ScanningException if the run failed for any other reason
	 */
	protected boolean run(IPosition position) throws ScanningException, InterruptedException {
		return run(position, true);
	}

	/**
	 * Runs the devices for this level runner by level for the given {@link IPosition}.
	 * This is equivalent to calling calling @{code run(position, true)}
	 *
	 * @param position the position to perform the run for
	 * @param block
	 *            set to <code>true</code> to block until the parallel tasks have completed.<br>
	 *            set to <code>false</code> to return after the last level is told to execute.<br>
	 *            In this case more work can be done on the calling thread. Use the latch() method to come back to the
	 *            last level's ExecutorService
	 * @return <code>true</code> if the run performed normally, <code>false</code> otherwise, for instance because
	 *         {@link IPositionListener#positionWillPerform(org.eclipse.scanning.api.scan.PositionEvent)} returned false
	 *         for some position listener
	 * @throws InterruptedException if a call to {@link #abort()} was made while this method
	 *     was running, not that this is not guaranteed
	 * @throws ScanningException if the run failed for any other reason
	 */
	protected boolean run(IPosition position, boolean block) throws ScanningException, InterruptedException {
		checkAborted();

		logger.debug("running {} for position {}", getLevelRole(), position);
		this.position = position;
		if (!pDelegate.firePositionWillPerform(position)) {
			return false;
		}

		final SortedMap<Integer, List<L>> devicesByLevel = getDevicesByLevel();
		final SortedMap<Integer, AnnotationManager> annotationManagersByLevel = getAnnotationManagersByLevel(devicesByLevel);

		try {
			final int finalLevel = devicesByLevel.isEmpty() ? 0 : devicesByLevel.lastKey();
			for (Integer level : devicesByLevel.keySet()) {
				checkAborted();
				runLevel(level, position, devicesByLevel.get(level),
						block || level != finalLevel, // if non-blocking, still block for all but the final level
						annotationManagersByLevel.get(level));
			}

			pDelegate.firePositionPerformed(finalLevel, position);
		} catch (ScanningException | CancellationException e) {
			throw e;
		} catch (Exception e) {
			checkAborted(); // if the runner has already been aborted, we prefer to throw that exception
			throw new ScanningException("Interrupted while performing " + getLevelRole().toString().toLowerCase(), e);
		}  finally {
			logger.debug("Finishing iterating over devicesByLevel for scan for {}", position);
		}
		return true;
	}

	private void runLevel(final int level, IPosition loc, final List<L> devicesForLevel,
			boolean block, AnnotationManager annotationManager)
			throws IllegalAccessException, InvocationTargetException, InstantiationException, ScanningException,
			EventException, InterruptedException, ExecutionException {
		final List<Callable<IPosition>> tasks = createTasks(devicesForLevel, loc);

		logger.trace("Invoking LevelStart on {}", devicesForLevel);
		annotationManager.invoke(LevelStart.class, loc, new LevelInformation(getLevelRole(), level, devicesForLevel));
		logger.trace("Invoked  LevelStart on {}", devicesForLevel);

		if (block) {
			runLevelBlocking(loc, level, devicesForLevel, tasks);
		} else {
			runLevelNonBlocking(level, tasks);
		}

		logger.trace("Invoking LevelEnd on {}", devicesForLevel);
		annotationManager.invoke(LevelEnd.class, loc, new LevelInformation(getLevelRole(), level, devicesForLevel));
		logger.trace("Invoked  LevelEnd on {}", devicesForLevel);
	}

	private void runLevelBlocking(IPosition loc, final int level,
			final List<L> devicesForLevel, final List<Callable<IPosition>> tasks)
			throws InterruptedException, ScanningException, ExecutionException {
		// Normally we block until done.
		logger.debug("running blocking {} tasks for level {}", getLevelRole(), level);
		final List<Future<IPosition>> taskFutures = threadPool.invokeAll(tasks, getTimeout(), TimeUnit.SECONDS); // blocks until timeout
		checkAborted(); // first check if we were aborted in the meantime
		checkForCancelledTasks(level, devicesForLevel, taskFutures); // any timed-out tasks will have been cancelled
		pDelegate.fireLevelPerformed(level, devicesForLevel, getPosition(loc, taskFutures));
	}

	private void runLevelNonBlocking(final int level, final List<Callable<IPosition>> tasks) {
		// The last one and we are non-blocking
		logger.debug("running non-blocking {} tasks for level {}", getLevelRole(), level);
		for (Callable<IPosition> callable : tasks) {
			threadPool.submit(callable);
		}
	}

	private void checkForCancelledTasks(final int level, final List<L> devicesForLevel,
			final List<Future<IPosition>> taskFutures) throws ScanningException {
		// Tasks that were not complete by the timeout will have been cancelled by the ExecutorService
		if (taskFutures.stream().anyMatch(Future::isCancelled)) {
			logger.error("Timeout performing {} tasks at level {}", getLevelRole(), level);
			String message = getTimeoutMessage(level, devicesForLevel, taskFutures);
			logger.error(message);
			throw new ScanningException(message);
		} else {
			logger.debug("Finished performing {} tasks at level {}", getLevelRole(), level);
		}
	}

	private String getTimeoutMessage(final int level, final List<L> devices, final List<Future<IPosition>> taskFutures) {
		Stream<String> timedOutDeviceStrings = IntStream.range(0, devices.size())
				.filter(i -> taskFutures.get(i).isCancelled())
				.mapToObj(devices::get)
				.map(Object::toString);

		return String.format("The timeout of %ds has been reached waiting for level %d device(s): %s",
				timeout, level, String.join(", ", (Iterable<String>) timedOutDeviceStrings::iterator));
	}

	private List<Callable<IPosition>> createTasks(List<L> devices, IPosition location) {
		return devices.stream().map(obj -> createTask(obj, location)).filter(Objects::nonNull).collect(toList());
	}

	/**
	 * Blocks until all the tasks have complete. In order for this call to be worth using run(position, false) should
	 * have been used to run the service.
	 *
	 * If executor does not shutdown within 1 minute, throws an exception.
	 *
	 * If nothing has been run by the runner, there will be no executor service created and latch() will directly
	 * return.
	 * @throws ScanningException if the timeout occurred before the tasks were complete
	 */
	protected IPosition await() throws ScanningException, InterruptedException {
		return await(getTimeout());
	}

	/**
	 * Blocks until all the tasks have complete. In order for this call to be worth using run(position, false) should
	 * have been used to run the service.
	 *
	 * If nothing has been run by the runner, there will be no executor service created and latch() will directly
	 * return.
	 *
	 * @return the position of the last 'run' call, which may not always be what was awaited.
	 * @throws ScanningException if the timeout occurred before the tasks were complete
	 */
	protected IPosition await(long time) throws ScanningException, InterruptedException {
		checkAborted();

		if (threadPool.isTerminated()) {
			logger.warn("await() called when executorService is terminated");
			return position;
		}
		if (!threadPool.awaitQuiescence(time, TimeUnit.SECONDS)) { // Might have nullified service during wait.
			final String message = String.format("The timeout of %d seconds has been reached waiting for the scan to complete, scan aborting.", timeout);
			logger.error(message);
			throw new ScanningException(message);
		}
		return position;
	}

	private void checkAborted() throws InterruptedException, ScanningException {
		if (abortException != null) {
			if (abortException instanceof InterruptedException) {
				throw (InterruptedException) abortException;
			} else if (abortException instanceof ScanningException) {
				throw (ScanningException) abortException;
			} else {
				throw new ScanningException("Unexpected exception type: " + abortException.getClass(), abortException); // not expected
			}
		}
	}

	/**
	 * Abort the level runner.
	 */
	public void abort() {
		abort("User requested abort", new InterruptedException("User requested abort"));
	}

	/**
	 * Abort the level runner with an error
	 *
	 * @param device
	 * @param value
	 * @param pos
	 * @param ne
	 */
	protected void abortWithError(INameable device, Object value, IPosition pos, Exception ne) {
		final String message = "Cannot run device named '" + device.getName() + "' value is '" + value + "' and position is '"
				+ pos + "'\nMessage: " + ne.getMessage();
		abort(message, ne);
	}

	protected void abortWithError(INameable device, IPosition pos, Exception ne) {
		final String message = "Cannot run device named '" + device.getName() + "' position is '" + pos + "'\nMessage: "
				+ ne.getMessage();
		abort(message, ne);
	}

	private void abort(String message, Throwable ne) {
		logger.debug("Abort called while performing {}: {}", getLevelRole(), message, ne);

		synchronized (this) {
			// check we haven't already aborted. abort will be called multiple times if the thread
			// pool is shut down
			if (abortException != null) {
				return;
			}

			setAbortException(ne);
		}

		doAbort();
	}

	protected void doAbort() {
		shutdownThreadPool();
	}

	protected void shutdownThreadPool() {
		if (!threadPool.isShutdown()) {
			threadPool.shutdownNow();
		}
	}

	protected void setAbortException(Throwable ne) {
		if (ne instanceof InterruptedException) {
			abortException = (InterruptedException) ne;
		} else if (ne instanceof ScanningException) {
			abortException = (ScanningException) ne;
		} else {
			abortException = new ScanningException(ne.getMessage(), ne);
		}
	}

	/**
	 * Attempts to close the thread pool and log exceptions
	 */
	public void close() {
		try {
			shutdownThreadPool();
			if (!threadPool.isTerminated()) {
				threadPool.awaitTermination(getTimeout(), TimeUnit.SECONDS);
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			logger.debug("Unexpected forced termination of pool", e);
		}
	}

	/**
	 * Get the scannables, ordered by level, lowest first
	 *
	 * @return the ordered scannables
	 * @throws ScanningException
	 */
	private SortedMap<Integer, List<L>> getDevicesByLevel() throws ScanningException {
		if (devicesByLevel != null) {
			return devicesByLevel;
		}

		final SortedMap<Integer, List<L>> devicesByLevel = createDevicesByLevelMap();
		if (cachingEnabled) {
			this.devicesByLevel = devicesByLevel;
		}
		return devicesByLevel;
	}

	private SortedMap<Integer, List<L>> createDevicesByLevelMap() throws ScanningException {
		final Collection<L> devices = getDevices();

		final SortedMap<Integer, List<L>> result = new TreeMap<>();
		for (L object : devices) {
			final int level = object.getLevel();
			result.putIfAbsent(level, new ArrayList<L>());
			result.get(level).add(object);
		}

		return result;
	}

	private SortedMap<Integer, AnnotationManager> getAnnotationManagersByLevel(Map<Integer, List<L>> devicesByLevel) {
		if (this.annotationManagers != null) {
			return this.annotationManagers;
		}

		final SortedMap<Integer, AnnotationManager> annotationManagers = createAnnotationManagersByLevelMap(devicesByLevel);
		this.annotationManagers = annotationManagers;
		return annotationManagers;
	}

	private SortedMap<Integer, AnnotationManager> createAnnotationManagersByLevelMap(Map<Integer, List<L>> devicesByLevel) {
		final SortedMap<Integer, AnnotationManager> annotationManagers = new TreeMap<>();
		for (Entry<Integer, List<L>> posEntry : devicesByLevel.entrySet()) {
			// Less annotations is more efficient
			final Integer pos = posEntry.getKey();
			annotationManagers.put(pos, new AnnotationManager(SequencerActivator.getInstance(), LevelStart.class, LevelEnd.class));
			annotationManagers.get(pos).addDevices(posEntry.getValue());
		}
		return annotationManagers;
	}

	public void addPositionListener(IPositionListener listener) {
		pDelegate.addPositionListener(listener);
	}

	public void removePositionListener(IPositionListener listener) {
		pDelegate.removePositionListener(listener);
	}

	private IPosition getPosition(IPosition position, List<Future<IPosition>> futures) throws InterruptedException, ExecutionException {
		final MapPosition mapPosition = new MapPosition();
		for (Future<IPosition> future : futures) {
			final IPosition pos = future.get();
			if (pos != null) {
				mapPosition.putAll(pos);
				mapPosition.putAllIndices(pos);
			}
		}
		if (mapPosition.size() < 1) {
			return position;
		}
		return mapPosition;
	}

	public static <T extends ILevel> LevelRunner<T> createEmptyRunner() {
		return new LevelRunner<T>(null) {

			@Override
			protected boolean run(IPosition position, boolean block) {
				this.position = position;
				return true;
			}

			@Override
			protected IPosition await(long time) {
				return position;
			}

			@Override
			protected Collection<T> getDevices() throws ScanningException {
				return Collections.emptyList();
			}

			@Override
			protected Callable<IPosition> createTask(T levelObject, IPosition position) {
				return null;
			}

			@Override
			protected LevelRole getLevelRole() {
				return null;
			}

		};
	}

	/**
	 * @return the timeout (in seconds) used for running the scan and for waiting for {{@link #eservice} to finish
	 */
	protected long getTimeout() {
		if (Boolean.getBoolean("org.eclipse.scanning.sequencer.debug")) {
			return Long.MAX_VALUE; // So long that hell may have frozen over...
		}
		return timeout;
	}

	/**
	 * Set the await time
	 *
	 * @param time
	 *            The await time in seconds
	 */
	public void setTimeout(long time) {
		this.timeout = time;
	}

	public void setCachingEnabled(boolean cachingEnabled) {
		this.cachingEnabled = cachingEnabled;
	}

}
