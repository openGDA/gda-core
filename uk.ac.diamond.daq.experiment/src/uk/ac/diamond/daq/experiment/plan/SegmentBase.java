package uk.ac.diamond.daq.experiment.plan;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.experiment.api.plan.IPlanRegistrar;
import uk.ac.diamond.daq.experiment.api.plan.ISampleEnvironmentVariable;
import uk.ac.diamond.daq.experiment.api.plan.ISegment;
import uk.ac.diamond.daq.experiment.api.plan.ITrigger;

/**
 * Base segment implementation which handles the enabling and disabling of triggers.
 * Final implementations provide limiting logic by implementing {@link #shouldTerminate(double)}
 */
public abstract class SegmentBase implements ISegment {
	private static final Logger logger = LoggerFactory.getLogger(SegmentBase.class);
	private String name;
	
	private List<ITrigger> enabledTriggers = new ArrayList<>();
	
	private IPlanRegistrar registrar;
	protected final ISampleEnvironmentVariable sev;
	private volatile boolean activated;
	
	SegmentBase(IPlanRegistrar registrar, ISampleEnvironmentVariable sev) {
		this.registrar = registrar;
		this.sev = sev;
	}
	
	@Override
	public void enable(ITrigger trigger) {
		if (!enabledTriggers.contains(trigger)) {
			enabledTriggers.add(trigger);
		}
	}
	
	@Override
	public boolean isActivated() {
		return activated;
	}
	
	@Override
	public List<ITrigger> getTriggers() {
		return enabledTriggers;
	}
	
	@Override
	public void activate() {
		activated = true;
		double startingSignal = sev.read();
		if (shouldTerminate(startingSignal)) {
			logger.warn("Skipping Segment {} as limit condition met on activation", getName());
			terminateSegment(startingSignal);
		} else {
			logger.info("Segment '{}' activated", getName());
			sev.addListener(this);
			
			enabledTriggers.forEach(tp -> tp.setEnabled(true));
		}
	}
	
	@Override
	public void signalChanged(double signal) {
		if (shouldTerminate(signal)) terminateSegment(signal);
	}
	
	abstract boolean shouldTerminate(double signal);
	
	private void deactivate(double terminatingSignal) {
		enabledTriggers.forEach(tp -> tp.setEnabled(false)); // Some might be needed in the next segment, but we don't want anything happening in the transfer period.
		activated = false;
		registrar.segmentComplete(this, terminatingSignal);
	}
	
	private synchronized void terminateSegment(double signal) {
		if (activated) {
			logger.info("Segment '{}' terminated", getName());
			sev.removeListener(this);
			deactivate(signal);
		} else {
			logger.debug("Ignoring terminating signal because segment '{}' is already terminated", getName());
		}
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public void setName(String name) {
		this.name = name;
	}
}
