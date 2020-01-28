/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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
import java.util.Objects;

public class SpecsPhoibosScannableValue implements Serializable {

	private String scannableName;
	private double scannableValue;
	private boolean enabled;

	private final transient PropertyChangeSupport pcs = new PropertyChangeSupport(this);

	/*
	 * Zero-argument constructor is required for correct deserialization from JSON.
	 * Without it, the PropertyChangeSupport does not get created.
	 */
    @SuppressWarnings("unused")
	private SpecsPhoibosScannableValue() {}

	public SpecsPhoibosScannableValue(String scannableName) {
		this.scannableName = scannableName;
	}

	public SpecsPhoibosScannableValue(SpecsPhoibosScannableValue scannableValue) {
		this(scannableValue.getScannableName());
		this.enabled = scannableValue.enabled;
		this.scannableValue = scannableValue.scannableValue;
	}

	public String getScannableName() {
		return scannableName;
	}

	public double getScannableValue() {
		return scannableValue;
	}

	public void setScannableValue(double scannableValue) {
		double oldValue = this.scannableValue;
		this.scannableValue = scannableValue;
		pcs.firePropertyChange("value", oldValue, scannableValue);
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		boolean oldValue = this.enabled;
		this.enabled = enabled;
		pcs.firePropertyChange("enabled", oldValue, enabled);
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		this.pcs.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		this.pcs.removePropertyChangeListener(listener);
	}

	@Override
	public int hashCode() {
		return Objects.hash(scannableName, enabled, scannableValue);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (this == obj) return true;
		if (getClass() != obj.getClass()) return false;

		SpecsPhoibosScannableValue otherScannableValue = (SpecsPhoibosScannableValue)obj;

		return scannableName.equals(otherScannableValue.getScannableName()) &&
				enabled == otherScannableValue.isEnabled() &&
				scannableValue == otherScannableValue.getScannableValue();
	}
}
