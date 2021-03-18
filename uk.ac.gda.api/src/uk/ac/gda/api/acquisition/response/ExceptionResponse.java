/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package uk.ac.gda.api.acquisition.response;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

/**
 * Represents the exception thrown by a REST service
 *
 * @author Maurizio Nagni
 */
@JsonDeserialize(builder = ExceptionResponse.Builder.class)
public class ExceptionResponse {

	private final String message;
	private final String causeMessage;

	private ExceptionResponse(String message, String causeMessage) {
		this.message = message;
		this.causeMessage = causeMessage;
	}

	public String getMessage() {
		return message;
	}

	public String getCauseMessage() {
		return causeMessage;
	}

	@JsonPOJOBuilder
	public static class Builder {
		private String message;
		private String causeMessage;

		public Builder withMessage(String message) {
			this.message = message;
			return this;
		}

		public Builder withCauseMessage(String causeMessage) {
			this.causeMessage = causeMessage;
			return this;
		}

		public ExceptionResponse build() {
			return new ExceptionResponse(message, causeMessage);
		}
	}
}
