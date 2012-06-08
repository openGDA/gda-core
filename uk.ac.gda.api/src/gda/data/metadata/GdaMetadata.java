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

package gda.data.metadata;

import gda.configuration.properties.LocalProperties;
import gda.factory.Configurable;
import gda.factory.FactoryException;
import gda.factory.Localizable;
import gda.observable.IObserver;
import gda.observable.ObservableComponent;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A concrete implementation of the {@link Metadata} interface. The class stores a list of {@link IMetadataEntry} objects
 * and provides access to those objects and their contents. Metadata can be added programatically or configured via XML.
 */
public class GdaMetadata implements Metadata, Configurable, Localizable, IObserver {
	private static final Logger logger = LoggerFactory.getLogger(GdaMetadata.class);
	private String name;

	private boolean local;

	Map<String, IMetadataEntry> metadataEntries = new LinkedHashMap<String, IMetadataEntry>();
	private ObservableComponent observableComponent = new ObservableComponent();

	/**
	 * Constructor.
	 */
	public GdaMetadata() {
		setName(GDAMetadataProvider.GDAMETADATANAME);
	}

	@Override
	public void configure() throws FactoryException {
		for (IMetadataEntry me : metadataEntries.values()) {
			me.configure();
		}
	}

	@Override
	public void setMetadataValue(String name, String metadataValue) {
		if (!metadataEntries.containsKey(name)) {
			logger.warn("Metadata entry for {} is not found.", name);
			return;
		}

		IMetadataEntry me = metadataEntries.get(name);
//		if (me instanceof StoredMetadataEntry) {
//			((StoredMetadataEntry) me).setValue(metadataValue);
//			return;
//		}
//		if (me instanceof PersistantMetadataEntry) {
			try {
				me.setValue(metadataValue);
			} catch (Exception e) {
				logger.error("Problem setting MetadataEntry entry {} to {} : {}", name, metadataValue);
				logger.error(e.toString());
			}
			return;
//		}

//		logger.warn("Metadata entry {} is not a StoredMetadataEntry or PersistantMetadataEntry, so its value cannot be set", name);
	}

	@Override
	public String getMetadataValue(String name) {
		String value = "";

		if (metadataEntries.containsKey(name)) {
			IMetadataEntry me = metadataEntries.get(name);
			value = me.getMetadataValue();
		}

		return value;
	}

	@Override
	public String getMetadataValue(String metadataName, String fallbackJavaProperty, String defaultValue) {
		String value = getMetadataValue(metadataName);

		if (value.equals("") && (fallbackJavaProperty != null) && !fallbackJavaProperty.equals("")) {
			value = LocalProperties.get(fallbackJavaProperty, defaultValue);
		} else if (value.equals("")) {
			value = defaultValue;
		}

		return value;
	}

	@Override
	public void addMetadataEntry(IMetadataEntry entry) {
		metadataEntries.put(entry.getName(), entry);
		entry.addIObserver(this);
	}

	@Override
	public ArrayList<IMetadataEntry> getMetadataEntries() {
		return new ArrayList<IMetadataEntry>(metadataEntries.values());
	}

	/**
	 * Sets the entries within this metadata.
	 * 
	 * @param entries
	 *            the metadata entries
	 */
	public void setMetadataEntries(ArrayList<IMetadataEntry> entries) {
		metadataEntries = new LinkedHashMap<String, IMetadataEntry>();
		for (IMetadataEntry entry : entries) {
			addMetadataEntry(entry);
		}
	}

	/**
	 * Removes the metadata entry with the specified name.
	 * 
	 * @param name
	 *            the metadata entry name
	 */
	public synchronized void removeMetadataEntry(String name) {
		metadataEntries.remove(name);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
		if (!name.equals(GDAMetadataProvider.GDAMETADATANAME)) {
			logger.warn("GdaMetadata should be named " + GDAMetadataProvider.GDAMETADATANAME);
		}
	}

	@Override
	public boolean isLocal() {
		return local;
	}

	@Override
	public void setLocal(boolean local) {
		this.local = local;
	}

	@Override
	public void update(Object source, Object arg) {
		if (source instanceof IMetadataEntry && getMetadataEntries().contains(source)){
			observableComponent.notifyIObservers(source,arg);
		}
		
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
}
