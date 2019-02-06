package uk.ac.diamond.daq.experiment.api.remote;

import java.io.Serializable;

public interface TriggerRequest extends Serializable {


	String getName();


	String getScanName();


	SignalSource getSignalSource();


	ExecutionPolicy getExecutionPolicy();


	String getSampleEnvironmentVariableName();


	double getTarget();


	double getTolerance();


	double getInterval();

}
