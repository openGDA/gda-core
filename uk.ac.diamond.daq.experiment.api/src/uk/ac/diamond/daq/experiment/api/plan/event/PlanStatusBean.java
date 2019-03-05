package uk.ac.diamond.daq.experiment.api.plan.event;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.scanning.api.event.IdBean;
import org.eclipse.scanning.api.event.status.StatusBean;

import uk.ac.diamond.daq.experiment.api.plan.IPlan;

/**
 * This bean holds the current status of an {@link IPlan}
 */
public class PlanStatusBean extends StatusBean {

	private String driverName;
	private String driverProfile;

	private List<SegmentRecord> segments = new CopyOnWriteArrayList<>();
	private List<TriggerRecord> triggers = new CopyOnWriteArrayList<>();
	private String lastTrigger;

	public List<SegmentRecord> getSegments() {
		return segments;
	}

	public void setSegments(List<SegmentRecord> segments) {
		this.segments = segments;
	}

	public List<TriggerRecord> getTriggers() {
		return triggers;
	}

	public void setTriggers(List<TriggerRecord> triggers) {
		this.triggers = triggers;
	}

	public String getLastTrigger() {
		return lastTrigger;
	}

	public void setLastTrigger(String lastTrigger) {
		this.lastTrigger = lastTrigger;
	}

	public String getDriverName() {
		return driverName;
	}

	public void setDriverName(String driverName) {
		this.driverName = driverName;
	}

	public String getDriverProfile() {
		return driverProfile;
	}

	public void setDriverProfile(String driverProfile) {
		this.driverProfile = driverProfile;
	}

	@Override
	public <T extends IdBean> void merge(T with) {
		super.merge(with);
		PlanStatusBean other = (PlanStatusBean) with;
		this.driverName = other.driverName;
		this.driverProfile = other.driverProfile;
		this.lastTrigger = other.lastTrigger;
		this.segments = other.segments;
		this.triggers = other.triggers;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((driverName == null) ? 0 : driverName.hashCode());
		result = prime * result + ((driverProfile == null) ? 0 : driverProfile.hashCode());
		result = prime * result + ((lastTrigger == null) ? 0 : lastTrigger.hashCode());
		result = prime * result + ((segments == null) ? 0 : segments.hashCode());
		result = prime * result + ((triggers == null) ? 0 : triggers.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		PlanStatusBean other = (PlanStatusBean) obj;
		if (driverName == null) {
			if (other.driverName != null)
				return false;
		} else if (!driverName.equals(other.driverName))
			return false;
		if (driverProfile == null) {
			if (other.driverProfile != null)
				return false;
		} else if (!driverProfile.equals(other.driverProfile))
			return false;
		if (lastTrigger == null) {
			if (other.lastTrigger != null)
				return false;
		} else if (!lastTrigger.equals(other.lastTrigger))
			return false;
		if (segments == null) {
			if (other.segments != null)
				return false;
		} else if (!segments.equals(other.segments))
			return false;
		if (triggers == null) {
			if (other.triggers != null)
				return false;
		} else if (!triggers.equals(other.triggers))
			return false;
		return true;
	}

	private static final long serialVersionUID = -2311026915354764454L;
}
