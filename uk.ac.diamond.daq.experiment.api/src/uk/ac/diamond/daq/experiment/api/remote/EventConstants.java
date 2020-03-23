package uk.ac.diamond.daq.experiment.api.remote;

import uk.ac.diamond.daq.experiment.api.plan.IPlan;

public class EventConstants {

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
