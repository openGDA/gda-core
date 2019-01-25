package uk.ac.diamond.daq.experiment.plan;

import java.math.BigDecimal;
import java.math.RoundingMode;

import uk.ac.diamond.daq.experiment.api.plan.IPlanRegistrar;
import uk.ac.diamond.daq.experiment.api.plan.ISampleEnvironmentVariable;
import uk.ac.diamond.daq.experiment.api.plan.Triggerable;

public class RepeatingTrigger extends TriggerBase {
	
	private double interval;
	private double lastTriggeringSignal;

	RepeatingTrigger(IPlanRegistrar registrar, ISampleEnvironmentVariable sev, Triggerable triggerable, double interval) {
		super(registrar, triggerable, sev);
		this.interval = interval;
	}

	@Override
	protected boolean evaluateTriggerCondition(double signal) {
		if (BigDecimal.valueOf(signal).subtract(BigDecimal.valueOf(lastTriggeringSignal)).abs().divide(BigDecimal.valueOf(interval), 5, RoundingMode.HALF_UP).compareTo(BigDecimal.ONE) < 0) {
			return false;
		}
		lastTriggeringSignal = signal;
		return true;
	}
	
	@Override
	protected void enable() {
		lastTriggeringSignal = getSEV().read();
		super.enable();
	}
	
	@Override
	public String toString() {
		return "RepeatingTrigger [triggerable="+getTriggerable()+", SEV="+getSEV()+", interval="+interval+"]";
	}

}
