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

public class SESExcitationEnergySource implements Serializable {

	private static final long serialVersionUID = -8582867486771114349L;
	private String name;
	private double value;

	private final transient PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

	public SESExcitationEnergySource() {

	}

	public SESExcitationEnergySource(String name, double value) {
		this.name = name;
		this.value = value;
	}

	public SESExcitationEnergySource(SESExcitationEnergySource e) {
		this.name = e.getName();
		this.value = e.getValue();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		String oldValue = this.name;
		this.name = name;
		propertyChangeSupport.firePropertyChange("name", oldValue, name);
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		double oldValue = this.value;
		this.value = value;
		propertyChangeSupport.firePropertyChange("value", oldValue, value);
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		this.propertyChangeSupport.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		this.propertyChangeSupport.removePropertyChangeListener(listener);
	}

	@Override
	public String toString() {
		return "SESExcitationEnergySource [name=" + name + ", value=" + value + "]";
	}

}