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

package uk.ac.diamond.daq.mapping.impl;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.eclipse.scanning.api.annotation.UiTooltip;
import org.eclipse.scanning.api.ui.IStageScanConfiguration;

/**
 * A bean which holds information about the current axes used for mapping experiments. The intention is that a single
 * instance of this, created and initialised by Spring, will be a single point of reference for any parts of the system
 * that need to know or check the axis names.
 */
public class MappingStageInfo implements IStageScanConfiguration {

	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

	@Override
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		this.pcs.addPropertyChangeListener(listener);
	}

	@Override
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		this.pcs.removePropertyChangeListener(listener);
	}

	private String activeFastScanAxis;
	private String activeSlowScanAxis;
	private String associatedAxis;
	private double beamSize;

	public void merge(MappingStageInfo other) {
		setActiveFastScanAxis(other.getActiveFastScanAxis());
		setActiveSlowScanAxis(other.getActiveSlowScanAxis());
		setAssociatedAxis(other.getAssociatedAxis());
		setBeamSize(other.getBeamSize());
	}

	@Override
	public String getActiveFastScanAxis() {
		return activeFastScanAxis;
	}

	public void setActiveFastScanAxis(String newValue) {
		String oldValue = this.activeFastScanAxis;
		this.activeFastScanAxis = newValue;
		this.pcs.firePropertyChange("activeFastScanAxis", oldValue, newValue);
	}

	@Override
	public String getActiveSlowScanAxis() {
		return activeSlowScanAxis;
	}

	public void setActiveSlowScanAxis(String newValue) {
		String oldValue = this.activeSlowScanAxis;
		this.activeSlowScanAxis = newValue;
		this.pcs.firePropertyChange("activeSlowScanAxis", oldValue, newValue);
	}

	@Override
	public String getAssociatedAxis() {
		return associatedAxis;
	}

	public void setAssociatedAxis(String newValue) {
		String oldValue = this.associatedAxis;
		this.associatedAxis = newValue;
		this.pcs.firePropertyChange("associatedAxis", oldValue, newValue);
	}

	@UiTooltip("The beam size to use for the beam position marker on the plot, in the same units as the scan axes")
	public double getBeamSize() {
		return beamSize;
	}

	public void setBeamSize(double newValue) {
		double oldValue = this.beamSize;
		this.beamSize = newValue;
		this.pcs.firePropertyChange("beamSize", oldValue, newValue);
	}

	@Override
	public String toString() {
		return "MappingStageInfo [activeFastScanAxis=" + activeFastScanAxis + ", activeSlowScanAxis="
				+ activeSlowScanAxis + ", associatedAxis=" + associatedAxis + ", beamSize=" + beamSize + "]";
	}
	
}
