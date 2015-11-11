/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

package gda.function.lookup.corba.impl;

import gda.device.DeviceException;
import gda.device.corba.CorbaDeviceException;
import gda.factory.corba.util.EventDispatcher;
import gda.factory.corba.util.EventService;
import gda.function.Lookup;
import gda.function.lookup.corba.CorbaLookupPOA;
import gda.observable.IObserver;

import org.omg.CORBA.Any;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A server side implementation for a distributed BeamInfo interface.
 */
public class LookupImpl extends CorbaLookupPOA implements IObserver {
	private static final Logger logger = LoggerFactory.getLogger(LookupImpl.class);
	private Lookup theObject = null;
	private org.omg.PortableServer.POA poa;
	private EventDispatcher dispatcher;
	private String name;

	/**
	 * Create server side implementation to the CORBA package.
	 *
	 * @param object
	 *            the BeamInfo implementation object
	 * @param poa
	 *            the portable object adapter
	 */
	public LookupImpl(Lookup object, org.omg.PortableServer.POA poa) {
		this.theObject = object;
		this.poa = poa;

		name = theObject.getName();
		dispatcher = EventService.getInstance().getEventDispatcher();
		theObject.addIObserver(this); //FIXME: potential race condition
	}

	/**
	 * Get the implementation object
	 *
	 * @return the BeamInfo implementation object
	 */
	public Lookup _delegate() {
		return theObject;
	}

	/**
	 * Set the implementation object.
	 *
	 * @param theobject
	 *            set the BeamInfo implementation object
	 */
	public void _delegate(Lookup theobject) {
		this.theObject = theobject;
	}

	@Override
	public org.omg.PortableServer.POA _default_POA() {
		return (poa != null) ? poa : super._default_POA();
	}

	@Override
	public void update(Object o, Object arg) {
		dispatcher.publish(name, arg);
	}

	@Override
	public String getFilename() {
		return theObject.getFilename();
	}

	@Override
	public int getNumberOfRows()throws CorbaDeviceException  {
		try {
			return theObject.getNumberOfRows();
		} catch (DeviceException e) {
			logger.error(e.getMessage(), e);
			throw new CorbaDeviceException(e.getMessage());
		}
	}

	@Override
	public String[] getScannableNames() throws CorbaDeviceException {
		try {
			String[] result;
			result = new String[theObject.getScannableNames().size()];
			for (int i = 0; i < theObject.getScannableNames().size(); i++) {
				result[i] = theObject.getScannableNames().get(i);
			}
			return result;
		} catch (DeviceException e) {
			logger.error(e.getMessage(), e);
			throw new CorbaDeviceException(e.getMessage());
		}
	}

	@Override
	public int lookupDecimalPlaces(String arg0) throws CorbaDeviceException {
		try {
			return theObject.lookupDecimalPlaces(arg0);
		} catch (DeviceException e) {
			logger.error(e.getMessage(), e);
			throw new CorbaDeviceException(e.getMessage());
		}
	}

	@Override
	public String lookupUnitString(String arg0) throws CorbaDeviceException {
		try {
			return theObject.lookupUnitString(arg0);
		} catch (DeviceException e) {
			logger.error(e.getMessage(), e);
			throw new CorbaDeviceException(e.getMessage());
		}
	}

	@Override
	public double lookupValue(Any arg0, String arg1) throws CorbaDeviceException {
		try {
			return theObject.lookupValue(arg0, arg1);
		} catch (DeviceException e) {
			logger.error(e.getMessage(), e);
			throw new CorbaDeviceException(e.getMessage());
		}
	}

	@Override
	public void setFilename(String arg0) {
		theObject.setFilename(arg0);
	}

	@Override
	public double[] getLookupKeys() throws CorbaDeviceException {
		try {
			return theObject.getLookupKeys();
		} catch (DeviceException e) {
			logger.error(e.getMessage(), e);
			throw new CorbaDeviceException(e.getMessage());
		}
	}

	@Override
	public void reload() {
		theObject.reload();
	}

}
