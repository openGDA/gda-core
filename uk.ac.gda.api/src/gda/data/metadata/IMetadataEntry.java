/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

import gda.factory.Configurable;
import gda.factory.Findable;
import gda.observable.IObservable;

import java.io.Serializable;

public interface IMetadataEntry extends Findable, IObservable, Configurable, Serializable {

	/**
	 * Returns the current value of the object. The method of retrieving this
	 * value will vary depending on the type of this piece of metadata.
	 * 
	 * @return The value of this metadata entry.
	 */
	public String getMetadataValue();

	/**
	 * Sets the name of this metadata entry.
	 * 
	 * @param name
	 *            The name of the metadata entry.
	 */
	@Override
	public void setName(String name);

	/**
	 * Gets the name of this metadata entry.
	 * 
	 * @return The name of this metadata entry.
	 */
	@Override
	public String getName();

	/**
	 * @return defEntryName
	 */
	public String getDefEntryName();

	/**
	 * @param defEntryName
	 */
	public void setDefEntryName(String defEntryName);

	/**
	 * Where appropriate, sets the value of the metadata.
	 * 
	 * @param metadataValue
	 * @throws Exception
	 */
	public void setValue(String metadataValue) throws Exception;

	/**
	 * @return true - if this instance of IMetadataEntry can persist its value,
	 *         so the storeValue method will try to do something
	 */
	public boolean canStoreValue();

}