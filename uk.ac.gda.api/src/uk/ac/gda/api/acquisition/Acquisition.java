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
import java.util.UUID;

import uk.ac.gda.api.acquisition.configuration.AcquisitionConfiguration;
import uk.ac.gda.api.acquisition.parameters.AcquisitionParameters;


/**
 * Represents an acquisition workflow.
 *
 * An acquisition procedure is a workflow defined by an {@link AcquisitionConfiguration} which is based on a {@link AcquisitionParameters}.
 *
 * @author Maurizio Nagni
 */
public interface Acquisition<T extends AcquisitionConfiguration<? extends AcquisitionParameters>> {

	/**
	 * An universal immutable identifier to discriminate between different acquisitions
	 *
	 * @return the acquisition identifier
	 */
	public UUID getUuid();

	/**
	 * A brief definition of the acquisition.
	 *
	 * @return the acquisition name
	 */
	public String getName();

	/**
	 * A short description of the acquisition.
	 *
	 * @return the acquisition abstract
	 */
	public String getDescription();

	/**
	 * The period between which the acquisition has been running
	 *
	 * @return the acquisition start and finish dates
	 */
	public Period getExecutionPeriod();

	/**
	 * Where this acquisition is, or will be stored. In future may be related to {@link #getUuid()}.
	 *
	 * @return the acquisition URL
	 */
	public URL getAcquisitionLocation();

	/**
	 * The acquisition workflow
	 *
	 * @return instructions for the acquisition engine
	 */
	public T getAcquisitionConfiguration();
}