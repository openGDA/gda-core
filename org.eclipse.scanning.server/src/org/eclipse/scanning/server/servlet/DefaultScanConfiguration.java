/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package org.eclipse.scanning.server.servlet;

import java.util.Collections;
import java.util.Set;

import org.eclipse.scanning.api.event.scan.ScanRequest;

/**
 * Defines scan defaults to are merged with a {@link ScanRequest} on the server.
 */
public class DefaultScanConfiguration {

	private Set<String> defaultPerScanMonitorNames = Collections.emptySet();

	private Set<String> defaultPerPointMonitorNames = Collections.emptySet();

	public Set<String> getDefaultPerScanMonitorNames() {
		return defaultPerScanMonitorNames;
	}

	public void setDefaultPerScanMonitorNames(Set<String> defaultPerScanMonitorNames) {
		this.defaultPerScanMonitorNames = defaultPerScanMonitorNames;
	}

	public Set<String> getDefaultPerPointMonitorNames() {
		return defaultPerPointMonitorNames;
	}

	public void setDefaultPerPointMonitorNames(Set<String> defaultPerPointMonitorNames) {
		this.defaultPerPointMonitorNames = defaultPerPointMonitorNames;
	}

}
