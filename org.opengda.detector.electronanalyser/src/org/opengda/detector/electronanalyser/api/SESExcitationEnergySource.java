/*-
 * Copyright © 2024 Diamond Light Source Ltd.
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

import com.fasterxml.jackson.annotation.JsonIgnore;

import gda.device.Scannable;
import gda.factory.Finder;

public class SESExcitationEnergySource implements Serializable {

	private static final long serialVersionUID = -8582867486771114349L;
	private String name;
	private String scannableName;
	private double value;

	private transient Scannable scannable;
	private transient PropertyChangeSupport propertyChangeSupport;

	public SESExcitationEnergySource() {}

	public SESExcitationEnergySource(String name, String scannableName) {
		this.name = name;
		this.scannableName = scannableName;
	}

	public SESExcitationEnergySource(String name, String scannableName, double value) {
		this.name = name;
		this.scannableName = scannableName;
		this.value = value;
	}

	public SESExcitationEnergySource(SESExcitationEnergySource e) {
		this.name = e.getName();
		this.scannableName = e.getScannableName();
		this.value = e.getValue();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		String oldValue = this.name;
		this.name = name;
		this.getPropertyChangeSupport().firePropertyChange("name", oldValue, name);
	}

	public String getScannableName() {
		return scannableName;
	}

	public void setScannableName(String scannableName) {
		String oldValue = this.scannableName;
		this.scannableName = scannableName;
		this.scannable = null;
		this.getPropertyChangeSupport().firePropertyChange("scannableName", oldValue, name);
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		double oldValue = this.value;
		this.value = value;
		this.getPropertyChangeSupport().firePropertyChange("value", oldValue, value);
	}

	@JsonIgnore
	public Scannable getScannable() {
		if (scannable == null) {
			scannable = Finder.find(getScannableName());
		}
		return scannable;
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		this.getPropertyChangeSupport().addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		this.getPropertyChangeSupport().removePropertyChangeListener(listener);
	}

	private PropertyChangeSupport getPropertyChangeSupport() {
		if (this.propertyChangeSupport == null) {
			this.propertyChangeSupport = new PropertyChangeSupport(this);
		}
		return this.propertyChangeSupport;
	}

	@Override
	public String toString() {
		return "SESExcitationEnergySource [name=" + name + ", scannableName=" + scannableName + ", value=" + value + "]";
	}
}