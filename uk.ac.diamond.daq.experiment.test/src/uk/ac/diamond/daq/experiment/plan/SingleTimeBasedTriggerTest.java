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

public class SingleTimeBasedTriggerTest {

	private double startTime = 1234;
	private double target = 10;
	private double tolerance = 0.2;

	private SingleTimeBasedTrigger trigger;

	@Mock
	private IPlanRegistrar registrar;

	@Mock
	private ISampleEnvironmentVariable sev;

	@Mock
	private Triggerable triggerable;

	@Rule
	public MockitoRule setupMocks = MockitoJUnit.rule();

	@Before
	public void setStartTimeAndCreateTrigger() {
		when(sev.read()).thenReturn(startTime);
		trigger = new SingleTimeBasedTrigger(registrar, sev, triggerable, target, tolerance);

		trigger.enable(); // performs initial read of sev
	}

	@Test
	public void doesNotTriggerEarly() {
		// one second too early
		var triggered = trigger.evaluateTriggerCondition(startTime + target - tolerance - 1);
		assertFalse(triggered);
	}

	@Test
	public void doesNotTriggerLate() {
		// one second too late
		var triggered = trigger.evaluateTriggerCondition(startTime + target + tolerance + 1);
		assertFalse(triggered);
	}

	@Test
	public void triggersWithinTolerance() {
		var triggered = trigger.evaluateTriggerCondition(startTime + target - tolerance);
		assertTrue(triggered);
	}

	@Test
	public void triggersOnlyOnce() {
		var first = trigger.evaluateTriggerCondition(startTime + target - tolerance);
		var second = trigger.evaluateTriggerCondition(startTime + target);

		assertTrue(first);
		assertFalse(second);
	}

}
