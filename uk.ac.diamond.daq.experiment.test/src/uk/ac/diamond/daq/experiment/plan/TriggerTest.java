package uk.ac.diamond.daq.experiment.plan;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import uk.ac.diamond.daq.experiment.api.plan.IPlanRegistrar;
import uk.ac.diamond.daq.experiment.api.plan.ISampleEnvironmentVariable;
import uk.ac.diamond.daq.experiment.api.plan.ITrigger;

public class TriggerTest {

	private class TriggerCounter {

		private CountDownLatch countdown;
		private int target;
		private int events;

		public TriggerCounter(int target) {
			reset(target);
		}

		public void eventTriggered() {
			events++;
			countdown.countDown();
		}

		public void await() throws InterruptedException {
			countdown.await(target*400, TimeUnit.MILLISECONDS);
		}

		public int getCount() {
			return events;
		}

		public void reset(int target) {
			this.target = target;
			countdown = new CountDownLatch(target);
			events = 0;
		}
	}

	private Plan ep = mock(Plan.class);

	@Test
	public void timedTriggerHasReasonableDuration() throws InterruptedException {
		TriggerCounter triggerCounter = new TriggerCounter(2);
		ITrigger timedTrigger = new TimedTrigger(ep, triggerCounter::eventTriggered, 50);
		Instant startTime = Instant.now();
		timedTrigger.setEnabled(true);
		triggerCounter.await();
		Duration duration = Duration.between(startTime, Instant.now());
		double durationInMilliSeconds = duration.toMillis();

		// 2 triggers at 50 ms each +/- 15%
		assertThat(durationInMilliSeconds, is(closeTo(100, 15)));
	}

	@Test
	public void testPositionTrigger() throws InterruptedException {
		TriggerCounter triggerCounter = new TriggerCounter(10);
		SEVTrigger trigger = new PositionTrigger(ep, getDummySEV(1.0), triggerCounter::eventTriggered, 0.1);

		trigger.setEnabled(true);

		for (int i=100; i >= 0; i--) {
			double signal = (double) i/100;
			trigger.signalChanged(signal);
		}
		triggerCounter.await();
		assertEquals(10, triggerCounter.getCount());
	}

	@Test
	public void testPositionTriggerAwkwardValues() throws InterruptedException {
		TriggerCounter triggerCounter = new TriggerCounter(6);

		SEVTrigger trigger = new PositionTrigger(ep, getDummySEV(1.94), triggerCounter::eventTriggered, 0.16);

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

		triggerCounter.await();
		assertEquals(6, triggerCounter.getCount());
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

		triggerCounter.await();
		assertEquals(1, triggerCounter.getCount()); // only fired once
		assertEquals(signalWhichShouldFire, trigger.triggeringSignal, tolerance);
	}

	/**
	 * Same as SingleFireTrigger, but recording the triggering signal
	 */
	class OneShotWithMemory extends SingleFireTrigger {

		OneShotWithMemory(IPlanRegistrar registrar, ISampleEnvironmentVariable sev, Runnable runnable, double triggerSignal, double tolerance) {
			super(registrar, sev, runnable, triggerSignal, tolerance);
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
		SEVTrigger trigger = new PositionTrigger(ep, getDummySEV(0), triggerCounter::eventTriggered, 2);

		trigger.setEnabled(true);
		for (int i=1;i<7;i++) trigger.signalChanged(i);
		triggerCounter.await();
		trigger.setEnabled(false);
		assertEquals(3, triggerCounter.getCount());

		triggerCounter.reset(5);

		trigger.setEnabled(true);
		for (int i=1;i<11;i++) trigger.signalChanged(i);
		triggerCounter.await();
		assertEquals(5, triggerCounter.getCount());
	}

	@Test
	public void enabledTriggerShouldIgnoreEnableCall() {
		ITrigger trigger = new PositionTrigger(ep, getDummySEV(0), ()-> {}, 9);
		trigger.setEnabled(true);
		trigger.setEnabled(true);
	}

	@Test
	public void disabledTriggerShouldIgnoreDisableCall() {
		ITrigger trigger = new TimedTrigger(ep, ()->{}, 1); // disabled on instantiation
		trigger.setEnabled(false);
	}

	@Test
	public void timedTriggerShouldStopWhenDisabled() throws InterruptedException {
		TriggerCounter triggerCounter = new TriggerCounter(3);

		ITrigger trigger = new TimedTrigger(ep, triggerCounter::eventTriggered, 15);

		Instant startTime = Instant.now();
		trigger.setEnabled(true);

		triggerCounter.await();
		trigger.setEnabled(false);

		Duration duration = Duration.between(startTime, Instant.now());
		int count = triggerCounter.getCount();
		double actualDuration = duration.toMillis();

		assertThat(count, is(3));
		assertThat(actualDuration, is(closeTo(45, 14)));

		Thread.sleep(50);	// NOSONAR we're not waiting for anything in particular to complete:
						 	// should the trigger -not- have stopped, we would expect a few extra events

		assertEquals("Trigger disabled, but still running!", count, triggerCounter.getCount());
	}

}
