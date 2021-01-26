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
import static uk.ac.gda.ui.tool.rest.ClientRestService.returnBody;

import java.net.URL;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import uk.ac.diamond.daq.experiment.api.entity.ExperimentErrorCode;
import uk.ac.diamond.daq.experiment.api.entity.ExperimentServiceResponse;
import uk.ac.diamond.daq.experiment.api.structure.ExperimentController;
import uk.ac.diamond.daq.experiment.api.structure.ExperimentControllerException;
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
public class ExperimentControllerServiceClient implements ExperimentController {

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
	 * @throws ExperimentControllerException
	 *             if methods fails to create the experiment location
	 */
	@Override
	public URL startExperiment(String experimentName) throws ExperimentControllerException {
		String restPath = String.format("/start/%s", experimentName);
		String url = formatURL(getServiceEndpoint(), restPath);
		ExperimentServiceResponse response = returnBody(url, HttpMethod.PUT, null, ExperimentServiceResponse.class);
		handleExperimentErrorCode(response.getErrorCode());
		return response.getRootNode();
	}

	private void handleExperimentErrorCode(ExperimentErrorCode errorCode) throws ExperimentControllerException {
		if (errorCode == null)
			return;

		switch (errorCode) {
			case ACQUISITION_EXISTS:
				throw new ExperimentControllerException("An acquisition with the same name already exists");
			case EXPERIMENT_EXISTS:
				throw new ExperimentControllerException("An experiment with the same name already exists");
			case CANNOT_CREATE_EXPERIMENT:
				throw new ExperimentControllerException("Cannot create the experiment structure");
			case CANNOT_CREATE_ACQUISITION:
				throw new ExperimentControllerException("Cannot create the acquisition structure");
			case NONE:
				break;
			default:
				break;
		}
	}

	/**
	 * Returns the experiment name, or {@code null} if no experiment is running
	 */
	@Override
	public String getExperimentName() {
		String url = formatURL(getServiceEndpoint(), "/name");
		return returnBody(url, HttpMethod.GET, null, String.class);
	}

	/**
	 * Closes the active experiment. Closes also any open multipart acquisition.
	 *
	 * @throws ExperimentControllerException
	 *             if methods fails or {@link #isExperimentInProgress()} returns {@code false}
	 */
	@Override
	public void stopExperiment() throws ExperimentControllerException {
		String url = formatURL(getServiceEndpoint(), "/stop");
		returnBody(url, HttpMethod.POST, null, Void.class);
	}

	/**
	 * Returns the state of the experiment
	 *
	 * @return {@code true} if the experiment is in progress, otherwise {@code false}
	 */
	@Override
	public boolean isExperimentInProgress() {
		String url = formatURL(getServiceEndpoint(), "/inProgress");
		return returnBody(url, HttpMethod.GET, null, Boolean.class);
	}

	/**
	 * Creates a new location within the experiment structure.
	 *
	 * @param acquisitionName
	 *            A user-friendly identifier for the acquisition
	 *
	 * @return The URL for the acquisition file
	 *
	 * @throws ExperimentControllerException
	 *             if methods fails to create the acquisition location
	 */
	@Override
	public URL prepareAcquisition(String acquisitionName) throws ExperimentControllerException {
		String restPath = String.format("/prepareAcquisition/%s", acquisitionName);
		String url = formatURL(getServiceEndpoint(), restPath);
		ExperimentServiceResponse response = returnBody(url, HttpMethod.PUT, null, ExperimentServiceResponse.class);
		handleExperimentErrorCode(response.getErrorCode());
		return response.getRootNode();
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
	 * @throws ExperimentControllerException
	 */
	@Override
	public URL startMultipartAcquisition(String acquisitionName) throws ExperimentControllerException {
		String restPath = String.format("/startMultipartAcquisition/%s", acquisitionName);
		String url = formatURL(getServiceEndpoint(), restPath);
		ExperimentServiceResponse response = returnBody(url, HttpMethod.PUT, null, ExperimentServiceResponse.class);
		handleExperimentErrorCode(response.getErrorCode());
		return response.getRootNode();
	}

	/**
	 * Closes the current multipart acquisition is complete.
	 *
	 * @throws ExperimentControllerException
	 *             if no open multipart acquisition exists
	 */
	@Override
	public void stopMultipartAcquisition() throws ExperimentControllerException {
		String url = formatURL(getServiceEndpoint(), "/stopMultipartAcquisition");
		returnBody(url, HttpMethod.POST, null, Void.class);
	}
}
