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
import static uk.ac.gda.devices.detector.xspress4.XspressPvName.ARRAY_DATA_TEMPLATE;
import static uk.ac.gda.devices.detector.xspress4.XspressPvName.DTC_FACTOR_TEMPLATE;
import static uk.ac.gda.devices.detector.xspress4.XspressPvName.RES_GRADE_TEMPLATE;
import static uk.ac.gda.devices.detector.xspress4.XspressPvName.ROI_RES_GRADE_BIN;
import static uk.ac.gda.devices.detector.xspress4.XspressPvName.SCA_TEMPLATE;
import static uk.ac.gda.devices.detector.xspress4.XspressPvName.SCA_TIMESERIES_ACQUIRE_TEMPLATE;
import static uk.ac.gda.devices.detector.xspress4.XspressPvName.SCA_TIMESERIES_CURRENTPOINT_TEMPLATE;
import static uk.ac.gda.devices.detector.xspress4.XspressPvName.SCA_TIMESERIES_TEMPLATE;
import static uk.ac.gda.devices.detector.xspress4.XspressPvName.TRIGGER_DETECTOR;
import static uk.ac.gda.devices.detector.xspress4.XspressPvName.UPDATE_ARRAYS_TEMPLATE;

import java.io.IOException;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import gda.device.DeviceException;
import gda.epics.LazyPVFactory;
import gda.epics.PV;
import gda.epics.ReadOnlyPV;
import gda.factory.FindableBase;
import uk.ac.gda.devices.detector.xspress3.controllerimpl.ACQUIRE_STATE;
import uk.ac.gda.devices.detector.xspress3.controllerimpl.XSPRESS3_EPICS_STATUS;

public class EpicsXspress4Controller extends FindableBase implements Xspress4Controller, InitializingBean {

	private static final Logger logger = LoggerFactory.getLogger(EpicsXspress4Controller.class);

	private String pvBase = "";
	private String hdfWriterPrefix = ":HDF5";
	private String metaWriterPrefix = ":OD:META";
	private String mainControlPrefix = ""; // set to ":CAM" for Odin based detector

	private int numElements = 64;
	private int numMcaChannels = 4096;
	private int numScalers = 8;

	private Map<XspressPvName, String> pvNameMap = new EnumMap<>(XspressPvName.class);

	// Standard scalers for each detector element, separate PV for each scaler type
	private ReadOnlyPV<Double>[][] pvForScalerValue = null; // [detectorElement][scalerNumber].scalerNumber=0...7

	// Resolution grade values for each detector element (array of 16 in-window counts, one value for each resolution grade)
	private ReadOnlyPV<Double[]>[][] pvForResGradeArray = null; // [detector element][window number]

	// Array of MCA for each detector element (summed over all res grades)
	private ReadOnlyPV<Double[]>[] pvForMcaArray; // [detectorElement]

	// PV to cause all array data PVs above to be updated (e.g. caput “1” to this)
	private PV<Integer> pvUpdateArrays = null;

	private ReadOnlyPV<Double>[] pvDtcFactor = null;

	private PV<Integer> pvRoiResGradeBin = null;

	// Time series value for detector element, scaler .
	private ReadOnlyPV<Double[]>[][] pvScalerTimeSeries = null; // [detectorElement][scalerNumber].scalerNumber=0...7

	private PV<Integer>[] pvScalerTimeSeriesAcquire = null; // [detectorElement]

	private ReadOnlyPV<Integer>[] pvScalerTimeSeriesCurrentPoint = null; // [detectorElement]

	/** Hdf file writing PVs  */
	private HdfFilePvProvider fileWritingPvs;

	/** PV to send a software trigger to the detector (Xspress4Odin) */
	private PV<Integer> pvSofwareTrigger = null;

	/** PVs to control the Metawriter (Xspress4Odin only) */
	private OdinPvProvider odinPvs;

	/** 'Camera control' PVs - start, stop, the detector, setup the scaler windows, connect, disconnect */
	private XspressCamControl cameraControlPvs;


	private double caClientTimeoutSecs = 10.0; // Timeout for CACLient put operations (seconds)

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
		pvForResGradeArray = new ReadOnlyPV[numElements][2];
		pvForMcaArray = new ReadOnlyPV[numElements];

		for (int element = 0; element < numElements; element++) {
			String pvnameResGrade = String.format(getPvName(RES_GRADE_TEMPLATE), element + 1, 6);
			pvForResGradeArray[element][1] = LazyPVFactory.newReadOnlyDoubleArrayPV(pvnameResGrade);

			String pvnameArrayData = String.format(getPvName(ARRAY_DATA_TEMPLATE), element + 1);
			pvForMcaArray[element] = LazyPVFactory.newReadOnlyDoubleArrayPV(pvnameArrayData);

			for (int sca = 0; sca < numScalers; sca++) {
				String pvnameScaler = String.format(getPvName(SCA_TEMPLATE), element + 1, sca);
				pvForScalerValue[element][sca] = LazyPVFactory.newReadOnlyDoublePV(pvnameScaler);
			}
		}

		pvUpdateArrays = LazyPVFactory.newIntegerPV(getPvName(UPDATE_ARRAYS_TEMPLATE));
		pvRoiResGradeBin = LazyPVFactory.newIntegerPV(getPvName(ROI_RES_GRADE_BIN));

		createOdinPvs();
		createCameraControlPvs();
		createDtcFactorPvs();
		createTimeSeriesPVs();
		createFileWritingPvs();
	}

	/**
	 * HDF file writing PVs
	 */
	private void createFileWritingPvs() {
		logger.info("Creating Hdf file file writing PVs");
		fileWritingPvs = new HdfFilePvProvider();
		fileWritingPvs.setPrefix(pvBase + hdfWriterPrefix);
		fileWritingPvs.setPvNameMap(pvNameMap);
		fileWritingPvs.createPvs();
		fileWritingPvs.checkPvsExist();
	}

	private void createOdinPvs() {
		if (!XspressPvProviderBase.pvExists(getPvName(TRIGGER_DETECTOR))) {
			return;
		}
		logger.info("Creating Odin specific PVs");
		odinPvs = new OdinPvProvider();
		odinPvs.setPrefix(pvBase + metaWriterPrefix);
		odinPvs.setPvNameMap(pvNameMap);
		odinPvs.createPvs();
		odinPvs.checkPvsExist();
	}

	private void createCameraControlPvs() {
		logger.info("Creating PVs for controlling camera and setting scaler window ranges");
		cameraControlPvs = new XspressCamControl();
		cameraControlPvs.setPvNameMap(pvNameMap);
		cameraControlPvs.setPrefix(pvBase + mainControlPrefix);
		cameraControlPvs.setNumChannels(numElements);
		cameraControlPvs.createPvs();
		cameraControlPvs.checkPvsExist();
	}

	public boolean isConnected() throws DeviceException {
		return getValue(cameraControlPvs.pvIsConnected);
	}

	public void connect() throws DeviceException {
		putValue(cameraControlPvs.pvConnect, 1);
	}

	public void disonnect() throws DeviceException {
		putValue(cameraControlPvs.pvDisconnect, 1);
	}

	/**
	 * Create PVs for reading DTC (deadtime correction factor) values
	 */
	private void createDtcFactorPvs() {
		String dtcFactorTemplate = getPvName(DTC_FACTOR_TEMPLATE);
		logger.debug("Using DTC factors from {} values", dtcFactorTemplate);
		pvDtcFactor = new ReadOnlyPV[numElements];
		for(int i=0; i<numElements;i++) {
			pvDtcFactor[i] = LazyPVFactory.newReadOnlyDoublePV(String.format(dtcFactorTemplate, i+1));
		}
	}

	/**
	 * Create PVs for reading scalers from time series arrays
	 */
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
	 * Generate full name of a 'main detector control' PV from the name map.
	 * @param pv
	 * @return pvBase + mainControlPrefix + pvName
	 */
	private String getPvName(XspressPvName pv) {
		return pvBase + mainControlPrefix + pvNameMap.getOrDefault(pv, pv.pvName());
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
		double[] vals = new double[numScalers];
		for(int i=0; i<numScalers; i++) {
			vals[i] = getScalerValue(element, i);
		}
		return vals;
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
		putValue(cameraControlPvs.pvTriggerMode, triggerMode);
	}

	@Override
	public int getTriggerMode() throws DeviceException {
		return getValue(cameraControlPvs.pvTriggerMode);
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
		putValue(cameraControlPvs.pvDtcEnergyKev, energyKev);
	}

	@Override
	public double getDeadtimeCorrectionEnergy() throws DeviceException {
		return getValue(cameraControlPvs.pvDtcEnergyKev);
	}

	@Override
	public void resetFramesReadOut() throws DeviceException {
		putValue(cameraControlPvs.pvArrayCounter, 0);
	}

	@Override
	public int getTotalFramesAvailable() throws DeviceException {
		return getValue(cameraControlPvs.pvArrayCounterRbv);
	}

	@Override
	public void setAcquireTime(double time) throws DeviceException {
		putValue(cameraControlPvs.pvAcquireTime, time);
	}

	@Override
	public double[] getDeadtimeCorrectionFactors() throws DeviceException {
		double[] vals = new double[numElements];
		for(int i=0; i<numElements; i++) {
			vals[i] = getValue(pvDtcFactor[i]);
		}
		return vals;
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
	 * Wrap {@link ReadOnlyPV#waitForValue(Predicate, double), which rethrows exceptions as DeviceException
	 * @param pv
	 * @param predicate
	 * @param timeoutSecs
	 * @throws InterruptedException
	 * @throws DeviceException
	 */
	private <T> void waitForValue(ReadOnlyPV<T> pv, Predicate<T> predicate, double timeoutSecs) throws DeviceException {
		try {
			pv.waitForValue(predicate, timeoutSecs);
		} catch(Exception e) {
			throw new DeviceException(e);
		}
	}
	private <T> void waitForValue(ReadOnlyPV<T> pv, Predicate<T> predicate) throws DeviceException {
		waitForValue(pv, predicate, caClientTimeoutSecs);
	}

	/**
	 * Wait for the value returned by a PV to change. This function blocks until PV value != startValue,
	 * timeout is reached or an exception is thrown.
	 * @param pv
	 * @param startValue
	 * @throws InterruptedException
	 * @throws DeviceException
	 */
	@Override
	public void waitForCounterToIncrement(int initialNumFrames, long timeoutMillis) throws DeviceException, InterruptedException {
		waitForValue(cameraControlPvs.pvArrayCounterRbv, v -> v != initialNumFrames);
	}

	@Override
	public int getNumMcaChannels() {
		return numMcaChannels;
	}

	@Override
	public void setNumMcaChannels(int numMcaChannels) {
		this.numMcaChannels = numMcaChannels;
	}

	@Override
	public void setScalerWindow(int channel, int windowNumber, int lowLimit, int highLimit) throws DeviceException {
		List< PV<Integer> > lowWindowPv = windowNumber == 0 ? cameraControlPvs.pvScaler5LowLimit : cameraControlPvs.pvScaler6LowLimit;
		List< PV<Integer> > highWindowPv = windowNumber == 0 ? cameraControlPvs.pvScaler5HighLimit : cameraControlPvs.pvScaler6HighLimit;

		logger.info("Setting scaler window limits for window {}, channel {} to : low = {}, high = {}", windowNumber, channel, lowLimit, highLimit);
		putValue(lowWindowPv.get(channel), 0);
		putValue(highWindowPv.get(channel), highLimit);
		putValue(lowWindowPv.get(channel), lowLimit);
	}

	@Override
	public XSPRESS3_EPICS_STATUS getDetectorState() throws DeviceException {
		return getValue(cameraControlPvs.pvGetState);
	}

	@Override
	public void sendSoftwareTrigger() throws DeviceException {
		putValueNoWait(pvSofwareTrigger, 1);
	}

	@Override
	public void startAcquire() throws DeviceException {
		putValueNoWait(cameraControlPvs.pvAcquire, ACQUIRE_STATE.Acquire);
	}

	@Override
	public void stopAcquire() throws DeviceException {
		putValueNoWait(cameraControlPvs.pvAcquire, ACQUIRE_STATE.Done);
	}

	public void startHdfWriting() throws DeviceException {
		// filepath and name have already been set
		int numFrames = getNumImagesRbv();
		logger.debug("Starting hdf writer : {} frames", numFrames);
		setHdfNumFrames(numFrames);
		startHdfWriter();
		logger.debug("Waiting for hdf writer to start");
		waitForValue(fileWritingPvs.pvHdfCapturingRbv, Boolean.TRUE::equals);
	}

	/**
	 * Wait for Hdf file image capture capture to start/stop
	 *
	 * @param state : true == wait for start, false = wait for stop.
	 * @throws DeviceException
	 */
	@Override
	public void waitForCaptureState(boolean state) throws DeviceException {
		 waitForValue(fileWritingPvs.pvHdfCapturingRbv, Boolean.valueOf(state)::equals);

	}
	@Override
	public void setNumImages(int numImages) throws DeviceException {
		putValue(cameraControlPvs.pvNumImages, numImages);
	}

	public int getNumImages() throws DeviceException {
		return getValue(cameraControlPvs.pvNumImages);
	}

	public int getNumImagesRbv() throws DeviceException {
		return getValue(cameraControlPvs.pvNumImagesRbv);
	}

	@Override
	public void startHdfWriter() throws DeviceException {
		putValueNoWait(fileWritingPvs.pvHdfCapturedControl, 1);
	}

	@Override
	public void stopHdfWriter() throws DeviceException {
		putValueNoWait(fileWritingPvs.pvHdfCapturedControl, 0);
	}

	@Override
	public void setHdfFilePath(String path) throws DeviceException {
		putValueNoWait(fileWritingPvs.pvHdfFilePath, path);
	}

	@Override
	public String getHdfFilePath() throws DeviceException {
		return getValue(fileWritingPvs.pvHdfFilePathRbv);
	}


	@Override
	public void setHdfFileName(String name) throws DeviceException {
		putValue(fileWritingPvs.pvHdfFileName, name);
	}

	@Override
	public String getHdfFullFileName() throws DeviceException {
		return getValue(fileWritingPvs.pvHdfFullFileNameRbv);
	}

	@Override
	public void setHdfNumFrames(int numFrames) throws DeviceException {
		putValue(fileWritingPvs.pvHdfNumCapture, numFrames);
	}

	@Override
	public int getHdfNumFramesRbv() throws DeviceException {
		return getValue(fileWritingPvs.pvHdfNumCaptureRbv);
	}

	@Override
	public int getHdfNumCapturedFrames() throws DeviceException {
		return getValue(fileWritingPvs.pvHdfNumCapturedRbv);
	}

	public String setMetaFileName() throws DeviceException {
		return getValue(odinPvs.pvMetaFileName);
	}

	public String getMetaOutputFileName() throws DeviceException {
		return getValue(odinPvs.pvMetaOutputFileRbv);
	}

	public void stopMetaWriter() throws DeviceException {
		putValue(odinPvs.pvMetaStop, 1);
		logger.debug("Waiting for Meta writer to stop");
		waitForValue(odinPvs.pvMetaIsActiveRbv, Boolean.FALSE::equals, caClientTimeoutSecs);
	}

	public void setHdfWriterPrefix(String hdfWriterPrefix) {
		this.hdfWriterPrefix = hdfWriterPrefix;
	}
	public String getHdfWriterPrefix() {
		return hdfWriterPrefix;
	}

	public void setMainControlPrefix(String mainControlPrefix) {
		this.mainControlPrefix = mainControlPrefix;
	}
	public String getMainControlPrefix() {
		return mainControlPrefix;
	}

	public void setMetaWriterPrefix(String metaWriterPrefix) {
		this.metaWriterPrefix = metaWriterPrefix;
	}
	public String getMetaWriterPrefix() {
		return metaWriterPrefix;
	}

}

