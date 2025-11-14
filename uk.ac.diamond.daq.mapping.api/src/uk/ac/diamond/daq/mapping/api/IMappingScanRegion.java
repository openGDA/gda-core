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

package uk.ac.diamond.daq.mapping.api;

import org.eclipse.scanning.api.points.models.IMapPathModel;

/**
 * This interface should be implemented by any class which can represent a mapping scan region. It includes the region shape (e.g. rectangle, circle) and a scan
 * path (e.g. raster, spiral).
 */
public interface IMappingScanRegion {

	/**
	 * Gets the region to be used for the mapping scan.
	 *
	 * @return region
	 */
	IMappingScanRegionShape getRegion();

	/**
	 * Sets the region to be used for the mapping scan.
	 *
	 * @param region
	 */
	void setRegion(IMappingScanRegionShape region);

	/**
	 * Gets the scan path to use in the mapping scan to fill the region. This will typically be a path type (e.g. raster) and parameters defining it (e.g.
	 * xStep, yStep).
	 *
	 * @return scanPath
	 */
	IMapPathModel getScanPath();

	/**
	 * Sets the scan path to use in the mapping scan to fill the region. This will typically be a path type (e.g. raster) and parameters defining it (e.g.
	 * xStep, yStep).
	 *
	 * @param scanPath
	 */
	void setScanPath(IMapPathModel scanPath);
}
