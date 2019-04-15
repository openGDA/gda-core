package uk.ac.diamond.daq.experiment.ui.plan.tree;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scanning.api.event.status.Status;

import uk.ac.diamond.daq.experiment.api.plan.event.SegmentRecord;
import uk.ac.diamond.daq.experiment.api.plan.event.TriggerRecord;

public class SegmentNode implements PlanTreeNode {

	private SegmentRecord segmentRecord;
	private List<TriggerNode> triggerEvents;
	private Status status;
	
	public SegmentNode(SegmentRecord segmentRecord, List<TriggerRecord> triggerRecords, Status status) {
		this.segmentRecord = segmentRecord;
		triggerEvents = new ArrayList<>();
		triggerRecords.forEach(rec -> 
			rec.getEvents().forEach(event -> 
			triggerEvents.add(new TriggerNode(event, rec, this)))
		);
		this.setStatus(status);
	}

	@Override
	public String getName() {
		return segmentRecord.getSegmentName();
	}

	@Override
	public long getTime() {
		return segmentRecord.getStartTime();
	}

	@Override
	public String getSevName() {
		return segmentRecord.getSampleEnvironmentName();
	}

	@Override
	public double getSignificantSignal() {
		return segmentRecord.getTerminationSignal();
	}
	
	public boolean hasFinished() {
		return segmentRecord.getEndTime() >= segmentRecord.getStartTime();
	}
	
	public List<TriggerNode> getTriggerEvents() {
		return triggerEvents;
	}
	
	@Override
	public long getRelativeStart() {
		return getTime();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (getTime() ^ (getTime() >>> 32));
		result = prime * result + getName().hashCode();
		result = prime * result + getSevName().hashCode();
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SegmentNode other = (SegmentNode) obj;
		if (!getName().equals(other.getName()))
			return false;
		if (!getSevName().equals(other.getSevName()))
			return false;
		if (getTime() != other.getTime())
			return false;
		if (hasFinished() && other.hasFinished()) {
			if (getSignificantSignal() != other.getSignificantSignal())
				return false;
			if (!getTriggerEvents().equals(other.getTriggerEvents()))
				return false;
		} else if (!triggerEvents.containsAll(other.getTriggerEvents())) {
			return false;
		}
		return true;
	}

	@Override
	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

}
