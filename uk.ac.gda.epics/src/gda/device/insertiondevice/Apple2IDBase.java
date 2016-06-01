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

import java.util.Deque;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceBase;
import gda.device.DeviceException;
import gda.device.MotorProperties.MotorEvent;

/**
 * This is the base class of a low-level controller for the AppleII ID.
 * <p>
 * It allows you to specify the ID gap and the position of the four phase motors and ensures that moves are made safely. Scannables can be built on top of this
 * to allow the user to specify position in terms of, for example, gap & polarisation or energy & polarisation.
 * <p>
 * This version expects as input an array of elements (all expressed in mm):<br>
 * [&lt;gap&gt;, &lt;top outer motor position&gt;, &lt;top inner motor position&gt;, &lt;bottom outer motor position&gt;, &lt;bottom inner motor position&gt;]
 * <p>
 * The ID handles four polarisation modes: <br>
 * LH (horizontal)<br>
 * C (circular)<br>
 * L1 (linear 1)<br>
 * L2 (linear 2)
 * <p>
 * LH is in fact a special case of C with all motor positions equal to 0.
 * <p>
 * Moves between different modes must be made via LH: this class takes care of this requirement.
 */
public abstract class Apple2IDBase extends DeviceBase implements IApple2ID {

	private static final Logger logger = LoggerFactory.getLogger(Apple2IDBase.class);

	protected static final String GAP_AND_PHASE_MODE = "GAP AND PHASE";

	// Polarisation mode: see above
	private enum PolarisationMode {
		LH, C, L1, L2, UNKNOWN
	}

	protected enum IDMotor {
		GAP, TOP_OUTER, TOP_INNER, BOTTOM_OUTER, BOTTOM_INNER
	}

	private double maxPhaseMotorPos = 28.0;
	private double minGapPos = 20.0;

	// Tolerance on position of individual motors
	private double motorPositionTolerance = 0.012;

	private Deque<Apple2IDPosition> pendingMoves;

	private volatile boolean busy = false;

	// --------------------------------------------------------------------------------------------------------------------------

	public Apple2IDBase() {
		pendingMoves = new LinkedList<>();
	}

	// Interface IApple2ID

	@Override
	public void asynchronousMoveTo(Apple2IDPosition position) throws DeviceException {

		// Check state of ID
		if (!isEnabled()) {
			throw new DeviceException("ID is disabled and cannot be moved");
		}

		final String gpMode = getIDMode();
		if (!gpMode.equals(GAP_AND_PHASE_MODE)) {
			throw new DeviceException(String.format("ID mode is %s but should be %s", gpMode, GAP_AND_PHASE_MODE));
		}

		// Check that requested motor positions constitute a valid polarisation mode
		final PolarisationMode requestedMode = getPolarisationMode(position);
		if (requestedMode == PolarisationMode.UNKNOWN) {
			throw new DeviceException("Illegal combination of phase motor positions requested");
		}

		// Check that requested ID gap is valid
		if (position.gap < minGapPos) {
			throw new DeviceException(String.format("ID gap must be at least %.3fmm", minGapPos));
		}

		// Check that requested motor positions are valid
		if (Math.abs(position.topOuterPos) > maxPhaseMotorPos || Math.abs(position.topInnerPos) > maxPhaseMotorPos
				|| Math.abs(position.bottomOuterPos) > maxPhaseMotorPos || Math.abs(position.bottomInnerPos) > maxPhaseMotorPos) {
			throw new DeviceException(String.format("motor position cannot exceed (+/-)%.3fmm", maxPhaseMotorPos));
		}

		// Get current polarisation mode
		final Apple2IDPosition currentPosition = new Apple2IDPosition(getMotorPosition(IDMotor.GAP), getMotorPosition(IDMotor.TOP_OUTER), getMotorPosition(IDMotor.TOP_INNER),
				getMotorPosition(IDMotor.BOTTOM_OUTER), getMotorPosition(IDMotor.BOTTOM_INNER));
		final PolarisationMode currentMode = getPolarisationMode(currentPosition);

		// Set up move(s)
		// If we are moving between different modes, we need to move via horizontal
		pendingMoves.clear();
		if (requestedMode != currentMode && currentMode != PolarisationMode.LH) {
			pendingMoves.add(new Apple2IDPosition(position.gap, 0, 0, 0, 0));
		}

		// Now add a move to the requested mode, unless it is LH, in which case we are already there or have just added a move there.
		if (requestedMode != PolarisationMode.LH) {
			pendingMoves.add(position.clone());
		}

		// Start the first move
		if (!pendingMoves.isEmpty()) {
			startNextMove();
		}
	}

	@Override
	public boolean isBusy() {
		return busy;
	}

	@Override
	public Apple2IDPosition getPosition() throws DeviceException {
		return new Apple2IDPosition(getMotorPosition(IDMotor.GAP), getMotorPosition(IDMotor.TOP_OUTER), getMotorPosition(IDMotor.TOP_INNER),
				getMotorPosition(IDMotor.BOTTOM_OUTER), getMotorPosition(IDMotor.BOTTOM_INNER));
	}

	@Override
	public boolean motorPositionsEqual(final double a, final double b) {
		return Math.abs(a - b) < motorPositionTolerance;
	}

	/**
	 * Subclasses should call this function when a single move has finished.
	 */
	protected synchronized void onMoveFinished() {
		if (pendingMoves.isEmpty()) {
			busy = false;
			notifyIObservers(this, MotorEvent.MOVE_COMPLETE);
		} else {
			startNextMove();
		}
	}

	private void startNextMove() {
		final Apple2IDPosition move = pendingMoves.removeFirst();
		try {
			logger.info("Moving ID to " + move.toString());
			busy = true;
			doMove(move);
		} catch (DeviceException e) {
			logger.error("Failed to move ID to " + move.toString(), e);
			busy = false;
		}
	}

	private PolarisationMode getPolarisationMode(Apple2IDPosition position) {

		if (motorPositionsEqual(position.topOuterPos, 0) && motorPositionsEqual(position.topInnerPos, 0) && motorPositionsEqual(position.bottomOuterPos, 0)
				&& motorPositionsEqual(position.bottomInnerPos, 0)) {
			return PolarisationMode.LH;
		}
		if (motorPositionsEqual(position.topOuterPos, position.bottomInnerPos) && motorPositionsEqual(position.topInnerPos, 0) && motorPositionsEqual(position.bottomOuterPos, 0)) {
			return PolarisationMode.C;
		}
		if (motorPositionsEqual(position.topOuterPos, -position.bottomInnerPos) && motorPositionsEqual(position.topInnerPos, 0) && motorPositionsEqual(position.bottomOuterPos, 0)) {
			return PolarisationMode.L1;
		}
		if (motorPositionsEqual(position.topInnerPos, -position.bottomOuterPos) && motorPositionsEqual(position.topOuterPos, 0) && motorPositionsEqual(position.bottomInnerPos, 0)) {
			return PolarisationMode.L2;
		}
		return PolarisationMode.UNKNOWN;
	}

	// (Abstract) class methods
	protected abstract void doMove(Apple2IDPosition position) throws DeviceException;

	protected abstract double getMotorPosition(IDMotor motor) throws DeviceException;

	// Configuration

	@Override
	public double getMaxPhaseMotorPos() {
		return maxPhaseMotorPos;
	}

	public void setMaxPhaseMotorPos(double maxPhaseMotorPos) {
		this.maxPhaseMotorPos = maxPhaseMotorPos;
	}

	public double getMinGapPos() {
		return minGapPos;
	}

	public void setMinGapPos(double minGapPos) {
		this.minGapPos = minGapPos;
	}

	public double getMotorPositionTolerance() {
		return motorPositionTolerance;
	}

	public void setMotorPositionTolerance(double motorPositionTolerance) {
		this.motorPositionTolerance = motorPositionTolerance;
	}
}
