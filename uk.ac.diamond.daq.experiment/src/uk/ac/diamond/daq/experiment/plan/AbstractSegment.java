package uk.ac.diamond.daq.experiment.plan;

import java.util.ArrayList;
import java.util.List;

import uk.ac.diamond.daq.experiment.api.plan.IPlanRegistrar;
import uk.ac.diamond.daq.experiment.api.plan.ISegment;
import uk.ac.diamond.daq.experiment.api.plan.ITrigger;

/**
 * Base segment implementation which handles the enabling and disabling of triggers.
 * Final implementations can call {@link #deactivate(double)}
 */
public abstract class AbstractSegment implements ISegment {
	
	private String name;
	
	private List<ITrigger> enabledTriggers = new ArrayList<>();
	
	private IPlanRegistrar registrar;
	
	private boolean activated;
	
	AbstractSegment(IPlanRegistrar registrar) {
		this.registrar = registrar;
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
	
	protected List<ITrigger> getEnabledTriggers() {
		return enabledTriggers;
	}
	
	@Override
	public void activate() {
		activated = true;
		enabledTriggers.forEach(tp -> tp.setEnabled(true));
	}
	
	protected void deactivate(double terminatingSignal) {
		enabledTriggers.forEach(tp -> tp.setEnabled(false)); // Some might be needed in the next segment, but we don't want anything happening in the transfer period.
		activated = false;
		registrar.segmentComplete(this, terminatingSignal);
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
