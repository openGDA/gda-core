/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package gda.epics;

import static java.text.MessageFormat.format;
import static org.apache.commons.lang.ArrayUtils.toObject;
import static org.apache.commons.lang.ArrayUtils.toPrimitive;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.epics.connection.EpicsController;
import gda.epics.util.EpicsGlobals;
import gda.observable.Observable;
import gda.observable.ObservableUtil;
import gda.observable.Observer;
import gov.aps.jca.CAException;
import gov.aps.jca.CAStatus;
import gov.aps.jca.CAStatusException;
import gov.aps.jca.Channel;
import gov.aps.jca.Monitor;
import gov.aps.jca.TimeoutException;
import gov.aps.jca.dbr.BYTE;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.DBRType;
import gov.aps.jca.dbr.DOUBLE;
import gov.aps.jca.dbr.ENUM;
import gov.aps.jca.dbr.FLOAT;
import gov.aps.jca.dbr.INT;
import gov.aps.jca.dbr.SHORT;
import gov.aps.jca.dbr.STRING;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;
import gov.aps.jca.event.PutEvent;
import gov.aps.jca.event.PutListener;

/**
 * A class with factory methods to return {@link PV}s representing Epics Process Variables. The {@link PV}s are lazy
 * meaning they don't connect until the first request through the Channel Access (CA) layer is to be made. The
 * {@link PV}s created are all backed by the {@link EpicsController} singleton.
 * <p>
 * The factory methods are specified by Java type. An underlying Epics type will be chosen based on this table:
 * <li> {@link Double} : {@link DBRType#DOUBLE}
 * <li> {@link Float} : {@link DBRType#FLOAT}
 * <li> {@link Integer} : {@link DBRType#INT}
 * <li> {@link Short} : {@link DBRType#SHORT}
 * <li> {@link Byte} : {@link DBRType#BYTE}
 * <li> {@link String} : {@link DBRType#STRING}
 * <li> {@link Enum} : {@link DBRType#ENUM}
 * <li> {@link Boolean} : {@link DBRType#ENUM}, {@link DBRType#INT} or {@link DBRType#SHORT} For referenence:
 * <p>
 * http://epics.cosylab.com/cosyjava/JCA-Common/Documentation/CAproto.html
 * <p>
 * http://java.sun.com/docs/books/jls/third_edition/html/typesValues.html#4.2
 */
public class LazyPVFactory {

	private static EpicsController epicsController = EpicsController.getInstance();

	public static final String CHECK_CHANNELS_PROPERTY_NAME = "gda.epics.lazypvfactory.check.channels";
	private static final String UNEXPECTED_TYPE_CONFIGURED = "Unexpected type configured";

	private LazyPVFactory() {}// Hide implicit constructor

	/**
	 * Envisaged for testing only. EpicsController is a singleton.
	 *
	 * @param controller
	 *            likely a mock controller!
	 */
	public static void setEpicsController(EpicsController controller) {
		epicsController = controller;
	}

	public static <E> PV<E> newEnumPV(String pvName, Class<E> enumType) {
		return new LazyPV<>(epicsController, pvName, enumType);
	}

	public static PV<Byte> newBytePV(String pvName) {
		return new LazyPV<>(epicsController, pvName, Byte.class);
	}

	public static PV<Double> newDoublePV(String pvName) {
		return new LazyPV<>(epicsController, pvName, Double.class);
	}

	public static PV<Float> newFloatPV(String pvName) {
		return new LazyPV<>(epicsController, pvName, Float.class);
	}

	public static PV<Integer> newIntegerPV(String pvName) {
		return new LazyPV<>(epicsController, pvName, Integer.class);
	}

	public static PV<Integer> newIntegerFromEnumPV(String pvName) {
		return new LazyPV<>(epicsController, pvName, Integer.class);
	}

	public static PV<Short> newShortPV(String pvName) {
		return new LazyPV<>(epicsController, pvName, Short.class);
	}

	/**
	 * Create a new String PV that connects an EPICS STRING PV. DBR STRINGs support <40 useful characters.
	 * @param pvName
	 * @return the String PV
	 */
	public static PV<String> newStringPV(String pvName) {
		return new LazyPV<>(epicsController, pvName, String.class);
	}

	public static PV<String> newStringFromEnumPV(String pvName) {
		return new LazyPV<>(epicsController, pvName, String.class);
	}

	public static PV<String> newStringFromWaveformPV(String pvName) {
		return new StringFromWaveform(newByteArrayPV(pvName));
	}

	public static PV<Byte[]> newByteArrayPV(String pvName) {
		return new LazyPV<>(epicsController, pvName, Byte[].class);
	}

	public static PV<Double[]> newDoubleArrayPV(String pvName) {
		return new LazyPV<>(epicsController, pvName, Double[].class);
	}

	public static PV<Float[]> newFloatArrayPV(String pvName) {
		return new LazyPV<>(epicsController, pvName, Float[].class);
	}

	public static PV<Integer[]> newIntegerArrayPV(String pvName) {
		return new LazyPV<>(epicsController, pvName, Integer[].class);
	}

	public static PV<Short[]> newShortArrayPV(String pvName) {
		return new LazyPV<>(epicsController, pvName, Short[].class);
	}

	public static PV<Boolean> newBooleanFromDoublePV(String pvName) {
		return new BooleanFromDouble(new LazyPV<>(epicsController, pvName, Double.class));
	}

	public static PV<Boolean> newBooleanFromIntegerPV(String pvName) {
		return new BooleanFromInteger(new LazyPV<>(epicsController, pvName, Integer.class));
	}

	public static PV<Boolean> newBooleanFromShortPV(String pvName) {
		return new BooleanFromShort(new LazyPV<>(epicsController, pvName, Short.class));
	}

	// NOTE: just uses a short under the covers, so there is no enumType parameter
	public static PV<Boolean> newBooleanFromEnumPV(String pvName) {
		return new BooleanFromShort(new LazyPV<>(epicsController, pvName, Short.class));
	}

	public static <E> NoCallbackPV<E> newNoCallbackEnumPV(String pvName, Class<E> enumType) {
		return new NoCallback<>(newEnumPV(pvName, enumType));
	}

	public static NoCallbackPV<Byte> newNoCallbackBytePV(String pvName) {
		return new NoCallback<>(newBytePV(pvName));
	}

	public static NoCallbackPV<Double> newNoCallbackDoublePV(String pvName) {
		return new NoCallback<>(newDoublePV(pvName));
	}

	public static NoCallbackPV<Float> newNoCallbackFloatPV(String pvName) {
		return new NoCallback<>(newFloatPV(pvName));
	}

	public static NoCallbackPV<Integer> newNoCallbackIntegerPV(String pvName) {
		return new NoCallback<>(newIntegerPV(pvName));
	}

	public static NoCallbackPV<Short> newNoCallbackShortPV(String pvName) {
		return new NoCallback<>(newShortPV(pvName));
	}

	/**
	 * Create a new String PV that connects an EPICS STRING PV. DBR STRINGs support <40 useful characters.
	 * @param pvName
	 * @return the String PV
	 */
	public static NoCallbackPV<String> newNoCallbackStringPV(String pvName) {
		return new NoCallback<>(newStringPV(pvName));
	}

	public static NoCallbackPV<String> newNoCallbackStringFromWaveformPV(String pvName) {
		return new NoCallback<>(newStringFromWaveformPV(pvName));
	}

	public static NoCallbackPV<Byte[]> newNoCallbackByteArrayPV(String pvName) {
		return new NoCallback<>(newByteArrayPV(pvName));
	}

	public static NoCallbackPV<Double[]> newNoCallbackDoubleArrayPV(String pvName) {
		return new NoCallback<>(newDoubleArrayPV(pvName));
	}

	public static NoCallbackPV<Float[]> newNoCallbackFloatArrayPV(String pvName) {
		return new NoCallback<>(newFloatArrayPV(pvName));
	}

	public static NoCallbackPV<Integer[]> newNoCallbackIntegerArrayPV(String pvName) {
		return new NoCallback<>(newIntegerArrayPV(pvName));
	}

	public static NoCallbackPV<Short[]> newNoCallbackShortArrayPV(String pvName) {
		return new NoCallback<>(newShortArrayPV(pvName));
	}

	public static NoCallbackPV<Boolean> newNoCallbackBooleanFromIntegerPV(String pvName) {
		return new NoCallback<>(newBooleanFromIntegerPV(pvName));
	}

	public static NoCallbackPV<Boolean> newNoCallbackBooleanFromShortPV(String pvName) {
		return new NoCallback<>(newBooleanFromShortPV(pvName));
	}

	//

	public static <E> ReadOnlyPV<E> newReadOnlyEnumPV(String pvName, Class<E> enumType) {
		return new ReadOnly<>(newEnumPV(pvName, enumType));
	}

	public static ReadOnlyPV<Byte> newReadOnlyBytePV(String pvName) {
		return new ReadOnly<>(newBytePV(pvName));
	}

	public static ReadOnlyPV<Double> newReadOnlyDoublePV(String pvName) {
		return new ReadOnly<>(newDoublePV(pvName));
	}

	public static ReadOnlyPV<Float> newReadOnlyFloatPV(String pvName) {
		return new ReadOnly<>(newFloatPV(pvName));
	}

	public static ReadOnlyPV<Integer> newReadOnlyIntegerPV(String pvName) {
		return new ReadOnly<>(newIntegerPV(pvName));
	}

	public static ReadOnlyPV<Integer> newReadOnlyIntegerFromEnumPV(String pvName) {
		return new ReadOnly<>(newIntegerFromEnumPV(pvName));
	}

	public static ReadOnlyPV<Short> newReadOnlyShortPV(String pvName) {
		return new ReadOnly<>(newShortPV(pvName));
	}

	/**
	 * Create a new String PV that connects an EPICS STRING PV. DBR STRINGs support <40 useful characters.
	 * @param pvName
	 * @return the String PV
	 */
	public static ReadOnlyPV<String> newReadOnlyStringPV(String pvName) {
		return new ReadOnly<>(newStringPV(pvName));
	}

	public static ReadOnlyPV<String> newReadOnlyStringFromWaveformPV(String pvName) {
		return new ReadOnly<>(newStringFromWaveformPV(pvName));
	}

	public static ReadOnlyPV<String> newReadOnlyStringFromEnumPV(String pvName) {
		final LazyPV<String> pv = new LazyPV<>(epicsController, pvName, String.class);
		return new ReadOnly<>(pv);
	}

	public static ReadOnlyPV<Byte[]> newReadOnlyByteArrayPV(String pvName) {
		return new ReadOnly<>(newByteArrayPV(pvName));
	}

	public static ReadOnlyPV<Double[]> newReadOnlyDoubleArrayPV(String pvName) {
		return new ReadOnly<>(newDoubleArrayPV(pvName));
	}

	public static ReadOnlyPV<Float[]> newReadOnlyFloatArrayPV(String pvName) {
		return new ReadOnly<>(newFloatArrayPV(pvName));
	}

	public static ReadOnlyPV<Integer[]> newReadOnlyIntegerArrayPV(String pvName) {
		return new ReadOnly<>(newIntegerArrayPV(pvName));
	}

	public static ReadOnlyPV<Short[]> newReadOnlyShortArrayPV(String pvName) {
		return new ReadOnly<>(newShortArrayPV(pvName));
	}

	public static ReadOnlyPV<Boolean> newReadOnlyBooleanFromIntegerPV(String pvName) {
		return new ReadOnly<>(newBooleanFromIntegerPV(pvName));
	}

	public static ReadOnlyPV<Boolean> newReadOnlyBooleanFromShortPV(String pvName) {
		return new ReadOnly<>(newBooleanFromShortPV(pvName));
	}

	// NOTE: just uses a short under the covers, so there is no enumType parameter.
	public static ReadOnlyPV<Boolean> newReadOnlyBooleanFromEnumPV(String pvName) {
		return new ReadOnly<>(newBooleanFromEnumPV(pvName));
	}

	public static ReadOnlyPV<Boolean> newReadOnlyBooleanFromDoublePV(String pvName) {
		return new ReadOnly<>(newBooleanFromDoublePV(pvName));
	}

	private static class LazyPV<T> implements PV<T> {

		private static final Logger logger = LoggerFactory.getLogger(LazyPV.class);

		static Map<Class<?>, DBRType> javaTypeToDBRType;

		static {

			javaTypeToDBRType = new HashMap<>();

			javaTypeToDBRType.put(Double.class, DBRType.DOUBLE);

			javaTypeToDBRType.put(Float.class, DBRType.FLOAT);

			javaTypeToDBRType.put(Integer.class, DBRType.INT);

			javaTypeToDBRType.put(Short.class, DBRType.SHORT);

			javaTypeToDBRType.put(Byte.class, DBRType.BYTE);

			javaTypeToDBRType.put(String.class, DBRType.STRING);
		}

		private final EpicsController controller;

		private final String pvName;

		private final Class<T> javaType;

		private final DBRType dbrType;

		private Channel channel; // created only when first accessed

		private T lastMonitoredValue;

		private Object lastMonitoredValueMonitor = new Object();

		private Map<MonitorListener, Monitor> monitors = new HashMap<>();

		private ValueMonitorListener valueMonitorListener; // presence indicates monitoring

		private PutCallbackListener putCallbackListener = new NullPutCallbackListener();

		private Object putCallbackGuard = new Object();

		private PVMonitor<T> observableMonitor;

		LazyPV(EpicsController controller, String pvName, Class<T> javaType) {
			this.controller = controller;
			this.pvName = pvName;
			this.javaType = javaType;
			if (javaType.isEnum()) {
				dbrType = DBRType.ENUM;
			} else if (javaType.isArray()) {
				dbrType = javaTypeToDBRType.get(javaType.getComponentType());
			} else {
				dbrType = javaTypeToDBRType.get(javaType);
			}
			if (LocalProperties.check(CHECK_CHANNELS_PROPERTY_NAME)) {
				logger.warn("Checking channel : '{}'", pvName);
				try {
					Channel temp = (controller.createChannel(pvName));
					controller.destroy(temp);
					temp=null;
				} catch (Exception e) {
					logger.error("Could not connect to channel  : '{}'", pvName, e);
				}
			}
		}

		@Override
		public String toString() {
			return MessageFormat.format("LazyPV({0}, {1})", pvName, javaType.getSimpleName());
		}

		private double defaultTimeout() {
			return EpicsGlobals.getTimeout();
		}

		@Override
		public String getPvName() {
			return pvName;
		}

		@Override
		public T get() throws IOException {
			T value = extractValueFromDbr(getDBR(dbrType));
			logger.debug("'{}' get() <-- {}", pvName, value);
			return value;
		}

		@Override
		public T get(int numElements) throws IOException {
			T value = extractValueFromDbr(getDBR(dbrType, numElements));
			logger.debug("'{}' get() <-- {}", pvName, value);
			return value;
		}

		@Override
		public T getLast() throws IOException {
			if (!isValueMonitoring()) {
				throw new IllegalStateException("Cannot get the last value of " + getPvName()
						+ " as this LazyPV is not set to monitor values");
			}

			initialiseLastMonitoredValue();
			logger.debug("'{}' get() <-- {} (via monitor)", pvName, lastMonitoredValue);
			return lastMonitoredValue;
		}

		@Override
		public T waitForValue(Predicate<T> predicate, double timeoutS) throws IOException, IllegalStateException,
				java.util.concurrent.TimeoutException, InterruptedException {
			logger.debug("'{}' waiting for value '{}'", pvName, predicate);
			if (!isValueMonitoring()) {
				this.setValueMonitoring(true);
			}

			initialiseLastMonitoredValue();

			synchronized (lastMonitoredValueMonitor) {

				if (timeoutS <= 0) {
					// wait indefinitely
					while (!predicate.test(lastMonitoredValue)) {
						lastMonitoredValueMonitor.wait();
					}
				} else {
					// wait for timeoutS seconds
					long deadline = System.currentTimeMillis() + ((long) (timeoutS * 1000));

					while (!predicate.test(lastMonitoredValue)) {
						long remaining = deadline - System.currentTimeMillis();
						logger.debug("deadline: {} remaining: {}\n", deadline , remaining);
						if (remaining <= 0) {
							String msg = "The requested value {0} was not observed from the PV {1} within the specified timeout of {2}s";
							throw new java.util.concurrent.TimeoutException(format(msg, predicate.toString(),
									getPvName(), timeoutS));
						}
						lastMonitoredValueMonitor.wait(remaining);
					}
				}

				logger.debug("'{}' waitForValue() <-- {} (after waiting for a value via monitor)", pvName,
						lastMonitoredValue);
				return lastMonitoredValue;
			}
		}

		/**
		 * If no value monitored yet; get value across CA
		 */
		private void initialiseLastMonitoredValue() throws IOException {
			if (lastMonitoredValue == null) {
				logger.debug("No monitors received from '{}', retrieving value across CA", pvName);
				setLastValueFromMonitor(extractValueFromDbr(getDBR(dbrType)));
			}
		}

		private void setLastValueFromMonitor(T lastValueFromMonitor) {
			synchronized (lastMonitoredValueMonitor) {
				this.lastMonitoredValue = lastValueFromMonitor;
				this.lastMonitoredValueMonitor.notifyAll();
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		public T extractValueFromDbr(DBR dbr) {

			if (javaType == Byte.class) {
				return (T) toObject(((BYTE) dbr).getByteValue())[0];
			}

			if (javaType == Double.class) {
				return (T) toObject(((DOUBLE) dbr).getDoubleValue())[0];
			}

			if (javaType == Float.class) {
				return (T) toObject(((FLOAT) dbr).getFloatValue())[0];
			}

			if (javaType == Integer.class) {
				return (T) toObject(((INT) dbr).getIntValue())[0];
			}

			if (javaType == Short.class) {
				return (T) toObject(((SHORT) dbr).getShortValue())[0];
			}

			if (javaType.isEnum()) {
				short s = ((ENUM) dbr).getEnumValue()[0];
				return javaType.getEnumConstants()[s];
			}

			if (javaType == String.class) {
				return (T) ((STRING) dbr).getStringValue()[0];
			}
			if (javaType == Byte[].class) {
				return (T) toObject(((BYTE) dbr).getByteValue());
			}

			if (javaType == Double[].class) {
				return (T) toObject(((DOUBLE) dbr).getDoubleValue());
			}

			if (javaType == Float[].class) {
				return (T) toObject(((FLOAT) dbr).getFloatValue());
			}

			if (javaType == Integer[].class) {
				return (T) toObject(((INT) dbr).getIntValue());
			}

			if (javaType == Short[].class) {
				return (T) toObject(((SHORT) dbr).getShortValue());
			}

			if (javaType == String[].class) {
				return (T) ((STRING) dbr).getStringValue();
			}

			throw new IllegalStateException(UNEXPECTED_TYPE_CONFIGURED);

		}

		@Override
		public synchronized void setValueMonitoring(boolean shouldMonitor) throws IOException {

			if (shouldMonitor && valueMonitorListener == null) {
				// start monitoring
				logger.debug("Configuring constant monitoring of pv '{}'", pvName);
				valueMonitorListener = new ValueMonitorListener();
				addMonitorListener(valueMonitorListener);
			}

			else if (!shouldMonitor && valueMonitorListener != null) {
				// stop monitoring
				logger.debug("Disabling constant monitoring of pv '{}'", pvName);
				removeMonitorListener(valueMonitorListener);
				valueMonitorListener = null;
				lastMonitoredValue = null;
			}

		}

		@Override
		public synchronized boolean isValueMonitoring() {
			return (valueMonitorListener != null);
		}

		@Override
		public void addMonitorListener(MonitorListener listener) throws IOException {

			logger.debug("Adding MonitorListener '{}' to pv '{}' ", listener.getClass().getName(), pvName);
			Monitor monitor;
			try {
				monitor = controller.setMonitor(getChannel(), dbrType, Monitor.VALUE, listener);
			} catch (Exception e) {
				throw new IOException("Could not add monitor listener to PV '" + getPvName() + "'", e);
			}
			monitors.put(listener, monitor);
		}

		@Override
		public void removeMonitorListener(MonitorListener listener) {
			logger.debug("Removing MonitorListener '{}' from pv '{}' ", listener.getClass().getName(), pvName);
			Monitor monitor = monitors.remove(listener);
			//protect against invalid listener
			if( monitor != null)
				controller.clearMonitor(monitor);
		}

		protected synchronized Channel getChannel() throws IOException {
			if (channel == null) {
				try {
					channel = (controller.createChannel(pvName));
				} catch (CAException e) {
					throw new IOException("Epics problem creating channel for pv '" + pvName + "'", e);
				} catch (gov.aps.jca.TimeoutException e) {
					throw new IOException("Timed out creating channel for pv '" + pvName + "'", e);
				}
			}
			return channel;
		}

		private synchronized DBR getDBR(DBRType dbrType) throws IOException {

			try {
				return controller.getDBR(getChannel(), dbrType);
			} catch (CAException e) {
				throw new IOException("Problem getting value from Epics pv '" + pvName + "'", e);
			} catch (TimeoutException e) {
				throw new IOException("Timed out getting value from Epics pv '" + pvName + "'", e);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new InterruptedIOException("Interupted while getting value from Epics pv '" + pvName + "'");
			}
		}

		private synchronized DBR getDBR(DBRType dbrType, int numElements) throws IOException {

			try {
				return controller.getDBR(getChannel(), dbrType, numElements);
			} catch (CAException e) {
				throw new IOException("Problem getting value from Epics pv '" + pvName + "'", e);
			} catch (TimeoutException e) {
				throw new IOException("Timed out getting value from Epics pv '" + pvName + "'", e);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new InterruptedIOException("Interupted while getting value from Epics pv '" + pvName + "'");
			}
		}

		private class ValueMonitorListener implements MonitorListener {

			@Override
			public void monitorChanged(MonitorEvent ev) {
				DBR dbr = ev.getDBR();
				if(dbr != null){
					setLastValueFromMonitor(extractValueFromDbr(dbr));
					logger.debug("'{}' <-- {}  (monitored value changed)", pvName, lastMonitoredValue);
				}
			}
		}

		// NoCallbackPV

		@Override
		public void putNoWait(T value) throws IOException {

			logger.debug("'{}' put() --> {}", pvName, value);

			try {

				if (javaType == Byte[].class) {
					controller.caput(getChannel(), toPrimitive((Byte[]) value));
				} else if (javaType == Double[].class) {
					controller.caput(getChannel(), toPrimitive((Double[]) value));
				} else if (javaType == Float[].class) {
					controller.caput(getChannel(), toPrimitive((Float[]) value));
				} else if (javaType == Integer[].class) {
					controller.caput(getChannel(), toPrimitive((Integer[]) value));
				} else if (javaType == Short[].class) {
					controller.caput(getChannel(), toPrimitive((Short[]) value));
				} else if (javaType == String[].class) {
					throw new IllegalStateException("String[] not supported");
				} else if (javaType == Byte.class) {
					controller.caput(getChannel(), (Byte) value);
				} else if (javaType == Double.class) {
					controller.caput(getChannel(), (Double) value);
				} else if (javaType == Float.class) {
					controller.caput(getChannel(), (Float) value);
				} else if (javaType == Integer.class) {
					controller.caput(getChannel(), (Integer) value);
				} else if (javaType == Short.class) {
					controller.caput(getChannel(), (Short) value);
				} else if (javaType == String.class) {
					controller.caput(getChannel(), (String) value);
				} else if (javaType.isEnum()) {
					controller.caput(getChannel(), ((Enum<?>) value).ordinal());
				} else {
					throw new IllegalStateException(UNEXPECTED_TYPE_CONFIGURED);
				}

			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new InterruptedIOException(format(
						"Interupted while putting value to EPICS pv ''{0}'', (value was: {1})", pvName, value));
			} catch (Exception e) {
				throw new IOException(format("Problem putting value to EPICS pv ''{0}'', (value was: {1})", pvName,
						value), e);
			}

		}

		@Override
		public void putNoWait(T value, PutListener pl) throws IOException {

			logger.debug("'{}' put() --> {}, with listener '{}'", pvName, value, pl.getClass().getName());

			try {

				if (javaType == Byte[].class) {
					controller.caput(getChannel(), toPrimitive((Byte[]) value), pl);
				} else if (javaType == Double[].class) {
					controller.caput(getChannel(), toPrimitive((Double[]) value), pl);
				} else if (javaType == Float[].class) {
					controller.caput(getChannel(), toPrimitive((Float[]) value), pl);
				} else if (javaType == Integer[].class) {
					controller.caput(getChannel(), toPrimitive((Integer[]) value), pl);
				} else if (javaType == Short[].class) {
					controller.caput(getChannel(), toPrimitive((Short[]) value), pl);
				} else if (javaType == String[].class) {
					throw new IllegalStateException("String[] not supported");
				} else if (javaType == Byte.class) {
					controller.caput(getChannel(), (Byte) value, pl);
				} else if (javaType == Double.class) {
					controller.caput(getChannel(), (Double) value, pl);
				} else if (javaType == Float.class) {
					controller.caput(getChannel(), (Float) value, pl);
				} else if (javaType == Integer.class) {
					controller.caput(getChannel(), (Integer) value, pl);
				} else if (javaType == Short.class) {
					controller.caput(getChannel(), (Short) value, pl);
				} else if (javaType == String.class) {
					controller.caput(getChannel(), (String) value, pl);
				} else if (javaType.isEnum()) {
					controller.caput(getChannel(), ((Enum<?>) value).ordinal(), pl);
				} else {
					throw new IllegalStateException(UNEXPECTED_TYPE_CONFIGURED);
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new InterruptedIOException(format(
						"Interupted while putting (with listener) value to EPICS pv ''{0}'', (value was: {1})", pvName, value));
			} catch (Exception e) {
				throw new IOException(format("Problem putting (with listener)  value to EPICS pv ''{0}'', (value was: {1})", pvName,
						value), e);
			}
		}

		// PV (with callback)

		@Override
		public void putWait(T value) throws IOException {
			putWait(value, defaultTimeout());
		}

		@Override
		public void putWait(T value, double timeoutS) throws IOException {
			putAsyncStart(value);
			putAsyncWait(timeoutS);
		}

		@Override
		public void putAsyncStart(T value) throws IllegalStateException, IOException {
			synchronized (putCallbackGuard) {
				if (putCallbackListener.isCallbackPending()) {
					throw new IllegalStateException("The pv " + getPvName()
							+ " is waiting to complete a startPutCallback already");
				}
				putCallbackListener = new PutCallbackListener();
				try {
					putNoWait(value, putCallbackListener);
				} catch (IllegalStateException | IOException e) {
					putCallbackListener.cancelPendingCallback();
					throw e;
				}
			}
		}

		@Override
		public void putAsyncWait() throws IOException {
			putAsyncWait(defaultTimeout());
		}

		@Override
		public boolean putAsyncIsWaiting() {
			return putCallbackListener.isCallbackPending();
		}

		@Override
		public void putAsyncCancel() {
			if (putAsyncIsWaiting()) {
				logger.debug("Cancelling pending callback on the pv {}", getPvName());
			}
			putCallbackListener.cancelPendingCallback();
		}

		@Override
		public void putAsyncWait(double timeoutS) throws IOException {
			synchronized (putCallbackGuard) {
				try {
					putCallbackListener.waitForCallback(timeoutS);
				} catch (CAStatusException e) {
					throw new IOException("Epics problem waiting for callback from PV " + getPvName(), e);
				} catch (TimeoutException e) {
					throw new IOException("Timed out waiting for callback from PV " + getPvName());
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					throw new InterruptedIOException("Interupted while waiting for callback from PV " + getPvName());
				} finally {
					putCallbackListener = new NullPutCallbackListener();
				}
			}
		}

		/**
		 * NOTE: This is a temporary solution that DOES not implement the interface as specified. Instead it gets the
		 * values of each PV specified in toReturn just AFTER the callback returns.
		 */
		@Override
		public PVValues putWait(T value, ReadOnlyPV<?>... toReturn) throws IOException {
			return putWait(value, defaultTimeout(), toReturn);
		}

		/**
		 * NOTE: This is a temporary solution that DOES not implement the interface as specified. Instead it gets the
		 * values of each PV specified in toReturn just AFTER the callback returns.
		 */
		@Override
		public PVValues putWait(T value, double timeoutS, ReadOnlyPV<?>... toReturn) throws IOException {
			putWait(value);
			CallbackResult result = new CallbackResult();
			for (ReadOnlyPV<?> pv : toReturn) {
				result.put(pv, pv.get());
			}
			return result;
		}

		private class PutCallbackListener implements PutListener {

			private volatile PutEvent event;

			private volatile boolean callbackPending = true;

			private volatile Object eventMonitor = new Object();

			@Override
			public void putCompleted(PutEvent ev) {
				synchronized (eventMonitor) {
					event = ev;
					callbackPending = false;
					eventMonitor.notifyAll();
				}
			}

			public void waitForCallback(double timeoutS) throws TimeoutException, CAStatusException, InterruptedException {
				synchronized (eventMonitor) {
					if (callbackPending) {
						eventMonitor.wait((long) (timeoutS * 1000));
					}
					if (event == null) {
						throw new TimeoutException("putWait timed out after " + timeoutS + "s");
					}
					if (event.getStatus() != CAStatus.NORMAL) {
						throw new CAStatusException(event.getStatus(), "putWait failed");
					}
				}
			}

			public boolean isCallbackPending() {
				return callbackPending;
			}

			public void cancelPendingCallback() {
				synchronized (eventMonitor) {
					callbackPending = false;
					eventMonitor.notifyAll();
				}
			}

		}

		private class NullPutCallbackListener extends PutCallbackListener {

			@Override
			public synchronized void waitForCallback(double timeoutS) throws TimeoutException {
				// just return
			}

			@Override
			public synchronized boolean isCallbackPending() {
				return false;
			}
		}

		class CallbackResult implements PVValues {

			Map<ReadOnlyPV<?>, Object> resultsMap = new HashMap<>();

			void put(ReadOnlyPV<?> pv, Object value) {
				resultsMap.put(pv, value);

			}

			@SuppressWarnings("unchecked")
			@Override
			public <N> N get(ReadOnlyPV<N> pv) {
				Object value = resultsMap.get(pv);
				if (value == null) {
					throw new IllegalArgumentException("There is no result for the PV " + getPvName());
				}
				return (N) value;
			}

		}

		/**
		 * When adding an observer we use the PVMonitor object to
		 * add the monitor listener and notify the observer.
		 *
		 */
		@Override
		public void addObserver(Observer<T> observer) throws Exception {
			if( observableMonitor == null){
				observableMonitor = new PVMonitor<>(this, this);
			}
			observableMonitor.addObserver(observer);
		}

		@Override
		public void addObserver(Observer<T> observer, Predicate<T> predicate) throws Exception {
			if( observableMonitor == null){
				observableMonitor = new PVMonitor<>(this, this);
			}
			observableMonitor.addObserver(observer, predicate);
		}


		@Override
		public void removeObserver(Observer<T>  observer) {
			if( observableMonitor == null){
				return;
			}
			observableMonitor.removeObserver(observer);
		}

	}

	private abstract static class AbstractReadOnlyAdapter<N, T> implements ReadOnlyPV<T> {

		Observable<T> obs = null;

		protected abstract T innerToOuter(N innerValue);

		protected abstract N outerToInner(T outerValue);

		private final ReadOnlyPV<N> ropv;

		public AbstractReadOnlyAdapter(PV<N> pv) {
			this.ropv = pv;
		}

		ReadOnlyPV<N> getPV() {
			return ropv;
		}

		private Predicate<N> newInnerPredicate(Predicate<T> outerPredicate) {
			return new InnerFromOuterPredicate(outerPredicate);
		}

		//

		@Override
		public T get() throws IOException {
			return innerToOuter(getPV().get());
		}

		@Override
		public T get(int numElements) throws IOException {
			return innerToOuter(getPV().get(numElements));
		}

		@Override
		public T getLast() throws IOException {
			return innerToOuter(getPV().getLast());
		}

		@Override
		public T waitForValue(Predicate<T> predicate, double timeoutS) throws IOException, IllegalStateException, java.util.concurrent.TimeoutException, InterruptedException {
			N innerValue = getPV().waitForValue(newInnerPredicate(predicate), timeoutS);
			return innerToOuter(innerValue);
		}

		@Override
		public T extractValueFromDbr(DBR dbr) {
			return innerToOuter(getPV().extractValueFromDbr(dbr));
		}

		//

		@Override
		public String getPvName() {
			return getPV().getPvName();
		}

		@Override
		public void setValueMonitoring(boolean shouldMonitor) throws IOException {
			getPV().setValueMonitoring(shouldMonitor);
		}

		@Override
		public boolean isValueMonitoring() {
			return getPV().isValueMonitoring();
		}

		@Override
		public void addMonitorListener(MonitorListener listener) throws IOException {
			getPV().addMonitorListener(listener);
		}

		@Override
		public void removeMonitorListener(MonitorListener listener) {
			getPV().removeMonitorListener(listener);
		}

		private class InnerFromOuterPredicate implements Predicate<N> {

			private final Predicate<T> outerPredicate;

			public InnerFromOuterPredicate(Predicate<T> outerPredicate) {
				this.outerPredicate = outerPredicate;
			}

			@Override
			public boolean test(N innerObject) {
				return outerPredicate.test(innerToOuter(innerObject));
			}

		}

		/**
		 * As the Observer may be of a different type e.g. String to the
		 * Observable e.g. Byte[] we need to adapter.
		 *
		 **/
		private class GenericObservable implements Observable<T> {

			private final Observable<N> stringFromWaveform;

			ObservableUtil<T> oc = new ObservableUtil<>();

			private Observer<N> observerN;

			public GenericObservable(Observable<N> stringFromWaveform) throws Exception {
				this.stringFromWaveform = stringFromWaveform;
				observerN = (source, arg) -> oc.notifyIObservers(AbstractReadOnlyAdapter.this, innerToOuter(arg));
				stringFromWaveform.addObserver(observerN);
			}

			@Override
			public void addObserver(Observer<T> observer) throws Exception {
				oc.addObserver(observer);
			}

			@Override
			public void addObserver(Observer<T> observer, Predicate<T> predicate) throws Exception {
				oc.addObserver(observer, predicate);
			}

			@Override
			public void removeObserver(Observer<T> observer) {
				oc.removeObserver(observer);
				if( !oc.isBeingObserved())
					stringFromWaveform.removeObserver(observerN);
			}

		}

		@Override
		public void addObserver(final Observer<T> observer) throws Exception {
			createStringObservable(getPV()).addObserver(observer);
		}

		@Override
		public void addObserver(final Observer<T> observer, Predicate<T> predicate) throws Exception {
			createStringObservable(getPV()).addObserver(observer, predicate);
		}

		private Observable<T> createStringObservable(ReadOnlyPV<N> pv) throws Exception {
			if( obs== null){
				obs = new GenericObservable(pv);
			}
			return obs;
		}

		@Override
		public void removeObserver(Observer<T> observer) {
			if( obs == null)
				return;
			obs.removeObserver(observer);
		}

	}

	private abstract static class AbstractPVAdapter<N, T> extends AbstractReadOnlyAdapter<N, T> implements PV<T> {

		private final PV<N> pv;

		public AbstractPVAdapter(PV<N> pv) {
			super(pv);
			this.pv = pv;
		}

		@Override
		PV<N> getPV() {
			return pv;
		}

		@Override
		public void putNoWait(T value) throws IOException {
			getPV().putNoWait(outerToInner(value));
		}

		@Override
		public void putNoWait(T value, PutListener pl) throws IOException {
			getPV().putNoWait(outerToInner(value), pl);
		}

		@Override
		public void putWait(T value) throws IOException {
			getPV().putWait(outerToInner(value));
		}

		@Override
		public void putWait(T value, double timeoutS) throws IOException {
			getPV().putWait(outerToInner(value), timeoutS);
		}

		@Override
		public void putAsyncStart(T value) throws IOException {
			getPV().putAsyncStart(outerToInner(value));
		}

		@Override
		public void putAsyncWait() throws IOException {
			getPV().putAsyncWait();
		}

		@Override
		public void putAsyncWait(double timeoutS) throws IOException {
			getPV().putAsyncWait(timeoutS);
		}

		@Override
		public boolean putAsyncIsWaiting() {
			return getPV().putAsyncIsWaiting();
		}

		@Override
		public void putAsyncCancel() {
			getPV().putAsyncCancel();
		}

		@Override
		public PVValues putWait(T value, ReadOnlyPV<?>... toReturn) throws IOException {
			return getPV().putWait(outerToInner(value), toReturn);
		}

		@Override
		public PVValues putWait(T value, double timeoutS, ReadOnlyPV<?>... toReturn) throws IOException {
			return getPV().putWait(outerToInner(value), timeoutS, toReturn);
		}

	}

	private static class ReadOnly<T> extends AbstractReadOnlyAdapter<T, T> {

		public ReadOnly(PV<T> pv) {
			super(pv);
		}

		@Override
		public String toString() {
			return MessageFormat.format("ReadOnly({0})", getPV());
		}

		@Override
		protected T innerToOuter(T innerValue) {
			return innerValue;
		}

		@Override
		protected T outerToInner(T outerValue) {
			return outerValue;
		}

		@Override
		public void addObserver(Observer<T> observer) throws Exception {
			getPV().addObserver(observer);
		}

		@Override
		public void addObserver(Observer<T> observer, Predicate<T> predicate) throws Exception {
			getPV().addObserver(observer, predicate);
		}

		@Override
		public void removeObserver(Observer<T> observer) {
			getPV().removeObserver(observer);
		}

	}

	private static class NoCallback<T> extends ReadOnly<T> implements NoCallbackPV<T> {

		private NoCallbackPV<T> pv;

		public NoCallback(PV<T> pv) {
			super(pv);
			this.pv = pv;
		}

		@Override
		public String toString() {
			return MessageFormat.format("NoCallback({0})", getPV());
		}

		@Override
		NoCallbackPV<T> getPV() {
			return pv;
		}

		@Override
		public void putNoWait(T value) throws IOException {
			getPV().putNoWait(value);
		}

		@Override
		public void putNoWait(T value, PutListener pl) throws IOException {
			getPV().putNoWait(value, pl);
		}

	}

	static class StringFromWaveform extends AbstractPVAdapter<Byte[], String> {

		StringFromWaveform(PV<Byte[]> byteArrayPV) {
			super(byteArrayPV);
		}

		@Override
		public String toString() {
			return MessageFormat.format("StringFromWaveform({0})", getPV());
		}

		@Override
		protected String innerToOuter(Byte[] innerValue) {
			final byte nullByte = 0;
			final int nullIndex = Arrays.asList(innerValue).indexOf(nullByte);
			final byte[] byteArray = toPrimitive(innerValue);
			if (nullIndex == -1) {
				// No null byte, so use entire array
				return new String(byteArray).trim();
			} else {
				return new String(byteArray, 0, nullIndex).trim();
			}
		}

		@Override
		protected Byte[] outerToInner(String outerValue) {
			return toObject(((outerValue + '\0').getBytes()));
		}

	}

	private static class BooleanFromInteger extends AbstractPVAdapter<Integer, Boolean> {

		private BooleanFromInteger(LazyPV<Integer> pv) {
			super(pv);
		}

		@Override
		public String toString() {
			return MessageFormat.format("BooleanFromInteger({0})", getPV());
		}

		@Override
		protected Boolean innerToOuter(Integer innerValue) {
			return innerValue > 0;
		}

		@Override
		protected Integer outerToInner(Boolean outerValue) {
			return (Boolean.TRUE.equals(outerValue) ? 1 : 0);
		}

	}

	private static class BooleanFromShort extends AbstractPVAdapter<Short, Boolean> {

		private BooleanFromShort(LazyPV<Short> pv) {
			super(pv);
		}

		@Override
		public String toString() {
			return MessageFormat.format("BooleanFromShort({0})", getPV());
		}

		@Override
		protected Boolean innerToOuter(Short innerValue) {
			return innerValue > 0;
		}

		@Override
		protected Short outerToInner(Boolean outerValue) {
			return (short) (Boolean.TRUE.equals(outerValue) ? 1 : 0);
		}

	}

	private static class BooleanFromDouble extends AbstractPVAdapter<Double, Boolean> {

		private BooleanFromDouble(LazyPV<Double> pv) {
			super(pv);
		}

		@Override
		public String toString() {
			return MessageFormat.format("BooleanFromDouble({0})", getPV());
		}

		@Override
		protected Boolean innerToOuter(Double innerValue) {
			return innerValue > 0;
		}

		@Override
		protected Double outerToInner(Boolean outerValue) {
			return (Boolean.TRUE.equals(outerValue) ? 1.0 : 0.0);
		}
	}
}

/**
 * Class to adapt between a monitor and the gda IObserver system
 *
 * @param <E>
 */
class PVMonitor<E> implements Observable<E>{

	private final class ObservableNotifyingListener implements MonitorListener {
		@Override
		public void monitorChanged(MonitorEvent arg0) {
			E extractValueFromDbr;
			try {
				DBR dbr = arg0.getDBR();
				if (dbr != null){
					extractValueFromDbr = pv.extractValueFromDbr(dbr);
					if( oc != null){
						oc.notifyIObservers(observable, extractValueFromDbr);
					}
				} else {
					arg0.toString();
				}
			} catch (Exception e) {
				logger.error("Error extracting data from histogram update", e);
			}
		}
	}

	static final Logger logger = LoggerFactory.getLogger(PVMonitor.class);

	private final PV<E> pv;

	private final Observable<E> observable;

	ObservableUtil<E> oc = null;

	private boolean monitorAdded = false;

	MonitorListener monitorListener = new ObservableNotifyingListener();

	public PVMonitor(Observable<E> observable, PV<E> pv){
		this.observable = observable;
		this.pv = pv;
	}

	@Override
	public void addObserver(Observer<E> observer) throws Exception {
		getObservableComponent().addObserver(observer);
		addMonitorListenerIfRequired();
	}

	@Override
	public void addObserver(Observer<E> observer, Predicate<E> predicate) throws Exception {
		getObservableComponent().addObserver(observer, predicate);
		addMonitorListenerIfRequired();
	}

	private ObservableUtil<E> getObservableComponent() {
		if (oc == null) {
			oc = new ObservableUtil<>();
		}
		return oc;
	}

	private void addMonitorListenerIfRequired() {
		if (!monitorAdded){
			try {
				pv.addMonitorListener(monitorListener);
				monitorAdded = true;
			} catch (IOException e) {
				throw new IllegalStateException("Error adding monitor to pv:"+ pv.getPvName(),e);
			}
		}
	}

	@Override
	public void removeObserver(Observer<E> observer) {
		if( oc == null){
			return;
		}
		oc.removeObserver(observer);
		if (!isBeingObserved() && monitorAdded) {
			pv.removeMonitorListener(monitorListener);
			monitorAdded = false;
		}
	}

	public boolean isBeingObserved() {
		return oc != null && oc.isBeingObserved();
	}

}
