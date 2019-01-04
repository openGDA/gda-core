package uk.ac.diamond.daq.experiment.api.plan;

import java.time.ZonedDateTime;

public interface ISegmentAccount {

	void terminated(double terminationSignal);

	String getSegmentName();

	ZonedDateTime getStartTime();

	ZonedDateTime getEndTime();

	double getTerminationSignal();

}