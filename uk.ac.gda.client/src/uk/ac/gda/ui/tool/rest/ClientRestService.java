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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import uk.ac.diamond.daq.mapping.api.document.DocumentMapper;
import uk.ac.gda.core.tool.spring.SpringApplicationContextFacade;

/**
 * Provides basic functionalities for a rest service client
 *
 * @author Maurizio Nagni
 */
class ClientRestService {

	static Logger logger = LoggerFactory.getLogger(ClientRestService.class);

	private ClientRestService() {
	}

	private static RestTemplate restTemplate;

	/**
	 * Submit a request to a service and return only the response body
	 * @param <T> the expected response type
	 * @param url the URL
	 * @param method the HTTP method (GET, POST, etc)
	 * @param requestEntity the entity (headers and/or body) to write to the request, may be {@code null}
	 * @param responseType the type of the return value
	 * @return the response as entity
	 */
	public static <T> T returnBody(String url, HttpMethod method, HttpEntity<?> requestEntity, Class<T> responseType) {
		ResponseEntity<T> response = getRestTemplate().exchange(url, method, requestEntity, responseType);
		return response.getBody();
	}

	/**
	 * Submit a request to a service and return only the response body
	 * @param <T> the expected response type
	 * @param url the URL
	 * @param method the HTTP method (GET, POST, etc)
	 * @param requestEntity the entity (headers and/or body) to write to the request, may be {@code null}
	 * @param responseType the type of the return value
	 * @return the response as entity
	 */
	public static <T> ResponseEntity<T> submitRequest(String url, HttpMethod method, HttpEntity<?> requestEntity, Class<T> responseType) {
		return getRestTemplate().exchange(url, method, requestEntity, responseType);
	}

	private static HttpHeaders createRequestHeader() {
	    HttpHeaders requestHeaders = new HttpHeaders();
	    requestHeaders.setContentType(MediaType.APPLICATION_JSON);
	    return requestHeaders;
	}

	/**
	 * Creates an {@link HttpEntity} with a given body. The associated header set the content type to {@link MediaType#APPLICATION_JSON}
	 * @param <T> the body type
	 * @param body the message request
	 * @return the http request entity
	 */
	public static <T> HttpEntity<T> createHttpEntity(T body) {
		return new HttpEntity<>(body, createRequestHeader());
	}

	/**
	 * Append a rest path to its endpoint
	 * @param serviceEndpoint the service host URL
	 * @param restPath the service endpoint
	 * @return the service request URL
	 */
	public static String formatURL(String serviceEndpoint, String restPath) {
		return String.format("%s%s", serviceEndpoint, restPath);
	}

	private static RestTemplate getRestTemplate() {
		return Optional.ofNullable(restTemplate)
				.orElseGet(ClientRestService::createRestTemplate);
	}

	private static RestTemplate createRestTemplate() {
		restTemplate = doCreateRestTemplate();
	    addMessageConverters(restTemplate);
	    addLoggingInterceptor(restTemplate);

		return restTemplate;
	}

	private static RestTemplate doCreateRestTemplate() {
		ClientHttpRequestFactory factory;
		if (logger.isDebugEnabled()) {
		    factory = new BufferingClientHttpRequestFactory(createSimpleHttpRequestFactory());
		} else {
			factory = createSimpleHttpRequestFactory();
		}
	    return new RestTemplate(factory);
	}

	private static SimpleClientHttpRequestFactory createSimpleHttpRequestFactory() {
		SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setConnectTimeout(3000);
		requestFactory.setReadTimeout(3000);
		return requestFactory;
	}

	private static void addMessageConverters(RestTemplate restTemplate) {
		List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
		MappingJackson2HttpMessageConverter jacksonMapper = new MappingJackson2HttpMessageConverter();
		jacksonMapper.setObjectMapper(getDocumentMapper().getJacksonObjectMapper());
		messageConverters.add(new MappingJackson2HttpMessageConverter());
		restTemplate.setMessageConverters(messageConverters);
	}

	private static void addLoggingInterceptor(RestTemplate restTemplate) {
		List<ClientHttpRequestInterceptor> interceptors = restTemplate.getInterceptors();
		if (CollectionUtils.isEmpty(interceptors)) {
		    interceptors = new ArrayList<>();
		}
		interceptors.add(new LoggingInterceptor());
		restTemplate.setInterceptors(interceptors);
	}

	private static DocumentMapper getDocumentMapper() {
		return SpringApplicationContextFacade.getBean(DocumentMapper.class);
	}
}