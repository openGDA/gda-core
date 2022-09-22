/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package uk.ac.gda.client.tomo.alignment.view.handlers;

import gda.device.DeviceException;

import org.eclipse.core.runtime.IProgressMonitor;

public interface ICameraStageMotorHandler {
	/**
	 * Delegate method to move the T3.X motor to the given position [on i12 - t3.x - Main X on the large detector table]
	 * 
	 * @param monitor
	 * @param t3xMoveToPosition
	 *            - the position to which this motor should be moved to.
	 * @throws DeviceException
	 * @throws InterruptedException
	 */
	void moveT3XTo(IProgressMonitor monitor, Double t3xMoveToPosition) throws DeviceException, InterruptedException;

	/**
	 * Delegate method to move the T3.M1Y motor to the given position [on i12 - t3.m1y - Mod1 Y on the large detector
	 * table]
	 * 
	 * @param monitor
	 * @param t3m1yMoveToPosition
	 *            - the position to which this motor should be moved to.
	 * @throws DeviceException
	 * @throws InterruptedException
	 */
	void moveT3M1YTo(IProgressMonitor monitor, Double t3m1yMoveToPosition) throws DeviceException, InterruptedException;

	/**
	 * Delegate method to move the T3.MZY motor to the given position [on i12 - t3.mZy - Mod1 Z on the large detector
	 * table]
	 * 
	 * @param monitor
	 * @param t3m1zValue
	 *            - the position to which this motor should be moved to.
	 * @throws DeviceException
	 * @throws InterruptedException
	 */

	void moveT3M1ZTo(IProgressMonitor monitor, double t3m1zValue) throws DeviceException, InterruptedException;

	/**
	 * @return the motor position of the T3.X motor [on i12 - t3.x - Main X on the large detector table]
	 * @throws DeviceException
	 */
	Double getT3XPosition() throws DeviceException;

	/**
	 * @return the motor position of the T3.m1z motor [on i12 - t3.m1z - Main Z on the large detector table]
	 * @throws DeviceException
	 */
	Double getT3M1ZPosition() throws DeviceException;

	/**
	 * @return the motor position of the T3.M1Y motor [on i12 - t3.m1y - Mod1 Y on the large detector table]
	 * @throws DeviceException
	 */
	Double getT3M1YPosition() throws DeviceException;

	/**
	 * @return the dead band retry value of the T3.X motor [on i12 - t3.x - Main X on the large detector table]
	 */
	double getT3XTolerance();

	/**
	 * @return the dead band retry value of the T3.M1Y motor [on i12 - t3.m1y - Mod1 Y on the large detector table]
	 */
	double getT3M1YTolerance();

	/**
	 * @return the user offset for t3.x motor
	 * @throws DeviceException
	 */
	double getT3xOffset() throws DeviceException;

	/**
	 * @return the user offset for t3.m1z motor
	 * @throws DeviceException
	 */
	double getT3m1zOffset() throws DeviceException;

	/**
	 * @return the user offset for t3.m1y motor
	 * @throws DeviceException
	 */
	double getT3m1yOffset() throws DeviceException;

	/**
	 * @return name of the motor responsible for camera stage z axisF - (on i12 it is t3_m1z)
	 */
	String getCameraStageZMotorName();

	/**
	 * @return name of the detector stage x motor
	 */
	String getT3XMotorName();

	/**
	 * @return detector stage z motor
	 */
	String getT3m1ZMotorName();

	/**
	 * @return name of the detector stage y motor
	 */
	String getT3m1YMotorName();
}
