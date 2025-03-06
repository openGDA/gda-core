/*-
 * Copyright © 2025 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.bluesky.client;

import static java.util.Collections.emptyMap;
import static org.apache.http.entity.ContentType.APPLICATION_JSON;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import uk.ac.diamond.daq.bluesky.api.BlueApiAuth;
import uk.ac.diamond.daq.bluesky.api.model.Device;
import uk.ac.diamond.daq.bluesky.api.model.Environment;
import uk.ac.diamond.daq.bluesky.api.model.Plan;
import uk.ac.diamond.daq.bluesky.api.model.PythonEnvironment;
import uk.ac.diamond.daq.bluesky.api.model.Task;
import uk.ac.diamond.daq.bluesky.api.model.TrackableTask;
import uk.ac.diamond.daq.bluesky.api.model.WorkerState;
import uk.ac.diamond.daq.bluesky.client.error.ApiException;
import uk.ac.diamond.daq.bluesky.client.error.ConnectionError;
import uk.ac.diamond.daq.bluesky.client.error.HttpValidationError;
import uk.ac.diamond.daq.bluesky.client.error.InvalidQuery;
import uk.ac.diamond.daq.bluesky.client.error.InvalidRequest;
import uk.ac.diamond.daq.bluesky.client.error.InvalidResponse;
import uk.ac.diamond.daq.bluesky.client.error.MissingResponse;
import uk.ac.diamond.daq.bluesky.client.error.ServerError;
import uk.ac.diamond.daq.bluesky.client.error.Unauthorised;
import uk.ac.diamond.daq.bluesky.client.error.UnexpectedResponse;

public class ApiClient implements BlueApiAuth {

	private static final Logger logger = LoggerFactory.getLogger(ApiClient.class);

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	/** Wrapper type around list of devices returned from blueapi */
	record Devices(List<Device> devices) {}

	/** Wrapper type around list of plans returned from blueapi */
	record Plans(List<Plan> plans) {}

	/** Wrapper type around list of plans returned from blueapi */
	record Tasks(List<TrackableTask> tasks) {}

	/** Wrapper around task id */
	record TaskId(@JsonProperty("task_id") String id) {}

	record WorkerStateChange(
			boolean defer,
			@JsonProperty("new_state") WorkerState state,
			Optional<String> reason
	) {
		public WorkerStateChange(WorkerState state) {
			this(false, state, null);
		}
	}

	private BlueApiAuthManager auth;
	private HttpClient client;
	private URI base;

	public ApiClient(String scheme, String host, int port) throws URISyntaxException {
		base = new URIBuilder()
				.setScheme(scheme)
				.setHost(host)
				.setPort(port)
				.build();
		client = HttpClientBuilder.create().build();
		configureAuth();
	}

	/** Initialise the authorisation handler using oidc configuration from server */
	private void configureAuth() {
		try {
			var authConfig = getOidcConfig();
			auth = authConfig.map(oidc -> {
				try {
					var baam = new BlueApiAuthManager(oidc);
					logger.info("Using blueAPI authentication");
					return baam;
				} catch (IOException e) {
					logger.error("Error configuring authentication", e);
					return null;
				}
			}).orElse(null);
		} catch (ApiException e) {
			logger.error("Error connecting to blueapi for oidc config", e);
		}
	}

	/** Build a URI from the configured base, the given endpoint and the keys/values in the query */
	private URI uri(String endpoint, Map<String, String> query) throws InvalidQuery {
		var builder = new URIBuilder(base).setPath(endpoint);
		for (var pair: query.entrySet()) {
			builder.addParameter(pair.getKey(), pair.getValue());
		}
		try {
			return builder.build();
		} catch (URISyntaxException e) {
			// Pretty sure this shouldn't be possible if we've gone via the builder
			throw new InvalidQuery(endpoint, query, e);
		}
	}

	/** Build a text entity containing the JSON serialisation of the given value */
	private HttpEntity entity(Object value) throws InvalidQuery {
		try {
			return new StringEntity(OBJECT_MAPPER.writeValueAsString(value), APPLICATION_JSON);
		} catch (Exception e) {
			throw new InvalidQuery(value, e);
		}
	}

	/**
	 * Make a get request to the given endpoint, deserialising the response to the given type.
	 *
	 * Will raise an exception if nothing is returned.
	 */
	private <T> T get(String endpoint, Class<T> t) throws ApiException {
		return getOptional(endpoint, emptyMap(), t)
				.orElseThrow(MissingResponse::new);
	}

	/**
	 * Make a GET request to the given endpoint with the given query parameters, deserialising
	 * the response to the given type.
	 *
	 * Will not raise an exception if nothing is returned (204 NO CONTENT).
	 */
	private <T> Optional<T> getOptional(String endpoint, Map<String, String> query, Class<T> t) throws ApiException {
		return call(new HttpGet(uri(endpoint, query)), t);
	}

	/** Make a PUT request sending the given value as JSON, deserialising the response to the given type */
	private <T> T put(String endpoint, Object value, Class<T> response) throws ApiException {
		var put = new HttpPut(uri(endpoint, emptyMap()));
		put.setEntity(entity(value));
		return call(put, response).orElseThrow(MissingResponse::new);
	}

	/** Make a POST request sending the given value as JSON, deserialising the response to the given type */
	private <T> T post(String endpoint, Object value, Class<T> response) throws ApiException {
		var put = new HttpPost(uri(endpoint, emptyMap()));
		put.setEntity(entity(value));
		return call(put, response).orElseThrow(MissingResponse::new);
	}


	/** Make a DELETE request, deserialising the response to the given type */
	private <T> T delete(String endpoint, Class<T> t) throws ApiException {
		return call(new HttpDelete(uri(endpoint, emptyMap())), t)
				.orElseThrow(MissingResponse::new);
	}

	private void addAuthHeader(HttpUriRequest req) {
		if (auth != null) {
			auth.getToken()
					.ifPresent(token -> req.setHeader(new BasicHeader("Authorization", "Bearer " + token)));
		} else {
			logger.debug("No authorisation configured/available");
		}
	}

	/** Execute the given request and handle the response */
	private <T> Optional<T> call(HttpUriRequest req, Class<T> expectedResponse) throws ApiException {
		logger.trace("Calling {}, expecting {}", req, expectedResponse);
		StatusLine status;
		String content = null;
		try {
			addAuthHeader(req);
			var response = client.execute(req);
			status = response.getStatusLine();
			var entity = response.getEntity();
			if (entity != null) {
				try (var cnt = entity.getContent()) {
					content = new String(cnt.readAllBytes(), StandardCharsets.UTF_8);
				}
			}
		} catch (IOException ioe) {
			throw new ConnectionError(ioe);
		}

		int statusCode = status.getStatusCode();
		logger.debug("Response status: {}", status);
		logger.trace("Content from response: {}", content);
		if (statusCode >= 500) {
			// Server error
			throw new ServerError(status, content);
		} else if (statusCode >= 400) {
			// User error
			if (statusCode == HttpStatus.SC_UNPROCESSABLE_ENTITY) {
				var error = read(content, HttpValidationError.class);
				// remove json parsing frames from stack
				throw error.withResetStackTrace();
			} else if (statusCode == HttpStatus.SC_UNAUTHORIZED) {
				throw new Unauthorised();
			}
			throw new InvalidRequest(statusCode, content);
		} else if (statusCode >= 300) {
			// Redirect
			throw new UnexpectedResponse(status, content);
		} else if (statusCode >= 200) {
			// Success
			if (statusCode == HttpStatus.SC_NO_CONTENT) {
				if (content != null) {
					logger.warn("'204 No Content' returned: {}", content);
				}
				return Optional.empty();
			}
			return Optional.of(read(content, expectedResponse));
		} else {
			// Info
			throw new UnexpectedResponse(status, content);
		}
	}

	/** Deserialise the given content into the required type, wrapping any errors. */
	private <T> T read(String content, Class<T> type) throws ApiException {
		try {
			logger.trace("Reading {} from {}", type, content);
			return OBJECT_MAPPER.readValue(content, type);
		} catch (JsonProcessingException e) {
			throw new InvalidResponse(content, type, e);
		}
	}

	/** GET /config/oidc */
	public Optional<BlueApiAuthConfig> getOidcConfig() throws ApiException {
		return getOptional("/config/oidc", emptyMap(), BlueApiAuthConfig.class);
	}

	/** GET /devices */
	public List<Device> getDevices() throws ApiException {
		return get("/devices", Devices.class).devices;
	}

	/** GET /devices/{name} */
	public Device getDevice(String name) throws ApiException {
		return get("/devices/" + name, Device.class);
	}

	/** GET /environment */
	public Environment getEnvironment() throws ApiException {
		return get("/environment", Environment.class);
	}

	/** DELETE /environment */
	public Environment deleteEnvironment() throws ApiException {
		return delete("/environment", Environment.class);
	}

	/** GET /plans */
	public List<Plan> getPlans() throws ApiException {
		return get("/plans", Plans.class).plans;
	}

	/** GET /plans/{name} */
	public Plan getPlan(String name) throws ApiException {
		return get("/plans/" + name, Plan.class);
	}

	/** GET /python_environment */
	public PythonEnvironment getPythonEnvironment() throws ApiException {
		return get("/python_environment", PythonEnvironment.class);
	}

	/** POST /tasks */
	public String submitTask(Task task) throws ApiException {
		return post("/tasks", task, TaskId.class)
				.id();
	}

	/** GET /tasks */
	public List<TrackableTask> getTasks() throws ApiException {
		return getTasks(Optional.empty());
	}

	/** GET /tasks */
	public List<TrackableTask> getTasks(Optional<String> status) throws ApiException {
		var query = status
				.map(st -> Map.of("task_status", st))
				.orElse(emptyMap());
		return getOptional("/tasks", query, Tasks.class)
				.orElseThrow(MissingResponse::new)
				.tasks;
	}

	/** GET /tasks/{id} */
	public TrackableTask getTask(String id) throws ApiException {
		return get("/tasks/" + id, TrackableTask.class);
	}

	/** DELETE /tasks/{id} */
	public TrackableTask deleteTask(String id) throws ApiException {
		return delete("/tasks/" + id, TrackableTask.class);
	}

	/** GET /worker/state */
	public WorkerState getWorkerState() throws ApiException {
		return get("/worker/state", WorkerState.class);
	}

	/** PUT /worker/state */
	public WorkerState setWorkerState(WorkerState state) throws ApiException {
		return put("/worker/state", new WorkerStateChange(state), WorkerState.class);
	}

	/** PUT /worker/state */
	public WorkerState setWorkerState(WorkerState state, boolean defer, String message) throws ApiException {
		return put("/worker/state", new WorkerStateChange(defer, state, Optional.ofNullable(message)), WorkerState.class);
	}

	/** GET /worker/task */
	public String getWorkerTask() throws ApiException {
		return get("/worker/task", TaskId.class).id();
	}

	/** PUT /worker/task */
	public String setWorkerTask(String taskId) throws ApiException {
		return put("/worker/task", new TaskId(taskId), TaskId.class).id();
	}

	@Override
	public boolean authEnabled() {
		return auth != null;
	}

	@Override
	public AuthDetails initLogin(int gdaClient) {
		if (auth == null) {
			return null;
		}
		return auth.initLogin(gdaClient);
	}

	@Override
	public void logout(int gdaClient) {
		auth.logout(gdaClient);
	}

	@Override
	public String loggedInUser(int clientID) {
		if (auth == null) return null;
		return auth.loggedInUser(clientID);
	}

	@Override
	public void cancelLogin(int clientID, UUID session) {
		if (auth != null) {
			auth.cancelLogin(clientID, session);
		}
	}
}
