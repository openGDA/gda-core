/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

package uk.ac.gda.client.live.stream.view;

/**
 * Enum containing all the known stream types
 */
public enum StreamType {
	MJPEG("MJPEG"),
	EPICS_ARRAY("EPICS Array");

	final String displayName;

	StreamType(String displayName) {
		this.displayName = displayName;
	}

	/**
	 * Note: If this is changed, views referenced in user workspaces will no longer be valid.
	 *
	 * @return suffix used to denote which stream is associated with a view.
	 */
	public String secondaryIdSuffix() {
		return "#" + name();
	}

	@Override
	public String toString() {
		return displayName;
	}
}