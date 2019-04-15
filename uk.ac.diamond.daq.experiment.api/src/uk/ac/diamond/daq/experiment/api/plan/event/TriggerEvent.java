package uk.ac.diamond.daq.experiment.api.plan.event;

import java.io.Serializable;
import java.time.Instant;

public class TriggerEvent implements Serializable {

	private static final long serialVersionUID = 5876166459318061316L;

	private String id;
	private long timestamp;
	private double triggeringSignal;
	private boolean failed;

	public TriggerEvent(double triggeringSignal) {
		timestamp = Instant.now().toEpochMilli();
		this.triggeringSignal = triggeringSignal;
	}

	public TriggerEvent() {}

	public void setId(String uniqueId) {
		this.id = uniqueId;
	}

	public String getId() {
		return id;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public double getTriggeringSignal() {
		return triggeringSignal;
	}

	public void setTriggeringSignal(double triggeringSignal) {
		this.triggeringSignal = triggeringSignal;
	}

	@Override
	public String toString() {
		return "TriggerEvent [timestamp=" + timestamp + ", triggeringSignal=" + triggeringSignal + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + (int) (timestamp ^ (timestamp >>> 32));
		long temp;
		temp = Double.doubleToLongBits(triggeringSignal);
		result = prime * result + (int) (temp ^ (temp >>> 32));
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
		TriggerEvent other = (TriggerEvent) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (timestamp != other.timestamp)
			return false;
		if (Double.doubleToLongBits(triggeringSignal) != Double.doubleToLongBits(other.triggeringSignal))
			return false;
		return true;
	}

	public boolean isFailed() {
		return failed;
	}

	public void setFailed(boolean failed) {
		this.failed = failed;
	}

}
