package uk.ac.diamond.daq.experiment.ui.plan.tree;

import org.eclipse.scanning.api.event.status.Status;

import uk.ac.diamond.daq.experiment.api.plan.event.TriggerEvent;
import uk.ac.diamond.daq.experiment.api.plan.event.TriggerRecord;

public class TriggerNode implements PlanTreeNode {

	private TriggerEvent event;
	private TriggerRecord triggerRecord;
	private SegmentNode parent;
	
	private Status status;
	
	public TriggerNode(TriggerEvent event, TriggerRecord triggerRecord, SegmentNode parent) {
		this.event = event;
		this.triggerRecord = triggerRecord;
		this.parent = parent;
	}

	@Override
	public String getName() {
		return triggerRecord.getTriggerName();
	}

	@Override
	public long getTime() {
		return event.getTimestamp();
	}
	
	@Override
	public String getSevName() {
		return triggerRecord.getSampleEnvironmentName();
	}

	@Override
	public double getSignificantSignal() {
		return event.getTriggeringSignal();
	}
	
	@Override
	public long getRelativeStart() {
		return parent.getTime();
	}
	
	public String getId() {
		return event.getId();
	}
	
	@Override
	public Status getStatus() {
		return status;
	}
	
	public void setStatus(Status status) {
		this.status = status;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (event.getTimestamp() ^ (event.getTimestamp() >>> 32));
		long temp;
		temp = Double.doubleToLongBits(event.getTriggeringSignal());
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((triggerRecord == null) ? 0 : triggerRecord.getTriggerName().hashCode());
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
		TriggerNode other = (TriggerNode) obj;
		if (event == null) {
			if (other.event != null)
				return false;
		} else if (event.getTimestamp() != other.getTime() || event.getTriggeringSignal() != other.getSignificantSignal())
			return false;
		if (triggerRecord == null) {
			if (other.triggerRecord != null)
				return false;
		} else if (!triggerRecord.getTriggerName().equals(other.triggerRecord.getTriggerName()))
			return false;
		return true;
	}

	public boolean hasFailed() {
		return event.isFailed();
	}

}
