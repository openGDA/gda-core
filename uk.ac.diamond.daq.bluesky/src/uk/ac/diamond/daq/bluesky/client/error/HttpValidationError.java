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

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Data was not not valid for the request */
@JsonIgnoreProperties(ignoreUnknown = true)
public class HttpValidationError extends ApiException {
	public record ValidationError(
			@JsonProperty("loc") List<Object> locations,
			@JsonProperty("msg") String message,
			String type,
			@JsonProperty
			Object input
			) implements Serializable {}

	private final List<ValidationError> detail;

	@JsonCreator
	public HttpValidationError(@JsonProperty("detail") List<ValidationError> detail) {
		super(detail.stream()
				.map(Object::toString)
				.collect(joining("; ")));
		this.detail = detail;
	}

	public List<ValidationError> getDetail() {
		return detail;
	}

	/**
	 * Set the stacktrace to the calling frame
	 *
	 * Allows instances of this error to be deserialized from JSON without the JSON
	 * parsing code being included in the traceback
	 */
	public HttpValidationError withResetStackTrace() {
		// Clear the jackson deserialisation frames from stack
		var trace = stream(Thread.currentThread().getStackTrace())
				// one frame is the `getStackTrace` method
				// one frame is this function
				.skip(2)
				.toArray(StackTraceElement[]::new);
		setStackTrace(trace);
		return this;
	}
}