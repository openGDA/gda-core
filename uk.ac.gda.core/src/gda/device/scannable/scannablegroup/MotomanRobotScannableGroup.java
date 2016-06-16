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

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.device.DeviceException;
import gda.device.Scannable;

/**
 * This scannable group represents a Motoman robot. It has two particular characteristics:
 *
 * (1) The two robot arm axes (KTheta & KPhi) cannot reliably be moved at the same time.<br>
 * (2) When the KTheta angle is very small (kThetaProtectionValue), KPhi must not exceed a certain value
 * (kPhiProtectionValue) in order to avoid possible damage to the robot.
 *
 * To allow this class to satisfy these conditions KTheta & KPhi (in that order) MUST be the first two devices in the
 * group.
 */
public class MotomanRobotScannableGroup extends ScannableGroup {

	private static final Logger logger = LoggerFactory.getLogger(MotomanRobotScannableGroup.class);

	private static final int NUM_MOTORS = 5;

	private double kThetaProtectionValue = 3.0;
	private double kPhiProtectionValue = 10.0;

	private class Move {
		private final Scannable device;
		private final Double targetPosition;

		public Move(Scannable device, Double targetPosition) {
			this.device = device;
			this.targetPosition = targetPosition;
		}
	}

	private boolean performingSequentialMoves = false;

	/**
	 * Run the moves in sequentialMoves one at a time.
	 */
	private class MoveRunner implements Runnable {

		private final List<Move> moves;

		public MoveRunner(final List<Move> moves) {
			this.moves = moves;
		}

		@Override
		public void run() {
			performingSequentialMoves = true;
			try {
				for (final Move move : moves) {
					move.device.moveTo(move.targetPosition);
				}
			} catch (DeviceException e) {
				logger.error("Exception moving MotomanRobotScannableGroup", e);
				try {
					stop();
				} catch (DeviceException e1) {
					logger.error("Exception stoping MotomanRobotScannableGroup", e1);
				}

			} finally {
				moves.clear();
				performingSequentialMoves = false;
			}
		}
	}

	@Override
	public void asynchronousMoveTo(Object position) throws DeviceException {
		if (groupMembers.size() != NUM_MOTORS) {
			throw new DeviceException("MotomanRobotScannableGroup requires " + NUM_MOTORS + " motors");
		}

		final Vector<Object[]> positions = extractPositionsFromObject(position);

		// The target positions must consist of NUM_MOTORS floating point numbers
		if (positions.size() != NUM_MOTORS) {
			throw new DeviceException("MotomanRobotScannableGroup requires " + NUM_MOTORS + " target positions");
		}

		// Target positions must be floating point numbers
		final List<Double> targetPositions = new ArrayList<>(NUM_MOTORS);
		for (int i = 0; i < NUM_MOTORS; i++) {
			targetPositions.add(Double.parseDouble(positions.get(i)[0].toString()));
		}

		// Check validity of KTheta/KPhi target positions
		final double kThetaTarget = targetPositions.get(0);
		final double kPhiTarget = targetPositions.get(1);
		if (kThetaTarget < kThetaProtectionValue && kPhiTarget > kPhiProtectionValue) {
			logger.error(String.format("Invalid combination of values KTheta %.3f, KPhi %.3f", kThetaTarget, kPhiTarget));
			logger.error(String.format("If KTheta is less than %.3f, KPhi must not exceed %.3f", kThetaProtectionValue, kPhiProtectionValue));
			throw new DeviceException("Invalid combination of KTheta and KPhi values");
		}

		// Queue up the moves that must be performed sequentially
		final List<Move> sequentialMoves = new ArrayList<>();

		for (int i = 0; i < 2; i++) {
			final Scannable device = groupMembers.get(i);
			sequentialMoves.add(new Move(device, targetPositions.get(i)));
		}

		// Rotation stage moves can be made directly
		for (int i = 2; i < NUM_MOTORS; i++) {
			final Scannable device = groupMembers.get(i);
			device.asynchronousMoveTo(targetPositions.get(i));
		}

		// Start the sequential moves
		performingSequentialMoves = true;
		(new Thread(new MoveRunner(sequentialMoves))).start();
	}

	@Override
	public boolean isBusy() throws DeviceException {
		if (performingSequentialMoves) {
			return true;
		}
		// Not performing sequential moves, check whether other devices are moving
		return super.isBusy();
	}

	private static final long pollTimeMillis = LocalProperties.getAsInt(LocalProperties.GDA_SCANNABLEBASE_POLLTIME, 100);

	@Override
	public void waitWhileBusy() throws DeviceException, InterruptedException {
		while (isBusy()) {
			Thread.sleep(pollTimeMillis);
		}
	}

	@Override
	public void atCommandFailure() throws DeviceException {
		stop();
		super.atCommandFailure();
	}

	public double getkThetaProtectionValue() {
		return kThetaProtectionValue;
	}

	public void setkThetaProtectionValue(double kThetaProtectionValue) {
		this.kThetaProtectionValue = kThetaProtectionValue;
	}

	public double getkPhiProtectionValue() {
		return kPhiProtectionValue;
	}

	public void setkPhiProtectionValue(double kPhiProtectionValue) {
		this.kPhiProtectionValue = kPhiProtectionValue;
	}
}
