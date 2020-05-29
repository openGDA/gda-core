/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package uk.ac.gda.api.acquisition.resource;

/**
 * Defines the type of the {@link AcquisitionConfigurationResource}.
 * Each type contains the a string representing the extension to be used when the configuration is saved as file
 *
 * @author Maurizio Nagni
 */
public enum AcquisitionConfigurationResourceType {

	/**
	 * Identifies a tomography configuration
	 */
	TOMO("tomo"),
	/**
	 * Identifies a mapping scan configuration
	 */
	MAP("map"),
	/**
	 * Identifies an experiment plan configuration
	 */
	PLAN("plan");

	private final String extension;

	AcquisitionConfigurationResourceType(String extension) {
		this.extension = extension;
	}

	/**
	 * The acquisition configuration extension
	 * @return the type extension
	 */
	public String getExtension() {
		return extension;
	}
}
