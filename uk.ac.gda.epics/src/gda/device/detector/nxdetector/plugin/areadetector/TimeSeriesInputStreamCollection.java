package gda.device.detector.nxdetector.plugin.areadetector;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

import gda.device.DeviceException;
import gda.device.detector.areadetector.v18.NDStatsPVs;
import gda.device.detector.areadetector.v18.NDStatsPVs.TSAcquireCommands;
import gda.device.detector.areadetector.v18.NDStatsPVs.TSControlCommands;
import gda.device.detector.areadetector.v18.NDStatsPVs.TSReadCommands;
import gda.device.scannable.PositionInputStream;
import gda.epics.PV;
import gda.epics.ReadOnlyPV;

/**
 * Represents a single one-off collection from an Epics time series array, or arrays that hang off the same
 * control PVs.
 */
class TimeSeriesInputStreamCollection implements PositionInputStream<List<Double[]>> {

	private final PV<TSControlCommands> tsControlPV;

	private final PV<TSAcquireCommands> tsAcquirePV;

	private final PV<TSReadCommands> tsReadPV;

	private final PV<Integer> tsNumPointsPV;

	private final ReadOnlyPV<Integer> tsProfileSizeXPV;

	private final ReadOnlyPV<Integer> tsProfileSizeYPV;

	private final ReadOnlyPV<Integer> tsCurrentPointPV;

	private final List<ReadOnlyPV<Double[]>> tsArrayPVList;

	private final int numPointsToCollect;

	private int numPointsReturned = 0;

	private Object completetionMonitor = new Object();

	private boolean complete = false;

	private boolean legacyTSpvs = true;

	private Map<ReadOnlyPV<Double[]>,Integer> tsArrayPV2numPointsMap;

	private Map<ReadOnlyPV<Double[]>, Boolean> tsArrayPV2isProfileMap;

	/**
	 * Create and start a time series collection.
	 *
	 * @param tsControlPV
	 * @param tsNumPointsPV
	 * @param tsCurrentPointPV
	 * @param tsArrayPVList
	 * @param numPointsToCollect
	 * @throws IOException
	 */
	public TimeSeriesInputStreamCollection(PV<TSControlCommands> tsControlPV, PV<TSAcquireCommands> tsAcquirePV, PV<TSReadCommands> tsReadPV,
			PV<Integer> tsNumPointsPV, ReadOnlyPV<Integer> tsCurrentPointPV,
			ReadOnlyPV<Integer> tsProfileSizeXPV, ReadOnlyPV<Integer> tsProfileSizeYPV,
			List<ReadOnlyPV<Double[]>> tsArrayPVList, int numPointsToCollect, boolean legacyTSpvs)
			throws IOException {
		if (tsArrayPVList.isEmpty()) {
			throw new IllegalArgumentException("No stats to collect");
		}
		this.tsControlPV = tsControlPV;
		this.tsAcquirePV=tsAcquirePV;
		this.tsReadPV=tsReadPV;
		this.tsNumPointsPV = tsNumPointsPV;
		this.tsProfileSizeXPV = tsProfileSizeXPV;
		this.tsProfileSizeYPV = tsProfileSizeYPV;
		this.tsCurrentPointPV = tsCurrentPointPV;
		this.tsArrayPVList = tsArrayPVList;
		this.numPointsToCollect = numPointsToCollect;
		this.legacyTSpvs =legacyTSpvs;
		this.tsArrayPV2numPointsMap = new HashMap<ReadOnlyPV<Double[]>,Integer>();
		this.tsArrayPV2isProfileMap = new HashMap<ReadOnlyPV<Double[]>,Boolean>();
		start();
	}

	private void start() throws IOException {
		tsNumPointsPV.putWait(numPointsToCollect);
		tsNumPointsPV.setValueMonitoring(true);
		Integer configuredNumPoints = tsNumPointsPV.get();
		populatePVMaps();
		if (numPointsToCollect != configuredNumPoints) {
			throw new IllegalArgumentException(
					MessageFormat
							.format("The number of points requested ({0}) exceeds the maximum configured for this EPICS installation ({1}).",
									numPointsToCollect, configuredNumPoints));
		}
		//The PV names for Time Series Stat in EPICS changed in  release https://cars9.uchicago.edu/software/epics/NDPluginStats.html June 25, 2018
		//following changes are made to support both OLD PVs and NEW PVs in transition period when DLS EPICS area detector updates across beamlines
		if (legacyTSpvs) {
			//keep to support legacy STAT Time Series in which TSControl does not implement BUSY record in EPICS
			tsControlPV.putWait(TSControlCommands.ERASE_AND_START);
		} else {
			//see I06-639 for reasons of EPICS changes, see I06-683 for reasons of GDA changes
			//new Time Series TSAcquire PV implements BUSY record i.e. caput callback in EPICS
			tsAcquirePV.putNoWait(TSAcquireCommands.ACQUIRE);
		}
	}

	/*
	 * populate maps containing PVs and number of points to measure and isProfile flags
	 */
	private void populatePVMaps() throws IOException {
		for (ReadOnlyPV<Double[]> arrayPV : tsArrayPVList) {
			String pvName;
			boolean pvNameIsProfile;
			pvName = arrayPV.getPvName();
			pvNameIsProfile = Stream.of(NDStatsPVs.ProfilesStat.values()).map(NDStatsPVs.ProfilesStat::name).anyMatch(pvName::contains);
			tsArrayPV2isProfileMap.put(arrayPV, pvNameIsProfile);
			if (pvNameIsProfile) {
				// check if it is X or Y related
				Integer nPoints = (pvName.contains("X_RBV"))? tsProfileSizeXPV.get():tsProfileSizeYPV.get();
				/*
				 * every scan point is full array [Value1,Value2,...,Value_ProfileXSize] must be saved, not one value as other TS Stats!
				 */
				tsArrayPV2numPointsMap.put(arrayPV,nPoints);
			} else {
				/*
				 * first scan point is [Value1,0,0,0,...,0N] second scan point is [Value1,Value2,0,...,0N] last scan point is [Value1,Value2,...,ValueN]
				 */
				tsArrayPV2numPointsMap.put(arrayPV,numPointsToCollect);
			}
		}
	}

	public void waitForCompletion() throws InterruptedException {
		waitForCompletion(Integer.MAX_VALUE);
	}

	public void waitForCompletion(int framesAcquired) throws InterruptedException {
		synchronized (completetionMonitor) {
			while (!complete && numPointsReturned < framesAcquired) {
				completetionMonitor.wait(100);
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
		try {
			if (legacyTSpvs) {
				//keep to support existing STAT Time Series
				tsControlPV.putWait(TSControlCommands.STOP);
			} else {
				//see I06-639 for reasons of EPICS changes, see I06-683 for reasons of GDA changes
				tsAcquirePV.putNoWait(TSAcquireCommands.DONE);
			}
		} finally {
			tidyup();
		}
	}

	private void tidyup() throws IOException {
		tsNumPointsPV.setValueMonitoring(false);
	}

	@Override
	public List<List<Double[]>> read(int maxToRead) throws NoSuchElementException, InterruptedException, DeviceException {
		if (numPointsReturned >= numPointsToCollect) {
			throw new IllegalStateException("All " + numPointsToCollect + " points to collect have been read.");
		}
		int desiredPoint = numPointsReturned + 1;

		int numPointsAvailable;
		try {
			numPointsAvailable = tsCurrentPointPV.waitForValue(tsCurrentPoint -> tsCurrentPoint >= desiredPoint, -1);
		} catch (InterruptedIOException e) {
			throw new InterruptedException("Interupted while waiting for point: " + desiredPoint);
		} catch (Exception e) {
			throw new DeviceException("Problem while waiting for point: " + desiredPoint, e);
		}

		try {
			if (legacyTSpvs) {
				// Jon Thompson and Giles Knap have discovered that this works around pushing 1 to the TSRead not updating
				// the arrays.
				tsControlPV.putWait(TSControlCommands.READ);
			} else {
				//see I06-639 for reasons of EPICS changes, see I06-683 for reasons of GDA changes
				tsReadPV.putWait(TSReadCommands.READ);
			}
		} catch (IOException e1) {
			throw new DeviceException("Problem asking EPICS to read data up into PVs: " + desiredPoint, e1);
		}

		int numNewPoints = numPointsAvailable - numPointsReturned;
		// Below a 'point' is a list of doubles, one for each pv to read.
		List<List<Double[]>> pointList = new ArrayList<List<Double[]>>(numNewPoints);
		for (int i = 0; i < numNewPoints; i++) {
			pointList.add(new ArrayList<Double[]>());
		}

		// Readout series of new points from each array
		for (ReadOnlyPV<Double[]> arrayPV : tsArrayPVList) {
			Double[] completeArray;
			try {
				completeArray = arrayPV.get(tsArrayPV2numPointsMap.get(arrayPV));
			} catch (IOException e) {
				throw new DeviceException(e);
			}
			for (int i = 0; i < numNewPoints; i++) {
				if (tsArrayPV2isProfileMap.get(arrayPV)) {
					pointList.get(i).add(completeArray);
				} else {
					Double[] element = new Double[] {completeArray[numPointsReturned + i]};
					pointList.get(i).add(element);
				}
			}
		}

		numPointsReturned = numPointsAvailable;
		if (numPointsReturned == numPointsToCollect) {
			try {
				collectionComplete();
			} catch (IOException e) {
				throw new DeviceException(e);
			}
		}
		return pointList;
	}
}