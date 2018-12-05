package uk.ac.diamond.daq.experiment.plan;

import uk.ac.diamond.daq.experiment.api.plan.IPlanRegistrar;
import uk.ac.diamond.daq.experiment.api.plan.ITrigger;

public abstract class TriggerBase implements ITrigger {
	
	private String name;

	protected final Runnable runnable;
	protected IPlanRegistrar registrar;
	
	protected boolean enabled;

	TriggerBase(IPlanRegistrar registrar, Runnable runnable) {
		this.runnable = runnable;
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
