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

package uk.ac.diamond.daq.mapping.region;

import java.util.Collections;
import java.util.List;

import org.eclipse.scanning.api.points.models.IScanPathModel;

import uk.ac.diamond.daq.mapping.api.IMappingRegionManager;
import uk.ac.diamond.daq.mapping.api.IMappingScanRegionShape;

public class MappingRegionManager implements IMappingRegionManager {

	// Initialise with empty lists - the real contents should be configured using Spring
	private List<IMappingScanRegionShape> regions = Collections.emptyList();
	private List<IScanPathModel> twoDPaths = Collections.emptyList();
	private List<IScanPathModel> oneDPaths = Collections.emptyList();
	private List<IScanPathModel> zeroDPaths = Collections.emptyList();

	public MappingRegionManager() {
	}

	@Override
	public List<IMappingScanRegionShape> getRegions() {
		return regions;
	}

	@Override
	public List<IScanPathModel> getValidPaths(IMappingScanRegionShape scanRegion) {
		// If a point, return the zero-dimensional single point path
		if (scanRegion instanceof PointMappingRegion) {
			return zeroDPaths;
		}
		// If a line, only one-dimensional paths are valid
		if (scanRegion instanceof LineMappingRegion) {
			return oneDPaths;
		}
		// If not a point or a line, assume we have a two-dimensional shape
		return twoDPaths;
	}

	public void setRegions(List<IMappingScanRegionShape> regions) {
		this.regions = regions;
	}

	public void setTwoDPaths(List<IScanPathModel> twoDPaths) {
		this.twoDPaths = twoDPaths;
	}

	public void setOneDPaths(List<IScanPathModel> oneDPaths) {
		this.oneDPaths = oneDPaths;
	}

	public void setZeroDPaths(List<IScanPathModel> zeroDPaths) {
		this.zeroDPaths = zeroDPaths;
	}
}
