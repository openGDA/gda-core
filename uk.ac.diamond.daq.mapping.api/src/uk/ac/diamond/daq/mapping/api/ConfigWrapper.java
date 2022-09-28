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

package uk.ac.diamond.daq.mapping.api;

import java.util.Objects;

public class ConfigWrapper {

	private boolean active;
	private String name;
	private String malcolmDeviceName;
	private String appName;

	private String label = "";
	private Object configObject;

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMalcolmDeviceName() {
		return malcolmDeviceName;
	}

	public void setMalcolmDeviceName(String malcolmDeviceName) {
		this.malcolmDeviceName = malcolmDeviceName;
	}

	public Object getConfigObject() {
		return configObject;
	}

	public void setConfigObject(Object configObject) {
		this.configObject = configObject;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public boolean isSameProcessing(ConfigWrapper other) {

		//not the same if not complete
		if (appName == null || other.appName == null || configObject == null) {
			return false;
		}

		return appName.contentEquals(other.appName) && configObject.equals(other.configObject);
	}



	@Override
	public int hashCode() {
		return Objects.hash(active, appName, configObject, malcolmDeviceName, name, label);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ConfigWrapper other = (ConfigWrapper) obj;
		return active == other.active && Objects.equals(appName, other.appName)
				&& Objects.equals(configObject, other.configObject)
				&& Objects.equals(malcolmDeviceName, other.malcolmDeviceName) && Objects.equals(name, other.name)
				&& Objects.equals(label, other.label);
	}

	@Override
	public String toString() {
		return "ConfigWrapper [active=" + active + ", name=" + name + ", pathToConfig=" + configObject.toString()
				+ ", malcolmDeviceName=" + malcolmDeviceName + ", appName=" + appName + "]";
	}

	public String getConfigString() {
		return configObject.toString();
	}

	/**
	 * Overwrites config object with Path to config file
	 *
	 * @param conf
	 */
	public void setPathToConfig(String conf) {
		configObject = conf;

	}

	public String getPathToConfig() {
		return configObject.toString();
	}
}
