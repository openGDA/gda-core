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

package uk.ac.diamond.daq.mapping.api;

import static gda.configuration.properties.LocalProperties.GDA_INITIAL_LENGTH_UNITS;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import uk.ac.diamond.daq.osgi.OsgiService;

/**
 * This class provides a mapping of path property (e.g. "xAxisStep", "xCentre", "radius") to the unit to be displayed in the
 * mapping GUI for the relevant field.
 * <p>
 * It can be used, for example, to define the initial unit to be shown for each field after a client reset, and also to
 * save the state of the GUI when submitting a scan.
 * <p>
 * If no unit is mapped for a given field, the value returned will default to:
 * <ul>
 * <li>the value set in the uk.ac.gda.client.defaultUnits property, or if this is not set,</li>
 * <li>the default value supplied by the caller</li>
 * </ul>
 */
@OsgiService(MappingRegionUnits.class)
public class MappingRegionUnits {
	private static final Logger logger = LoggerFactory.getLogger(MappingRegionUnits.class);

	private final Map<String, String> units;

	public MappingRegionUnits(Map<String, String> units) {
		if (units == null) {
			throw new IllegalArgumentException("Default units map must not be null");
		}
		this.units = units;
	}

	public String getUnits(String propertyName, String defaultValue) {
		if (units.containsKey(propertyName)) {
			return units.get(propertyName);
		}

		final String result = LocalProperties.get(GDA_INITIAL_LENGTH_UNITS, defaultValue).toLowerCase();
		logger.debug("No initial units set for {}, using default ({})", propertyName, result);
		return result;
	}
}
