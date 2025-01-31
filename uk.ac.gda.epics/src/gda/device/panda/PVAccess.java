/*-
 * Copyright © 2025 Diamond Light Source Ltd.
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

package gda.device.panda;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import org.epics.pvaClient.PvaClient;
import org.epics.pvaClient.PvaClientChannel;
import org.epics.pvaClient.PvaClientGet;
import org.epics.pvaClient.PvaClientGetData;
import org.epics.pvaClient.PvaClientMonitor;
import org.epics.pvaClient.PvaClientPut;
import org.epics.pvaClient.PvaClientPutData;
import org.epics.pvdata.pv.PVDouble;
import org.epics.pvdata.pv.PVField;
import org.epics.pvdata.pv.PVInt;
import org.epics.pvdata.pv.PVIntArray;
import org.epics.pvdata.pv.PVString;
import org.epics.pvdata.pv.PVStringArray;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.PVUByteArray;
import org.epics.pvdata.pv.PVUIntArray;
import org.epics.pvdata.pv.PVUShortArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;

public class PVAccess {
	private static final Logger logger = LoggerFactory.getLogger(PVAccess.class);
	private String pvName;
	private double connectionTimeoutSecs = 5.0;
	private PvaClientGet pvGet;
	private PvaClientPut pvPut;

	/** Timeout, poll interval used by {#link {@link #waitForCondition(Function)}
	 * {@link #waitForCondition(Function, double, double)}  functions **/
	private double conditionTimeoutSec = 5.0;
	private double conditionPollIntervalSecs = 0.1;
	private PvaClientChannel clientChannel;
	private boolean connected;

	public PVAccess(String pvName) {
		this(pvName, true);
	}

	/**
	 * Create new PVAccess and optionally connect to it.
	 * @param pvName
	 * @param connect
	 */
	public PVAccess(String pvName, boolean connect) {
		this.pvName = pvName;
		if (connect) {
			connect();
		}
	}

	/**
	 * Create PvaClientGet, PvaClientPut objects to get and set values for the PV.
	 */
	public void connect() {
		if (connected) {
			return;
		}
		logger.info("Creating PVA channels : {}", pvName);
		PvaClient pvaClient = PvaClient.get("pva");
		clientChannel = pvaClient.channel(pvName, "pva", connectionTimeoutSecs);
		pvGet = clientChannel.createGet();
		pvPut = clientChannel.createPut();

		pvGet.connect();
		pvPut.connect();

		pvGet.waitConnect();
		pvPut.waitConnect();
		connected = true;
	}

	public void disconnect() {
		logger.info("Disconnecting PVA channels : {}", pvName);
		pvGet.destroy();
		pvPut.destroy();
		connected = false;
	}

	private PvaClientMonitor createMonitor() {
		return clientChannel.createMonitor("record[queueSize=1]field()");
	}

	/**
	 * Return PVField corresponding to PV value from PVStructure object
	 * <li> For scalar/structure value this is just the 'value' field.
	 * <li> For enum PV the 'value' field is an enum, and the PV value field is first subfield inside it.
	 * @param pvStructure
	 * @return PVField object for the PV value
	 */
	private PVField getValuePvField(PVStructure pvStructure) {
		PVStructure valueField = pvStructure.getSubField(PVStructure.class, "value");

		// value from structure (assume first field in struct is the value)
		if (valueField != null && valueField.getField().getID().equals("enum_t")) {
			logger.trace("PVField is enum");
			PVField[] fields = valueField.getPVFields();
			return fields[0];
		}

		// value from scaler
		return pvStructure.getSubField("value");
	}

	/**
	 * Apply integer, double or string value to PV
	 *
	 * @param value
	 * @throws DeviceException
	 */
	public void putValue(Object value) throws DeviceException {
		logger.debug("Setting value {} = {}", pvName, value);
		PvaClientPutData putData = pvPut.getData();
		PVField pvField = getValuePvField(putData.getPVStructure());
		switch(pvField) {
			case PVInt pvInt when value instanceof Integer intVal -> pvInt.put(intVal);
			case PVDouble pvDbl when value instanceof Double dblValue -> pvDbl.put(dblValue);
			case PVString pvStr when value instanceof String strValue -> pvStr.put(strValue);
			default -> {
				String msg="Conversion of value %s to %s was not handled by putValue() function".formatted(value, pvField.getClass());
				throw new DeviceException(msg);
			}
		}
		pvPut.put();
	}

	/**
	 * Apply an array of values to a field in a {@link PVStructure}.
	 *
	 * <li> Only deals with Integer or String values
	 * <li> Conversion of integer values to short, byte is done automatically, depending on the type of
	 * the PVStructure field
	 * <li> Only field types : {@link PVStringArray}, {@link PVUIntArray}, {@link PVIntArray}, {@link PVUShortArray},
	 * are handled {@link PVUByteArray}). DeviceException is thrown for other types.
	 *
	 * @param pvStructure pv structure
	 * @param fieldName field name within the structure that the values should be applied to
	 * @param vals list of integer or string values to be set
	 *
	 * @throws DeviceException  if field type was not handled
	 *
	 */
	public <T> void putArray(PVStructure pvStructure, String fieldName, List<T> vals) throws DeviceException {
		PVField field = pvStructure.getSubField(fieldName);
		logger.debug("Setting values for {} ({}) : {}",fieldName, field.getClass().getCanonicalName(), vals);

		// First see if we are dealing with String values
		if (field instanceof PVStringArray pvStringArray) {
			if (!(vals.get(0) instanceof String)) {
				throw new DeviceException("Could not convert values to String array");
			}
			String[] stringArrayVals = new String[vals.size()];
			for(int i=0; i<vals.size(); i++) {
				stringArrayVals[i] = (String) vals.get(i);
			}
			pvStringArray.setCapacity(stringArrayVals.length);
			pvStringArray.put(0, stringArrayVals.length, stringArrayVals, 0);
			return;
		}

		// Deal with integer values
		if (!(vals.get(0) instanceof Integer)) {
			throw new DeviceException("Could not convert values to Integer array");
		}

		int[] arrayVals = new int[vals.size()];
		for(int i=0; i<vals.size(); i++) {
			arrayVals[i] = (Integer) vals.get(i);
		}

		// Apply the integer value to PV, converting the int array to short or byte array as needed.
		// Unfortunately the 'setCapacity' and 'put' methods for different PVField types are all on different interfaces!!!
		switch(field) {
			case PVUIntArray pvInt -> {
				pvInt.setCapacity(arrayVals.length);
				pvInt.put(0, arrayVals.length, arrayVals, 0);
			}
			case PVIntArray pvInt -> {
				pvInt.setCapacity(arrayVals.length);
				pvInt.put(0, arrayVals.length, arrayVals, 0);
			}
			case PVUShortArray pvInt -> {
				short[] shortArray = new short[arrayVals.length];
				for(int i=0; i<arrayVals.length; i++) {
					shortArray[i] = (short)arrayVals[i];
				}
				pvInt.setCapacity(shortArray.length);
				pvInt.put(0, shortArray.length, shortArray, 0);
			}
			case PVUByteArray pvInt -> {
				byte[] byteArray = new byte[arrayVals.length];
				for(int i=0; i<arrayVals.length; i++) {
					byteArray[i] = (byte)arrayVals[i];
				}
				pvInt.setCapacity(byteArray.length);
				pvInt.put(0, byteArray.length, byteArray, 0);
			}
			default ->
				throw new DeviceException("Could not set values on PVStructure field type %s".formatted(field.getClass().getCanonicalName()));
		}
	}

	public void putComplete() {
		pvPut.put();
	}

	public Object getValue() throws DeviceException {
		return getValue(Object.class);
	}

	/**
	 * Read value from PV and cast to specific class type
	 *
	 * @param <T>
	 * @param objectClass
	 * @return PV value cast to required type
	 *
	 * @throws DeviceException if returned PV type could not be converted to requested class type
	 */
	public <T> T getValue(Class<T> objectClass) throws DeviceException {
		logger.debug("Reading value from {}", pvName);
		// update with the latest value from Epics
		pvGet.issueGet();
		pvGet.waitGet();
		// get the actual data
		PvaClientGetData getData = pvGet.getData();
		PVField pvField = getValuePvField(getData.getPVStructure());
		Object value = extractPvValue(pvField);
		if (objectClass.isInstance(value)) {
			return objectClass.cast(value);
		}
		throw new DeviceException("Could not convert "+value+" to "+objectClass.getCanonicalName());
	}

	/**
	 * Extract scalar value stored in PVField object (if value is int, double or String)
	 *
	 * @param pvField
	 * @return int, double, or String value ; otherwise return the original PVField
	 * @throws DeviceException
	 */
	private Object extractPvValue(PVField pvField) {
		var val = switch(pvField) {
			case PVInt pvInt -> pvInt.get();
			case PVDouble pvDbl -> pvDbl.get();
			case PVString pvStr -> pvStr.get();
			default -> {
				logger.warn("Conversion of {} to scalar value was not handled by getValue() function", pvField.getClass());
				yield pvField;
			}
		};
		logger.trace("Value = {}", val);
		return val;
	}

	public boolean waitForValue(Object desiredValue) throws InterruptedException, DeviceException {
		return waitForCondition(pvValue -> pvValue.equals(desiredValue));
	}

	/**
	 * Calls {@link  #waitForCondition(Predicate, double, double)} with default values for timeout and update interval
	 *
	 * @param testFunction
	 * @return true if condition evaluates to true within timeout, false otherwise
	 * @throws InterruptedException
	 * @throws DeviceException
	 */
	public boolean waitForCondition(Predicate<Object> testFunction) throws InterruptedException, DeviceException {
		return waitForCondition(testFunction, conditionTimeoutSec, conditionPollIntervalSecs);
	}

	/**
	 * Wait for condition defined by testFunction to evaluate to true.
	 * Uses polling to detect changes in PV value.
	 *
	 * @param testFunction - function to be that takes PV readback value as input and returns true or false.
	 * @param timeoutSeconds
	 * @param intervalSeconds
	 * @return true if function evaluated to true within timeout, false otherwise
	 * @throws InterruptedException
	 * @throws DeviceException
	 */
	public boolean waitForCondition(Predicate<Object> testFunction, double timeoutSeconds, double intervalSeconds) throws InterruptedException, DeviceException {

		if (Boolean.TRUE.equals(testFunction.test(getValue()))) {
			return true;
		}

		double timeLeft = timeoutSeconds;
		while(timeLeft > 0) {
			Boolean result = testFunction.test(getValue());
			if (Boolean.TRUE.equals(result)) {
				return true;
			}
			timeLeft -= intervalSeconds;
			Thread.sleep((long)(1000*intervalSeconds));
		}
		return false;
	}

	/**
	 * Wait for condition defined by testFunction to evaluate to true.
	 * Uses monitor callbacks to wait for PV value to change.
	 *
	 * @param testFunction - function to be that takes pv readback value as input and returns true or false.
	 * @param timeoutSeconds
	 * @return true if function evaluated to true within timeout, false otherwise
	 * @throws DeviceException
	 */
	public boolean monitorForCondition(Predicate<Object> testFunction, double timeoutSeconds) throws DeviceException {
		logger.debug("Monitoring {} for condition", pvName);
		if (Boolean.TRUE.equals(testFunction.test(getValue(Object.class)))) {
			logger.debug("{} already in correct state", pvName);
			return true;
		}
		var monitor = createMonitor();
		monitor.start();
		double timeLeft = timeoutSeconds;
		boolean testResultIsGood = false;

		// wait up to 'timeoutSecs' for testFunction to evaluate to true
		while(timeLeft > 0 && !testResultIsGood) {
			logger.debug("Remaining timeout = {} secs", timeLeft);
			long startTime = System.currentTimeMillis();
			// wait for PV to change
			if (monitor.waitEvent(timeLeft)) {
				// extract the value using data from the monitor event and evaluate testFunction
				PVField pvField = getValuePvField(monitor.getData().getPVStructure());
				Object value = extractPvValue(pvField);
				testResultIsGood = testFunction.test(value);
				logger.debug("{} changed : value = {}, testFunction = {}", pvName, value, testResultIsGood);
				monitor.releaseEvent();
			}
			// update timeLeft by subtracting time spent waiting for PV to change
			long timeTakenMs = System.currentTimeMillis() - startTime;
			timeLeft -= 0.001*timeTakenMs;
		}

		logger.debug("Finished monitoring : final state of testfunction = {}", testResultIsGood);

		monitor.stop();
		monitor.destroy();

		return testResultIsGood;
	}

	public String getPvName() {
		return pvName;
	}

	public void setPvName(String pvName) {
		this.pvName = pvName;
	}

	public double getConnectionTimeoutSecs() {
		return connectionTimeoutSecs;
	}

	public void setConnectionTimeoutSecs(double connectionTimeoutSecs) {
		this.connectionTimeoutSecs = connectionTimeoutSecs;
	}

	public double getConditionTimeoutSec() {
		return conditionTimeoutSec;
	}

	/**
	 * Set the timeout used by {@link #waitForCondition(Predicate)}
	 * @param conditionTimeoutSec
	 */
	public void setConditionTimeoutSec(double conditionTimeoutSec) {
		this.conditionTimeoutSec = conditionTimeoutSec;
	}

	public double getConditionPollIntervalSecs() {
		return conditionPollIntervalSecs;
	}

	/**
	 * Set the poll interval time used by {@link #waitForCondition(Predicate)}
	 * @param conditionPollIntervalSecs
	 */
	public void setConditionPollIntervalSecs(double conditionPollIntervalSecs) {
		this.conditionPollIntervalSecs = conditionPollIntervalSecs;
	}

	/**
	 * Return the 'value' field for putting values on the PV
	 * @return PVStructure
	 */
	public PVField getPvPutValueField() {
		PvaClientPutData putData = pvPut.getData();
		return getValuePvField(putData.getPVStructure());
	}
}
