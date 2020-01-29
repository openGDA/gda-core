/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package uk.ac.gda.server.ncd.subdetector;

import static gda.epics.LazyPVFactory.newDoublePV;
import static gda.epics.LazyPVFactory.newEnumPV;
import static gda.epics.LazyPVFactory.newIntegerFromEnumPV;
import static gda.epics.LazyPVFactory.newIntegerPV;
import static gda.epics.LazyPVFactory.newReadOnlyDoublePV;
import static gda.epics.LazyPVFactory.newReadOnlyIntegerPV;
import static uk.ac.gda.server.ncd.subdetector.NcdEpicsTetramm.TetramGeometry.SQUARE;
import static uk.ac.gda.server.ncd.subdetector.NcdEpicsTetramm.TetramRange.MICRO;
import static uk.ac.gda.server.ncd.subdetector.NcdEpicsTetramm.TetramResolution.BIT_24;
import static uk.ac.gda.server.ncd.subdetector.tetramm.NcdTetrammController.TriggerState.EXT_TRIG;
import static uk.ac.gda.server.ncd.subdetector.tetramm.NcdTetrammController.TriggerState.FREE_RUN;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.detector.areadetector.v17.NDFileHDF5;
import gda.epics.PV;
import gda.epics.PVWithSeparateReadback;
import gda.epics.ReadOnlyPV;
import gda.factory.FactoryException;
import gda.factory.FindableConfigurableBase;
import uk.ac.gda.server.ncd.subdetector.tetramm.NcdTetrammController;

public class NcdEpicsTetramm extends FindableConfigurableBase implements NcdTetrammController {
	public enum TetramResolution {
		BIT_16, BIT_24;
	}
	public enum TetramGeometry {
		DIAMOND, SQUARE;
	}
	public enum TetramRange {
		MICRO, NANO;
	}

	private static final Logger logger = LoggerFactory.getLogger(NcdEpicsTetramm.class);

	private static final double TIMEOUT = 5;

	/** The base of the PV for this TetrAMM (up to but not including the first ':') */
	private String basePv;

	/** AreaDetector file writer plugin */
	private NDFileHDF5 fileWriter;

	private PV<Integer> valuesPerReadingPV;
	private PV<Double> averagingTimePV;
	private PV<Integer> acquirePV;
	private PV<Integer> channelsPV;
	private PV<TriggerState> triggerModePV;
	private ReadOnlyPV<Double> sampleTimePV;
	private ReadOnlyPV<Integer> numToAveragePV;
	private ReadOnlyPV<Integer> numAveragedPV;
	private PV<TetramResolution> resolutionPV;
	private PV<TetramGeometry> geometryPV;
	private PV<TetramRange> rangePV;

	/** The trigger state for the detector when it is not involved in a scan */
	private TriggerState idleTriggerState = FREE_RUN;

	/** The acquire state for the detector when it is not involved in a scan */
	private boolean idleAquireState = true;

	/** The averaging time for the detector while the detector is not in a scan */
	private double idleAveragingTime = 0.1;

	/** The values per reading for the detector while the detector is not in a scan */
	private int idleValuesPerReading = 10;

	/** The format the filewriter should use for its file path building */
	private String filenameFormat = "%s/%s.h5";

	/** The resolution that should be used for data collections */
	private TetramResolution collectionResolution = BIT_24;

	/** The geometry that should be used for data collections */
	private TetramGeometry collectionGeometry = SQUARE;

	/** The range that should be used for data collections */
	private TetramRange collectionRange = MICRO;

	@Override
	public void configure() throws FactoryException {
		valuesPerReadingPV = new PVWithSeparateReadback<>(
				newIntegerPV(basePv + ":DRV:ValuesPerRead"),
				newReadOnlyIntegerPV(basePv + ":DRV:ValuesPerRead_RBV"));
		averagingTimePV = new PVWithSeparateReadback<>(
				newDoublePV(basePv + ":DRV:AveragingTime"),
				newReadOnlyDoublePV(basePv + ":DRV:AveragingTime_RBV"));
		sampleTimePV = newReadOnlyDoublePV(basePv + ":DRV:SampleTime_RBV");
		numToAveragePV = newReadOnlyIntegerPV(basePv + ":DRV:NumAverage_RBV");
		numAveragedPV = newReadOnlyIntegerPV(basePv + ":DRV:NumAveraged_RBV");

		acquirePV = newIntegerPV(basePv + ":DRV:Acquire");

		channelsPV = newIntegerFromEnumPV(basePv + ":DRV:NumChannels");
		triggerModePV = newEnumPV(basePv + ":DRV:TriggerMode", TriggerState.class);

		resolutionPV = newEnumPV(basePv + ":DRV:Resolution", TetramResolution.class);
		rangePV = newEnumPV(basePv + ":DRV:Range", TetramRange.class);
		geometryPV = newEnumPV(basePv + ":DRV:Geometry", TetramGeometry.class);
	}

	@Override
	public void setAcquire(boolean state) throws DeviceException {
		try {
			if (state) {
				start();
			} else {
				stop();
			}
		} catch (IOException e) {
			throw new DeviceException(getName() + " - Could not set acquire state of detector", e);
		}
	}

	/** Stop the detector acquiring */
	public void stop() throws IOException {
		logger.debug("Stopping {}", getName());
		acquirePV.putWait(0, TIMEOUT);
	}

	/** Put the detector in to acquire mode */
	public void start() throws IOException {
		logger.debug("Starting {}", getName());
		acquirePV.putNoWait(1); // start doesn't return until it's stopped
	}

	@Override
	public int getNumberOfChannels() throws DeviceException {
		try {
			return channelsPV.get();
		} catch (IOException e) {
			throw new DeviceException("Could not get number of channels", e);
		}
	}

	/** Stop the file writer */
	public void endCollection() throws DeviceException {
		try {
			fileWriter.stopCapture();
		} catch (Exception e) {
			throw new DeviceException("Could not stop file writer", e);
		}
	}

	@Override
	public void initialise() throws DeviceException {
		setAcquire(false);
		setTriggerState(EXT_TRIG);
		setGeometry(collectionGeometry);
		setRange(collectionRange);
		setResolution(collectionResolution);
	}

	@Override
	public void reset() throws DeviceException {
		logger.debug("{} - Resetting to idle configuration", getName());
		setAcquire(false);
		setTriggerState(idleTriggerState);
		setAveragingTime(idleAveragingTime);
		setValuesPerReading(idleValuesPerReading);
		setAcquire(idleAquireState);
	}

	@Override
	public int getValuesPerReading() throws DeviceException {
		return get(valuesPerReadingPV, "values per reading");
	}

	@Override
	public void setValuesPerReading(int values) throws DeviceException {
		set(valuesPerReadingPV, values, "values per reading");
	}

	@Override
	public double getAveragingTime() throws DeviceException {
		return get(averagingTimePV, "averaging time");
	}

	@Override
	public void setAveragingTime(double time) throws DeviceException {
		set(averagingTimePV, time, "averaging time");
		refreshFileSizeDimensions(time);
	}

	/**
	 * Frame size is dependent on trigger gate width so ensure new frame has
	 * been received when duration changes
	 * @throws DeviceException
	 */
	private void refreshFileSizeDimensions(double averagingTime) throws DeviceException {
		if (!idleAquireState || idleTriggerState != FREE_RUN) {
			setAcquire(false);
			setTriggerState(FREE_RUN);
			setAcquire(true);
			int target = getSamplesToAverage();
			try {
				numAveragedPV.waitForValue(d -> d == target, 2 * averagingTime);
			} catch (IllegalStateException | TimeoutException | IOException | InterruptedException e) {
				logger.warn("Could not ensure filewriter had correct dimensions. Scan may fail.", e);
			}
			setAcquire(false);
		}
	}

	@Override
	public double getSampleTime() throws DeviceException {
		return get(sampleTimePV, "sampling time");
	}

	@Override
	public int getSamplesToAverage() throws DeviceException {
		return get(numToAveragePV, "number of samples to average");
	}

	@Override
	public int getNumberOfAveragedSamples() throws DeviceException {
		return get(numAveragedPV, "number of samples averaged");
	}

	@Override
	public boolean isAcquiring() throws DeviceException {
		return get(acquirePV, "acquiring state") == 1;
	}

	@Override
	public void setNumberOfChannels(int channelCount) throws DeviceException {
		set(channelsPV, channelCount, "number of channels");
	}

	@Override
	public void setTriggerState(TriggerState state) throws DeviceException {
		set(triggerModePV, state, "trigger state");
	}

	@Override
	public TriggerState getTriggerState() throws DeviceException {
		return get(triggerModePV, "trigger state");
	}

	public void setResolution(TetramResolution res) throws DeviceException {
		set(resolutionPV, res, "resolution");
	}

	public void setRange(TetramRange range) throws DeviceException {
		set(rangePV, range, "range");
	}

	public void setGeometry(TetramGeometry geom) throws DeviceException {
		set(geometryPV, geom, "geometry");
	}

	@Override
	public void setFilePath(String directory, String name) throws DeviceException {
		try {
			fileWriter.setFileTemplate(filenameFormat);
			fileWriter.setFilePath(directory);
			fileWriter.setFileName(name);
		} catch (Exception e) {
			throw new DeviceException(getName() + " - Could not set file name and/or directory");
		}
	}

	@Override
	public String getLastFilePath() throws DeviceException {
		try {
			return fileWriter.getFullFileName_RBV();
		} catch (Exception e) {
			throw new DeviceException(getName() + " - Could not get last file name", e);
		}
	}

	@Override
	public void setDimensions(int framesPerPoint, int[] dimensions) throws DeviceException {
		int[] newDims = Arrays.copyOf(dimensions, dimensions.length + 1);
		newDims[dimensions.length] = framesPerPoint;

		try {
			if (logger.isDebugEnabled()) {
				logger.debug("{} - Setting dimensions to {}", getName(), Arrays.toString(newDims));
			}
			fileWriter.setExtraDimensions(newDims);
		} catch (Exception e) {
			throw new DeviceException(getName() + " - Could not set scan dimensions", e);
		}
	}

	@Override
	public void setRecording(boolean state) throws DeviceException {
		try {
			if (state) {
				fileWriter.startCapture();
			} else {
				fileWriter.stopCapture();
			}
		} catch (Exception e) {
			throw new DeviceException(getName() + " - Could not start file writer", e);
		}
	}

	@Override
	public boolean isRecording() throws DeviceException {
		try {
			return fileWriter.getCapture() == 1;
		} catch (Exception e) {
			throw new DeviceException(getName() + " - Could not not get capture state of fileWriter", e);
		}
	}

	/** Utility to get the value from a PV, wrapping any exceptions as DeviceException */
	private <T> T get(ReadOnlyPV<T> call, String message) throws DeviceException {
		try {
			return call.get();
		} catch (Exception e) {
			throw new DeviceException(getName() + " - Could not get " + message, e);
		}
	}

	/** Utility to set the value of a PV, wrapping any exceptions as DeviceException */
	private <T> void set(PV<T> pv, T value, String message) throws DeviceException {
		try {
			pv.putWait(value, TIMEOUT);
		} catch (Exception e) {
			throw new DeviceException(getName() + " - Could not set " + message + " to " + value, e);
		}
	}

	public String getBasePv() {
		return basePv;
	}

	public void setBasePv(String basePv) {
		this.basePv = basePv;
	}

	public NDFileHDF5 getFileWriter() {
		return fileWriter;
	}

	public void setFileWriter(NDFileHDF5 fileWriter) {
		this.fileWriter = fileWriter;
	}

	public TriggerState getIdleTriggerState() {
		return idleTriggerState;
	}

	public void setIdleTriggerState(TriggerState initialTriggerState) {
		this.idleTriggerState = initialTriggerState;
	}

	public boolean isIdleAquire() {
		return idleAquireState;
	}

	public void setIdleAquire(boolean initialAquireState) {
		this.idleAquireState = initialAquireState;
	}

	public double getIdleAveragingTime() {
		return idleAveragingTime;
	}

	public void setIdleAveragingTime(double idleAveragingTime) {
		this.idleAveragingTime = idleAveragingTime;
	}

	public int getIdleValuesPerReading() {
		return idleValuesPerReading;
	}

	public void setIdleValuesPerReading(int idleValuesPerReading) {
		this.idleValuesPerReading = idleValuesPerReading;
	}

	public void setCollectionResolution(TetramResolution collectionResolution) {
		this.collectionResolution = collectionResolution;
	}

	public void setCollectionGeometry(TetramGeometry collectionGeometry) {
		this.collectionGeometry = collectionGeometry;
	}

	public void setCollectionRange(TetramRange collectionRange) {
		this.collectionRange = collectionRange;
	}
}
