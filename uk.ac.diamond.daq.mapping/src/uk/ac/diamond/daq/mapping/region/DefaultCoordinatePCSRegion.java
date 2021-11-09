/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.region;

import static uk.ac.diamond.daq.mapping.api.constants.RegionConstants.CALC_POINTS;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Base class to handle property change support for mapping regions. When each coordinate value is changed, the pcs for it
 * should be triggered, but a point recalculation should only be triggered once for any change to a set of coordinates e.g.
 * when dragging the centre of a region. The {@link #coordinates} member is used to hold the labels and values of the shape's
 * coordinates and must be initialised by the sub class constructor calling {@code super(...)}.
 * The {@link #updatePropertyValuesAndFire} method then iterates over these for the required identifiers that are passed in
 * updating them, after which the path calculation is triggered.
 */
public class DefaultCoordinatePCSRegion {

	private PropertyChangeSupport pcs = new PropertyChangeSupport(this);

	protected final Map<String, Double> coordinates = new HashMap<>();

	/**
	 * This constructor must be invoked by the base class to provide a {@link Map} of coordinate names to initial values. This is
	 * then used for all subsequent mutations of the values. We must copy values into our {@link HashMap} rather than set it to the
	 * incoming one in case the sub class uses {@code Map.of} to supply the parameter, which yields an unmodifiable map.
	 *
	 * @param coordinates	The {@link Map} of value identifiers to their initial values
	 */
	protected DefaultCoordinatePCSRegion(Map<String, Double> coordinates) {
		this.coordinates.putAll(coordinates);
	}

	private void handleBadCoordinatesMap() {
		if (coordinates.isEmpty()) {
			throw new IllegalStateException(
					"The coordinates Map is null or empty, your Mapping region has not been initialised properly - please specify it in the constructor");
		}
	}

	/**
	 * Switches the value of the identified entry in the {@link #coordinates} map with the supplied value and returns the old one
	 *
	 * @param id			The id of the value to update
	 * @param newValue		The value to overwrite the current value with
	 * @return				The value before the overwrite takes place
	 */
	private double switchValue(String id, double newValue) {
		double oldValue = coordinates.get(id);
		coordinates.put(id, newValue);
		return oldValue;
	}

	/**
	 * Triggers the {@link #switchValue} function for the supplied property and fires the corresponding property change support event
	 *
	 * @param entry		{@link Map.Entry} containing the value identifier and its associated new value
	 */
	private void setValueAndFire (Map.Entry<String, Double> entry) {
		double oldValue = switchValue(entry.getKey(), entry.getValue());
		pcs.firePropertyChange(entry.getKey(), oldValue, entry.getValue());
	}

	/**
	 * Iterates over the supplied coordinate identifier properties calling the value update function trigger with the supplied new value.
	 * Once this is done for all requested properties, the points re-calculation event is fired
	 *
	 * @param propertyValuePairs	{@link Map} of ordinate identifier to the new values to set the ordinate to
	 */
	protected void updatePropertyValuesAndFire(Map<String, Double> propertyValuePairs) {
		handleBadCoordinatesMap();
		propertyValuePairs.entrySet().forEach(this::setValueAndFire);
		pcs.firePropertyChange(CALC_POINTS, 0, 1);
	}

	/**
	 * Iterates over the supplied properties {@link Map}, which may contain entries with keys that are not related to coordinate setting.
	 * To handle this the properties are filtered in a stream, passing in those that match to {@link #setValueAndFire} with the appropriate
	 * call to {@code map} to cast the value. After this the points calculation event is fired.
	 *
	 * @param properties			{@link Map} of property names to property objects
	 */
	protected void updateAndFireFromPropertiesMap(Map<String, Object> properties) {
		handleBadCoordinatesMap();
		properties.entrySet().stream()
			.filter(entry -> coordinates.keySet().contains(entry.getKey()))
			.map (entry -> new AbstractMap.SimpleEntry<String, Double>(entry.getKey(), (double)entry.getValue()))
			.forEach(this::setValueAndFire);
		pcs.firePropertyChange(CALC_POINTS, 0, 1);
	}

	/**
	 * To allow for the odd case where the property to be updated isn't a double and so needs to be triggered by the sub class
	 *
     * @param propertyName  the programmatic name of the property that was changed
     * @param oldValue      the old value of the property
     * @param newValue      the new value of the property
	 */
	protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
		 pcs.firePropertyChange(propertyName, oldValue, newValue);
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		pcs.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		pcs.removePropertyChangeListener(listener);
	}

	public void usePCS(PropertyChangeSupport pcs) {
		this.pcs = pcs;
	}
}
