package uk.ac.diamond.daq.experiment.plan;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.scanning.api.event.IdBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.experiment.api.plan.IPlanRegistrar;
import uk.ac.diamond.daq.experiment.api.plan.ISampleEnvironmentVariable;
import uk.ac.diamond.daq.experiment.api.plan.ITrigger;
import uk.ac.diamond.daq.experiment.api.plan.Triggerable;
import uk.ac.diamond.daq.experiment.api.plan.event.TriggerEvent;

public abstract class TriggerBase implements ITrigger {
	
	private static final Logger logger = LoggerFactory.getLogger(TriggerBase.class);
	
	private String name;
	private final Triggerable triggerable;
	private final ISampleEnvironmentVariable sev;	
	private final IPlanRegistrar registrar;
	
	private ExecutorService executorService;	
	private boolean enabled;
	private volatile boolean evaluating;

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
	
	public String getSampleEnvironmentName() {
		return sev.getName();
	}
	
	@Override
	public synchronized void signalChanged(double signal) {
		if (evaluating) {
			logger.debug("Signal {} ignored by trigger '{}' because it is currently evaluating a previous one", signal, getName());
		} else {
			evaluating = true;
			try {
				if (evaluateTriggerCondition(signal)) { // FIXME all implementations should be purely functional
														// to move this outside synchronised method
					logger.debug("Trigger '{}' now triggering due to signal {}", getName(), signal);
					executorService.execute(()->{
						final TriggerEvent event = new TriggerEvent(signal);
						registrar.triggerOccurred(this);
						try {
							final Object id = triggerable.trigger();
							
							if (id != null && id instanceof IdBean) {
								event.setId(((IdBean) id).getUniqueId());
							}
							
						} catch (Exception e) {
							logger.error("Problem while executing trigger '{}'", getName(), e);
							event.setFailed(true);
						} finally {
							registrar.triggerComplete(this, event, getSEV().getName());
						}
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
