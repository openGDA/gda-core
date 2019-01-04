package uk.ac.diamond.daq.experiment.plan;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import uk.ac.diamond.daq.experiment.api.plan.IPlanRegistrar;
import uk.ac.diamond.daq.experiment.api.plan.ISampleEnvironmentVariable;
import uk.ac.diamond.daq.experiment.api.plan.SEVListener;

public abstract class SEVTrigger extends TriggerBase implements SEVListener {
	
	private ISampleEnvironmentVariable sev;
	
	private ExecutorService executorService;
	
	SEVTrigger(IPlanRegistrar registrar, ISampleEnvironmentVariable sev, Runnable runnable) {
		super(registrar, runnable);
		this.sev = sev;
	}
	
	@Override
	protected void enable() {
		executorService = Executors.newSingleThreadExecutor();
		sev.addListener(this);
	}
	
	@Override
	protected void disable() {
		sev.removeListener(this);
		executorService.shutdownNow();
	}
	
	@Override
	public void signalChanged(double signal) {
		if (evaluateTriggerCondition(signal)) {
			executorService.execute(()->{
				registrar.triggerOccurred(this, signal);
				runnable.run();
			});
		}
	}
	
	protected ISampleEnvironmentVariable getSEV() {
		return sev;
	}
	
	protected Runnable getRunnable() {
		return runnable;
	}
	
	/**
	 * Decide whether the signal spat out by the SEV should trigger us.
	 * @param signal
	 * @return
	 */
	protected abstract boolean evaluateTriggerCondition(double signal);
	
	

}
