/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.devices.specs.phoibos.api;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A class for holding a list of {@link SpecsPhoibosRegion}'s. It has property change support for use in dynamic GUIs.
 * The sequence listens to events in each region to pass those through.
 * <p>
 * It implements {@link Serializable} to allow sequences to be passed over RMI.
 *
 * @author James Mudd
 */
public class SpecsPhoibosSequence implements Serializable {

	/**
	 * Generated serial ID
	 */
	private static final long serialVersionUID = -4374428045332451738L;

	private final List<SpecsPhoibosRegion> regions = new LinkedList<>();
	private static final String REGIONS_NAME = "regions";

	private final transient PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	// The sequence listens to events in each region to pass those through
	private final transient PropertyChangeListener regionListener = pcs::firePropertyChange;

	public SpecsPhoibosSequence() {
		// No-arg constructor
	}

	/**
	 * Copy constructor for creating a new sequence from an existing one
	 *
	 * @param sequence To be copied
	 */
	public SpecsPhoibosSequence(SpecsPhoibosSequence sequence) {
		for (SpecsPhoibosRegion region : sequence.getRegions()) {
			// Call the copy constructor for each region then add to this sequence
			addRegion(new SpecsPhoibosRegion(region));
		}
		// Setup the listeners
		updateRegionListeners();
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		this.pcs.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		this.pcs.removePropertyChangeListener(listener);
	}

	/**
	 * Gets all regions in the sequence
	 *
	 * @see #getEnabledRegions()
	 * @return List of all regions includes disabled
	 */
	public List<SpecsPhoibosRegion> getRegions() {
		return this.regions;
	}

	/**
	 * Get the regions in the sequence that are enabled
	 *
	 * @see #getRegions()
	 * @return List of only the enabled regions
	 */
	public List<SpecsPhoibosRegion> getEnabledRegions() {
		return regions.stream().filter(SpecsPhoibosRegion::isEnabled).collect(Collectors.toList());
	}

	public void setRegions(final List<SpecsPhoibosRegion> regions) {
		List<SpecsPhoibosRegion> oldValue = new LinkedList<>(regions);
		this.regions.clear();
		this.regions.addAll(regions);
		for (SpecsPhoibosRegion region : this.regions) {
			region.addPropertyChangeListener(regionListener);
		}
		pcs.firePropertyChange(REGIONS_NAME, oldValue, this.regions);
	}

	/**
	 * Adds a region to the sequence.
	 *
	 * @param region
	 *            The region to add
	 * @throws IllegalArgumentException
	 *             If the sequence already contains a region with the same name as the region being added
	 */
	public void addRegion(SpecsPhoibosRegion region) {
		// Check region name is not already in the sequence
		if (getRegionNames().contains(region.getName())) {
			throw new IllegalArgumentException(
					"The sequence already contains a region with the name: " + region.getName());
		}

		List<SpecsPhoibosRegion> oldValue = new LinkedList<>(regions);
		// TODO Validate region?
		regions.add(region);
		// Add PCS for that region
		region.addPropertyChangeListener(regionListener);
		pcs.firePropertyChange(REGIONS_NAME, oldValue, regions);
	}

	public void removeRegion(SpecsPhoibosRegion region) {
		List<SpecsPhoibosRegion> oldValue = new LinkedList<>(regions);
		region.removePropertyChangeListener(regionListener);
		regions.remove(region);
		pcs.firePropertyChange(REGIONS_NAME, oldValue, regions);
	}

	@Override
	public String toString() {
		return "SpecsPhoibosSequence [regions=" + regions + "]";
	}

	/**
	 * Gets the names of all the regions in the sequence.
	 *
	 * @return List of the region names in the sequence
	 */
	public List<String> getRegionNames() {
		List<String> names = new LinkedList<>();
		for (SpecsPhoibosRegion region : regions) {
			names.add(region.getName());
		}
		return names;
	}

	/**
	 * This can be called to ensure that the sequence has one listener per region it contains. This is required after
	 * loading a sequence from XML because the XMLDecoder calls {@link #getRegions()} than then adds the regions to the
	 * list.
	 */
	public void updateRegionListeners() {
		for (SpecsPhoibosRegion specsPhoibosRegion : regions) {
			// If there already is a listener remove it. If there is not nothing will happen. Preventing multiple region
			// listeners
			specsPhoibosRegion.removePropertyChangeListener(regionListener);
			// Add the region listener again for this region
			specsPhoibosRegion.addPropertyChangeListener(regionListener);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((regions == null) ? 0 : regions.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SpecsPhoibosSequence other = (SpecsPhoibosSequence) obj;
		if (regions == null) {
			if (other.regions != null)
				return false;
		} else if (!regions.equals(other.regions))
			return false;
		return true;
	}

}
