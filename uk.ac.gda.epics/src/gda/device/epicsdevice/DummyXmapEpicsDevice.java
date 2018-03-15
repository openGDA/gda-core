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

package gda.device.epicsdevice;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.scannable.ScannableBase;
import gov.aps.jca.dbr.DBR_Enum;

/**
 * Epics device for simulating XMAP.
 * <p>
 * This provides equivalent functionality to DummyXmapController, but at a lower level, thus allowing us to test:<br>
 * - the real XMAP controller (EpicsXmapController), and<br>
 * - DummyXmapEDXDMappingController, which is only a very minimal variant of the real EDXDMappingController
 */
public class DummyXmapEpicsDevice implements XmapEpicsDevice {

	private static final Logger logger = LoggerFactory.getLogger(DummyXmapEpicsDevice.class);

	private static final String NOT_IMPLEMENTED = "Not implemented";
	private static final double REAL_TIME = 1000.0;
	private static final int EVENTS = -1000;

	private final DummyEpicsChannel statusChannel = new DummyEpicsChannel();

	private static final long RNG_SEED = 42424242;
	private final Random generator = new Random(RNG_SEED);

	private int numberOfBins = 1024;
	private double acquisitionTime;
	private int numberOfROIs;

	private long acquisitionStartTime = 0;

	@Override
	public IEpicsChannel createEpicsChannel(ReturnType returnType, String record, String field, double putTimeout) {
		return statusChannel;
	}

	@Override
	public IEpicsChannel createEpicsChannel(ReturnType returnType, String record, String field) {
		return statusChannel;
	}

	@Override
	public void dispose() throws DeviceException {
		throw new NotImplementedException(NOT_IMPLEMENTED);
	}

	@Override
	public void closeUnUsedChannels() throws DeviceException {
		throw new NotImplementedException(NOT_IMPLEMENTED);
	}

	@Override
	public boolean isConfigured() {
		return true;
	}

	@Override
	public void setValue(Object type, String record, String field, Object val, double putTimeout) throws DeviceException {
		logger.debug("setValue(type={}, record={}, field={}, val={}, putTimeout={}", type, record, field, val, putTimeout);
		if (type != null) {
			throw new DeviceException("Only null is allowed for type value");
		}
		// Value will be set immediately, so timeout is irrelevant
		setValue(record, field, val);
	}

	// Record names that are valid, but setting their value requires no action in this dummy
	private final List<String> setValueNoActionRequired = Arrays.asList("ERASEALL", "ERASESTART", "SCAACTIVATE", "SETPRESETTYPE", "SETRESUME");

	@Override
	public void setValue(String record, String field, Object val) throws DeviceException {
		logger.debug("setValue(record={}, field={}, val={}", record, field, val);

		if (setValueNoActionRequired.contains(record)) {
			// no action required
		} else if (record.equals("ACQUIRE")) {
			handleAcquire(val);
		} else if (record.equals("STOPALL")) {
			stopAcquisition();
		} else if (record.equals("SETNBINS")) {
			numberOfBins = (int) val;
		} else if (record.equals("SETPRESETVALUE")) {
			acquisitionTime = (double) val;
		} else if (record.startsWith("SCALOWLIMITS") || record.startsWith("SCAHIGHLIMITS")) {
			if (val instanceof double[]) {
				numberOfROIs = ((double[]) val).length;
			} else {
				logger.warn("Trying to set {} with a value that is not an array of doubles", record);
			}
		} else {
			final String message = String.format("Unknown record %s passed to setValue()", record);
			logger.error(message);
			throw new DeviceException(message);
		}
	}

	private void handleAcquire(Object val) throws DeviceException {
		if (val.equals(0)) {
			stopAcquisition();
		} else if (val.equals(1)) {
			startAcquisition();
		} else {
			final String message = String.format("Invalid value %s for ACQUIRE", val);
			logger.error(message);
			throw new DeviceException(message);
		}
	}

	private void startAcquisition() {
		acquisitionStartTime = System.currentTimeMillis();
		statusChannel.notifyDeviceBusy();
	}

	private void stopAcquisition() {
		statusChannel.notifyDeviceNotBusy();
		acquisitionStartTime = 0;
	}

	@Override
	public void setValueNoWait(String record, String field, Object val) throws DeviceException {
		// Value will be set immediately, so waiting is irrelevant
		setValue(record, field, val);
	}

	@Override
	public Object getValue(ReturnType returnType, String record, String field) throws DeviceException {
		logger.debug("getValue(type={}, record={}, field={}", returnType, record, field);
		if (record.equals("GETNBINS")) {
			return numberOfBins;
		} else if (record.startsWith("REALTIME")) {
			return REAL_TIME;
		} else if (record.equals("GETPRESETVALUE")) {
			return acquisitionTime;
		} else if (record.startsWith("DATA") || record.startsWith("SCADATA") ) {
			return getIntegerData();
		} else if (record.startsWith("ENERGYBINS") ) {
			return getDoubleData(3.6);
		} else if (record.equals("SCAELEMENTS")) {
			return numberOfROIs;
		} else if (record.startsWith("EVENTS")) {
			return EVENTS;
		} else if (record.startsWith("INPUTCOUNTRATE") || record.startsWith("OUTPUTCOUNTRATE")) {
			return generator.nextDouble() * 100d;
		} else if (record.startsWith("SCALOWLIMITS") || record.startsWith("SCAHIGHLIMITS")) {
			return new double[][] {};
		} else if (record.startsWith("ELIVETIME") || record.startsWith("TLIVETIME")) {
			return getTimeSinceAcquisitionStart();
		} else if (record.equals("DeadTime")) {
			return generator.nextDouble() * 0.02;
		} else {
			final String message = String.format("Unknown record %s passed to getValue()", record);
			logger.error(message);
			throw new DeviceException(message);
		}
	}

	private int[] getIntegerData() {
		final int[] data = new int[numberOfBins];
		for (int i = 0; i < numberOfBins; i++) {
			// say that for this simulation cannot count more than 10MHz
			data[i] = generator.nextInt((int) (REAL_TIME) * 10000);
		}
		return data;
	}

	private double[] getDoubleData(double maxValue) {
		final double[] data = new double[numberOfBins];
		for (int i = 0; i < numberOfBins; i++) {
			data[i] = generator.nextDouble() * maxValue;
		}
		return data;
	}

	private double getTimeSinceAcquisitionStart() {
		if (acquisitionStartTime == 0) {
			return 0.0;
		} else {
			return (System.currentTimeMillis() - acquisitionStartTime) / 1000.0;
		}
	}

	@Override
	public String getValueAsString(String record, String field) throws DeviceException {
		return (String) getValue(ReturnType.DBR_NATIVE, record, field);
	}

	@Override
	public String getRecordPV(String mcaName) {
		// Called by EDXDMappingElement constructor, but value not used in dummy system
		return "BLXXI-EA-DET-01:" + mcaName;
	}

	/**
	 * Private class to simulate the Epics channel<br>
	 * All the EDXDController actually uses is the status update callback
	 */
	private static class DummyEpicsChannel extends ScannableBase implements IEpicsChannel {

		private static final EpicsMonitorEvent busyEvent = new EpicsMonitorEvent(new DBR_Enum(1));
		private static final EpicsMonitorEvent notBusyEvent = new EpicsMonitorEvent(new DBR_Enum(0));

		private Object value;

		public DummyEpicsChannel() {
			super();
			setName("dummy status channel");
		}

		@Override
		public boolean isBusy() throws DeviceException {
			return false;
		}
		@Override
		public Object getValue() throws DeviceException {
			return value;
		}
		@Override
		public void setValue(Object position) throws DeviceException {
			this.value = position;
		}
		@Override
		public void dispose() {
			// nothing to do
		}

		public void notifyDeviceBusy() {
			notifyIObservers(this, busyEvent);
		}

		public void notifyDeviceNotBusy() {
			notifyIObservers(this, notBusyEvent);
		}
	}
}