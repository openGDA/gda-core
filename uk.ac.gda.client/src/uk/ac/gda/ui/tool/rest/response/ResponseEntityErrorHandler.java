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

package uk.ac.gda.ui.tool.rest.response;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.client.HttpMessageConverterExtractor;
import org.springframework.web.client.ResponseErrorHandler;

import uk.ac.gda.client.UIHelper;
import uk.ac.gda.core.tool.GDAHttpException;
import uk.ac.gda.ui.tool.ClientMessages;

/**
 * Handles the error responses from the service endpoints
 *
 * @author Maurizio Nagni
 */
public class ResponseEntityErrorHandler implements ResponseErrorHandler {

	private static final Logger logger = LoggerFactory.getLogger(ResponseEntityErrorHandler.class);

	private final List<HttpMessageConverter<?>> messageConverters;

	public ResponseEntityErrorHandler(List<HttpMessageConverter<?>> messageConverters) {
		this.messageConverters = messageConverters;
	}

	@Override
	public boolean hasError(ClientHttpResponse httpResponse) throws IOException {
		return hasError(httpResponse.getStatusCode());
	}

	protected boolean hasError(HttpStatus statusCode) {
		return (statusCode.is4xxClientError() || statusCode.is5xxServerError());
	}

	@Override
	public void handleError(ClientHttpResponse httpResponse) throws IOException {
		HttpMessageConverterExtractor<GDAHttpException> errorMessageExtractor = new HttpMessageConverterExtractor<>(GDAHttpException.class, messageConverters);
		var errorResponse = errorMessageExtractor.extractData(httpResponse);

		if (httpResponse.getStatusCode().series() == HttpStatus.Series.SERVER_ERROR) {
			UIHelper.showError(ClientMessages.SERVICE_ERROR, errorResponse.getMessage(), logger);
		}

		if (httpResponse.getStatusCode().series() == HttpStatus.Series.CLIENT_ERROR) {
			UIHelper.showError(ClientMessages.CLIENT_ERROR, errorResponse.getMessage(), logger);
		}
		throw new IOException(errorResponse.getMessage());
	}
}
