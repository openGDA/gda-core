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

import uk.ac.diamond.daq.osgi.OsgiService;

/**
 * A bean which holds information about the current axes used for mapping experiments. The intention is that a single
 * instance of this, created and initialised by Spring, will be a single point of reference for any parts of the system
 * that need to know or check the axis names.
 */
@OsgiService(MappingStageInfo.class)
@OsgiService(IStageScanConfiguration.class)
public class MappingStageInfo implements IStageScanConfiguration {

	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	private String defaultStreamSourceConfig = "";

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
	public final String getDefaultStreamSourceConfig() {
		return defaultStreamSourceConfig;
	}

	public final void setDefaultStreamSourceConfig(String defaultStreamSourceConfig) {
		this.defaultStreamSourceConfig = defaultStreamSourceConfig;
	}

	@Override
	public String toString() {
		return "MappingStageInfo [activeFastScanAxis=" + activeFastScanAxis + ", activeSlowScanAxis="
				+ activeSlowScanAxis + ", associatedAxis=" + associatedAxis + ", beamSize=" + beamSize + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((activeFastScanAxis == null) ? 0 : activeFastScanAxis.hashCode());
		result = prime * result + ((activeSlowScanAxis == null) ? 0 : activeSlowScanAxis.hashCode());
		result = prime * result + ((associatedAxis == null) ? 0 : associatedAxis.hashCode());
		long temp;
		temp = Double.doubleToLongBits(beamSize);
		result = prime * result + (int) (temp ^ (temp >>> 32));
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
		MappingStageInfo other = (MappingStageInfo) obj;
		if (activeFastScanAxis == null) {
			if (other.activeFastScanAxis != null)
				return false;
		} else if (!activeFastScanAxis.equals(other.activeFastScanAxis))
			return false;
		if (activeSlowScanAxis == null) {
			if (other.activeSlowScanAxis != null)
				return false;
		} else if (!activeSlowScanAxis.equals(other.activeSlowScanAxis))
			return false;
		if (associatedAxis == null) {
			if (other.associatedAxis != null)
				return false;
		} else if (!associatedAxis.equals(other.associatedAxis))
			return false;
		return Double.doubleToLongBits(beamSize) == Double.doubleToLongBits(other.beamSize);
	}

}
