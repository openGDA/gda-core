/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package gda.device.scannable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.function.Function;

import javax.measure.Quantity;

import org.junit.Before;
import org.junit.Test;

import gda.device.DeviceException;

public class SequentialScannableTest {
	// Allow for inaccuracy in floating point values
	private static final double FP_TOLERANCE = 1e-9;

	private DummyScannableForTest scannable1;
	private DummyScannableForTest scannable2;
	private DummyScannableForTest scannable3;

	private final MockFunction function1 = new MockFunction(1.0);
	private final MockFunction function2 = new MockFunction(2.0);
	private final MockFunction function3 = new MockFunction(3.0);

	private SequentialScannable sequentialScannable;

	private LocalDateTime testStartTime;

	@Before
	public void setUp() throws Exception {
		testStartTime = LocalDateTime.now();

		scannable1 = new DummyScannableForTest("scannable1", 0.0);
		scannable2 = new DummyScannableForTest("scannable2", 0.0);
		scannable3 = new DummyScannableForTest("scannable3", 0.0);

		sequentialScannable = new SequentialScannable();
		sequentialScannable.setScannables(Arrays.asList(scannable1, scannable2, scannable3));
		sequentialScannable.setFunctions(Arrays.asList(function1, function2, function3));
		sequentialScannable.configure();
	}

	@Test
	public void testInitialState() throws Exception {
		assertEquals(SequentialScannable.Order.ALWAYS_FORWARDS, sequentialScannable.getOrder());
		assertEquals(0.0, (double) sequentialScannable.getPosition(), FP_TOLERANCE);
		assertNull(scannable1.getLastMoved());
		assertNull(scannable2.getLastMoved());
	}

	@Test
	public void testOrderAlwaysForward() throws Exception {
		// Primary scannable moved forwards
		sequentialScannable.moveTo(4.0);
		assertEquals(4.0, (double) sequentialScannable.getPosition(), FP_TOLERANCE);
		assertEquals(4.0, (double) scannable1.getPosition(), FP_TOLERANCE);
		assertEquals(8.0, (double) scannable2.getPosition(), FP_TOLERANCE);
		assertEquals(12.0, (double) scannable3.getPosition(), FP_TOLERANCE);
		checkOrderOfMove(scannable1, scannable2, scannable3);

		// Primary scannable moved backwards
		sequentialScannable.moveTo(2.0);
		assertEquals(2.0, (double) sequentialScannable.getPosition(), FP_TOLERANCE);
		assertEquals(2.0, (double) scannable1.getPosition(), FP_TOLERANCE);
		assertEquals(4.0, (double) scannable2.getPosition(), FP_TOLERANCE);
		assertEquals(6.0, (double) scannable3.getPosition(), FP_TOLERANCE);
		checkOrderOfMove(scannable1, scannable2, scannable3);
	}

	@Test
	public void testOrderWithMovement() throws Exception {
		sequentialScannable.setOrder(SequentialScannable.Order.WITH_MOVEMENT);
		// Primary scannable moved forwards
		sequentialScannable.moveTo(4.0);
		assertEquals(4.0, (double) sequentialScannable.getPosition(), FP_TOLERANCE);
		assertEquals(4.0, (double) scannable1.getPosition(), FP_TOLERANCE);
		assertEquals(8.0, (double) scannable2.getPosition(), FP_TOLERANCE);
		assertEquals(12.0, (double) scannable3.getPosition(), FP_TOLERANCE);
		checkOrderOfMove(scannable1, scannable2, scannable3);

		// Primary scannable moved backwards
		sequentialScannable.moveTo(2.0);
		assertEquals(2.0, (double) sequentialScannable.getPosition(), FP_TOLERANCE);
		assertEquals(2.0, (double) scannable1.getPosition(), FP_TOLERANCE);
		assertEquals(4.0, (double) scannable2.getPosition(), FP_TOLERANCE);
		assertEquals(6.0, (double) scannable3.getPosition(), FP_TOLERANCE);
		checkOrderOfMove(scannable3, scannable2, scannable1);
	}

	@Test
	public void testOrderContraryToMovement() throws Exception {
		sequentialScannable.setOrder(SequentialScannable.Order.CONTRARY_TO_MOVEMENT);
		// Primary scannable moved forwards
		sequentialScannable.moveTo(4.0);
		assertEquals(4.0, (double) sequentialScannable.getPosition(), FP_TOLERANCE);
		assertEquals(4.0, (double) scannable1.getPosition(), FP_TOLERANCE);
		assertEquals(8.0, (double) scannable2.getPosition(), FP_TOLERANCE);
		assertEquals(12.0, (double) scannable3.getPosition(), FP_TOLERANCE);
		checkOrderOfMove(scannable3, scannable2, scannable1);

		// Primary scannable moved backwards
		sequentialScannable.moveTo(2.0);
		assertEquals(2.0, (double) sequentialScannable.getPosition(), FP_TOLERANCE);
		assertEquals(2.0, (double) scannable1.getPosition(), FP_TOLERANCE);
		assertEquals(4.0, (double) scannable2.getPosition(), FP_TOLERANCE);
		assertEquals(6.0, (double) scannable3.getPosition(), FP_TOLERANCE);
		checkOrderOfMove(scannable1, scannable2, scannable3);
	}

	/**
	 * Check that scannables have been moved in the expected specified
	 *
	 * @param scannables
	 *            the scannables to check, in the order in which they are expected to have moved
	 */
	private void checkOrderOfMove(DummyScannableForTest... scannables) {
		LocalDateTime previousTime = testStartTime;
		for (DummyScannableForTest scannable : scannables) {
			final LocalDateTime lastMoved = scannable.getLastMoved();
			assertTrue(lastMoved.isAfter(previousTime));
			previousTime = lastMoved;
		}
	}

	/**
	 * Mockito mocks do not work well with generics (at least with the Eclipse Mars compiler currently we are using with
	 * Buckminster), so create a class to do something similar.
	 * <p>
	 * The function simply multiplies the input number by the given factor.
	 */
	private class MockFunction implements Function<Quantity<? extends Quantity<?>>, Quantity<? extends Quantity<?>>> {
		private final double multiplier;

		public MockFunction(double multiplier) {
			this.multiplier = multiplier;
		}

		@Override
		public Quantity<? extends Quantity<?>> apply(Quantity<? extends Quantity<?>> t) {
			return t.multiply(multiplier);
		}

		@Override
		public String toString() {
			return "MockFunction [multiplier=" + multiplier + "]";
		}
	}

	/**
	 * Extension to {@link DummyScannable} that records the time of the last move and notifies the correct event
	 */
	private class DummyScannableForTest extends DummyScannable {
		private LocalDateTime lastMoved;

		public DummyScannableForTest(String name, double initialPosition) {
			super(name, initialPosition);
		}

		@Override
		public void rawAsynchronousMoveTo(Object position) throws DeviceException {
			try {
				// Ensure there is a measurable time difference between moving the scannables
				Thread.sleep(1);
			} catch (InterruptedException e) {
				throw new DeviceException("Error in sleep()", e);
			}
			lastMoved = LocalDateTime.now();
			super.rawAsynchronousMoveTo(position);
			notifyIObservers(this, ScannableStatus.IDLE);
		}

		public LocalDateTime getLastMoved() {
			return lastMoved;
		}

		@Override
		public String toString() {
			return "DummyScannableForTest [lastMoved=" + lastMoved + ", " + super.toString() + "]";
		}
	}
}
