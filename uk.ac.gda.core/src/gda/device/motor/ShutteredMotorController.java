/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package gda.device.motor;

import gda.device.Device;
import gda.device.DeviceException;
import gda.device.scannable.ScannableMotor;

/**
 * Interface to a shuttered motor controller that will calculate the start and end positions
 * of a move based on the desired time-to-velocity, desired velocity, and fudge factors added
 * to make sure that the motors are up to speed. Assumes that the shutter will be triggered open slightly before
 * openPosition and triggered to close slightly before closePosition.
 * 
 *   /---------------\
 *  /                 \
 * /                   \
 * 1   2   3 - 4   5   6
 * (Figure indicates velocity of the axis and keys to the points are given below.)
 * 
 * Point 1: actual start position (open position - start position offset)
 * Point 2: time after motor has reached desired speed, before it reaches the desired start position
 * Points 3 and 4: at velocity, the start point (shutterIsOpenPosition) and end point of the desired
 * exposure range. Right before points 3 and 4, the shutter will be triggered to open
 * (shutterOpenIsTriggeredPosition) or close, respectively. gda/px/camera/Trigger will handle
 * the triggering.
 * Point 5: exposure has finished (beyond close position), but before deceleration has started
 * Point 6: decelerating to final stop position (close position + end position offset)
 */
public interface ShutteredMotorController extends Device {
	
	public void setMotor(ScannableMotor theMotor);
	/**
	 * Move the motor to the actual start position required for proper run-up to the desired velocity.
	 * Set speed and time to velocity on underlying motor to accomplish the move properly.
	 * Done after setDefaultSpeeds() to ensure that axes are moving fast to the start position.
	 * @throws DeviceException 
	 */
	public void prepareForExposure() throws DeviceException;
	
	/**
	 * Perform the calculations necessary to get the actual start position of the move, and the final
	 * end position.
	 */
	public void doCalculations();
	
	/**
	 * Perform the actual move, once the exposure has been prepared with prepareForExposure()
	 * @throws DeviceException 
	 */
	public void expose() throws DeviceException;

	/**
	 * Reset to the default speed and default time to velocity, usually done after an exposure()
	 * @throws DeviceException 
	 */
	public void setDefaultSpeeds() throws DeviceException;
	/**
	 * @param desiredSpeed
	 */
	public void setDesiredSpeed(double desiredSpeed);
	
	/**
	 * @return speed
	 */
	public double getDesiredSpeed();
	
	/**
	 * @param defaultSpeed
	 */
	public void setDefaultSpeed(double defaultSpeed);
	
	public double getDefaultSpeed();
	
	/**
	 * @param desiredTimeToVelocity
	 */
	public void setDesiredTimeToVelocity(double desiredTimeToVelocity);
	
	/**
	 * @return desiredTimeToVelocity
	 */
	public double getDesiredTimeToVelocity();
	
	/**
	 * @param desiredTimeToVelocity
	 */
	public void setDefaultTimeToVelocity(double desiredTimeToVelocity);
	
	/**
	 * @return defaultTimeToVelocity
	 */
	public double getDefaultTimeToVelocity();
	
	/**
	 * The minimum time to velocity.
	 * @param minimumTimeToVelocity
	 */
	public void setMinimumTimeToVelocity(double minimumTimeToVelocity);
	/**
	 * The minimum time to velocity. If the desired time to velocity is set to less than this value, it will be replaced by the minimumTimeToVelocity
	 * @return minimum time to velocity
	 */
	public double getMinimumTimeToVelocity();
	/**
	 * @param openPosition
	 */
	public void setOpenPosition(double openPosition);
	
	/**
	 * The initial position at which the shutter is expected to be open
	 */
	public double getOpenPosition();

	/**
	 * The exact position where the shutter is expected to be closed.
	 * @param closePosition
	 */
	public void setClosePosition(double closePosition);

	/**
	 * The final position at which the shutter is expected to be open
	 */
	public double getClosePosition();
	
	/**
	 * @return startPositionOffset the offset from the actual start position when prepareForExposure() is done
	 */
	public double getStartPositionOffset();
	
	/**
	 * @return endPositionOffset the offset from the close position that expose() will move to
	 */
	public double getEndPositionOffset();
	
	
	/**
	 * The minimum difference between the start and open positions.
	 * Also used for end and close positions.
	 * @return minimumDiff
	 */
	public double getMinimumStartToOpenPositionDifference();
	/**
	 * @param exposureTime
	 */
	public void setExposureTime(double exposureTime);
	
	/**
	 * @param fudgeTime
	 */
	public void setStartTimeFudgeFactor(double fudgeTime);
	
	/**
	 * @return fudgeTime the time allowed between the axis reaching full velocity and reaching the start position
	 */
	public double getStartTimeFudgeFactor();
	
	/**
	 * @param fudgeTime
	 */
	public void setEndTimeFudgeFactor(double fudgeTime);
	
	/**
	 * Retrieve the time between the close position and when the deceleration should start
	 * @return fudgeTime
	 */
	public double getEndTimeFudgeFactor();
	
	/**
	 * The actual position where the move will start
	 * @return start position
	 */
	public double getStartPosition();
	
	/**
	 * The actual position where the move will conclude
	 * @return final position
	 */
	public double getEndPosition();
	
	/**
	 * The distance between the open and close positions
	 * @param moveDistance
	 */
	public void setMoveDistance(double moveDistance);
	
	/**
	 * 
	 * @return distance between open and close positions
	 */
	public double getMoveDistance();
	
	public void setAcceleration(double acceleration);
	
	public double getAcceleration();
}