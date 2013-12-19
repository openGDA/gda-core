/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package uk.ac.gda.common.rcp.jface.viewers;

import java.util.Set;

import org.eclipse.core.databinding.observable.map.IMapChangeListener;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.observable.map.MapChangeEvent;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.jface.viewers.OwnerDrawLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;

/**
 * A OwnerDrawLabelProvider extended to listen to MapChangeEvent events from an IObservableMap
 * 
 * Derivations should implement erase, paint and measure
 * To get the new value of the item whose change led to the MapChangeEvent from the element passed to
 * these functions use the construct:
 * 
 * Object value = attributeMaps[0].get(element);
 * 
 */
public abstract class ObservableMapOwnerDrawProvider extends OwnerDrawLabelProvider {


	/**
	 * Observable maps typically mapping from viewer elements to label values.
	 * Subclasses may use these maps to provide custom labels.
	 * 
	 * @since 1.4
	 */
	protected IObservableMap[] attributeMaps;

	private IMapChangeListener mapChangeListener = new IMapChangeListener() {
		@Override
		public void handleMapChange(MapChangeEvent event) {
			@SuppressWarnings("rawtypes")
			Set affectedElements = event.diff.getChangedKeys();
			LabelProviderChangedEvent newEvent = new LabelProviderChangedEvent(
					ObservableMapOwnerDrawProvider.this, affectedElements
							.toArray());
			fireLabelProviderChanged(newEvent);
		}
	};

	/**
	 * Creates a new label provider that tracks changes to one attribute.
	 * 
	 * @param attributeMap
	 */
	public ObservableMapOwnerDrawProvider(IObservableMap attributeMap) {
		this(new IObservableMap[] { attributeMap });
	}

	/**
	 * Creates a new label provider that tracks changes to more than one
	 * attribute. This constructor should be used by subclasses that override
	 * {@link #update(ViewerCell)} and make use of more than one attribute.
	 * 
	 * @param attributeMaps
	 */
	protected ObservableMapOwnerDrawProvider(IObservableMap[] attributeMaps) {
		System.arraycopy(attributeMaps, 0,
				this.attributeMaps = new IObservableMap[attributeMaps.length],
				0, attributeMaps.length);
		for (int i = 0; i < attributeMaps.length; i++) {
			attributeMaps[i].addMapChangeListener(mapChangeListener);
		}
	}

	@Override
	public void dispose() {
		for (int i = 0; i < attributeMaps.length; i++) {
			attributeMaps[i].removeMapChangeListener(mapChangeListener);
		}
		super.dispose();
		this.attributeMaps = null;
		this.mapChangeListener = null;
	}

}
