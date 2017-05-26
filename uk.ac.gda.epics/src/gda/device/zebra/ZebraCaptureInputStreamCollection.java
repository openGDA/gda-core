package gda.device.zebra;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.scannable.PositionInputStream;
import gda.epics.ReadOnlyPV;

/**
 * Represents a single one-off collection from an Epics time series array, or arrays that hang off the same
 * control PVs.
 */
class ZebraCaptureInputStreamCollection implements PositionInputStream<Double> {
	private static final Logger logger = LoggerFactory.getLogger(ZebraCaptureInputStreamCollection.class);

//	private final PV<Integer> tsNumPointsPV;

	private final ReadOnlyPV<Integer> numDownloadedPV;

	private final ReadOnlyPV<Double[]> tsArrayPV;

	private int numPointsToCollect;

	private int numPointsReturned = 0;

	private Object completetionMonitor = new Object();

	private boolean complete = false;

	private boolean started = false;

	/**
	 * Create and start a time series collection.
	 *
	 */
	public ZebraCaptureInputStreamCollection(ReadOnlyPV<Integer> numDownloadedPV,  ReadOnlyPV<Double[]> tsArrayPV){
		this.numDownloadedPV = numDownloadedPV;
		this.tsArrayPV = tsArrayPV;
	}

	public void start(int numPointsToCollect) throws IOException {
		logger.trace("@{}.start({}) stack trace {}", Integer.toHexString(hashCode()),
				numPointsToCollect, Arrays.toString(Thread.currentThread().getStackTrace()));
		this.numPointsToCollect = numPointsToCollect;
		numDownloadedPV.setValueMonitoring(true);
		numPointsReturned=0;
		started = true;
	}

	public void waitForCompletion() throws InterruptedException {
		synchronized (completetionMonitor) {
			if (!complete) {
				completetionMonitor.wait();
			}
		}
	}

	private void collectionComplete() throws IOException {
		synchronized (completetionMonitor) {
			complete = true;
			completetionMonitor.notifyAll();
		}
		tidyup();
	}

	public boolean isComplete() {
		return complete;
	}

	public void stop() throws IOException {
		tidyup();
	}

	private void tidyup() throws IOException {
		numDownloadedPV.setValueMonitoring(false);
		numPointsReturned = 0;
		logger.trace("@{}.tidyup() Reset numPointsReturned, stack trace {}", Integer.toHexString(hashCode()),
				Arrays.toString(Thread.currentThread().getStackTrace()));
	}

	@Override
	public List<Double> read(int maxToRead) throws NoSuchElementException, InterruptedException, DeviceException {
		logger.trace("@{}.read({}) started {}, stack trace {}", Integer.toHexString(hashCode()),
				maxToRead, started, Arrays.toString(Thread.currentThread().getStackTrace()));
		while(!started){
			Thread.sleep(1000);
		}
		if (numPointsReturned >= numPointsToCollect) {
			throw new IllegalStateException("All " + numPointsToCollect + " points to collect have been read.");
		}
		int desiredPoint = numPointsReturned + 1;

		int numPointsAvailable=0;
		try {
			//numDownloadedPV.setValueMonitoring(true);
			final int numDownloaded = numDownloadedPV.get(); // force numDownloadedPV to go and get a new value
			logger.info("{} numDownloadedPV: {}, desiredPoint {}", numDownloadedPV.getPvName(), numDownloaded, desiredPoint);
			numPointsAvailable = numDownloadedPV.waitForValue(new GreaterThanOrEqualTo(desiredPoint), -1);
			logger.info("{} numPointsAvailable: {} Finished waiting", numDownloadedPV.getPvName(), numPointsAvailable);
		} catch (InterruptedException e) {
			throw new InterruptedException("Interupted while waiting for point: " + desiredPoint);
		} catch (Exception e) {
			throw new DeviceException("Problem while waiting for point: " + desiredPoint, e);
		}

		int numNewPoints = numPointsAvailable - numPointsReturned;
		// Below a 'point' is a list of doubles, one for each pv to read.
		List<Double> pointList = new ArrayList<Double>(numNewPoints);

		Double[] completeArray;
		try {
			//TODO Speak to Tom as this is a bug
			Thread.sleep(100); //allow time for array pv is to setup properly
			completeArray = tsArrayPV.get(numPointsAvailable);
		} catch (IOException e) {
			throw new DeviceException(e);
		}
		for (int i = 0; i < numNewPoints; i++) {
			pointList.add(completeArray[numPointsReturned + i]);
//			System.out.println("point read = " +completeArray[numPointsReturned + i]);
		}
		logger.trace("{} pointList: {}", tsArrayPV.getPvName(), pointList);

		numPointsReturned = numPointsAvailable;
		//The zebra may generate more points than expected so warn but finish
		if (numPointsReturned >= numPointsToCollect) {
			if(numPointsReturned > numPointsToCollect)
				logger.warn("Zebra produced more points (" +numPointsReturned + ") than expected (" + numPointsToCollect + ")");
			try {
				collectionComplete();
			} catch (IOException e) {
				throw new DeviceException(e);
			}
		}
		return pointList;
	}
}
