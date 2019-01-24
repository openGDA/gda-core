package uk.ac.diamond.daq.experiment.plan;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.experiment.api.plan.IPlanRegistrar;
import uk.ac.diamond.daq.experiment.api.plan.ISampleEnvironmentVariable;
import uk.ac.diamond.daq.experiment.api.plan.ITrigger;
import uk.ac.diamond.daq.experiment.api.plan.Triggerable;

public abstract class TriggerBase implements ITrigger {
	
	private static final Logger logger = LoggerFactory.getLogger(TriggerBase.class);
	
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
	
	private volatile boolean evaluating;
	
	@Override
	public synchronized void signalChanged(double signal) {
		if (evaluating) {
			logger.debug("Signal {} ignored by trigger '{}' because it is currently evaluating a previous one", signal, getName());
		} else {
			evaluating = true;
			try {
				if (evaluateTriggerCondition(signal)) { // FIXME all implementations should be purely functional
														// to move this outside synchronised method
					executorService.execute(()->{
						registrar.triggerOccurred(this, signal);
						triggerable.trigger();
					});
				}
			} finally {
				evaluating = false;
			}
		}
	}
	
	/**
	 * Determine whether the broadcasted signal should trigger us. 
	 * Called from a synchronised method so it should be fast.
	 */
	protected abstract boolean evaluateTriggerCondition(double signal);

}
