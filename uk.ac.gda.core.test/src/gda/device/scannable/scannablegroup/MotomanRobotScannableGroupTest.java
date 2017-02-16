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

package gda.device.scannable.scannablegroup;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import gda.device.DeviceException;
import gda.device.Scannable;

public class MotomanRobotScannableGroupTest {

	private MotomanRobotScannableGroup group;
	private Scannable kTheta;
	private Scannable kPhi;
	private Scannable sTheta;
	private Scannable sKappa;
	private Scannable sPhi;

	@Before
	public void setUp() {
		kTheta = createMockScannable("kTheta");
		kPhi = createMockScannable("kPhi");
		sTheta = createMockScannable("sTheta");
		sKappa = createMockScannable("sKappa");
		sPhi = createMockScannable("sPhi");

		group = new MotomanRobotScannableGroup();
		group.setGroupMembers(Arrays.asList(kTheta, kPhi, sTheta, sKappa, sPhi));
	}

	private Scannable createMockScannable(final String name) {
		final Scannable scannable = mock(Scannable.class);
		when(scannable.getInputNames()).thenReturn(new String[] { name });
		return scannable;
	}

	@Test(expected = DeviceException.class)
	public void testNotEnoughMotors() throws DeviceException {
		final MotomanRobotScannableGroup smallGroup = new MotomanRobotScannableGroup();
		smallGroup.setGroupMembers(Arrays.asList(kTheta, kPhi, sTheta, sKappa));
		smallGroup.asynchronousMoveTo(new Double[] { 1.0, 2.0, 3.0, 4.0, 5.0 });
	}

	@Test(expected = DeviceException.class)
	public void testNotEnoughPositions() throws DeviceException {
		group.asynchronousMoveTo(new Double[] { 1.0, 2.0, 3.0, 4.0 });
	}

	@Test(expected = DeviceException.class)
	public void testKThetaBelowLimitKPhiAboveLimit() throws DeviceException {
		group.asynchronousMoveTo(new Double[] { 2.9, 10.1, 3.0, 4.0, 5.0 });
	}

	@Test
	public void testKThetaBelowLimitKPhiBelowLimit() throws DeviceException, InterruptedException {
		final Double[] moves = new Double[] { 2.9, 9.9, 3.0, 4.0, 5.0 };
		group.asynchronousMoveTo(moves);
		group.waitWhileBusy();
		checkMoves(moves, 1);
	}

	@Test
	public void testKThetaAboveLimitKPhiAboveLimit() throws DeviceException, InterruptedException {
		final Double[] moves = new Double[] { 3.1, 10.1, 3.0, 4.0, 5.0 };
		group.asynchronousMoveTo(moves);
		group.waitWhileBusy();
		checkMoves(moves, 1);
	}

	@Test
	public void testMoveTwiceToSamePosition() throws DeviceException, InterruptedException {
		final Double[] moves = new Double[] { 3.1, 10.1, 3.0, 4.0, 5.0 };

		group.asynchronousMoveTo(moves);
		group.waitWhileBusy();
		group.asynchronousMoveTo(moves);
		group.waitWhileBusy();

		checkMoves(moves, 2);
	}

	@Test
	public void testMoveToDifferentPositions() throws DeviceException, InterruptedException {
		final Double[] moves1 = new Double[] { 3.1, 10.1, 3.0, 4.0, 5.0 };
		final Double[] moves2 = new Double[] { 2.9, 9.9, 3.5, 4.5, 5.5 };

		group.asynchronousMoveTo(moves1);
		group.waitWhileBusy();
		group.asynchronousMoveTo(moves2);
		group.waitWhileBusy();

		checkMoves(moves1, 1);
		checkMoves(moves2, 1);
	}

	@Test
	public void testStopOnDeviceError() throws DeviceException, InterruptedException {
		final Double[] moves = new Double[] { 3.1, 10.1, 3.0, 4.0, 5.0 };
		doThrow(DeviceException.class).when(kPhi).moveTo(any());

		group.asynchronousMoveTo(moves);
		group.waitWhileBusy();
		verify(sTheta).stop();
		verify(sPhi).stop();
		verify(sTheta).stop();
		verify(sKappa).stop();
		verify(sPhi).stop();
	}

	private void checkMoves(final Double[] expectedMoves, final int times) throws DeviceException {
		// Note that kTheta & kPhi are moved synchronously
		verify(kTheta, times(times)).moveTo(expectedMoves[0]);
		verify(kPhi, times(times)).moveTo(expectedMoves[1]);

		verify(sTheta, times(times)).asynchronousMoveTo(expectedMoves[2]);
		verify(sKappa, times(times)).asynchronousMoveTo(expectedMoves[3]);
		verify(sPhi, times(times)).asynchronousMoveTo(expectedMoves[4]);
	}
}
