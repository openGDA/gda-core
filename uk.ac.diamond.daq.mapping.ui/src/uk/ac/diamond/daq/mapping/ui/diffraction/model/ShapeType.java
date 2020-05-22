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

package uk.ac.diamond.daq.mapping.ui.diffraction.model;

import uk.ac.diamond.daq.mapping.api.IMappingScanRegionShape;
import uk.ac.diamond.daq.mapping.region.CentredRectangleMappingRegion;
import uk.ac.diamond.daq.mapping.region.LineMappingRegion;
import uk.ac.diamond.daq.mapping.region.PointMappingRegion;

public enum ShapeType {
	POINT(PointMappingRegion.class, new String[] {}),
	LINE(LineMappingRegion.class, new String[] { "points" }),
	CENTRED_RECTANGLE(CentredRectangleMappingRegion.class, new String[] { "xAxisPoints", "yAxisPoints" });

	private final Class<? extends IMappingScanRegionShape> mappingShape;
	private final String[] properties;

	private ShapeType(Class<? extends IMappingScanRegionShape> mappingShape, String[] properties) {
		this.mappingShape = mappingShape;
		this.properties = properties;
	}

	public boolean hasMappedShape(IMappingScanRegionShape regionShape) {
		return mappingShape.isInstance(regionShape);
	}

	public String[] getProperties() {
		return properties;
	}
}
