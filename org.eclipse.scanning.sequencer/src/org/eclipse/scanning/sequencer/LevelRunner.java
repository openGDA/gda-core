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
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
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

	private final ForkJoinPool executorService;
	private final PositionDelegate pDelegate;

	protected IPosition position;
	private ScanningException abortException;
	private boolean levelCachingAllowed = true;

	private SoftReference<Map<Integer, List<L>>> devicesByLevel;
	private SoftReference<Map<Integer, AnnotationManager>> annotationManagers;

	/**
	 * The timeout (in seconds) is overridden by some subclasses.
	 */
	private long timeout = Long.getLong("org.eclipse.scanning.sequencer.default.timeout", 10);

	protected LevelRunner(INameable device) {
		pDelegate = new PositionDelegate(device);
		executorService = createExecutorService();
	}

	private ForkJoinPool createExecutorService() {
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
	 * @return the objects which we would like to order by level
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
	 * Call to set the value at the location specified<br>
	 * Same as calling run(position, true)
	 *
	 * For parameters, return code & exceptions see {{@link #run(IPosition, boolean)}
	 */
	protected boolean run(IPosition position) throws ScanningException {
		return run(position, true);
	}

	/**
	 * Call to set the value at the location specified
	 *
	 * @param loc
	 *            the position to move to
	 * @param block
	 *            set to <code>true</code> to block until the parallel tasks have completed.<br>
	 *            set to <code>false</code> to return after the last level is told to execute.<br>
	 *            In this case more work can be done on the calling thread. Use the latch() method to come back to the
	 *            last level's ExecutorService
	 * @return <code>true</code> if the run performed normally, <code>false</code> otherwise, for instance because
	 *         {@link IPositionListener#positionWillPerform(org.eclipse.scanning.api.scan.PositionEvent)} returned false
	 *         for some position listener
	 * @throws CancellationException
	 *             can be thrown if abort has been called while blocking
	 * @throws ScanningException
	 *             if the value cannot be set for any other reason
	 */
	protected boolean run(IPosition loc, boolean block) throws ScanningException {
		if (abortException != null) throw abortException;

		/**
		 * NOTE: The position is passed down to run in the thread pool. A subsequent run and await could in theory
		 * return the last run position while returning the last-1 run position. Position is a best guess of what
		 * position happened.
		 */
		logger.debug("running {} for position {}", getLevelRole(), loc);
		this.position = loc;
		if (!pDelegate.firePositionWillPerform(loc)) {
			return false;
		}

		final Map<Integer, List<L>> devicesByLevel = getDevicesByLevel();
		final Map<Integer, AnnotationManager> annotationManagersByLevel = getAnnotationManagersByLevel(devicesByLevel);

		try {
			final Integer finalLevel = 0;
			for (Iterator<Integer> levelIterator = devicesByLevel.keySet().iterator(); levelIterator.hasNext();) {
				if (abortException != null) throw abortException; // check aborted

				final int level = levelIterator.next();
				final List<L> devicesForLevel = devicesByLevel.get(level);
				final List<Callable<IPosition>> tasks = createTasks(devicesForLevel, loc);

				annotationManagersByLevel.get(level).invoke(LevelStart.class, loc, new LevelInformation(getLevelRole(), level, devicesForLevel));

				if (block || levelIterator.hasNext()) {
					runLevelBlocking(loc, level, devicesForLevel, tasks);
				} else {
					runLevelNonBlocking(level, tasks);
				}

				annotationManagersByLevel.get(level).invoke(LevelEnd.class, loc, new LevelInformation(getLevelRole(), level, devicesForLevel));
			}

			pDelegate.firePositionPerformed(finalLevel, loc);
		} catch (ScanningException | CancellationException e) {
			throw e;
		} catch (Exception e) {
			if (abortException != null) {
				throw abortException;
			}
			throw new ScanningException("Scanning interrupted while moving to new position!", e);
		}

		return true;
	}

	private void runLevelBlocking(IPosition loc, final int level,
			final List<L> devicesForLevel, final List<Callable<IPosition>> tasks)
			throws InterruptedException, ScanningException, ExecutionException {
			// Normally we block until done.
			logger.debug("running blocking {} tasks for level {}", getLevelRole(), level);
			final List<Future<IPosition>> taskFutures = executorService.invokeAll(tasks, getTimeout(), TimeUnit.SECONDS); // blocks until timeout
			checkForCancelledTasks(level, devicesForLevel, taskFutures); // any timed-out tasks will have been cancelled
			pDelegate.fireLevelPerformed(level, devicesForLevel, getPosition(loc, taskFutures));
	}

	private void runLevelNonBlocking(final int level, final List<Callable<IPosition>> tasks) {
		// The last one and we are non-blocking
		logger.debug("running non-blocking {} tasks for level {}", getLevelRole(), level);
		for (Callable<IPosition> callable : tasks) {
			executorService.submit(callable);
		}
	}

	private void checkForCancelledTasks(final int level, final List<L> devicesForLevel,
			final List<Future<IPosition>> taskFutures) throws ScanningException {
		// Check first for abort exception
		if (abortException != null) throw abortException;

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
		if (abortException != null) {
			throw abortException;
		}
		if (executorService.isTerminated()) {
			logger.warn("await() called when executorService is terminated");
			return position;
		}
		if (!executorService.awaitQuiescence(time, TimeUnit.SECONDS)) { // Might have nullified service during wait.
			final String message = String.format("The timeout of %d seconds has been reached waiting for the scan to complete, scan aborting.", timeout);
			logger.error(message);
			throw new ScanningException(message);
		}
		return position;
	}

	/**
	 * Abort the level runner.
	 */
	public void abort() {
		abort("User requested abort", new ScanningException("User requested abort"));
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
		logger.debug(message, ne); // Just for testing we make sure that the stack is visible.
		abortException = ne instanceof ScanningException ? (ScanningException) ne
				: new ScanningException(ne.getMessage(), ne);
		if (!executorService.isShutdown()) {
			executorService.shutdownNow();
		}
	}

	/**
	 * Attempts to close the thread pool and log exceptions
	 */
	public void close() {
		try {
			if (!executorService.isShutdown()) {
				executorService.shutdownNow();
			}
			if (!executorService.isTerminated()) {
				executorService.awaitTermination(getTimeout(), TimeUnit.SECONDS);
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
	private Map<Integer, List<L>> getDevicesByLevel() throws ScanningException {
		if (devicesByLevel != null && devicesByLevel.get() != null) {
			return devicesByLevel.get();
		}

		final Collection<L> devices = getDevices();

		if (devices == null) {
			return Collections.emptyMap();
		}

		final Map<Integer, List<L>> devicesByLevel = new TreeMap<>();
		for (L object : devices) {
			final int level = object.getLevel();

			if (!devicesByLevel.containsKey(level)) {
				devicesByLevel.put(level, new ArrayList<L>(7));
			}
			devicesByLevel.get(level).add(object);
		}
		if (levelCachingAllowed) {
			this.devicesByLevel = new SoftReference<>(devicesByLevel);
		}

		return devicesByLevel;
	}

	private Map<Integer, AnnotationManager> getAnnotationManagersByLevel(Map<Integer, List<L>> devicesByLevel) {

		if (annotationManagers != null && annotationManagers.get() != null) {
			return annotationManagers.get();
		}

		final Map<Integer, AnnotationManager> ret = new HashMap<>();
		for (Entry<Integer, List<L>> posEntry : devicesByLevel.entrySet()) {
			// Less annotations is more efficient
			final Integer pos = posEntry.getKey();
			ret.put(pos, new AnnotationManager(SequencerActivator.getInstance(), LevelStart.class, LevelEnd.class));
			ret.get(pos).addDevices(posEntry.getValue());
		}
		if (levelCachingAllowed) {
			annotationManagers = new SoftReference<>(ret);
		}
		return ret;
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

	public void setLevelCachingAllowed(boolean levelCachingAllowed) {
		this.levelCachingAllowed = levelCachingAllowed;
	}

}
