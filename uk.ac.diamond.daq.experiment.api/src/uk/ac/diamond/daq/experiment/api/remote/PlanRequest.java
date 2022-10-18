package uk.ac.diamond.daq.experiment.api.remote;

import java.io.Serializable;
import java.util.List;

import uk.ac.diamond.daq.experiment.api.plan.DriverBean;
import uk.ac.gda.common.entity.Document;

public interface PlanRequest extends Document, Serializable {

	/**
	 * @return
	 * @deprecated use {@link Document#getName()}
	 */
	@Deprecated(since="GDA 9.21")
	String getPlanName();

	/**
	 * @return
	 * @deprecated use {@link Document#getDescription()}
	 */
	@Deprecated(since="GDA 9.21")
	String getPlanDescription();


	DriverBean getDriverBean();


	default boolean isDriverUsed() {
		return getDriverBean() != null;
	}


	List<SegmentRequest> getSegmentRequests();
}
