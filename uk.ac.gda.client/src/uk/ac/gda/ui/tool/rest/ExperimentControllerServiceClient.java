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

import static uk.ac.gda.ui.tool.rest.ClientRestService.formatURL;
import static uk.ac.gda.ui.tool.rest.ClientRestService.submitRequest;

import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import uk.ac.diamond.daq.experiment.api.entity.ExperimentErrorCode;
import uk.ac.diamond.daq.experiment.api.entity.ExperimentServiceResponse;
import uk.ac.diamond.daq.experiment.api.structure.ExperimentController;
import uk.ac.gda.client.exception.GDAClientRestException;
import uk.ac.gda.ui.tool.spring.ClientSpringContext;

/**
 * Provides to the GDA client access a {@link ExperimentController} service
 *
 * <p>
 * <i>client.experiment.service.endpoint</i> is the property which configures the URL endpoint to the service. The
 * default value is {@code http://127.0.0.1:8888/experiment}
 * </p>
 *
 * @author Maurizio Nagni
 */
@Service
public class ExperimentControllerServiceClient {

	private static final Logger logger = LoggerFactory.getLogger(ExperimentControllerServiceClient.class);

	@Autowired
	private ClientSpringContext clientContext;

	private String getServiceEndpoint() {
		return formatURL(clientContext.getRestServiceEndpoint(), "/experiment");
	}

	/**
	 * Starts a new named experiment
	 *
	 * @param experimentName
	 *            A user-friendly identifier for the experiment
	 *
	 * @return the experiment file URL; created at the end of the experiment ({@link #stopExperiment()}
	 *
//	 * @throws ExperimentControllerException
//	 *             if methods fails to create the experiment location
	 */
	public URL startExperiment(String experimentName) throws GDAClientRestException {
		var restPath = String.format("/session/start/%s", experimentName);
		String url = formatURL(getServiceEndpoint(), restPath);
		ResponseEntity<ExperimentServiceResponse> response;
		response = submitRequest(url, HttpMethod.PUT, null, new ParameterizedTypeReference<ExperimentServiceResponse>() {});
		handleExperimentErrorCode(response.getBody().getErrorCode());
		return response.getBody().getRootNode();
	}

	private void handleExperimentErrorCode(ExperimentErrorCode errorCode) throws GDAClientRestException {
		if (errorCode == null)
			return;

		switch (errorCode) {
			case ACQUISITION_EXISTS:
				throw new GDAClientRestException("An acquisition with the same name already exists");
			case EXPERIMENT_EXISTS:
				throw new GDAClientRestException("An experiment with the same name already exists");
			case CANNOT_CREATE_EXPERIMENT:
				throw new GDAClientRestException("Cannot create the experiment structure");
			case CANNOT_CREATE_ACQUISITION:
				throw new GDAClientRestException("Cannot create the acquisition structure");
			case NONE:
				break;
			default:
				break;
		}
	}

	/**
	 * Returns the experiment name, or {@code null} if no experiment is running
	 */
	public String getExperimentName() {
		String url = formatURL(getServiceEndpoint(), "/session/name");
		ResponseEntity<String> response;
		try {
			response = submitRequest(url, HttpMethod.GET, null, new ParameterizedTypeReference<String>() {});
		} catch (GDAClientRestException e) {
			return null;
		}
		return response.getBody();
	}

	/**
	 * Closes the active experiment. Closes also any open multipart acquisition.
	 *
//	 * @throws ExperimentControllerException
//	 *             if methods fails or {@link #isExperimentInProgress()} returns {@code false}
	 */
	public void stopExperiment() throws GDAClientRestException {
		String url = formatURL(getServiceEndpoint(), "/session/stop");
		submitRequest(url, HttpMethod.POST, null, new ParameterizedTypeReference<Void>() {});
	}

	/**
	 * Returns the state of the experiment
	 *
	 * @return {@code true} if the experiment is in progress, otherwise {@code false}
	 */
	public boolean isExperimentInProgress() {
		String url = formatURL(getServiceEndpoint(), "/session/inProgress");
		try {
			return submitRequest(url, HttpMethod.GET, null, new ParameterizedTypeReference<Boolean>() {}).getBody();
		} catch (GDAClientRestException e) {
			logger.warn("Cannot verify the experiment status", e);
		}
		return false;
	}

	/**
	 * Creates a new location within the experiment structure.
	 *
	 * @param acquisitionName
	 *            A user-friendly identifier for the acquisition
	 *
	 * @return The URL for the acquisition file
	 *
//	 * @throws ExperimentControllerException
//	 *             if methods fails to create the acquisition location
	 */
	public URL prepareAcquisition(String acquisitionName) throws GDAClientRestException {
		var restPath = String.format("/session/prepareAcquisition/%s", acquisitionName);
		String url = formatURL(getServiceEndpoint(), restPath);
		ResponseEntity<ExperimentServiceResponse> response;
		response = submitRequest(url, HttpMethod.PUT, null, new ParameterizedTypeReference<ExperimentServiceResponse>() {});
		handleExperimentErrorCode(response.getBody().getErrorCode());
		return response.getBody().getRootNode();
	}

	/**
	 * Prepares the controller for an acquisition composed of multiple parts. Each part should then be given a URL by
	 * calling {@link #prepareAcquisition(String)}, and the overall acquisition should be ended with
	 * {@link #stopMultipartAcquisition()}
	 *
	 * @param acquisitionName
	 *            A user-friendly identifier for the acquisition
	 *
	 * @return the URL of the acquisition file, created when the multipart acquisition is stopped
	 *
//	 * @throws ExperimentControllerException
	 */
	public URL startMultipartAcquisition(String acquisitionName) throws GDAClientRestException {
		var restPath = String.format("/session/startMultipartAcquisition/%s", acquisitionName);
		String url = formatURL(getServiceEndpoint(), restPath);
		ResponseEntity<ExperimentServiceResponse> response;
		response = submitRequest(url, HttpMethod.PUT, null, new ParameterizedTypeReference<ExperimentServiceResponse>() {});
		handleExperimentErrorCode(response.getBody().getErrorCode());
		return response.getBody().getRootNode();
	}

	/**
	 * Closes the current multipart acquisition is complete.
	 *
//	 * @throws ExperimentControllerException
//	 *             if no open multipart acquisition exists
	 */
	public void stopMultipartAcquisition() throws GDAClientRestException {
		String url = formatURL(getServiceEndpoint(), "/session/stopMultipartAcquisition");
		submitRequest(url, HttpMethod.POST, null, new ParameterizedTypeReference<Void>() {});
	}
}
