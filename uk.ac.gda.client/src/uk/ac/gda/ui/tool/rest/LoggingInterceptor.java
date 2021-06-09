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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

/**
 * Intercepts http communication in order
 * <ol>
 * <li>
 * Logs the request body, if the logger is in debug mode.
 * </li>
 * <li>
 * Submits the request to the endpoint.
 * </li>
 * <li>
 * Logs the response body, If the logger is in debug mode.
 * </li>
 * </ol>

 *
 * @author Maurizio Nagni
 */
public class LoggingInterceptor implements ClientHttpRequestInterceptor {

	static Logger logger = LoggerFactory.getLogger(LoggingInterceptor.class);

	@Override
	public ClientHttpResponse intercept(HttpRequest req, byte[] reqBody, ClientHttpRequestExecution ex)
			throws IOException {
		// logs the request
		if (logger.isDebugEnabled()) {
			logger.debug("Request body: {}", new String(reqBody, StandardCharsets.UTF_8));
		}

		ClientHttpResponse response = ex.execute(req, reqBody);
		// logs the response
		// if the status code >= 400 getBody() throws a IOException
		// if the staus code >= 400 the response is returned so to be handled by ResponseEntityErrorHandler
		if (logger.isDebugEnabled() && !isError(response.getStatusCode())) {
			var isr = new InputStreamReader(response.getBody(), StandardCharsets.UTF_8);
			String body = new BufferedReader(isr).lines().collect(Collectors.joining("\n"));
			logger.debug("Response - Status: {}, Body: {}", response.getStatusCode(), body);
		}
		return response;
	}

	private boolean isError(HttpStatus status) {
		return status.is4xxClientError() || status.is5xxServerError();
	}
}
