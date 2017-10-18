/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package gda.device.insertiondevice;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import gda.device.DeviceException;
import gda.factory.FactoryException;

public class Apple2IDBaseTest {

	private static final String MODE_GAP = "GAP";
	private static final String MODE_GAP_AND_PHASE = "GAP AND PHASE";

	private class TestApple2IDBase extends Apple2IDBase {

		private String idMode;
		private boolean enabled;
		private List<Apple2IDPosition> moves = new ArrayList<>();
		private Apple2IDPosition currentPosition;

		public TestApple2IDBase(String idMode, boolean enabled) {
			super();
			this.idMode = idMode;
			this.enabled = enabled;
		}

		@Override
		public String getIDMode() throws DeviceException {
			return idMode;
		}

		@Override
		public boolean isEnabled() throws DeviceException {
			return enabled;
		}

		@Override
		public void configure() throws FactoryException {
		}

		@Override
		protected void doMove(Apple2IDPosition position) throws DeviceException {
			moves.add(position.clone());
			onMoveFinished();
		}

		@Override
		protected double getMotorPosition(IDMotor motor) throws DeviceException {
			if (currentPosition == null) {
				return 0;
			}
			switch (motor) {
			case GAP:
				return currentPosition.gap;
			case TOP_OUTER:
				return currentPosition.topOuterPos;
			case TOP_INNER:
				return currentPosition.topInnerPos;
			case BOTTOM_OUTER:
				return currentPosition.bottomOuterPos;
			case BOTTOM_INNER:
				return currentPosition.bottomInnerPos;
			}
			return 0;
		}

		public List<Apple2IDPosition> getMoves() {
			return moves;
		}

		public void setCurrentPosition(final Apple2IDPosition position) {
			currentPosition = position;
		}
	}

	private TestApple2IDBase controller;

	@Test(expected = DeviceException.class)
	public void testInvalidMode() throws DeviceException {
		controller = new TestApple2IDBase(MODE_GAP, true);
		controller.asynchronousMoveTo(new Apple2IDPosition(25, 0, 0, 0, 0));
	}

	@Test(expected = DeviceException.class)
	public void testInvalidState() throws DeviceException {
		controller = new TestApple2IDBase(MODE_GAP_AND_PHASE, false);
		controller.asynchronousMoveTo(new Apple2IDPosition(25, 0, 0, 0, 0));
	}

	@Test(expected = DeviceException.class)
	public void testInvalidPolarisation() throws DeviceException {
		controller = new TestApple2IDBase(MODE_GAP_AND_PHASE, true);
		controller.asynchronousMoveTo(new Apple2IDPosition(25, 0, 10, 0, 0));
	}

	@Test(expected = DeviceException.class)
	public void testInvalidGap() throws DeviceException {
		controller = new TestApple2IDBase(MODE_GAP_AND_PHASE, true);
		controller.asynchronousMoveTo(new Apple2IDPosition(15, 0, 0, 0, 0));
	}

	@Test(expected = DeviceException.class)
	public void testInvalidMotorPositions() throws DeviceException {
		controller = new TestApple2IDBase(MODE_GAP_AND_PHASE, true);
		controller.asynchronousMoveTo(new Apple2IDPosition(25, 40, 0, 0, 40));
	}

	@Test
	public void testMoveFromHorizontalToCircular() throws DeviceException, InterruptedException {
		controller = new TestApple2IDBase(MODE_GAP_AND_PHASE, true);
		controller.setCurrentPosition(new Apple2IDPosition(30, 0, 0, 0, 0));
		controller.asynchronousMoveTo(new Apple2IDPosition(25, 10, 0, 0, 10));
		while (controller.isBusy()) {
			Thread.sleep(10);
		}
		final List<Apple2IDPosition> moves = controller.getMoves();
		assertEquals(1, moves.size());
		assertEquals(new Apple2IDPosition(25, 10, 0, 0, 10), moves.get(0));
	}

	@Test
	public void testMoveFromLinear1ToHorizontal() throws DeviceException, InterruptedException {
		controller = new TestApple2IDBase(MODE_GAP_AND_PHASE, true);
		controller.setCurrentPosition(new Apple2IDPosition(30, 10, 0, 0, -10));
		controller.asynchronousMoveTo(new Apple2IDPosition(30, 0, 0, 0, 0));
		while (controller.isBusy()) {
			Thread.sleep(10);
		}
		final List<Apple2IDPosition> moves = controller.getMoves();
		assertEquals(1, moves.size());
		assertEquals(new Apple2IDPosition(30, 0, 0, 0, 0), moves.get(0));
	}

	@Test
	public void testMoveFromLinear1ToHorizontalWithGapChange() throws DeviceException, InterruptedException {
		controller = new TestApple2IDBase(MODE_GAP_AND_PHASE, true);
		controller.setCurrentPosition(new Apple2IDPosition(30, 10, 0, 0, -10));
		controller.asynchronousMoveTo(new Apple2IDPosition(25, 0, 0, 0, 0));
		while (controller.isBusy()) {
			Thread.sleep(10);
		}
		final List<Apple2IDPosition> moves = controller.getMoves();
		assertEquals(2, moves.size()); //1st move ploarisation to LH, 2nd move the gap.
		assertEquals(new Apple2IDPosition(25, 0, 0, 0, 0), moves.get(0));
	}

	@Test
	public void testMoveFromLinear2ToCircular() throws DeviceException, InterruptedException {
		controller = new TestApple2IDBase(MODE_GAP_AND_PHASE, true);
		controller.setCurrentPosition(new Apple2IDPosition(30, 0, 10, -10, 0));
		controller.asynchronousMoveTo(new Apple2IDPosition(25, 20, 0, 0, 20));
		while (controller.isBusy()) {
			Thread.sleep(10);
		}

		// Should move via horizontal
		final List<Apple2IDPosition> moves = controller.getMoves();
		assertEquals(2, moves.size());
		assertEquals(new Apple2IDPosition(25, 0, 0, 0, 0), moves.get(0));
		assertEquals(new Apple2IDPosition(25, 20, 0, 0, 20), moves.get(1));
	}
}
