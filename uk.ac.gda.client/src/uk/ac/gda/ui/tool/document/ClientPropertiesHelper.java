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

package uk.ac.gda.ui.tool.document;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.ac.gda.api.acquisition.AcquisitionType;
import uk.ac.gda.client.properties.acquisition.AcquisitionConfigurationProperties;
import uk.ac.gda.client.properties.acquisition.AcquisitionPropertyType;
import uk.ac.gda.client.properties.camera.CameraConfigurationProperties;
import uk.ac.gda.client.properties.mode.Modes;
import uk.ac.gda.ui.tool.spring.ClientSpringContext;

/**
 * Collects together the most used methods to extract data from the client properties
 *
 * @author Maurizio Nagni
 */
@Component
public class ClientPropertiesHelper {

	@Autowired
	private ClientSpringContext clientSpringContext;

	private ClientSpringContext getClientSpringContext() {
		return clientSpringContext;
	}

	/**
	 * Returns the {@link AcquisitionConfigurationProperties} associated with a specific acquisition property type
	 * @param type the required acquisition property type
	 * @return an optional element
	 */
	public Optional<AcquisitionConfigurationProperties> getAcquisitionConfigurationProperties(AcquisitionPropertyType type) {
		return getAcquisitionPropertiesDocuments().stream()
				.filter(a -> a.getType().equals(type))
				.findFirst();
	}


	/**
	 * Returns the {@link AcquisitionConfigurationProperties} associated with a specific acquisition type
	 * @param type the required acquisition type
	 * @return an optional element
	 */
	public Optional<AcquisitionConfigurationProperties> getAcquisitionConfigurationProperties(AcquisitionType type) {
		return getAcquisitionConfigurationProperties(DocumentFactory.getType(type));
	}

	/**
	 * Returns the available {@link AcquisitionConfigurationProperties}s
	 * @return the available collection
	 */
	public List<AcquisitionConfigurationProperties> getAcquisitionPropertiesDocuments() {
		return getClientSpringContext().getClientProperties().getAcquisitions();
	}

	/**
	 * Returns the available {@link AcquisitionConfigurationProperties}s
	 * @return the available collection
	 */
	public Optional<CameraConfigurationProperties> getAcquisitionPropertiesDocuments(String cameraId) {
		return getClientSpringContext().getClientProperties().getCameras().stream()
			.filter(c -> c.getId().equals(cameraId))
			.findFirst();
	}

	public Modes getModes() {
		return getClientSpringContext().getClientProperties().getModes();
	}
}
