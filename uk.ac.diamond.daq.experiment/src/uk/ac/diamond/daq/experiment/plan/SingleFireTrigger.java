package uk.ac.diamond.daq.experiment.plan;

import java.math.BigDecimal;

import uk.ac.diamond.daq.experiment.api.plan.IPlanRegistrar;
import uk.ac.diamond.daq.experiment.api.plan.ISampleEnvironmentVariable;
import uk.ac.diamond.daq.experiment.api.plan.Triggerable;

/**
 * Will trigger only once; when SEV signal is at triggerSignal ± tolerance. 
 *
 */
public class SingleFireTrigger extends SEVTrigger {
	
	private boolean hasTriggered = false;
	private BigDecimal lowerLimit;
	private BigDecimal upperLimit;

	/**
	 * Create an ITriggeringPoint that will trigger an {@code runnable} only once,
	 * when signal from {@code sev} = {@code triggerSignal} ± {@code tolerance}
	 * @param sev whose sampled signal this trigger listens to
	 * @param runnable 
	 * @param triggerSignal signal from sev which should trigger us
	 * @param tolerance
	 */
	SingleFireTrigger(IPlanRegistrar registrar, ISampleEnvironmentVariable sev, Triggerable triggerable, double triggerSignal, double tolerance) {
		super(registrar, sev, triggerable);
		BigDecimal position = BigDecimal.valueOf(triggerSignal);
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
		return "SingleFireTrigger [SEV="+getSEV()+", runnable="+getTriggerable()
			+", triggering when "+lowerLimit.doubleValue()+" <= signal <= "+upperLimit.doubleValue()+"]";
	}

}
