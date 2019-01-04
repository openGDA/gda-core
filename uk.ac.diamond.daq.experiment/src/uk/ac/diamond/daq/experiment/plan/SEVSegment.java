package uk.ac.diamond.daq.experiment.plan;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.experiment.api.plan.IPlanRegistrar;
import uk.ac.diamond.daq.experiment.api.plan.ISampleEnvironmentVariable;
import uk.ac.diamond.daq.experiment.api.plan.ISegment;
import uk.ac.diamond.daq.experiment.api.plan.LimitCondition;
import uk.ac.diamond.daq.experiment.api.plan.SEVListener;

/**
 * Concrete implementation of {@link ISegment} which evaluates {@link #limitCriterionMet(double)}
 * by testing a {@link LimitCondition} (a Predicate{@literal <Double>})
 */
public class SEVSegment extends AbstractSegment implements SEVListener {
	
	private static final Logger logger = LoggerFactory.getLogger(SEVSegment.class);

	private LimitCondition limitCondition;
	
	private ISampleEnvironmentVariable sev;

	SEVSegment(IPlanRegistrar registrar, ISampleEnvironmentVariable sev, LimitCondition limitCondition) {
		super(registrar);
		this.sev = sev;
		this.limitCondition = limitCondition;
	}
	
	@Override
	public void activate() {
		double startingSignal = sev.read();
		if (limitCondition.limitReached(startingSignal)) {
			logger.warn("Skipping Segment {} as limit condition met on activation", getName());
			terminateSegment(startingSignal);
		} else {
			logger.info("Segment '{}' activated", getName());
			sev.addListener(this);
			super.activate();
		}
	}
	
	private void terminateSegment(double signal) {
		logger.info("Segment '{}' terminated", getName());
		sev.removeListener(this);
		deactivate(signal);
	}
	
	@Override
	public void signalChanged(double signal) {
		if (limitCondition.limitReached(signal)) terminateSegment(signal);
	}

	@Override
	public String toString() {
		return "SEVSegment [SEV="+getSEV()+", enables="+getEnabledTriggers()+", limitCondition=" + limitCondition + "]";
	}
	
	protected ISampleEnvironmentVariable getSEV() {
		return sev;
	}

}
