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

package gda.device.epicsdevice;

import gda.device.Detector;
import gda.device.Device;
import gda.device.DeviceBase;
import gda.device.DeviceException;
import gda.epics.interfaceSpec.Field;
import gda.epics.interfaceSpec.GDAEpicsInterfaceReader;
import gda.epics.xml.EpicsRecord;
import gda.factory.Configurable;
import gda.factory.FactoryException;
import gda.factory.Finder;
import gda.factory.Localizable;
import gda.observable.IObserver;
import gov.aps.jca.event.PutListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.python.core.PyString;
import org.python.expose.ExposedType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * EpicsDevice class which is placed on the Finder by EpicsDeviceFactory when that factory is created by the
 * ObjectServer if a java property has been set.
 * <p>
 * This is essentially a list of pv's held on the finder under a common name.
 */
@ExposedType(name="findableepicsdevice")
public class FindableEpicsDevice extends DeviceBase implements Device, Configurable, Localizable, IEpicsDevice {
	
	private static final Logger logger = LoggerFactory.getLogger(FindableEpicsDevice.class);

	private EpicsDevice epicsDevice;
	private ArrayList<String> epicsRecordNames = new ArrayList<String>();
	private String deviceName;
	private HashMap<String, String> recordPVs = new HashMap<String, String>();
	protected boolean dummy = false;

	public FindableEpicsDevice() {
		configured = false;
		// do nothing
	}
	
	/**
	 * Constructor to use when you have constructed the EpicsDEvice yourself - not by CASTOR
	 */
	public FindableEpicsDevice(String name, EpicsDevice epicsDevice) {
		this.epicsDevice = epicsDevice;
		configured = true;
		setName(name);
		setDummy(epicsDevice.isInDummyMode());
	}
	@Override
	public void configure() throws FactoryException {
		if (!configured) {
			gda.epics.interfaceSpec.Device device = null;
			if (!epicsRecordNames.isEmpty()) {
				for (String epicsRecordName : epicsRecordNames) {
					EpicsRecord epicsRecord = (EpicsRecord) Finder.getInstance().find(epicsRecordName);
					if (epicsRecord == null)
						throw new IllegalArgumentException("EpicsDevice:" + getName() + " unable to find record  "
								+ epicsRecordName);
					String fullRecordName = epicsRecord.getFullRecordName();
					String shortName = epicsRecord.getShortName();
					if (shortName == null || shortName.equals(""))
						throw new IllegalArgumentException("EpicsDevice:" + getName() + " shortName is not set for  "
								+ epicsRecordName);
					recordPVs.put(shortName, fullRecordName);
				}
			} else if (getDeviceName() != null) {
				try {
					device = GDAEpicsInterfaceReader.getDeviceFromType(null, deviceName);
					for (Iterator<String> fieldName = device.getFieldNames(); fieldName.hasNext();) {
						Field field = device.getField(fieldName.next());
						recordPVs.put(field.getName(), field.getPV());
					}
				} catch (Exception e) {
					throw new IllegalArgumentException("configure failed for FindableEpicsDevice " + getName(), e);
				}

			}
			if (recordPVs.size() == 0 && !dummy)
				throw new IllegalArgumentException("EpicsDevice:" + getName()
						+ " no epicsRecordNames, deviceConfig and not in dummy mode ");

			try {
				epicsDevice = new EpicsDevice(getName(), recordPVs, dummy);
				if (device != null){
					epicsDevice.setAttribute("gda.epics.interfaceSpec.Device", device);
					epicsDevice.setDocString(device.getDescription());
				}
				configured = true;
			} catch (DeviceException e) {
				throw new IllegalArgumentException("configure failed for FindableEpicsDevice " + getName(), e);
			}
		}
	}

	public HashMap<String, String> getRecordPVs() {
		return recordPVs;
	}

	public void setRecordPVs(HashMap<String, String> recordPVs) {
		this.recordPVs = recordPVs;
	}

	public void setDummy(boolean dummy) {
		this.dummy = dummy;
	}

	public boolean getDummy() {
		return dummy;
	}

	private void checkConfigured() throws DeviceException {
		if (!configured)
			throw new DeviceException("EpicsDevice:" + getName() + " not yet configured");
	}

	@Override
	public boolean isConfigured() {
		try {
			checkConfigured();
		} catch (DeviceException e) {
			logger.error(e.getMessage());
			return false;
		}
		return true;
	}

	public void setEpicsRecordNames(ArrayList<String> epicsRecordNames) {
		this.epicsRecordNames = epicsRecordNames;
	}

	public ArrayList<String> getEpicsRecordNames() {
		return epicsRecordNames;
	}

	@Override
	public void close() throws DeviceException {
		checkConfigured();
		epicsDevice.close();
	}

	@Override
	public Object getAttribute(String attributeName) throws DeviceException {
		try {
			// logger.info("getAttribute - in " + getName() + " "
			// +attributeName);
			checkConfigured();
			return epicsDevice.getAttribute(attributeName);
		} finally {
			// logger.info("getAttribute - out " + getName() + " "
			// +attributeName);
		}
	}

	@Override
	public void setAttribute(final String attributeName, final Object value) throws DeviceException {
		try {
			// logger.info("setAttribute - in " + getName() + " " +
			// attributeName);
			checkConfigured();
			epicsDevice.setAttribute(attributeName, value);
		} finally {
			// logger.info("setAttribute - out " + getName() + " "
			// +attributeName);
		}
	}

	@Override
	public void notifyIObservers(Object theObserved, Object theArgument) {
		try {
			// logger.info("notifyIObservers - in " + theArgument.toString());
			super.notifyIObservers(theObserved, theArgument);
		} finally {
			// logger.info("notifyIObservers - out " + theArgument.toString());
		}
	}

	@Override
	public void addIObserver(IObserver anIObserver) {
		if (isConfigured())
			epicsDevice.addIObserver(anIObserver);
	}

	@Override
	public void deleteIObserver(IObserver anIObserver) {
		if (isConfigured())
			epicsDevice.deleteIObserver(anIObserver);
	}

	@Override
	public void deleteIObservers() {
		if (isConfigured())
			epicsDevice.deleteIObservers();
	}

	@Override
	public IEpicsChannel createEpicsChannel(ReturnType returnType, String record, String field, double putTimeout) {
		if (isConfigured())
			return epicsDevice.createEpicsChannel(returnType, record, field, putTimeout);
		return null;
	}

	@Override
	public IEpicsChannel createEpicsChannel(ReturnType returnType, String record, String field) {
		if (isConfigured())
			return epicsDevice.createEpicsChannel(returnType, record, field);
		return null;
	}

	public IEpicsChannel createEpicsChannel() {
		if (isConfigured())
			return epicsDevice.createEpicsChannel(ReturnType.DBR_CTRL, "", "");
		return null;
	}

	public IEpicsChannel createEpicsChannel(String name, ReturnType returnType, String record, String field) {
		if (isConfigured())
			return epicsDevice.createEpicsChannel(name, returnType, record, field);
		return null;
	}

	public IEpicsChannel createEpicsChannel(String name) {
		if (isConfigured())
			return epicsDevice.createEpicsChannel(name, ReturnType.DBR_CTRL, "", "");
		return null;
	}

	public Detector createDetector(String record, String field) {
		if (isConfigured())
			return epicsDevice.createDetector(null, record, field);
		return null;
	}

	public Detector createDetector(String name, String record, String field) {
		if (isConfigured())
			return epicsDevice.createDetector(name, record, field);
		return null;
	}

	public Object getValue(ReturnType returnType, String record, String field) throws DeviceException {
		checkConfigured();
		return epicsDevice.getValue(returnType, record, field);
	}

	public String getValueAsString(String record, String field) throws DeviceException {
		checkConfigured();
		return epicsDevice.getValueAsString(record, field);
	}

	public int getElementCount(String record, String field) throws DeviceException {
		checkConfigured();
		return epicsDevice.getElementCount(record, field);
	}

	public Object getValue() throws DeviceException {
		checkConfigured();
		return epicsDevice.getValue(ReturnType.DBR_CTRL, "", "");
	}

	public void setValue(Object type, String record, String field, Object val, double putTimeout)
			throws DeviceException {
		checkConfigured();
		epicsDevice.setValue(type, record, field, val, putTimeout);
	}

	public void setValue(String record, String field, Object val, double connection_timeout, PutListener listener)
	throws DeviceException {
		checkConfigured();
		epicsDevice.setValue(record, field, val, connection_timeout, listener );
	}
	
	public void setValue(String record, String field, Object val) throws DeviceException {
		checkConfigured();
		epicsDevice.setValue(null, record, field, val, 5.0);
	}

	public void setValueNoWait(String record, String field, Object val) throws DeviceException {
		checkConfigured();
		epicsDevice.setValue(null, record, field, val, -1.0);
	}
	
	public void setValue(Object val) throws DeviceException {
		checkConfigured();
		setValue("", "", val);
	}

	@Override
	public void dispose() throws DeviceException {
		if (isConfigured())
			epicsDevice.dispose();

	}

	@Override
	public void closeUnUsedChannels() throws DeviceException {
		if (isConfigured())
			epicsDevice.closeUnUsedChannels();
	}

	public static IEpicsChannel createSimpleScannable(String device, String record, String field) {
		IEpicsChannel epicsChannel = null;
		IEpicsDevice epicsDevice = (IEpicsDevice) Finder.getInstance().find(device);
		if (epicsDevice == null) {
			logger.error("Unable to find device " + device);
		} else {
			epicsChannel = epicsDevice.createEpicsChannel(ReturnType.DBR_NATIVE, record, field);
		}
		return epicsChannel;
	}

	public static IEpicsChannel createChannel(ReturnType returnType, String device, String record, String field) {
		Object findable = Finder.getInstance().find(device);
		if (findable == null || !(findable instanceof IEpicsDevice)) {
			throw new IllegalArgumentException("FindableEpicsDevice.createChannel.Unable to find IEpicsDevice called "
					+ device);
		}
		return ((IEpicsDevice) findable).createEpicsChannel(returnType, record, field);
	}

	public static IEpicsChannel getCtrlEnumChannel(String device, String record, String field) throws DeviceException {
		IEpicsChannel channel = FindableEpicsDevice.createChannel(ReturnType.DBR_CTRL, device, record, field);
		Object val = channel.getValue();
		if (!(val instanceof EpicsCtrlEnum)) {
			throw new IllegalArgumentException("FindableEpicsDevice.createChannel.  " + channel.getName()
					+ " does not return EpicsCtrlEnum");
		}
		return channel;
	}

	public String getDeviceName() {
		return deviceName;
	}

	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	@Override
	public String toString() {
		return getName();
	}

	final Object __getattr__(String name) {
		return epicsDevice.__getattr__(name);
	}
	
	public Object __getattr__(PyString name) {
		return epicsDevice.__getattr__(name.internedString());
	}	
}
