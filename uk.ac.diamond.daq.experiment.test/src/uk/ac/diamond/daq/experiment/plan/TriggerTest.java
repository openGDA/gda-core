package uk.ac.diamond.daq.experiment.plan;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import uk.ac.diamond.daq.experiment.api.plan.IPlanRegistrar;
import uk.ac.diamond.daq.experiment.api.plan.ISampleEnvironmentVariable;
import uk.ac.diamond.daq.experiment.api.plan.ITrigger;
import uk.ac.diamond.daq.experiment.api.plan.Triggerable;

public class TriggerTest {

	private class TriggerCounter {

		private CountDownLatch countdown;
		private int target;

		public TriggerCounter(int target) {
			reset(target);
		}

		public void eventTriggered() {
			countdown.countDown();
		}

		public boolean await() throws InterruptedException {
			return countdown.await(target*50, TimeUnit.MILLISECONDS);
		}

		public void reset(int target) {
			this.target = target;
			countdown = new CountDownLatch(target);
		}
	}

	private Plan ep = mock(Plan.class);

	@Test
	public void testPositionTrigger() throws InterruptedException {
		TriggerCounter triggerCounter = new TriggerCounter(10);
		ITrigger trigger = new RepeatingTrigger(ep, getDummySEV(1.0), triggerCounter::eventTriggered, 0.1);

		trigger.setEnabled(true);

		for (int i=100; i >= 0; i--) {
			double signal = (double) i/100;
			trigger.signalChanged(signal);
		}
		boolean expectedEventsRecorded = triggerCounter.await();
		assertAllEventsCaptured(expectedEventsRecorded);
	}

	@Test
	public void testPositionTriggerAwkwardValues() throws InterruptedException {
		TriggerCounter triggerCounter = new TriggerCounter(6);

		ITrigger trigger = new RepeatingTrigger(ep, getDummySEV(1.94), triggerCounter::eventTriggered, 0.16);

		double[] signals = new double[] {1.94, // initial value
										 1.74, // delta = 0.2 - trigger!
										 1.7,
										 1.59,
										 1.58, // 0.16 since last trigger - trigger!
										 1.42, // trigger!
										 1.0,  // trigger!
										 0.9,
										 0.88,
										 0.76, // trigger!
										 0.0   // trigger!
									};

		trigger.setEnabled(true);

		for (double signal : signals) {
			trigger.signalChanged(signal);
		}

		boolean expectedEventsRecorded = triggerCounter.await();
		assertAllEventsCaptured(expectedEventsRecorded);
	}

	private ISampleEnvironmentVariable getDummySEV(double startingValue) {
		return new SampleEnvironmentVariable(()->startingValue);
	}

	@Test
	public void testSingleFireTrigger() throws InterruptedException {

		TriggerCounter triggerCounter = new TriggerCounter(1);

		double signalWhichShouldFire = 0.617;
		double tolerance = 0.01;
		OneShotWithMemory trigger = new OneShotWithMemory(ep, getDummySEV(0.0), triggerCounter::eventTriggered, signalWhichShouldFire, tolerance);
		trigger.setEnabled(true);

		for (int i=0; i<=200; i++) {
			double signal = (double) i/100;
			trigger.signalChanged(signal);
		}

		boolean expectedEventsRecorded = triggerCounter.await();
		assertAllEventsCaptured(expectedEventsRecorded);
		assertEquals(signalWhichShouldFire, trigger.triggeringSignal, tolerance);
	}

	/**
	 * Same as SingleFireTrigger, but recording the triggering signal
	 */
	class OneShotWithMemory extends SingleTrigger {

		OneShotWithMemory(IPlanRegistrar registrar, ISampleEnvironmentVariable sev, Triggerable triggerable, double triggerSignal, double tolerance) {
			super(registrar, sev, triggerable, triggerSignal, tolerance);
		}

		double triggeringSignal;

		@Override
		protected boolean evaluateTriggerCondition(double signal) {
			boolean willTrigger = super.evaluateTriggerCondition(signal);
			if (willTrigger) triggeringSignal = signal;
			return willTrigger;
		}

	}

	@Test
	public void sameTriggerInMultipleSegments() throws InterruptedException {
		// When a segment ends, it disables the trigger
		// a later segment should be able to reenable the same trigger
		TriggerCounter triggerCounter = new TriggerCounter(3);
		ITrigger trigger = new RepeatingTrigger(ep, getDummySEV(0), triggerCounter::eventTriggered, 2);

		trigger.setEnabled(true);
		for (int i=1;i<7;i++) trigger.signalChanged(i);
		boolean expectedEventsRecorded = triggerCounter.await();
		trigger.setEnabled(false);
		assertAllEventsCaptured(expectedEventsRecorded);

		triggerCounter.reset(5);

		trigger.setEnabled(true);
		for (int i=1;i<11;i++) trigger.signalChanged(i);
		expectedEventsRecorded = triggerCounter.await();
		assertAllEventsCaptured(expectedEventsRecorded);
	}

	@Test
	public void enabledTriggerShouldIgnoreEnableCall() {
		ITrigger trigger = new RepeatingTrigger(ep, getDummySEV(0), ()-> {}, 9);
		trigger.setEnabled(true);
		trigger.setEnabled(true);
	}

	@Test
	public void disabledTriggerShouldIgnoreDisableCall() {
		ITrigger trigger = new RepeatingTrigger(ep, null, ()->{}, 1);
		trigger.setEnabled(false);
	}

	private void assertAllEventsCaptured(boolean allCaptured) {
		assertThat("Timed out before expected events were recorded", allCaptured, is(true));
	}
}
