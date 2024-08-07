/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package gda.device.monitor;

import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.ControllerRecord;
import gda.device.DeviceException;
import gda.device.Monitor;
import gda.epics.connection.EpicsChannelManager;
import gda.epics.connection.EpicsController;
import gda.epics.connection.EpicsController.MonitorType;
import gda.epics.connection.InitializationListener;
import gda.epics.util.EpicsGlobals;
import gda.factory.FactoryException;
import gov.aps.jca.Channel;
import gov.aps.jca.TimeoutException;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.DBRType;
import gov.aps.jca.dbr.DBR_Byte;
import gov.aps.jca.dbr.DBR_CTRL_Byte;
import gov.aps.jca.dbr.DBR_CTRL_Double;
import gov.aps.jca.dbr.DBR_CTRL_Float;
import gov.aps.jca.dbr.DBR_CTRL_Int;
import gov.aps.jca.dbr.DBR_CTRL_Short;
import gov.aps.jca.dbr.DBR_CTRL_String;
import gov.aps.jca.dbr.DBR_Double;
import gov.aps.jca.dbr.DBR_Float;
import gov.aps.jca.dbr.DBR_Int;
import gov.aps.jca.dbr.DBR_LABELS_Enum;
import gov.aps.jca.dbr.DBR_Short;
import gov.aps.jca.dbr.DBR_String;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;
import uk.ac.gda.api.remoting.ServiceInterface;

/**
 * A class which monitors the value of single Epics PV. The value is then broadcast to IObservers of this object or can be retrieved via the getValue method.
 * This will not monitor changes to limits, alarms or status.
 */
@ServiceInterface(Monitor.class)
public class EpicsMonitor extends MonitorBase implements InitializationListener, ControllerRecord {

	private static final Logger logger = LoggerFactory.getLogger(EpicsMonitor.class);

	private static final int CHANNEL_MANAGER_INITIALISATION_TIMEOUT = 1000; // in milliseconds
	/** The size of the change in the monitored value to cause an event to be sent. In decimal i.e. 100% = 1.0 */
	private double sensitivity = 0.0;

	protected volatile double latestDblValue;
	protected volatile int latestIntValue;
	protected volatile short latestShtValue;
	protected volatile float latestFltValue;
	protected volatile String latestStrValue = "";
	private volatile byte latestByteValue;

	protected volatile Double lastDblValueReported;
	protected volatile Integer lastIntValueReported;
	protected volatile Short lastShtValueReported;
	protected volatile Float lastFltValueReported;

	private volatile double[] latestDblArray;
	private volatile int[] latestIntArray;
	private volatile short[] latestShtArray;
	private volatile float[] latestFltArray;
	private volatile String[] latestStrArray;
	private volatile byte[] latestByteArray;

	protected volatile double[] lastDblArrayReported;
	protected volatile int[] lastIntArrayReported;
	protected volatile short[] lastShtArrayReported;
	protected volatile float[] lastFltArrayReported;

	private int elementCount;
	private String unit = "";
	private DBRType type;

	private String pvName;
	protected Channel theChannel;
	private boolean poll = false;
	private volatile Object latestValue;

	private String deviceName;

	private EpicsChannelManager channelManager;
	protected EpicsController controller;

	private boolean isInitialised = false;

	/**
	 * Constructor
	 */
	public EpicsMonitor() {
		controller = EpicsController.getInstance();
		channelManager = new EpicsChannelManager(this);
	}

	@Override
	public void configure() throws FactoryException {
		// this only represents a single value which should be the same string as its name
		if (!isConfigured()) {
			this.setInputNames(new String[0]);
			this.setExtraNames(new String[] { getName() });

			if (pvName == null) {
				throw new FactoryException("No PV set for " + getName());
			}
			createChannelAccess();
			channelManager.tryInitialize(CHANNEL_MANAGER_INITIALISATION_TIMEOUT);
		}
	}

	private void fetchInitialValue() {
		// fill the latestValue attribute in case its a while until an update
		try {
			final boolean oldPoll = isPoll();
			poll = true;
			getPosition();
			poll = oldPoll;
		} catch (DeviceException e) {
			// only warn because connection is made later. remove second arg for cleaner message to user
			logger.warn("Error fetching initial value during configure from " + getName(), e);
		}
	}

	private void createChannelAccess() throws FactoryException {
		try {
			theChannel = channelManager.createChannel(pvName, new ValueMonitorListener(), MonitorType.CTRL, false);

			// acknowledge that creation phase is completed
			channelManager.creationPhaseCompleted();
		} catch (Exception e) {
			throw new FactoryException("failed to create channel", e);
		}
	}

	@Override
	public Object getPosition() throws DeviceException {
		if (!isConfigured()) {
			return null;
		}

		try {
			waitForInitialisation();
		} catch (Exception e) {
			throw new DeviceException("Error waiting for initialisation for " + getName(), e);
		}

		if (poll || latestValue == null) {
			if (elementCount == 1) {
				return getSingularValue();
			}
			else if (elementCount > 1) {
				return getArrayValue();
			}
			else {
				throw new DeviceException("Element count is zero for EpicsMonitor " + getName());
			}
		}

		return latestValue;
	}

	protected Object getSingularValue() throws DeviceException {
		try {
			final DBR dbr = controller.getCTRL(theChannel);

			if (dbr.isDOUBLE()) {
				latestDblValue = ((DBR_CTRL_Double) dbr).getDoubleValue()[0];
				return latestDblValue;
			} else if (dbr.isFLOAT()) {
				latestFltValue = ((DBR_CTRL_Float) dbr).getFloatValue()[0];
				return latestFltValue;
			} else if (dbr.isINT()) {
				latestIntValue = ((DBR_CTRL_Int) dbr).getIntValue()[0];
				return latestIntValue;
			} else if (dbr.isSHORT()) {
				latestShtValue = ((DBR_CTRL_Short) dbr).getShortValue()[0];
				return latestShtValue;
			} else if (dbr.isENUM()) {
				// Use DBR_LABLES_Enum (not DBR_ENUM) to allow the getLables method to be called this
				// allows the monitor to return the new string not the short specifying
				// the position on the enum.
				final String[] labels = ((DBR_LABELS_Enum) dbr).getLabels();
				final Short labelNumber = ((DBR_LABELS_Enum) dbr).getEnumValue()[0];
				latestStrValue = labels[labelNumber];
				return latestStrValue;
			} else if (dbr.isBYTE()) {
				latestByteValue = ((DBR_CTRL_Byte) dbr).getByteValue()[0];
				return latestByteValue;
			} else if (dbr.isSTRING()) {
				latestStrValue = ((DBR_CTRL_String) dbr).getStringValue()[0];
				return latestStrValue;
			}
		} catch (Exception e) {
			throw new DeviceException("Can NOT get " + theChannel.getName(), e);
		}

		return "No value is obtained from EPICS";
	}

	private Object getArrayValue() throws DeviceException {
		try {
			final DBR dbr = controller.getCTRL(theChannel);

			if (dbr.isDOUBLE()) {
				latestDblArray = ((DBR_CTRL_Double) dbr).getDoubleValue();
				return latestDblArray;
			} else if (dbr.isFLOAT()) {
				latestFltArray = ((DBR_CTRL_Float) dbr).getFloatValue();
				return latestFltArray;
			} else if (dbr.isINT()) {
				latestIntArray = ((DBR_CTRL_Int) dbr).getIntValue();
				return latestIntArray;
			} else if (dbr.isSHORT()) {
				latestShtArray = ((DBR_CTRL_Short) dbr).getShortValue();
				return latestShtArray;
			} else if (dbr.isENUM()) {
				// Use DBR_LABLES_Enum (not DBR_ENUM) to allow the getLables method to be called this
				// allows the monitor to return the new string not the short specifying
				// the position on the enum.
				latestStrArray = ((DBR_LABELS_Enum) dbr).getLabels();
				return latestStrArray;
			} else if (dbr.isBYTE()) {
				latestByteArray = ((DBR_CTRL_Byte) dbr).getByteValue();
				return latestByteArray;
			} else if (dbr.isSTRING()) {
				latestStrArray = ((DBR_CTRL_String) dbr).getStringValue();
				return latestStrArray;
			}
		} catch (Exception e) {
			throw new DeviceException("Can NOT get " + theChannel.getName(), e);
		}

		return "No value is obtained from EPICS";
	}

	/**
	 * Monitor value changes in EPICS and update observers with value, not including unit, alarms, status or limits.
	 */
	private class ValueMonitorListener implements MonitorListener {
		boolean first = true;

		/**
		 * @see gov.aps.jca.event.MonitorListener#monitorChanged(gov.aps.jca.event.MonitorEvent)
		 */
		@Override
		public void monitorChanged(MonitorEvent arg0) {
			if (first) {
				first = false;
				return;
			}
			final DBR dbr = arg0.getDBR();
			if (dbr.getCount() == 1) { // Single PV
				if (dbr.isDOUBLE()) {
					latestDblValue = ((DBR_Double) dbr).getDoubleValue()[0];
					latestValue = latestDblValue;
					if (lastDblValueReported == null || valueChanged(latestDblValue, lastDblValueReported)) {
						notifyIObservers(this, latestDblValue);
						lastDblValueReported = latestDblValue;
					}
				} else if (dbr.isINT()) {
					latestIntValue = ((DBR_Int) dbr).getIntValue()[0];
					latestValue = latestIntValue;
					if (lastIntValueReported == null || valueChanged(latestIntValue, lastIntValueReported)) {
						notifyIObservers(this, latestIntValue);
						lastIntValueReported = latestIntValue;
					}
				} else if (dbr.isSHORT()) {
					latestShtValue = ((DBR_Short) dbr).getShortValue()[0];
					latestValue = latestShtValue;
					if (lastShtValueReported == null || valueChanged(latestShtValue, lastShtValueReported)) {
						notifyIObservers(this, latestShtValue);
						lastShtValueReported = latestShtValue;
					}
				} else if (dbr.isFLOAT()) {
					latestFltValue = ((DBR_Float) dbr).getFloatValue()[0];
					latestValue = latestFltValue;
					if (lastFltValueReported == null || valueChanged(latestFltValue, lastFltValueReported)) {
						notifyIObservers(this, latestFltValue);
						lastFltValueReported = latestFltValue;
					}
				} else if (dbr.isSTRING()) {
					final String lastValue = latestStrValue;
					latestStrValue = ((DBR_String) dbr).getStringValue()[0];
					latestValue = latestStrValue;
					if (!lastValue.equalsIgnoreCase(latestStrValue)) {
						notifyIObservers(this, latestStrValue);
					}
				} else if (dbr.isENUM()) {
					final String lastValue = latestStrValue;
					// Use DBR_LABLES_Enum (not DBR_ENUM) to allow the getLables method to be called this
					// allows the monitor to return the new string not the short specifying
					// the position on the enum.
					final String[] labels = ((DBR_LABELS_Enum) dbr).getLabels();
					final Short labelNumber = ((DBR_LABELS_Enum) dbr).getEnumValue()[0];
					latestStrValue = labels[labelNumber];
					latestValue = latestStrValue;
					if (!lastValue.equalsIgnoreCase(latestStrValue)) {
						notifyIObservers(this, latestStrValue);
					}
				} else if (dbr.isBYTE()) {
					final byte lastValue = latestByteValue;
					latestByteValue = ((DBR_Byte) dbr).getByteValue()[0];
					latestValue = latestByteValue;
					if (latestByteValue != lastValue) {
						notifyIObservers(this, latestByteValue);
					}
				} else {
					logger.error("The monitored PV type was not matched");
				}
			} else if (dbr.getCount() > 1) { // Array PV
				if (dbr.isDOUBLE()) {
					latestDblArray = ((DBR_Double) dbr).getDoubleValue();
					latestValue = latestDblArray;
					if (doubleArrayValueChanged()) {
						notifyIObservers(this, latestDblArray);
						lastDblArrayReported = latestDblArray.clone();
					}
				} else if (dbr.isINT()) {
					latestIntArray = ((DBR_Int) dbr).getIntValue();
					latestValue = latestIntArray;
					if (intArrayValueChanged()) {
						notifyIObservers(this, latestIntArray);
						lastIntArrayReported = latestIntArray.clone();
					}
				} else if (dbr.isSHORT()) {
					latestShtArray = ((DBR_Short) dbr).getShortValue();
					latestValue = latestShtArray;
					if (shortArrayValueChanged()) {
						notifyIObservers(this, latestShtArray);
						lastShtArrayReported = latestShtArray.clone();

					}
				} else if (dbr.isFLOAT()) {
					latestFltArray = ((DBR_Float) dbr).getFloatValue();
					latestValue = latestFltArray;
					if (floatArrayValueChanged()) {
						notifyIObservers(this, latestFltArray);
						lastFltArrayReported = latestFltArray.clone();
					}
				} else if (dbr.isSTRING()) {
					final String[] lastValue = latestStrArray.clone();
					latestStrArray = ((DBR_String) dbr).getStringValue();
					latestValue = latestStrArray;
					for (int i = 0; i < latestStrArray.length; i++) {
						if (!lastValue[i].equalsIgnoreCase(latestStrArray[i])) {
							notifyIObservers(this, latestStrValue);
							return; // The array has changed no point in looking though any other elements
						}
					}
				} else if (dbr.isENUM()) {
					final String[] lastValue = latestStrArray.clone();
					// Use DBR_LABLES_Enum (not DBR_ENUM) to allow the getLables method to be called this
					// allows the monitor to return the new string not the short specifying
					// the position on the enum.
					latestStrArray = ((DBR_LABELS_Enum) dbr).getLabels();
					latestValue = latestStrArray;
					for (int i = 0; i < latestStrArray.length; i++) {
						if (!lastValue[i].equalsIgnoreCase(latestStrArray[i])) {
							notifyIObservers(this, latestStrValue);
							return; // The array has changed no point in looking though any other elements
						}
					}
				} else if (dbr.isBYTE()) {
					final byte[] lastValue = latestByteArray.clone();
					latestByteArray = ((DBR_Byte) dbr).getByteValue();
					latestValue = latestByteArray;
					for (int i = 0; i < lastValue.length; i++) {
						if (latestByteArray[i] != lastValue[i]) {
							notifyIObservers(this, latestByteArray);
							return; // The array has changed no point in looking though any other elements
						}
					}
				} else {
					logger.error("The monitored PV type was not matched");
				}
			}
		}

		private boolean valueChanged(Number latestValue, Number lastValueReported) {
			double diff = Math.abs(latestValue.doubleValue() - lastValueReported.doubleValue());
			double newValue = Math.abs(latestValue.doubleValue());
			double oldValue = Math.abs(lastValueReported.doubleValue());

			// Find the largest number
			double largest = Math.max(oldValue, newValue);

			return diff >= largest * sensitivity;
		}

		private boolean doubleArrayValueChanged() {
			if (lastDblArrayReported == null || latestDblArray.length != lastDblArrayReported.length) {
				return true;
			}
			return IntStream.range(0,latestDblArray.length)
					.anyMatch(i -> valueChanged(latestDblArray[i], lastDblArrayReported[i]));
		}

		private boolean intArrayValueChanged() {
			if (lastIntArrayReported == null || latestIntArray.length != lastIntArrayReported.length) {
				return true;
			}
			return IntStream.range(0,latestIntArray.length)
					.anyMatch(i -> valueChanged(latestIntArray[i], lastIntArrayReported[i]));
		}

		private boolean floatArrayValueChanged() {
			if (lastFltArrayReported == null || latestFltArray.length != lastFltArrayReported.length) {
				return true;
			}
			return IntStream.range(0,latestFltArray.length)
					.anyMatch(i -> valueChanged(latestFltArray[i], lastFltArrayReported[i]));
		}

		private boolean shortArrayValueChanged() {
			if (lastShtArrayReported == null || latestShtArray.length != lastShtArrayReported.length) {
				return true;
			}
			return IntStream.range(0,latestShtArray.length)
					.anyMatch(i -> valueChanged(latestShtArray[i], lastShtArrayReported[i]));
		}
	}

	/**
	 * Does the same job as the other formatPosition method except rather than using a supplied format string, use the index of the array of formats this object
	 * holds. This is to be used when an object has multiple elements which describe its position and those element require different formatting.
	 *
	 * @param format
	 *            the index in the array of formats to use
	 * @param number
	 *            the number to format
	 * @return a formatted string
	 */
	public String formatPosition(int format, double number) {
		if (format < outputFormat.length) {
			return String.format(outputFormat[format], number);
		}
		return String.format(outputFormat[0], number);
	}

	/**
	 * Returns the name of the pv this object is monitoring
	 *
	 * @return the name of the pv
	 */
	public String getPvName() {
		return pvName;
	}

	/**
	 * Sets the name of the pv this object monitors. This must be called before the configure method makes the connections to the pv.
	 *
	 * @param pvName
	 */
	public void setPvName(String pvName) {
		this.pvName = pvName;
	}

	/**
	 * Returns the sensitivity of updates from this monitor. The sensitivity is the percentage change which must occur in the pv value for the IObservers to be
	 * informed. This prevents unnecessary updating.
	 *
	 * @return the sensitivity of updates from this monitor
	 */
	public double getSensitivity() {
		return sensitivity * 100;
	}

	/**
	 * Sets the percentage sensitivity level of updates to IObservers from this object.
	 *
	 * @param sensitivity
	 */
	public void setSensitivity(double sensitivity) {
		this.sensitivity = sensitivity / 100;
	}

	private void waitForInitialisation() throws TimeoutException, FactoryException {
		configure();
		final long startTime_ms = System.currentTimeMillis();
		final double timeout_s = EpicsGlobals.getTimeout();
		final long timeout_ms = (long) (timeout_s * 1000.);

		while (!isInitialised && (System.currentTimeMillis() - startTime_ms < timeout_ms)) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
		if (!isInitialised) {
			throw new TimeoutException(getName() + " not yet initalised. Does the PV " + pvName + " exist?");
		}
	}

	@Override
	public void initializationCompleted() {
		isInitialised = true;
		DBR dbr = null;
		try {
			dbr = controller.getCTRL(theChannel);
		} catch (Exception e) {
			logger.error("failed to initialise " + getName() + " after connection", e);
		}
		if (dbr != null) {
			if (dbr.isDOUBLE()) {
				unit = ((DBR_CTRL_Double) dbr).getUnits();
			} else if (dbr.isINT()) {
				unit = ((DBR_CTRL_Int) dbr).getUnits();
			} else if (dbr.isSHORT()) {
				unit = ((DBR_CTRL_Short) dbr).getUnits();
			} else if (dbr.isFLOAT()) {
				unit = ((DBR_CTRL_Float) dbr).getUnits();
			} else if (dbr.isSTRING()) {
				unit = "";
			} else if (dbr.isCTRL() && dbr.isENUM()) {
				unit = "";
			} else if (dbr.isBYTE()) {
				unit = ((DBR_CTRL_Byte) dbr).getUnits();
			}

			elementCount = dbr.getCount();
			type = dbr.getType();
		}
		if (elementCount > 1) {
			latestDblArray = new double[elementCount];
			latestIntArray = new int[elementCount];
			latestShtArray = new short[elementCount];
			latestFltArray = new float[elementCount];
			latestStrArray = new String[elementCount];
			latestByteArray = new byte[elementCount];
		}
		logger.info("Monitor -  " + getName() + " is initialised.");
		setConfigured(isInitialised);

		fetchInitialValue();
	}

	/**
	 * gets the name of the device.
	 *
	 * @return String name
	 */
	public String getDeviceName() {
		return deviceName;
	}

	/**
	 * sets the name of the device.
	 *
	 * @param deviceName
	 */
	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	@Override
	public int getElementCount() throws DeviceException {
		return elementCount;
	}

	/**
	 * @param elementCount
	 */
	public void setElementCount(int elementCount) {
		this.elementCount = elementCount;
	}

	@Override
	public String getUnit() {
		return unit;
	}

	/**
	 * @param unit
	 */
	public void setUnit(String unit) {
		this.unit = unit;
	}

	/**
	 * @return the DBRType that the object looks at
	 */
	public DBRType getDBRType() {
		return type;
	}

	/**
	 * @param type
	 */
	public void setType(DBRType type) {
		this.type = type;
	}

	/**
	 * check the poll flag.
	 *
	 * @return Boolean
	 */
	public boolean isPoll() {
		return poll;
	}

	/**
	 * Set poll to true to ensure Monitor.getPosition() always poll the data from hardware. The default is false.
	 *
	 * @param poll
	 */
	public void setPoll(boolean poll) {
		this.poll = poll;
	}

	@Override
	public String getControllerRecordName() {
		return getPvName();
	}
}

