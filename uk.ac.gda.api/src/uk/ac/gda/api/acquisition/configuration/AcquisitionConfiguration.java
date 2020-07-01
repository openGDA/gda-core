/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package uk.ac.gda.api.acquisition.configuration;

import java.util.Map;

import uk.ac.gda.api.acquisition.Acquisition;
import uk.ac.gda.api.acquisition.parameters.AcquisitionParameters;


/**
 * Represents the workflow of an {@link Acquisition}.
 *
 * Actually may defines {@link MultipleScans} and {@link ImageCalibration} configurations.
 *
 * @author Maurizio Nagni
 */
public interface AcquisitionConfiguration<T extends AcquisitionParameters> {

	/**
	 * The configuration for acquisition multiple repetitions
	 * @return the repetition configuration
	 */
	MultipleScans getMultipleScans();

	/**
	 * The configuration for image calibration acquisition
	 * @return the calibration acquisition procedure
	 */
	ImageCalibration getImageCalibration();

	/**
	 * @return the parameters defining the acquisition
	 */
	T getAcquisitionParameters();

	/**
	 * @return a dictionary of text data
	 */
	Map<String, String> getMetadata();

}
