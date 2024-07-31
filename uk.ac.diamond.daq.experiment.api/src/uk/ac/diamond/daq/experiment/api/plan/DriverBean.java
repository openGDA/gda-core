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

package uk.ac.diamond.daq.experiment.api.plan;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;

public class DriverBean implements Serializable {

	private static final long serialVersionUID = 8840080167686307649L;

	public static final String DRIVER_PROPERTY = "driver";
	public static final String PROFILE_PROPERTY = "profile";

	private final PropertyChangeSupport pcs;

	public DriverBean() {
		pcs = new PropertyChangeSupport(this);
	}

	private String driver;
	private String profile;

	public String getDriver() {
		return driver;
	}

	public void setDriver(String driver) {
		var old = this.driver;
		this.driver = driver;
		pcs.firePropertyChange(DRIVER_PROPERTY, old, driver);
	}

	public String getProfile() {
		return profile;
	}

	public void setProfile(String profile) {
		var old = this.profile;
		this.profile = profile;
		pcs.firePropertyChange(PROFILE_PROPERTY, old, profile);
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		this.pcs.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		this.pcs.removePropertyChangeListener(listener);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((driver == null) ? 0 : driver.hashCode());
		result = prime * result + ((profile == null) ? 0 : profile.hashCode());
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
		DriverBean other = (DriverBean) obj;
		if (driver == null) {
			if (other.driver != null)
				return false;
		} else if (!driver.equals(other.driver)) {
			return false;
		}
		if (profile == null) {
			if (other.profile != null)
				return false;
		} else if (!profile.equals(other.profile)) {
			return false;
		}
		return true;
	}

}
