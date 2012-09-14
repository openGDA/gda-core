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

import uk.ac.gda.client.tomo.composites.MotionControlComposite.SAMPLE_WEIGHT;
import uk.ac.gda.client.tomo.i12.ThetaServoSet;

/**
 * Interface that presents the owners of the objects of this class with the lookup table values.
 */
public interface ISampleWeightLookupTableHandler {

	/**
	 * @param sampleWeight
	 * @return big step velocity for the sample weight
	 * @throws Exception 
	 */
	double getBigStepVelocity(SAMPLE_WEIGHT sampleWeight) throws Exception;

	/**
	 * @param sampleWeight
	 * @return big step servo set for the sample weight
	 * @throws Exception 
	 */
	ThetaServoSet getBigStepServoSet(SAMPLE_WEIGHT sampleWeight) throws Exception;

	/**
	 * @param sampleWeight
	 * @return big step Accl for the given sample weight
	 * @throws Exception 
	 */
	double getBigStepAccl(SAMPLE_WEIGHT sampleWeight) throws Exception;

	/**
	 * @param sampleWeight
	 * @return small step velocity for the sample weight
	 * @throws Exception 
	 */
	double getSmallStepVelocity(SAMPLE_WEIGHT sampleWeight) throws Exception;

	/**
	 * @param sampleWeight
	 * @return small step servo set for the sample weight
	 * @throws Exception 
	 */
	ThetaServoSet getSmallStepServoSet(SAMPLE_WEIGHT sampleWeight) throws Exception;

	/**
	 * @param sampleWeight
	 * @return small step Accl for the given sample weight
	 * @throws Exception 
	 */
	double getSmallStepAccl(SAMPLE_WEIGHT sampleWeight) throws Exception;
}
