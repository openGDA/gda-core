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

import java.security.PublicKey;

import org.apache.sshd.server.auth.pubkey.PublickeyAuthenticator;
import org.apache.sshd.server.session.ServerSession;

/**
 * An authenticator designed to be composed to create a {@link PublickeyAuthenticator}. Each authenticator can either
 * accept of reject a user/key pair or pass it back unchecked.
 */
@FunctionalInterface
public interface Authenticator {
	/** The result of checking a user/key pair */
	public enum State {
		/** This user/key pair has been authenticated */
		ACCEPT,
		/** This user/key pair has been rejected */
		REJECT,
		/** This authenticator can't check this user/key pair */
		UNKNOWN;
	}

	/**
	 * Try and authenticate this user/key pairing.
	 * Either accept/reject it or return UNKNOWN to show it can't be handled
	 * @param username The user name of the client trying to connect
	 * @param key The public key the user is offering as authentication
	 * @param session The user's server session
	 * @return The result of this authentication
	 */
	public State authenticate(String username, PublicKey key, ServerSession session);
}
