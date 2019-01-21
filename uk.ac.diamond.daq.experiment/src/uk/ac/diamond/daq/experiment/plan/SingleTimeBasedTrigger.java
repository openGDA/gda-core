package uk.ac.diamond.daq.experiment.plan;

import uk.ac.diamond.daq.experiment.api.plan.IPlanRegistrar;
import uk.ac.diamond.daq.experiment.api.plan.ISampleEnvironmentVariable;
import uk.ac.diamond.daq.experiment.api.plan.Triggerable;

/**
 * Provides simple absolute to relative conversion enabling {@link SingleTrigger} to operate
 * with a time {@link ISampleEnvironmentVariable}.
 * <p>
 * Example: "trigger this triggerable 5 seconds after the start of this segment"
 */
public class SingleTimeBasedTrigger extends SingleTrigger {

	private double startTime;
	
	SingleTimeBasedTrigger(IPlanRegistrar registrar, ISampleEnvironmentVariable sev, Triggerable triggerable,
			double target, double tolerance) {
		super(registrar, sev, triggerable, target, tolerance);
	}
	
	@Override
	protected void enable() {
		startTime = getSEV().read();
		super.enable();
	}
	
	@Override
	protected boolean evaluateTriggerCondition(double signal) {
		return super.evaluateTriggerCondition(signal - startTime);
	}

}
