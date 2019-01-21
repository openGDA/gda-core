package uk.ac.diamond.daq.experiment.plan;

import uk.ac.diamond.daq.experiment.api.plan.IPlanRegistrar;
import uk.ac.diamond.daq.experiment.api.plan.ISampleEnvironmentVariable;

public class FixedDurationSegment extends SegmentBase {
	
	private double duration; // in seconds
	private double startTime;
	
	/**
	 * duration in seconds
	 */
	FixedDurationSegment(IPlanRegistrar registrar, ISampleEnvironmentVariable timer, double duration) {
		super(registrar, timer);
		this.duration = duration;
	}

	@Override
	public void activate() {
		this.startTime = sev.read();
		super.activate();
	}

	@Override
	boolean shouldTerminate(double signal) {
		return signal - startTime >= duration;
	}
	
	@Override
	public String toString() {
		return "FixedDurationSegment [name=" + getName() + "triggers=" + getTriggers()
			+ "duration=" + duration + " seconds]";
	}

}
