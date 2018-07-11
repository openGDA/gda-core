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

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

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

	protected IPosition position;
	private volatile ForkJoinPool eservice; // Different threads may nullify the service, better to make volatile.
	private ScanningException abortException;
	private PositionDelegate pDelegate;
	private boolean levelCachingAllowed = true;

	private SoftReference<Map<Integer, List<L>>> sortedObjects;
	private SoftReference<Map<Integer, AnnotationManager>> sortedManagers;

	/**
	 * The timeout (in seconds) is overridden by some subclasses.
	 */
	private long timeout = Long.getLong("org.eclipse.scanning.sequencer.default.timeout", 10);

	protected LevelRunner(INameable device) {
		pDelegate = new PositionDelegate(device);
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
	 *
	 * @throws ScanningException
	 */
	protected abstract Callable<IPosition> create(L levelObject, IPosition position) throws ScanningException;

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

		if (abortException != null) {
			throw abortException;
		}

		/**
		 * NOTE: The position is passed down to run in the thread pool. A subsequent run and await could in theory
		 * return the last run position while returning the last-1 run position. Position is a best guess of what
		 * position happened.
		 */
		this.position = loc;
		if (!pDelegate.firePositionWillPerform(loc)) {
			return false;
		}

		final Map<Integer, List<L>> positionMap = getLevelOrderedDevices();
		final Map<Integer, AnnotationManager> managerMap = getLevelOrderedManagerMap(positionMap);

		try {
			// TODO Should we actually create the service size to the size
			// of the largest level population? This would mean that you try to
			// start everything at the same time.
			if (eservice == null) {
				this.eservice = createService();
			}

			final Integer finalLevel = 0;
			for (Iterator<Integer> it = positionMap.keySet().iterator(); it.hasNext();) {

				if (abortException != null) {
					throw abortException;
				}

				final int level = it.next();
				final List<L> lobjects = positionMap.get(level);
				final Collection<Callable<IPosition>> tasks = new ArrayList<>(lobjects.size());
				for (L lobject : lobjects) {
					final Callable<IPosition> c = create(lobject, loc);
					if (c != null) { // legal to say that there is nothing to do for a given object.
						tasks.add(c);
					}
				}

				managerMap.get(level).invoke(LevelStart.class, loc, new LevelInformation(getLevelRole(), level, lobjects));
				if (!it.hasNext() && !block) {
					// The last one and we are non-blocking
					for (Callable<IPosition> callable : tasks) {
						eservice.submit(callable);
					}
				} else {
					// Normally we block until done.
					// Blocks until level has run
					final List<Future<IPosition>> pos = eservice.invokeAll(tasks, getTimeout(), TimeUnit.SECONDS);

					// If timed out, some isDone will be false.
					for (Future<IPosition> future : pos) {
						if (!future.isDone()) {
							throw new ScanningException(
									"The timeout of " + timeout + "s has been reached waiting for level " + level
											+ " objects " + toString(lobjects));
						}
					}
					pDelegate.fireLevelPerformed(level, lobjects, getPosition(loc, pos));
				}
				managerMap.get(level).invoke(LevelEnd.class, loc, new LevelInformation(getLevelRole(), level, lobjects));
			}

			pDelegate.firePositionPerformed(finalLevel, loc);
		} catch (ScanningException | CancellationException e) {
			throw e;
		} catch (Exception e) {
			if (abortException != null) {
				throw abortException;
			}
			throw new ScanningException("Scanning interrupted while moving to new position!", e);
		} finally {
			if (block) {
				await();
			}
		}

		return true;
	}

	protected String toString(List<L> lobjects) {
		final StringBuilder buf = new StringBuilder("[");
		for (L l : lobjects) {
			buf.append(l);
			buf.append(", ");
		}
		return buf.toString();
	}

	/**
	 * Blocks until all the tasks have complete. In order for this call to be worth using run(position, false) should
	 * have been used to run the service.
	 *
	 * If executor does not shutdown within 1 minute, throws an exception.
	 *
	 * If nothing has been run by the runner, there will be no executor service created and latch() will directly
	 * return.
	 */
	protected IPosition await() throws ScanningException {
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
	 */
	protected IPosition await(long time) throws ScanningException {
		if (abortException != null) {
			throw abortException;
		}
		if (eservice == null) {
			return position;
		}
		if (eservice.isTerminated()) {
			eservice = null;
			return position;
		}
		if (!eservice.awaitQuiescence(time, TimeUnit.SECONDS)) { // Might have nullified service during wait.
			throw new ScanningException("The timeout of " + timeout
					+ "s has been reached, scan aborting. Please implement ITimeoutable to define how long your device needs to write.");
		}
		return position;
	}

	/**
	 * Abort the level runner.
	 */
	public void abort() {
		if (eservice == null) {
			return; // We are already finished
		}
		eservice.shutdownNow();
		eservice = null;
	}

	/**
	 * Abort the level runner with an error
	 *
	 * @param device
	 * @param value
	 * @param pos
	 * @param ne
	 */
	protected void abortWithError(INameable device, Object value, IPosition pos, Throwable ne) {
		final String message = "Cannot run device named '" + device.getName() + "' value is '" + value + "' and position is '"
				+ pos + "'\nMessage: " + ne.getMessage();
		abort(message, ne);
	}

	protected void abortWithError(INameable device, IPosition pos, Throwable ne) {
		final String message = "Cannot run device named '" + device.getName() + "' position is '" + pos + "'\nMessage: "
				+ ne.getMessage();
		abort(message, ne);
	}

	private void abort(String message, Throwable ne) {
		logger.debug(message, ne); // Just for testing we make sure that the stack is visible.
		abortException = ne instanceof ScanningException ? (ScanningException) ne
				: new ScanningException(ne.getMessage(), ne);
		if (eservice != null) {
			eservice.shutdownNow();
			eservice = null;
		}
	}

	/**
	 * Attempts to close the thread pool and log exceptions
	 */
	public void close() {
		if (eservice == null) {
			return; // We are already finished
		}
		try {
			eservice.shutdown();
			eservice.awaitTermination(getTimeout(), TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			logger.debug("Unexpected forced termination of pool", e);
		} finally {
			eservice = null;
		}
	}

	public void reset() {
		if (abortException != null) {
			logger.trace("Resetting abortException to null, was ", abortException);
		}
		abortException = null;
	}

	/**
	 * Get the scannables, ordered by level, lowest first
	 *
	 * @return the ordered scannables
	 * @throws ScanningException
	 */
	private Map<Integer, List<L>> getLevelOrderedDevices() throws ScanningException {
		if (sortedObjects != null && sortedObjects.get() != null) {
			return sortedObjects.get();
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
			sortedObjects = new SoftReference<>(devicesByLevel);
		}

		return devicesByLevel;
	}

	private Map<Integer, AnnotationManager> getLevelOrderedManagerMap(Map<Integer, List<L>> positionMap) {

		if (sortedManagers != null && sortedManagers.get() != null) {
			return sortedManagers.get();
		}

		final Map<Integer, AnnotationManager> ret = new HashMap<>();
		for (Entry<Integer, List<L>> posEntry : positionMap.entrySet()) {
			// Less annotations is more efficient
			final Integer pos = posEntry.getKey();
			ret.put(pos, new AnnotationManager(SequencerActivator.getInstance(), LevelStart.class, LevelEnd.class));
			ret.get(pos).addDevices(posEntry.getValue());
		}
		if (levelCachingAllowed) {
			sortedManagers = new SoftReference<>(ret);
		}
		return ret;
	}

	private ForkJoinPool createService() {
		// TODO Need spring config for this.
		Integer processors = Integer.getInteger("org.eclipse.scanning.level.runner.pool.count");
		if (processors == null || processors < 1) {
			processors = Runtime.getRuntime().availableProcessors();
		}
		return new ForkJoinPool(processors);
		// Slightly faster than thread pool executor @see ScanAlgorithmBenchMarkTest
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
			protected Callable<IPosition> create(T levelObject, IPosition position) throws ScanningException {
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
