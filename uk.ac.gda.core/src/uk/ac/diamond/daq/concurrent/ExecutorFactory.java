/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.concurrent;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.UnaryOperator;

/**
 * An {@link ExecutorService} factory to complement the Java {@link Executors} factory methods
 * and remove the need for creating a {@link ThreadFactory} manually.
 * <p>
 * Uses named daemon threads.
 *
 * @see Executors
 *
 * @since 9.8
 */
public final class ExecutorFactory {

	private static final String DEFAULT_EXECUTOR_THREAD_TEMPLATE = "GdaExecutorThread-%d";
	private static final String DEFAULT_THREAD_POOL_TEMPLATE = "GdaThreadPool-%d";
	private static final AtomicInteger poolCounter = new AtomicInteger();

	private ExecutorFactory() {}

	/**
	 * Create a single threaded ExecutorService.
	 * @return An {@link ExecutorService} that runs a single worker thread
	 * @see Executors#newSingleThreadExecutor()
	 */
	public static ExecutorService singleThread() {
		return singleThread(DEFAULT_EXECUTOR_THREAD_TEMPLATE, poolCounter.getAndIncrement());
	}

	/**
	 * Create a single threaded ExecutorService. The worker thread created will be named using
	 * the given format and arguments using {@link String#format(String, Object...)}.
	 *
	 * @param nameFormat The format String for the name
	 * @param args (optional) Formatting arguments for the name
	 * @return An {@link ExecutorService} that runs a single worker thread with the given name
	 *
	 * @see Executors#newSingleThreadExecutor()
	 */
	public static ExecutorService singleThread(String nameFormat, Object... args) {
		return Executors.newSingleThreadExecutor(Threads.named(nameFormat, args).factory());
	}

	/**
	 * Create a thread pool ExecutorService that uses the given number of threads.
	 *
	 * @return An {@link ExecutorService} that runs a fixed number of worker threads
	 * @see Executors#newFixedThreadPool(int)
	 */
	public static ExecutorService threadPool(int poolSize) {
		return threadPool(poolSize, DEFAULT_THREAD_POOL_TEMPLATE, poolCounter.getAndIncrement());
	}

	/**
	 * Create a thread pool ExecutorService that uses the given number of threads. The worker
	 * threads will be named using the given format and arguments using {@link String#format(String, Object...)}.
	 *
	 * @param nameFormat Format to use to name the threads in the pool
	 * @param args (optional) Formatting arguments for the name
	 * @return An {@link ExecutorService} that runs a fixed number of worker threads
	 *
	 * @see Executors#newFixedThreadPool(int)
	 */
	public static ExecutorService threadPool(int poolSize, String nameFormat, Object... args) {
		return Executors.newFixedThreadPool(poolSize, Threads.named(nameFormat, args).factory());
	}

	/**
	 * Create a thread pool ExecutorService that reuses existing threads if possible but creates new ones
	 * if none are available.
	 *
	 * @return An {@link ExecutorService} that runs a pool of worker threads
	 *
	 * @see Executors#newCachedThreadPool()
	 */
	public static ExecutorService cachedThreadPool() {
		return cachedThreadPool(DEFAULT_THREAD_POOL_TEMPLATE, poolCounter.getAndIncrement());
	}

	/**
	 * Create a thread pool ExecutorService that reuses existing threads if possible but creates new Threads
	 * if none are available. The worker threads will be named using the given format and arguments using
	 * {@link String#format(String, Object...)}.
	 *
	 * @param nameFormat Format to use to name the threads in the pool
	 * @param args (optional) Formatting arguments for the name
	 * @return An {@link ExecutorService} that runs a fixed number of worker threads
	 *
	 * @see Executors#newCachedThreadPool()
	 */
	public static ExecutorService cachedThreadPool(String nameFormat, Object... args) {
		return Executors.newCachedThreadPool(Threads.named(nameFormat, args).factory());
	}

	/**
	 * Create a {@link ScheduledExecutorService} that uses a pool of threads for execution.
	 * Threads created by the executor will be named using the given format and arguments using
	 * {@link String#format(String, Object...)}.
	 * <p>
	 * In most cases it is possible to use the common scheduling methods in {@link Async} instead.
	 *
	 * @param poolSize Number of threads to create in thread pool
	 * @param nameFormat Format to use to name the threads in the pool
	 * @param args (optional) Formatting arguments for the name
	 * @return a {@link ScheduledExecutorService} that uses a thread pool for execution
	 *
	 * @see Executors#newScheduledThreadPool(int)
	 * @see Async
	 */
	public static ScheduledExecutorService scheduled(int poolSize, String nameFormat, Object... args) {
		return Executors.newScheduledThreadPool(poolSize,
				Threads.named(nameFormat, args)
				.factory());
	}

	/**
	 * Create a {@link ScheduledExecutorService} that uses a fixed pool of threads.
	 *
	 * @return a {@link ScheduledExecutorService} that uses a fixed thread pool for execution.
	 *
	 * @see #scheduled(int, String, Object...)
	 * @see Executors#newScheduledThreadPool(int)
	 */
	public static ScheduledExecutorService scheduled(int poolSize) {
		return scheduled(poolSize, DEFAULT_THREAD_POOL_TEMPLATE, poolCounter.getAndIncrement());
	}

	/**
	 * Wrap an {@link ExecutorService} so that every task is wrapped before being submitted.
	 * @param <T> The type of the ExecutorService to be wrapped.
	 * @param service The ExecutorService that will actually run all tasks.
	 * @param wrapper A function to wrap Callables. Runnable tasks are wrapped as Callables
	 * before being passed to the same function (See {@link Executors#callable(Runnable)}.
	 *
	 * @return An ExecutorService that wraps tasks before running them.
	 */
	public static <T extends ExecutorService> ExecutorWrappers.ExecutorWrapper<T> wrap(T service, UnaryOperator<Callable<?>> wrapper) {
		return new ExecutorWrappers.ExecutorWrapper<T>(service) {
			@SuppressWarnings("unchecked")
			@Override
			<U> Callable<U> wrapCallable(Callable<U> task) {
				return (Callable<U>) wrapper.apply(task);
			}
		};
	}

	/**
	 * Wrap a {@link ScheduledExecutorService} so that every task is wrapped before being submitted.
	 * @param <T> The type of the {@link ScheduledExecutorService} to be wrapped.
	 * @param service The ExecutorService that will actually run all tasks.
	 * @param wrapper A function to wrap Callables. Runnable tasks are wrapped as Callables
	 * before being passed to the same function (See {@link Executors#callable(Runnable)}.
	 *
	 * @return A ScheduledExecutorService that wraps tasks before running them.
	 */
	public static <T extends ScheduledExecutorService> ExecutorWrappers.ScheduleExecutorWrapper<T> wrap(T service, UnaryOperator<Callable<?>> wrapper) {
		return new ExecutorWrappers.ScheduleExecutorWrapper<T>(service) {
			@SuppressWarnings("unchecked")
			@Override
			<U> Callable<U> wrapCallable(Callable<U> task) {
				return (Callable<U>) wrapper.apply(task);
			}
		};
	}

}
