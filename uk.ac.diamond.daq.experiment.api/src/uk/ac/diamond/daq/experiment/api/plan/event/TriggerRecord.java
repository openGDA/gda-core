package uk.ac.diamond.daq.experiment.api.plan.event;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Keep track of when and why a particular ITrigger is triggered during an experiment
 */
public class TriggerRecord implements Serializable {

	private static final long serialVersionUID = -1263340531173097425L;

	private String triggerName;
	private List<TriggerEvent> events;

	public TriggerRecord(String triggerName) {
		this.triggerName = triggerName;
		events = new ArrayList<>();
	}

	public TriggerRecord() {}

	public String getTriggerName() {
		return triggerName;
	}

	public List<TriggerEvent> getEvents() {
		return events;
	}

	public void setEvents(List<TriggerEvent> events) {
		this.events = events;
	}

	public void addEvent(TriggerEvent event) {
		events.add(event);
	}
}
