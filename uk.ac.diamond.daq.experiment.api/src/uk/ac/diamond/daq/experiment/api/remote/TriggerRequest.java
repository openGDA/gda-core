package uk.ac.diamond.daq.experiment.api.remote;

import java.io.Serializable;
import java.util.UUID;

public interface TriggerRequest extends Serializable {


	String getName();


	UUID getScanId();


	/**
	 * @deprecated favour UUID
	 */
	@Deprecated
	String getScanName();


	SignalSource getSignalSource();


	ExecutionPolicy getExecutionPolicy();


	String getSampleEnvironmentVariableName();


	double getTarget();


	double getTolerance();


	double getInterval();

}
