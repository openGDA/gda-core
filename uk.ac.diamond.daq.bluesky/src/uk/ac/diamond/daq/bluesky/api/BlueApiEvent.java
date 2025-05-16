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

import static java.util.Objects.requireNonNull;

import java.io.Serializable;
import java.util.UUID;

public sealed interface BlueApiEvent extends Serializable
		permits BlueApiEvent.Login, BlueApiEvent.Logout, BlueApiEvent.Timeout, BlueApiEvent.Denied, BlueApiEvent.Error {

	public record Login(int client, UUID session, String user) implements BlueApiEvent {
	}

	public record Logout(int client) implements BlueApiEvent {
	}

	public record Timeout(int client, UUID session) implements BlueApiEvent {
	}

	public record Denied(int client, UUID session) implements BlueApiEvent {
	}

	public record Error(int client, UUID session, String message) implements BlueApiEvent {
	}

	public static BlueApiEvent login(int client, UUID session, String user) {
		return new BlueApiEvent.Login(client, session, user);
	}

	public static BlueApiEvent logout(int client) {
		return new BlueApiEvent.Logout(client);
	}

	public static BlueApiEvent timeout(int client, UUID session) {
		return new BlueApiEvent.Timeout(client, session);
	}

	public static BlueApiEvent denied(int client, UUID session) {
		return new BlueApiEvent.Denied(client, session);
	}

	public static BlueApiEvent error(int client, UUID session, String message) {
		return new BlueApiEvent.Error(client, session, message);
	}

	public int client();

	public default UUID session() {
		return null;
	}

	public default boolean matches(int client, UUID session) {
		return client() == client && requireNonNull(session).equals(session());
	}

}
