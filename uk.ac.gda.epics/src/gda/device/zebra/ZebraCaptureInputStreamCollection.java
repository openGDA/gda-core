package gda.device.zebra;

import gda.device.DeviceException;
import gda.device.scannable.PositionInputStream;
import gda.epics.ReadOnlyPV;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a single one-off collection from an Epics time series array, or arrays that hang off the same
 * control PVs. 
 */
class ZebraCaptureInputStreamCollection implements PositionInputStream<Double> {
	private static final Logger logger = LoggerFactory.getLogger(ZebraCaptureInputStreamCollection.class);

//	private final PV<Integer> tsNumPointsPV;

	private final ReadOnlyPV<Integer> numCapturePV;

	private final ReadOnlyPV<Double[]> tsArrayPV;

	private int numPointsToCollect;

	private int numPointsReturned = 0;

	private Object completetionMonitor = new Object();

	private boolean complete = false;

	private boolean started = false;

	/**
	 * Create and start a time series collection.
	 * 
	 * @param numCapturePV
	 * @param tsArrayPV
	 */
	public ZebraCaptureInputStreamCollection(	ReadOnlyPV<Integer> numCapturePV, ReadOnlyPV<Double[]> tsArrayPV){
		this.numCapturePV = numCapturePV;
		this.tsArrayPV = tsArrayPV;
	}

	public void start(int numPointsToCollect) throws IOException {
		this.numPointsToCollect = numPointsToCollect;
		numCapturePV.setValueMonitoring(true);
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
		numCapturePV.setValueMonitoring(false);
	}

	@Override
	public List<Double> read(int maxToRead) throws NoSuchElementException, InterruptedException, DeviceException {
		while(!started){
			Thread.sleep(1000);
		}
		if (numPointsReturned >= numPointsToCollect) {
			throw new IllegalStateException("All " + numPointsToCollect + " points to collect have been read.");
		}
		int desiredPoint = numPointsReturned + 1;

		int numPointsAvailable;
		try {
			numPointsAvailable = numCapturePV.waitForValue(new GreaterThanOrEqualTo(desiredPoint), -1);
		} catch (InterruptedIOException e) {
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
			Thread.sleep(1000); //allow time for array pv is to setup properly 
			completeArray = tsArrayPV.get();
		} catch (IOException e) {
			throw new DeviceException(e);
		}
//		System.out.println("read maxToRead="+maxToRead);
		for (int i = 0; i < numNewPoints; i++) {
			pointList.add(completeArray[numPointsReturned + i]);
//			System.out.println("point read = " +completeArray[numPointsReturned + i]);
		}

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