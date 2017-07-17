/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.dawnsci.analysis.api.persistence.IClassRegistry;

import uk.ac.diamond.daq.mapping.region.CentredRectangleMappingRegion;
import uk.ac.diamond.daq.mapping.region.CircularMappingRegion;
import uk.ac.diamond.daq.mapping.region.LineMappingRegion;
import uk.ac.diamond.daq.mapping.region.PointMappingRegion;
import uk.ac.diamond.daq.mapping.region.PolygonMappingRegion;
import uk.ac.diamond.daq.mapping.region.RectangularMappingRegion;

public class MappingUIClassRegistry implements IClassRegistry {

	private static final Map<String, Class<?>> idToClassMap;

	static {
		Map<String, Class<?>> tmp = new HashMap<>();

		registerClass(tmp, DetectorModelWrapper.class);
		registerClass(tmp, ClusterProcessingModelWrapper.class);
		registerClass(tmp, ScanPathModelWrapper.class);
		registerClass(tmp, MappingScanDefinition.class);
		registerClass(tmp, MappingScanRegion.class);
		registerClass(tmp, CircularMappingRegion.class);
		registerClass(tmp, LineMappingRegion.class);
		registerClass(tmp, PointMappingRegion.class);
		registerClass(tmp, PolygonMappingRegion.class);
		registerClass(tmp, RectangularMappingRegion.class);
		registerClass(tmp, CentredRectangleMappingRegion.class);
		registerClass(tmp, SimpleSampleMetadata.class);
		registerClass(tmp, ScriptFiles.class);

		idToClassMap = Collections.unmodifiableMap(tmp);
	}

	private static void registerClass(Map<String, Class<?>> map, Class<?> clazz) {
		map.put(clazz.getSimpleName(), clazz);
	}

	@Override
	public Map<String, Class<?>> getIdToClassMap() {
		return idToClassMap;
	}

}
