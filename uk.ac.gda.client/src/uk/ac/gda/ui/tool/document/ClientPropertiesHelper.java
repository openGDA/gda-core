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

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningAcquisition;
import uk.ac.gda.api.acquisition.AcquisitionKeys;
import uk.ac.gda.api.acquisition.AcquisitionPropertyType;
import uk.ac.gda.api.acquisition.AcquisitionTemplateType;
import uk.ac.gda.api.acquisition.AcquisitionType;
import uk.ac.gda.client.properties.acquisition.AcquisitionConfigurationProperties;
import uk.ac.gda.client.properties.acquisition.AcquisitionTemplateConfiguration;
import uk.ac.gda.client.properties.camera.CameraConfigurationProperties;
import uk.ac.gda.client.properties.mode.Modes;
import uk.ac.gda.ui.tool.spring.ClientSpringContext;
import uk.ac.gda.ui.tool.spring.ClientSpringProperties;

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
	 * Retrieves from the client properties a specific acquisition type for the given acquisition property type
	 *
	 * @param acquisitionKey the acquisition keys
	 * @return the template configuration, otherwise {@code Optional#empty()} if the client has no properties defined for the given pair
	 *
	 * @see <a href="https://confluence.diamond.ac.uk/display/DIAD/Acquisition+Configuration+Properties">Acquisition Configuration Properties</a>
	 */
	public Optional<AcquisitionTemplateConfiguration> getAcquisitionTemplateConfiguration(AcquisitionKeys acquisitionKey) {
		return getAcquisitionTemplateConfiguration(getAcquisitionConfigurationProperties(acquisitionKey.getPropertyType()), acquisitionKey.getTemplateType());
	}

	/**
	 * Retrieves from the client properties a specific acquisition type for the given acquisition type.
	 *
	 * <p>
	 * This methods is useful when given a {@link ScanningAcquisition} document is necessary to retrieve the associated client {@code AcquisitionTemplateConfiguration}
	 * </p>
	 *
	 * @param acquisitionType the acquisition type
	 * @param templateType the acquisition type template
	 * @return the template configuration, otherwise {@code Optional#empty()} if the client has no properties defined for the given pair
	 *
	 * @see <a href="https://confluence.diamond.ac.uk/display/DIAD/Acquisition+Configuration+Properties">Acquisition Configuration Properties</a>
	 */
	public Optional<AcquisitionTemplateConfiguration> getAcquisitionTemplateConfiguration(AcquisitionType acquisitionType,
			AcquisitionTemplateType templateType) {
		return getAcquisitionTemplateConfiguration(getAcquisitionConfigurationProperties(acquisitionType), templateType);
	}

	/**
	 * Retrieves a specific acquisition type from a {@code AcquisitionConfigurationProperties} instance.
	 *
	 * <p>
	 * The {@code configurationProperties} is created by Spring loading the application properties.
	 * At the moment acquisition type/template are only partially consistent because while a
	 * Tomography.TWO_DIMENSION_POINT or Calibration.FLAT have a meaning, Tomography.FLAT  has not despite is,
	 * actually perfectly legal from the syntax point of view.
	 * Further more the properties are edited manually and this can be error prone. In sense the acquisition type templates may be absent.
	 * Even more in general this case may appear if there is some sort of ACL on who can access specific acquisition type templates.
	 * For all the cases above this methods returns an {@code Optional} result.
	 * </p>
	 *
	 * @param configurationProperties an element from {@link ClientSpringProperties#getAcquisitions()}
	 * @param templateType a specific acquisition type
	 * @return the template configuration, otherwise {@code Optional#empty()} if the client has no properties defined for the given pair
	 */
	private Optional<AcquisitionTemplateConfiguration> getAcquisitionTemplateConfiguration(Optional<AcquisitionConfigurationProperties> configurationProperties,
			AcquisitionTemplateType templateType) {
		var templates = configurationProperties
				.map(AcquisitionConfigurationProperties::getTemplates)
				.orElseGet(Collections::emptyList);

		return templates.stream()
				.filter(a -> templateType.equals(a.getTemplate()))
				.findFirst();
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
