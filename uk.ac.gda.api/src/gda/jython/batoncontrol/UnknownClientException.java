/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package gda.jython.batoncontrol;

public class UnknownClientException extends RuntimeException {

	private static final String ERROR_SERVER_UNKNOWN_CLIENT = "Client %s unrecognised by server, has client outlived its server? Try restarting the client";

	public UnknownClientException(String clientId) {
		super(String.format(ERROR_SERVER_UNKNOWN_CLIENT, clientId));
	}

}
