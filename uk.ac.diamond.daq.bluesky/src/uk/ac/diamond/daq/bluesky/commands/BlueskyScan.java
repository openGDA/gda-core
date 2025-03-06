/*-
 * Copyright © 2023 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.bluesky.commands;

import java.util.List;
import java.util.Map;

import uk.ac.diamond.daq.bluesky.api.model.Task;

public record BlueskyScan(String motor, double start, double stop, int points, List<String> detectors) {

	private static final String PLAN_NAME = "spec_scan";

	public Task toPlan() {
		var spec = Map.of("num", points, "start", start, "axis", motor, "stop", stop, "type", "Line");
		return new Task(PLAN_NAME, Map.of("detectors", detectors, "spec", spec));
	}
}
