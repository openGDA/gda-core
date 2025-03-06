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

package uk.ac.diamond.daq.bluesky.api;

import java.io.Serializable;
import java.util.UUID;

public interface BlueApiAuth {
	public static final String SERVICE_NAME = "blueapi_auth";
	public record AuthDetails(
			/** The URL where the user to go to log in and grant access to blueAPI */
			String url,
			/** The code to be entered if prompted by the auth server */
			String code,
			/** The combined URL to save the user having to type the code */
			String fullUrl,
			/** A unique id for this login flow to distinguish between multiple attempts from the same client */
			UUID session) implements Serializable {}

	/**
	 * Check if authorisation is supported in the current configuration
	 * If this returns false, the {@link #initLogin} method will return null
	 */
	boolean authEnabled();

	/** Begin the login flow for the given client */
	AuthDetails initLogin(int clientID);

	/** Notify the server that the login attempt is never going to succeed and it can stop polling the auth server for a response */
	void cancelLogin(int clientID, UUID session);

	/** Request that the auth server revoke the authentication from the given client and drop any tokens */
	void logout(int clientID);

	/** Username of the logged in user (or null if not logged in) */
	String loggedInUser(int clientID);

}