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
import org.eclipse.scanning.api.points.MapPosition;

import uk.ac.diamond.daq.osgi.OsgiService;

/**
 * Defines scan defaults to be merged with a {@link ScanRequest} on the server.
 */
@OsgiService(DefaultScanConfiguration.class)
public class DefaultScanConfiguration {

	private Set<String> perScanMonitorNames = Collections.emptySet();

	private Set<String> perPointMonitorNames = Collections.emptySet();

	private Set<String> templateFilePaths = Collections.emptySet();

	/**
	 * The position to which one or more devices should be moved at the start of the scan, or null if there is no start
	 * position to move to. This setting will apply to every scan.
	 */
	private MapPosition startPosition;

	/**
	 * The position to which one or more devices should be moved at the end of the scan, or null if there is no start
	 * position to move to. This setting will apply to every scan.
	 */
	private MapPosition endPosition;

	public Set<String> getPerScanMonitorNames() {
		return perScanMonitorNames;
	}

	public void setPerScanMonitorNames(Set<String> perScanMonitorNames) {
		this.perScanMonitorNames = perScanMonitorNames;
	}

	public Set<String> getPerPointMonitorNames() {
		return perPointMonitorNames;
	}

	public void setPerPointMonitorNames(Set<String> perPointMonitorNames) {
		this.perPointMonitorNames = perPointMonitorNames;
	}

	public Set<String> getTemplateFilePaths() {
		return templateFilePaths;
	}

	public void setTemplateFilePaths(Set<String> templateFilePaths) {
		this.templateFilePaths = templateFilePaths;
	}

	public MapPosition getStartPosition() {
		return startPosition;
	}

	public void setStartPosition(MapPosition startPosition) {
		this.startPosition = startPosition;
	}

	public MapPosition getEndPosition() {
		return endPosition;
	}

	public void setEndPosition(MapPosition endPosition) {
		this.endPosition = endPosition;
	}

}
