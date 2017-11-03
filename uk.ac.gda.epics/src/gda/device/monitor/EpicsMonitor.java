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

import java.lang.reflect.Array;

import org.python.core.PySequence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.epics.ConfigurationNotFoundException;
import gda.configuration.epics.Configurator;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.epics.connection.EpicsChannelManager;
import gda.epics.connection.EpicsController;
import gda.epics.connection.EpicsController.MonitorType;
import gda.epics.connection.InitializationListener;
import gda.epics.interfaceSpec.GDAEpicsInterfaceReader;
import gda.epics.interfaceSpec.InterfaceException;
import gda.epics.interfaces.SimplePvType;
import gda.epics.util.EpicsGlobals;
import gda.epics.xml.EpicsRecord;
import gda.factory.FactoryException;
import gda.factory.Finder;
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

/**
 * A class which monitors the value of single Epics PV. The value is then broadcast to IObservers of this object or can be
 * retrieved via the getValue method. This will not monitor changes to limits, alarms or status.
 */
public class EpicsMonitor extends MonitorBase implements gda.device.Monitor, InitializationListener, Scannable {

	private static final Logger logger = LoggerFactory.getLogger(EpicsMonitor.class);

	private double sensitivity = 0.0;

	protected volatile double latestDblValue;
	protected volatile int latestIntValue;
	protected volatile short latestShtValue;
	protected volatile float latestFltValue;
	protected volatile String latestStrValue="";
	private volatile byte latestByteValue;

	private volatile double[] latestDblArray;
	private volatile int[] latestIntArray;
	private volatile short[] latestShtArray;
	private volatile float[] latestFltArray;
	private volatile String[] latestStrArray;
	private volatile byte[] latestByteArray;

	private int elementCount;
	private String unit = "";
	private DBRType type;

	private String pvName;
	protected Channel theChannel;
	private boolean poll = false;
	private volatile Object latestValue;

	private String epicsRecordName;
	private EpicsRecord epicsRecord;
	private String deviceName;

	private EpicsChannelManager channelManager;
	protected EpicsController controller;

	private boolean isInitialised=false;

	/**
	 * Constructor
	 */
	public EpicsMonitor() {
		controller = EpicsController.getInstance();
		channelManager = new EpicsChannelManager(this);
	}

	@Override
	public void configure() throws FactoryException {
		// this only represents a single value which should be the same string
		// as its name
		this.setInputNames(new String[0]);
		this.setExtraNames(new String[] { getName() });

		if (!configured) {

			if (pvName == null) {

				// EPICS interface version 2 for phase I beamlines + I22
				if (getEpicsRecordName() != null) {
					if ((epicsRecord = (EpicsRecord) Finder.getInstance().find(epicsRecordName)) != null) {
						pvName = epicsRecord.getFullRecordName();
					} else {
						logger.error("Epics Record " + epicsRecordName + " not found");
						throw new FactoryException("Epics Record " + epicsRecordName + " not found");
					}
				}

				// EPICS interface version 3 for phase II beamlines (excluding I22).
				else if (getDeviceName() != null) {
					SimplePvType pvConfig;
					try {
						pvConfig = Configurator.getConfiguration(getDeviceName(), SimplePvType.class);
						pvName = pvConfig.getRECORD().getPv();
					} catch (ConfigurationNotFoundException e) {
						// Try to read from unchecked xml
						try {
							pvName = getPV();
						} catch (Exception ex) {
							logger.error(
									"Can NOT find EPICS configuration for simplePvType " + getDeviceName() + "."
											+ e.getMessage(), ex);
							throw new FactoryException("Can NOT find EPICS configuration for motor " + getDeviceName()
									+ "." + e.getMessage(), e);
						}
					}
				}

				// Nothing specified in Server XML file
				else {
					logger.error("Missing EPICS interface configuration for device " + getName());
					throw new FactoryException("Missing EPICS interface configuration for the device " + getName());
				}
			}

			createChannelAccess();
			channelManager.tryInitialize(100);

			configured = true;
		}
	}

	/**
	 * @return pv
	 * @throws InterfaceException
	 */
	String getPV() throws InterfaceException {
		return GDAEpicsInterfaceReader.getPVFromSimplePVType(getDeviceName());
	}

	private void fetchInitialValue() {
		// fill the latestValue attribute in case its a while until an update
		try {
			boolean old_poll = isPoll();
			poll = true;
			getPosition();
			poll = old_poll;
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
		} catch (Throwable th) {
			throw new FactoryException("failed to create channel", th);
		}
	}

	@Override
	public Object getPosition() throws DeviceException {

		if (!configured) {
			return null;
		}

		try {
			waitForInitialisation();
		} catch (Exception e) {
			throw new DeviceException("Error waiting for initialisation for " + getName(),e);
		}
		if (poll || latestValue == null) {
			if (elementCount == 1)
				return getSingularValue();
			else if (elementCount > 1)
				return getArrayValue();
			else
				throw new DeviceException("Element count is zero for EpicsMonitor " + getName());
		}

		return latestValue;
	}

	protected Object getSingularValue() throws DeviceException {
		try {
			DBR dbr = controller.getCTRL(theChannel);

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
				String[] labels = ((DBR_LABELS_Enum) dbr).getLabels();
				Short labelNumber = ((DBR_LABELS_Enum) dbr).getEnumValue()[0];
				latestStrValue = labels[labelNumber];
				return latestStrValue;
			} else if (dbr.isBYTE()) {
				latestByteValue = ((DBR_CTRL_Byte) dbr).getByteValue()[0];
				return latestByteValue;
			} else if (dbr.isSTRING()) {
				latestStrValue = ((DBR_CTRL_String) dbr).getStringValue()[0];
				return latestStrValue;
			}
		} catch (Throwable e) {
			throw new DeviceException("Can NOT get " + theChannel.getName(), e);
		}

		return "No value is obtained from EPICS";
	}

	private Object getArrayValue() throws DeviceException {
		try {
			DBR dbr = controller.getCTRL(theChannel);

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
		} catch (Throwable e) {
			throw new DeviceException("Can NOT get " + theChannel.getName(), e);
		}

		return "No value is obtained from EPICS";
	}

	/**
	 * Monitor value changes in EPICS and update observers with value, not including unit, alarms,
	 * status or limits.
	*/
	private class ValueMonitorListener implements MonitorListener {
		boolean first=true;
		/**
		 * @see gov.aps.jca.event.MonitorListener#monitorChanged(gov.aps.jca.event.MonitorEvent)
		 */
		@Override
		public void monitorChanged(MonitorEvent arg0) {
			if (first) {
				first=false;
				return;
			}
			DBR dbr = arg0.getDBR();
			if (dbr.getCount() == 1) { // Single PV
				if (dbr.isDOUBLE()) {
					double lastValue = latestDblValue;
					latestDblValue = ((DBR_Double) dbr).getDoubleValue()[0];
					latestValue = latestDblValue;
					if (Math.abs((latestDblValue - lastValue) / lastValue) * 100.0 >= sensitivity) {
						notifyIObservers(this, latestDblValue);
					}
				} else if (dbr.isINT()) {
					int lastValue = latestIntValue;
					latestIntValue = ((DBR_Int) dbr).getIntValue()[0];
					latestValue = latestIntValue;
					if (Math.abs((lastValue - latestIntValue) / lastValue) * 100.0 >= sensitivity) {
						notifyIObservers(this, latestIntValue);
					}
				} else if (dbr.isSHORT()) {
					short lastValue = latestShtValue;
					latestShtValue = ((DBR_Short) dbr).getShortValue()[0];
					latestValue = latestShtValue;
					if (Math.abs((lastValue - latestShtValue) / lastValue) * 100.0 >= sensitivity) {
						notifyIObservers(this, latestShtValue);
					}
				} else if (dbr.isFLOAT()) {
					float lastValue = latestFltValue;
					latestFltValue = ((DBR_Float) dbr).getFloatValue()[0];
					latestValue = latestFltValue;
					if (Math.abs((lastValue - latestFltValue) / lastValue) * 100.0 >= sensitivity) {
						notifyIObservers(this, latestFltValue);
					}
				} else if (dbr.isSTRING()) {
					String lastValue = latestStrValue;
					latestStrValue = ((DBR_String) dbr).getStringValue()[0];
					latestValue = latestStrValue;
					if (!lastValue.equalsIgnoreCase(latestStrValue)) {
						notifyIObservers(this, latestStrValue);
					}
				} else if (dbr.isENUM()) {
					String lastValue = latestStrValue;
					// Use DBR_LABLES_Enum (not DBR_ENUM) to allow the getLables method to be called this
					// allows the monitor to return the new string not the short specifying
					// the position on the enum.
					String[] labels = ((DBR_LABELS_Enum) dbr).getLabels();
					Short labelNumber = ((DBR_LABELS_Enum) dbr).getEnumValue()[0];
					latestStrValue = labels[labelNumber];
					latestValue = latestStrValue;
					if (!lastValue.equalsIgnoreCase(latestStrValue)) {
						notifyIObservers(this, latestStrValue);
					}
				} else if (dbr.isBYTE()) {
					byte lastValue = latestByteValue;
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
					double[] lastValue = latestDblArray;
					latestDblArray = ((DBR_Double) dbr).getDoubleValue();
					latestValue = latestDblArray;
					for (int i = 0; i < latestDblArray.length; i++) {
						if (Math.abs((latestDblArray[i] - lastValue[i]) / lastValue[i]) * 100.0 >= sensitivity) {
							notifyIObservers(this, latestDblArray);
						}
					}
				} else if (dbr.isINT()) {
					int[] lastValue = latestIntArray;
					latestIntArray = ((DBR_Int) dbr).getIntValue();
					latestValue = latestIntArray;
					for (int i = 0; i < lastValue.length; i++) {
						if (Math.abs((latestIntArray[i] - lastValue[i]) / lastValue[i]) * 100.0 >= sensitivity) {
							notifyIObservers(this, latestIntArray);
						}
					}
				} else if (dbr.isSHORT()) {
					short[] lastValue = latestShtArray;
					latestShtArray = ((DBR_Short) dbr).getShortValue();
					latestValue = latestShtArray;
					for (int i = 0; i < latestShtArray.length; i++) {
						if (Math.abs((lastValue[i] - latestShtArray[i]) / lastValue[i]) * 100.0 >= sensitivity) {
							notifyIObservers(this, latestShtValue);
						}
					}
				} else if (dbr.isFLOAT()) {
					float[] lastValue = latestFltArray;
					latestFltArray = ((DBR_Float) dbr).getFloatValue();
					latestValue = latestFltArray;
					for (int i = 0; i < latestFltArray.length; i++) {
						if (Math.abs((lastValue[i] - latestFltArray[i]) / lastValue[i]) * 100.0 >= sensitivity) {
							notifyIObservers(this, latestFltValue);
						}
					}
				} else if (dbr.isSTRING()) {
					String[] lastValue = latestStrArray;
					latestStrArray = ((DBR_String) dbr).getStringValue();
					latestValue = latestStrArray;
					for (int i = 0; i < latestStrArray.length; i++) {
						if (!lastValue[i].equalsIgnoreCase(latestStrArray[i])) {
							notifyIObservers(this, latestStrValue);
						}
					}
				} else if (dbr.isENUM()) {
					String[] lastValue = latestStrArray;
					// Use DBR_LABLES_Enum (not DBR_ENUM) to allow the getLables method to be called this
					// allows the monitor to return the new string not the short specifying
					// the position on the enum.
					latestStrArray = ((DBR_LABELS_Enum) dbr).getLabels();
					latestValue = latestStrArray;
					for (int i = 0; i < latestStrArray.length; i++) {
						if (!lastValue[i].equalsIgnoreCase(latestStrArray[i])) {
							notifyIObservers(this, latestStrValue);
						}
					}
				} else if (dbr.isBYTE()) {
					byte[] lastValue = latestByteArray;
					latestByteArray = ((DBR_Byte) dbr).getByteValue();
					latestValue = latestByteArray;
					for (int i = 0; i < lastValue.length; i++) {
						if (latestByteArray[i] != lastValue[i]) {
							notifyIObservers(this, latestByteArray);
						}
					}
				} else {
					logger.error("The monitored PV type was not matched");
				}
			}
		}

	}

	@Override
	public String toFormattedString() {
		String myString = "";
		try {
			Object position = this.getPosition();

			if (position == null) {
				logger.warn("getPosition() from " + this.getName() + " returns NULL.");
				return this.getName() + " : NOT AVAILABLE";
			}
			// print out simple version if only one inputName and
			// getPosition and getReportingUnits do not return arrays.
			if (!(position.getClass().isArray() || position instanceof PySequence)) {
				myString += this.getName() + " : ";
				if (position instanceof String) {
					myString += position.toString();
				} else {
					myString += this.formatPosition(0, Double.parseDouble(position.toString()));
				}
			} else {
				myString += this.getName() + " : ";
				if (position instanceof PySequence) {
					for (int i = 0; i < ((PySequence) position).__len__(); i++) {
						if (i > 0) {
							myString += " ";
						}
						myString += this.formatPosition(i, Double.parseDouble(((PySequence) position).__finditem__(i)
								.toString()));
					}
				} else {
					for (int i = 0; i < Array.getLength(position); i++) {
						if (i > 0) {
							myString += " ";
						}
						myString += this.formatPosition(i, Double.parseDouble(Array.get(position, i).toString()));
					}
				}

			}
		} catch (Exception e) {
			logger.warn("Exception formatting {}", getName(), e);
		}
		return myString.isEmpty() ? valueUnavailableString() : myString + " " + getUnit();
	}

	/**
	 * Does the same job as the other formatPosition method except rather than using a supplied format string, use the
	 * index of the array of formats this object holds. This is to be used when an object has multiple elements which
	 * describe its position and those element require different formatting.
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
	 * Sets the name of the pv this object monitors. This must be called before the configure method makes the
	 * connections to the pv.
	 *
	 * @param pvName
	 */
	public void setPvName(String pvName) {
		this.pvName = pvName;
	}

	/**
	 * Returns the sensitivity of updates from this monitor. The sensitivity is the percentage change which must occur
	 * in the pv value for the IObservers to be informed. This prevents unneccessary updating.
	 *
	 * @return the sensitivity of updates from this monitor
	 */
	public double getSensitivity() {
		return sensitivity;
	}

	/**
	 * Sets the sensitivity level of updates to IObservers from this object.
	 *
	 * @param sensitivity
	 */
	public void setSensitivity(double sensitivity) {
		this.sensitivity = sensitivity;
	}

	private void ensuredConfigured() throws FactoryException{
		if(!configured)
			configure();
	}

	public boolean isInitialised() {
		return isInitialised;
	}
	/**
	 * @param initialised
	 */
	public void setInitialised(boolean initialised) {
		this.isInitialised = initialised;
	}
	private void waitForInitialisation() throws TimeoutException, FactoryException{
		ensuredConfigured();
		long startTime_ms =	System.currentTimeMillis();
		double timeout_s = EpicsGlobals.getTimeout();
		long timeout_ms = (long)(timeout_s*1000.);

		while (!isInitialised() && (System.currentTimeMillis() - startTime_ms < timeout_ms) ){
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				//do nothing
			}
		}
		if(!isInitialised())
			throw new TimeoutException(getName() + " not yet initalised. Does the PV " + pvName + " exist?");
	}

	@Override
	public void initializationCompleted() {
		setInitialised(true);
		DBR dbr = null;
		try {
			dbr = controller.getCTRL(theChannel);
		} catch (Throwable e) {
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

		fetchInitialValue();
	}

	/**
	 * @return epicsRecordName
	 */
	public String getEpicsRecordName() {
		return epicsRecordName;
	}

	/**
	 * @param epicsRecordName
	 */
	public void setEpicsRecordName(String epicsRecordName) {
		this.epicsRecordName = epicsRecordName;
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
}
