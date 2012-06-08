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

package gda.data.metadata.corba.impl;

import gda.data.metadata.IMetadataEntry;
import gda.data.metadata.Metadata;
import gda.data.metadata.corba.CorbaMetadata;
import gda.data.metadata.corba.CorbaMetadataEntry;
import gda.data.metadata.corba.CorbaMetadataHelper;
import gda.device.DeviceException;
import gda.device.corba.CorbaDeviceException;
import gda.factory.Findable;
import gda.factory.corba.util.EventService;
import gda.factory.corba.util.EventSubscriber;
import gda.factory.corba.util.NameFilter;
import gda.factory.corba.util.NetService;
import gda.observable.IObserver;
import gda.observable.ObservableComponent;

import java.util.ArrayList;

import org.omg.CORBA.COMM_FAILURE;
import org.omg.CORBA.TRANSIENT;

/**
 * A client side implementation of the adapter pattern for the Metadata class
 */
public class MetadataAdapter implements Metadata, Findable, EventSubscriber {
	private CorbaMetadata corbaMetadata;

	private NetService netService;

	private String name;

	protected ObservableComponent observableComponent = new ObservableComponent();
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
	public MetadataAdapter(org.omg.CORBA.Object obj, String name, NetService netService) {
		corbaMetadata = CorbaMetadataHelper.narrow(obj);
		this.netService = netService;
		this.name = name;
		
		EventService eventService = EventService.getInstance();
		if (eventService != null) {
			eventService.subscribe(this, new NameFilter(name, this.observableComponent));
		}
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		// see bugzilla bug #443
	}

	@Override
	public void addMetadataEntry(IMetadataEntry entry) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				CorbaMetadataEntry cme = MetadataCorbaUtils.marshal(entry);
				corbaMetadata.addMetadataEntry(cme);
				return;
			} catch (COMM_FAILURE cf) {
				corbaMetadata = CorbaMetadataHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaMetadata = CorbaMetadataHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException e) {
				throw new DeviceException(e.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public ArrayList<IMetadataEntry> getMetadataEntries() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				CorbaMetadataEntry[] cmes = corbaMetadata.getMetadataEntries();
				ArrayList<IMetadataEntry> metadataEntries = new ArrayList<IMetadataEntry>();
				for (CorbaMetadataEntry cme : cmes) {
					metadataEntries.add(MetadataCorbaUtils.unmarshal(cme));
				}
				return metadataEntries;
			} catch (COMM_FAILURE cf) {
				corbaMetadata = CorbaMetadataHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaMetadata = CorbaMetadataHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException e) {
				throw new DeviceException(e.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public String getMetadataValue(String name) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaMetadata.getMetadataValue(name);
			} catch (COMM_FAILURE cf) {
				corbaMetadata = CorbaMetadataHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaMetadata = CorbaMetadataHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException e) {
				throw new DeviceException(e.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public String getMetadataValue(String name, String property, String defaultValue) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaMetadata.getMetadataValue2(name, property, defaultValue);
			} catch (COMM_FAILURE cf) {
				corbaMetadata = CorbaMetadataHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaMetadata = CorbaMetadataHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException e) {
				throw new DeviceException(e.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void setMetadataValue(String name, String metadataValue) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaMetadata.setMetadataValue(name, metadataValue);
				return;
			} catch (COMM_FAILURE cf) {
				corbaMetadata = CorbaMetadataHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaMetadata = CorbaMetadataHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException e) {
				throw new DeviceException(e.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void addIObserver(IObserver observer) {
		observableComponent.addIObserver(observer);
	}

	@Override
	public void deleteIObserver(IObserver observer) {
		observableComponent.deleteIObserver(observer);
	}

	@Override
	public void deleteIObservers() {
		observableComponent.deleteIObservers();
	}

	/**
	 * Notify observers of this class.
	 * 
	 * @param source the observed object
	 * @param arg the changed code
	 */
	public void notifyIObservers(Object source, Object arg) {
		observableComponent.notifyIObservers(source, arg);
	}

	@Override
	public void inform(Object message) {
		if (message instanceof MetadataCorbaMessage){
			try {
				IMetadataEntry entry = MetadataCorbaUtils.unmarshal(((MetadataCorbaMessage)message).getMetadataObj());
				Object arg = ((MetadataCorbaMessage)message).getArg();
				notifyIObservers(entry, arg);
			} catch (DeviceException e) {
				// TODO Auto-generated catch block
//				logger.error("TODO put description of error here", e);
			}
		}
	}

}
