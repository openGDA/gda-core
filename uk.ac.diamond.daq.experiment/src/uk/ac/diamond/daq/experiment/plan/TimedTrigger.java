package uk.ac.diamond.daq.experiment.plan;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import uk.ac.diamond.daq.experiment.api.plan.IPlanRegistrar;
import uk.ac.diamond.daq.experiment.api.plan.ITrigger;
import uk.ac.diamond.daq.experiment.api.plan.Triggerable;


/**
 * This {@link ITrigger} simply triggers its runnable with a given period.
 *
 */
public class TimedTrigger extends TriggerBase {
	
	private long period; // ms
	
	private ScheduledExecutorService executorService;

	/**
	 * @param runnable to trigger periodically
	 * @param period in ms
	 */
	TimedTrigger(IPlanRegistrar registrar, Triggerable triggerable, long period) {
		super(registrar, triggerable);
		this.period = period;
	}
	
	@Override
	protected void enable() {
		double startTime = System.currentTimeMillis();
		executorService = Executors.newSingleThreadScheduledExecutor(runnable -> {
			Thread thread = new Thread(runnable);
			thread.setName("experiment.plan.timed.trigger");
			thread.setDaemon(true);
			return thread;
		});
		
		executorService.scheduleAtFixedRate(()->{
			double triggeringTime = System.currentTimeMillis()- startTime;
			registrar.triggerOccurred(this, triggeringTime);
			triggerable.trigger();
		}, period, period, TimeUnit.MILLISECONDS);
	}
	
	@Override
	protected void disable() {
		executorService.shutdownNow();
	}
	
	@Override
	public String toString() {
		return "TimedTrigger [runnable = "+triggerable+", triggerInterval = "+period+" ms]";
	}

}
