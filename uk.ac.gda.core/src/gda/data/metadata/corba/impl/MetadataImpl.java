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
import gda.data.metadata.corba.CorbaMetadataEntry;
import gda.data.metadata.corba.CorbaMetadataPOA;
import gda.device.DeviceException;
import gda.device.corba.CorbaDeviceException;
import gda.factory.corba.util.EventDispatcher;
import gda.factory.corba.util.EventService;
import gda.observable.IObserver;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * A server side implementation for a distributed Metadata class
 */
public class MetadataImpl extends CorbaMetadataPOA implements IObserver {
	private Metadata metadata;

	private org.omg.PortableServer.POA poa;

	private EventDispatcher dispatcher;

	private String name;

	/**
	 * Create server side implementation to the CORBA package.
	 *
	 * @param metadata
	 *            the Metadata implementation object
	 * @param poa
	 *            the portable object adapter
	 */
	public MetadataImpl(Metadata metadata, org.omg.PortableServer.POA poa) {
		this.metadata = metadata;
		this.poa = poa;
		dispatcher = EventService.getInstance().getEventDispatcher();
		metadata.addIObserver(this); //FIXME: potential race condition
		name = metadata.getName();
	}

	/**
	 * Get the implementation object
	 *
	 * @return the Metadata implementation object
	 */
	public Metadata _delegate() {
		return metadata;
	}

	/**
	 * Set the implementation object.
	 *
	 * @param metadata
	 *            set the Metadata implementation object
	 */
	public void _delegate(Metadata metadata) {
		this.metadata = metadata;
	}

	@Override
	public org.omg.PortableServer.POA _default_POA() {
		return (poa != null) ? poa : super._default_POA();
	}

	//
	// gda.device.metadata Class Methods.
	//

	@Override
	public void addMetadataEntry(CorbaMetadataEntry metadataEntry) throws CorbaDeviceException {
		try {
			IMetadataEntry me = MetadataCorbaUtils.unmarshal(metadataEntry);
			metadata.addMetadataEntry(me);
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public CorbaMetadataEntry[] getMetadataEntries() throws CorbaDeviceException {
		CorbaMetadataEntry[] mes;
		try {
			ArrayList<IMetadataEntry> metadataEntries = metadata.getMetadataEntries();
			mes = new CorbaMetadataEntry[metadataEntries.size()];
			for (int i = 0; i < metadataEntries.size(); i++) {
				mes[i] = MetadataCorbaUtils.marshal(metadataEntries.get(i));
			}
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
		return mes;
	}

	@Override
	public String getMetadataValue(String name) throws CorbaDeviceException {
		String metadataValue = null;

		try {
			metadataValue = metadata.getMetadataValue(name);
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}

		return metadataValue;
	}

	@Override
	public String getMetadataValue2(String name, String property, String defaultValue) throws CorbaDeviceException {
		String metadataValue = null;

		try {
			metadataValue = metadata.getMetadataValue(name, property, defaultValue);
		} catch (DeviceException ex) {
			throw new CorbaDeviceException("Error getting metadataValue for " + name + " with property " + property
					+ " and defaultValue = " + defaultValue + "." + ex.getMessage());
		}

		return metadataValue;
	}

	@Override
	public void setMetadataValue(String name, String metadataValue) throws CorbaDeviceException {
		try {
			metadata.setMetadataValue(name, metadataValue);
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public void update(Object source, Object arg) {
		// use CORBA event service to broadcast events rather than CORBA messaging service

		if (source instanceof IMetadataEntry && arg instanceof Serializable) {

			MetadataCorbaMessage msg;
			try {
				msg = new MetadataCorbaMessage(MetadataCorbaUtils.marshal((IMetadataEntry) source),
						(Serializable) arg);
				dispatcher.publish(name, msg);
			} catch (DeviceException e) {
				// TODO Auto-generated catch block
			}
		}
	}
}
