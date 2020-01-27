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

public class ConfigWrapper {

	private boolean active;
	private String name;
	private String pathToConfig;
	private String malcolmDeviceName;
	private String appName;

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

	public String getPathToConfig() {
		return pathToConfig;
	}

	public void setPathToConfig(String pathToConfig) {
		this.pathToConfig = pathToConfig;
	}

	public boolean isSameProcessing(ConfigWrapper other) {

		//not the same if not complete
		if (appName == null || other.appName == null || pathToConfig == null || other.pathToConfig == null) {
			return false;
		}

		return appName.contentEquals(other.appName) && pathToConfig.equals(other.pathToConfig);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (active ? 1231 : 1237);
		result = prime * result + ((appName == null) ? 0 : appName.hashCode());
		result = prime * result + ((malcolmDeviceName == null) ? 0 : malcolmDeviceName.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((pathToConfig == null) ? 0 : pathToConfig.hashCode());
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
		ConfigWrapper other = (ConfigWrapper) obj;
		if (active != other.active)
			return false;
		if (appName == null) {
			if (other.appName != null)
				return false;
		} else if (!appName.equals(other.appName))
			return false;
		if (malcolmDeviceName == null) {
			if (other.malcolmDeviceName != null)
				return false;
		} else if (!malcolmDeviceName.equals(other.malcolmDeviceName))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (pathToConfig == null) {
			if (other.pathToConfig != null)
				return false;
		} else if (!pathToConfig.equals(other.pathToConfig))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ConfigWrapper [active=" + active + ", name=" + name + ", pathToConfig=" + pathToConfig
				+ ", malcolmDeviceName=" + malcolmDeviceName + ", appName=" + appName + "]";
	}
}
