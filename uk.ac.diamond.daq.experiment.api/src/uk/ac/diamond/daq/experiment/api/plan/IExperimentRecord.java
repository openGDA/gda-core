package uk.ac.diamond.daq.experiment.api.plan;

import java.util.List;

import uk.ac.diamond.daq.experiment.api.plan.event.SegmentRecord;
import uk.ac.diamond.daq.experiment.api.plan.event.TriggerRecord;

public interface IExperimentRecord {

	List<SegmentRecord> getSegmentRecords();

	List<TriggerRecord> getTriggerRecords();

	SegmentRecord getSegmentRecord(String segmentName);

	TriggerRecord getTriggerRecord(String triggerName);

	String summary();

}