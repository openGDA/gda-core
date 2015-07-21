/*-
 * Copyright © 2015 Diamond Light Source Ltd.
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

package gda.data.nexus.hdf5;

import ncsa.hdf.hdf5lib.H5;
import ncsa.hdf.hdf5lib.exceptions.HDF5LibraryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HDF5PropertiesResource extends HDF5BaseResource {

	private static final Logger logger = LoggerFactory.getLogger(HDF5PropertiesResource.class);

	public HDF5PropertiesResource() {
	}

	/**
	 * Wrap the specified datatype resource identifier
	 * @param resource datatype identifier to wrap
	 */
	public HDF5PropertiesResource(int resource) {
		this.resource = resource;
	}

	@Override
	public void close() {
		if (resource < 0) {
			return;
		}
		try {
			H5.H5Pclose((int)resource);
			resource = -1;
		} catch (HDF5LibraryException e) {
			logger.error("Could not close HDF5 object", e);
		}
	}
}
