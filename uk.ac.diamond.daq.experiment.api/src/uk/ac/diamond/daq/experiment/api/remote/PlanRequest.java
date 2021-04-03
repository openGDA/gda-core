package uk.ac.diamond.daq.experiment.api.remote;

import java.io.Serializable;
import java.util.List;

import uk.ac.diamond.daq.experiment.api.plan.DriverBean;
import uk.ac.gda.common.entity.Document;

public interface PlanRequest extends Serializable {

	/**
	 * @return
	 * @deprecated use {@link Document#getName()}
	 */
	@Deprecated
	String getPlanName();

	/**
	 * @return
	 * @deprecated use {@link Document#getDescription()}
	 */
	@Deprecated
	String getPlanDescription();


	DriverBean getDriverBean();


	default boolean isDriverUsed() {
		return getDriverBean() != null;
	}


	List<SegmentRequest> getSegmentRequests();
}
