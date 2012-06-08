/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package gda.device.continuouscontroller;

import gda.device.DeviceException;

/**
 * A trajectory move controller represents a motor controller that move one or more axes through an arbitrary trajectory
 * while generating hardware triggers. Hardware triggers can be uniformly spaced in time if configured with
 * {@link #setTriggerPeriod(double)}, or may be non-uniform if configured with {@link #setTriggerDeltas(double[])}; not
 * all controllers need support the latter.
 */
public interface TrajectoryMoveController extends ContinuousMoveController {

	/**
	 * Get the number of axes configured on the controller. This may be less than the maximum number the controller
	 * supports,
	 * 
	 * @return number of configured axes.
	 */
	public int getNumberAxes();

	/**
	 * Called in sequence specifies the trajectory over all axes point by point. Alternatively use
	 * {@link #setAxisTrajectory(int, double[])}.
	 * 
	 * @param point
	 *            Array with a value for each axis. If an axis value is null null, indicating this axis is not to be
	 *            used, then all positions for this axis must also be null.
	 * @throws DeviceException
	 */
	public void addPoint(Double[] point) throws DeviceException;

	/**
	 * Return the last point added.
	 * 
	 * @return the last point added, or null if no points have been added or {@link #setAxisTrajectory(int, double[])}
	 *         has been used to configure the controller.
	 */
	public Double[] getLastPointAdded() throws DeviceException;

	/**
	 * Set the entire trajectory for one axis. Alternatively use {@link #addPoint(Double[])}.
	 * 
	 * @param axisIndex
	 * @param trajectory
	 * @throws DeviceException
	 * @throws InterruptedException 
	 */
	public void setAxisTrajectory(int axisIndex, double[] trajectory) throws DeviceException, InterruptedException;

	/**
	 * Set a profile of potentially non-uniformly spaced trigger times. May not be supported by all controllers.
	 * Alternatively use {@link #setTriggerPeriod(double)}.
	 * 
	 * @param triggerDeltas
	 * @throws DeviceException if not supported or the request is not possible.
	 */
	public void setTriggerDeltas(double[] triggerDeltas) throws DeviceException;

}
