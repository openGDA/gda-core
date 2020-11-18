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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import uk.ac.diamond.daq.experiment.api.structure.ExperimentController;
import uk.ac.diamond.daq.experiment.api.structure.ExperimentControllerException;

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
public class ExperimentControllerService implements ExperimentController {

	@Value("${client.experiment.service.endpoint:http://127.0.0.1:8888/experiment}")
	protected String serviceEndpoint;

	private String getServiceEndpoint() {
		return serviceEndpoint;
	}

	@Override
	public URL startExperiment(String experimentName) throws ExperimentControllerException {
		String restPath = String.format("/start/%s", experimentName);
		String url = formatURL(getServiceEndpoint(), restPath);
		return returnBody(url, HttpMethod.PUT, null, URL.class);
	}

	@Override
	public String getExperimentName() {
		String restPath = "/name";
		String url = formatURL(getServiceEndpoint(), restPath);
		return returnBody(url, HttpMethod.GET, null, String.class);
	}

	@Override
	public void stopExperiment() throws ExperimentControllerException {
		String restPath = "/stop";
		String url = formatURL(getServiceEndpoint(), restPath);
		returnBody(url, HttpMethod.POST, null, Void.class);
	}

	@Override
	public boolean isExperimentInProgress() {
		String restPath = "/inProgress";
		String url = formatURL(getServiceEndpoint(), restPath);
		return returnBody(url, HttpMethod.GET, null, Boolean.class);
	}

	@Override
	public URL prepareAcquisition(String acquisitionName) throws ExperimentControllerException {
		String restPath = String.format("/prepareAcquisition/%s", acquisitionName);
		String url = formatURL(getServiceEndpoint(), restPath);
		return returnBody(url, HttpMethod.PUT, null, URL.class);
	}

	@Override
	public URL startMultipartAcquisition(String acquisitionName) throws ExperimentControllerException {
		String restPath = String.format("/startMultipartAcquisition/%s", acquisitionName);
		String url = formatURL(getServiceEndpoint(), restPath);
		return returnBody(url, HttpMethod.PUT, null, URL.class);
	}

	@Override
	public void stopMultipartAcquisition() throws ExperimentControllerException {
		String restPath = "/stopMultipartAcquisition";
		String url = formatURL(getServiceEndpoint(), restPath);
		returnBody(url, HttpMethod.POST, null, Void.class);
	}
}