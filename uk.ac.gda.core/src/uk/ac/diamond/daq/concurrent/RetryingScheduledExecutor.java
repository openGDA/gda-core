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
import java.util.concurrent.Delayed;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.RunnableScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.python.core.PyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.jython.logging.PythonException;

/** A {@link ScheduledThreadPoolExecutor} that allows periodic commands
 * to continue running after failure with an exponential back off policy.
 * <p>
 * When a task fails, the delay between runs will increase according to the provided
 * scale factor (subject to the minimum and maximum bounds specified). The delay will
 * be reset after a successful execution.
 * <p>
 * A command can stop itself by throwing {@link AbortScheduledException}.
 * Any other {@link Throwable} that does not derive from {@link Exception}
 * will also halt further executions.
 */
public class RetryingScheduledExecutor extends ScheduledThreadPoolExecutor {

	private static final Logger logger = LoggerFactory.getLogger(RetryingScheduledExecutor.class);

	private static final long DEFAULT_MIN_BACKOFF_NS = 1000000000L;
	private static final long DEFAULT_MAX_BACKOFF_NS = 30000000000L;
	private static final double DEFAULT_BACKOFF_SCALE = 1.5;

	public RetryingScheduledExecutor(int corePoolSize, RejectedExecutionHandler handler) {
		super(corePoolSize, handler);
	}

	public RetryingScheduledExecutor(int corePoolSize, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
		super(corePoolSize, threadFactory, handler);
	}

	public RetryingScheduledExecutor(int corePoolSize, ThreadFactory threadFactory) {
		super(corePoolSize, threadFactory);
	}

	public RetryingScheduledExecutor(int corePoolSize) {
		super(corePoolSize);
	}

	@Override
	public RetryingScheduledFutureTask<?> scheduleAtFixedRate(
			Runnable command, long initialDelay, long period, TimeUnit unit) {
		return scheduleAtFixedRate(
				command, initialDelay, period,
				unit.convert(DEFAULT_MIN_BACKOFF_NS, TimeUnit.NANOSECONDS),
				unit.convert(DEFAULT_MAX_BACKOFF_NS, TimeUnit.NANOSECONDS),
				unit, DEFAULT_BACKOFF_SCALE);
	}

	public RetryingScheduledFutureTask<?> scheduleAtFixedRate(
			Runnable command, long initialDelay, long period,
			long minBackOffTime, long maxBackOffTime, TimeUnit unit,
			double backOffScale) {
		RunnableWithRetryParams r = new RunnableWithRetryParams(command);
		r.period = unit.toNanos(period);
		r.backOffMultiplier = backOffScale;
		r.maxBackOffTime = unit.toNanos(maxBackOffTime);
		r.minBackOffTime = unit.toNanos(minBackOffTime);
		return (RetryingScheduledFutureTask<?>) super.scheduleAtFixedRate(
				r, initialDelay, period, unit);
	}

	@Override
	public RetryingScheduledFutureTask<?> scheduleWithFixedDelay(
			Runnable command, long initialDelay, long period, TimeUnit unit) {
		return scheduleWithFixedDelay(
				command, initialDelay, period,
				unit.convert(DEFAULT_MIN_BACKOFF_NS, TimeUnit.NANOSECONDS),
				unit.convert(DEFAULT_MAX_BACKOFF_NS, TimeUnit.NANOSECONDS),
				unit, DEFAULT_BACKOFF_SCALE);
	}

	public RetryingScheduledFutureTask<?> scheduleWithFixedDelay(
			Runnable command, long initialDelay, long period,
			long minBackOffTime, long maxBackOffTime, TimeUnit unit,
			double backOffScale) {
		RunnableWithRetryParams r = new RunnableWithRetryParams(command);
		r.period = unit.toNanos(-period);
		r.backOffMultiplier = backOffScale;
		r.maxBackOffTime = unit.toNanos(maxBackOffTime);
		r.minBackOffTime = unit.toNanos(minBackOffTime);
		return (RetryingScheduledFutureTask<?>) super.scheduleWithFixedDelay(
				r, initialDelay, period, unit);
	}

	@Override
	protected <V> RetryingScheduledFutureTask<V> decorateTask(
			Callable<V> callable, RunnableScheduledFuture<V> task) {
		long delayTime = task.getDelay(TimeUnit.NANOSECONDS);
		return RetryingScheduledFutureTask.createFromCallable(this, callable, delayTime);
	}

	@Override
	protected <V> RetryingScheduledFutureTask<V> decorateTask(
			Runnable runnable, RunnableScheduledFuture<V> task) {
		long delayTime = task.getDelay(TimeUnit.NANOSECONDS);
		return RetryingScheduledFutureTask.createFromRunnable(this, runnable, delayTime);
	}

	private <V> void reExecute(RetryingScheduledFutureTask<V> task) {
		if (!super.isTerminating() && !super.isShutdown()) {
			super.getQueue().add(task);
		}
	}


	/** Runnable wrapper to hold the retry/backoff parameters for use in decorateTask */
	private class RunnableWithRetryParams implements Runnable {

		private long period = 0;
		private double backOffMultiplier = DEFAULT_BACKOFF_SCALE;
		private long maxBackOffTime = DEFAULT_MAX_BACKOFF_NS;
		private long minBackOffTime = DEFAULT_MIN_BACKOFF_NS;
		private Runnable runnable;

		public RunnableWithRetryParams(Runnable runnable) {
			this.runnable = runnable;
		}

		@Override
		public void run() {
			runnable.run();
		}

	}


	private static class RetryingScheduledFutureTask<V> extends FutureTask<V> implements RunnableScheduledFuture<V> {

		private final RetryingScheduledExecutor executor;

		/** How the period should be scaled by each successive failure as a form of back off */
		private double backOffMultiplier = 1.5;

		/** Maximum delay in nanoseconds that can be added by back offs */
		private long maxBackOffTime;

		/** Minimum additional in nanoseconds delay for a back off */
		private long minBackOffTime;

		/** Set if the last run raised an exception */
		private boolean failed = false;

		/** The time the task is enabled to execute in nanoTime units */
		private long triggerTime;

		/**
		 * Period in nanoseconds for repeating tasks.
		 * period > 0 -> Fixed rate execution.
		 * period < 0 -> Fixed delay execution.
		 * period == 0 -> Non-repeating
		 */
		private long initialPeriod;

		/** Current period after backoff delays **/
		private long activePeriod;

		public static <V> RetryingScheduledFutureTask<V> createFromCallable(
				RetryingScheduledExecutor executor, Callable<V> callable, long delay) {
			return create(executor, callable, delay, 0, 1, 0, 0);
		}

		public static <V> RetryingScheduledFutureTask<V> createFromRunnable(
				RetryingScheduledExecutor executor, Runnable runnable, long delay) {
			Callable<V> callable = Executors.callable(runnable, null);
			if (runnable instanceof RunnableWithRetryParams) {
				RunnableWithRetryParams rParams = (RunnableWithRetryParams) runnable;
				return create(executor,
						callable,
						delay,
						rParams.period,
						rParams.backOffMultiplier,
						rParams.maxBackOffTime,
						rParams.minBackOffTime);
			} else {
				return create(executor, callable, delay, 0, 1, 0, 0);
			}
		}

		private static <V> RetryingScheduledFutureTask<V> create(
				RetryingScheduledExecutor executor, Callable<V> callable,
				long delayTime, long period,
				double backOffMultiplier, long maxBackOffTime, long minBackOffTime) {

			CallableWrapper<V> wrappedCallable = new CallableWrapper<V>() {
				@Override
				public V call() {
					RetryingScheduledFutureTask<V> task = getParentTask();
					V returnValue = null;
					try {
						try {
							returnValue = callable.call();
							task.failed = false;
						} catch (PyException e) {
							// extract a potential AbortScheduledException to throw up
							Throwable c = PythonException.from(e);
							throw c instanceof AbortScheduledException ? (AbortScheduledException) c : e;
						}
					} catch (AbortScheduledException e) {
						logger.debug("Callable aborted execution", e);
						throw e;
					} catch (Exception e) {
						// only log the first failure at the error level - sequential failures are not interesting
						boolean logError = !task.failed;
						task.failed = true;

						// TODO: include a way of identifying a callable and logging when it executes properly again
						String logMessage = "Callable threw exception";
						if (task.initialPeriod != 0) {
							logMessage = String.format("%s - period time is %d ns", logMessage, Math.abs(task.activePeriod));
						}
						if (logError) {
							logger.error(logMessage, e);
						} else {
							logger.trace(logMessage, e);
						}
					}
					task.updateNextPeriod();
					return returnValue;
				}
			};

			long triggerTime = System.nanoTime() + delayTime;
			RetryingScheduledFutureTask<V> task = new RetryingScheduledFutureTask<>(
					wrappedCallable, executor, triggerTime);
			task.initialPeriod = period;
			task.activePeriod = period;
			task.backOffMultiplier = backOffMultiplier;
			task.maxBackOffTime = maxBackOffTime;
			task.minBackOffTime = minBackOffTime;
			wrappedCallable.setParentTask(task);
			return task;
		}

		private RetryingScheduledFutureTask(Callable<V> callable, RetryingScheduledExecutor executor, long triggerTime) {
			super(callable);
			this.executor = executor;
			this.triggerTime = triggerTime;
		}

		@Override
		public long getDelay(TimeUnit unit) {
			return unit.convert(triggerTime - System.nanoTime(), TimeUnit.NANOSECONDS);
		}

		@Override
		public int compareTo(Delayed other) {
			if (other == this) return 0;
			long diff = getDelay(TimeUnit.NANOSECONDS) - other.getDelay(TimeUnit.NANOSECONDS);
			return (diff < 0) ? -1 : (diff > 0) ? 1 : 0;
		}

		@Override
		public boolean isPeriodic() {
			return initialPeriod != 0;
		}

		@Override
		public void run() {
			boolean periodic = isPeriodic();
			if (!periodic) {
				super.run();
			} else if (super.runAndReset()) {
				if (activePeriod > 0) {
					triggerTime += activePeriod;
				} else {
					triggerTime = System.nanoTime() + activePeriod * -1;
				}
				executor.reExecute(this);
			}
		}

		private void updateNextPeriod() {
			if (!failed) {
				activePeriod = initialPeriod;
			} else {
				long ip = initialPeriod < 0 ? initialPeriod * -1 : initialPeriod;
				long ap = activePeriod < 0 ? activePeriod * -1 : activePeriod;
				long nextPeriod = (long) (ap * backOffMultiplier);
				if ((nextPeriod - ip) < minBackOffTime) nextPeriod = ip + minBackOffTime;
				if ((nextPeriod - ip) > maxBackOffTime) nextPeriod = ip + maxBackOffTime;

				activePeriod = initialPeriod < 0 ? -nextPeriod : nextPeriod;
			}
		}

		/** Holds a reference to the parent task so the implementing callable can update failure state */
		private abstract static class CallableWrapper<V> implements Callable<V> {

			private RetryingScheduledFutureTask<V> parentTask;

			public void setParentTask(RetryingScheduledFutureTask<V> parentTask) {
				this.parentTask = parentTask;
			}

			public RetryingScheduledFutureTask<V> getParentTask() {
				return parentTask;
			}
		}
	}
}
