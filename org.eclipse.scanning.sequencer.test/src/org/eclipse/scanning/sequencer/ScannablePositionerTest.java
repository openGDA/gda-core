package org.eclipse.scanning.sequencer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Semaphore;

import org.eclipse.scanning.api.INameable;
import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.device.IScannableDeviceService;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.PositionEvent;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.event.IPositionListener;
import org.eclipse.scanning.test.util.WaitingScannable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ScannablePositionerTest {

	private INameable scan = mock(INameable.class);
	private IScannableDeviceService deviceServ;
	private ScannablePositioner scanPositioner;
	
	private List<IScannable<?>> abortedScannables;
	private List<IScannable<?>> usedScannables;
	
	private IScannable<?> firstScannable;
	private IScannable<?> secondScannable;
	private IScannable<?> thirdScannable;
	private IScannable<?> fourthScannable;
	private IScannable<?> fifthScannable;
	
	private IPositionListener listener = mock(IPositionListener.class);
	private IPosition position = mock(IPosition.class);

	@BeforeEach
	public void setUp() throws ScanningException {
		when(scan.getName()).thenReturn("Test solstice scan");
		deviceServ = mock(IScannableDeviceService.class);
		abortedScannables = new ArrayList<>();
		usedScannables = new ArrayList<>();
		scanPositioner = new ScannablePositioner(deviceServ, scan);	
		when(listener.positionWillPerform(any(PositionEvent.class))).thenReturn(true);
		firstScannable = new AbortableScannable(1, "level1", 11);
		secondScannable = new AbortableScannable(2, "level2a", 12);
		thirdScannable = new AbortableScannable(2, "level2b", 13);
		fourthScannable = new AbortableScannable(3, "level3", 14);
		fifthScannable = new AbortableScannable(5, "level5", 15);
	}

	@Test
	void abortTestWithMultipleScannables() throws ScanningException, InterruptedException {
		// All Scannables have abort called when we call abort.
		usedScannables.addAll(Arrays.asList(firstScannable, secondScannable, thirdScannable, fourthScannable, fifthScannable));
		initDeviceServ(usedScannables);
		scanPositioner.setScannables(usedScannables);
		setExpectedLocations();
		scanPositioner.run(position);
		scanPositioner.abort();
		checkAllScannablesAborted();
	}

	@Test
	void abortTestWithEarlyException() throws ScanningException, InterruptedException {
		// We make attempt to abort all Scannables, even if an earlier one throws an exception
		final ScanningException scanningException = new ScanningException();
		firstScannable = new ThrowExceptionOnAbortScannable(1, "level1", 11, scanningException);
		usedScannables.addAll(Arrays.asList(firstScannable, secondScannable, thirdScannable, fourthScannable, fifthScannable));
		scanPositioner.setScannables((List<IScannable<?>>) usedScannables);
		initDeviceServ(usedScannables);
		setExpectedLocations();
		scanPositioner.run(position);
		scanPositioner.abort();
		checkAllScannablesAborted();
	}

	@Test
	void onlyCallAbortOnCorrectScannables() throws ScanningException, InterruptedException {
		final List<IScannable<?>> allScannables = Arrays.asList(firstScannable, secondScannable, thirdScannable, fourthScannable, fifthScannable);
		usedScannables.addAll(Arrays.asList(firstScannable, secondScannable));
		initDeviceServ(allScannables);
		scanPositioner.setScannables(usedScannables);
		setExpectedLocations();
		scanPositioner.run(position);
		scanPositioner.abort();
		checkAllScannablesAborted();
	}
	
	@Test
	void iscannableInProcessOfMovingAborted() throws ScanningException, InterruptedException {
		thirdScannable = new InprocessOfMovingScannable(2, "level2b", 13);
		usedScannables.addAll(Arrays.asList(firstScannable, secondScannable, thirdScannable));
		initDeviceServ(usedScannables);
		scanPositioner.setScannables(usedScannables);
		setExpectedLocations();
		scanPositioner.run(position);
		scanPositioner.abort();
		checkAllScannablesAborted();
	}

	private void checkAllScannablesAborted() {
		// Each IScannable is added to the list when abort is called on it - we want to
		// make sure we have each one just once, but don't care about the order.
		assertThat(abortedScannables.containsAll(usedScannables), is(true));
		assertThat(usedScannables.size(), is(equalTo(abortedScannables.size())));
	}

	private void setExpectedLocations() throws ScanningException {
		for (IScannable<?> scannable : usedScannables) {
			when(position.get(scannable.getName())).thenReturn(((double) scannable.getPosition()) + 1);
		}
	}

	private void initDeviceServ(List<IScannable<?>> scannables) throws ScanningException {
		for (IScannable scannable : scannables) {
			when(deviceServ.getScannable(scannable.getName())).thenReturn(scannable);
		}
	}

	public void addAbortedScannable(SimpleScannable t) {
		abortedScannables.add(t);
	}

	private class AbortableScannable extends SimpleScannable {

		AbortableScannable(int level, String name, double value) {
			super(level, name, value);
		}

		@Override
		public void abort() throws ScanningException, InterruptedException {
			addAbortedScannable(this);
		}

	}

	private class ThrowExceptionOnAbortScannable extends AbortableScannable {

		private Exception thrownOnAbort;

		ThrowExceptionOnAbortScannable(int level, String name, double value, Exception throwOnAbort) {
			super(level, name, value);
			this.thrownOnAbort = throwOnAbort;
		}

		@Override
		public void abort() throws ScanningException, InterruptedException {
			super.abort();
			if (thrownOnAbort instanceof ScanningException)
				throw (ScanningException) thrownOnAbort;
			if (thrownOnAbort instanceof InterruptedException)
				throw (InterruptedException) thrownOnAbort;
			throw new ScanningException(thrownOnAbort);
		}

	}

	/**
	 * Copy of {@link WaitingScannable} by Matt Dickie, with only the minimal
	 * functions we need- we simulate aborting while waiting for a move to complete,
	 * so don't need to make the call to complete.
	 * 
	 */
	private class InprocessOfMovingScannable extends AbortableScannable {
		
			private Semaphore semaphore = new Semaphore(1, true); // true to make semaphore fair, see javadoc

			InprocessOfMovingScannable(int level, String name, double value) throws InterruptedException {
				super(level, name, value);
				semaphore.acquire();
			}

			@Override
			public Double setPosition(Double position, IPosition loc) throws ScanningException {
				try {
					semaphore.release(); // Notify waiting thread
					semaphore.acquire(); // Wait to be notified ourselves, note this only works because the semaphore is fair
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt(); // shouldn't happen in test code
					throw new ScanningException(e);
				}

				return super.setPosition(position, loc);
			}
	}

}
