package uk.ac.diamond.daq.experiment.api.plan;

import java.time.ZonedDateTime;

public interface ITriggerEvent {

	ZonedDateTime getTimestamp();

	double getTriggeringSignal();

}