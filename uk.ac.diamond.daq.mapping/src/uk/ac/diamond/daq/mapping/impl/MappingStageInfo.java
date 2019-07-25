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

	private String plotXAxisName;
	private String plotYAxisName;
	private String associatedAxis;
	private String beamSize;

	public void merge(MappingStageInfo other) {
		setPlotXAxisName(other.getPlotXAxisName());
		setPlotYAxisName(other.getPlotYAxisName());
		setAssociatedAxis(other.getAssociatedAxis());
		setBeamSize(other.getBeamSize());
	}

	@Override
	public String getPlotXAxisName() {
		return plotXAxisName;
	}

	public void setPlotXAxisName(String newValue) {
		String oldValue = this.plotXAxisName;
		this.plotXAxisName = newValue;
		this.pcs.firePropertyChange("plotXAxisName", oldValue, newValue);
	}

	@Override
	public String getPlotYAxisName() {
		return plotYAxisName;
	}

	public void setPlotYAxisName(String newValue) {
		String oldValue = this.plotYAxisName;
		this.plotYAxisName = newValue;
		this.pcs.firePropertyChange("plotYAxisName", oldValue, newValue);
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

	public String getBeamSize() {
		return beamSize;
	}

	public void setBeamSize(String newValue) {
		String oldValue = this.beamSize;
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
		return "MappingStageInfo [plotXAxisName=" + plotXAxisName + ", plotYAxisName="
				+ plotYAxisName + ", associatedAxis=" + associatedAxis + ", beamSize=" + beamSize + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((plotXAxisName == null) ? 0 : plotXAxisName.hashCode());
		result = prime * result + ((plotYAxisName == null) ? 0 : plotYAxisName.hashCode());
		result = prime * result + ((associatedAxis == null) ? 0 : associatedAxis.hashCode());
		result = prime * result + ((beamSize == null) ? 0 : beamSize.hashCode());
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
		if (plotXAxisName == null) {
			if (other.plotXAxisName != null)
				return false;
		} else if (!plotXAxisName.equals(other.plotXAxisName))
			return false;
		if (plotYAxisName == null) {
			if (other.plotYAxisName != null)
				return false;
		} else if (!plotYAxisName.equals(other.plotYAxisName))
			return false;
		if (associatedAxis == null) {
			if (other.associatedAxis != null)
				return false;
		} else if (!associatedAxis.equals(other.associatedAxis))
			return false;
		if (beamSize == null) {
			if (other.beamSize != null)
				return false;
		} else if (!beamSize.equals(other.beamSize))
			return false;
		return true;
	}

}
