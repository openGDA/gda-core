/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

package gda.function;

import gda.device.DeviceException;
import gda.factory.Findable;
import gda.observable.IObservable;

import java.util.ArrayList;

/**
 * This interface defines generic lookup functions for looking up the value for a given object that corresponding to a
 * specific key. It is designed to support multiple objects for the same given key at the same time using <a
 * href=http://
 * commons.apache.org/collections/api-3.2/org/apache/commons/collections/map/MultiValueMap.html>MultiValueMap</a>. It
 * should support columnar data table with object name as column header and the key at left-most column.
 */
public interface Lookup extends Findable, IObservable {

	/**
	 * returns an arrayList of scannable names in the lookup table.
	 * 
	 * @return ArrayList of scannable names
	 */
	public abstract ArrayList<String> getScannableNames() throws DeviceException;

	/**
	 * lookup and returns the value corresponding to the specified key (row name), such as energy, and column name, such
	 * as scannable name
	 * 
	 * @param energy
	 * @param scannableName
	 * @return value for the named scannable
	 */
	public abstract double lookupValue(Object energy, String scannableName) throws DeviceException;

	/**
	 * Returns the physical unit for a particular scannable name which its value is in
	 * 
	 * @param scannableName
	 * @return the Unit
	 */
	public abstract String lookupUnitString(String scannableName)throws DeviceException;

	/**
	 * returns the number of decimal places of the value for a given scannable name.
	 * 
	 * @param scannableName
	 * @return decimal places
	 * @throws DeviceException 
	 */
	public abstract int lookupDecimalPlaces(String scannableName) throws DeviceException;

	/**
	 * Returns the number of value rows in the table.
	 * 
	 * @return the number of x values
	 */
	public abstract int getNumberOfRows()  throws DeviceException;

	/**
	 * Sets the lookup table data filename
	 * 
	 * @param filename
	 *            the filename
	 */
	public abstract void setFilename(String filename);

	/**
	 * Returns the lookup table (data) filename
	 * 
	 * @return the filename
	 */
	public abstract String getFilename();

	/**
	 * @return the values in the first column. The implementation ensures that the method does not include the headers
	 *         but just the values in the first column. This may be useful to find a specific key or interpolate if
	 *         necessary.
	 */
	double[] getLookupKeys() throws DeviceException;

	/**
	 * Reloads the lookuptable, i.e reads the file provided and reloads the map values
	 */
	void reload();

}