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

package uk.ac.gda.api.acquisition;

import java.net.URL;
import java.time.Period;

import uk.ac.gda.api.acquisition.configuration.AcquisitionConfiguration;
import uk.ac.gda.api.acquisition.parameters.AcquisitionParameters;
import uk.ac.gda.common.entity.Document;


/**
 * Represents an acquisition workflow.
 *
 * An acquisition procedure is a workflow defined by an {@link AcquisitionConfiguration} which is based on a {@link AcquisitionParameters}.
 *
 * @author Maurizio Nagni
 */
public interface Acquisition<T extends AcquisitionConfiguration<? extends AcquisitionParameters>> extends Document {

	/**
	 * The period between which the acquisition has been running
	 *
	 * @return the acquisition start and finish dates
	 */
	Period getExecutionPeriod();

	/**
	 * Where this acquisition is, or will be stored. In future may be related to {@link #getUuid()}.
	 *
	 * @return the acquisition URL
	 */
	URL getAcquisitionLocation();

	/**
	 * The acquisition workflow
	 *
	 * @return instructions for the acquisition engine
	 */
	T getAcquisitionConfiguration();

	/**
	 * The acquisition engine responsible for executing this {@code Acquisition}
	 * @return the acquisition engine, otherwise {@code null} if the engine selection is delegated to an external controller
	 */
	AcquisitionEngineDocument getAcquisitionEngine();
}
