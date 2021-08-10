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

import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.ac.gda.api.acquisition.Acquisition;
import uk.ac.gda.api.acquisition.configuration.processing.ProcessingRequestPair;
import uk.ac.gda.api.acquisition.parameters.AcquisitionParameters;
import uk.ac.gda.api.acquisition.parameters.DevicePositionDocument;


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

	/**
	 * The configuration for pre/post processing requests.
	 * <p>
	 * Because the value mapped to each key is a {@link String}, complex structures can be encoded using formats like XML or JSON.
	 * Consequence of this is that either the acquisition engine or the client submitting a request to the acquisition engine
	 * should be aware of the format sent with the specific key. By default the value should be assumed to be a JSON document
	 * </p>
	 * @return a map of key, value.
	 */
	List<ProcessingRequestPair<?>> getProcessingRequest();


	/**
	 * Defines where the beamline is supposed to return at the end of the acquisition.
	 * <p>
	 * An Acquisition may be composed by multiple sections: acquisition, calibrations (that is dark, flat) eventually others.
	 * Each of those section may well have its specific configuration so the beamline may change accordingly.
	 * On the other hand this property, if not empty, represent the state where to move when all the other sections have been executed.
	 * </p>
	 * @return a set of position documents, otherwise an empty set.
	 */
	Set<DevicePositionDocument> getEndPosition();
}
