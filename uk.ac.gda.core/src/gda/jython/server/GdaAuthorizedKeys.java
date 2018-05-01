/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package gda.jython.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.UserPrincipal;
import java.security.PublicKey;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.sshd.common.util.io.ModifiableFileWatcher;
import org.apache.sshd.server.auth.pubkey.PublickeyAuthenticator;
import org.apache.sshd.server.config.keys.AuthorizedKeysAuthenticator;
import org.apache.sshd.server.session.ServerSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;

/**
 * Public key authentication provider for GDA
 * <p>
 * This enables facilities to have a central directory for controlling access to GDA.
 * Given /GDA_KEYS/ as the root directory, the authorized keys file used to authenticate a user
 * will be one of:
 *
 * <ul>
 * <li>/GDA_ROOT/username.pub</li>
 * <li>/GDA_ROOT/$BEAMLINE/username.pub`</li>
 * </ul>
 *
 * where $BEAMLINE is the value of the {@code gda.beamline} property at startup.
 * <p>
 * If both files are present, the non-beamline version is used.
 */
public class GdaAuthorizedKeys implements PublickeyAuthenticator {

	private static final Logger logger = LoggerFactory.getLogger(GdaAuthorizedKeys.class);
	private static final String BEAMLINE = LocalProperties.get(LocalProperties.GDA_BEAMLINE_NAME);
	private final Path directory;
	private final Map<String, AuthorizedKeysAuthenticator> auth = new ConcurrentHashMap<>();

	public GdaAuthorizedKeys(String keysDirectory) {
		directory = Paths.get(keysDirectory);
	}

	/**
	 * Authenticate a given user and public key for an SSH session.
	 * <p>
	 * Allow access if the user has a valid authorizedKeys file (file exists and is read/writable by the user only)
	 * and contains the public key.
	 *
	 * @return true if user is authenticated
	 */
	@Override
	public boolean authenticate(String username, PublicKey key, ServerSession session) {
		if (logger.isInfoEnabled()) {
			logger.info("Authenticating '{}' from '{}'",
					username,
					resolveHost(session.getClientAddress()));
		}

		AuthorizedKeysAuthenticator keys = auth.computeIfAbsent(username, this::getAuthorizedKeysFor);
		if (validKeysForUser(keys, username)) {
			return keys.authenticate(username, key, session);
		}
		return false;
	}

	/**
	 * Return an KeyAuthenticator for the given user.
	 * <p>
	 * Returns authenticator using one of
	 * <ul>
	 * <li>/GDA_ROOT/username.pub</li>
	 * <li>/GDA_ROOT/$BEAMLINE/username.pub`</li>
	 * </ul>
	 * if they exist or null if neither is available.
	 * @param username user to get key authenticator for
	 * @return Authenticator or null if neither file is available
	 */
	private AuthorizedKeysAuthenticator getAuthorizedKeysFor(String username) {
		AuthorizedKeysAuthenticator keyFile = null;
		keyFile = accessFileFor(username, null);
		if (keyFile == null) {
			keyFile = accessFileFor(username, BEAMLINE);
		}
		return keyFile;
	}

	/**
	 * If there exists a file in the given subdirectory named ${username}.pub, return a KeyAuthenticator
	 * that uses it.
	 * @param username name to use to look up the key file
	 * @param subdirectory if non-null, look in subdirectory instead of top level directory
	 * @return AuthorizedKeyAuthenticator for the given user if file exists
	 */
	private AuthorizedKeysAuthenticator accessFileFor(String username, String subdirectory) {
		Path keys = directory;
		if (subdirectory != null) {
			keys = keys.resolve(subdirectory);
		}
		keys = keys.resolve(username + ".pub");
		if (keys.toFile().exists()) {
			return new AuthorizedKeysAuthenticator(keys);
		}
		return null;
	}

	/**
	 * Check if the given authenticator is valid for the user. AuthorizedKeys files must be readable only
	 * by the user
	 * @param keys
	 * @param username
	 * @return true if the file authenticator is valid
	 */
	private static boolean validKeysForUser(AuthorizedKeysAuthenticator keys, String username) {
		if (keys == null) {
			return false;
		}
		try {
			checkOwnership(keys.getPath(), username);
			SimpleImmutableEntry<String, Object> violations = ModifiableFileWatcher.validateStrictConfigFilePermissions(keys.getPath());
			// Violations will report that the file is not owned by the current user. In our case it shouldn't be as gda2
			// is running the server.
			// If the permissions are set incorrectly the returned pair is <message, invalid_permission>
			if (violations != null && !violations.getValue().equals(username)) {
				logger.error("{}", violations.getKey());
				return false;
			}
		} catch (IllegalStateException | IOException e) {
			// If we can't verify the access rights on the file, prevent access
			logger.error("Could not verify correct read/write permissions for {}", keys.getPath(), e);
			return false;
		}
		return true;
	}

	/**
	 * Check that a file is owned by the given user and that noone else can edit it. <p>
	 * Throws {@link IllegalStateException} if file is not owned by the user or if others have write access.
	 *
	 * @param path to check
	 * @param username that should own the file
	 */
	private static void checkOwnership(Path path, String username) {
		try {
			UserPrincipal user = Files.getOwner(path, LinkOption.NOFOLLOW_LINKS);
			if (!user.getName().equals(username)) {
				logger.error("Can't authenticate against {}. File must be owned by the user and must not be a symbolic link", path);
				throw new IllegalStateException("Key file must be owned by user and must not be a symbolic link");
			}
		} catch (IOException e) {
			logger.error("Could not read key file {}", path, e);
			throw new IllegalStateException("Could not read key file");
		}
	}

	/**
	 * Get the host name of the connecting machine - if not available, return the address
	 * @param socket
	 * @return Hostname of socket address if it can be resolved.
	 */
	private static String resolveHost(SocketAddress socket) {
		String addr = socket.toString();
		try {
			InetSocketAddress sock = (InetSocketAddress)socket;
			addr = InetAddress.getByAddress(sock.getAddress().getAddress()).getCanonicalHostName();
		} catch (ClassCastException | UnknownHostException e) {
			logger.error("Couldn't resolve hostname", e);
		}
		return addr;
	}
}