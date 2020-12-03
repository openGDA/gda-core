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
 * Represents an exhaustive response from the service or engine handling the acquisition request.
 *
 * <p>
 *  Actually the response contains only a text string. Further content will be added case by case.
 * </p>
 */
@JsonDeserialize(builder = RunAcquisitionResponse.Builder.class)
public class RunAcquisitionResponse {

	private final boolean submitted;
	private final String message;

	private RunAcquisitionResponse(boolean submitted, String message) {
		this.submitted = submitted;
		this.message = message;
	}

	public boolean isSubmitted() {
		return submitted;
	}

	public String getMessage() {
		return message;
	}

	@JsonPOJOBuilder
	public static class Builder {
		private boolean submitted;
		private String message;

		public Builder withSubmitted(boolean submitted) {
			this.submitted = submitted;
			return this;
		}

		public Builder withMessage(String message) {
			this.message = message;
			return this;
		}

		public RunAcquisitionResponse build() {
			return new RunAcquisitionResponse(submitted, message);
		}
	}
}
