/*-
 * Copyright © 2026 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.bluesky.api.model;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonCreator;

public sealed interface TaskOutcome permits TaskOutcome.TaskResult, TaskOutcome.TaskError {
	public record TaskResult(Object result, String type) implements TaskOutcome {}
	public record TaskError(String type, String message) implements TaskOutcome {}

	static final Logger logger = LoggerFactory.getLogger(TaskOutcome.class);
	static final String DISCRIMINATOR = "outcome";

	@JsonCreator
	public static TaskOutcome fromResult(Map<String, Object> result) {
		return switch (result.get(DISCRIMINATOR)) {
			case String s when s.equals("success") -> new TaskResult(
					result.get("result"),
					result.get("type").toString()
			);
			case String s when s.equals("error") -> new TaskError(
					result.get("type").toString(),
					result.get("message").toString()
			);
			case null -> {
				logger.error("TaskOutcome had no '{}' field", DISCRIMINATOR);
				yield null;
			}
			case Object other -> {
				logger.error("TaskOutcome had unknown {}: {}", DISCRIMINATOR, other);
				yield null;
			}
		};
	}
}
