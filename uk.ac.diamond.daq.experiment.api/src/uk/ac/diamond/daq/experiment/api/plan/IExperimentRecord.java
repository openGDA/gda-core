package uk.ac.diamond.daq.experiment.api.plan;

import java.util.List;

public interface IExperimentRecord {

	List<ISegmentAccount> getSegmentAccounts();

	List<ITriggerAccount> getTriggerAccounts();

	ISegmentAccount getSegmentAccount(String segmentName);

	ITriggerAccount getTriggerAccount(String triggerName);

	String summary();

}