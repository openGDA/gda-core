package uk.ac.diamond.daq.experiment.plan;

import java.time.ZonedDateTime;

import uk.ac.diamond.daq.experiment.api.plan.ITriggerEvent;

public class TriggerEvent implements ITriggerEvent {
	
	private final ZonedDateTime timestamp;
	private final double triggeringSignal;
	
	TriggerEvent(double triggeringSignal) {
		timestamp = ZonedDateTime.now();
		this.triggeringSignal = triggeringSignal;
	}
	
	@Override
	public ZonedDateTime getTimestamp() {
		return timestamp;
	}
	
	@Override
	public double getTriggeringSignal() {
		return triggeringSignal;
	}

}
