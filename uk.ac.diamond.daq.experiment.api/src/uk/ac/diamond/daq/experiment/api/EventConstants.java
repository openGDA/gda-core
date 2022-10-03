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
