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

package uk.ac.diamond.daq.mapping.ui.experiment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.scanning.api.points.models.GridModel;
import org.eclipse.scanning.api.points.models.IScanPathModel;
import org.eclipse.scanning.api.points.models.OneDEqualSpacingModel;
import org.eclipse.scanning.api.points.models.OneDStepModel;
import org.eclipse.scanning.api.points.models.RasterModel;
import org.eclipse.scanning.api.points.models.SinglePointModel;

import uk.ac.diamond.daq.mapping.api.IMappingScanRegionShape;
import uk.ac.diamond.daq.mapping.path.LissajousModel;
import uk.ac.diamond.daq.mapping.path.SpiralModel;
import uk.ac.diamond.daq.mapping.region.CircularMappingRegion;
import uk.ac.diamond.daq.mapping.region.LineMappingRegion;
import uk.ac.diamond.daq.mapping.region.PointMappingRegion;
import uk.ac.diamond.daq.mapping.region.PolygonMappingRegion;
import uk.ac.diamond.daq.mapping.region.RectangularMappingRegion;

public class MappingRegionManager {

	List<IMappingScanRegionShape> regions;
	List<IScanPathModel> twoDPaths;
	List<IScanPathModel> oneDPaths;
	List<IScanPathModel> zeroDPaths;

	public MappingRegionManager() {
		regions = new ArrayList<>();
		regions.add(new RectangularMappingRegion());
		regions.add(new CircularMappingRegion());
		regions.add(new PolygonMappingRegion());
		regions.add(new LineMappingRegion());
		regions.add(new PointMappingRegion());

		twoDPaths = new ArrayList<>();
		twoDPaths.add(new GridModel());
		twoDPaths.add(new RasterModel());
		twoDPaths.add(new SpiralModel());
		twoDPaths.add(new LissajousModel());

		oneDPaths = new ArrayList<>();
		oneDPaths.add(new OneDEqualSpacingModel());
		oneDPaths.add(new OneDStepModel());

		zeroDPaths = Arrays.<IScanPathModel> asList(new SinglePointModel());
	}

	public List<IMappingScanRegionShape> getRegions() {
		return regions;
	}

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
}
