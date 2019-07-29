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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.factory.FactoryException;
import gda.factory.FindableConfigurableBase;
import gda.observable.IObserver;
import gda.observable.ObservableComponent;
import uk.ac.gda.api.remoting.ServiceInterface;

/**
 * A concrete implementation of the {@link Metadata} interface. The class stores a list of {@link IMetadataEntry} objects
 * and provides access to those objects and their contents. Metadata can be added programatically or configured via XML.
 */
@ServiceInterface(Metadata.class)
public class GdaMetadata extends FindableConfigurableBase implements Metadata, IObserver {
	private static final Logger logger = LoggerFactory.getLogger(GdaMetadata.class);

	private final Map<String, IMetadataEntry> metadataEntries = new LinkedHashMap<>();
	private final ObservableComponent observableComponent = new ObservableComponent();

	/**
	 * Constructor.
	 */
	public GdaMetadata() {
		setName(GDAMetadataProvider.GDAMETADATANAME);
	}

	@Override
	public void configure() throws FactoryException {
		if (isConfigured()) {
			return;
		}
		for (IMetadataEntry me : metadataEntries.values()) {
			me.configure();
		}
		setConfigured(true);
	}

	@Override
	public void setMetadataValue(String name, String metadataValue) {
		if (!metadataEntries.containsKey(name)) {
			logger.warn("Metadata entry for {} is not found.", name);
			return;
		}

		IMetadataEntry me = metadataEntries.get(name);
			try {
				me.setValue(metadataValue);
			} catch (Exception e) {
				logger.error("Problem setting MetadataEntry entry '{}' to '{}'", name, metadataValue, e);
			}
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
	public List<IMetadataEntry> getMetadataEntries() {
		return new ArrayList<>(metadataEntries.values());
	}

	/**
	 * Sets the entries within this metadata.
	 *
	 * @param entries
	 *            the metadata entries
	 */
	public void setMetadataEntries(List<IMetadataEntry> entries) {
		metadataEntries.clear();
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
	public void setName(String name) {
		super.setName(name);
		if (!name.equals(GDAMetadataProvider.GDAMETADATANAME)) {
			logger.warn("GdaMetadata should be named " + GDAMetadataProvider.GDAMETADATANAME);
		}
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


	/**
	 * Convenience method for getting metadata from Jython
	 * <pre>
	 * >>> metadata['visit']
	 * 'ab1234-5'
	 * >>> # is equivalent to
	 * >>> metadata.getMetadataValue('visit')
	 * 'ab1234-5'
	 * >>>
	 * </pre>
	 * NB. this will return an empty string if no matching metadata entry is present
	 *
	 * @param metaname The name of the metadata to get
	 *
	 * @see #getMetadataValue(String)
	 */
	public String __getitem__(String metaname) {
		return getMetadataValue(metaname);
	}

	/**
	 * Convenience method for setting metadata from Jython
	 * <pre>
	 * >>> metadata['visit'] = 'ab1234-5'
	 * >>> # is equivalent to
	 * >>> metadata.setMetadataValue('visit', 'ab1234-5')
	 * </pre>
	 * NB this will not add a new metadata entry if an existing one is not present
	 *
	 * @param metaname The name of the metadata to set
	 * @param metavalue The new value of the metadata entry
	 *
	 * @see #setMetadataValue(String, String)
	 */
	public void __setitem__(String metaname, String metavalue) {
		setMetadataValue(metaname, metavalue);
	}

	/**
	 * Convenience method for checking if a metadata entry is present from Jython.
	 * <pre>
	 * >>> 'visit' in metadata
	 * True
	 * >>> 'foobar' in metadata
	 * False
	 * </pre>
	 *
	 * @param name The metadata entry to check
	 * @return True if there is a metadata entry present for the given name
	 */
	public boolean __contains__(Object name) {
		if (name instanceof String) {
			return metadataEntries.containsKey(name);
		}
		return false;
	}
}
