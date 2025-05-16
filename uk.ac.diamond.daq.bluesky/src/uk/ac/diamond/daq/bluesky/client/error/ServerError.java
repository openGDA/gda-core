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

package uk.ac.diamond.daq.bluesky.client.error;

import org.apache.http.StatusLine;

/** Internal Server Error */
public class ServerError extends ApiException {

	private final String content;
	private final String reason;
	private final int code;

	public ServerError(StatusLine status, String content) {
		super("Status " + status);
		this.code = status.getStatusCode();
		this.reason = status.getReasonPhrase();
		this.content = content;
	}

	public String getContent() {
		return content;
	}

	public String getReason() {
		return reason;
	}

	public int getCode() {
		return code;
	}

	@Override
	public String getMessage() {
		return "Status %d %s".formatted(code, reason);
	}

}
