package uk.ac.diamond.daq.experiment.api.remote;

import java.io.Serializable;
import java.util.UUID;

public interface TriggerRequest extends SEVListenerRequest, Serializable {


	UUID getScanId();


	/**
	 * @deprecated favour UUID
	 */
	@Deprecated(since="GDA 9.27")
	String getScanName();


	ExecutionPolicy getExecutionPolicy();


	double getTarget();


	double getTolerance();


	double getInterval();

}
