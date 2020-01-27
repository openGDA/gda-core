/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Implementations of {@link ExecutorService} and {@link ScheduledExecutorService} that allow all tasks
 * to be wrapped before being submitted.
 */
public class ExecutorWrappers {

	/**
	 * {@link ExecutorService} that wraps all tasks before submitting to a delegate service. Implementations
	 * should only need to implement {link {@link #wrapCallable(Callable)} although {link #wrapRunnable} can
	 * be customised as well if required.
	 *
	 * @param <S> The type of ExecutorService wrapped by this service. See {@link #getWrapped()}
	 */
	public static abstract class ExecutorWrapper<S extends ExecutorService> implements ExecutorService {
		protected final S service;

		public ExecutorWrapper(S service) {
			this.service = service;
		}

		protected Runnable wrapRunnable(Runnable task) {
			Callable<?> callable = wrapCallable(Executors.callable(task));
			return new Runnable() {
				@Override
				public void run() {
					try {
						callable.call();
					} catch (RuntimeException re) {
						throw re;
					} catch (Exception e) {
						// Should never happen unless wrapCallable implementation does weird things
						throw new RuntimeException(e);
					}
				}
			};
		}

		abstract <T> Callable<T> wrapCallable(Callable<T> task);

		private <T> Collection<Callable<T>> wrapCallables(Collection<? extends Callable<T>> tasks) {
			return tasks.stream()
					.map(this::wrapCallable)
					.collect(toList());
		}

		@Override
		public void execute(Runnable command) {
			service.execute(wrapRunnable(command));
		}

		@Override
		public void shutdown() {
			service.shutdown();
		}

		@Override
		public List<Runnable> shutdownNow() {
			return service.shutdownNow();
		}

		@Override
		public boolean isShutdown() {
			return service.isShutdown();
		}

		@Override
		public boolean isTerminated() {
			return service.isTerminated();
		}

		@Override
		public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
			return service.awaitTermination(timeout, unit);
		}

		@Override
		public <T> Future<T> submit(Callable<T> task) {
			return service.submit(wrapCallable(task));
		}

		@Override
		public <T> Future<T> submit(Runnable task, T result) {
			return service.submit(wrapRunnable(task), result);
		}

		@Override
		public Future<?> submit(Runnable task) {
			return service.submit(wrapRunnable(task));
		}

		@Override
		public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
			return service.invokeAll(wrapCallables(tasks));
		}

		@Override
		public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
				throws InterruptedException {
			return service.invokeAll(wrapCallables(tasks), timeout, unit);
		}

		@Override
		public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
			return service.invokeAny(wrapCallables(tasks));
		}

		@Override
		public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
				throws InterruptedException, ExecutionException, TimeoutException {
			return service.invokeAny(wrapCallables(tasks), timeout, unit);
		}

		/** Get the delegate {@link ExecutorService} wrapped by this service */
		public S getWrapped() {
			return service;
		}
	}

	/**
	 * {@link ScheduledExecutorService} that wraps all tasks before submitting to a delegate service. Implementations
	 * should only need to implement {link {@link #wrapCallable(Callable)} although {link #wrapRunnable} can
	 * be customised as well if required.
	 *
	 * @param <S> The type of ScheduledExecutorService wrapped by this service. See {@link #getWrapped()}
	 */
	public static abstract class ScheduleExecutorWrapper<S extends ScheduledExecutorService> extends ExecutorWrapper<S> implements ScheduledExecutorService {
		public ScheduleExecutorWrapper(S service) {
			super(service);
		}
		@Override
		public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
			return service.schedule(wrapRunnable(command), delay, unit);
		}
		@Override
		public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
			return service.schedule(wrapCallable(callable), delay, unit);
		}
		@Override
		public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
			return service.scheduleAtFixedRate(wrapRunnable(command), initialDelay, period, unit);
		}
		@Override
		public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay,
				TimeUnit unit) {
			return service.scheduleWithFixedDelay(wrapRunnable(command), initialDelay, delay, unit);
		}
	}
}
