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

package uk.ac.gda.ui.tool.spring;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gda.configuration.properties.LocalProperties;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningAcquisition;
import uk.ac.gda.core.tool.spring.SpringApplicationContextFacade;
import uk.ac.gda.ui.tool.controller.AcquisitionController;

/**
 * Defines the context for a spring/rest enabled client.
 *
 * <p>
 * The context, in particular, is related to the active client perspective
 * </p>
 *
 * @author Maurizio Nagni
 */
@Component
public class ClientSpringContext {

	public static final String REST_ENDPOINT = "client.rest.gda.service.endpoint";
	public static final String REST_ENDPOINT_DEFAULT = "http://127.0.0.1:8888";

	private AcquisitionController<ScanningAcquisition> acquisitionController;

	@Autowired
	private ClientSpringProperties clientProperties;

	/**
	 * Returns the GDA rest service endpoint
	 *
	 * @return the service location
	 */
	public final String getRestServiceEndpoint() {
		return LocalProperties.get(REST_ENDPOINT, REST_ENDPOINT_DEFAULT);
	}

	public Optional<AcquisitionController<ScanningAcquisition>> getAcquisitionController() {
		if (acquisitionController == null) {
			this.acquisitionController = SpringApplicationContextFacade.getBean("experimentAcquisitionController", AcquisitionController.class);
		}
		return Optional.ofNullable(acquisitionController);
	}

	/**
	 * Returns the client properties using spring.
	 *
	 * @return the client properties
	 */
	public ClientSpringProperties getClientProperties() {
		return clientProperties;
	}
}
