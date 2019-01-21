package uk.ac.diamond.daq.experiment.plan;

import java.math.BigDecimal;

import uk.ac.diamond.daq.experiment.api.plan.IPlanRegistrar;
import uk.ac.diamond.daq.experiment.api.plan.ISampleEnvironmentVariable;
import uk.ac.diamond.daq.experiment.api.plan.ITrigger;
import uk.ac.diamond.daq.experiment.api.plan.Triggerable;

/**
 * Will trigger only once: when SEV signal is at triggerSignal ± tolerance. 
 */
public class SingleTrigger extends TriggerBase {
	
	private boolean hasTriggered = false;
	private BigDecimal lowerLimit;
	private BigDecimal upperLimit;

	/**
	 * Create an {@link ITrigger} that will trigger a {@code Triggerable} only once,
	 * when signal from {@code sev} = {@code triggerSignal} ± {@code tolerance}
	 * @param registrar to report to
	 * @param sev providing the triggering signal source
	 * @param triggerable to trigger 
	 * @param target signal from sev which should trigger us
	 * @param tolerance
	 */
	SingleTrigger(IPlanRegistrar registrar, ISampleEnvironmentVariable sev, Triggerable triggerable, double target, double tolerance) {
		super(registrar, triggerable, sev);
		BigDecimal position = BigDecimal.valueOf(target);
		BigDecimal positiveTolerance = BigDecimal.valueOf(tolerance).abs();
		lowerLimit = position.subtract(positiveTolerance);
		upperLimit = position.add(positiveTolerance);
	}

	@Override
	protected boolean evaluateTriggerCondition(double signal) {
		if (hasTriggered) return false;
		BigDecimal preciseSignal = BigDecimal.valueOf(signal);
		hasTriggered = preciseSignal.compareTo(lowerLimit) >=0 && preciseSignal.compareTo(upperLimit) <=0;
		return hasTriggered;
	}
	
	@Override
	public String toString() {
		return "SingleTrigger [SEV="+getSEV()+", runnable="+getTriggerable()
			+", triggering when "+lowerLimit.doubleValue()+" <= signal <= "+upperLimit.doubleValue()+"]";
	}

}
