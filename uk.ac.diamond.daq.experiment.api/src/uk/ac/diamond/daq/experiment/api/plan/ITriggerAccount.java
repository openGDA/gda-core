package uk.ac.diamond.daq.experiment.api.plan;

import java.util.List;

public interface ITriggerAccount {

	void triggered(double triggeringSignal);

	String getTriggerName();

	List<ITriggerEvent> getEvents();

}