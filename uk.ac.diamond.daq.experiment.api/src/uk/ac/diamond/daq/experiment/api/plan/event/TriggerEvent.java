package uk.ac.diamond.daq.experiment.api.plan.event;

import java.io.Serializable;
import java.time.Instant;

public class TriggerEvent implements Serializable {

	private static final long serialVersionUID = 5876166459318061316L;

	private long timestamp;
	private double triggeringSignal;
	private Boolean success;

	public TriggerEvent(double triggeringSignal) {
		timestamp = Instant.now().toEpochMilli();
		this.triggeringSignal = triggeringSignal;
	}

	public TriggerEvent() {}

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

	/**
	 * {@code null} if triggered job is ongoing
	 */
	public Boolean isSuccessful() {
		return success;
	}

	public void setSuccessful(boolean success) {
		this.success = success;
	}

	@Override
	public String toString() {

		String successMessage = success == null ? "ongoing..." : "success="	+ success;
		return "TriggerEvent [timestamp=" + timestamp + ", triggeringSignal=" + triggeringSignal + ", "
				+ successMessage + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((success == null) ? 0 : success.hashCode());
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
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		TriggerEvent other = (TriggerEvent) obj;
		if (success == null) {
			if (other.success != null)
				return false;
		} else if (!success.equals(other.success))
			return false;
		if (timestamp != other.timestamp)
			return false;
		if (Double.doubleToLongBits(triggeringSignal) != Double.doubleToLongBits(other.triggeringSignal))
			return false;
		return true;
	}

}
