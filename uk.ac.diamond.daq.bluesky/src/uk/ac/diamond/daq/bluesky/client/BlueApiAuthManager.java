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

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toSet;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.http.NameValuePair;
import org.apache.http.client.fluent.Request;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.auth0.jwt.JWT;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import gda.factory.ConfigurableAware;
import gda.jython.InterfaceProvider;
import gda.jython.JythonServer.JythonServerThread;
import gda.jython.JythonServerFacade;
import uk.ac.diamond.daq.bluesky.api.BlueApiAuth;
import uk.ac.diamond.daq.bluesky.api.BlueApiEvent;
import uk.ac.diamond.daq.msgbus.MsgBus;

class BlueApiAuthManager implements BlueApiAuth, ConfigurableAware {

	private static final String DEVICE_CODE = "urn:ietf:params:oauth:grant-type:device_code";
	private static final String REFRESH_TOKEN = "refresh_token";

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	private static final Logger logger = LoggerFactory.getLogger(BlueApiAuthManager.class);

	/** Constant form field identifying the auth flow being used for initial authentication */
	private static final NameValuePair DEVICE_GRANT = new BasicNameValuePair("grant_type", DEVICE_CODE);
	/** Constant form field identifying the auth flow being used when refreshing tokens */
	private static final NameValuePair REFRESH_GRANT = new BasicNameValuePair("grant_type", REFRESH_TOKEN);

	/** The OIDC configuration provided by the 'well-known' endpoint */
	@JsonIgnoreProperties(ignoreUnknown = true)
	private static record OidcConfig(@JsonProperty("device_authorization_endpoint")
		String deviceAuthorizationEndpoint,
		@JsonProperty("token_endpoint")
		String tokenEndpoint,
		@JsonProperty("revocation_endpoint")
		String revokeEndpoint
	) {}

	/**
	 * Data returned from the initial step in the device auth flow - provides the address and code the user
	 * needs to authenticate
	 */
	private static record DeviceFlowInfo(
		@JsonProperty("device_code")
		String deviceCode,
		@JsonProperty("user_code")
		String userCode,
		@JsonProperty("verification_uri")
		String verificationUri,
		@JsonProperty("verification_uri_complete")
		String verificationUriComplete,
		@JsonProperty("expires_in")
		int expires,
		int interval
	) {
		public NameValuePair deviceCodePair() {
			return new BasicNameValuePair("device_code", deviceCode);
		}
	}

	/**
	 * Data returned from the auth server after the user has logged in and authorised the application
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	private static record TokenInfo(
		@JsonProperty("access_token")
		String accessToken,
		@JsonProperty("expires_in")
		int expires,
		@JsonProperty("refresh_token")
		String refreshToken,
		@JsonProperty("refresh_expires_in")
		int refreshExpires
	) {}

	/**
	 * Error returned from the auth server when querying for tokens before the user has authorised
	 * the application.
	 */
	private static record PollError(
			String error,
			@JsonProperty("error_description")
			String description
	) {}

	/** Username/refresh token pair for an authenticated used */
	private record LoginDetails(String user, String refreshToken) {

		public NameValuePair refreshPair() {
			return new BasicNameValuePair(REFRESH_TOKEN, refreshToken);
		}}

	/** Active access token */
	private record ActiveToken(int client, String token, Instant expiry) {
		private boolean isAlive() {
			return Instant.now().isBefore(expiry);
		}
	}

	/** Form field identifying the client */
	private NameValuePair clientId = new BasicNameValuePair("client_id", "blueapi-cli");
	/** Constant form field identifying the required scopes */
	private final NameValuePair scope = new BasicNameValuePair("scope", "openid offline_access");

	/** Information from the the well-known endpoint used for device/token endpoints */
	private OidcConfig conf;

	/** Lock to prevent concurrent modification of user logins */
	private final ReentrantReadWriteLock tokenLock = new ReentrantReadWriteLock();

	/** Map of authorised clients and the associated user/token details */
	private final Map<Integer, LoginDetails> userLogins = new HashMap<>();

	private final Map<UUID, Thread> loginSessions = new ConcurrentHashMap<>();

	/** The access token for the current thread - saves regenerating access tokens for every request */
	private ThreadLocal<ActiveToken> accessToken = new InheritableThreadLocal<>();

	public BlueApiAuthManager(BlueApiAuthConfig config) throws IOException {
		logger.info("Creating new auth handler");
		var response = Request.Get(config.wellKnownUrl())
				.execute();
		var content = response.returnContent().asStream();
		conf = OBJECT_MAPPER.readValue(content, OidcConfig.class);
	}

	@Override
	public void postConfigure() {
		InterfaceProvider.getBatonStateProvider().addBatonChangedObserver(this::batonChanged);
	}

	/** Refresh token information whenever anything changes with batons - ensures old tokens are removed */
	private void batonChanged(Object src, @SuppressWarnings("unused") Object evt) {
		if (src instanceof JythonServerFacade jsf) {
			// This is server side so 'other clients' is all non-servers
			Set<Integer> clients = Stream.of(jsf.getOtherClientInformation()).map(cd -> cd.getIndex()).collect(toSet());
			updateTokens(tokens -> tokens.keySet().removeIf(k -> !clients.contains(k)));
		}
	}

	@Override
	public boolean authEnabled() {
		return conf != null;
	}

	@Override
	public AuthDetails initLogin(int gdaClient) {
		if (conf == null) {
			logger.error("Auth manager has not been configured");
			throw new IllegalStateException("Authentication has not been configured");
		}
		logger.debug("Logging in client: {}", gdaClient);
		var session = UUID.randomUUID();
		try {
			var req = Request.Post(conf.deviceAuthorizationEndpoint())
					.bodyForm(clientId, scope)
					.execute();
			var flow = OBJECT_MAPPER.readValue(req.returnContent().asStream(), DeviceFlowInfo.class);

			Thread.startVirtualThread(() -> {
				try {
					loginSessions.put(session, Thread.currentThread());
					login(gdaClient, session, flow);
				} finally {
					loginSessions.remove(session);
				}
			});
			return new AuthDetails(
					flow.verificationUri(),
					flow.userCode(),
					flow.verificationUriComplete(),
					session
					);
		} catch (IOException e) {
			logger.error("Failed to initiate device flow", e);
		}
		return null;
	}

	@Override
	public void cancelLogin(int clientID, UUID session) {
		logger.info("Login attempt cancelled for {}/{}", clientID, session);
		var proc = loginSessions.remove(session);
		if (proc != null) {
			// might have already removed thread if login completed while being cancelled
			logger.debug("Interrupting thread: {}", proc);
			proc.interrupt();
		}
	}

	@Override
	public void logout(int gdaClient) {
		logger.debug("Logging out client: {}", gdaClient);
		updateTokens(tokens -> {
			var token = tokens.remove(gdaClient);
			if (token != null) {
				try {
					var req = Request.Post(conf.revokeEndpoint())
							.bodyForm(clientId, new BasicNameValuePair("token", token.refreshToken()), new BasicNameValuePair("token_type_hint", "refresh_token"))
							.execute();
					req.discardContent();
				} catch (IOException e) {
					logger.error("Error logging out client ({})", gdaClient, e);
				}
			}
		});
		MsgBus.publish(BlueApiEvent.logout(gdaClient));
	}

	@Override
	public String loggedInUser(int clientID) {
		return readTokens(tokens -> {
			var user = tokens.get(clientID);
			return user == null ? null : user.user();
		});
	}

	private void login(int gdaClient, UUID session, DeviceFlowInfo info) {
		var interval = Duration.ofSeconds(info.interval);
		var timeout = Instant.now().plusSeconds(info.expires);
		while (Instant.now().isBefore(timeout)) {
			var evt = pollForLogin(gdaClient, session, info);
			if (evt != null) {
				MsgBus.publish(evt);
				return;
			}
			try {
				Thread.sleep(interval);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				MsgBus.publish(BlueApiEvent.error(gdaClient, session, "Server interrupted while waiting for authorization"));
				return;
			}
		}
		logger.warn("Timed out waiting for user to authenticate");
		MsgBus.publish(BlueApiEvent.timeout(gdaClient, session));
	}

	private BlueApiEvent pollForLogin(int gdaClient, UUID session, DeviceFlowInfo info) {
		try {
			var resp = Request.Post(conf.tokenEndpoint())
					.bodyForm(clientId, DEVICE_GRANT, info.deviceCodePair())
					.execute();
			var response = resp.returnResponse();
			if (response.getStatusLine().getStatusCode() < 300) {
				var content = OBJECT_MAPPER.readValue(response.getEntity().getContent(), TokenInfo.class);
				var user = JWT.decode(content.accessToken()).getClaim("fedid").asString();
				updateTokens(tokens -> tokens.put(gdaClient, new LoginDetails(user, content.refreshToken)));
				return BlueApiEvent.login(gdaClient, session, user);
			} else {
				var error = OBJECT_MAPPER.readValue(response.getEntity().getContent(), PollError.class);
				switch (error.error) {
				case "authorization_pending", "slow_down" ->
					// The slow down case should not happen as we're sleeping for the interval we're given
					// but if it does, ignore it and move on
					logger.trace("Still waiting for user to authenticate");
				case "expired_token" -> {
					logger.warn("Token expired while waiting for user to authenticate");
					return BlueApiEvent.timeout(gdaClient, session);
				}
				case "access_denied" -> {
					logger.info("User denied access to blueApi");
					return BlueApiEvent.denied(gdaClient, session);
				}
				default -> {
					logger.warn("Unexpected error from auth polling: {}", error);
					return BlueApiEvent.error(gdaClient, session, error.description);
				}
				}
			}
		} catch (IOException e) {
			// Potential errors:
			// * Connection error making http request
			// * Invalid content in response (failing to deserialize)
			// * Unexpected response from server (3xx redirect etc)
			logger.error("Error while polling auth server for token", e);
			return BlueApiEvent.error(gdaClient, session, "Error while waiting for authorization: " + e);
		}
		return null;
	}

	/** Read from the map of user logins - handles locks to ensure map is not modified while reading */
	private <T> T readTokens(Function<Map<Integer, LoginDetails>, T> read) {
		tokenLock.readLock().lock();
		try {
			return read.apply(userLogins);
		} finally {
			tokenLock.readLock().unlock();
		}
	}

	/** Update the map of user logins - locks map to prevent read access while map is being updated */
	private void updateTokens(Consumer<Map<Integer, LoginDetails>> update) {
		tokenLock.writeLock().lock();
		try {
			update.accept(userLogins);
		} finally {
			tokenLock.writeLock().unlock();
		}
	}

	public void setClientId(String clientId) {
		this.clientId = new BasicNameValuePair("client_id", requireNonNull(clientId, "Client ID must not be null"));
	}

	Optional<String> getThreadToken() {
		var threadToken = accessToken.get();
		if (threadToken != null && threadToken.isAlive()) {
			return Optional.of(threadToken.token());
		} else {
			// Remove void token - possibly the null value just added by the `get` call
			accessToken.remove();
			return Optional.empty();
		}
	}

	Optional<String> getToken() {
		var threadToken = getThreadToken();
		if (threadToken.isPresent()) return threadToken;

		var client = JythonServerThread.clientId();
		if (client.isEmpty()) return Optional.empty();

		var login = readTokens(tokens -> tokens.get(client.get()));
		if (login == null) {
			logger.debug("Client {} has no auth info", client.get());
			return Optional.empty();
		}
		logger.info("Getting access token for {}", login.user);

		try {
			var response = Request.Post(conf.tokenEndpoint())
					.bodyForm(REFRESH_GRANT, clientId, login.refreshPair())
					.execute()
					.returnResponse();
			var content = OBJECT_MAPPER.readValue(response.getEntity().getContent(), TokenInfo.class);
			updateTokens(tokens -> tokens.put(client.get(), new LoginDetails(login.user, content.refreshToken())));
			accessToken.set(new ActiveToken(client.get(), content.accessToken, Instant.now().plusSeconds(content.expires)));
			return Optional.of(content.accessToken);
		} catch (IOException e) {
			logger.error("Error refreshing auth tokens", e);
			accessToken.remove();
			return Optional.empty();
		}
	}
}
