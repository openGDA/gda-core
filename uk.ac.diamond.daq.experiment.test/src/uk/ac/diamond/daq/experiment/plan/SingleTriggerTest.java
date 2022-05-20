package uk.ac.diamond.daq.experiment.plan;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import uk.ac.diamond.daq.experiment.api.plan.IPlanRegistrar;
import uk.ac.diamond.daq.experiment.api.plan.ISampleEnvironmentVariable;
import uk.ac.diamond.daq.experiment.api.plan.Triggerable;

public class SingleTriggerTest {

	private double target = 5.0;
	private double tolerance = 0.01;
	private double tinyOffset = 1e-9;

	private SingleTrigger trigger;

	@Mock
	private IPlanRegistrar registrar;

	@Mock
	private ISampleEnvironmentVariable sev;

	@Mock
	private Triggerable triggerable;

	@Rule
	public MockitoRule setupMocks = MockitoJUnit.rule();

	@Before
	public void createTrigger() {
		trigger = new SingleTrigger(registrar, sev, triggerable, target, tolerance);
	}

	@Test
	public void doesNotTriggerBelowLowerBound() {
		var shouldTrigger = trigger.evaluateTriggerCondition(target - tolerance - tinyOffset);
		assertFalse(shouldTrigger);
	}

	@Test
	public void doesNotTriggerAboveUpperBound() {
		var shouldTrigger = trigger.evaluateTriggerCondition(target + tolerance + tinyOffset);
		assertFalse(shouldTrigger);
	}

	@Test
	public void triggersAtLowerBound() {
		var shouldTrigger = trigger.evaluateTriggerCondition(target - tolerance);
		assertTrue(shouldTrigger);
	}

	@Test
	public void triggersAtUpperBound() {
		var shouldTrigger = trigger.evaluateTriggerCondition(target + tolerance);
		assertTrue(shouldTrigger);
	}

	@Test
	public void triggersOnlyOnce() {
		var firstTime = trigger.evaluateTriggerCondition(target);
		var secondTime = trigger.evaluateTriggerCondition(target);
		assertTrue(firstTime);
		assertFalse(secondTime);
	}

	/**
	 * This trigger triggers only once since being enabled,
	 * but can be used in multiple segments, being enabled
	 * at the start of a segment and disabled at the end.
	 */
	@Test
	public void canBeReenabled() {
		trigger.enable();
		assertTrue(trigger.evaluateTriggerCondition(target));
		assertFalse(trigger.evaluateTriggerCondition(target));
		trigger.disable();
		trigger.enable();
		assertTrue(trigger.evaluateTriggerCondition(target + tolerance));
	}

}
