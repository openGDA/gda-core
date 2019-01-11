package uk.ac.diamond.daq.experiment.plan;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

import uk.ac.diamond.daq.experiment.api.plan.ISampleEnvironmentVariable;
import uk.ac.diamond.daq.experiment.api.plan.SEVListener;
import uk.ac.diamond.daq.experiment.api.plan.SEVSignal;

public class SampleEnvironmentVariableTest {

	private ISampleEnvironmentVariable sev;

	@Before
	public void setUp() {
		SEVSignal signalProvider = SEVSignalSims.linearEvolution(0, 0.1);
		sev = new SampleEnvironmentVariable(signalProvider);
	}

	@Test
	public void disabledWhenNooneListening() {
		assertFalse(sev.isEnabled());
		assertEquals(0.2, sev.read(), 1e-8); // 1 call on sev instantiation, the other one now.
	}

	@Test
	public void enabledWhenSomeoneListens() throws InterruptedException {
		LatchSEVListener countDownSEVListener = new LatchSEVListener();
		sev.addListener(countDownSEVListener);
		countDownSEVListener.await();
		assertTrue(sev.isEnabled());
	}

	@Test
	public void switchOffWhenLastListenerRemoved() {
		addNListeners(2);
		removeListeners();
		assertFalse(sev.isEnabled());
	}

	private void addNListeners(int n) {
		SEVListener listener = mock(SEVListener.class); // only need one regardless of n
		for (int i=0; i<n; i++) sev.addListener(listener);
	}

	private void removeListeners() {
		for (SEVListener listener : sev.getListeners().toArray(new SEVListener[sev.getListeners().size()])) sev.removeListener(listener);
	}

	double sensorSignal = 0;
	int sevBroadcastCounts = 0;

	/**
	 * This method sets sensorSignal to the given signal and returns once SEV broadcasts the change
	 * (or once LatchSEVListener times out if the signal was not broadcast)
	 */
	private void changeSignal(ISampleEnvironmentVariable sev, double signal) throws InterruptedException {
		LatchSEVListener listener = new LatchSEVListener();
		sev.addListener(listener);
		sensorSignal = signal;
		boolean received = listener.await();
		if (!received) System.out.println("Did not receive signal " + signal);
		sev.removeListener(listener);
	}

	@Test
	public void toleranceTest() throws InterruptedException {

		double tolerance = 1e-4;  // changes in signal within this tolerance
								  // should not be broadcast by the SEV

		SampleEnvironmentVariable envVar = new SampleEnvironmentVariable(()->sensorSignal, tolerance);
		envVar.addListener(new SEVListener() {

			@Override
			public void signalChanged(double signal) {
				sevBroadcastCounts++;
			}
		});

		changeSignal(envVar, 1); // broadcast here
		changeSignal(envVar, 2); // here
		changeSignal(envVar, sensorSignal + tolerance / 2.0); // below tolerance - don't broadcast
		changeSignal(envVar, 0); // and broadcast here

		assertThat(sevBroadcastCounts, is(3));
	}

	/**
	 * For tests that require waiting for signals to reach the listeners,
	 * an instance of this listener can be added to the SEV.
	 *
	 * After the signal changes, call await() which returns either when:
	 *  - the SEV-sampled signal reaches this listener
	 *  - the timeout is reached with no broadcast from the SEV
	 *
	 * This avoids the use of Thread.sleep(...)
	 */
	private class LatchSEVListener implements SEVListener {

		private CountDownLatch latch;

		private LatchSEVListener() {
			latch = new CountDownLatch(1);
		}

		@Override
		public void signalChanged(double signal) {
			latch.countDown();
		}

		private boolean await() throws InterruptedException {
			return latch.await(25, TimeUnit.MILLISECONDS);
		}
	}

}
