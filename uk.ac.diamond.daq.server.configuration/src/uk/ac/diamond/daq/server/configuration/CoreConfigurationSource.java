/*-
 * Copyright © 2023 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.server.configuration;

import java.net.URL;
import java.util.stream.Stream;

/** Source for configuration included in the built product */
public interface CoreConfigurationSource {
	default Stream<URL> getSpringXml() { return Stream.empty(); }
	default Stream<URL> getPropertiesFiles() { return Stream.empty(); }
	default Stream<URL> getLoggingConfiguration() { return Stream.empty(); }
}
