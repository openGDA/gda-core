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
import gda.device.scannable.ScannableUtils;
import gda.factory.Findable;
import gda.factory.corba.util.EventService;
import gda.factory.corba.util.EventSubscriber;
import gda.factory.corba.util.NameFilter;
import gda.factory.corba.util.NetService;
import gda.function.Lookup;
import gda.function.lookup.corba.CorbaLookup;
import gda.function.lookup.corba.CorbaLookupHelper;
import gda.observable.IObserver;
import gda.observable.ObservableComponent;

import java.io.Serializable;
import java.util.ArrayList;

import org.omg.CORBA.COMM_FAILURE;
import org.omg.CORBA.TRANSIENT;
import org.python.core.PyObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A client side implementation of the Lookup interface
 */
public class LookupAdapter implements Lookup, EventSubscriber, Findable {
	private static final Logger logger = LoggerFactory.getLogger(LookupAdapter.class);
	CorbaLookup paramsObj;

	NetService netService;

	String name;

	private ObservableComponent observableComponent = new ObservableComponent();

	/**
	 * Create client side interface to the CORBA package.
	 *
	 * @param obj
	 *            the CORBA object
	 * @param name
	 *            the name of the object
	 * @param netService
	 *            the CORBA naming service
	 */
	public LookupAdapter(org.omg.CORBA.Object obj, String name, NetService netService) {
		paramsObj = CorbaLookupHelper.narrow(obj);
		this.netService = netService;
		this.name = name;

		// subscribe to events coming over CORBA from the impl
		EventService eventService = EventService.getInstance();
		if (eventService != null)
			eventService.subscribe(this, new NameFilter(name, observableComponent));
	}

	// this is for accepting events over the CORBA event mechanism
	@Override
	public void inform(Object obj) {
		if (obj == null)
			logger.debug("Received event for NULL");
		notifyIObservers(this, obj);
	}

	@Override
	public void setName(String name) {
		// see bugzilla bug #443
	}

	@Override
	public String getName() {
		return name;
	}

	/**
	 * @param anIObserver
	 *
	 * @see gda.observable.IObservable#addIObserver(gda.observable.IObserver)
	 */
	@Override
	public void addIObserver(IObserver anIObserver) {
		observableComponent.addIObserver(anIObserver);
	}

	/**
	 * @param anIObserver
	 *
	 * @see gda.observable.IObservable#deleteIObserver(gda.observable.IObserver)
	 */
	@Override
	public void deleteIObserver(IObserver anIObserver) {
		observableComponent.deleteIObserver(anIObserver);
	}

	/**
	 *
	 * @see gda.observable.IObservable#deleteIObservers()
	 */
	@Override
	public void deleteIObservers() {
		observableComponent.deleteIObservers();
	}

	/**
	 * Notify observers of this class.
	 *
	 * @param theObserved
	 *            the observed class
	 * @param changeCode
	 *            the changed code
	 */
	public void notifyIObservers(java.lang.Object theObserved, java.lang.Object changeCode) {
		observableComponent.notifyIObservers(theObserved, changeCode);
	}
	@Override
	public String getFilename() {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				String result = paramsObj.getFilename();
				return result;
			} catch (COMM_FAILURE cf) {
				paramsObj = CorbaLookupHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				paramsObj = CorbaLookupHelper.narrow(netService.reconnect(name));
			}
		}
		return "Not Available";
	}

	@Override
	public int getNumberOfRows() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return paramsObj.getNumberOfRows();
			} catch (CorbaDeviceException e) {
				throw new DeviceException("Could not get number of rows", e);
			} catch (COMM_FAILURE cf) {
				paramsObj = CorbaLookupHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				paramsObj = CorbaLookupHelper.narrow(netService.reconnect(name));
			}
		}
		return -1;
	}

	@Override
	public ArrayList<String> getScannableNames() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				String[] result = paramsObj.getScannableNames();
				ArrayList<String> scannableNames = new ArrayList<String>();
				for (int j = 0; j < result.length; j++) {
					scannableNames.add(result[j]);
				}
				return scannableNames;
			} catch (CorbaDeviceException e) {
				throw new DeviceException("Could not get scannable names", e);
			} catch (COMM_FAILURE cf) {
				paramsObj = CorbaLookupHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				paramsObj = CorbaLookupHelper.narrow(netService.reconnect(name));
			}
		}
		return null;
	}

	@Override
	public int lookupDecimalPlaces(String scannableName) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return paramsObj.lookupDecimalPlaces(scannableName);
			} catch (CorbaDeviceException e) {
				throw new DeviceException("Could not lookup decimal places for scannable " + scannableName, e);
			} catch (COMM_FAILURE cf) {
				paramsObj = CorbaLookupHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				paramsObj = CorbaLookupHelper.narrow(netService.reconnect(name));
			}
		}
		return -1;
	}
	@Override
	public String lookupUnitString(String scannableName) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return paramsObj.lookupUnitString(scannableName);
			} catch (CorbaDeviceException e) {
				throw new DeviceException("Could not look up unit string for scannable " + scannableName, e);
			} catch (COMM_FAILURE cf) {
				paramsObj = CorbaLookupHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				paramsObj = CorbaLookupHelper.narrow(netService.reconnect(name));
			}
		}
		return "Unit null";
	}
	@Override
	public double lookupValue(Object energy, String scannableName) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				if (energy instanceof PyObject) {
					energy = ScannableUtils.convertToJava((PyObject) energy);
				}
				org.omg.CORBA.Any any = org.omg.CORBA.ORB.init().create_any();
				any.insert_Value((Serializable) energy);
				return paramsObj.lookupValue(any, scannableName);
			} catch (CorbaDeviceException e) {
				throw new DeviceException("Could not lookup value for energy " + energy + " and scannable " + scannableName, e);
			} catch (COMM_FAILURE cf) {
				paramsObj = CorbaLookupHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				paramsObj = CorbaLookupHelper.narrow(netService.reconnect(name));
			}
		}
		return -1.0;
	}

	@Override
	public double[] getLookupKeys() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return paramsObj.getLookupKeys();
			} catch (CorbaDeviceException e) {
				throw new DeviceException("Could not look up keys", e);
			} catch (COMM_FAILURE cf) {
				paramsObj = CorbaLookupHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				paramsObj = CorbaLookupHelper.narrow(netService.reconnect(name));
			}
		}
		return null;
	}



	@Override
	public void setFilename(String filename) {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				paramsObj.setFilename(filename);
				return;
			} catch (COMM_FAILURE cf) {
				paramsObj = CorbaLookupHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				paramsObj = CorbaLookupHelper.narrow(netService.reconnect(name));
			}
		}
	}

	@Override
	public void reload() {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				paramsObj.reload();
				return;
			} catch (COMM_FAILURE cf) {
				paramsObj = CorbaLookupHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				paramsObj = CorbaLookupHelper.narrow(netService.reconnect(name));
			}
		}
	}

}
