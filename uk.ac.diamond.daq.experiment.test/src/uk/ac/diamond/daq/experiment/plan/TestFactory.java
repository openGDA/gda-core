package uk.ac.diamond.daq.experiment.plan;

import java.util.function.DoubleSupplier;

import uk.ac.diamond.daq.experiment.api.plan.ISampleEnvironmentVariable;
import uk.ac.diamond.daq.experiment.api.plan.ITrigger;
import uk.ac.diamond.daq.experiment.api.plan.Triggerable;

/**
 * Factory that injects our mock implementations
 */
class TestFactory extends PlanFactory {

	private final ISampleEnvironmentVariable sev;

	public TestFactory(ISampleEnvironmentVariable sev) {
		this.sev = sev;
	}

	@Override
	public ISampleEnvironmentVariable addSEV(DoubleSupplier signalProvider) {
		return sev;
	}

	@Override
	public ITrigger addTrigger(String name, Triggerable triggerable, ISampleEnvironmentVariable sev,
			double triggerInterval) {
		ITrigger trigger = new DummySEVTrigger(name, getRegistrar(), triggerInterval, sev);
		trigger.setName(name);
		return trigger;
	}
}
