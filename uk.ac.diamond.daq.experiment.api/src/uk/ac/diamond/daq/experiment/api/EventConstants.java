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

package uk.ac.diamond.daq.experiment.api;

import uk.ac.diamond.daq.experiment.api.plan.IPlan;
import uk.ac.diamond.daq.experiment.api.structure.ExperimentController;

public class EventConstants {

	/**
	 * Topic for {@link ExperimentController} events
	 */
	public static final String EXPERIMENT_CONTROLLER_TOPIC = "uk.ac.diamond.daq.experiment.controller.topic";

	/**
	 * Default topic for experiment {@link IPlan} updates
	 */
	public static final String EXPERIMENT_PLAN_TOPIC = "uk.ac.diamond.daq.experiment.plan.topic";

	/**
	 * Request topic for all experiment nexus file operations
	 */
	public static final String NEXUS_REQUEST_TOPIC = "uk.ac.diamond.daq.experiment.nexus.request.topic";

	/**
	 * Response topic for all experiment nexus file operations
	 */
	public static final String NEXUS_RESPONSE_TOPIC = "uk.ac.diamond.daq.experiment.nexus.response.topic";

	private EventConstants() {/*static access only!*/}

}
