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

package uk.ac.gda.devices.detector.xspress4;
import static uk.ac.gda.devices.detector.xspress4.XspressPvName.ACQUIRE_TIME_TEMPLATE;
import static uk.ac.gda.devices.detector.xspress4.XspressPvName.ARRAY_COUNTER;
import static uk.ac.gda.devices.detector.xspress4.XspressPvName.ARRAY_COUNTER_RBV;
import static uk.ac.gda.devices.detector.xspress4.XspressPvName.ARRAY_DATA_TEMPLATE;
import static uk.ac.gda.devices.detector.xspress4.XspressPvName.DTC_ENERGY_KEV;
import static uk.ac.gda.devices.detector.xspress4.XspressPvName.DTC_FACTORS;
import static uk.ac.gda.devices.detector.xspress4.XspressPvName.RES_GRADE_TEMPLATE;
import static uk.ac.gda.devices.detector.xspress4.XspressPvName.ROI_RES_GRADE_BIN;
import static uk.ac.gda.devices.detector.xspress4.XspressPvName.SCA_ARRAY_TEMPLATE;
import static uk.ac.gda.devices.detector.xspress4.XspressPvName.SCA_TEMPLATE;
import static uk.ac.gda.devices.detector.xspress4.XspressPvName.SCA_TIMESERIES_ACQUIRE_TEMPLATE;
import static uk.ac.gda.devices.detector.xspress4.XspressPvName.SCA_TIMESERIES_CURRENTPOINT_TEMPLATE;
import static uk.ac.gda.devices.detector.xspress4.XspressPvName.SCA_TIMESERIES_TEMPLATE;
import static uk.ac.gda.devices.detector.xspress4.XspressPvName.TRIGGER_MODE_TEMPLATE;
import static uk.ac.gda.devices.detector.xspress4.XspressPvName.UPDATE_ARRAYS_TEMPLATE;

import java.io.IOException;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import gda.device.DeviceException;
import gda.epics.LazyPVFactory;
import gda.epics.PV;
import gda.epics.ReadOnlyPV;
import gda.factory.FindableBase;

public class EpicsXspress4Controller extends FindableBase implements Xspress4Controller, InitializingBean {

	private static Logger logger = LoggerFactory.getLogger(EpicsXspress4Controller.class);

	private String pvBase = "";
	private int numElements = 64;
	private int numMcaChannels = 4096;
	private int numScalers = 8;

	private Map<XspressPvName, String> pvNameMap = new EnumMap<>(XspressPvName.class);

	// Standard scalers for each detector element, separate PV for each scaler type
	private ReadOnlyPV<Double>[][] pvForScalerValue = null; // [detectorElement][scalerNumber].scalerNumber=0...7

	// Standard scalers for each detector element, provided as an array
	private ReadOnlyPV<Double[]>[] pvForScalerArray = null; // [detectorElement]scalerNumber=0...7

	// Resolution grade values for each detector element (array of 16 in-window counts, one value for each resolution grade)
	private ReadOnlyPV<Double[]>[][] pvForResGradeArray = null; // [detector element][window number]

	// Array of MCA for each detector element (summed over all res grades)
	private ReadOnlyPV<Double[]>[] pvForMcaArray; // [detectorElement]

	// PV to cause all array data PVs above to be updated (e.g. caput “1” to this)
	private PV<Integer> pvUpdateArrays = null;

	// PV to set the exposure time (used for Software triggered collection)
	private PV<Double> pvAcquireTime = null;

	private PV<Integer> pvTriggerMode = null;

	private PV<Integer> pvArrayCounter = null;

	private ReadOnlyPV<Integer> pvArrayCounterRbv = null;

	private ReadOnlyPV<Double[]> pvDtcFactors = null;

	private PV<Integer> pvRoiResGradeBin = null;

	private PV<Double> pvDtcEnergyKev = null;

	// Time series value for detector element, scaler .
	private ReadOnlyPV<Double[]>[][] pvScalerTimeSeries = null; // [detectorElement][scalerNumber].scalerNumber=0...7

	private PV<Integer>[] pvScalerTimeSeriesAcquire = null; // [detectorElement]

	private ReadOnlyPV<Integer>[] pvScalerTimeSeriesCurrentPoint = null; // [detectorElement]

	private double caClientTimeoutSecs = 10.0; // Timeout for CACLient put operations (seconds)
	private long pollIntervalMillis = 50;

	/**
	 * Update the PV name map using new values passed in.
	 *
	 * @param nameMap key = logical name (i.e. matching a value in {@link XspressPvName}), value = PV name/pattern
	 */
	public void updatePvNameMap(Map<String, String> nameMap) {
		for(var ent : nameMap.entrySet()) {
			try {
				XspressPvName pvName = XspressPvName.valueOf(ent.getKey());
				 logger.info("Updating PV for {} : old value = {}, new value = {}", ent.getKey(), pvNameMap.get(pvName), ent.getValue());
				pvNameMap.put(pvName, ent.getValue());
			} catch(IllegalArgumentException e) {
				 logger.info("No PV for {} was found", ent.getKey());
			}
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		pvForScalerValue = new ReadOnlyPV[numElements][numScalers];
		pvForScalerArray = new ReadOnlyPV[numElements];
		pvForResGradeArray = new ReadOnlyPV[numElements][2];
		pvForMcaArray = new ReadOnlyPV[numElements];

		for (int element = 0; element < numElements; element++) {

			String pvnameScalerArray = pvBase + String.format(getPvName(SCA_ARRAY_TEMPLATE), element + 1);
			pvForScalerArray[element] = LazyPVFactory.newReadOnlyDoubleArrayPV(pvnameScalerArray);

			String pvnameResGrade = pvBase + String.format(getPvName(RES_GRADE_TEMPLATE), element + 1, 5);
			pvForResGradeArray[element][0] = LazyPVFactory.newReadOnlyDoubleArrayPV(pvnameResGrade);

			pvnameResGrade = pvBase + String.format(getPvName(RES_GRADE_TEMPLATE), element + 1, 6);
			pvForResGradeArray[element][1] = LazyPVFactory.newReadOnlyDoubleArrayPV(pvnameResGrade);

			String pvnameArrayData = pvBase + String.format(getPvName(ARRAY_DATA_TEMPLATE), element + 1);
			pvForMcaArray[element] = LazyPVFactory.newReadOnlyDoubleArrayPV(pvnameArrayData);

			for (int sca = 0; sca < numScalers; sca++) {
				String pvnameScaler = pvBase + String.format(getPvName(SCA_TEMPLATE), element + 1, sca);
				pvForScalerValue[element][sca] = LazyPVFactory.newReadOnlyDoublePV(pvnameScaler);
			}
		}

		pvTriggerMode = LazyPVFactory.newIntegerPV(pvBase + getPvName(TRIGGER_MODE_TEMPLATE));
		pvAcquireTime = LazyPVFactory.newDoublePV(pvBase + getPvName(ACQUIRE_TIME_TEMPLATE));
		pvUpdateArrays = LazyPVFactory.newIntegerPV(pvBase + getPvName(UPDATE_ARRAYS_TEMPLATE));
		pvArrayCounter = LazyPVFactory.newIntegerPV(pvBase + getPvName(ARRAY_COUNTER));
		pvArrayCounterRbv = LazyPVFactory.newReadOnlyIntegerPV(pvBase + getPvName(ARRAY_COUNTER_RBV));
		pvDtcFactors = LazyPVFactory.newReadOnlyDoubleArrayPV(pvBase + getPvName(DTC_FACTORS));
		pvRoiResGradeBin = LazyPVFactory.newIntegerPV(pvBase + getPvName(ROI_RES_GRADE_BIN));
		pvDtcEnergyKev = LazyPVFactory.newDoublePV(pvBase + getPvName(DTC_ENERGY_KEV));

		createTimeSeriesPVs();
	}


	private void createTimeSeriesPVs() {
		pvScalerTimeSeries = new ReadOnlyPV[numElements][numScalers];
		pvScalerTimeSeriesAcquire = new PV[numElements];
		pvScalerTimeSeriesCurrentPoint = new ReadOnlyPV[numElements];
		for (int element = 0; element < numElements; element++) {
			for (int sca = 0; sca < numScalers; sca++) {
				// scaler numbering for time series arrays starts at 1 not zero!
				String pvTimeSeriesName = pvBase + String.format(getPvName(SCA_TIMESERIES_TEMPLATE), element + 1, sca + 1);
				pvScalerTimeSeries[element][sca] = LazyPVFactory.newDoubleArrayPV(pvTimeSeriesName);
			}
			String pvTimeSeriesAcquireName = pvBase + String.format(getPvName(SCA_TIMESERIES_ACQUIRE_TEMPLATE), element + 1);
			pvScalerTimeSeriesAcquire[element] = LazyPVFactory.newIntegerPV(pvTimeSeriesAcquireName);

			String pvTimeSeriesCurrentPointName = pvBase + String.format(getPvName(SCA_TIMESERIES_CURRENTPOINT_TEMPLATE), element + 1);
			pvScalerTimeSeriesCurrentPoint[element] = LazyPVFactory.newIntegerPV(pvTimeSeriesCurrentPointName);
		}
	}

	/**
	 * Get name of PV from the name map.
	 * @param pv
	 * @return
	 */
	private String getPvName(XspressPvName pv) {
		return pvNameMap.computeIfAbsent(pv, XspressPvName::pvName);

	}

	public double getCaClientTimeout() {
		return caClientTimeoutSecs;
	}

	/**
	 * Timeout to use for channel access put/get operations (seconds)
	 * @param caClientTimeoutSecs
	 */
	public void setCaClientTimeout(double caClientTimeoutSecs) {
		this.caClientTimeoutSecs = caClientTimeoutSecs;
	}

	private <T> void checkPv(ReadOnlyPV<T> pv) throws DeviceException {
		Optional.ofNullable(pv).orElseThrow(() -> new DeviceException("PV has not been set"));
	}

	private double[] getArray(ReadOnlyPV<Double[]> pv) throws DeviceException {
		checkPv(pv);
		try {
			return ArrayUtils.toPrimitive(pv.get());
		}catch(IOException e) {
			throw new DeviceException(e);
		}
	}

	private double[] getArray(ReadOnlyPV<Double[]> pv, int numValues) throws DeviceException {
		checkPv(pv);
		try {
			return ArrayUtils.toPrimitive(pv.get(numValues));
		}catch(IOException e) {
			throw new DeviceException(e);
		}
	}

	private <T> T getValue(ReadOnlyPV<T> pv) throws DeviceException {
		checkPv(pv);
		try {
			return pv.get();
		}catch(IOException e) {
			throw new DeviceException(e);
		}
	}

	private <T> void putValue(PV<T> pv, T value) throws DeviceException {
		checkPv(pv);
		try {
			pv.putWait(value, caClientTimeoutSecs);
		}catch(IOException e) {
			throw new DeviceException(e);
		}
	}

	private <T> void putValueNoWait(PV<T> pv, T value) throws DeviceException {
		checkPv(pv);
		try {
			pv.putNoWait(value);
		}catch(IOException e) {
			throw new DeviceException(e);
		}
	}

	@Override
	public double getScalerValue(int element, int scalerNumber) throws DeviceException {
		return getValue(pvForScalerValue[element][scalerNumber]);
	}

	@Override
	public double[] getScalerArray(int element) throws DeviceException {
		return getArray(pvForScalerArray[element]);
	}

	@Override
	public double[][] getScalerTimeseries(int element, int startFrame, int endFrame) throws DeviceException {
		updateArrays();
		double[][] values = new double[numScalers][];
		for(int i=0; i<numScalers; i++) {
			double[] vals = getArray(pvScalerTimeSeries[element][i], endFrame+1);
			values[i] = Arrays.copyOfRange(vals, startFrame, vals.length);
		}
		return values;
	}

	/**
	 * Find index of first element in the array with value <= 0
	 * @param vals
	 * @return
	 */
	private int findIndexOfFirstZero(double[] vals) {
		int maxIndex = 0;
		while (maxIndex < vals.length && vals[maxIndex] > 0) {
			maxIndex++;
		}
		return maxIndex;
	}

	@Override
	public int getTimeSeriesNumPoints() throws DeviceException {
		// Read 'scaler 0' array values for all detector elements (i.e. the time frame lengths) and
		// find the number of available data points from the number of leading non-zero elements
		// (Time series arrays seem to not always be filled with number of data points reported by TSCurrentPoint...)
		int maxLength = Integer.MAX_VALUE;
		for(ReadOnlyPV<Double[]>[] pv : pvScalerTimeSeries) {
			double[] ar = getArray(pv[0]);
			maxLength = Math.min(maxLength, findIndexOfFirstZero(ar));
		}
		return maxLength;
//
//		int minNumPoints = Integer.MAX_VALUE;
//		for(ReadOnlyPV<Integer> pv : pvScalerTimeSeriesCurrentPoint) {
//			minNumPoints = Math.min(minNumPoints, getValue(pv));
//		}
//		return minNumPoints;
	}

	@Override
	public void startTimeSeries() throws DeviceException {
		for(int i=0; i<numElements; i++) {
			putValueNoWait(pvScalerTimeSeriesAcquire[i], 1);
		}
	}

	@Override
	public void stopTimeSeries() throws DeviceException {
		for(int i=0; i<numElements; i++) {
			putValueNoWait(pvScalerTimeSeriesAcquire[i], 0);
		}
	}

	@Override
	public double[] getResGradeArrays(int element, int window) throws DeviceException{
		return getArray(pvForResGradeArray[element][window]);
	}

	@Override
	public double[] getMcaData(int element) throws DeviceException {
		return getArray(pvForMcaArray[element]);
	}

	public void updateArrays() throws DeviceException {
		putValue(pvUpdateArrays, 1);
	}

	@Override
	public double[][] getMcaData() throws DeviceException {
		updateArrays();
		double[][] mcaData = new double[numElements][];
		for(int i=0; i<mcaData.length; i++) {
			mcaData[i] = getMcaData(i);
		}
		return mcaData;
	}

	@Override
	public void setTriggerMode(int triggerMode) throws DeviceException {
		putValue(pvTriggerMode, triggerMode);
	}

	@Override
	public int getTriggerMode() throws DeviceException {
		return getValue(pvTriggerMode);
	}

	@Override
	public boolean setSaveResolutionGradeData(boolean saveResGradeData) throws DeviceException {
		// Set the bin size for resolution grade dimension:
		// 		1 = save each grades ('Region of interest' readout mode).
		// 		16 = integrate over grades ('MCA', 'MCA + scalers' readout mode).
		int newNumBins = saveResGradeData ? 1 : 16;
		int currentNumBins = getValue(pvRoiResGradeBin);

		if (newNumBins != currentNumBins) {
			putValue(pvRoiResGradeBin, newNumBins);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void setDeadtimeCorrectionEnergy(double energyKev) throws DeviceException {
		putValue(pvDtcEnergyKev, energyKev);
	}

	@Override
	public double getDeadtimeCorrectionEnergy() throws DeviceException {
		return getValue(pvDtcEnergyKev);
	}

	@Override
	public void resetFramesReadOut() throws DeviceException {
		putValue(pvArrayCounter, 0);
	}

	@Override
	public int getTotalFramesAvailable() throws DeviceException {
		return getValue(pvArrayCounterRbv);
	}
	@Override
	public void setAcquireTime(double time) throws DeviceException {
		putValue(pvAcquireTime, time);
	}

	@Override
	public double[] getDeadtimeCorrectionFactors() throws DeviceException {
		 return getArray(pvDtcFactors);
	}

	public String getBasePv() {
		return pvBase;
	}

	public void setBasePv(String pvBase) {
		this.pvBase = pvBase;
	}

	@Override
	public int getNumElements() {
		return numElements;
	}

	@Override
	public void setNumElements(int numElements) {
		this.numElements = numElements;
	}

	@Override
	public int getNumScalers() {
		return numScalers;
	}

	@Override
	public void setNumScalers(int numScalers) {
		this.numScalers = numScalers;
	}

	/**
	 * Wait for the value returned by a PV to change. This function blocks until PV value != startValue,
	 * timeout is reached or an exception is thrown.
	 * @param pv
	 * @param startValue
	 * @param timeoutSecs
	 * @throws InterruptedException
	 * @throws DeviceException
	 */
	private <T> void waitForValueToChange(ReadOnlyPV<T> pv, T startValue, double timeoutSecs) throws  InterruptedException, DeviceException {
		long endTime = (long) timeoutSecs*1000 + System.currentTimeMillis();
		while( System.currentTimeMillis() < endTime && getValue(pv) == startValue) {
			Thread.sleep(pollIntervalMillis);
		}
	}

	/**
	 * Wait for the value returned by a PV to change. This function blocks until PV value != startValue,
	 * timeout is reached or an exception is thrown.
	 * @param pv
	 * @param startValue
	 * @throws InterruptedException
	 * @throws DeviceException
	 */
	private <T> void waitForValueToChange(ReadOnlyPV<T> pv, T startValue) throws  InterruptedException, DeviceException {
		waitForValueToChange(pv, startValue, caClientTimeoutSecs);
	}

	@Override
	public void waitForCounterToIncrement(int initialNumFrames, long timeoutMillis) throws DeviceException, InterruptedException {
		waitForValueToChange(pvArrayCounterRbv, initialNumFrames);
	}

	@Override
	public int getNumMcaChannels() {
		return numMcaChannels;
	}

	@Override
	public void setNumMcaChannels(int numMcaChannels) {
		this.numMcaChannels = numMcaChannels;
	}
}

