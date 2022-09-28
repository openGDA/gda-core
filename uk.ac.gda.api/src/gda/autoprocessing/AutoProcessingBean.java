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

/**
 * Bean to hold autoprocessing configuration to be later used
 * in a ProcessingScannable or ProcessingRequest.
 * <p>
 * Config object must serialise as json easily, i.e. a simple map or path
 * to configuration file.
 */
public class AutoProcessingBean {

	private boolean active;
	private String appName;
	private Object config;
	private String displayName;


	public AutoProcessingBean(String appName, Object config) {
		this.appName = appName;
		this.config = config;

		if (config instanceof String) {
			displayName = (String)config;
		}
	}

	public String getAppName() {
		return appName;
	}

	public Object getConfig() {
		return config;
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
}
