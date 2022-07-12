/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package uk.ac.gda.ui.tool.spring.properties;

import java.util.Collections;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.ac.gda.api.acquisition.AcquisitionPropertyType;
import uk.ac.gda.client.properties.acquisition.AcquisitionConfigurationProperties;
import uk.ac.gda.ui.tool.spring.ClientSpringProperties;

/**
 * A set of utilities to use the Client Spring loaded AcquisitionConfigurationProperties
 */
@Component
public class AcquisitionConfigurationPropertiesUtils {

	@Autowired
	private ClientSpringProperties clientProperties;

	/**
	 * Returns the cameras for a given {@link AcquisitionPropertyType}
	 * @param acquisitionType the acquisition type to filter
	 * @return a set of camera ids, eventually empty
	 */
	public final Set<String> getCameras(AcquisitionPropertyType acquisitionType) {
		return clientProperties.getAcquisitions().stream()
				.filter(acquisitionConf -> acquisitionType.equals(acquisitionConf.getType()))
				.findFirst()
				.map(AcquisitionConfigurationProperties::getCameras)
				.orElse(Collections.emptySet());
		}

}
