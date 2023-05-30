/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package uk.ac.gda.devices.pressurecell.controller.epics;

import static java.util.Objects.requireNonNull;
import static uk.ac.gda.devices.pressurecell.controller.epics.EpicsPressureDataController.TriggerControl.CAPTURE;
import static uk.ac.gda.devices.pressurecell.controller.epics.EpicsPressureDataController.TriggerControl.DONE;
import static uk.ac.gda.devices.pressurecell.controller.epics.EpicsPressureDataController.TriggerState.ARMED;
import static uk.ac.gda.devices.pressurecell.controller.epics.EpicsPressureDataController.TriggerState.IDLE;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.detector.areadetector.v17.NDFileHDF5;
import gda.epics.LazyPVFactory;
import gda.epics.PV;
import gda.epics.ReadOnlyPV;
import gda.factory.Configurable;
import gda.factory.FactoryException;
import uk.ac.gda.devices.pressurecell.data.PressureCellDataController;

public class EpicsPressureDataController implements PressureCellDataController, Configurable {
	private static final Logger logger = LoggerFactory.getLogger(EpicsPressureDataController.class);
	/** The sampling rate of the datawriter */
	private static final int SAMPLE_RATE = 10_000;

	enum TriggerControl {
		DONE, CAPTURE;
	}

	enum TriggerState {
		IDLE, ARMED, GATING, ACQUIRING;
	}

	enum SoftwareTrigger {
		OFF, ON;
	}

	private String filenameFormat = "%s/%s.h5";

	private NDFileHDF5 fileWriter;

	private String basePV;

	private PV<Integer> samplesBeforePV;
	private PV<Integer> samplesAfterPV;

	private PV<TriggerControl> triggerControlPV;
	private ReadOnlyPV<TriggerState> triggerStatePV;
	private PV<SoftwareTrigger> softwareTriggerPV;
	private ReadOnlyPV<Integer> bufferedSamplesPV;

	private boolean configured;

	private double timeout = 5.0;

	@Override
	public void configure() throws FactoryException {
		requireNonNull(fileWriter, "fileWriter must be configured");
		requireNonNull(basePV, "basePV must be configured");

		samplesBeforePV = LazyPVFactory.newIntegerPV(basePV + ":TRIG:PRECOUNT");
		samplesAfterPV = LazyPVFactory.newIntegerPV(basePV + ":TRIG:POSTCOUNT");

		triggerControlPV = LazyPVFactory.newEnumPV(basePV + ":TRIG:Capture", TriggerControl.class);
		triggerStatePV = LazyPVFactory.newReadOnlyEnumPV(basePV + ":TRIG:Mode_RBV", TriggerState.class);
		softwareTriggerPV = LazyPVFactory.newEnumPV(basePV + ":TRIG:Soft_Trigger", SoftwareTrigger.class);
		bufferedSamplesPV = LazyPVFactory.newReadOnlyIntegerPV(basePV + ":TRIG:BufferSamples");
		configured = true;
	}

	@Override
	public void setFilePath(String directory, String filename) throws DeviceException {
		logger.debug("Set file paths to '{}' and '{}'", directory, filename);
		try {
			fileWriter.setFileTemplate(filenameFormat);
			fileWriter.setFilePath(directory);
			fileWriter.setFileName(filename);
		} catch (Exception e) {
			throw new DeviceException("Could not set file path and directory", e);
		}
	}

	@Override
	public void setAcquire(boolean acquiring) throws DeviceException {
		logger.debug("Setting trigger control to {}", acquiring);
		try {
			if (acquiring) {
				triggerControlPV.putNoWait(CAPTURE);
				triggerStatePV.waitForValue(s -> s == ARMED, timeout);
			} else {
				triggerControlPV.putNoWait(DONE);
				triggerStatePV.waitForValue(s -> s == IDLE, timeout);
			}
		} catch (IOException | IllegalStateException | TimeoutException e) {
			throw new DeviceException("Could not set pressure cell trigger control to " + acquiring, e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new DeviceException("Thread interrupted while waiting for triggers to be armed", e);
		}
	}

	@Override
	public void setDataWriter(boolean acquiring) throws DeviceException {
		logger.debug("{} pressure cell datawriter", acquiring ? "Starting" : "Stopping");
		try {
			if (acquiring) {
				fileWriter.startCapture();
			} else {
				fileWriter.stopCapture();
			}
		} catch (Exception e) {
			throw new DeviceException("Could not set pressure cell writing to " + acquiring, e);
		}
	}

	@Override
	public String getLastFileName() throws DeviceException {
		try {
			return fileWriter.getFullFileName_RBV();
		} catch (Exception e) {
			throw new DeviceException("Could not get last file name", e);
		}
	}

	@Override
	public void setSamples(int before, int after) throws DeviceException {
		try {
			int total = before + after;
			int expectedTotal = fileWriter.getFile()
					.getPluginBase()
					.getArraySize1_RBV();
			samplesBeforePV.putWait(before);
			samplesAfterPV.putWait(after);
			if (total != expectedTotal) {
				logger.info("Sample counts changed ({} -> {}) - running software triggered collection to update filewriter",
						expectedTotal, total);
				runSoftwareCollection(before, after);
			}
		} catch (IOException | IllegalStateException | TimeoutException e) {
			throw new DeviceException("Could not set samples for pressure cell", e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new DeviceException("Thread interrupted while waiting for samples to be updated", e);
		} catch (Exception e) {
			throw new DeviceException("Failed to check or set data size for filewriter", e);
		}
	}

	/**
	 * Run a software triggered collection without writing data
	 *
	 * This allows the size of the expected dataset to be updated when the number of samples to
	 * be collected is changed.
	 * @throws InterruptedException
	 * @throws IOException
	 * @throws TimeoutException
	 * @throws IllegalStateException
	 * @throws DeviceException
	 */
	private void runSoftwareCollection(int before, int after) throws TimeoutException, IOException, InterruptedException, DeviceException {
		setAcquire(true);

		// Wait for buffer to fill
		int timeoutBefore = before/SAMPLE_RATE + 5;
		logger.debug("Waiting {}s for buffer to fill", timeoutBefore);
		bufferedSamplesPV.waitForValue(v -> v >= before, timeoutBefore);

		// Trigger the jump
		softwareTriggerPV.putNoWait(SoftwareTrigger.ON);

		// Wait for the rest of the data to be collected
		int timeoutAfter = after/SAMPLE_RATE + 5;
		logger.debug("Waiting {}s for collection to complete", timeoutAfter);
		triggerStatePV.waitForValue(v -> v == TriggerState.IDLE, timeoutAfter);
		softwareTriggerPV.putNoWait(SoftwareTrigger.OFF);
	}

	public NDFileHDF5 getFileWriter() {
		return fileWriter;
	}

	public void setFileWriter(NDFileHDF5 fileWriter) {
		this.fileWriter = fileWriter;
	}

	@Override
	public boolean isConfigured() {
		return configured;
	}

	@Override
	public void reconfigure() throws FactoryException {

	}

	@Override
	public boolean isConfigureAtStartup() {
		return true;
	}

	public String getBasePV() {
		return basePV;
	}

	public void setBasePV(String basePV) {
		this.basePV = basePV;
	}
}
