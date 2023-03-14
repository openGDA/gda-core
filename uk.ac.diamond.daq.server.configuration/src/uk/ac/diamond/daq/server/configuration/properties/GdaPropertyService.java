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

package uk.ac.diamond.daq.server.configuration.properties;

import static org.osgi.service.component.annotations.ReferenceCardinality.MANDATORY;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import gda.configuration.properties.LocalProperties;
import uk.ac.diamond.daq.server.configuration.BeamlineConfiguration;
import uk.ac.diamond.daq.services.PropertyService;
import uk.ac.diamond.daq.util.logging.deprecation.DeprecationLogger;

/**
 * A service implementation to provide access to GDA properties. If a service
 * needs access to properties, depending on this service is the preferred way to
 * access them.
 *
 * @since 9.6
 * @author James Mudd
 */
@Component(name="GdaPropertyService", immediate=true)
public class GdaPropertyService implements PropertyService {
	private static final DeprecationLogger logger = DeprecationLogger.getLogger(GdaPropertyService.class);

	@Reference(cardinality = MANDATORY)
	private BeamlineConfiguration config;

	@Activate
	public void activate() {
		config.getPropertiesFiles().forEach(LocalProperties::loadPropertiesFrom);
		config.directProperties().entrySet()
				.forEach(e -> LocalProperties.set(e.getKey(), e.getValue()));
	}

	@Override
	public String getAsString(String property, String defaultValue) {
		return config.properties().getString(property, defaultValue);
	}

	@Override
	public int getAsInt(String property, int defaultValue) {
		return config.properties().getInt(property, defaultValue);
	}

	@Override
	public double getAsDouble(String property, double defaultValue) {
		return config.properties().getDouble(property, defaultValue);
	}

	@Override
	public boolean getAsBoolean(String property, boolean defaultValue) {
		return config.properties().getBoolean(property, defaultValue);
	}

	@Override
	public boolean isSet(String property) {
		return config.properties().containsKey(property);
	}

	@Override
	@Deprecated(since="GDA 9.6")
	public void set(String property, String value) {
		logger.deprecatedMethod("set(String, String)");
		logger.warn("Setting the property '{}' to '{}'. This feature will be removed in the future", property, value);
		config.properties().setProperty(property, value);
	}

}
