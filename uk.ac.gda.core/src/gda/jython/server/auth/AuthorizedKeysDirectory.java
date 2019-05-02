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

package gda.jython.server.auth;

import static gda.jython.server.auth.Authenticator.State.ACCEPT;
import static gda.jython.server.auth.Authenticator.State.REJECT;
import static gda.jython.server.auth.Authenticator.State.UNKNOWN;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.isRegularFile;
import static java.nio.file.LinkOption.NOFOLLOW_LINKS;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.UserPrincipal;
import java.security.PublicKey;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.sshd.common.util.io.ModifiableFileWatcher;
import org.apache.sshd.server.config.keys.AuthorizedKeysAuthenticator;
import org.apache.sshd.server.session.ServerSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Public key authenticator for GDA
 * <p>
 * This enables facilities to have a central directory for controlling access to GDA.
 * Given the key directory $GDA_ROOT, the authorised keys file used to authenticate a user
 * will be <code>$GDA_ROOT/username.pub</code>
 */
public class AuthorizedKeysDirectory implements Authenticator {
	private static final Logger logger = LoggerFactory.getLogger(AuthorizedKeysDirectory.class);
	/** The extension used by public keys */
	private static final String KEY_FILE_EXTENSION = ".pub";
	/** The directory from which to read public keys */
	private final Path directory;
	/**
	 * A cache of username to public key authenticators. The keys themselves are not cached so changing a
	 * key file while the server is running will still be reflected by subsequent logins.
	 */
	private final Map<String, AuthorizedKeysAuthenticator> auth = new ConcurrentHashMap<>();

	/** Create a key authenticator to read public keys from the given directory */
	public AuthorizedKeysDirectory(Path keysDirectory) {
		directory = keysDirectory;
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
	public State authenticate(String username, PublicKey key, ServerSession session) {
		AuthorizedKeysAuthenticator keys = auth.computeIfAbsent(username, this::accessFileFor);
		if (keys == null || !validPermissions(keys.getPath(), username)) {
			logger.trace("No valid key file found for {} in {}", username, directory);
			auth.remove(username);
			return UNKNOWN;
		} else {
			return keys.authenticate(username, key, session) ? ACCEPT : REJECT;
		}
	}

	/**
	 * If there exists a file in this authenticator's directory named ${username}{@value #KEY_FILE_EXTENSION},
	 * return a KeyAuthenticator that uses it.
	 * @param username name to use to look up the key file
	 * @return AuthorizedKeyAuthenticator for the given user if file exists
	 */
	private AuthorizedKeysAuthenticator accessFileFor(String username) {
		return Optional.of(directory.resolve(username + KEY_FILE_EXTENSION))
				.filter(p -> exists(p, NOFOLLOW_LINKS))
				.filter(p -> isRegularFile(p, NOFOLLOW_LINKS))
				.filter(p -> validPermissions(p, username))
				.map(AuthorizedKeysAuthenticator::new)
				.orElse(null);
	}

	/**
	 * Check that permissions are valid. Key files should be owned by the given user and should not be
	 * modifiable by any other users.
	 * @param file
	 * @param username
	 * @return
	 */
	private static boolean validPermissions(Path file, String username) {
		return ownedByUser(file, username) && noOtherAccess(file, username);
	}

	/**
	 * Check that a file is owned by the given user
	 *
	 * @param file to check
	 * @param username that should own the file
	 */
	private static boolean ownedByUser(Path file, String username) {
		try {
			UserPrincipal user = Files.getOwner(file, NOFOLLOW_LINKS);
			if (!user.getName().equals(username)) {
				logger.error("Can't authenticate against {}. File must be owned by the user and must not be a symbolic link", file);
				return false;
			}
		} catch (IOException e) {
			logger.error("Could not read key file {}", file, e);
			return false;
		}
		return true;
	}

	/**
	 * Check if the given path is accessible only by the user. AuthorizedKeys files must be writable only
	 * by the user
	 * @param file to check
	 * @param username of the user that should have access
	 * @return true if the file has the correct permissions
	 */
	private static boolean noOtherAccess(Path file, String username) {
		try {
			SimpleImmutableEntry<String, Object> violations = ModifiableFileWatcher.validateStrictConfigFilePermissions(file);
			/* In theory the keys file should be owned by the current user. In our case, the file must be
			 * owned by the user being authenticated instead. This will be reported as an error so we have
			 * to explicitly let it through. As the ownership check is the final check made, we can still
			 * rely on the group/world writable check.
			 * violations is in the form key=message, value=username */
			if (violations != null && !violations.getValue().equals(username)) {
				logger.error("{}", violations.getKey());
				return false;
			}
		} catch (IOException e) {
			// If we can't verify the access rights on the file, prevent access
			logger.error("Could not verify correct read/write permissions for {}", file, e);
			return false;
		}
		return true;
	}
}