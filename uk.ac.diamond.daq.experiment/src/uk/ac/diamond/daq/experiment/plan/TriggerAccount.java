package uk.ac.diamond.daq.experiment.plan;

import java.util.ArrayList;
import java.util.List;

import uk.ac.diamond.daq.experiment.api.plan.ITriggerAccount;
import uk.ac.diamond.daq.experiment.api.plan.ITriggerEvent;

/**
 * Keep track of when and why a particular ITrigger is triggered during an experiment
 * 
 * @author Douglas Winter
 */
public class TriggerAccount implements ITriggerAccount {
	
	private final String triggerName;
	private List<ITriggerEvent> events;
	
	TriggerAccount(String triggerName) {
		this.triggerName = triggerName;
		events = new ArrayList<>();
	}
	
	@Override
	public void triggered(double triggeringSignal) {
		events.add(new TriggerEvent(triggeringSignal));
	}
	
	@Override
	public String getTriggerName() {
		return triggerName;
	}
	
	@Override
	public List<ITriggerEvent> getEvents() {
		return events;
	}
}
