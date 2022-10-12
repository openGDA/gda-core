package uk.ac.diamond.daq.experiment.api.remote;

public interface SEVListenerRequest {


	/**
	 * Name of this request
	 */
	String getName();


	/**
	 * Name of SEV this request will listen to.
	 * {@code null} if signal source is not position.
	 */
	String getSampleEnvironmentVariableName();


	/**
	 * The type of signal produced by the SEV this request listens to
	 */
	SignalSource getSignalSource();

}
