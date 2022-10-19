package uk.ac.diamond.daq.experiment.plan;

import java.util.function.DoubleSupplier;

import gda.device.Scannable;
import uk.ac.diamond.daq.experiment.api.plan.IPlanFactory;
import uk.ac.diamond.daq.experiment.api.plan.IPlanRegistrar;
import uk.ac.diamond.daq.experiment.api.plan.ISampleEnvironmentVariable;
import uk.ac.diamond.daq.experiment.api.plan.ISegment;
import uk.ac.diamond.daq.experiment.api.plan.ITrigger;
import uk.ac.diamond.daq.experiment.api.plan.LimitCondition;
import uk.ac.diamond.daq.experiment.api.plan.Payload;
import uk.ac.diamond.daq.experiment.api.plan.PayloadService;
import uk.ac.diamond.daq.experiment.plan.trigger.RepeatingTrigger;
import uk.ac.diamond.daq.experiment.plan.trigger.SingleTimeBasedTrigger;
import uk.ac.diamond.daq.experiment.plan.trigger.SingleTrigger;
import uk.ac.gda.core.tool.spring.SpringApplicationContextFacade;

public class PlanFactory implements IPlanFactory {
	
	private static final String SYSTEM_TIMER_NAME = "System timer";

	private IPlanRegistrar registrar;
	
	private static ISampleEnvironmentVariable timer;
	
	private PayloadService payloadService;

	@Override
	public ISampleEnvironmentVariable addSEV(Scannable scannable) {
		return new SampleEnvironmentVariable(scannable);
	}
	
	@Override
	public ISampleEnvironmentVariable addSEV(DoubleSupplier signalSource) {
		return new SampleEnvironmentVariable(signalSource);
	}
	
	public static ISampleEnvironmentVariable getSystemTimer() {
		if (timer == null) {
			SampleEnvironmentVariable systemTimer = new SampleEnvironmentVariable(new SystemTimerSignal());
			systemTimer.setName(SYSTEM_TIMER_NAME);
			timer = systemTimer;
		}
		return timer;
	}
	
	@Override
	public ISampleEnvironmentVariable addTimer() {
		return getSystemTimer();
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
	public ITrigger addTrigger(String name, Payload payload, ISampleEnvironmentVariable sev, double target,	double tolerance) {
		ITrigger trigger;
		if (sev.equals(timer)) {
			trigger = new SingleTimeBasedTrigger(registrar, sev, payload, target, tolerance);
		} else {
			trigger = new SingleTrigger(registrar, sev, payload, target, tolerance);
		}
		trigger.setName(name);
		return trigger;
	}
	
	@Override
	public ITrigger addTrigger(String name, Object payload, ISampleEnvironmentVariable sev, double target,	double tolerance) {
		return addTrigger(name, getPayloadService().wrap(payload), sev, target, tolerance);
	}

	@Override
	public ITrigger addTrigger(String name, Payload payload, ISampleEnvironmentVariable sev, double interval) {
		ITrigger trigger = new RepeatingTrigger(registrar, sev, payload, interval);
		trigger.setName(name);
		return trigger;
	}
	
	@Override
	public ITrigger addTrigger(String name, Object payload, ISampleEnvironmentVariable sev, double interval) {
		return addTrigger(name, getPayloadService().wrap(payload), sev, interval);
	}

	@Override
	public void setRegistrar(IPlanRegistrar registrar) {
		this.registrar = registrar;
	}
	
	public IPlanRegistrar getRegistrar() {
		return registrar;
	}
	
	private PayloadService getPayloadService() {
		if (payloadService == null) {
			payloadService = SpringApplicationContextFacade.getBean(PayloadService.class);
		}
		return payloadService;
	}
}
