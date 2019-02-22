package uk.ac.diamond.daq.experiment.api.remote;

import java.io.Serializable;
import java.util.List;

public interface PlanRequest extends Serializable {


	String getPlanName();


	String getPlanDescription();


	/**
	 * {@code null} if none used in the plan
	 */
	String getExperimentDriverName();
	
	
	/**
	 * {@code null} if no driver used
	 */
	String getExperimentDriverProfile();


	List<SegmentRequest> getSegmentRequests();

}
