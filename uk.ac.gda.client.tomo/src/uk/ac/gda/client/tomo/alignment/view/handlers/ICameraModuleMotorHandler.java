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

public interface ICameraModuleMotorHandler {

	/**
	 * @return the position of the Lens motor [on i12 cam1.x on the Monochromatic camera]
	 * @throws DeviceException
	 */
	double getCam1XPosition() throws DeviceException;

	/**
	 * Move the Lens motor [on i12 cam1.x on the Monochromatic camera] to the given position
	 * 
	 * @param monitor
	 * @param cam1xPos
	 * @throws DeviceException
	 * @throws InterruptedException
	 */
	void moveCam1X(IProgressMonitor monitor, double cam1xPos) throws DeviceException, InterruptedException;

	/**
	 * @return the position of the Optic focus motor [on i12 cam1.z on the Monochromatic camera]
	 * @throws DeviceException
	 */
	double getCam1ZPosition() throws DeviceException;

	/**
	 * @param monitor
	 * @param cam1ZPos
	 *            - position to which the motor should be moved to.
	 * @throws DeviceException
	 * @throws InterruptedException
	 */
	void moveCam1Z(IProgressMonitor monitor, Double cam1ZPos) throws DeviceException, InterruptedException;

	/**
	 * @return the position of the Optic roll motor [on i12 cam1.roll on the Monochromatic camera]
	 * @throws DeviceException
	 */
	double getCam1RollPosition() throws DeviceException;

	/**
	 * Move the Optic roll motor [on i12 cam1.roll on the Monochromatic camera] to the given position.
	 * 
	 * @param monitor
	 * @param cam1RollPos
	 * @throws DeviceException
	 * @throws InterruptedException
	 */
	void moveCam1Roll(IProgressMonitor monitor, double cam1RollPos) throws DeviceException, InterruptedException;

	/**
	 * @return the dead band retry value of the Lens motor [on i12 cam1.x on the Monochromatic camera]
	 */
	double getCam1XTolerance();

	/**
	 * @return the dead band retry value of the Optic focus motor [on i12 cam1.z on the Monochromatic camera]
	 */
	double getCam1ZTolerance();
	
	/**
	 * @return the dead band retry value of the Optic roll motor [on i12 cam1.roll on the Monochromatic camera]
	 */
	double getCam1RollTolerance();

}
