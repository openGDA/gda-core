package uk.ac.diamond.daq.experiment.plan;

import uk.ac.diamond.daq.experiment.api.plan.IPlanRegistrar;
import uk.ac.diamond.daq.experiment.api.plan.ISampleEnvironmentVariable;
import uk.ac.diamond.daq.experiment.api.plan.LimitCondition;

public class SimpleSegment extends SegmentBase {
	
	private LimitCondition limitCondition;
	
	SimpleSegment(IPlanRegistrar registrar, ISampleEnvironmentVariable sev, LimitCondition limitCondition) {
		super(registrar, sev);
		this.limitCondition = limitCondition;
	}
	
	@Override
	boolean shouldTerminate(double signal) {
		return limitCondition.limitReached(signal);
	}
	
	@Override
	public String toString() {
		return "SimpleSegment [name=" + getName() + ", triggers=" + getTriggers() + 
				", sev=" + sev + ", limitCondition=" + limitCondition + "]";
	}

}
