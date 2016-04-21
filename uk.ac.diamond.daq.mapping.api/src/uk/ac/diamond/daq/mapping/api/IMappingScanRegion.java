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

import org.eclipse.scanning.api.points.models.IScanPathModel;

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
	public IMappingScanRegionShape getRegion();

	/**
	 * Sets the region to be used for the mapping scan.
	 *
	 * @param region
	 */
	public void setRegion(IMappingScanRegionShape region);

	/**
	 * Gets the scan path to use in the mapping scan to fill the region. This will typically be a path type (e.g. raster) and parameters defining it (e.g.
	 * xStep, yStep).
	 *
	 * @return scanPath
	 */
	public IScanPathModel getScanPath();

	/**
	 * Sets the scan path to use in the mapping scan to fill the region. This will typically be a path type (e.g. raster) and parameters defining it (e.g.
	 * xStep, yStep).
	 *
	 * @param scanPath
	 */
	public void setScanPath(IScanPathModel scanPath);

	// /**
	// * Gets a dataset representing the context of the scan region. In the anticipated usage, this will be an RGB screenshot of the map visualisation plot,
	// * showing the overview image or coarse map that was used to define this region, as it was seen by the user at the time the scan was started.
	// * <p>
	// * TODO an IDataset should not be serialized using JSON over a messaging system, so should not really be part of the experiment bean. The best alternative
	// * is probably to use the PersistenceService to save the context image (also allows saving of regions, masks etc as separate parts of a NeXus file) and
	// then
	// * pass the filename so the server can link to it or copy parts into the new scan file.
	// *
	// * @return The region context image. Might be <code>null</code> if the context has not been set for this scan.
	// */
	// public IDataset getRegionContext();
	//
	// /**
	// * Sets the dataset representing the context of the scan region. In the anticipated usage, this will be an RGB screenshot of the map visualisation plot,
	// * showing the overview image or coarse map that was used to define this region, as it was seen by the user at the time the scan was started.
	// *
	// * @param regionContext
	// */
	// public void setRegionContext(IDataset regionContext);
}
