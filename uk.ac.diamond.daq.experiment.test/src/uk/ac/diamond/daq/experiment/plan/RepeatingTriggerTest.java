package uk.ac.diamond.daq.experiment.plan;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import uk.ac.diamond.daq.experiment.api.plan.IPlanRegistrar;
import uk.ac.diamond.daq.experiment.api.plan.ISampleEnvironmentVariable;
import uk.ac.diamond.daq.experiment.api.plan.Triggerable;

public class RepeatingTriggerTest {

	private double initialSignal = 0.0;
	private double interval = 0.3;
	private double smallOffset = 1e-4;

	private RepeatingTrigger trigger;

	@Mock
	private IPlanRegistrar registrar;

	@Mock
	private ISampleEnvironmentVariable sev;

	@Mock
	private Triggerable triggerable;

	@Rule
	public MockitoRule setupMocks = MockitoJUnit.rule();

	@Before
	public void setupTrigger() {
		when(sev.read()).thenReturn(initialSignal);
		trigger = new RepeatingTrigger(registrar, sev, triggerable, interval);
		trigger.enable(); // caches initial signal
	}

	@Test
	public void doesNotTriggerBeforeInterval() {
		var triggered = trigger.evaluateTriggerCondition(initialSignal + interval -smallOffset);
		assertFalse(triggered);
	}

	@Test
	public void triggersWithExactInterval() {
		var triggered = trigger.evaluateTriggerCondition(initialSignal + interval);
		assertTrue(triggered);
	}

	@Test
	public void triggersWhenIntervalExceeded() {
		var triggered = trigger.evaluateTriggerCondition(initialSignal + interval + smallOffset);
		assertTrue(triggered);
	}

	/**
	 * Will trigger whenever the absolute difference between given signal
	 * and previous triggering signal is equal or greater than interval
	 */
	@Test
	public void multipleTriggers() {

		// signal not large enough compared to initial signal
		double signal = initialSignal + interval / 2;
		assertFalse(trigger.evaluateTriggerCondition(signal));

		// greater than interval
		signal += 2 * interval;
		assertTrue(trigger.evaluateTriggerCondition(signal));

		// large enough (sign not important)
		signal -= interval * 1.5;
		assertTrue(trigger.evaluateTriggerCondition(signal));

		// not quite large enough
		signal += interval - smallOffset;
		assertFalse(trigger.evaluateTriggerCondition(signal));

		// exact required interval
		signal += smallOffset;
		assertTrue(trigger.evaluateTriggerCondition(signal));

	}



}
