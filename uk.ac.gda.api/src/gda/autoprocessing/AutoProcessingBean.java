/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package gda.autoprocessing;

import java.io.Serializable;
import java.util.Objects;

/**
 * Bean to hold autoprocessing configuration to be later used
 * in a ProcessingScannable or ProcessingRequest.
 * <p>
 * Config object must serialise as json easily, i.e. a simple map or path
 * to configuration file.
 */
public class AutoProcessingBean implements Serializable {

	private boolean active;
	private String appName;
	private Object config;
	private String displayName;

	/**
	 * No-args constructor is needed for serialization
	 */
	public AutoProcessingBean() {
		this("","");
	}

	public AutoProcessingBean(String appName, Object config) {
		this.appName = appName;
		this.config = config;

		if (config instanceof String) {
			displayName = (String)config;
		}
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getAppName() {
		return appName;
	}

	public Object getConfig() {
		return config;
	}

	public void setConfig(Object config) {
		this.config = config;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	@Override
	public int hashCode() {
		return Objects.hash(active, appName, config, displayName);
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AutoProcessingBean other = (AutoProcessingBean) obj;
		return active == other.active && Objects.equals(appName, other.appName) && Objects.equals(config, other.config)
				&& Objects.equals(displayName, other.displayName);
	}
}
