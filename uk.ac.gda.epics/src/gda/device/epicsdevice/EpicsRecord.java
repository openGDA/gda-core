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
import gda.device.scannable.ScannableBase;
import gda.observable.IObserver;

import java.util.Vector;

/**
 * EpicsRecord Class
 */
public class EpicsRecord extends ScannableBase implements IEpicsChannel {
	final ReturnType returnType;
	final String record, field;
	final double putTimeOutInSec;
	final EpicsDevice epicsDevice;
	Vector<IObserver> observers = null;
	Boolean returnValAsString=false;

	public void setReturnValAsString(Boolean returnValAsString) {
		this.returnValAsString = returnValAsString;
	}

	EpicsRecord(EpicsDevice epicsDevice, String name, ReturnType returnType, String record, String field,
			double putTimeOutInSec) {
		this.epicsDevice = epicsDevice;
		this.returnType = returnType;
		this.record = record != null ? record : "";
		this.field = field != null ? field : "";
		this.putTimeOutInSec = putTimeOutInSec;
		setName(name == null ? (epicsDevice.getName() + ":" + record + "." + field) : name);
		setInputNames(new String[] { getName() });

	}

	/**
	 * @see gda.device.epicsdevice.IEpicsChannel#dispose()
	 */
	@Override
	public void dispose() {
		for (IObserver observer : observers) {
			deleteIObserver(observer);
		}
		observers.removeAllElements();
		observers = null;
	}

	/**
	 * @return EpicsRegistrationRequest
	 */
	public EpicsRegistrationRequest buildRequest() {
		return new EpicsRegistrationRequest(returnType, record, field, "", putTimeOutInSec, true);
	}

	/**
	 * @see gda.device.DeviceBase#addIObserver(gda.observable.IObserver)
	 */
	@Override
	public void addIObserver(IObserver anIObserver) {
		try {
			epicsDevice.addObserver(
					new EpicsRegistrationRequest(returnType, record, field, "", putTimeOutInSec, false), anIObserver);
			if (observers == null) {
				observers = new Vector<IObserver>();
			}
			observers.add(anIObserver);
		} catch (Exception e) {
			throw new RuntimeException("addIObserver failed :", e);
		}
	}

	/**
	 * @see gda.device.DeviceBase#deleteIObserver(gda.observable.IObserver)
	 */
	@Override
	public void deleteIObserver(IObserver anIObserver) {
		epicsDevice.deleteObserver(new EpicsRegistrationRequest(returnType, record, field, "", putTimeOutInSec,
				false), anIObserver);
		observers.removeElement(anIObserver);
	}

	/**
	 * @see gda.device.DeviceBase#deleteIObservers()
	 */
	@Override
	public void deleteIObservers() {
		throw new UnsupportedOperationException("deleteIObservers is not supported");
	}

	/**
	 * @see gda.device.epicsdevice.IEpicsChannel#getValue()
	 */
	@Override
	public Object getValue() throws DeviceException {
		return epicsDevice.getValue(returnType, record, field);
	}

	/**
	 * @param _returnType
	 * @param subField
	 * @return Object
	 * @throws DeviceException
	 */
	public Object getValue(ReturnType _returnType, String subField) throws DeviceException {
		return epicsDevice.getValue(_returnType, record, field + subField);
	}

	/**
	 * @return String
	 * @throws DeviceException
	 */
	public String getValueAsString() throws DeviceException {
		return epicsDevice.getValueAsString(record, field);
	}

	/**
	 * @param subField
	 * @return String
	 * @throws DeviceException
	 */
	public String getValueAsString(String subField) throws DeviceException {
		return epicsDevice.getValueAsString(record, field + subField);
	}

	/**
	 * @see gda.device.epicsdevice.IEpicsChannel#setValue(java.lang.Object)
	 */
	@Override
	public void setValue(Object position) throws DeviceException {
		epicsDevice.setValue(null, record, field, position, putTimeOutInSec);
	}

	/**
	 * @param subField
	 * @param position
	 * @throws DeviceException
	 */
	public void setValue(String subField, Object position) throws DeviceException {
		epicsDevice.setValue(null, record, field + subField, position, putTimeOutInSec);
	}

	/**
	 * @see gda.device.scannable.ScannableBase#asynchronousMoveTo(java.lang.Object)
	 */
	@Override
	public void asynchronousMoveTo(Object position) throws DeviceException {
		setValue(position);
	}

	@Override
	public Object rawGetPosition() throws DeviceException {
		return returnValAsString ? getValueAsString("") : getValue();
	}

	/**
	 * @see gda.device.scannable.ScannableBase#isBusy()
	 */
	@Override
	public boolean isBusy() {
		return false;
	}
}
