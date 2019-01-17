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

package uk.ac.diamond.daq.mapping.ui.experiment;

import static gda.configuration.properties.LocalProperties.GDA_INITIAL_LENGTH_UNITS;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;

/**
 * This class provides a mapping of path property (e.g. "fastAxisStep", "xCentre") to the initial unit to be displayed
 * in the mapping GUI for the relevant field.
 * <p>
 * If no unit has been configured for a given field, the value returned will default to:
 * <ul>
 * <li>the value set in the uk.ac.gda.client.defaultUnits property</li>
 * <li>millimetres if this property is not set</li>
 * </ul>
 */
public class InitialLengthUnits {
	private static final Logger logger = LoggerFactory.getLogger(InitialLengthUnits.class);

	private final Map<String, String> units;

	public InitialLengthUnits(Map<String, String> units) {
		if (units == null) {
			throw new IllegalArgumentException("Default units map must not be null");
		}
		this.units = units;
	}

	public String getDefaultUnit(String propertyName) {
		if (units.containsKey(propertyName)) {
			return units.get(propertyName);
		}

		final String defaultUnits = LocalProperties.get(GDA_INITIAL_LENGTH_UNITS, "mm").toLowerCase();
		logger.debug("No initial unit set for {}, using default ({})", propertyName, defaultUnits);
		return defaultUnits;
	}
}
