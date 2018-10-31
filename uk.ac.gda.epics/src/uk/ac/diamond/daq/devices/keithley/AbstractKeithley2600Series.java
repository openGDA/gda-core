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

package uk.ac.diamond.daq.devices.keithley;

import static java.util.stream.Collectors.toMap;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.scannable.PositionConvertorFunctions;
import gda.device.scannable.ScannableBase;
import gda.device.scannable.ScannableUtils;
import gda.factory.FactoryException;
import uk.ac.diamond.daq.concurrent.Async;

/**
 * Abstract base class for Keithley 2600 Series source meter.
 *
 * @author James Mudd
 * @since GDA 9.11
 */
public abstract class AbstractKeithley2600Series extends ScannableBase {

	private static final Logger logger = LoggerFactory.getLogger(AbstractKeithley2600Series.class);

	/** The time to wait after a output change for stability in ms */
	private long settleTimeMs = 1100;
	/** The time to wait after a output change for stability in ms */
	private long switchOnDelayTimeMs = 4000;

	/** The status showing if the voltage or current limits are reached */
	protected Status limitStatus = Status.NORMAL;

	/** Future to block on while settling */
	protected Future<?> setting;

	/** Flag indicating the next time {@link #rawAsynchronousMoveTo(Object)} is called the output will be switched on */
	protected boolean switchOnAtNextMove;

	protected enum SourceMode {
		/** The source will target a voltage setpoint by varying current */
		VOLTAGE("DCVOLTS"),
		/** The source will target a current setpoint by varying voltage */
		CURRENT("DCAMPS");

		private static final Map<String, SourceMode> LOOKUP = Arrays.asList(SourceMode.values()).stream().collect(toMap(k -> k.toEpics(), k -> k));

		private final String epicsName;

		private SourceMode(String epicsName) {
			this.epicsName = epicsName;
		}

		public String toEpics() {
			return epicsName;
		}

		protected static SourceMode fromEpics(String mode) {
			return LOOKUP.get(mode);
		}
	}

	protected enum ResistanceMode {
		TWO_WIRE("2-wire"), FOUR_WIRE("4-wire");

		private static final Map<String, ResistanceMode> LOOKUP = Arrays.asList(ResistanceMode.values()).stream().collect(toMap(k -> k.toEpics(), k -> k));

		private final String epicsName;

		private ResistanceMode(String epicsName) {
			this.epicsName = epicsName;
		}

		public String toEpics() {
			return epicsName;
		}

		protected static ResistanceMode fromEpics(String mode) {
			return LOOKUP.get(mode);
		}
	}

	protected enum Status {
		// Order is important ordinals must match EPICS enum
		NORMAL, VOLTAGE_LIMIT, CURRENT_LIMIT;

		// Cache values for efficiency
		private static final Status[] values = Status.values();

		/**
		 * Convert from EPICS enum ordinal to {@link Status}
		 *
		 * @param value
		 *            (ordinal)
		 * @return the MotorStatus instance corresponding to value
		 */
		public static Status fromEpicsInt(int value) {
			return values[value];
		}
	}

	@Override
	public void configure() throws FactoryException {
		setupNamesAndFormat();
	}

	public Set<SourceMode> getSourceModes() {
		return EnumSet.allOf(SourceMode.class);
	}

	public Set<ResistanceMode> getResistanceModes() {
		return EnumSet.allOf(ResistanceMode.class);
	}

	@Override
	public String toFormattedString() {
		try {
			// Custom to add mode
			return ScannableUtils.getFormattedCurrentPosition(this) + " mode: " + getSourceMode();
		} catch (Exception e) {
			logger.error("Error getting {} status", getName(), e);
			return String.format("%s : %s", getName(), VALUE_UNAVAILABLE);
		}
	}

	protected abstract SourceMode getSourceMode() throws DeviceException;

	protected void waitForSettling() {
		logger.debug("Waiting for settling...");
		try {
			Thread.sleep(getSettleTime());
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			logger.warn("Interrupted waiting for settling", e);
		}
	}

	protected void waitForSwitchOn() {
		logger.debug("Waiting for switch on...");
		try {
			Thread.sleep(getSwitchOnDelayTimeMs());
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			logger.warn("Interrupted waiting for switch on", e);
		}
	}

	/**
	 * The idea is to all this whenever an current or voltage limit might be reached. If it is this will throw aborting the ongoing request or scan.
	 *
	 * @throws DeviceException
	 */
	protected void checkStatusAndThrow() throws DeviceException {
		if (limitStatus != Status.NORMAL) {
			throw new DeviceException(getName() + " is at " + limitStatus);
		}
	}

	@Override
	public Object rawGetPosition() throws DeviceException {
		// Demand (input names)
		double demand = getDemand();
		// Read backs (extra names)
		double voltage = getActualVoltage();
		double current = getActualCurrent();
		double resistance = getActualResistance();

		return new double[] { demand, voltage, current, resistance };
	}

	protected abstract double getActualResistance() throws DeviceException;

	protected abstract double getActualCurrent() throws DeviceException;

	protected abstract double getActualVoltage() throws DeviceException;

	private double getDemand() throws DeviceException {
		SourceMode sourceMode = getSourceMode();
		switch (sourceMode) {
		case VOLTAGE:
			return getDemandVoltage();
		case CURRENT:
			return getDemandCurrent();
		default:
			throw new IllegalStateException("Unkown SourceMode: " + sourceMode);
		}
	}

	protected abstract double getDemandCurrent() throws DeviceException;

	protected abstract double getDemandVoltage() throws DeviceException;

	protected void setupNamesAndFormat() {
		setInputNames(new String[] { "demand" });
		setExtraNames(new String[] { "voltage", "current", "resistance" });
		setOutputFormat(new String[] { "%5.5g", "%5.5g", "%5.5g", "%5.5g" });
	}

	@Override
	public boolean isBusy() throws DeviceException {
		checkStatusAndThrow();
		return !setting.isDone();
	}

	@Override
	public void rawAsynchronousMoveTo(Object position) throws DeviceException {
		checkStatusAndThrow();
		double demand = PositionConvertorFunctions.toDouble(position);
		setting = Async.submit(() -> setOutputDemandAndWaitToSettle(demand));
	}

	protected abstract void setOutputDemandAndWaitToSettle(double demand);

	@Override
	public void waitWhileBusy() throws DeviceException, InterruptedException {
		logger.trace("waitWhileBusy called");
		checkStatusAndThrow();
		if (setting != null) {
			try {
				setting.get(); // Waits for settling to finish
			} catch (Exception e) {
				throw new DeviceException("Exception waiting for settling", e);
			}
		}
		checkStatusAndThrow();
		logger.trace("Settling finshed");
	}

	@Override
	public void atScanEnd() throws DeviceException {
		outputOff();
	}

	protected abstract void outputOff() throws DeviceException;

	protected abstract void outputOn() throws DeviceException;

	@Override
	public void atCommandFailure() throws DeviceException {
		outputOff();
	}

	@Override
	public void stop() throws DeviceException {
		outputOff();
	}

	public void setSourceMode(String mode) throws DeviceException {
		setSourceMode(SourceMode.valueOf(mode.toUpperCase()));
	}

	protected abstract void setSourceMode(SourceMode mode) throws DeviceException;

	public void setResistanceMode(String mode) throws DeviceException {
		setResistanceMode(ResistanceMode.valueOf(mode.toUpperCase()));
	}

	protected abstract void setResistanceMode(ResistanceMode mode) throws DeviceException;

	@Override
	public void atScanStart() throws DeviceException {
		switchOnAtNextMove = true;
	}

	public abstract ResistanceMode getResistanceMode() throws DeviceException;

	public abstract boolean isOutputOn() throws DeviceException;

	/**
	 * @return The delay applied after the demand is changed.
	 */
	public long getSettleTime() {
		return settleTimeMs;
	}

	/**
	 * @param settleTime
	 *            The delay to be applied after the demand is changed.
	 */
	public void setSettleTime(long settleTime) {
		this.settleTimeMs = settleTime;
	}

	/**
	 * @return The delay applied after the source is switched on.
	 */
	public long getSwitchOnDelayTimeMs() {
		return switchOnDelayTimeMs;
	}

	/**
	 * @param switchOnDelayTimeMs
	 *            The delay applied after the source is switched on.
	 */
	public void setSwitchOnDelayTimeMs(long switchOnDelayTimeMs) {
		this.switchOnDelayTimeMs = switchOnDelayTimeMs;
	}

}