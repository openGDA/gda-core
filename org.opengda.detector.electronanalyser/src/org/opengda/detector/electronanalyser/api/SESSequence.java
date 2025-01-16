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

package org.opengda.detector.electronanalyser.api;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.security.InvalidParameterException;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;

import uk.ac.diamond.osgi.services.ServiceProvider;

@JsonIgnoreProperties({"spectrum", "confirmAfterEachIteration", "filename", "numIterations", "repeatUntilStopped", "runModeIndex"})
public class SESSequence implements Serializable, ICopy, Cloneable{
	/**
	 * Generated serial ID
	 */
	private static final long serialVersionUID = -4374428045332451738L;

	private String elementSet = "UNKNOWN";

	@JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
	private final List<SESExcitationEnergySource> excitationEnergySources= new LinkedList<>();

	@JsonAlias({ "regions", "region"})
	@JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
	private final List<SESRegion> regions = new LinkedList<>();

	public static final String REGIONS = "regions";

	private final transient PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
	// The sequence listens to events in each region to pass those through
	private final transient PropertyChangeListener regionListener = propertyChangeSupport::firePropertyChange;

	public SESSequence() {
		//Create default single regions
		addRegion(new SESRegion());
		final SESSettingsService settings = ServiceProvider.getService(SESSettingsService.class);
		setExcitationEnergySource(settings.getSESExcitationEnergySourceList());
	}

	/**
	 * Copy constructor for creating a new sequence from an existing one
	 *
	 * @param sequence To be copied
	 */
	public SESSequence(SESSequence sequence) {
		copy(sequence);
	}

	@Override
	public void copy(ICopy toCopy) {
		if (!(toCopy instanceof SESSequence)) {
			throw new InvalidParameterException("Must be instanceof SESSequence");
		}
		final SESSequence sequence = (SESSequence) toCopy;
		setRegions(
			sequence.getRegions().stream()
			.map(SESRegion::new)
			.collect(Collectors.toList())
		);
		setExcitationEnergySource(
			sequence.getExcitationEnergySources().stream()
			.map(SESExcitationEnergySource::new)
			.collect(Collectors.toList())
		);
		setElementSet(sequence.getElementSet());
	}

	@Override
	public SESSequence clone() {
		return new SESSequence(this);
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		this.propertyChangeSupport.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		this.propertyChangeSupport.removePropertyChangeListener(listener);
	}

	public String getElementSet() {
		return elementSet;
	}

	public void setElementSet(String elementSet) {
		String oldValue = this.elementSet;
		this.elementSet = elementSet;
		propertyChangeSupport.firePropertyChange("elementSet", oldValue, elementSet);
	}

	/**
	 * Gets all regions in the sequence
	 *
	 * @see #getEnabledRegions()
	 * @return List of all regions includes disabled
	 */
	public List<SESRegion> getRegions() {
		return this.regions;
	}

	/**
	 * Get the regions in the sequence that are enabled
	 *
	 * @see #getRegions()
	 * @return List of only the enabled regions
	 */
	@JsonIgnore
	public List<SESRegion> getEnabledRegions() {
		return regions.stream().filter(SESRegion::isEnabled).collect(Collectors.toList());
	}

	 @JsonSetter
	public void setRegions(List<SESRegion> newRegions ) {
		List<SESRegion> oldValue = new LinkedList<>(regions);
		oldValue.stream().forEach(r -> r.removePropertyChangeListener(regionListener));
		clearRegions();
		this.regions.addAll(newRegions);
		getRegions().stream().forEach(r -> r.addPropertyChangeListener(regionListener));
		propertyChangeSupport.firePropertyChange(REGIONS, oldValue, this.regions);
	}

	private void clearRegions() {
		getRegions().stream().forEach(r -> r.removePropertyChangeListener(regionListener));
		regions.clear();
	}

	private void clearExcitationEnergySources() {
		getExcitationEnergySources().stream().forEach(e -> e.removePropertyChangeListener(regionListener));
		excitationEnergySources.clear();
	}

	/**
	 * Adds a region to the sequence.
	 *
	 * @param region
	 *            The region to add
	 * @throws IllegalArgumentException
	 *             If the sequence already contains a region with the same name as the region being added
	 */
	public void addRegion(SESRegion region) {
		// Check region name is not already in the sequence
		if (getRegionNames().contains(region.getName())) {
			throw new IllegalArgumentException("The sequence already contains a region with the name: " + region.getName());
		}
		final List<SESRegion> oldValue = new LinkedList<>(regions);
		regions.add(region);
		region.addPropertyChangeListener(regionListener);
		propertyChangeSupport.firePropertyChange(REGIONS, oldValue, regions);
	}

	public void removeRegion(SESRegion region) {
		List<SESRegion> oldValue = new LinkedList<>(regions);
		region.removePropertyChangeListener(regionListener);
		regions.remove(region);
		propertyChangeSupport.firePropertyChange(REGIONS, oldValue, regions);
	}

	public List<SESExcitationEnergySource> getExcitationEnergySources() {
		return excitationEnergySources;
	}

	@JsonSetter
	public void setExcitationEnergySource(List<SESExcitationEnergySource> newExcitationEnergySources) {
		final List<SESExcitationEnergySource> oldValue = new LinkedList<>(excitationEnergySources);
		oldValue.stream().forEach(e -> e.removePropertyChangeListener(regionListener));
		clearExcitationEnergySources();
		this.excitationEnergySources.addAll(newExcitationEnergySources);
		this.excitationEnergySources.stream().forEach(e -> e.addPropertyChangeListener(regionListener));
		propertyChangeSupport.firePropertyChange("excitationEnergySources", oldValue, this.excitationEnergySources);
	}

	/**
	 * @param excitationEnergySource to add to the sequence
	 * @throws IllegalArgumentException if the sequence already contains a excitationEnergySource with the same name as the excitationEnergySource being added
	 */
	public void addExcitationEnergySource(SESExcitationEnergySource excitationEnergySource) {
		// Check region name is not already in the sequence
		if (getExcitationEnergySourceNames().contains(excitationEnergySource.getName())) {
			throw new IllegalArgumentException("The sequence already contains a excitationEnergySource with the name: " + excitationEnergySource.getName());
		}
		excitationEnergySources.add(excitationEnergySource);
	}

	public void removeExcitationEnergySource(SESExcitationEnergySource excitationEnergySource) {
		excitationEnergySources.remove(excitationEnergySource);
	}

	@JsonIgnore
	public SESExcitationEnergySource getExcitationEnergySourceByName(String name) {
		final List<String> names = getExcitationEnergySources().stream().map(SESExcitationEnergySource::getName).toList();
		final int index = names.indexOf(name);
		return index == -1 ? null : getExcitationEnergySources().get(index);
	}

	@JsonIgnore
	public SESExcitationEnergySource getExcitationEnergySourceByScannableName(String scannableName) {
		final List<String> scannableNames = getExcitationEnergySources().stream().map(SESExcitationEnergySource::getScannableName).toList();
		final int index = scannableNames.indexOf(scannableName);
		return index == -1 ? null : getExcitationEnergySources().get(index);
	}

	@JsonIgnore
	public SESExcitationEnergySource getExcitationEnergySourceByRegion(SESRegion region) {
		return getExcitationEnergySourceByName(region.getExcitationEnergySource());
	}

	@JsonIgnore
	public SESRegion getRegionByName(String regionName) {
		final List<String> names = getRegions().stream().map(SESRegion::getName).toList();
		final int index = names.indexOf(regionName);
		return getRegions().get(index);
	}

	@Override
	public int hashCode() {
		return Objects.hash(elementSet, excitationEnergySources, regions);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SESSequence other = (SESSequence) obj;
		return Objects.equals(elementSet, other.elementSet) && Objects.equals(excitationEnergySources, other.excitationEnergySources)
				&& Objects.equals(regions, other.regions);
	}

	@Override
	public String toString() {
		return "SESSequence [elementSet=" + elementSet + ", excitationEnergySources=" + excitationEnergySources + ", regions=" + regions + "]";
	}

	/**
	 * Gets the names of all the regions in the sequence.
	 *
	 * @return List of the region names in the sequence
	 */
	@JsonIgnore
	public List<String> getRegionNames() {
		return getRegions().stream().map(SESRegion::getName).toList();
	}

	/**
	 * Gets the names of all the regions in the sequence.
	 *
	 * @return List of the region names in the sequence
	 */
	@JsonIgnore
	public List<String> getExcitationEnergySourceNames() {
		return getExcitationEnergySources().stream().map(SESExcitationEnergySource::getName).toList();
	}

}