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

import uk.ac.gda.ui.components.MotionControlComposite.SAMPLE_WEIGHT;

/**
 *
 */
public interface ISampleWeightRotationHandler extends ITomoHandler {

	/**
	 * Handles the sample weight on the beamline controls like setting the velocity and accelaration etc.
	 * 
	 * @param sampleWeight
	 * @throws Exception 
	 */
	void handleSampleWeight(SAMPLE_WEIGHT sampleWeight) throws Exception;

}
