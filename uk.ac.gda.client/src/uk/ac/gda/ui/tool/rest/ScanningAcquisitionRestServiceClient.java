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

package uk.ac.gda.ui.tool.rest;

import static uk.ac.gda.ui.tool.rest.ClientRestService.createHttpEntity;
import static uk.ac.gda.ui.tool.rest.ClientRestService.formatURL;
import static uk.ac.gda.ui.tool.rest.ClientRestService.submitRequest;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import gda.configuration.properties.LocalProperties;
import uk.ac.diamond.daq.mapping.api.document.base.AcquisitionBase;
import uk.ac.diamond.daq.mapping.api.document.base.AcquisitionConfigurationBase;
import uk.ac.diamond.daq.mapping.api.document.base.AcquisitionParametersBase;
import uk.ac.diamond.daq.mapping.api.document.exception.ScanningAcquisitionServiceException;
import uk.ac.gda.api.acquisition.response.RunAcquisitionResponse;

/**
 * Provides to the GDA client access to the ScanningAcquisition rest service
 *
 * <p>
 * <i>client.acquisition.service.endpoint</i> is the property which configures the URL endpoint to the service. The
 * default value is {@code http://127.0.0.1:8888/acquisition}
 * </p>
 *
 * @author Maurizio Nagni
 */
@Service
public class ScanningAcquisitionRestServiceClient {

	private String getServiceEndpoint() {
		return LocalProperties.get("client.acquisition.service.endpoint", "http://127.0.0.1:8888/acquisition");
	}

	/**
	 * Starts a new named experiment
	 *
	 * @param acquisition
	 *            the scanning acquisition to process
	 *
	 * @throws ScanningAcquisitionServiceException
	 *             if methods fails to submit the acquisition request
	 */
	public ResponseEntity<RunAcquisitionResponse> run(AcquisitionBase<? extends AcquisitionConfigurationBase<? extends AcquisitionParametersBase>> acquisition) throws ScanningAcquisitionServiceException {
		String restPath = "/run";
		String url = formatURL(getServiceEndpoint(), restPath);
		ResponseEntity<RunAcquisitionResponse> response = submitRequest(url, HttpMethod.POST, createHttpEntity(acquisition), RunAcquisitionResponse.class);
		return response;
	}
}