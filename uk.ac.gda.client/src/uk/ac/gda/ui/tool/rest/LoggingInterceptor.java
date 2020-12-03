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
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

/**
 * Intercepts http communication in order to log their content. Is active only when {@link Logger#isDebugEnabled()} is {@code true}
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
		if (logger.isDebugEnabled()) {
			InputStreamReader isr = new InputStreamReader(response.getBody(), StandardCharsets.UTF_8);
			String body = new BufferedReader(isr).lines().collect(Collectors.joining("\n"));
			logger.debug("Response body: {}", body);
		}
		return response;
	}

}
