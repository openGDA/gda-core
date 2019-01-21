package uk.ac.diamond.daq.experiment.plan;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import uk.ac.diamond.daq.experiment.api.plan.IPlanRegistrar;
import uk.ac.diamond.daq.experiment.api.plan.ISampleEnvironmentVariable;
import uk.ac.diamond.daq.experiment.api.plan.ITrigger;
import uk.ac.diamond.daq.experiment.api.plan.Triggerable;

public abstract class TriggerBase implements ITrigger {
	
	private String name;
	private final Triggerable triggerable;
	private final ISampleEnvironmentVariable sev;	
	private final IPlanRegistrar registrar;
	
	private ExecutorService executorService;	
	private boolean enabled;

	TriggerBase(IPlanRegistrar registrar, Triggerable triggerable, ISampleEnvironmentVariable sev) {
		this.registrar = registrar;
		this.triggerable = triggerable;
		this.sev = sev;
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
	
	protected void enable() {
		executorService = Executors.newSingleThreadExecutor();
		sev.addListener(this);
	}
	
	protected void disable() {
		sev.removeListener(this);
		executorService.shutdownNow();
	}
	
	protected ISampleEnvironmentVariable getSEV() {
		return sev;
	}
	
	protected Triggerable getTriggerable() {
		return triggerable;
	}
	
	@Override
	public void signalChanged(double signal) {
		if (evaluateTriggerCondition(signal)) {
			executorService.execute(()->{
				registrar.triggerOccurred(this, signal);
				triggerable.trigger();
			});
		}
	}
	
	/**
	 * Decide whether the signal spat out by the SEV should trigger us.
	 * @param signal
	 * @return
	 */
	protected abstract boolean evaluateTriggerCondition(double signal);

}
