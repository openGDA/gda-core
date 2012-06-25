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
import gov.aps.jca.CAException;
import gov.aps.jca.TimeoutException;

import org.eclipse.core.runtime.IProgressMonitor;

import uk.ac.gda.ui.components.ModuleButtonComposite.CAMERA_MODULE;

/**
 *
 */
public interface ICameraMotionController extends ITomoHandler{

	/**
	 * @param module
	 * @return the double value for t3.x - this is obtained by finding the t3.m1z value and then looking up
	 *         corresponding values for t3.x
	 * @throws DeviceException
	 * @throws TimeoutException
	 * @throws CAException
	 * @throws InterruptedException
	 * @throws Exception
	 */
	double getT3X(CAMERA_MODULE module) throws DeviceException, TimeoutException, CAException, InterruptedException,
			Exception;

	/**
	 * @param module
	 * @return the double value for t3.m1y - this is obtained by finding the t3.m1z value and then looking up
	 *         corresponding values for t3.m1y
	 */
	double getT3M1y(CAMERA_MODULE module) throws DeviceException, TimeoutException, CAException, InterruptedException,
			Exception;

	/**
	 * Moves t3m1Zto the necessary position and moves the t3.x and the t3.m1y to appropriate positions by looking up
	 * their values.
	 * 
	 * @param monitor
	 * @param module
	 * @param t3m1zValue
	 * @throws DeviceException
	 * @throws InterruptedException
	 */
	void moveT3m1ZTo(IProgressMonitor monitor, CAMERA_MODULE module, double t3m1zValue) throws DeviceException,
			InterruptedException;
}
