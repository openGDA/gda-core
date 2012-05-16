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

package uk.ac.gda.ui.components;

import java.lang.reflect.InvocationTargetException;

import uk.ac.gda.ui.components.MotionControlComposite.SAMPLE_WEIGHT;

/**
 * Motion control listener interface.
 */
public interface IMotionControlListener extends IModuleChangeListener {

	/**
	 * @throws Exception
	 */
	void rotateLeft90() throws Exception;

	/**
	 * @param selected
	 * @throws Exception
	 */
	void moveAxisOfRotation(boolean selected) throws Exception;

	/**
	 * @throws Exception
	 */
	void rotateRight90() throws Exception;

	/**
	 * @throws Exception
	 */
	void degreeMovedBy(double degree) throws Exception;

	/**
	 * @throws Exception
	 */
	void degreeMovedTo(double degree) throws Exception;

	/**
	 * @param selected
	 * @throws Exception
	 */
	void horizontal(boolean selected) throws Exception;

	/**
	 * @param selected
	 */
	void findRotationAxis(boolean selected) throws Exception;

	/**
	 * @param selected
	 * @throws Exception
	 */
	void vertical(boolean selected) throws Exception;

	/**
	 * @param selected
	 * @throws Exception
	 */
	void tilt(boolean selected) throws Exception;

	/**
	 * Method to let listeners know that they should reset the camera distance field
	 * 
	 * @throws Exception
	 */
	void resetCameraDistance() throws Exception;

	/**
	 * Method to let listeners know that they should reset the X-ray energy field
	 */
	void resetXrayEnergy();

	/**
	 * to inform listeners that the camera distance has changed.
	 * 
	 * @param cameraDistance
	 * @throws InterruptedException
	 * @throws InvocationTargetException
	 * @throws Exception
	 */
	void cameraDistanceChanged(double cameraDistance) throws InvocationTargetException, InterruptedException, Exception;

	/**
	 * to inform the listeners that the x-ray energy has changed.
	 * 
	 * @param xRayEnergy
	 */
	void xRayEnergyChanged(double xRayEnergy);

	/**
	 * to inform the listeners that the sample weight has changed.
	 * 
	 * @param sampleWeight
	 */
	void setSampleWeight(SAMPLE_WEIGHT sampleWeight);
}
