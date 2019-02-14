package uk.ac.diamond.daq.experiment.plan;

import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.scan.ScanRequest;

import uk.ac.diamond.daq.experiment.api.plan.IPlanFactory;
import uk.ac.diamond.daq.experiment.api.plan.IPlanRegistrar;
import uk.ac.diamond.daq.experiment.api.plan.ISampleEnvironmentVariable;
import uk.ac.diamond.daq.experiment.api.plan.ISegment;
import uk.ac.diamond.daq.experiment.api.plan.ITrigger;
import uk.ac.diamond.daq.experiment.api.plan.LimitCondition;
import uk.ac.diamond.daq.experiment.api.plan.SignalSource;
import uk.ac.diamond.daq.experiment.api.plan.Triggerable;

public class PlanFactory implements IPlanFactory {
	
	private static IEventService eventService;

	private IPlanRegistrar registrar;
	
	private ISampleEnvironmentVariable timer;

	@Override
	public ISampleEnvironmentVariable addSEV(SignalSource signalProvider) {
		return new SampleEnvironmentVariable(signalProvider);
	}
	
	@Override
	public ISampleEnvironmentVariable addTimer() {
		if (timer == null) timer = new SampleEnvironmentVariable(new SystemTimerSignal());
		return timer;
	}

	@Override
	public ISegment addSegment(String name, ISampleEnvironmentVariable sev, double duration, ITrigger... triggers) {
		ISegment segment = new FixedDurationSegment(registrar, sev, duration);
		segment.setName(name);
		for (ITrigger trigger : triggers) segment.enable(trigger);
		return segment;
	}
	
	@Override
	public ISegment addSegment(String name, ISampleEnvironmentVariable sev, LimitCondition limit, ITrigger... triggers) {
		ISegment segment = new SimpleSegment(registrar, sev, limit);
		segment.setName(name);
		for (ITrigger trigger : triggers) segment.enable(trigger);
		return segment;
	}

	@Override
	public ITrigger addTrigger(String name, Triggerable triggerable, ISampleEnvironmentVariable sev, double target,	double tolerance) {
		ITrigger trigger;
		if (sev.equals(timer)) {
			trigger = new SingleTimeBasedTrigger(registrar, sev, triggerable, target, tolerance);
		} else {
			trigger = new SingleTrigger(registrar, sev, triggerable, target, tolerance);
		}
		trigger.setName(name);
		return trigger;
	}

	@Override
	public ITrigger addTrigger(String name, ScanRequest<?> scanRequest, ISampleEnvironmentVariable sev, double target, double tolerance) {
		Triggerable triggerable = new TriggerableScan(scanRequest, false, eventService);
		return addTrigger(name, triggerable, sev, target, tolerance);
	}

	@Override
	public ITrigger addTrigger(String name, ScanRequest<?> scanRequest, boolean importantScan,
			ISampleEnvironmentVariable sev, double target, double tolerance) {
		Triggerable triggerable = new TriggerableScan(scanRequest, importantScan, eventService);
		return addTrigger(name, triggerable, sev, target, tolerance);
	}

	@Override
	public ITrigger addTrigger(String name, Triggerable triggerable, ISampleEnvironmentVariable sev, double interval) {
		ITrigger trigger = new RepeatingTrigger(registrar, sev, triggerable, interval);
		trigger.setName(name);
		return trigger;
	}

	@Override
	public ITrigger addTrigger(String name, ScanRequest<?> scanRequest, ISampleEnvironmentVariable sev,
			double interval) {
		Triggerable triggerable = new TriggerableScan(scanRequest, false, eventService);
		return addTrigger(name, triggerable, sev, interval);
	}

	@Override
	public ITrigger addTrigger(String name, ScanRequest<?> scanRequest, boolean importantScan,
			ISampleEnvironmentVariable sev, double interval) {
		Triggerable triggerable = new TriggerableScan(scanRequest, importantScan, eventService);
		return addTrigger(name, triggerable, sev, interval);
	}

	@Override
	public void setRegistrar(IPlanRegistrar registrar) {
		this.registrar = registrar;
	}
	
	public IPlanRegistrar getRegistrar() {
		return registrar;
	}
	
	public void setEventService(IEventService service) {
		eventService = service;
	}
}
