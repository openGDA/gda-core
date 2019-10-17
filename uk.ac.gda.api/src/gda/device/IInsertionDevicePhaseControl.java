/*-
 * Copyright © 2015 Diamond Light Source Ltd.
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

package gda.device;

public interface IInsertionDevicePhaseControl extends Device {
	public final String TOP_OUTER_AXIS = "BLPUOMTR";
	public final String TOP_INNER_AXIS = "BLPUIMTR";
	public final String BOTTOM_OUTER_AXIS = "BLPLOMTR";
	public final String BOTTOM_INNER_AXIS = "BLPLIMTR";
	public final String ID_MODE="BLMSEL";
	public final String ID_MOVE="ALLMOVE";
	public final String PV_SEPARATOR=":";

	public final String GAP_AND_PHASE_MODE="GAP AND PHASE";

	public final double LINEAR_HORIZONTAL_TOP_OUTER_MOTOR_POSITION=0.0;
	public final double LINEAR_HORIZONTAL_TOP_INNER_MOTOR_POSITION=0.0;
	public final double LINEAR_HORIZONTAL_BOTTOM_OUTER_MOTOR_POSITION=0.0;
	public final double LINEAR_HORIZONTAL_BOTTOM_INNER_MOTOR_POSITION=0.0;

	public final double LINEAR_VERTICAL_TOP_OUTER_MOTOR_POSITION=30.0;
	public final double LINEAR_VERTICAL_TOP_INNER_MOTOR_POSITION=0.0;
	public final double LINEAR_VERTICAL_BOTTOM_OUTER_MOTOR_POSITION=0.0;
	public final double LINEAR_VERTICAL_BOTTOM_INNER_MOTOR_POSITION=30.0;

	public final String ID_MOVE_START="1";
	/**
	 * move to linear horizontal phase
	 * @throws DeviceException
	 */
	public void hortizontal() throws DeviceException;
	/**
	 * move to linear vertical phase
	 * @throws DeviceException
	 */
	public void vertical() throws DeviceException;
	/**
	 * move to phase specified.
	 * A phase to motor positions lookup table required to support this method.
	 * @param phaseInDegree
	 * @throws DeviceException
	 */
	public void moveToPhase(double phaseInDegree) throws DeviceException;
	public boolean isBusy();
}
