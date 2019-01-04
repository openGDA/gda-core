package uk.ac.diamond.daq.experiment.plan;

import java.time.Duration;
import java.time.Instant;

import uk.ac.diamond.daq.experiment.api.plan.IPlanRegistrar;

public class TimedSegment extends AbstractSegment {
	
	private long duration; // ms

	TimedSegment(IPlanRegistrar registrar, long duration) {
		super(registrar);
		this.duration = duration;
	}
	
	@Override
	public void activate() {
		super.activate(); // enable all triggers
		Instant startTime = Instant.now();
		while (Duration.between(startTime, Instant.now()).toMillis() < duration) {
			// stay active
		}
		deactivate(Duration.between(startTime, Instant.now()).toMillis());
	}

}
