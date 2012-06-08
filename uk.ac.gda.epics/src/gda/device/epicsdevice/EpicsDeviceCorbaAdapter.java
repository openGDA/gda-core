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

import gda.device.DeviceException;
import gda.device.epicsdevice.corba.impl.EpicsdeviceAdapter;
import gda.observable.IObservable;
import gda.observable.IObserver;
import gda.observable.ObservableComponent;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.scannable.*;

/**
 * A client side implementation of the adapter pattern for the ControlPoint class
 */
public class EpicsDeviceCorbaAdapter implements IEpicsDevice {
	
	private static final Logger logger = LoggerFactory.getLogger(EpicsDeviceCorbaAdapter.class);

	private HashMap<EpicsRegistrationRequest, ChannelMonitorList> registrations = new HashMap<EpicsRegistrationRequest, ChannelMonitorList>();

	final private EpicsdeviceAdapter adapter;

	/**
	 * @param adapter
	 * @param observable
	 */
	public EpicsDeviceCorbaAdapter(EpicsdeviceAdapter adapter, @SuppressWarnings("unused") IObservable observable) {
		this.adapter = adapter;
	}

	/**
	 * @see gda.device.epicsdevice.IEpicsDevice#dispose()
	 */
	@Override
	synchronized public void dispose() {
		// unregister all open registrations - if some are left open no updates
		// will take place.
		{
			Iterator<Map.Entry<EpicsRegistrationRequest, ChannelMonitorList>> iter = registrations.entrySet()
					.iterator();
			while (iter.hasNext()) {
				Map.Entry<EpicsRegistrationRequest, ChannelMonitorList> e = iter.next();
				try {
					EpicsDevice.removeRegistration(e.getKey(), adapter);
				} catch (Exception expt) {
					logger.error(expt.getMessage());
				}
				iter.remove();
			}
		}
	}

	/**
	 * @param returnType
	 * @param record
	 * @param field
	 * @return value
	 * @throws DeviceException
	 */
	public Object getValue(ReturnType returnType, String record, String field) throws DeviceException {
		return EpicsDevice.getValueDev(returnType, record, field, adapter);
	}

	/**
	 * @param type
	 * @param record
	 * @param field
	 * @param putTimeout
	 * @param value
	 * @throws DeviceException
	 */
	public void setValue(Object type, String record, String field, double putTimeout, Object value)
			throws DeviceException {
		// only allow null value for type
		if (type != null)
			throw new DeviceException("EpicsdeviceAdapter.setValue - error, Only null is allowed for type value");
		EpicsDevice.setValueDev(record, field, value, putTimeout, adapter);
	}

	void _register(CorbaEpicsRecord wrapper){
		EpicsRegistrationRequest request = wrapper.buildRequest();
		ChannelMonitorList registeredList = addToRegistration(request);
		registeredList.add(wrapper);
		if (!registeredList.isRegistered())
			getRegistration(request);
	}

	synchronized ChannelMonitorList addToRegistration(EpicsRegistrationRequest request)
	{
		// search map to see if this type is already registered
		// if it is add this observer to list
		// else send command to implementor and retrieve the ID
		// store ID and observer in map
		// I need a map of wrapperstrings and registrations
		if (!registrations.containsKey(request)) {
			ChannelMonitorList registeredList = new ChannelMonitorList();
			registrations.put(request, registeredList);
			return registeredList;
		}
		return registrations.get(request);
	}

	void getRegistration(EpicsRegistrationRequest request) {
		try {
			EpicsDevice.getRegistration(request, adapter);
		} catch (DeviceException e) {
			// TODO - we should record the fact that the registration failed
			// somewhere and allow access to this information
			// As the server does the connection to Epics in a separate thread
			// we will not see
			// channel connection problems
			logger.error(e.getMessage());
		}
	}

	/**
	 * @param theObserved
	 * @param theArgument
	 */
	public void notifyIObservers(@SuppressWarnings("unused") Object theObserved, Object theArgument) {
		/*
		 * if theArgument is of type EpicsDeviceEvent then look for it in the registration list. If an entry is found
		 * that notify it
		 */
		EpicsRegistrationRequest requestWithPVName = ((EpicsDeviceEvent) theArgument).request;
		EpicsRegistrationRequest request = requestWithPVName.removePVName();
		ChannelMonitorList registeredForThisEvent = registrations.get(request);
		if (registeredForThisEvent != null) {
			registeredForThisEvent.update(requestWithPVName, ((EpicsDeviceEvent) theArgument).event);
		}
	}

	/**
	 * @param theObserved
	 * @param theArgument
	 */
	public void notifyOfEpicsDeviceEvent(Object theObserved, Object theArgument) {
		if (theArgument instanceof EpicsDeviceEvent) {
			notifyIObservers(theObserved, theArgument);
		} else {
			adapter.notifyOfOtherEvents(theObserved, theArgument);
		}
	}

	void _unregister(CorbaEpicsRecord record) throws DeviceException {
		// remove ID from map
		// if list is empty for this type send command to stop monitor
		Iterator<Map.Entry<EpicsRegistrationRequest, ChannelMonitorList>> iter = registrations.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<EpicsRegistrationRequest, ChannelMonitorList> e = iter.next();
			ChannelMonitorList list = e.getValue();
			EpicsRegistrationRequest request = e.getKey();
			if (list.contains(record)) {
				list.unregister(record);
				if (list.isEmpty()) {
					iter.remove();
					removeRegistration(request);
				}
				break;
			}
		}

	}

	void removeRegistration(EpicsRegistrationRequest request) throws DeviceException {
		EpicsDevice.removeRegistration(request, adapter);
	}

	void registerWithBaseForUpdatesFromServer(IObserver observer) {
		// register with parent as well as the parent is the device
		adapter.addIObserver(observer);

	}

	void unregisterWithBaseForUpdatesFromServer(IObserver observer) {
		// register with parent as well as the parent is the device
		adapter.deleteIObserver(observer);

	}

	@Override
	public IEpicsChannel createEpicsChannel(ReturnType returnType, String record, String field) {
		return createEpicsChannel(returnType, record, field, defPutTimeOutInSec);
	}

	@Override
	public IEpicsChannel createEpicsChannel(ReturnType returnType, String record, String field, double putTimeout) {
		return new CorbaEpicsRecord(this, returnType, record, field, putTimeout);
	}

	@Override
	public void closeUnUsedChannels() throws DeviceException {
		EpicsDevice.closeUnUsedChannelsDev(adapter);
	}
}

final class CorbaEpicsRecord extends ScannableBase implements IEpicsChannel, IObserver {
	
	private static final Logger logger = LoggerFactory.getLogger(CorbaEpicsRecord.class);
	
	final ReturnType returnType;
	final String record, field;
	private ObservableComponent observableComponent = new ObservableComponent();
	private final Double putTimeout;
	private final EpicsDeviceCorbaAdapter adapter;
	private boolean receivedUpdate = false;

	CorbaEpicsRecord(EpicsDeviceCorbaAdapter adapter, ReturnType returnType, String record, String field,
			Double putTimeout) {
		this.adapter = adapter;
		this.returnType = returnType;
		this.record = record != null ? record : "";
		this.field = field != null ? field : "";
		this.putTimeout = putTimeout;
	}

	@Override
	public void dispose() {
		deleteIObservers();
	}

	/**
	 * @return boolean true if received update
	 */
	public boolean hasReceivedUpdate() {
		return receivedUpdate;
	}

	/**
	 * @return EpicsRegistrationRequest
	 */
	public EpicsRegistrationRequest buildRequest() {
		return new EpicsRegistrationRequest(returnType, record, field, "", putTimeout, true);
	}

	@Override
	public synchronized void addIObserver(IObserver anIObserver) {
		// logger.info("addIObserver in " + this.record);
		if (!observableComponent.IsBeingObserved()) {
			// add before registering so we get the first event
			observableComponent.addIObserver(anIObserver);
			try {
				adapter.registerWithBaseForUpdatesFromServer(this);
				adapter._register(this);
			} catch (Exception e) {
				logger.error(e.getMessage(),e);
			}
		} else
			observableComponent.addIObserver(anIObserver);
		// logger.info("addIObserver out " + this.record);
	}

	@Override
	public synchronized void deleteIObserver(IObserver anIObserver) {
		observableComponent.deleteIObserver(anIObserver);
		if (!observableComponent.IsBeingObserved()) {
			try {
				adapter._unregister(this);
				adapter.unregisterWithBaseForUpdatesFromServer(this);
			} catch (Exception e) {
				logger.error(e.getMessage(),e);
			}
		}
	}

	@Override
	public void deleteIObservers() {
		observableComponent.deleteIObservers();
		try {
			adapter._unregister(this);
			adapter.unregisterWithBaseForUpdatesFromServer(this);
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
	}

	@Override
	public synchronized void update(Object theObserved, Object changeCode) {
		receivedUpdate = true;
		observableComponent.notifyIObservers(theObserved, changeCode);
	}

	@Override
	public synchronized Object getValue() throws DeviceException {
		return adapter.getValue(returnType, record, field);
	}

	@Override
	public void setValue(Object position) throws DeviceException {
		adapter.setValue(null, record, field, putTimeout, position);
	}

	@Override
	public void asynchronousMoveTo(Object position) throws DeviceException {
		setValue(position);

	}

	@Override
	public Object getPosition() throws DeviceException {
		return getValue();
	}

	@Override
	public boolean isBusy() {
		return false;
	}

}

final class ChannelMonitorList {
	ArrayList<CorbaEpicsRecord> records;
	Object lastValue;
	EpicsRegistrationRequest lastEpicsRegistrationRequest;
	boolean registered = false;

	ChannelMonitorList() {
		records = new ArrayList<CorbaEpicsRecord>();
	}

	synchronized boolean isRegistered() {
		boolean oldValue = registered;
		registered = true;
		return oldValue;
	}

	synchronized void add(CorbaEpicsRecord record) {
		// registered = true;
		records.add(record);
		if (lastValue != null && lastEpicsRegistrationRequest != null) {
			// logger.debug("add - last value exists " + record.field);
			record.update(lastEpicsRegistrationRequest, lastValue);
		} else {
			// logger.debug("add - no last value");
		}
	}

	synchronized void update(EpicsRegistrationRequest requestWithPVName, Object object) {
		if (lastValue == null) {
			// logger.debug("update - first update" + requestWithPVName.pvName);
		} else {
			// logger.debug("update - not first update" +
			// requestWithPVName.pvName);
		}
		lastValue = object;
		lastEpicsRegistrationRequest = requestWithPVName;
		for (CorbaEpicsRecord record : records) {
			record.update(requestWithPVName, object);
		}
	}

	synchronized void unregister(CorbaEpicsRecord record) {
		int index = records.indexOf(record);
		if (index != -1) {
			records.remove(index);
		}
	}

	synchronized boolean isEmpty() {
		return records.isEmpty();
	}

	synchronized boolean contains(CorbaEpicsRecord record) {
		return records.contains(record);
	}
}
