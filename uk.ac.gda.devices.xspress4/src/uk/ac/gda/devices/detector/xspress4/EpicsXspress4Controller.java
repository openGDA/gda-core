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

import java.io.IOException;

import org.apache.commons.lang.ArrayUtils;
import org.springframework.beans.factory.InitializingBean;

import gda.device.DeviceException;
import gda.epics.LazyPVFactory;
import gda.epics.PV;
import gda.epics.ReadOnlyPV;
import gda.factory.FindableBase;

public class EpicsXspress4Controller extends FindableBase implements Xspress4Controller, InitializingBean {
	private String pvBase = "";
	private int numElements = 64;
	private int numMcaChannels = 4096;
	private int numScalers = 8;

	// Standard scalers for each detector element, separate PV for each scaler type
	private static final String SCA_TEMPLATE = ":C%d_SCA%d:Value_RBV"; // detectorElement, sca number
	private ReadOnlyPV<Double>[][] pvForScalerValue = null; // [detectorElement][scalerNumber].scalerNumber=0...7

	// Standard scalers for each detector element, provided as an array
	private static final String SCA_ARRAY_TEMPLATE = ":C%d_SCAS"; // detectorElement
	private ReadOnlyPV<Double[]>[] pvForScalerArray = null; // [detectorElement]scalerNumber=0...7

	// Resolution grade values for each detector element (array of 16 in-window counts, one value for each resolution grade)
	private static final String RES_GRADE_TEMPLATE = ":C%d_SCA%d_RESGRADES"; // detectorElement, SCA{5,6}
	private ReadOnlyPV<Double[]>[][] pvForResGradeArray = null; // [detector element][window number]

	// Array of MCA for each detector element (summed over all res grades)
	private static final String ARRAY_DATA_TEMPLATE = ":ARR%d:ArrayData"; //detectorElement
	private ReadOnlyPV<Double[]>[] pvForMcaArray; // [detectorElement]

	// PV to cause all array data PVs above to be updated (e.g. caput “1” to this)
	private static final String UPDATE_ARRAYS_TEMPLATE = ":UPDATE_ARRAYS";
	private PV<Integer> pvUpdateArrays = null;

	// PV to set the exposure time (used for Software triggered collection)
	private static final String ACQUIRE_TIME_TEMPLATE = ":AcquireTime";
	private PV<Double> pvAcquireTime = null;

	private static final String TRIGGER_MODE_TEMPLATE = ":TriggerMode";
	private PV<Integer> pvTriggerMode = null;

	private static final String ARRAY_COUNTER = ":ArrayCounter";
	private PV<Integer> pvArrayCounter = null;

	private static final String ARRAY_COUNTER_RBV = ":ArrayCounter_RBV";
	private ReadOnlyPV<Integer> pvArrayCounterRbv = null;

	private static final String DTC_FACTORS = ":DTC_FACTORS";
	private ReadOnlyPV<Double[]> pvDtcFactors = null;

	private static final String ROI_RES_GRADE_BIN = ":ROI:BinY";
	private PV<Integer> pvRoiResGradeBin = null;

	private static final String DTC_ENERGY_KEV = ":DTC_ENERGY";
	private PV<Double> pvDtcEnergyKev = null;

	private double caClientTimeoutSecs = 10.0; // Timeout for CACLient put operations (seconds)
	private long pollIntervalMillis = 50;

	@Override
	public void afterPropertiesSet() throws Exception {
		pvForScalerValue = new ReadOnlyPV[numElements][numScalers];
		pvForScalerArray = new ReadOnlyPV[numElements];
		pvForResGradeArray = new ReadOnlyPV[numElements][2];
		pvForMcaArray = new ReadOnlyPV[numElements];

		for (int element = 0; element < numElements; element++) {

			String pvnameScalerArray = pvBase + String.format(SCA_ARRAY_TEMPLATE, element + 1);
			pvForScalerArray[element] = LazyPVFactory.newReadOnlyDoubleArrayPV(pvnameScalerArray);

			String pvnameResGrade = pvBase + String.format(RES_GRADE_TEMPLATE, element + 1, 5);
			pvForResGradeArray[element][0] = LazyPVFactory.newReadOnlyDoubleArrayPV(pvnameResGrade);

			pvnameResGrade = pvBase + String.format(RES_GRADE_TEMPLATE, element + 1, 6);
			pvForResGradeArray[element][1] = LazyPVFactory.newReadOnlyDoubleArrayPV(pvnameResGrade);

			String pvnameArrayData = pvBase + String.format(ARRAY_DATA_TEMPLATE, element + 1);
			pvForMcaArray[element] = LazyPVFactory.newReadOnlyDoubleArrayPV(pvnameArrayData);

			for (int sca = 0; sca < numScalers; sca++) {
				String pvnameScaler = pvBase + String.format(SCA_TEMPLATE, element + 1, sca);
				pvForScalerValue[element][sca] = LazyPVFactory.newReadOnlyDoublePV(pvnameScaler);
			}
		}

		pvTriggerMode = LazyPVFactory.newIntegerPV(pvBase + TRIGGER_MODE_TEMPLATE);
		pvAcquireTime = LazyPVFactory.newDoublePV(pvBase + ACQUIRE_TIME_TEMPLATE);
		pvUpdateArrays = LazyPVFactory.newIntegerPV(pvBase + UPDATE_ARRAYS_TEMPLATE);
		pvArrayCounter = LazyPVFactory.newIntegerPV(pvBase + ARRAY_COUNTER);
		pvArrayCounterRbv = LazyPVFactory.newReadOnlyIntegerPV(pvBase + ARRAY_COUNTER_RBV);
		pvDtcFactors = LazyPVFactory.newReadOnlyDoubleArrayPV(pvBase + DTC_FACTORS);
		pvRoiResGradeBin = LazyPVFactory.newIntegerPV(pvBase + ROI_RES_GRADE_BIN);
		pvDtcEnergyKev = LazyPVFactory.newDoublePV(pvBase + DTC_ENERGY_KEV);
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

	private double[] getArray(ReadOnlyPV<Double[]> pv) throws DeviceException {
		try {
			return ArrayUtils.toPrimitive(pv.get());
		}catch(IOException e) {
			throw new DeviceException(e);
		}
	}
	private <T> T getValue(ReadOnlyPV<T> pv) throws DeviceException {
		try {
			return pv.get();
		}catch(IOException e) {
			throw new DeviceException(e);
		}
	}

	private <T> void putValue(PV<T> pv, T value) throws DeviceException {
		try {
			pv.putWait(value, caClientTimeoutSecs);
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

