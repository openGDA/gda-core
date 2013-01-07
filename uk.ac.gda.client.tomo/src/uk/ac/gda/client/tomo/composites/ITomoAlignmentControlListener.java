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

package uk.ac.gda.client.tomo.composites;

import java.lang.reflect.InvocationTargetException;

import uk.ac.gda.client.tomo.composites.TomoAlignmentControlComposite.RESOLUTION;
import uk.ac.gda.client.tomo.composites.TomoAlignmentControlComposite.SAMPLE_WEIGHT;

/**
 * Motion control listener interface.
 */
public interface ITomoAlignmentControlListener extends IModuleChangeListener {

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
	 * @param selected
	 * @throws Exception
	 */
	void autoFocus(boolean selected) throws Exception;

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
	 * @throws Exception
	 */
	void setSampleWeight(SAMPLE_WEIGHT sampleWeight) throws Exception;

	/**
	 * Event fired to update the preferred sample exposure time.
	 * 
	 * @throws Exception
	 */
	void updatePreferredSampleExposureTime() throws Exception;

	/**
	 * * Event fired to update the preferred sample exposure time.
	 * 
	 * @throws Exception
	 */
	void updatePreferredFlatExposureTime() throws Exception;

	/**
	 * @throws Exception
	 */
	void sampleFlatTimeChanged() throws Exception;

	/**
	 * Save alignment configuration
	 * 
	 * @throws InterruptedException
	 * @throws InvocationTargetException
	 * @throws Exception
	 */
	String saveAlignmentConfiguration() throws InvocationTargetException, InterruptedException, Exception;

	/**
	 * Informs the listeners that "Define ROI" button has been selected.
	 * 
	 * @param selection
	 */
	void defineRoi(boolean selection);

	/**
	 * Informs the listeners to reset the ROI bounds
	 */
	void resetRoi();

	/**
	 * Invokes listeners when the resolution button is clicked.
	 * 
	 * @param resolution
	 * @throws Exception
	 */
	void resolutionChanged(RESOLUTION resolution) throws Exception;

	/**
	 * Invokes listeners when the save operation is complete.
	 * 
	 * @param experimentConfigId
	 */
	void saveComplete(String experimentConfigId);

}
