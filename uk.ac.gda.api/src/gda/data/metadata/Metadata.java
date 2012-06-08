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

import gda.device.DeviceException;
import gda.factory.Findable;
import gda.observable.IObservable;

import java.util.ArrayList;

/**
 * An interface specifying access to metadata in GDA.
 * 
 * @see GdaMetadata GDAMetadata : a concrete implementation.
 */
public interface Metadata extends Findable, IObservable {
	/**
	 * The Constant NONE.
	 */
	public static final String NONE = "None available";
	
	/**
	 * Adds an entry to the metadata list or replaces its contents. If the name of the entry matches one already in the
	 * metadata list then the properties of that entry are changed to be the same as those passed in.
	 * 
	 * @param entry
	 *            MetadataListEntry The entry to be added.
	 * @throws DeviceException
	 */
	public abstract void addMetadataEntry(IMetadataEntry entry) throws DeviceException;

	/**
	 * Fetches all the metadata entries in the list of metadata.
	 * 
	 * @return ArrayList The list of MetadataEntry objects.
	 * @throws DeviceException
	 */
	public abstract ArrayList<IMetadataEntry> getMetadataEntries() throws DeviceException;

	@Override
	public abstract String getName();

	@Override
	public abstract void setName(String name);

	/**
	 * Sets the the value of a named metadata entry, for further information on what type of metadata this method can be
	 * used for see {@link IMetadataEntry}
	 * 
	 * @see IMetadataEntry
	 * @param name
	 *            The name for which the metadata value is to be changed.
	 * @param metdataValue
	 *            The value required for the metadata entry.
	 * @throws DeviceException
	 */
	public void setMetadataValue(String name, String metdataValue) throws DeviceException;

	/**
	 * Returns the value of a named metadata entry.
	 * 
	 * @param name
	 *            The name of the required metadata entry.
	 * @return The value of the required metadata entry.
	 * @throws DeviceException
	 */
	public String getMetadataValue(String name) throws DeviceException;

	/**
	 * Gets the metadata value.
	 * 
	 * @param defaultValue
	 *            The default value for the fallback property i.e. it's value if the property is not set.
	 * @param name
	 *            The name of the required metadata entry.
	 * @param fallbackPropertyName
	 *            If set the value of this property will be used as a fallback.
	 * @return The metadata value
	 * @throws DeviceException
	 */
	public String getMetadataValue(String name, String fallbackPropertyName, String defaultValue)
			throws DeviceException;
}
