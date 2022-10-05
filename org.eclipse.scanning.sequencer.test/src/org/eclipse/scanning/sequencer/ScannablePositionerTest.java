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
import java.util.function.Consumer;

import org.eclipse.scanning.api.INameable;
import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.device.IScannableDeviceService;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.PositionEvent;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.event.IPositionListener;
import org.junit.Before;
import org.junit.Test;

public class ScannablePositionerTest implements Consumer<SimpleScannable> {

	private INameable scan = mock(INameable.class);
	private IScannableDeviceService deviceServ;
	private ScannablePositioner scanPositioner;
	
	private List<IScannable> abortedScannables;
	private List<IScannable<?>> usedScannables;
	
	private IScannable<?> firstScannable;
	private IScannable<?> secondScannable;
	private IScannable<?> thirdScannable;
	private IScannable<?> fourthScannable;
	private IScannable<?> fifthScannable;
	
	private IPositionListener listener = mock(IPositionListener.class);
	private IPosition position = mock(IPosition.class);

	@Before
	public void setUp() throws ScanningException {
		when(scan.getName()).thenReturn("Test solstice scan");
		deviceServ = mock(IScannableDeviceService.class);
		abortedScannables = new ArrayList<>();
		usedScannables = new ArrayList<>();
		scanPositioner = new ScannablePositioner(deviceServ, scan);	
		when(listener.positionWillPerform(any(PositionEvent.class))).thenReturn(true);
		firstScannable = new CallBackOnAbortScannable(1, "level1", 11, this);
		secondScannable = new CallBackOnAbortScannable(2, "level2a", 12, this);
		thirdScannable = new CallBackOnAbortScannable(2, "level2b", 13, this);
		fourthScannable = new CallBackOnAbortScannable(3, "level3", 14, this);
		fifthScannable = new CallBackOnAbortScannable(5, "level5", 15, this);
	}

	@Test
	// All Scannables have abort called when we call abort.
	public void abortTestWithMultipleScannables() throws ScanningException, InterruptedException {
		usedScannables.addAll(Arrays.asList(firstScannable, secondScannable, thirdScannable, fourthScannable, fifthScannable));
		initDeviceServ(usedScannables);
		scanPositioner.setScannables(usedScannables);
		setExpectedLocations();
		scanPositioner.run(position);
		scanPositioner.abort();
		checkAllScannablesAborted();
	}
	
	@Test
	// We make attempt to abort all Scannables, even if an earlier one throws an exception
	public void abortTestWithEarlyException() throws ScanningException, InterruptedException {
		final ScanningException scanningException = new ScanningException();
		firstScannable = new ThrowExceptionOnAbortScannable(1, "level1", 11, this, scanningException);
		usedScannables.addAll(Arrays.asList(firstScannable, secondScannable, thirdScannable, fourthScannable, fifthScannable));
		scanPositioner.setScannables((List<IScannable<?>>) usedScannables);
		initDeviceServ(usedScannables);
		setExpectedLocations();
		scanPositioner.run(position);
		scanPositioner.abort();
		checkAllScannablesAborted();
	}
	
	@Test
	public void onlyCallAbortOnCorrectScannables() throws ScanningException, InterruptedException {
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
	public void iscannableInProcessOfMovingAborted() throws ScanningException, InterruptedException {
		thirdScannable = new InprocessOfMovingScannable(2, "level2b", 13, this);
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

	private static class CallBackOnAbortScannable extends SimpleScannable {

		private Consumer<SimpleScannable> callBack;

		CallBackOnAbortScannable(int level, String name, double value, Consumer<SimpleScannable> callback) {
			super(level, name, value);
			this.callBack = callback;
		}

		@Override
		public void abort() throws ScanningException, InterruptedException {
			callBack.accept(this);
		}

	}

	private static class ThrowExceptionOnAbortScannable extends CallBackOnAbortScannable {

		private Exception thrownOnAbort;

		ThrowExceptionOnAbortScannable(int level, String name, double value, Consumer<SimpleScannable> callback,
				Exception throwOnAbort) {
			super(level, name, value, callback);
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

	@Override
	public void accept(SimpleScannable t) {
		abortedScannables.add(t);
	}
	
	/**
	 * Copy of {@link WaitingScannable} by Matt Dickie, with only the minimal
	 * functions we need- we simulate aborting while waiting for a move to complete,
	 * so don't need to make the call to complete.
	 * 
	 */
	private static class InprocessOfMovingScannable extends CallBackOnAbortScannable {
		
			private Semaphore semaphore = new Semaphore(1, true); // true to make semaphore fair, see javadoc

			InprocessOfMovingScannable(int level, String name, double value, Consumer<SimpleScannable> callback) throws InterruptedException {
				super(level, name, value, callback);
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
