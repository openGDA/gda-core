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

package uk.ac.gda.client.properties;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Configures various tests related to properties parsing.
 *
 * @author Maurizio Nagni
 */
@Configuration
@ComponentScan(basePackages = {
		"uk.ac.gda.client.properties",
		"uk.ac.gda.ui.tool.spring",
		"uk.ac.gda.core.tool.spring",
		"uk.ac.diamond.daq.mapping.api.document"})
public class ConfigurationPropertiesTestConfiguration {

}
