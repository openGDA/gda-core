package uk.ac.diamond.daq.experiment.plan;

import java.math.BigDecimal;
import java.math.RoundingMode;

import uk.ac.diamond.daq.experiment.api.plan.IPlanRegistrar;
import uk.ac.diamond.daq.experiment.api.plan.ISampleEnvironmentVariable;

public class PositionTrigger extends SEVTrigger {
	
	private double triggerInterval;
	private double previousTrigger;

	PositionTrigger(IPlanRegistrar registrar, ISampleEnvironmentVariable sev, Runnable runnable, double triggerInterval) {
		super(registrar, sev, runnable);
		this.triggerInterval = triggerInterval;
	}

	@Override
	protected boolean evaluateTriggerCondition(double signal) {
		if (BigDecimal.valueOf(signal).subtract(BigDecimal.valueOf(previousTrigger)).abs().divide(BigDecimal.valueOf(triggerInterval), 5, RoundingMode.HALF_UP).compareTo(BigDecimal.ONE) < 0) {
			return false;
		}
		previousTrigger = signal;
		return true;
	}
	
	@Override
	protected void enable() {
		previousTrigger = getSEV().read();
		super.enable();
	}
	
	@Override
	public String toString() {
		return "PositionTrigger [SEV="+getSEV()+", runnable="+getRunnable()+", triggerInterval="+triggerInterval+"]";
	}

}
