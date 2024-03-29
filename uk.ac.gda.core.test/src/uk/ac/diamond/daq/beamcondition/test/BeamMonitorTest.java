/*-
 * Copyright © 2018 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.diamond.daq.beamcondition.test;

import static gda.jython.JythonStatus.IDLE;
import static gda.jython.JythonStatus.PAUSED;
import static gda.jython.JythonStatus.RUNNING;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.concurrent.Delayed;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import gda.jython.ICurrentScanController;
import gda.jython.IJythonServerStatusProvider;
import gda.jython.IScriptController;
import gda.jython.ITerminalPrinter;
import gda.jython.InterfaceProvider;
import gda.jython.JythonServerStatus;
import uk.ac.diamond.daq.beamcondition.BeamCondition;
import uk.ac.diamond.daq.beamcondition.BeamMonitor;
import uk.ac.diamond.daq.concurrent.Async;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class BeamMonitorTest {

	@Mock private ITerminalPrinter printer;
	@Mock private IJythonServerStatusProvider jythonServerStatus;
	@Mock private ICurrentScanController scanController;
	@Mock private IScriptController scriptController;

	@Mock private BeamCondition condition1;
	@Mock private BeamCondition condition2;
	@Mock private MockedStatic<Async> asyncMock;

	private BeamMonitor monitor;
	private MockFuture<Object> future;


	@BeforeEach
	public void setUp() throws Exception {
		Mockito.when(Async.scheduleWithFixedDelay(any(Runnable.class), anyLong(), anyLong(), any(TimeUnit.class)))
			.thenAnswer(i -> {
				future = new MockFuture<Object>(i);
				return future;
			});
		InterfaceProvider.setTerminalPrinterForTesting(printer);
		InterfaceProvider.setJythonServerStatusProviderForTesting(jythonServerStatus);
		InterfaceProvider.setCurrentScanControllerForTesting(scanController);
		InterfaceProvider.setScriptControllerForTesting(scriptController);
		when(condition1.beamOn()).thenReturn(true);
		when(condition2.beamOn()).thenReturn(true);
		when(condition1.toString()).thenReturn("condition1");
		when(condition2.toString()).thenReturn("condition2");
		monitor = new BeamMonitor(1L, MILLISECONDS);// Doesn't use these values as we're mocking Async
		monitor.setName("BeamMonitor");
		monitor.addCheck(condition1);
		monitor.addCheck(condition2);

	}

	@AfterEach
	public void tearDown() {
		monitor.shutdown();
		InterfaceProvider.setTerminalPrinterForTesting(null);
		InterfaceProvider.setJythonServerStatusProviderForTesting(null);
		InterfaceProvider.setCurrentScanControllerForTesting(null);
		InterfaceProvider.setScriptControllerForTesting(null);
		reset(printer, jythonServerStatus, scanController, scriptController);
	}

	@Test
	public void testProcessNotStartedOnConstruction() throws Exception {
		Mockito.verifyNoInteractions(Async.class);
	}

	@Test
	public void testAsyncProcessOnlyCalledOnceWhenMonitorStarts() throws Exception {
		monitor.on();
		monitor.off();
		monitor.on();
		monitor.off();

		Mockito.verify(Async.class, times(1));
		Async.scheduleWithFixedDelay(any(), eq(1L), eq(1L), eq(TimeUnit.MILLISECONDS));
		Mockito.verifyNoMoreInteractions(Async.class);
	}

	@Test
	public void testDefaultDelay() throws Exception {
		monitor = new BeamMonitor();
		monitor.on();

		Mockito.verify(Async.class, times(1));
		Async.scheduleWithFixedDelay(any(), eq(100L), eq(100L), eq(TimeUnit.MILLISECONDS));
		Mockito.verifyNoMoreInteractions(Async.class);
	}

	@Test
	public void testNothingHappensIfBeamIsOn() throws Exception {
		assertTrue(monitor.isBeamOn());
		monitor.on();
		future.call();
		verifyNoInteractions(jythonServerStatus);
		verifyNoInteractions(printer);
		verifyNoInteractions(scanController);
		verifyNoInteractions(scriptController);
	}

	@Test
	public void testNothingHappensIfMonitorIsOff() throws Exception {
		when(condition1.beamOn()).thenReturn(false);
		assertFalse(monitor.isBeamOn());
		// On and off to start background thread
		monitor.on();
		monitor.off();

		future.call();
		verifyNoInteractions(jythonServerStatus);
		verifyNoInteractions(printer);
		verifyNoInteractions(scanController);
		verifyNoInteractions(scriptController);
	}

	@Test
	public void testNothingHappensIfScanIsAlreadyPausedByUser() {
		when(condition1.beamOn()).thenReturn(false);
		when(jythonServerStatus.getJythonServerStatus()).thenReturn(new JythonServerStatus(IDLE, PAUSED));
		monitor.on();

		future.call();

		verifyNoInteractions(printer);
		verify(scanController, never()).pauseCurrentScan();
		verify(scriptController, never()).pauseCurrentScript();
	}

	@Test
	public void testNothingHappensIfScanIsAlreadyPausedByMonitor() {
		when(condition1.beamOn()).thenReturn(false);
		when(jythonServerStatus.getJythonServerStatus()).thenReturn(new JythonServerStatus(IDLE, RUNNING));
		monitor.on();

		future.call();
		verify(scanController).pauseCurrentScan();

		future.call();
		verifyNoMoreInteractions(scanController);
		verify(scriptController, never()).pauseCurrentScript();
	}

	@Test
	public void testNothingHappensIfUserResumesPausedScan() throws Exception {
		when(condition1.beamOn()).thenReturn(false);
		when(jythonServerStatus.getJythonServerStatus()).thenReturn(new JythonServerStatus(IDLE, RUNNING));
		monitor.on();

		future.call();
		verify(scanController).pauseCurrentScan();

		// beam comes back - scan is running
		when(condition1.beamOn()).thenReturn(true);
		future.call();

		verifyNoMoreInteractions(scanController);
		verify(scriptController, never()).pauseCurrentScript();
	}

	@Test
	public void testScanIsResumedWhenBeamReturns() throws Exception {
		when(jythonServerStatus.getJythonServerStatus()).thenReturn(new JythonServerStatus(IDLE, RUNNING));
		when(condition1.beamOn()).thenReturn(false);
		monitor.on();

		future.call();
		verify(scanController).pauseCurrentScan();
		verify(printer).print("Beam lost - pausing scan");

		when(jythonServerStatus.getJythonServerStatus()).thenReturn(new JythonServerStatus(IDLE, PAUSED));
		when(condition1.beamOn()).thenReturn(true);
		future.call();

		verify(scanController).resumeCurrentScan();
		verify(printer).print("Beam back - resuming");
		verifyNoInteractions(scriptController);
	}

	@Test
	public void testScanIsPausedIfBeamIsDropped() throws Exception {
		when(jythonServerStatus.getJythonServerStatus()).thenReturn(new JythonServerStatus(IDLE, RUNNING));
		when(condition1.beamOn()).thenReturn(false);
		monitor.on();

		future.call();

		verify(scanController).pauseCurrentScan();
		verifyNoInteractions(scriptController);
		verify(printer).print("Beam lost - pausing scan");
	}

	@Test
	public void testScanNotResumedIfUserPauses() throws Exception {
		/* If the user pauses the scan, the beam is dropped and then returned, the scan should remain paused */
		when(jythonServerStatus.getJythonServerStatus()).thenReturn(new JythonServerStatus(IDLE, PAUSED));
		monitor.on();

		future.call();

		verifyNoInteractions(scanController);
		verifyNoInteractions(scriptController);
	}

	/**
	 * <pre>
	 * - Scan is running
	 * - Beam is lost
	 * - Monitor paused scan
	 * - Monitor is disabled
	 *</pre>
	 * The monitor has paused the scan and should resume it when it is disabled.
	 * @throws Exception
	 */
	@Test
	public void testScanIsResumedIfMonitorDisabledWhilePausedByMonitor() throws Exception {
		when(jythonServerStatus.getJythonServerStatus()).thenReturn(new JythonServerStatus(IDLE, RUNNING));
		when(condition1.beamOn()).thenReturn(false);
		monitor.on();

		future.call(); // pauses scan
		verify(scanController).pauseCurrentScan();

		when(jythonServerStatus.getJythonServerStatus()).thenReturn(new JythonServerStatus(IDLE, PAUSED));
		monitor.off();

		verify(scanController).resumeCurrentScan();
	}

	@Test
	public void testScanIsNotResumedIfMonitorDisabledWhilePausedByUser() throws Exception {
		when(jythonServerStatus.getJythonServerStatus()).thenReturn(new JythonServerStatus(IDLE, PAUSED));
		monitor.on();
		monitor.off();

		verifyNoMoreInteractions(scanController);
	}

	/**
	 * <pre>
	 * - Scan is running
	 * - Beam is lost
	 * - Monitor paused scan
	 * - User resumes scan
	 * - Monitor is disabled
	 *</pre>
	 * The monitor thinks it has paused the scan but the scan is running. The monitor should not attempt
	 * to resume it.
	 * @throws Exception
	 */
	@Test
	public void testScanIsNotResumedIfMonitorDisabledAfterUserResumedScan() throws Exception {
		when(jythonServerStatus.getJythonServerStatus()).thenReturn(new JythonServerStatus(IDLE, RUNNING));
		when(condition1.beamOn()).thenReturn(false);
		monitor.on();

		future.call(); // pauses scan
		verify(scanController).pauseCurrentScan();

		monitor.off();

		verifyNoMoreInteractions(scanController);
	}


	@Test
	public void testScanNotPausedIfOnlyScriptIsRunning() throws Exception {
		when(jythonServerStatus.getJythonServerStatus()).thenReturn(new JythonServerStatus(RUNNING, IDLE, "script_name"));
		when(condition1.beamOn()).thenReturn(false);
		monitor.on();

		future.call();

		verifyNoInteractions(scanController);
	}

	@Test
	public void testScriptIsNotPausedIfScanIsRunning() throws Exception {
		when(jythonServerStatus.getJythonServerStatus()).thenReturn(new JythonServerStatus(RUNNING, IDLE, "script_name"));
		when(condition1.beamOn()).thenReturn(false);
		monitor.on();

		future.call();

		verifyNoInteractions(scanController);
	}

	@Test
	public void testBeamOffWhenConditionsAreFalse() throws Exception {
		when(condition2.beamOn()).thenReturn(false);
		assertFalse("Beam should be off if any condition is false", monitor.isBeamOn());
	}

	@Test
	public void testRemovedConditionsAreIgnored() throws Exception {
		when(condition1.beamOn()).thenReturn(false);
		monitor.removeCheck(condition1);
		assertTrue("Removed condition should be ignored", monitor.isBeamOn());
	}

	@Test
	public void testBeamOnWhenMonitorHasNoConditions() {
		when(condition1.beamOn()).thenReturn(false);
		when(condition2.beamOn()).thenReturn(false);
		monitor.clearChecks();
		verifyNoInteractions(condition1, condition2);
		assertTrue("Beam should be on if monitor has no conditions", monitor.isBeamOn());
	}

	/**
	 * <pre>
	 * - Beam is lost
	 * - Scan is paused
	 * - User resumes scan
	 * - Scan ends
	 * - Next scan starts
	 * - Beam is still down
	 * </pre>
	 * If a user manually resumes a scan paused by the monitor, the next scan should not be affected
	 * @throws Exception
	 */
	@Test
	public void testNextScanIsPausedWhenUserResumesScan() throws Exception {
		when(jythonServerStatus.getJythonServerStatus()).thenReturn(new JythonServerStatus(IDLE, RUNNING));
		when(condition1.beamOn()).thenReturn(false);
		monitor.on();

		future.call();

		when(jythonServerStatus.getJythonServerStatus()).thenReturn(new JythonServerStatus(IDLE, IDLE));
		future.call();

		when(jythonServerStatus.getJythonServerStatus()).thenReturn(new JythonServerStatus(IDLE, RUNNING));
		future.call();

		verify(scanController, times(2)).pauseCurrentScan();

	}

	@Test
	public void testToString() throws Exception {
		assertEquals("BeamMonitor: Off (Beam On)", monitor.toString());
		monitor.on();
		assertEquals("BeamMonitor: On (Beam On)", monitor.toString());
		when(condition1.beamOn()).thenReturn(false);
		assertEquals("BeamMonitor: On (Beam Off)", monitor.toString());
		monitor.off();
		assertEquals("BeamMonitor: Off (Beam Off)", monitor.toString());
	}

	@Test
	public void testDetailString() throws Exception {
		assertEquals("BeamMonitor\n    condition1\n    condition2", monitor.detail());
		when(condition1.beamOn()).thenReturn(false);
		when(condition1.toString()).thenReturn("condition1 - false");
		assertEquals("BeamMonitor\n    condition1 - false\n    condition2", monitor.detail());
	}

	@Test
	public void testShutdownStopsMonitor() throws Exception {
		monitor.on();
		monitor.shutdown();

		assertThat("Future should have been cancelled", future.isCancelled());
	}

	@Test
	public void testMonitorIsRestartedAfterClosing() throws Exception {
		monitor.on();
		monitor.shutdown();
		assertThat("Future should have been cancelled", future.isCancelled());
		monitor.on();
		assertThat("Future should be recreated when monitor is restarted", not(future.isCancelled()));
	}

	/**
	 * This is a hack to let us run what should be a scheduled task directly
	 *
	 * @param <T>
	 */
	private static class MockFuture<T> extends FutureTask<T> implements ScheduledFuture<T> {
		private Runnable method;
		private long delay;
		private long interval;
		private TimeUnit unit;
		public MockFuture(Runnable runnable, long delay, long interval, TimeUnit unit) {
			super(runnable, null);
			this.method = runnable;
			this.delay = delay;
			this.interval = interval;
			this.unit = unit;
		}
		public MockFuture(InvocationOnMock i) {
			this(
					i.getArgument(0),
					i.getArgument(1),
					i.getArgument(2),
					i.getArgument(3)
			);
		}
		public void call() {
			method.run();
		}
		@Override
		public long getDelay(TimeUnit unit) {
			return 0;
		}
		@Override
		public int compareTo(Delayed o) {
			return 0;
		}
	}
}
