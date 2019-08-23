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

package uk.ac.diamond.daq.devices.keithley;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.data.nexus.extractor.NexusGroupData;
import gda.data.nexus.tree.NexusTreeProvider;
import gda.device.DeviceException;
import gda.device.detector.NXDetectorData;
import gda.device.detector.NexusDetector;
import gov.aps.jca.CAException;
import gov.aps.jca.CAStatus;
import gov.aps.jca.Channel;
import gov.aps.jca.event.PutEvent;

public class Keithley2600SeriesAverageMode extends Keithley2600Series implements NexusDetector {

	private static final Logger logger = LoggerFactory.getLogger(Keithley2600Series.class);

	/** Start average mode acquisition */
	public static final String ACQUIRE = "MeasOlapIVStart";

	/** Average voltage */
	public static final String MEAN_V = "MeanV";

	/** Average current */
	public static final String MEAN_I = "MeanI";

	/** Number of readings to be taken */
	private static final String NUMBER_OF_READINGS = "MeasCount";
	private static final String NUMBER_OF_READINGS_RBV = "MeasCountR";

	/** Interval between readings */
	private static final String DWELL_TIME = "MeasInterval";
	private static final String DWELL_TIME_RBV = "MeasIntervalR";

	/** Effective measurement time */
	private static final String MEASUREMENT_PERIOD = "MeasPeriodR";
	private double meanVoltage;
	private double meanCurrent;

	private boolean firstReadoutInScan;
	private int status = IDLE;


	public void setDwellTime(int demand) throws DeviceException {
		// TODO validation
		logger.debug("{} setting dwell time to: {}", getName(), demand);
		try {
			epicsController.caputWait(getChannel(DWELL_TIME), demand);
		} catch (Exception exception) {
			throw new DeviceException("Failed to set dwell time to: " + demand, exception);
		}
	}

	public int getDwellTime() throws DeviceException {
		try {
			return epicsController.cagetInt(getChannel(DWELL_TIME_RBV));
		} catch (Exception exception) {
			throw new DeviceException("Failed to get dwell time", exception);
		}
	}

	public void setNumberOfReadings(int demand) throws DeviceException {
		// TODO validation
		logger.debug("{} setting number of readings to: {}", getName(), demand);
		try {
			epicsController.caputWait(getChannel(NUMBER_OF_READINGS), demand);
		} catch (Exception exception) {
			throw new DeviceException("Failed to set number of readings to: " + demand, exception);
		}
	}

	public int getNumberOfReadings() throws DeviceException {
		try {
			return epicsController.cagetInt(getChannel(NUMBER_OF_READINGS_RBV));
		} catch (Exception exception) {
			throw new DeviceException("Failed to get number of readings", exception);
		}
	}

	public double getMeanVoltage() throws DeviceException {
		try {
			return epicsController.cagetDouble(getChannel(MEAN_V));
		} catch (Exception e) {
			throw new DeviceException("Failed to get mean voltage", e);
		}
	}

	public double getMeanCurrent() throws DeviceException {
		try {
			return epicsController.cagetDouble(getChannel(MEAN_I));
		} catch (Exception e) {
			throw new DeviceException("Failed to get mean current", e);
		}
	}

	@Override
	public void collectData() throws DeviceException {
		status = BUSY;

		outputOn();

		try {
			logger.debug("{}: Initiating acquisition.", getName());
			epicsController.caput(getChannel(ACQUIRE), "Acquire", this::putCompleted);
			logger.debug("{}: Acquisition initiated.", getName());
		} catch (CAException exception) {
			logger.error("Error occured while initiating acquisition.", exception);
			status = IDLE;
			throw new DeviceException("An error occured while initiating acquisition.", exception);
		} catch (InterruptedException exception) {
			logger.error("Acquisition interrupted.", exception);
			status = IDLE;
			Thread.currentThread().interrupt();
			throw new DeviceException("Acquisition was interrupted.", exception);
		}
	}

	private void putCompleted(PutEvent event) {
		logger.debug("{}: Acquisition callback received.", getName());

		if (event.getStatus() == CAStatus.NORMAL) {
			logger.debug("{}: Acquisition completed", getName());
		} else {
			logger.error("Acquisition failed. Channel {} : Status {}", ((Channel) event.getSource()).getName(), event.getStatus());
		}

		status = IDLE;
	}

	@Override
	public void setCollectionTime(double time) throws DeviceException {
		double timeInMilliseconds = time * 1000;
		int dwellTime = (int) (timeInMilliseconds / getNumberOfReadings());
		setDwellTime(dwellTime);
	}

	@Override
	public double getCollectionTime() throws DeviceException {
		try {
			return epicsController.cagetInt(getChannel(MEASUREMENT_PERIOD));
		} catch (Exception exception) {
			throw new DeviceException("Failed to get measurement period", exception);
		}
	}

	@Override
	public int getStatus() throws DeviceException {
		return status;
	}

	@Override
	public NexusTreeProvider readout() throws DeviceException {
		NXDetectorData data = new NXDetectorData(this);

		try {
			meanVoltage = getMeanVoltage();
			meanCurrent = getMeanCurrent();
		} catch (DeviceException exception) {
			logger.error("Error getting mean voltage or current.", exception);
			throw exception;
		}

		if (getSourceMode() == SourceMode.CURRENT) {
			data.setPlottableValue(getName(),  meanVoltage);
		} else if (getSourceMode() == SourceMode.VOLTAGE) {
			data.setPlottableValue(getName(),  meanCurrent);
		}

		data.addData(getName(), "mean current", new NexusGroupData(meanCurrent), "A");
		data.addData(getName(), "mean voltage", new NexusGroupData(meanVoltage), "V");

		if (firstReadoutInScan) {
			data.addElement(getName(), "dwell_time", new NexusGroupData(getDwellTime()), "ms", false);
			data.addElement(getName(), "number_of_readings", new NexusGroupData(getNumberOfReadings()), "", false);
			firstReadoutInScan = false;
		}

		return data;
	}

	@Override
	public void waitWhileBusy() throws DeviceException, InterruptedException {
		while (getStatus() == BUSY) {
			Thread.sleep(1000);
		}

		super.waitWhileBusy();
	}

	@Override
	public int[] getDataDimensions() throws DeviceException {
		return new int[] { 1 };
	}

	@Override
	public void prepareForCollection() throws DeviceException {
		// Nothing needed
	}

	@Override
	public void endCollection() throws DeviceException {
		// Nothing needed
	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		return false;
	}

	@Override
	public String getDescription() throws DeviceException {
		return "Keithley 2600 Series";
	}

	@Override
	public String getDetectorID() throws DeviceException {
		return getName();
	}

	@Override
	public String getDetectorType() throws DeviceException {
		return "SourceMeter";
	}

	@Override
	public boolean isBusy() throws DeviceException {
		return super.isBusy() || getStatus() == BUSY;
	}

	@Override
	public void atScanStart() throws DeviceException {
		super.atScanStart();
		firstReadoutInScan = true;
	}

	@Override
	public void atScanEnd() throws DeviceException {
		super.atScanEnd();
		firstReadoutInScan = false;
	}
}
