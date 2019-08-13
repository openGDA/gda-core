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

package uk.ac.gda.tomography.model;

import java.net.URL;
import java.time.Period;
import java.util.List;
import java.util.UUID;

/**
 * An acquisition is a procedure defined by a configuration and executed by a script aware of the configuration format.
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
	 * The script which executes the acquisition.
	 *
	 * @return the location where find the script
	 */
	public URL getScript();

	/**
	 * The data necessary to execute the acquisition
	 *
	 * @return the acquisition execution parameters
	 */
	public T getAcquisitionConfiguration();

	/**
	 * A list of time stamped notes which could be either automatically generated or created by a user
	 *
	 * @return the acquisition logs
	 */
	public List<ActionLog> getLogs();

}
