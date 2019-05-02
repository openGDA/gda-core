/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.apache.sshd.server.auth.pubkey.PublickeyAuthenticator;
import org.apache.sshd.server.session.ServerSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A PublickeyAuthenticator that can provide multiple authentication methods to
 * access this GDA server instance.
 */
public class GdaAuthenticator implements PublickeyAuthenticator {
	private static final Logger logger = LoggerFactory.getLogger(GdaAuthenticator.class);
	/** List of authenticators to use to authenticate users against keys */
	private final Collection<Authenticator> authenticators;

	/** Create a sequential authenticator that uses the given authenticators */
	public GdaAuthenticator(Collection<Authenticator> authenticators) {
		this.authenticators = new ArrayList<>(authenticators);
	}

	/** Create a sequential authenticator that uses the given authenticators */
	public GdaAuthenticator(Authenticator... authenticators) {
		this.authenticators = Arrays.asList(authenticators);
	}

	@Override
	public boolean authenticate(String username, PublicKey key, ServerSession session) {
		if (logger.isInfoEnabled()) {
			logger.info("Authenticating '{}' from '{}'",
					username,
					resolveHost(session.getClientAddress()));
		}
		return acceptKey(username, key, session);
	}

	/**
	 * Check the given public key using each of the authenticators in turn.
	 * The first definitive accept/reject response is used. If no authenticators can handle
	 * this key, it is rejected.
	 */
	private boolean acceptKey(String username, PublicKey key, ServerSession session) {
		for (Authenticator auth: authenticators) {
			switch (auth.authenticate(username, key, session)) {
			case ACCEPT:
				return true;
			case REJECT:
				return false;
			case UNKNOWN:
			}
		}
		return false;
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
			addr = sock.getAddress().getCanonicalHostName();
		} catch (ClassCastException e) {
			logger.error("Couldn't resolve hostname", e);
		}
		return addr;
	}
}
