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

import gda.factory.FactoryException;
import gda.factory.Findable;
import gda.observable.IObservable;
import gda.observable.IObserver;
import gda.observable.ObservableComponent;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An item of metadata. The user does not need to know the means of access to the metadata value. This is based on the
 * type of this metadata. The current value can be retrieved by by the {@link MetadataEntry#getMetadataValue()} method
 * or via a containing Metadata object and it's {@link Metadata#getMetadataValue(String)} method.
 *
 * @see Metadata
 * @see GdaMetadata
 */
public abstract class MetadataEntry implements Findable, IObservable, Serializable, IMetadataEntry {

	private static final Logger logger = LoggerFactory.getLogger(MetadataEntry.class);

	private transient Metadata metadata;

	private String name = "";

	private String defEntryName = "";

	private transient ObservableComponent observableComponent = new ObservableComponent();

	/**
	 * Configure Method.
	 *
	 * @throws FactoryException
	 */
	@Override
	public void configure() throws FactoryException {
		metadata = GDAMetadataProvider.getInstance();
	}

	@Override
	public String getMetadataValue() {
		String value = "";

		try {
			value = readActualValue();
		} catch (Exception e) {
			/*
			 * fallback to defEntry if supplied
			 */
			try {
				if (defEntryName == null || defEntryName.isEmpty())
					throw e;
				logger.warn("Error getting value for " + name + "; falling back to default entry " + defEntryName);
				value = metadata.getMetadataValue(defEntryName);

			} catch (Exception e2) {
				logger.error( "Error getting value for " + name, e2);
			}
		}


		return value == null ? "" : value;
	}

	/**
	 * Returns the actual value of the metadata entry. Any exception can be
	 * thrown by this method, which will cause {@link #getMetadataValue()} to
	 * look for a default value (if one is specified).
	 *
	 * @return the entry's actual value
	 *
	 * @throws Exception if the value cannot be retrieved
	 */
	protected abstract String readActualValue() throws Exception;

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getDefEntryName() {
		return defEntryName;
	}

	@Override
	public void setDefEntryName(String defEntryName) {
		this.defEntryName = defEntryName;
	}

	@Override
	public void addIObserver(IObserver anIObserver) {
		observableComponent.addIObserver(anIObserver);
	}

	@Override
	public void deleteIObserver(IObserver anIObserver) {
		observableComponent.deleteIObserver(anIObserver);
	}

	@Override
	public void deleteIObservers() {
		observableComponent.deleteIObservers();
	}

	/**
	 * Notify all observers on the list of the requested change.
	 *
	 * @param theObserved
	 *            the observed component
	 * @param theArgument
	 *            the data to be sent to the observer.
	 */
	public void notifyIObservers(Object theObserved, Object theArgument) {
		observableComponent.notifyIObservers(theObserved, theArgument);
	}

	@Override
	public void setValue(String metadataValue) throws Exception {
		// do nothing - state not saved is the default behaviour
	}

	@Override
	public boolean canStoreValue() {
		return false;
	}

	@Override
	public String toString() {
		return String.format("Metadata: %s = %s", getName(), getMetadataValue());
	}
}