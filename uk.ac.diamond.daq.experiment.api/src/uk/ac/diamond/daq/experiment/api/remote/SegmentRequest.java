package uk.ac.diamond.daq.experiment.api.remote;

import java.io.Serializable;
import java.util.List;

public interface SegmentRequest extends SEVListenerRequest, Serializable {


	Inequality getInequality();


	double getInequalityArgument();


	/**
	 * in minutes
	 */
	double getDuration();


	List<TriggerRequest> getTriggerRequests();

}
