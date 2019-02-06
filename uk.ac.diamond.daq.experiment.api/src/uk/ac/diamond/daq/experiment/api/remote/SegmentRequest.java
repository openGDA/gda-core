package uk.ac.diamond.daq.experiment.api.remote;

import java.io.Serializable;
import java.util.List;

public interface SegmentRequest extends Serializable {


	String getName();


	SignalSource getSignalSource();


	/**
	 * null if signal source is not position
	 */
	String getSampleEnvironmentVariableName();


	Inequality getInequality();


	double getInequalityArgument();


	/**
	 * in minutes
	 */
	double getDuration();


	List<TriggerRequest> getTriggerRequests();

}
