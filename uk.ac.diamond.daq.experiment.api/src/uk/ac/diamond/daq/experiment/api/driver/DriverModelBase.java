/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.experiment.api.driver;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;

/**
 * This base class is essentially a {@link Serializable} version of FindableBase with {@link PropertyChangeSupport}
 */
public abstract class DriverModelBase implements DriverModel {

	private static final long serialVersionUID = -5230839380845124567L;
	private String name;

	final PropertyChangeSupport pcs;

	public DriverModelBase() {
		pcs = new PropertyChangeSupport(this);
	}

	@Override
	public String getLabel() {
		return getName();
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		String oldName = getName();
		this.name = name;
		pcs.firePropertyChange("name", oldName, name);
	}

	@Override
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		pcs.addPropertyChangeListener(listener);
	}

	@Override
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		pcs.removePropertyChangeListener(listener);
	}

}
