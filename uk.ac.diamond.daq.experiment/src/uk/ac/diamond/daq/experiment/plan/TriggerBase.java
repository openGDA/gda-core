package uk.ac.diamond.daq.experiment.plan;

import uk.ac.diamond.daq.experiment.api.plan.IPlanRegistrar;
import uk.ac.diamond.daq.experiment.api.plan.ITrigger;
import uk.ac.diamond.daq.experiment.api.plan.Triggerable;

public abstract class TriggerBase implements ITrigger {
	
	private String name;

	protected final Triggerable triggerable;
	protected IPlanRegistrar registrar;
	
	protected boolean enabled;

	TriggerBase(IPlanRegistrar registrar, Triggerable triggerable) {
		this.triggerable = triggerable;
		this.registrar = registrar;
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		if (this.enabled == enabled) {
			// already in requested state
			// so we can ignore the call
			return;
		}
		this.enabled = enabled;
		if (enabled) {
			enable();
		} else {
			disable();
		}
	}
	
	@Override
	public boolean isEnabled() {
		return enabled;
	}
	
	protected abstract void enable();
	protected abstract void disable();

}
