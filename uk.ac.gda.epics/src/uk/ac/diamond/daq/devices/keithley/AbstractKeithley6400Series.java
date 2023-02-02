/*-
 * Copyright © 2023 Diamond Light Source Ltd.
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

import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.detector.NexusDetector;
import gda.device.scannable.PositionConvertorFunctions;
import gda.device.scannable.ScannableBase;
import gda.device.scannable.ScannableUtils;
import uk.ac.diamond.daq.concurrent.Async;

public abstract class AbstractKeithley6400Series extends ScannableBase implements NexusDetector {

	private static final Logger logger = LoggerFactory.getLogger(AbstractKeithley6400Series.class);

	protected long settleTimeMs = 1;

	private String basePVName = null;

	private int status=IDLE;
	private int waitWhileBusySleepTime=100;

	protected boolean firstReadoutInScan;

	protected Future<?> setting = null;

	public abstract double getCollectionTimeS() throws DeviceException;

	public abstract String getReadbackRate() throws DeviceException;

	public abstract void setReadbackRate(double demand) throws DeviceException;

	public abstract double getReading() throws DeviceException;

	protected abstract boolean isDisabled() throws DeviceException;

	public String getBasePVName() {
		return basePVName;
		}

	public void setBasePVName(String basePVName) {
		this.basePVName = basePVName;
		}

	/** Set readback rate here instead of exposure time in scan command "scan detector exposure" */
	@Override
	public void setCollectionTime(double time) throws DeviceException {
		setReadbackRate(time);
	}

	/* Simply get some dummy period (note, not exposure time nor readback rate) after which can read data */
	@Override
	public double getCollectionTime() throws DeviceException {
		return getCollectionTimeS();
	}

	/** Return readback rate and current in a "pos" command */
	@Override
	public Object rawGetPosition() throws DeviceException {
		// input names
		String rr = getReadbackRate();
		// extra names
		String cc = Double.toString(getReading());
		return new String[] {rr, cc};
	}

	@Override
	public void collectData() throws DeviceException {
		setStatus(BUSY);
		// there is no "acquire" command, using some dummy collection time
		logger.debug("{}: Initiating acquisition", getName());
		try {
			/* Thread.sleep(getDummyCollectionTimeMs()); */
			long c_time = (long) (getCollectionTime()*1000);
			Thread.sleep(c_time);
		} catch (InterruptedException e) {
			logger.error("Failed to sleep for acquisition time", e);
		} finally {
		setStatus(IDLE);
		}
	}

	@Override
	public void waitWhileBusy() throws DeviceException, InterruptedException {
		while (status == BUSY) {
			Thread.sleep(getWaitWhileBusySleepTime());
		}
		super.waitWhileBusy();
		logger.debug("Acquisition for {} finished", getName());
	}

	protected void waitForSettling() {
		logger.debug("Waiting for settling...");
		try {
			Thread.sleep(getSettleTimeMs());
			// if (isFirstPoint) { Thread.sleep(getAdditionalFirstPointSettleTimeMs()); }
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			logger.warn("Interrupted waiting for settling", e);
		}
	}

	protected void setOutputDemandAndWaitToSettle(double demand) {
		// Set readback rate when used with "pos" command - called from rawAsynchronousMoveTo
		try {
			setCollectionTime(demand);
		} catch (DeviceException e) {
			throw new RuntimeException("Failed to set readback rate", e);
		}
		waitForSettling();
	}

	protected void setupNamesAndFormat() {
		setInputNames(new String[] { "readback_rate" });
		setExtraNames(new String[] { "current" });
		setOutputFormat(new String[] { "%5.5g", "%5.5g" });

	}

	@Override
	public int[] getDataDimensions() throws DeviceException {
		return new int[] { 1 };
	}

	@Override
	public void rawAsynchronousMoveTo(Object position) throws DeviceException {
		double demand = PositionConvertorFunctions.toDouble(position);
		setting = Async.submit(() -> setOutputDemandAndWaitToSettle(demand));
	}

	@Override
	public boolean isBusy() throws DeviceException {
		if (setting != null){
			return !setting.isDone() || status == BUSY;
			}
		return status == BUSY;
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

	@Override
	public String getDescription() {
		return "Keithley 6487 as NXDetector";
	}

	@Override
	public void prepareForCollection() throws DeviceException {
		// pass
	}

	@Override
	public void endCollection() throws DeviceException {
		// pass
	}

	@Override
	public int getStatus() throws DeviceException {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public long getSettleTimeMs() {
		return settleTimeMs;
	}

	public void setSettleTimeMs(long settleTimeMs) {
		this.settleTimeMs = settleTimeMs;
	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		return false;
	}


	public int getWaitWhileBusySleepTime() {
		return waitWhileBusySleepTime;
	}

	public void setWaitWhileBusySleepTime(int waitWhileBusySleepTime) {
		this.waitWhileBusySleepTime = waitWhileBusySleepTime;
	}

	@Override
	public String getDetectorID() throws DeviceException {
		return getName();
	}

	@Override
	public String getDetectorType() throws DeviceException {
		return "Keithley 6487";
	}

	@Override
	public String toFormattedString() {
		try {
			return ScannableUtils.getFormattedCurrentPosition(this);
		} catch (Exception e) {
			logger.error("Error getting {} status", getName(), e);
			return String.format("%s : %s", getName(), VALUE_UNAVAILABLE);
		}
	}

}
