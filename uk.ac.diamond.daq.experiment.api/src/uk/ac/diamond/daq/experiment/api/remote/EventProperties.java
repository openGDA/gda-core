package uk.ac.diamond.daq.experiment.api.remote;

public class EventProperties {

	public static final String EXPERIMENT_STRUCTURE_JOB_REQUEST_TOPIC = "experiment.structure.job.request.topic";

	public static final String EXPERIMENT_STRUCTURE_JOB_RESPONSE_TOPIC = "experiment.structure.job.response.topic";

	private EventProperties() {
		throw new IllegalAccessError("static access only!");
	}
}
