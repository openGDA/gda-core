package uk.ac.diamond.daq.experiment.api.remote;

import java.io.Serializable;
import java.util.List;

public interface PlanRequest extends Serializable {


	String getPlanName();


	String getPlanDescription();

	boolean isDriverUsed();

	String getExperimentDriverName();


	String getExperimentDriverProfile();


	List<SegmentRequest> getSegmentRequests();

}
