package uk.ac.diamond.daq.experiment.api.remote;

import java.io.Serializable;
import java.util.List;

import uk.ac.diamond.daq.experiment.api.plan.DriverBean;

public interface PlanRequest extends Serializable {


	String getPlanName();


	String getPlanDescription();


	DriverBean getDriverBean();


	default boolean isDriverUsed() {
		return getDriverBean() != null;
	}


	List<SegmentRequest> getSegmentRequests();
}
