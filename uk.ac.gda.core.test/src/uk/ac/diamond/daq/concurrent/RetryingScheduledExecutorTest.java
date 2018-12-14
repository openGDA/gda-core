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

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class RetryingScheduledExecutorTest {

	private RetryingScheduledExecutor executor;

	private List<Long> callTimes;

	@Before
	public void setup() {
		callTimes = new ArrayList<Long>(8);
		executor = new RetryingScheduledExecutor(1);
	}

	@After
	public void teardown() {
		executor.shutdownNow();
	}

	@Test
	public void testSubmitCallable() throws Exception {
		Callable<Long> callable = () -> {
			return System.nanoTime();
		};
		long startTime = System.nanoTime();
		Future<Long> future = executor.submit(callable);
		long callTime = future.get();
		assertTrue(startTime <= callTime);
	}

	@Test
	public void testScheduleRunnableNoRepeat() throws Exception {
		Runnable runnable = () -> {
			callTimes.add(System.nanoTime());
		};
		long startTime = System.nanoTime();
		long delayTime = MILLISECONDS.toNanos(50);
		Future<?> future = executor.schedule(runnable, delayTime, NANOSECONDS);
		future.get();
		assertEquals(1, callTimes.size());
		assertTrue(startTime + delayTime <= callTimes.get(0));
	}

	@Test
	public void testScheduleCallableNoRepeat() throws Exception {
		Callable<Long> callable = () -> {
			return System.nanoTime();
		};
		long startTime = System.nanoTime();
		long delayTime = MILLISECONDS.toNanos(50);
		Future<Long> future = executor.schedule(callable, delayTime, NANOSECONDS);
		long callTime = future.get();
		assertTrue(startTime + delayTime <= callTime);
	}

	@Test
	public void testScheduleZeroCores() throws Exception {
		executor = new RetryingScheduledExecutor(0);
		Runnable runnable = () -> {
			callTimes.add(System.nanoTime());
		};
		long startTime = System.nanoTime();
		Future<?> future = executor.schedule(runnable, 0, NANOSECONDS);
		future.get();
		assertEquals(1, callTimes.size());
		assertTrue(startTime <= callTimes.get(0));
	}

	@Test
	public void testScheduledRepeatFixedRate() throws Exception {
		Runnable runnable = () -> {
			callTimes.add(System.nanoTime());
		};
		long startTime = System.nanoTime();
		long period = MILLISECONDS.toNanos(15);
		executor.scheduleAtFixedRate(runnable, 0, period, NANOSECONDS);
		while (callTimes.size() < 8) {
			Thread.sleep(7);
		}
		executor.shutdown();
		executor.awaitTermination(10, SECONDS);
		long minimumStartTime = startTime;
		for (long time : callTimes) {
			assertTrue(minimumStartTime <= time);
			minimumStartTime += period;
		}
	}

	@Test
	public void testScheduleRepeatFixedDelay() throws Exception {
		final long runTimeMs = 30;
		final long runTime = MILLISECONDS.toNanos(runTimeMs);
		Runnable runnable = () -> {
			callTimes.add(System.nanoTime());
			try {
				Thread.sleep(runTimeMs);
			} catch (InterruptedException e) {
			}
		};
		long delay = MILLISECONDS.toNanos(15);
		long startTime = System.nanoTime();
		executor.scheduleWithFixedDelay(runnable, 0, delay, NANOSECONDS);
		while (callTimes.size() < 8) {
			Thread.sleep(15);
		}
		executor.shutdown();
		executor.awaitTermination(10, SECONDS);
		long minimumStartTime = startTime;
		for (long time : callTimes) {
			assertTrue(minimumStartTime <= time);
			minimumStartTime += delay + runTime;
		}
	}

	@Test
	public void testScheduleRepeatAbort() throws Exception {
		final int runs = 3;
		Runnable runnable = () -> {
			if (callTimes.size() < runs) {
				callTimes.add(System.nanoTime());
			} else {
				throw new AbortScheduledException();
			}
		};
		Future<?> future = executor.scheduleAtFixedRate(runnable, 0, 1, MILLISECONDS);
		try {
			future.get();
			fail("Expected ExecutionException");
		} catch (ExecutionException e) {
		}
		assertEquals(runs, callTimes.size());
	}

	@Test
	public void testScheduleBackOff() throws Exception {
		long period = MILLISECONDS.toNanos(10);
		long maxDelay = MILLISECONDS.toNanos(60);
		final int scaleFactor = 2;

		final int seqErrors = 4;
		Runnable runnable = () -> {
			callTimes.add(System.nanoTime());
			if (callTimes.size() % seqErrors != 0) {
				throw new RuntimeException();
			}
		};
		long startTime = System.nanoTime();
		executor.scheduleAtFixedRate(runnable, 0, period, 0, maxDelay, NANOSECONDS, scaleFactor);

		while (callTimes.size() < 9) {
			Thread.sleep(15);
		}
		executor.shutdownNow();
		executor.awaitTermination(10, SECONDS);

		int count = 0;
		long minimumCallTime = startTime;
		long currentDelay = period;
		for (long time : callTimes) {
			count++;
			assertTrue(minimumCallTime <= time);
			currentDelay = count % seqErrors == 0 ? period : currentDelay * scaleFactor;
			currentDelay = currentDelay < maxDelay ? currentDelay : maxDelay;
			minimumCallTime += currentDelay;
		}
	}

	@Test
	public void testMultipleJobs() throws Exception {
		executor = new RetryingScheduledExecutor(4);
		final CyclicBarrier barrier = new CyclicBarrier(4);

		class CyclicRunnable implements Runnable {

			private final int maximumRuns;
			private final CyclicBarrier barrier;
			private int currentRuns = 0;

			public CyclicRunnable(int maximumRuns, CyclicBarrier barrier) {
				this.maximumRuns = maximumRuns;
				this.barrier = barrier;
			}

			@Override
			public void run() {
				if (currentRuns == 0) {
					try {
						barrier.await();
					} catch (Exception e) {
						throw new AbortScheduledException(e);
					}
				}
				currentRuns++;
				synchronized (callTimes) {
					callTimes.add(System.nanoTime());
				}
				if (currentRuns == maximumRuns) {
					throw new AbortScheduledException();
				}
			}
		}

		CyclicRunnable r1 = new CyclicRunnable(8, barrier);
		CyclicRunnable r2 = new CyclicRunnable(8, barrier);
		CyclicRunnable r3 = new CyclicRunnable(8, barrier);
		CyclicRunnable r4 = new CyclicRunnable(8, barrier);
		Future<?> f1 = executor.scheduleAtFixedRate(r1, 0, 10, MILLISECONDS);
		Future<?> f2 = executor.scheduleAtFixedRate(r2, 0, 10, MILLISECONDS);
		Future<?> f3 = executor.scheduleAtFixedRate(r3, 0, 10, MILLISECONDS);
		Future<?> f4 = executor.scheduleAtFixedRate(r4, 0, 10, MILLISECONDS);
		try { f1.get(); } catch (ExecutionException e) {}
		try { f2.get(); } catch (ExecutionException e) {}
		try { f3.get(); } catch (ExecutionException e) {}
		try { f4.get(); } catch (ExecutionException e) {}
		assertEquals(8 * 4, callTimes.size());
	}
}
