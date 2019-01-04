package uk.ac.diamond.daq.experiment.plan;

import uk.ac.diamond.daq.experiment.api.plan.IPlanFactory;
import uk.ac.diamond.daq.experiment.api.plan.IPlanRegistrar;
import uk.ac.diamond.daq.experiment.api.plan.ISampleEnvironmentVariable;
import uk.ac.diamond.daq.experiment.api.plan.ISegment;
import uk.ac.diamond.daq.experiment.api.plan.ITrigger;
import uk.ac.diamond.daq.experiment.api.plan.LimitCondition;
import uk.ac.diamond.daq.experiment.api.plan.SEVSignal;

public class PlanFactory implements IPlanFactory {
	
	private IPlanRegistrar registrar;

	@Override
	public ISampleEnvironmentVariable addSEV(SEVSignal signalProvider) {
		return new SampleEnvironmentVariable(signalProvider);
	}

	@Override
	public ISegment addSegment(String name, long duration, ITrigger... triggers) {
		ISegment segment = new TimedSegment(registrar, duration);
		segment.setName(name);
		for (ITrigger trigger : triggers) segment.enable(trigger);
		return segment;
	}
	
	@Override
	public ISegment addSegment(String name, ISampleEnvironmentVariable sev, LimitCondition limit, ITrigger... triggers) {
		ISegment segment = new SEVSegment(registrar, sev, limit);
		segment.setName(name);
		for (ITrigger trigger : triggers) segment.enable(trigger);
		return segment;
	}

	@Override
	public ITrigger addTrigger(String name, ISampleEnvironmentVariable sev, Runnable runnable,	double triggerInterval) {
		ITrigger trigger = new PositionTrigger(registrar, sev, runnable, triggerInterval);
		trigger.setName(name);
		return trigger;
	}

	@Override
	public ITrigger addTrigger(String name, ISampleEnvironmentVariable sev, Runnable runnable,	double triggerSignal, double tolerance) {
		ITrigger trigger = new SingleFireTrigger(registrar, sev, runnable, triggerSignal, tolerance);
		trigger.setName(name);
		return trigger;
	}

	@Override
	public ITrigger addTimerTrigger(String name, Runnable runnable, long period) {
		ITrigger trigger = new TimedTrigger(registrar, runnable, period);
		trigger.setName(name);
		return trigger;
	}

	@Override
	public void setRegistrar(IPlanRegistrar registrar) {
		this.registrar = registrar;
	}
	
	public IPlanRegistrar getRegistrar() {
		return registrar;
	}

}
