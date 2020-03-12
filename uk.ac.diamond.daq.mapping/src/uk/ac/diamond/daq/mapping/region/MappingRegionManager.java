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

import static java.util.stream.Collectors.toList;

import java.util.Collections;
import java.util.List;

import org.eclipse.scanning.api.points.models.IScanPointGeneratorModel;

import uk.ac.diamond.daq.mapping.api.IMappingRegionManager;
import uk.ac.diamond.daq.mapping.api.IMappingScanRegionShape;
import uk.ac.diamond.daq.osgi.OsgiService;

@OsgiService(IMappingRegionManager.class)
public class MappingRegionManager implements IMappingRegionManager {

	// Initialise with empty lists - the real contents should be configured using Spring
	private List<IMappingScanRegionShape> regions = Collections.emptyList();
	private List<IScanPointGeneratorModel> twoDPaths = Collections.emptyList();
	private List<IScanPointGeneratorModel> oneDPaths = Collections.emptyList();
	private List<IScanPointGeneratorModel> zeroDPaths = Collections.emptyList();

	@Override
	public List<IMappingScanRegionShape> getTemplateRegions() {
		return regions.stream().map(IMappingScanRegionShape::copy).collect(toList());
	}

	@Override
	public <T extends IMappingScanRegionShape> T getTemplateRegion(Class<T> regionClass) {
		return regions.stream()
				.filter(regionClass::isInstance)
				.map(IMappingScanRegionShape::copy)
				.map(regionClass::cast)
				.findFirst()
				.orElseThrow(()-> new IllegalArgumentException("No region found of class '" + regionClass.getName() + "'"));
	}

	@Override
	public List<IScanPointGeneratorModel> getValidPaths(IMappingScanRegionShape scanRegion) {
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

	public void setTwoDPaths(List<IScanPointGeneratorModel> twoDPaths) {
		this.twoDPaths = twoDPaths;
	}

	public void setOneDPaths(List<IScanPointGeneratorModel> oneDPaths) {
		this.oneDPaths = oneDPaths;
	}

	public void setZeroDPaths(List<IScanPointGeneratorModel> zeroDPaths) {
		this.zeroDPaths = zeroDPaths;
	}
}
